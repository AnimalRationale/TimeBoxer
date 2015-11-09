package pl.appnode.timeboxer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Build;

import static pl.appnode.timeboxer.Constants.KEY_SETTINGS_FIRSTRUN;
import static pl.appnode.timeboxer.Constants.KEY_SETTINGS_ORIENTATION;
import static pl.appnode.timeboxer.Constants.KEY_SETTINGS_THEME;

public class PreferencesSetupHelper {

    public static void themeSetup(Context context) {

        if (isDarkTheme(context)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                context.setTheme(android.R.style.Theme_Holo);
            } else {
                context.setTheme(android.R.style.Theme_Material);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            context.setTheme(android.R.style.Theme_Holo_Light);
        } else {
            context.setTheme(android.R.style.Theme_Material_Light);
        }
    }

    public static void orientationSetup(Activity activity) {

        if (isRotationOn(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static boolean isDarkTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_THEME, false);
    }

    public static boolean isRotationOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_ORIENTATION, false);
    }

    public static boolean isFirstRun(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstRun = settings.getBoolean(KEY_SETTINGS_FIRSTRUN, true);
        settings.edit().putBoolean(KEY_SETTINGS_FIRSTRUN, false).apply();
        return firstRun;
    }
}