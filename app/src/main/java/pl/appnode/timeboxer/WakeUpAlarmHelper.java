package pl.appnode.timeboxer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import static pl.appnode.timeboxer.Constants.CANCEL_WAKE_UP_ALARM;
import static pl.appnode.timeboxer.Constants.SET_WAKE_UP_ALARM;
import static pl.appnode.timeboxer.Constants.WAKE_UP_MARGIN;

/**
 * Sets or cancels WakeUpAlarm for acquiring wake lock on timer's finish.
 */

public class WakeUpAlarmHelper {

    public static void alarmManagerWakeUp (Long timerDuration, int timerId, int command) {
        Intent alarmIntent = new Intent(AppContextHelper.getContext(), WakeUpAlarmReceiver.class);
        alarmIntent.setData(Uri.parse("TimerID://" + timerId));
        alarmIntent.setAction(String.valueOf(timerId));
        PendingIntent alarmWakeIntent = PendingIntent.getBroadcast(
                AppContextHelper.getContext(), 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) AppContextHelper.getContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (command == SET_WAKE_UP_ALARM) {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    timerDuration - WAKE_UP_MARGIN,
                    alarmWakeIntent);
        } else if (command == CANCEL_WAKE_UP_ALARM) {
            alarmManager.cancel(alarmWakeIntent);
        }
    }
}
