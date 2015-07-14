package pl.appnode.timeboxer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static pl.appnode.timeboxer.Constants.ALARMS_PREFS_FILE;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION;
import static pl.appnode.timeboxer.Constants.DEFAULT_TIMER_DURATION_MODIFIER;
import static pl.appnode.timeboxer.Constants.MINUTE_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.RESTORE;
import static pl.appnode.timeboxer.Constants.SECOND;
import static pl.appnode.timeboxer.Constants.MINUTE;
import static pl.appnode.timeboxer.Constants.SECOND_IN_MILLIS;
import static pl.appnode.timeboxer.Constants.TIMERS_COUNT;


public class TimersBroadcastService extends Service {

    private List<TimerInfo> createList() {
        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        String alarmPrefix;
        int timeFactor = SECOND_IN_MILLIS;

        List<TimerInfo> result = new ArrayList<TimerInfo>();
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            TimerInfo alarm = new TimerInfo();
            alarmPrefix = "Alarm_" + i;
            alarm.mName = alarmsPrefs.getString(alarmPrefix, "Def Alarm " + i);
            alarm.mDuration = alarmsPrefs.getInt(alarmPrefix + "_Duration", DEFAULT_TIMER_DURATION
                    + (i * DEFAULT_TIMER_DURATION_MODIFIER));
            alarm.mTimeUnit = alarmsPrefs.getInt(alarmPrefix + "_TimeUnit", SECOND);
            switch (alarm.mTimeUnit) {
                case SECOND:  alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
                    timeFactor = SECOND_IN_MILLIS;
                    break;
                case MINUTE:  alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
                    timeFactor = MINUTE_IN_MILLIS;
                    break;
            }
            alarm.mStatus = alarmsPrefs.getInt(alarmPrefix + "_State", 0);
            alarm.mRingtoneUri = alarmsPrefs.getString(alarmPrefix + "_Ringtone", setRingtone());
            alarm.mRingtoneVolume = alarmsPrefs.getInt(alarmPrefix + "_RingtoneVol", setMaxVolume());
            alarm.mFullscreenOff = alarmsPrefs.getBoolean(alarmPrefix + "_FullScreenOff", true);
            alarm.mFinishTime = alarmsPrefs.getLong(alarmPrefix + "_FinishTime", 0);
            if (alarm.mStatus == 1 & alarm.mFinishTime > SystemClock.elapsedRealtime()) {
                int continuation = (int) (((alarm.mFinishTime - SystemClock.elapsedRealtime()) + timeFactor) / timeFactor);
                if (continuation < 100) {
                    alarm.mDurationCounter = continuation;
                    sAlarmState[i - 1] = RESTORE;
                    Log.d(TAG, "Alarm #" + i + " set to RESTORE.");
                }
            } else alarm.mDurationCounter = alarm.mDuration;
            result.add(alarm);
            Log.d(TAG, "Result add #" + i);
        }
        Log.d(TAG, "RETURN!");
        return result;
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
