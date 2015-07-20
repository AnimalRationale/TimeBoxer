package pl.appnode.timeboxer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static pl.appnode.timeboxer.Constants.ALARMS_PREFS_FILE;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION_MODIFIER;
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
import static pl.appnode.timeboxer.Constants.RUNNING;
import static pl.appnode.timeboxer.Constants.SECOND;
import static pl.appnode.timeboxer.Constants.MINUTE;
import static pl.appnode.timeboxer.Constants.SECOND_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.TIMERS_COUNT;
import static pl.appnode.timeboxer.Constants.TIMER_PREFIX;


public class TimersBroadcastService extends Service {

    private static final String TAG = "TimersBroadcastService";
    protected static List<TimerItem> sTimersList = new ArrayList<>(TIMERS_COUNT);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startMode = START_STICKY;
        createTimersList();
        Log.d(TAG, "Starting broadcast service.");
        MainActivity.sIsTimersBroadcastService = true;
        return startMode;
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
            if (timer.mStatus == RUNNING & timer.mFinishTime > SystemClock.elapsedRealtime()) {
                int continuation = (int) (((timer.mFinishTime - SystemClock.elapsedRealtime()) + timeFactor) / timeFactor);
                if (continuation < 100) {
                    timer.mDurationCounter = continuation;
                    // TODO start timer for continuation
                    Log.d(TAG, "Alarm #" + i + " restored with duration: " + continuation);
                }
            } else timer.mDurationCounter = timer.mDuration;
            sTimersList.add(timer);
            Log.d(TAG, "Timer #" + i + " added to list.");
        }
        Log.d(TAG, "TimersList created.");
    }

    public static void saveSharedPrefs() {
        SharedPreferences timersPrefs = AppContextHelper.getContext().getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
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
                    + " :: Status: " + timer.mStatus + " Vol: " + timer.mRingtoneVolume + " FSOFF: " + timer.mFullscreenSwitchOff);
        }
        editor.apply();
        Log.d(TAG, "SharedPrefs saved.");
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
        AudioManager audioManager = (AudioManager) AppContextHelper.getContext().getSystemService(AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
    }

    public static void timerAction (int position) {
        TimerItem timer = sTimersList.get(position);
        if (timer.mStatus == RUNNING) {
            timer.mStatus = IDLE;
            stopTimer(position);
        } else if (timer.mStatus == IDLE) {
            timer.mStatus = RUNNING;
            startTimer(position);
        }
    }

    private static void stopTimer(int position) {
        MainActivity.mTimersAdapter.notifyItemChanged(position);
    }

    private static void startTimer(int position) {
        MainActivity.mTimersAdapter.notifyItemChanged(position);
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}