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
import static pl.appnode.timeboxer.Constants.MINUTE_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.RUNNING;
import static pl.appnode.timeboxer.Constants.SECOND;
import static pl.appnode.timeboxer.Constants.MINUTE;
import static pl.appnode.timeboxer.Constants.SECOND_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.TIMERS_COUNT;


public class TimersBroadcastService extends Service {

    private static final String TAG = "TimersBroadcastService";

    private List<TimerInfo> createList() {
        SharedPreferences timersPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        String alarmPrefix;
        int timeFactor = SECOND_IN_MILLIS;
        List<TimerInfo> timersList = new ArrayList<TimerInfo>();
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            TimerInfo timer = new TimerInfo();
            alarmPrefix = "Timer_" + i;
            timer.mName = timersPrefs.getString(alarmPrefix, "Timer " + i);
            timer.mDuration = timersPrefs.getInt(alarmPrefix + "_Duration", DEFAULT_TIMER_DURATION
                    + (i * DEFAULT_TIMER_DURATION_MODIFIER));
            timer.mTimeUnit = timersPrefs.getInt(alarmPrefix + "_TimeUnit", SECOND);
            switch (timer.mTimeUnit) {
                case SECOND:  timer.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
                    timeFactor = SECOND_IN_MILLIS;
                    break;
                case MINUTE:  timer.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
                    timeFactor = MINUTE_IN_MILLIS;
                    break;
            }
            timer.mStatus = timersPrefs.getInt(alarmPrefix + "_State", 0);
            timer.mRingtoneUri = timersPrefs.getString(alarmPrefix + "_Ringtone", setRingtone());
            timer.mRingtoneVolume = timersPrefs.getInt(alarmPrefix + "_RingtoneVol", setMaxVolume());
            timer.mFullscreenSwitchOff = timersPrefs.getBoolean(alarmPrefix + "_FullScreenOff", true);
            timer.mFinishTime = timersPrefs.getLong(alarmPrefix + "_FinishTime", 0);
            if (timer.mStatus == RUNNING & timer.mFinishTime > SystemClock.elapsedRealtime()) {
                int continuation = (int) (((timer.mFinishTime - SystemClock.elapsedRealtime()) + timeFactor) / timeFactor);
                if (continuation < 100) {
                    timer.mDurationCounter = continuation;
                    // TODO start timer for continuation
                    Log.d(TAG, "Alarm #" + i + " restored with duration: " + continuation);
                }
            } else timer.mDurationCounter = timer.mDuration;
            timersList.add(timer);
            Log.d(TAG, "List of timers #" + i);
        }
        Log.d(TAG, "List created.");
        return timersList;
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
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}