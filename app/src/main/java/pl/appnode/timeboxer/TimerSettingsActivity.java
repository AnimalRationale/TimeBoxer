package pl.appnode.timeboxer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import static pl.appnode.timeboxer.Constants.MINUTE;
import static pl.appnode.timeboxer.Constants.RINGTONE_INTENT_REQUEST;
import static pl.appnode.timeboxer.Constants.SECOND;
import static pl.appnode.timeboxer.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.timeboxer.PreferencesSetupHelper.themeSetup;


public class TimerSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TimerSettingsActivity";
    private int mTimerId;
    private String mTimerName;
    private int mTimerTimeUnit;
    private String mTimerRingtoneUri;
    private int mTimerRingtoneVolume;
    private boolean mTimerFullscreenOff;
    private int mOriginalVolume;
    private AudioManager mAudioManager;
    private Uri mCurrentRingtoneUri;
    private Ringtone mRingtone;
    private String mRingtoneName;
    private TextView mTitle;
    private EditText mEditTimerName;
    private Switch mFullscreenOffSwitch;
    private RadioButton mRbSeconds;
    private Button mRingtoneTextButton;
    private ImageButton mPlayStopButton;
    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this); // Setting theme
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_timer_settings);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount=0.8f;
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.setFinishOnTouchOutside(false);
        colorFixForMaterialDark();
    }

    public void onResume() {
        super.onResume();
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Log.d(TAG, "onActivityResult fo Ringtone Picker.");
        if (requestCode == RINGTONE_INTENT_REQUEST && resultCode == RESULT_OK && resultIntent.getExtras() != null) {
            mCurrentRingtoneUri = resultIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            mRingtone = RingtoneManager.getRingtone(this.getApplicationContext(), mCurrentRingtoneUri);
            mRingtoneName =  mRingtone.getTitle(this.getApplicationContext());
            mRingtoneTextButton.setText(mRingtoneName);
            Log.d(TAG, "Ringtone Result: " + mRingtoneName);
        }
    }

    private Uri setNotNullRingtone(String ringtoneIn) {
        Uri ringtoneOut;
        if (ringtoneIn == null) {
            ringtoneOut = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (ringtoneOut == null) {
                ringtoneOut = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (ringtoneOut == null) {
                    ringtoneOut = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            return ringtoneOut;
        }
        return Uri.parse(ringtoneIn);
    }

    private void setVolume() {
        if (mTimerRingtoneVolume <= 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        } else if (mTimerRingtoneVolume >= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mTimerRingtoneVolume, 0);}
        Log.d(TAG, "Set ringtone volume: " + mTimerRingtoneVolume);
    }

    private void playRingtone() {
        setVolume();
        mPlayStopButton.setBackgroundResource(R.drawable.round_button_red);
        mPlayStopButton.setImageResource(R.mipmap.ic_stop_white_36dp);
        mIsPlaying = true;
        mRingtone.play();
    }

    private void stopRingtone() {
        mRingtone.stop();
        mIsPlaying = false;
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mOriginalVolume, 0);
        mPlayStopButton.setBackgroundResource(R.drawable.round_button_green);
        mPlayStopButton.setImageResource(R.mipmap.ic_play_arrow_white_36dp);
        Log.d(TAG, "Restored ringtone volume: " + mOriginalVolume);
    }

    private void getSettingsIntentData(Intent settingsIntent) {
        if (settingsIntent.getExtras() != null) {
            mTimerId = (int) settingsIntent.getExtras().get("AlarmId");
            mTimerName = (String) settingsIntent.getExtras().get("AlarmName");
            mTimerTimeUnit = (int) settingsIntent.getExtras().get("AlarmUnit");
            mTimerFullscreenOff = (boolean) settingsIntent.getExtras().get("AlarmFullscreenOff");
            mTimerRingtoneUri = (String) settingsIntent.getExtras().get("AlarmRingtoneUri");
            if (settingsIntent.getExtras().get("AlarmRingtoneVol") != null) {
                mTimerRingtoneVolume = (int) settingsIntent.getExtras().get("AlarmRingtoneVol");
            } else {
                mTimerRingtoneVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            }
        }
    }

    private void resultOk() {
        Intent resultIntent = getIntent();
        resultIntent.putExtra("AlarmId", mTimerId);
        resultIntent.putExtra("AlarmName", mEditTimerName.getText().toString());
        mTimerFullscreenOff = mFullscreenOffSwitch.isChecked();
        resultIntent.putExtra("AlarmFullscreenOff", mTimerFullscreenOff);
        if (mRbSeconds.isChecked()) {
            mTimerTimeUnit = SECOND;
        } else {
            mTimerTimeUnit = MINUTE;
        }
        resultIntent.putExtra("AlarmUnit", mTimerTimeUnit);
        resultIntent.putExtra("AlarmRingtoneUri", mCurrentRingtoneUri.toString());
        resultIntent.putExtra("AlarmRingtoneVol", mTimerRingtoneVolume);
        Log.d(TAG, "INTENT: ID=" + mTimerId + " Name:"
                + mEditTimerName.getText().toString() + " Unit:" + mTimerTimeUnit
                + " Ringtone:" + mCurrentRingtoneUri.toString() + " Volume: " + mTimerRingtoneVolume);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void resultCancel() {
        Intent resultIntent = getIntent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    private void ringtonePicker() {
        Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtoneIntent.putExtra
                (RingtoneManager.EXTRA_RINGTONE_TITLE,
                        getResources().getString(R.string.settings_timer_ringtone_picker) + mTitle.getText());
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mCurrentRingtoneUri);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        startActivityForResult(ringtoneIntent, RINGTONE_INTENT_REQUEST);
    }

    private void colorFixForMaterialDark() {
        if (isDarkTheme(this) & Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageView[] imageViews = new ImageView[2];
            imageViews[0] = (ImageView) findViewById(R.id.volumeIconMute);
            imageViews[1] = (ImageView) findViewById(R.id.volumeIconUp);
            for (int i = 0; i < imageViews.length; i++) {
                GradientDrawable drawable = (GradientDrawable) imageViews[i].getBackground().getCurrent();
                drawable.setColor(getResources().getColor(R.color.primary_light));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okTimerSettings:
                stopRingtone();
                resultOk();
                break;
            case R.id.cancelTimerSettings:
                stopRingtone();
                resultCancel();
                break;
            case R.id.changeRingtone:
                stopRingtone();
                ringtonePicker();
                break;
            case R.id.playTimerSettings:
                if (mRingtone.isPlaying() || mIsPlaying) {
                    stopRingtone();
                } else playRingtone();
                break;
        }
    }
}