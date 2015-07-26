package pl.appnode.timeboxer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

import static pl.appnode.timeboxer.Constants.ALARMS_PREFS_FILE;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION_MODIFIER;
import static pl.appnode.timeboxer.Constants.FINISHED;
import static pl.appnode.timeboxer.Constants.IDLE;
import static pl.appnode.timeboxer.Constants.MINUTE_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_NAME;
import static pl.appnode.timeboxer.Constants.PREFS_DURATION;
import static pl.appnode.timeboxer.Constants.PREFS_FINISHTIME;
import static pl.appnode.timeboxer.Constants.PREFS_FULLSCREEN_OFF;
import static pl.appnode.timeboxer.Constants.PREFS_RINGTONE;
import static pl.appnode.timeboxer.Constants.PREFS_RINGTONE_VOL;
import static pl.appnode.timeboxer.Constants.PREFS_STATE;
import static pl.appnode.timeboxer.Constants.PREFS_TIME_UNIT;
import static pl.appnode.timeboxer.Constants.RESTORE;
import static pl.appnode.timeboxer.Constants.RUNNING;
import static pl.appnode.timeboxer.Constants.SECOND;
import static pl.appnode.timeboxer.Constants.MINUTE;
import static pl.appnode.timeboxer.Constants.SECOND_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.TIME_DEVIATION_FOR_LAST_TICK;
import static pl.appnode.timeboxer.Constants.TIMERS_COUNT;
import static pl.appnode.timeboxer.Constants.TIMER_PREFIX;
import static pl.appnode.timeboxer.Constants.WIDGET_BUTTONS;
import static pl.appnode.timeboxer.Constants.WIDGET_BUTTON_ACTION;

public class TimersService extends Service {

    private static final String TAG = "TimersService";
    protected static List<TimerItem> sTimersList = new ArrayList<>(TIMERS_COUNT);
    private static CustomCountDownTimer[] sTimers = new CustomCountDownTimer[4];
    private static Context sContext;
    private int mOrientation;
    private static RemoteViews sWidgetViews = null;
    private static ComponentName sWidget = null;
    private static AppWidgetManager sWidgetManager = null;
    protected static boolean sIsMainActivityVisible = false;
    protected static boolean sIsScreenInteractive = true;


    private BroadcastReceiver mScreenStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                sIsScreenInteractive = true;
                updateWidget();
                Log.d(TAG, "Receiver for SCREEN_ON.");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                sIsScreenInteractive = false;
                Log.d(TAG, "Receiver for SCREEN_OFF.");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createTimersList();
        MainActivity.sIsTimersBroadcastService = true;
        mOrientation = this.getResources().getConfiguration().orientation;
        sContext = AppContextHelper.getContext();
        IntentFilter screenStatusIntentFilter = new IntentFilter();
        screenStatusIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStatusIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStatusBroadcastReceiver, new IntentFilter(screenStatusIntentFilter));
        Log.d(TAG, "Creating timers service.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startMode = START_STICKY;
        int ids[] = AppWidgetManager.getInstance(sContext).getAppWidgetIds(new ComponentName(sContext,TimeBoxerWidgetProvider.class));
        if (ids.length != 0) {
            setUpWidget();
        }
        PowerManager device = (PowerManager) AppContextHelper.getContext().getSystemService(Context.POWER_SERVICE);
        sIsScreenInteractive = device.isScreenOn();
        Log.d(TAG, "Starting timers service.");
        return startMode;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenStatusBroadcastReceiver);
    }

    private void createTimersList() {
        SharedPreferences timersPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        int timeFactor = SECOND_IN_MILLIS;
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            TimerItem timer = new TimerItem();
            String timerPrefix = TIMER_PREFIX + i;
            timer.mName = timersPrefs.getString(timerPrefix, DEFAULT_TIMER_NAME + i);
            timer.mDuration = timersPrefs.getInt(timerPrefix + PREFS_DURATION, DEFAULT_TIMER_DURATION
                    + (i * DEFAULT_TIMER_DURATION_MODIFIER));
            timer.mTimeUnit = timersPrefs.getInt(timerPrefix + PREFS_TIME_UNIT, SECOND);
            switch (timer.mTimeUnit) {
                case SECOND:  timer.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
                    timeFactor = SECOND_IN_MILLIS;
                    break;
                case MINUTE:  timer.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
                    timeFactor = MINUTE_IN_MILLIS;
                    break;
            }
            timer.mStatus = timersPrefs.getInt(timerPrefix + PREFS_STATE, 0);
            timer.mRingtoneUri = timersPrefs.getString(timerPrefix + PREFS_RINGTONE, setRingtone());
            timer.mRingtoneVolume = timersPrefs.getInt(timerPrefix + PREFS_RINGTONE_VOL, setMaxVolume());
            timer.mFullscreenSwitchOff = timersPrefs.getBoolean(timerPrefix + PREFS_FULLSCREEN_OFF, true);
            timer.mFinishTime = timersPrefs.getLong(timerPrefix + PREFS_FINISHTIME, 0);
            Log.d(TAG, "Alarm #" + i + " status: " + timer.mStatus + " / finishTime: " + timer.mFinishTime);
            if (timer.mStatus == RUNNING && timer.mFinishTime != 0) {
                int continuation = (int) (((timer.mFinishTime - SystemClock.elapsedRealtime())
                        + timeFactor) / timeFactor);
                Log.d(TAG, "Alarm #" + i + " status RUNNING, continuation: " + continuation + " / finishTime: " + timer.mFinishTime);
                if (continuation < 100) {
                    timer.mDurationCounter = continuation;
                    timer.mStatus = RESTORE;
                    Log.d(TAG, "Alarm #" + i + " restored with duration: " + continuation);
                }
            } else {
                timer.mDurationCounter = timer.mDuration;
                timer.mStatus = IDLE;
            }
            sTimersList.add(timer);
            Log.d(TAG, "Timer #" + i + " added to list.");
            if (timer.mStatus == RESTORE ) {
                startTimer( i - 1 );
                Log.d(TAG, "Timer #" + i + " restored.");
            }
        }
        Log.d(TAG, "TimersList created.");
    }

    public static void saveSharedPrefs() {
        SharedPreferences timersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = timersPrefs.edit();
        for (int i = 0; i < TIMERS_COUNT; i++) {
            String timerPrefix = TIMER_PREFIX + (i + 1);
            TimerItem timer = sTimersList.get(i);
            editor.putString(timerPrefix, timer.mName);
            editor.putInt(timerPrefix + PREFS_DURATION, timer.mDuration);
            editor.putInt(timerPrefix + PREFS_TIME_UNIT, timer.mTimeUnit);
            editor.putInt(timerPrefix + PREFS_STATE, timer.mStatus);
            if (timer.mStatus == RUNNING) {
                editor.putLong(timerPrefix + PREFS_FINISHTIME, timer.mFinishTime);
            } else editor.putLong(timerPrefix + PREFS_FINISHTIME, 0);
            editor.putString(timerPrefix + PREFS_RINGTONE, timer.mRingtoneUri);
            editor.putInt(timerPrefix + PREFS_RINGTONE_VOL, timer.mRingtoneVolume);
            editor.putBoolean(timerPrefix + PREFS_FULLSCREEN_OFF, timer.mFullscreenSwitchOff);
            Log.d(TAG, "Create SharedPrefs: " + timerPrefix + ": " + timer.mDuration
                    + ": TimeUnit: " + timer.mTimeUnitSymbol
                    + " :: Status: " + timer.mStatus + " Vol: " + timer.mRingtoneVolume
                    + " FSOFF: " + timer.mFullscreenSwitchOff);
        }
        editor.apply();
        Log.d(TAG, "SharedPrefs saved.");
    }

    public static void saveTimerDuration(int position) {
        SharedPreferences timersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = timersPrefs.edit();
        String timerPrefix = TIMER_PREFIX + (position + 1);
        TimerItem timer = sTimersList.get(position);
        editor.putInt(timerPrefix + PREFS_DURATION, timer.mDuration);
        editor.apply();
        Log.d(TAG, "Saved timer #" + position + " duration: " + timer.mDuration);
    }

    private static void saveTimerStatus(int position) {
        SharedPreferences timersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = timersPrefs.edit();
        String timerPrefix = TIMER_PREFIX + (position + 1);
        TimerItem timer = sTimersList.get(position);
        editor.putInt(timerPrefix + PREFS_STATE, timer.mStatus);
        if (timer.mStatus == RUNNING) {
            editor.putLong(timerPrefix + PREFS_FINISHTIME, timer.mFinishTime);
        } else editor.putLong(timerPrefix + PREFS_FINISHTIME, 0);
        Log.d(TAG, "Saved timer #" + position + " status: " + timer.mStatus + " / finishTime: " + timer.mFinishTime);
        editor.apply();
    }

    private static String setRingtone() {
        Uri ringtoneUri;
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (ringtoneUri == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return ringtoneUri.toString();
    }

    private static int setMaxVolume() {
        AudioManager audioManager = (AudioManager) AppContextHelper
                .getContext().getSystemService(AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
    }

    public static void timerAction (int position) {
        TimerItem timer = sTimersList.get(position);
        if (timer.mStatus == RUNNING || timer.mStatus == FINISHED) {
            stopTimer(position);
        } else if (timer.mStatus == IDLE) {
            startTimer(position);
        }
    }

    private static void startTimer(int position) {
        TimerItem timer = sTimersList.get(position);
        timer.mStatus = RUNNING;
        if (MainActivity.mTimersAdapter != null) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
            Log.d(TAG, "Timer start: #" + position);
        }
        int timeUnitFactor;
        if (timer.mTimeUnit == SECOND) {
            timeUnitFactor = SECOND_IN_MILLIS;
        } else {timeUnitFactor = (MINUTE_IN_MILLIS);}
        timer.mFinishTime = SystemClock.elapsedRealtime() + (timer.mDurationCounter * timeUnitFactor);
        sTimers[position] = new CustomCountDownTimer(timer.mDurationCounter * timeUnitFactor,
                timeUnitFactor - (timeUnitFactor / TIME_DEVIATION_FOR_LAST_TICK), position, timeUnitFactor);
        Log.d(TAG, "CustomCDT #" + position + " started for: " + timer.mDurationCounter * timeUnitFactor + ", "
                + (timeUnitFactor - (timeUnitFactor / TIME_DEVIATION_FOR_LAST_TICK)));
        sTimers[position].start();
        saveTimerStatus(position);
    }

    private static void stopTimer(int position) {
        TimerItem timer = sTimersList.get(position);
        if (sTimers[position] != null) {
            sTimers[position].stopRingtone();
            if (timer.mStatus != FINISHED) {
                sTimers[position].cancelAlarmManagerWakeUp();
            }
            sTimers[position].cancel();
            sTimers[position] = null;
        }
        timer.mStatus = IDLE;
        timer.mFinishTime = 0;
        timer.mDurationCounter = timer.mDuration;
        saveTimerStatus(position);
        NotificationManager notificationManager =
                (NotificationManager) AppContextHelper.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(position);
        if (MainActivity.mTimersAdapter != null) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
            Log.d(TAG, "Timer stop: #" + position);
        }
        updateWidget();
    }

    protected static void updateTime(int position, int timeToFinish) {
        TimerItem timer = sTimersList.get(position);
        timer.mDurationCounter = timeToFinish;
        if (sIsMainActivityVisible) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
        } else {
            updateWidget();
        }
    }

    protected static void finishTimer(int position) {
        TimerItem timer = sTimersList.get(position);
        timer.mDurationCounter = 0;
        timer.mStatus = FINISHED;
        timer.mFinishTime = 0;
        saveTimerStatus(position);
        if (sIsMainActivityVisible) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
        }
        updateWidget();
        Log.d(TAG, "Timer finished: #" + position);
    }

    private static void getWidget(Context context) {
        sWidgetViews = new RemoteViews(context.getPackageName(), R.layout.widget_timeboxer);
        sWidget = new ComponentName(context, TimeBoxerWidgetProvider.class);
        sWidgetManager = AppWidgetManager.getInstance(context);
    }

    protected static void updateWidget() {
        int ids[] = AppWidgetManager.getInstance(sContext)
                .getAppWidgetIds(new ComponentName(sContext, TimeBoxerWidgetProvider.class));
        if (ids.length != 0) {
            setUpWidget();
        }
    }

    private static void setUpWidget() {
        getWidget(sContext);
        assignWidgetButtons(sContext);
        setUpWidgetFromTimersList();
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
        Log.d(TAG, "Widget updated.");
    }

    private static void setUpWidgetFromTimersList() {
        for (int i = 0; i < TIMERS_COUNT; i++) {
            TimerItem timer = sTimersList.get(i);
            if (timer.mStatus == RUNNING) {
                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource", R.drawable.round_button_orange);
                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1], timer.mDurationCounter + timer.mTimeUnitSymbol);
            } else if (timer.mStatus == IDLE) {
                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource", R.drawable.round_button_green);
                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1], timer.mDuration + timer.mTimeUnitSymbol);
            } else if (timer.mStatus == FINISHED) {
                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource", R.drawable.round_button_red);
                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1], timer.mDurationCounter + timer.mTimeUnitSymbol);
            }
        }
    }

    private static void assignWidgetButtons(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[0], pendingIntent);
        Log.d(TAG, "Reassigning widget app button.");
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[i], getPendingSelfIntent(context, WIDGET_BUTTON_ACTION[i]));
            Log.d(TAG, "Reassigning timer #" + i + " widget button for action: " + WIDGET_BUTTON_ACTION[i]);
        }
    }

    private static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, TimeBoxerWidgetProvider.class);
        intent.setAction(action);
        Log.d(TAG, "WidgetSetUp Service pendingSelfIntent for action: " + action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Configuration change.");
        if(newConfig.orientation != mOrientation)
        {
            mOrientation = newConfig.orientation;
            Log.d(TAG, "Orientation change.");
            setUpWidget();
        }
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}