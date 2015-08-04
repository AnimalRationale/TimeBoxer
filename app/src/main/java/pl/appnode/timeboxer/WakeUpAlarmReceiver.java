package pl.appnode.timeboxer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import static pl.appnode.timeboxer.Constants.ACTION_TIMER_WAKE_UP;
import static pl.appnode.timeboxer.Constants.EXTRA_TIMER_ID;


public class WakeUpAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "WakeUpAlarmReceiver";
    private static final String LOCK_TAG = "pl.appnode.timeboxer";
    private static PowerManager.WakeLock sWakeLock = null;

    private static synchronized void acquireWakeLock (Context context) {
        if (sWakeLock == null) {
            Log.d(TAG, "Wake lock == null, setting up.");
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
            sWakeLock.setReferenceCounted(true);
        }
        sWakeLock.acquire();
        Log.d(TAG, "Wake lock acquired.");
    }

    public static synchronized void releaseLock() {
        if (sWakeLock != null) {
            Log.d(TAG, "Releasing wakelock.");
            try {
                sWakeLock.release();
                Log.d(TAG, "Wake lock released.");
            } catch (Throwable thex) {
                Log.d(TAG, "Wake lock exception catch.");
                // wakeLock should be already released
            }
        } else {
            Log.d(TAG, "Wakelock null.");
        }
    }

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        if (alarmIntent.getAction() == ACTION_TIMER_WAKE_UP) {
            acquireWakeLock(context);
            Log.d(TAG, "Timer wake up for timer #" + alarmIntent.getIntExtra(EXTRA_TIMER_ID, 99));
            Intent serviceIntent = new Intent(context, TimersService.class);
            context.startService(serviceIntent);
            Log.d(TAG, "Starting service.");
        }
    }
}