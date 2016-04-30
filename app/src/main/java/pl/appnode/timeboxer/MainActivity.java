package pl.appnode.timeboxer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import static pl.appnode.timeboxer.PreferencesSetupHelper.isFirstRun;
import static pl.appnode.timeboxer.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.timeboxer.PreferencesSetupHelper.themeSetup;

/**
 * Starts TimeBoxer service if needed, displays user interface of TimeBoxer (with list of timers),
 * shows app introduction on first run.
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    static TimersAdapter mTimersAdapter;
    static boolean sIsTimersBroadcastService = false;
    private static boolean sThemeChangeFlag;
    static int sSeekbarAnimationsCounter;

    public static void increaseSeekbarAnimationsCounter() {
        sSeekbarAnimationsCounter++;
    }

    public static int getSeekbarAnimationsCounter() {
        return sSeekbarAnimationsCounter;
    }

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume.");
        TimersService.setIsMainActivityVisible(true);
        orientationSetup(this);
        super.onResume();
        checkThemeChange();
        checkFirstRun();
        sSeekbarAnimationsCounter = 0;
        mTimersAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        TimersService.setIsMainActivityVisible(false);
        TimersService.updateWidget();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Sets color of menu icons in light theme
        if (!isDarkTheme(this)) {
            for(int i = 0; i < menu.size(); i++){
                Drawable drawable = menu.getItem(i).getIcon();
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(ContextCompat
                            .getColor(AppContextHelper.getContext(), R.color.black_overlay),
                            PorterDuff.Mode.SRC_IN);
                }
            }
        }
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
        if (id == R.id.action_help) {
            Intent settingsIntent = new Intent(this, AppIntroActivity.class);
            this.startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    // Shows main menu on top of the screen on devices with physical menu button
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

    private void checkFirstRun() {
        if (isFirstRun(this)) {
            Log.d(TAG, "First run!");
            Intent intent = new Intent(AppContextHelper.getContext(), AppIntroActivity.class);
            startActivity(intent);
        } else Log.d(TAG, "Not first run");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == SETTINGS_INTENT_REQUEST && resultCode == RESULT_OK
                && resultIntent.getExtras() != null) {
            Log.d(TAG, "Proper ResultIntent.");
            int position = resultIntent.getIntExtra(TIMER_SETTINGS_INTENT_TIMER_ID, 0);
            TimerItem timer = TimersService.getTimer(position);
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