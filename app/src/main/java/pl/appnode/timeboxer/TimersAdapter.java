package pl.appnode.timeboxer;

import android.content.Context;
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
        timersViewHolder.vDuration.setText(timer.mDuration + timer.mTimeUnitSymbol);
    }

    @Override
    public TimersViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        CardView card = (CardView) itemView;
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
}