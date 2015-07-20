package pl.appnode.timeboxer;

import android.os.CountDownTimer;

public class CustomCountDownTimer extends CountDownTimer {

    public CustomCountDownTimer (long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
    }
}
