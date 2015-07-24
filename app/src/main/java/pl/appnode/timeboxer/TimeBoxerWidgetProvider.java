package pl.appnode.timeboxer;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

public class TimeBoxerWidgetProvider extends android.appwidget.AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TimersService.class));
    }

}