package pl.appnode.timeboxer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;

public class CustomCountDownTimer extends CountDownTimer {

    private int mTimeUnitFactor;
    private int mTimerId;
    private NotificationManager mNM ;
    private NotificationCompat.Builder mNotify;
    Context context = AppContextHelper.getContext();

    public CustomCountDownTimer (long millisInFuture, long countDownInterval, int position, int timeUnitFactor) {
        super(millisInFuture, countDownInterval);
        mTimerId = position;
        mTimeUnitFactor = timeUnitFactor;
        notificationStart();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        notificationUpdate();
        TimersBroadcastService.updateTime(mTimerId, (int) millisUntilFinished / mTimeUnitFactor);
    }

    @Override
    public void onFinish() {
        TimersBroadcastService.finishTimer(mTimerId);
    }

    private void notificationStart () {
        TimerItem timer = TimersBroadcastService.sTimersList.get(mTimerId);
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
        mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify = new NotificationCompat.Builder(context)
                .setContentTitle(timer.mDuration
                        + context.getResources().getString(R.string.notification_title))
                .setContentText(timer.mName
                        + context.getResources().getString(R.string.notification_text02)
                        + timer.mDuration + timer.mTimeUnit
                        + context.getResources().getString(R.string.notification_text03))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(resultPendingIntent);
        mNM.notify(mTimerId, mNotify.build());
    }

    private void notificationUpdate() {
        TimerItem timer = TimersBroadcastService.sTimersList.get(mTimerId);
        mNotify.setContentTitle(timer.mDurationCounter + timer.mTimeUnit
                + context.getResources().getString(R.string.notification_title));
        mNM.notify(mTimerId, mNotify.build());
    }
}