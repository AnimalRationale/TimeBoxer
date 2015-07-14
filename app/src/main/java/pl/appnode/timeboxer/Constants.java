package pl.appnode.timeboxer;

/** Set of constants  */

public final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    public static final String ALARMS_PREFS_FILE = "AlarmsPrefsFile";
    public static final int DEFAULT_TIMER_DURATION = 12;
    public static final int DEFAULT_TIMER_DURATION_MODIFIER = 4;
    public static final int OFF = 0;
    public static final int SWITCHING = 1;
    public static final int ON = 2;
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
    public static final String COUNTDOWN_BROADCAST = "pl.appnode.timeboxer";
}