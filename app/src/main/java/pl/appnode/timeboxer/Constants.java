package pl.appnode.timeboxer;

/** Set of constants  */

public final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

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

    public static final String TIMER_SETTINGS_INTENT_TIMER_ID = "TimerId";
    public static final String TIMER_SETTINGS_INTENT_TIMER_NAME = "TimerName";
    public static final String TIMER_SETTINGS_INTENT_TIMER_UNIT = "TimerUnit";
    public static final String TIMER_SETTINGS_INTENT_TIMER_RINGTONE_URI = "TimerRingtoneUri";
    public static final String TIMER_SETTINGS_INTENT_TIMER_RINGTONE_VOL = "TimerRingtoneVol";
    public static final String TIMER_SETTINGS_INTENT_TIMER_FULLSCREEN_OFF = "TimerFullscreenOff";

    public static final int DEFAULT_TIMER_DURATION = 12;
    public static final int DEFAULT_TIMER_DURATION_MODIFIER = 4;
    public static final int MAX_TIMER_DURATION = 100;
    public static final int IDLE = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int RESTORE = 3;
    public static final int SECOND = 0;
    public static final int MINUTE = 1;
    public static final int SECOND_IN_MILLIS = 1000;
    public static final int MINUTE_IN_MILLIS = 1000 * 60;
    public static final int TIME_DEVIATION_FOR_LAST_TICK = 200;
    public static final int WAKE_UP_MARGIN = 2000;
    public static final int BUTTON_PRESS_DELAY = 700;
    public static final int STOP = 0;
    public static final int START = 1;
    public static final int UPDATE = 2;
    public static final int EMPTY = 99;
    public static final int TIMERS_COUNT = 4;
    public static final int SETTINGS_INTENT_REQUEST = 501;
    public static final int RINGTONE_INTENT_REQUEST = 502;
    public static final float TIMER_SETTINGS_DIALOG_BACKGROUND_TRANSPARENCY = 0.8f;
    public static final String COUNTDOWN_BROADCAST = "pl.appnode.timeboxer";
    public static final int[] WIDGET_BUTTONS = {
            R.id.widget_round_button0,
            R.id.widget_round_button1,
            R.id.widget_round_button2,
            R.id.widget_round_button3,
            R.id.widget_round_button4,
    };
    public static final String[] WIDGET_BUTTON_ACTION = {"A", "0", "1", "2", "3"};

    public static final int OFF_SCREEN_START_FROM_SERVICE = 1;
    public static final int OFF_SCREEN_DEACTIVATED = 0;
}