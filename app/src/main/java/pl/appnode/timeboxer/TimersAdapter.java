package pl.appnode.timeboxer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import static pl.appnode.timeboxer.Constants.MAX_TIMER_DURATION;
import static pl.appnode.timeboxer.Constants.RUNNING;
import static pl.appnode.timeboxer.Constants.SETTINGS_INTENT_REQUEST;
import static pl.appnode.timeboxer.PreferencesSetupHelper.isDarkTheme;

public class TimersAdapter extends RecyclerView.Adapter<TimersAdapter.TimersViewHolder>{

    private static final String TAG = "TimersAdapter";
    private List<TimerItem> mAdapterTimersList;
    private Context mContext;

    public TimersAdapter(List<TimerItem> timersList, Context context) {
        this.mAdapterTimersList = timersList;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mAdapterTimersList.size();
    }

    @Override
    public void onBindViewHolder(final TimersViewHolder timersViewHolder, final int position) {
        final TimerItem timer = mAdapterTimersList.get(position);
        timersViewHolder.vTitle.setText(timer.mName);
        timersViewHolder.vTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer.mStatus != RUNNING) {
                    showTimerSettings(position);
                }
            }
        });
        timersViewHolder.vDuration.setText(timer.mDuration + timer.mTimeUnitSymbol);
        timersViewHolder.vMinutesBar.setMax(MAX_TIMER_DURATION);
        timersViewHolder.vMinutesBar.setProgress(timer.mDuration);
    }

    @Override
    public TimersViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(Color.BLACK);
        } else card.setCardBackgroundColor(Color.WHITE);
        return new TimersViewHolder(itemView);
    }

    public static class TimersViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected Button vDuration;
        protected SeekBar vMinutesBar;

        public TimersViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDuration = (Button) v.findViewById(R.id.button_round_01);
            vMinutesBar = (SeekBar) v.findViewById(R.id.time_seek_bar);
        }
    }

    private void showTimerSettings(int position) {
        TimerItem timer = mAdapterTimersList.get(position);
        Intent settingsIntent = new Intent(mContext, TimerSettingsActivity.class);
        settingsIntent.putExtra("AlarmId", position);
        settingsIntent.putExtra("AlarmName", timer.mName);
        settingsIntent.putExtra("AlarmUnit", timer.mTimeUnit);
        settingsIntent.putExtra("AlarmRingtoneUri", timer.mRingtoneUri);
        settingsIntent.putExtra("AlarmRingtoneVol", timer.mRingtoneVolume);
        settingsIntent.putExtra("AlarmFullscreenOff", timer.mFullscreenSwitchOff);
        ((MainActivity)mContext).startActivityForResult(settingsIntent, SETTINGS_INTENT_REQUEST);
    }
}