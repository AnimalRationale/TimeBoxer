package pl.appnode.timeboxer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import static pl.appnode.timeboxer.Constants.EXTRA_COMMAND_WAKE_UP_TIMER_ID;
import static pl.appnode.timeboxer.Constants.EXTRA_TIMER_ID;
import static pl.appnode.timeboxer.Constants.TIMERS_COUNT;
import static pl.appnode.timeboxer.Constants.WAKE_LOCK_TIME_OUT;

/**
 * Receives wake up alarm set for {@value #WAKE_UP_MARGIN} before every timer finish
 * and acquires partial wake lock for {@value #WAKE_LOCK_TIME_OUT} period.
 */
public class WakeUpAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "WakeUpAlarmReceiver";
    private static final String LOCK_TAG = "pl.appnode.timeboxer";
    private static PowerManager.WakeLock[] sWakeLocks = new PowerManager.WakeLock[4];

    private static synchronized void acquireWakeLock (Context context, int timerId) {
        if (sWakeLocks[timerId] == null) {
            Log.d(TAG, "Wake lock null, setting up for timer #" + timerId);
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sWakeLocks[timerId] = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
            sWakeLocks[timerId].setReferenceCounted(false);
        }
        sWakeLocks[timerId].acquire(WAKE_LOCK_TIME_OUT);
        Log.d(TAG, "Wake lock acquired for timer #" + timerId);
    }

    public static synchronized void releaseLock(int timerId) {
        if (sWakeLocks[timerId] != null) {
            Log.d(TAG, "Releasing wake lock for timer #" + timerId);
            try {
                sWakeLocks[timerId].release();
                Log.d(TAG, "Wake lock released for timer #" + timerId);
            } catch (Throwable thex) {
                Log.d(TAG, "Wake lock exception catch for timer #" + timerId + " - msg:"
                        + thex.getMessage());
                // wakeLock should be already released
            }
        } else {
            Log.d(TAG, "Wakelock null - timer #" + timerId);
        }
    }

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        int timerId = alarmIntent.getIntExtra(EXTRA_TIMER_ID, 99);
        if (timerId >= 0 & timerId < TIMERS_COUNT) {
            acquireWakeLock(context, timerId);
            Intent serviceIntent = new Intent(context, TimersService.class);
            serviceIntent.putExtra(EXTRA_COMMAND_WAKE_UP_TIMER_ID, timerId);
            context.startService(serviceIntent);
            Log.d(TAG, "Starting service after wake lock for timer#" + timerId);
        } else Log.d(TAG, "Invalid timer ID, wake lock not acquired, ID:" + timerId);
    }
}