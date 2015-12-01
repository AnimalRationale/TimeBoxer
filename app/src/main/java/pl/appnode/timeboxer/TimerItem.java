package pl.appnode.timeboxer;

/**
 * Defines structure of items holding timers information.
 */
public class TimerItem {
    String mName; // Timer's name
    int mDuration; // Timer's duration
    int mDurationCounter; // Current value of timer's duration to display
    int mTimeUnit; // Timer's time unit: seconds or minutes
    String mTimeUnitSymbol; // Symbol of seconds or minutes, depending on localization
    int mStatus; // Current state of timer
    long mFinishTime; // System time of timers finish
    String mRingtoneUri; // Uri for ringtone
    int mRingtoneVolume; // Device depending ringtone volume level
    boolean mFullscreenSwitchOff; // true if timer should end with full screen off (alarm clock mode)
    String mTimeOfStart; // Time of timer's start displayed in notification
}