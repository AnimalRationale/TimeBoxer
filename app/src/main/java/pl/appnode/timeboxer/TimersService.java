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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static pl.appnode.timeboxer.Constants.ACTION_INTENT_HIDE_SWITCHOFF_ACTIVITY;
import static pl.appnode.timeboxer.Constants.ALARMS_PREFS_FILE;
import static pl.appnode.timeboxer.Constants.CANCEL_WAKE_UP_ALARM;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION_MODIFIER;
import static pl.appnode.timeboxer.Constants.EXTRA_COMMAND_SWITCH_OFF_TIMER_ID;
import static pl.appnode.timeboxer.Constants.EXTRA_COMMAND_WAKE_UP_TIMER_ID;
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
import static pl.appnode.timeboxer.Constants.WIDGET_BUTTONS_PROGRESS_BARS;
import static pl.appnode.timeboxer.Constants.WIDGET_BUTTON_ACTION;

/**
 * Controls timers states:
 * - initialises list of timers;
 * - starts, stops and cancels timers;
 * - saves timers settings to persistent storage;
 * - sets up and if needed updates widget;
 *
 * Updates user interface, depending on app's and device
 * current state (no updates if screen is off, no widget updates if app is visible).
 */
public class TimersService extends Service {

    private static final String LOGTAG = "TimersService";
    static List<TimerItem> sTimersList = new ArrayList<>(TIMERS_COUNT);
    private static CustomCountDownTimer[] sTimers = new CustomCountDownTimer[4];
    private static Context sContext;
    private int mOrientation;
    private static RemoteViews sWidgetViews = null;
    private static ComponentName sWidget = null;
    private static AppWidgetManager sWidgetManager = null;
    private static boolean sIsMainActivityVisible = false;
    private static boolean sIsScreenInteractive = true;
    private static boolean sIsFullscreenSwitchOffRunning= false;


    static void setIsMainActivityVisible(boolean sIsMainActivityVisible) {
        TimersService.sIsMainActivityVisible = sIsMainActivityVisible;
    }

    static boolean getIsScreenInteractive() {
        return sIsScreenInteractive;
    }

    public static void setIsFullscreenSwitchOffRunning(boolean sIsFullscreenSwitchOffRunning) {
        TimersService.sIsFullscreenSwitchOffRunning = sIsFullscreenSwitchOffRunning;
    }

    static TimerItem getTimer(int position) {
        if (position > 0 && position < sTimersList.size()) {
            return sTimersList.get(position);
        } else return sTimersList.get(0);
    }

    private final BroadcastReceiver mScreenStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                sIsScreenInteractive = true;
                refreshMinuteTimersAfterScreenOn();
                updateWidget();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                sIsScreenInteractive = false;
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
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        int startMode = START_STICKY;
        int ids[] = AppWidgetManager.getInstance(sContext).getAppWidgetIds(
                new ComponentName(sContext,TimeBoxerWidgetProvider.class));
        if (ids.length != 0) {
            setUpWidget();
        }
        PowerManager device = (PowerManager) AppContextHelper.getContext()
                .getSystemService(Context.POWER_SERVICE);
        sIsScreenInteractive = device.isScreenOn();
        if (startIntent != null && startIntent.hasExtra(EXTRA_COMMAND_WAKE_UP_TIMER_ID)) {
            wakeUpTimer(startIntent.getIntExtra(EXTRA_COMMAND_WAKE_UP_TIMER_ID, 99));
        }
        if (startIntent != null && startIntent.hasExtra(EXTRA_COMMAND_SWITCH_OFF_TIMER_ID)) {
            timerAction(startIntent.getIntExtra(EXTRA_COMMAND_SWITCH_OFF_TIMER_ID, 99));
        }
        return startMode;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenStatusBroadcastReceiver);
    }

    // Initialises timers list, if needed restores state of interrupted timers
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
            Log.d(LOGTAG, "Alarm #" + i + " status: " + timer.mStatus + " / finishTime: " + timer.mFinishTime);
            if (timer.mStatus == RUNNING && timer.mFinishTime != 0) {
                int continuation = (int) (((timer.mFinishTime - SystemClock.elapsedRealtime())
                        + timeFactor) / timeFactor);
                Log.d(LOGTAG, "Alarm #" + i + " status RUNNING, continuation: " + continuation
                        + " / finishTime: " + timer.mFinishTime);
                if (continuation < 100) {
                    timer.mDurationCounter = continuation;
                    timer.mStatus = RESTORE;
                    Log.d(LOGTAG, "Alarm #" + i + " restored with duration: " + continuation);
                }
            } else {
                timer.mDurationCounter = timer.mDuration;
                timer.mStatus = IDLE;
            }
            sTimersList.add(timer);
            Log.d(LOGTAG, "Timer #" + i + " added to list.");
            if (timer.mStatus == RESTORE ) {
                startTimer( i - 1 );
                Log.d(LOGTAG, "Timer #" + i + " restored.");
            }
        }
        Log.d(LOGTAG, "TimersList created.");
    }

    /**
     * Saves all timers settings to persistent storage (shared preferences).
     */
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
        }
        editor.apply();
    }

    /**
     * Saves given timer duration time to persistent storage (shared preferences).
     *
     * @param position timer's id (position on timers list)
     */
    public static void saveTimerDuration(int position) {
        SharedPreferences timersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = timersPrefs.edit();
        String timerPrefix = TIMER_PREFIX + (position + 1);
        TimerItem timer = sTimersList.get(position);
        editor.putInt(timerPrefix + PREFS_DURATION, timer.mDuration);
        editor.apply();
    }

    /**
     * Saves given timer current status to persistent storage (shared preferences), used
     * in case of restoring timers (eg. after timer's service force close by system).
     *
     * @param position timer's id (position on timers list)
     */
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
        Log.d(LOGTAG, "Saved timer #" + position + " status: " + timer.mStatus + " / finishTime: "
                + timer.mFinishTime);
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

    // Returns given device maximum volume level for alarm ringtone
    private static int setMaxVolume() {
        AudioManager audioManager = (AudioManager) AppContextHelper
                .getContext().getSystemService(AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
    }

    /**
     * Performs proper action on timer, depending on given timer's current status
     * - starts timer if current status is idle;
     * - stops timer if current status is running;
     *
     * @param position timer's id (position on timers list)
     */
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
            Log.d(LOGTAG, "Timer start: #" + position);
        }
        int timeUnitFactor;
        if (timer.mTimeUnit == SECOND) {
            timeUnitFactor = SECOND_IN_MILLIS;
        } else {timeUnitFactor = (MINUTE_IN_MILLIS);}
        timer.mFinishTime = SystemClock.elapsedRealtime() + (timer.mDurationCounter * timeUnitFactor);
        Calendar calendar = GregorianCalendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        String separator;
        if (minutes < 10) {
            separator = ":0";
        } else separator = ":";
        timer.mTimeOfStart = hour + separator + minutes;
        Log.d(LOGTAG, "Calendar time of start: " + timer.mTimeOfStart);
        sTimers[position] = new CustomCountDownTimer(timer.mDurationCounter * timeUnitFactor,
                timeUnitFactor - (timeUnitFactor / TIME_DEVIATION_FOR_LAST_TICK),
                position, timeUnitFactor);
        Log.d(LOGTAG, "CustomCDT #" + position + " started for: " + timer.mDurationCounter * timeUnitFactor + ", "
                + (timeUnitFactor - (timeUnitFactor / TIME_DEVIATION_FOR_LAST_TICK)));
        sTimers[position].start();
        saveTimerStatus(position);
    }

    private static void stopTimer(int position) {
        TimerItem timer = sTimersList.get(position);
        if (sTimers[position] != null) {
            sTimers[position].stopRingtone();
            sTimers[position].cancel();
            sTimers[position] = null;
            WakeUpAlarmHelper.alarmManager(0l, position, CANCEL_WAKE_UP_ALARM);
        }
        if (sIsFullscreenSwitchOffRunning) {
            Intent hideFullscreenOffIntent = new Intent(ACTION_INTENT_HIDE_SWITCHOFF_ACTIVITY);
            LocalBroadcastManager.getInstance(AppContextHelper.getContext())
                    .sendBroadcast(hideFullscreenOffIntent);
            Log.d(LOGTAG, "Sending hide intent: " + hideFullscreenOffIntent.toString());
        }
        timer.mStatus = IDLE;
        timer.mFinishTime = 0;
        timer.mDurationCounter = timer.mDuration;
        saveTimerStatus(position);
        NotificationManager notificationManager =
                (NotificationManager) AppContextHelper.getContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(position);
        if (MainActivity.mTimersAdapter != null) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
            Log.d(LOGTAG, "Timer stop: #" + position);
        }
        updateWidget();
    }

    /**
     * Updates user interface, if timers list is visible then proper element on timers list,
     * otherwise updates widget.
     *
     * @param position timer's id (position on timers list)
     */
    static void updateTime(int position) {
        if (sIsMainActivityVisible) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
        } else {
            updateWidget();
        }
    }

    /**
     * Handles given timer's finish.
     *
     * @param position timer's id (position on timers list)
     */
    static void finishTimer(int position) {
        TimerItem timer = sTimersList.get(position);
        timer.mDurationCounter = 0;
        timer.mStatus = FINISHED;
        timer.mFinishTime = 0;
        saveTimerStatus(position);
        if (sIsMainActivityVisible) {
            MainActivity.mTimersAdapter.notifyItemChanged(position);
        }
        updateWidget();
        if (timer.mFullscreenSwitchOff) {
            showFullscreenSwitchOff(position + 1, timer.mName);
        }
        Log.d(LOGTAG, "Timer finished: #" + position);
    }

    private static void getWidget(Context context) {
        sWidgetViews = new RemoteViews(context.getPackageName(), R.layout.widget_timeboxer);
        sWidget = new ComponentName(context, TimeBoxerWidgetProvider.class);
        sWidgetManager = AppWidgetManager.getInstance(context);
    }

    /**
     * Updates widget.
     */
    static void updateWidget() {
        int ids[] = AppWidgetManager.getInstance(sContext)
                .getAppWidgetIds(new ComponentName(sContext, TimeBoxerWidgetProvider.class));
        if (ids.length != 0) {
            setUpWidget();
        }
    }

    // Provides full structure of widget (proper button assigns and states) - on every update
    // widget have to be completely rebuild.
    private static void setUpWidget() {
        getWidget(sContext);
        assignWidgetButtons(sContext);
        setUpWidgetFromTimersList();
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
        Log.d(LOGTAG, "Widget updated.");
    }

    private static void setUpWidgetFromTimersList() {
        for (int i = 0; i < TIMERS_COUNT; i++) {
            TimerItem timer = sTimersList.get(i);
            switch (timer.mStatus) {
                case IDLE:
                                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource",
                                        R.drawable.round_button_green);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setBackgroundResource", R.drawable.round_button_green);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setMax", timer.mDuration);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setProgress", 0);
                                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1],
                                        timer.mDuration + timer.mTimeUnitSymbol);
                                break;

                case RUNNING:
                                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource",
                                        R.drawable.round_button_orange);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setBackgroundResource", R.drawable.round_button_red);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setMax", timer.mDuration);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setProgress", timer.mDurationCounter);
                                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1],
                                        timer.mDurationCounter + timer.mTimeUnitSymbol);
                                break;

                case FINISHED:
                                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource",
                                        R.drawable.round_button_red);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setBackgroundResource", R.drawable.round_button_red);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setMax", timer.mDuration);
                                sWidgetViews.setInt(WIDGET_BUTTONS_PROGRESS_BARS[i + 1],
                                        "setProgress", 0);
                                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1],
                                        timer.mDurationCounter + timer.mTimeUnitSymbol);
                                break;
                default:
                                break;
            }
        }
    }

    private static void assignWidgetButtons(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[0], pendingIntent);
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[i],
                    getPendingSelfIntent(context, WIDGET_BUTTON_ACTION[i]));
        }
    }

    private static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, TimeBoxerWidgetProvider.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig.orientation != mOrientation)
        {
            mOrientation = newConfig.orientation;
            setUpWidget();
        }
    }

    private static void showFullscreenSwitchOff(int timerId, String timerName) {
        Intent intent = new Intent(sContext, FullscreenSwitchOffActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("AlarmID", timerId);
        intent.putExtra("AlarmName", timerName);
        sContext.startActivity(intent);
    }

    private void refreshMinuteTimersAfterScreenOn() {
        int i = 0;
        for (TimerItem timer : sTimersList) {
            if (sTimers[i] != null && timer.mTimeUnit == MINUTE) {
                if (timer.mFinishTime > SystemClock.elapsedRealtime()) {
                    sTimers[i].onTick(timer.mFinishTime - SystemClock.elapsedRealtime());
                }
            }
            i++;
        }
    }

    private void wakeUpTimer(int timerId) {
        TimerItem timer = sTimersList.get(timerId);
        if (sTimers[timerId] != null && timer.mTimeUnit == MINUTE) {
            sTimers[timerId].cancel();
            sTimers[timerId].onFinish();
        }
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}