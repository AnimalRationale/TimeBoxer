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
    Context mContext = AppContextHelper.getContext();
    TimerItem mTimer;

    public CustomCountDownTimer (long millisInFuture, long countDownInterval, int position, int timeUnitFactor) {
        super(millisInFuture, countDownInterval);
        mTimerId = position;
        mTimer = TimersBroadcastService.sTimersList.get(mTimerId);
        mTimeUnitFactor = timeUnitFactor;
        notificationStart();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        TimersBroadcastService.updateTime(mTimerId, (int) millisUntilFinished / mTimeUnitFactor);
        notificationUpdate();
    }

    @Override
    public void onFinish() {
        notificationFinish();
        TimersBroadcastService.finishTimer(mTimerId);
    }

    private void notificationStart () {
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, 0);
        mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify = new NotificationCompat.Builder(mContext)
                .setContentTitle(mTimer.mDuration
                        + mContext.getResources().getString(R.string.notification_title))
                .setContentText(mTimer.mName
                        + mContext.getResources().getString(R.string.notification_text02)
                        + mTimer.mDuration + mTimer.mTimeUnitSymbol
                        + mContext.getResources().getString(R.string.notification_text03))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(resultPendingIntent);
        mNM.notify(mTimerId, mNotify.build());
    }

    private void notificationUpdate() {
        mNotify.setContentTitle(mTimer.mDurationCounter + mTimer.mTimeUnitSymbol
                + mContext.getResources().getString(R.string.notification_title));
        mNM.notify(mTimerId, mNotify.build());
    }

    private void notificationFinish() {
        mNotify.setContentTitle(mTimer.mName
                + mContext.getResources().getString(R.string.notification_text02)
                + mTimer.mDuration + mTimer.mTimeUnitSymbol
                + mContext.getResources().getString(R.string.notification_text03_finished))
                .setContentText(mContext.getResources().getString(R.string.notification_text_finished))
                .setSmallIcon(R.mipmap.ic_launcher);
        mNM.notify(mTimerId, mNotify.build());
    }
}