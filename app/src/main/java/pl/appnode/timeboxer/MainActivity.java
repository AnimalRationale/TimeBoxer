package pl.appnode.timeboxer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    protected static TimersAdapter mTimersAdapter;
    protected static boolean sIsTimersBroadcastService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!sIsTimersBroadcastService) {
            Intent serviceIntent = new Intent(AppContextHelper.getContext(), TimersBroadcastService.class);
            AppContextHelper.getContext().startService(serviceIntent);
            Log.d(TAG, "Starting service, service = " + sIsTimersBroadcastService);
        }
        setContentView(R.layout.activity_main);
        RecyclerView recyclerTimersList = (RecyclerView) findViewById(R.id.timersList);
        recyclerTimersList.setItemAnimator(null);
        recyclerTimersList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerTimersList.setLayoutManager(llm);
        mTimersAdapter = new TimersAdapter(TimersBroadcastService.mTimersList, MainActivity.this);
        recyclerTimersList.setAdapter(mTimersAdapter);
        Log.d(TAG, "TimersAdapter");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}