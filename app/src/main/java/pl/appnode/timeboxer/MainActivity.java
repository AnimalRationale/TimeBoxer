package pl.appnode.timeboxer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import static pl.appnode.timeboxer.Constants.SECOND;
import static pl.appnode.timeboxer.Constants.SETTINGS_INTENT_REQUEST;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_FULLSCREEN_OFF;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_ID;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_NAME;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_RINGTONE_URI;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_RINGTONE_VOL;
import static pl.appnode.timeboxer.Constants.TIMER_SETTINGS_INTENT_TIMER_UNIT;
import static pl.appnode.timeboxer.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.timeboxer.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.timeboxer.PreferencesSetupHelper.themeSetup;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    protected static TimersAdapter mTimersAdapter;
    protected static boolean sIsTimersBroadcastService = false;
    private static boolean sThemeChangeFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!sIsTimersBroadcastService) {
            Intent serviceIntent = new Intent(AppContextHelper.getContext(),
                    TimersService.class);
            AppContextHelper.getContext().startService(serviceIntent);
            Log.d(TAG, "Starting service, service = " + sIsTimersBroadcastService);
        }
        themeSetup(this);
        sThemeChangeFlag = isDarkTheme(this);
        showActionOverflowMenu();
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
        RecyclerView recyclerTimersList = (RecyclerView) findViewById(R.id.timersList);
        recyclerTimersList.setItemAnimator(null);
        recyclerTimersList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerTimersList.setLayoutManager(llm);
        mTimersAdapter = new TimersAdapter(this);
        recyclerTimersList.setAdapter(mTimersAdapter);
        Log.d(TAG, "TimersAdapter");
    }

    @Override
    public void onResume() {
        TimersService.sIsMainActivityVisible = true;
        orientationSetup(this);
        super.onResume();
        checkThemeChange();
        mTimersAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        TimersService.sIsMainActivityVisible = false;
        TimersService.updateWidget();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AboutDialog.showDialog(MainActivity.this);
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, PreferencesActivity.class);
            this.startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showActionOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    private void checkThemeChange() {
        if (sThemeChangeFlag != isDarkTheme(this)) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == SETTINGS_INTENT_REQUEST && resultCode == RESULT_OK
                && resultIntent.getExtras() != null) {
            Log.d(TAG, "Proper ResultIntent.");
            int position = resultIntent.getIntExtra(TIMER_SETTINGS_INTENT_TIMER_ID, 0);
            TimerItem timer = TimersService.sTimersList.get(position);
            timer.mName = (String) resultIntent.getExtras().get(TIMER_SETTINGS_INTENT_TIMER_NAME);
            timer.mFullscreenSwitchOff = (boolean) resultIntent.getExtras()
                    .get(TIMER_SETTINGS_INTENT_TIMER_FULLSCREEN_OFF);
            timer.mTimeUnit = (int) resultIntent.getExtras().get(TIMER_SETTINGS_INTENT_TIMER_UNIT);
            if (timer.mTimeUnit == SECOND) {
                timer.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
            } else timer.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
            timer.mRingtoneUri = (String) resultIntent.getExtras()
                    .get(TIMER_SETTINGS_INTENT_TIMER_RINGTONE_URI);
            timer.mRingtoneVolume = (int) resultIntent.getExtras()
                    .get(TIMER_SETTINGS_INTENT_TIMER_RINGTONE_VOL);
            mTimersAdapter.notifyItemChanged(position);
            TimersService.saveSharedPrefs();
            TimersService.updateWidget();
        }
    }
}