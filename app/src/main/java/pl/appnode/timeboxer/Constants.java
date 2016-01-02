package pl.appnode.timeboxer;

/** Set of constants  */

final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    /**
     * Filename for sharedprefernces file with saved timers settings,
     * keys with suffixes used for saving and reading timers settings.
     */
    public static final String ALARMS_PREFS_FILE = "TimersPrefsFile";
    public static final String TIMER_PREFIX = "Timer_";
    public static final String DEFAULT_TIMER_NAME = "Timer ";
    public static final String PREFS_DURATION = "_Duration";
    public static final String PREFS_TIME_UNIT = "_TimeUnit";
    public static final String PREFS_STATE = "_State";
    public static final String PREFS_RINGTONE = "_Ringtone";
    public static final String PREFS_RINGTONE_VOL = "_RingtoneVol";
    public static final String PREFS_FULLSCREEN_OFF = "_FullScreenOff";
    public static final String PREFS_FINISHTIME = "_FinishTime";

    /** Keys for saving app settings (theme, orientation, colors transitions) and first run check). */
    public static final String KEY_SETTINGS_THEME = "settings_checkbox_theme";
    public static final String KEY_SETTINGS_ORIENTATION = "settings_checkbox_orientation";
    public static final String KEY_SETTINGS_TRANSITIONS = "settings_checkbox_transitions";
    public static final String KEY_SETTINGS_FIRSTRUN = "settings_first_run";

    /** Keys for intent for timer's settings activity. */
    public static final String TIMER_SETTINGS_INTENT_TIMER_ID = "TimerId";
    public static final String TIMER_SETTINGS_INTENT_TIMER_NAME = "TimerName";
    public static final String TIMER_SETTINGS_INTENT_TIMER_UNIT = "TimerUnit";
    public static final String TIMER_SETTINGS_INTENT_TIMER_RINGTONE_URI = "TimerRingtoneUri";
    public static final String TIMER_SETTINGS_INTENT_TIMER_RINGTONE_VOL = "TimerRingtoneVol";
    public static final String TIMER_SETTINGS_INTENT_TIMER_FULLSCREEN_OFF = "TimerFullscreenOff";

    /** Default values for timers, used on first run or if sharedpreferences file was removed. */
    public static final int DEFAULT_TIMER_DURATION = 12;
    // Time duration modifier for generating different timers (value added to previous timer duration)
    public static final int DEFAULT_TIMER_DURATION_MODIFIER = 4;
    public static final int MAX_TIMER_DURATION = 100;

    /** Timers states. */
    public static final int IDLE = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int RESTORE = 3;

    /** Time values used in TimeBoxer.*/
    public static final int SECOND = 0;
    public static final int MINUTE = 1;
    public static final int SECOND_IN_MILLIS = 1000;
    public static final int MINUTE_IN_MILLIS = 1000 * 60;

    /** Countdown time deviation making possible PERFORMING last onTick() of countdown timer. */
    public static final int TIME_DEVIATION_FOR_LAST_TICK = 200;

    /** Time in milliseconds before timer's finish for system wake up alarm. */
    public static final int WAKE_UP_MARGIN = 2000;

    /**
     * Delay before accepting next click of button on timers list
     * (preventing accidental timer on/off).
     */
    public static final int BUTTON_PRESS_DELAY = 700;

    /** Number of timers. */
    public static final int TIMERS_COUNT = 4;

    public static final int SETTINGS_INTENT_REQUEST = 501;
    public static final int RINGTONE_INTENT_REQUEST = 502;
    public static final float TIMER_SETTINGS_DIALOG_BACKGROUND_TRANSPARENCY = 0.7f;

    /** Array with stored ids of widget buttons. */
    public static final int[] WIDGET_BUTTONS = {
            R.id.widget_round_button0,
            R.id.widget_round_button1,
            R.id.widget_round_button2,
            R.id.widget_round_button3,
            R.id.widget_round_button4,
    };
    /** Array with defined widget buttons actions. */
    public static final String[] WIDGET_BUTTON_ACTION = {"A", "0", "1", "2", "3"};

    /** Array with stored ids of widget buttons circular progress bars. */
    public static final int[] WIDGET_BUTTONS_PROGRESS_BARS = {
            R.id.widget_round_button0,
            R.id.widget_button1_progress_bar,
            R.id.widget_button2_progress_bar,
            R.id.widget_button3_progress_bar,
            R.id.widget_button4_progress_bar,
    };

    public static final int CANCEL_WAKE_UP_ALARM = 0;
    public static final int SET_WAKE_UP_ALARM = 1;
    public static final String ACTION_TIMER_WAKE_UP = "TIMER_WAKE_UP";
    public static final String EXTRA_TIMER_ID = "TimerID";

    /** Period of time in milliseconds for setting up device wake lock to perform timer's finish. */
    public static final int WAKE_LOCK_TIME_OUT = 6000;
    public static final String EXTRA_COMMAND_WAKE_UP_TIMER_ID = "WakeUpCommand";
    public static final String ACTION_TIMER_SWITCH_OFF = "TimerSwitchOff";
    public static final String EXTRA_COMMAND_SWITCH_OFF_TIMER_ID = "SwitchOff";
    public static final String ACTION_INTENT_HIDE_SWITCHOFF_ACTIVITY = "HideFullscreenOff";
}