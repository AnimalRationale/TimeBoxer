package pl.appnode.timeboxer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import static pl.appnode.timeboxer.Constants.BUTTON_PRESS_DELAY;
import static pl.appnode.timeboxer.Constants.FINISHED;
import static pl.appnode.timeboxer.Constants.IDLE;
import static pl.appnode.timeboxer.Constants.MAX_TIMER_DURATION;
import static pl.appnode.timeboxer.Constants.RUNNING;
import static pl.appnode.timeboxer.Constants.SETTINGS_INTENT_REQUEST;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_FULLSCREEN_OFF;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_ID;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_NAME;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_RINGTONE_URI;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_RINGTONE_VOL;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_UNIT;

import static pl.appnode.timeboxer.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.timeboxer.TimersService.sTimersList;

/**
 * Adapts items from data set into views grouped in list.
 */
public class TimersAdapter extends RecyclerView.Adapter<TimersAdapter.TimerViewHolder>{

    private static final String TAG = "TimersAdapter";
    private final Context mContext;
    private long mLastClickTime = 0;

    public TimersAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return sTimersList.size();
    }

    @Override
    public void onBindViewHolder(final TimerViewHolder timerViewHolder, final int position) {
        final TimerItem timer = sTimersList.get(position);
        timerViewHolder.vTitle.setText(timer.mName);
        timerViewHolder.vProgressBar.setMax(timer.mDuration);

        timerViewHolder.vTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer.mStatus != RUNNING) {
                    showTimerSettingsActivity(position);
                }
            }
        });
        if (timer.mStatus == RUNNING) {
            timerViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_orange);
            timerViewHolder.vMinutesBar.setVisibility(View.GONE);
            timerViewHolder.vDuration.setText(timer.mDurationCounter + timer.mTimeUnitSymbol);
            timerViewHolder.vProgressBar.setProgress(timer.mDurationCounter);
            timerViewHolder.vProgressBar.setBackgroundResource(R.drawable.round_button_red);
        } else if (timer.mStatus == IDLE) {
            timerViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_green);
            timerViewHolder.vMinutesBar.setVisibility(View.VISIBLE);
            timerViewHolder.vDuration.setText(timer.mDuration + timer.mTimeUnitSymbol);
            timerViewHolder.vProgressBar.setProgress(0);
            timerViewHolder.vProgressBar.setBackgroundResource(R.drawable.round_button_green);
        } else if (timer.mStatus == FINISHED) {
            timerViewHolder.vDuration.setBackgroundResource(R.drawable.round_button_red);
            timerViewHolder.vMinutesBar.setVisibility(View.GONE);
            timerViewHolder.vDuration.setText(timer.mDurationCounter + timer.mTimeUnitSymbol);
            timerViewHolder.vProgressBar.setProgress(0);
            timerViewHolder.vProgressBar.setBackgroundResource(R.drawable.round_button_red);
        }
        // Timers list has constant number of elements, there should be no scrolling
        // and re-binding (spawning and garbage collecting of listeners); in case of adding
        // option for increasing amount ot timers, listeners should be removed from onBindViewHolder
        timerViewHolder.vDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < BUTTON_PRESS_DELAY) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                startTimerWithAnimation(timerViewHolder.vProgressBar, position);
            }
        });
        timerViewHolder.vMinutesBar.setMax(MAX_TIMER_DURATION);
        timerViewHolder.vMinutesBar.setProgress(timer.mDuration);
        timerViewHolder.vMinutesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                timer.mDuration = progress;
                timer.mDurationCounter = progress;
                timerViewHolder.vDuration.setText(progress + timer.mTimeUnitSymbol);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setDuration(timer);
            }
        });
    }

    @Override
    public TimerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(Color.BLACK);
        } else card.setCardBackgroundColor(Color.WHITE);
        return new TimerViewHolder(itemView);
    }

    public static class TimerViewHolder extends RecyclerView.ViewHolder {

        final TextView vTitle;
        final Button vDuration;
        final SeekBar vMinutesBar;
        final ProgressBar vProgressBar;

        public TimerViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDuration = (Button) v.findViewById(R.id.button_round_01);
            vMinutesBar = (SeekBar) v.findViewById(R.id.time_seek_bar);
            vProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        }
    }

    private void showTimerSettingsActivity(int position) {
        TimerItem timer = sTimersList.get(position);
        Intent settingsIntent = new Intent(mContext, TimerSettingsActivity.class);
        settingsIntent.putExtra(TIMER_SETTINGS_INTENT_TIMER_ID, position);
        settingsIntent.putExtra(TIMER_SETTINGS_INTENT_TIMER_NAME, timer.mName);
        settingsIntent.putExtra(TIMER_SETTINGS_INTENT_TIMER_UNIT, timer.mTimeUnit);
        settingsIntent.putExtra(TIMER_SETTINGS_INTENT_TIMER_RINGTONE_URI, timer.mRingtoneUri);
        settingsIntent.putExtra(TIMER_SETTINGS_INTENT_TIMER_RINGTONE_VOL, timer.mRingtoneVolume);
        settingsIntent.putExtra(TIMER_SETTINGS_INTENT_TIMER_FULLSCREEN_OFF, timer.mFullscreenSwitchOff);
        ((MainActivity)mContext).startActivityForResult(settingsIntent, SETTINGS_INTENT_REQUEST);
    }

    private void setDuration(final TimerItem timer) {
        final int position = sTimersList.indexOf(timer);
        notifyItemChanged(position);
        TimersService.saveTimerDuration(position);
    }

    private void startTimerWithAnimation(final ProgressBar progressBar, final int position) {
        if (progressBar != null) {
            final int originalMax = progressBar.getMax();
            int animationMax = 300;
            progressBar.setMax(animationMax);
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, animationMax);
            animation.setDuration(700);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
            animation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    progressBar.setMax(originalMax);
                    TimersService.timerAction(position);
                }
            });
        }
    }
}