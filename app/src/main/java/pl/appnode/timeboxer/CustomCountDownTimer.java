package pl.appnode.timeboxer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CustomCountDownTimer extends CountDownTimer {

    private static final String TAG = "CustomCountDownTimer";
    private int mTimeUnitFactor;
    private int mTimerId;
    private NotificationManager mNM ;
    private NotificationCompat.Builder mNotify;
    Context mContext = AppContextHelper.getContext();
    TimerItem mTimer;
    private Ringtone mRingtone;
    private int mRingtoneVolume;
    private int mOriginalVolume;
    private AudioManager mAudioManager;

    public CustomCountDownTimer (long millisInFuture, long countDownInterval, int position, int timeUnitFactor) {
        super(millisInFuture, countDownInterval);
        mTimerId = position;
        mTimer = TimersBroadcastService.sTimersList.get(mTimerId);
        mTimeUnitFactor = timeUnitFactor;
        setUpRingtone();
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
        setVolume();
        mRingtone.play();
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

    private void setUpRingtone() {
        Uri alert = setNotNullRingtone(mTimer.mRingtoneUri);
        mRingtoneVolume = mTimer.mRingtoneVolume;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRingtone = RingtoneManager.getRingtone(mContext.getApplicationContext(), alert);
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
    }

    private Uri setNotNullRingtone(String ringtone) {
        Uri ringtoneUri;
        if (ringtone == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (ringtoneUri == null) {
                    ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            return ringtoneUri;
        }
        try {
            Uri.parse(ringtone);
        } catch (Throwable thex) {
            Log.d(TAG, "Parsing URI exception.");
        }
        return Uri.parse(ringtone);
    }

    public void setVolume() {
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (mRingtoneVolume <= 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        } else if (mRingtoneVolume >= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mRingtoneVolume, 0);}
        Log.d(TAG, "Original vol: " + mOriginalVolume + " Set: " + mRingtoneVolume);
    }

    public void restoreVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mOriginalVolume, 0);
        Log.d(TAG, "Used vol: " + mRingtoneVolume + " Restored: " + mOriginalVolume);
    }

    public void stopRingtone () {
        mRingtone.stop();
        restoreVolume();
    }
}