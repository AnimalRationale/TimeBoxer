package pl.appnode.timeboxer;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static pl.appnode.timeboxer.Constants.WIDGET_BUTTON_ACTION;

public class TimeBoxerWidgetProvider extends android.appwidget.AppWidgetProvider {

    private static final String TAG = "WidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TimersService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Widget onReceive.");
        int i = 1;
        while (i != 5) {
            if (WIDGET_BUTTON_ACTION[i].equals(intent.getAction())) {
                i = 5;
                int j = Integer.parseInt(intent.getAction());
                if (TimersService.sTimersList != null) {
                    TimersService.timerAction(Integer.parseInt(intent.getAction()));
                    Log.d(TAG, "Timers list not null. Action: " + j);}
                else {
                    Log.d(TAG, "Timers list null. Action: " + j);
                    Intent commandIntent = new Intent(context, TimersService.class);
                    commandIntent.putExtra("buttonId", j);
                    context.startActivity(commandIntent);
                }
            } else i++;
        }
    }
}