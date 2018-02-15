package ru.vpcb.notifications.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import ru.vpcb.notifications.MainActivity;
import ru.vpcb.notifications.R;
import ru.vpcb.notifications.data.FDFixture;
import timber.log.Timber;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static ru.vpcb.notifications.Utils.Config.EMPTY_WIDGET_ID;
import static ru.vpcb.notifications.Utils.Config.WIDGET_INTENT_BUNDLE;
import static ru.vpcb.notifications.Utils.Config.EMPTY_FIXTURE_ID;
import static ru.vpcb.notifications.Utils.Config.WIDGET_PID_BASE;
import static ru.vpcb.notifications.Utils.Config.WIDGET_BUNDLE_FIXTURE_ID;
import static ru.vpcb.notifications.Utils.Config.WIDGET_PID_OFFSET0;
import static ru.vpcb.notifications.Utils.Config.WIDGET_PID_OFFSET1;
import static ru.vpcb.notifications.Utils.Config.WIDGET_PREFERENCES;
import static ru.vpcb.notifications.Utils.Config.WIDGET_PID_SCALE;
import static ru.vpcb.notifications.Utils.Config.WIDGET_BUNDLE_WIDGET_ID;
import static ru.vpcb.notifications.Utils.Config.WIDGET_SERVICE_REFRESH_ACTION;

/**
 * Implementation of App Widget functionality.
 */
public class RecipeWidgetProvider extends AppWidgetProvider {

    /**
     * Updates widget with parameters or creates empty new one
     * createWidget() method used for new widget
     * fillWidget()   method used to fill widget
     * <p>
     * This method called from WidgetService when user creates new widget
     * This method called by system when onUpdate() method called
     */
    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             int widgetId, FDFixture fixture) {

        if (fixture == null || fixture.getId() <= 0) {
            createWidget(context, appWidgetManager, widgetId);
        } else {
            fillWidget(context, appWidgetManager, widgetId, fixture);
        }
    }

    private static void createWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        if (widgetId <= 0) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.match_widget_provider);
        String widgetHead = context.getString(R.string.widget_head_text);
        int widgetPID = WIDGET_PID_BASE + WIDGET_PID_SCALE * widgetId + WIDGET_PID_OFFSET0;

        Intent intent = new Intent(context, MainActivity.class); // call activity
        Bundle args = new Bundle();
        args.putInt(WIDGET_BUNDLE_WIDGET_ID, widgetId);
        intent.putExtra(WIDGET_INTENT_BUNDLE, args);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent, 0);
        views.setTextViewText(R.id.text_sm_item_league, widgetHead);
        views.setOnClickPendingIntent(R.id.app_widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    public static void fillWidget(Context context, AppWidgetManager appWidgetManager,
                                  int widgetId, FDFixture fixture) {

        if (widgetId <= 0 || fixture == null || fixture.getId() <= 0) return;

        int widgetPID = WIDGET_PID_BASE + WIDGET_PID_SCALE * widgetId + WIDGET_PID_OFFSET0;
        int fixtureId = fixture.getId();

        putWidgetFixtureId(context, widgetId, fixtureId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.match_widget_provider);

        Intent intent = new Intent(context, MainActivity.class);       // call activity second time
        Bundle args = new Bundle();
        args.putInt(WIDGET_BUNDLE_FIXTURE_ID, fixtureId);

        intent.putExtra(WIDGET_INTENT_BUNDLE, args);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetPID, intent, FLAG_UPDATE_CURRENT);

// test!!!
        views.setTextViewText(R.id.text_sm_item_league, fixture.getCompetitionName());
        views.setTextViewText(R.id.text_sm_team_home, fixture.getHomeTeamName());
        views.setTextViewText(R.id.text_sm_team_away, fixture.getAwayTeamName());
        views.setTextViewText(R.id.text_sm_item_time, fixture.getMatchTime());
        views.setTextViewText(R.id.text_sm_item_date, fixture.getMatchDateWidget());
        views.setTextViewText(R.id.text_sm_item_score_home, "" + fixture.getMatchScoreHome());
        views.setTextViewText(R.id.text_sm_item_score_away, "" + fixture.getMatchScoreAway());
        views.setTextViewText(R.id.text_sm_item_status, "" + fixture.getStatus());

        views.setOnClickPendingIntent(R.id.app_widget_container, pendingIntent);

        views.setOnClickPendingIntent(R.id.match_refresh, getPendingIntent(context, widgetId, fixtureId));

        appWidgetManager.updateAppWidget(widgetId, views);
    }


    private static PendingIntent getPendingIntent(Context context, int widgetId, int fixtureId) {

        Intent intent = new Intent(context, RecipeWidgetProvider.class);
        intent.setAction(WIDGET_SERVICE_REFRESH_ACTION);
        Bundle args = new Bundle();
        args.putInt(WIDGET_BUNDLE_WIDGET_ID, widgetId);
        args.putInt(WIDGET_BUNDLE_FIXTURE_ID, fixtureId);
        intent.putExtra(WIDGET_INTENT_BUNDLE, args);

        int widgetPID = WIDGET_PID_BASE + WIDGET_PID_SCALE * widgetId + WIDGET_PID_OFFSET1;
        return PendingIntent.getBroadcast(context, widgetPID, intent, 0);

    }

    public static int getWidgetFixtureId(Context context, int widgetId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(WIDGET_PREFERENCES + WIDGET_BUNDLE_FIXTURE_ID + widgetId, EMPTY_FIXTURE_ID);
    }

    public static void putWidgetFixtureId(Context context, int widgetId, int fixtureId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WIDGET_PREFERENCES + WIDGET_BUNDLE_FIXTURE_ID + widgetId, fixtureId);
        editor.apply();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RecipeWidgetService.startWidgetUpdateAction(context);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(WIDGET_SERVICE_REFRESH_ACTION)) {
            Bundle bundle = intent.getBundleExtra(WIDGET_INTENT_BUNDLE);
            if (bundle == null) return;

            int widgetId = bundle.getInt(WIDGET_BUNDLE_WIDGET_ID, EMPTY_WIDGET_ID);
            int fixtureId = bundle.getInt(WIDGET_BUNDLE_FIXTURE_ID, EMPTY_FIXTURE_ID);
// test!!!
//            fixtureId = 161639;
            RecipeWidgetService.startFillWidgetAction(context, widgetId, fixtureId);


        }


    }
}

