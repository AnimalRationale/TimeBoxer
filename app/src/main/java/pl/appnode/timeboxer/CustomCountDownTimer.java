package pl.appnode.timeboxer;

import android.os.CountDownTimer;

public class CustomCountDownTimer extends CountDownTimer {

    private int mTimeUnitFactor;
    private int mTimerId;

    public CustomCountDownTimer (long millisInFuture, long countDownInterval, int position, int timeUnitFactor) {
        super(millisInFuture, countDownInterval);
        mTimerId = position;
        mTimeUnitFactor = timeUnitFactor;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        TimersBroadcastService.updateTime(mTimerId, (int) millisUntilFinished / mTimeUnitFactor);
    }

    @Override
    public void onFinish() {
        TimersBroadcastService.finishTimer(mTimerId);
    }
}