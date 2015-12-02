package pl.appnode.timeboxer;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static pl.appnode.timeboxer.Constants.WIDGET_BUTTON_ACTION;

/**
 * Handles user actions in TimeBoxer's widget .
 */
public class TimeBoxerWidgetProvider extends android.appwidget.AppWidgetProvider {

    private static final String LOGTAG = "WidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TimersService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Checks clicked button, assigns timer id and calls/starts TimersService for proper action
        for (int i = 1; i < 5; i++) {
            if (WIDGET_BUTTON_ACTION[i].equals(intent.getAction())) {
                i = 5;
                int j = Integer.parseInt(intent.getAction());
                if (TimersService.sTimersList != null) {
                    TimersService.timerAction(Integer.parseInt(intent.getAction()));
                }
                else {
                    Intent commandIntent = new Intent(context, TimersService.class);
                    commandIntent.putExtra("buttonId", j);
                    context.startActivity(commandIntent);
                }
            }
        }
    }
}