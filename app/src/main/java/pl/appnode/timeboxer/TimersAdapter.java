package pl.appnode.timeboxer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

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
import static pl.appnode.timeboxer.TimersBroadcastService.sTimersList;

public class TimersAdapter extends RecyclerView.Adapter<TimersAdapter.TimerViewHolder>{

    private static final String TAG = "TimersAdapter";
    private Context mContext;

    public TimersAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return sTimersList.size();
    }

    @Override
    public void onBindViewHolder(final TimerViewHolder timersViewHolder, final int position) {
        final TimerItem timer = sTimersList.get(position);
        timersViewHolder.vTitle.setText(timer.mName);
        timersViewHolder.vTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer.mStatus != RUNNING) {
                    showTimerSettingsActivity(position);
                }
            }
        });
        timersViewHolder.vDuration.setText(timer.mDuration + timer.mTimeUnitSymbol);
        timersViewHolder.vMinutesBar.setMax(MAX_TIMER_DURATION);
        timersViewHolder.vMinutesBar.setProgress(timer.mDuration);
        timersViewHolder.vMinutesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                timer.mDuration = progress;
                timer.mDurationCounter = progress;
                timersViewHolder.vDuration.setText(progress + timer.mTimeUnitSymbol);
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

        protected TextView vTitle;
        protected Button vDuration;
        protected SeekBar vMinutesBar;

        public TimerViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDuration = (Button) v.findViewById(R.id.button_round_01);
            vMinutesBar = (SeekBar) v.findViewById(R.id.time_seek_bar);
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

    public void setDuration(final TimerItem timer) {
        final int position = sTimersList.indexOf(timer);
        notifyItemChanged(position);
    }
}