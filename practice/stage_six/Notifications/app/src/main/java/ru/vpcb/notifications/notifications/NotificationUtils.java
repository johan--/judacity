package ru.vpcb.notifications.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ru.vpcb.notifications.MainActivity;
import ru.vpcb.notifications.R;
import ru.vpcb.notifications.Utils.FootballUtils;
import ru.vpcb.notifications.data.FDFixture;

import static ru.vpcb.notifications.Utils.Config.EMPTY_LONG_DASH;
import static ru.vpcb.notifications.Utils.Config.NT_ACTION_ACTIVITY_PENDING_ID;
import static ru.vpcb.notifications.Utils.Config.NT_ACTION_APPLY;
import static ru.vpcb.notifications.Utils.Config.NT_ACTION_CANCEL;
import static ru.vpcb.notifications.Utils.Config.NT_ACTION_CREATE;
import static ru.vpcb.notifications.Utils.Config.NT_BUNDLE_INTENT_NOTIFICATION_BODY;
import static ru.vpcb.notifications.Utils.Config.NT_BUNDLE_INTENT_NOTIFICATION_ID;
import static ru.vpcb.notifications.Utils.Config.NT_DELAY_TIME_MINIMUM;
import static ru.vpcb.notifications.Utils.Config.NT_FB_JOB_CHANNEL_ID;
import static ru.vpcb.notifications.Utils.Config.NT_FB_JOB_DISPATCHER_ID;
import static ru.vpcb.notifications.Utils.Config.NT_FLEXTIME_SECONDS;
import static ru.vpcb.notifications.Utils.Config.NT_RANDOM_RANGE;
import static ru.vpcb.notifications.Utils.Config.showMessage;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 13-Feb-18
 * Email: vadim.v.voronov@gmail.com
 */

public class NotificationUtils {

    private static final Random rnd = new Random();

    synchronized public static void scheduleReminder(@NonNull final Context context, FDFixture fixture) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Calendar current = Calendar.getInstance();

        Calendar next = Calendar.getInstance();
        next.setTime(fixture.getDate());

        long currentTime = TimeUnit.MILLISECONDS.toSeconds(current.getTimeInMillis());   // current time in seconds
        long matchTime = TimeUnit.MILLISECONDS.toSeconds(next.getTimeInMillis());                    // target time, clear seconds
        int scheduleTime = (int) (matchTime - currentTime);                    //

        if (scheduleTime <= NT_DELAY_TIME_MINIMUM) {
            showMessage(context, context.getString(R.string.notification_delay_time_min));
            return;
        }
// unique ID for every job based on it time and current time seconds
        String id = NT_FB_JOB_DISPATCHER_ID + (matchTime + rnd.nextInt(NT_RANDOM_RANGE));

        String s = context.getString(R.string.notification_body, fixture.getHomeTeamName(),
                fixture.getAwayTeamName(), FootballUtils.formatStringDate(context,next));

        Bundle bundle = new Bundle();
        bundle.putString(NT_BUNDLE_INTENT_NOTIFICATION_BODY, s);

        Job constraintReminderJob = dispatcher.newJobBuilder()      // ВНИМАНИЕ. Работает если приложение запущено
                /* The Service that will be used to write to preferences */
                .setService(NotificationJobService.class)                      // служба для отработки NotificationUtils
                .setTag(id)                            // уникальный тэг String
                .setConstraints(Constraint.DEVICE_CHARGING)                 // диспетчер работает только во время зарядки
                .setLifetime(Lifetime.FOREVER)                              // диспетчер работает всегда или до следующей загрузки
                .setRecurring(false)                                                     // работать постоянно вызывая notifications
                .setTrigger(Trigger.executionWindow(scheduleTime, scheduleTime + NT_FLEXTIME_SECONDS)) // старт окна // конец окна выдача будет ближе к концу
                .setReplaceCurrent(true)                                    // заменить предыдущий диспетчер
                .setExtras(bundle)
                .build();
        dispatcher.schedule(constraintReminderJob);
    }

    private static String getParam(String[] params, int index) {
        String s = params[index];
        if (s == null || s.isEmpty()) return EMPTY_LONG_DASH;
        return s;
    }


    public static void sendNotification(Context context, String sMatch) {

        int id = (int) TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTimeInMillis()) + rnd.nextInt(NT_RANDOM_RANGE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NT_FB_JOB_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.notification_small)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(sMatch)                                // time
                .setStyle(new NotificationCompat.BigTextStyle().bigText(sMatch))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(applyAction(context, id))
                .addAction(cancelAction(context, id))
                .setAutoCancel(true);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
//        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;


        notificationManager.notify(id, notificationBuilder.build());

    }

    private static NotificationCompat.Action cancelAction(Context context, int id) {
        Intent intent = new Intent(context, NotifyIntentService.class);
        intent.setAction(NT_ACTION_CANCEL);
        intent.putExtra(NT_BUNDLE_INTENT_NOTIFICATION_ID, id);
        id = id + rnd.nextInt(NT_RANDOM_RANGE);
        PendingIntent cancelPendingIntent = PendingIntent.getService(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_cancel,
                context.getString(android.R.string.cancel),
                cancelPendingIntent);
        return action;
    }

    private static NotificationCompat.Action applyAction(Context context, int id) {
        Intent intent = new Intent(context, NotifyIntentService.class);
        intent.setAction(NT_ACTION_APPLY);
        intent.putExtra(NT_BUNDLE_INTENT_NOTIFICATION_ID, id);
        id = id + rnd.nextInt(NT_RANDOM_RANGE);
        PendingIntent applyPendingIntent = PendingIntent.getService(
                context,
                id,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_check,
                context.getString(android.R.string.ok),
                applyPendingIntent);
        return action;
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                NT_ACTION_ACTIVITY_PENDING_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.drawable.notification_large);
    }


    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        notificationManager.cancelAll();
    }

    public static void clearNotifications(Context context, int id) {
        if (id <= 0) return;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        notificationManager.cancel(id);
    }

    public static void executeTask(Context context, String action, String s, int id) {
        if (NT_ACTION_APPLY.equals(action)) {
            clearNotifications(context, id);
        } else if (NT_ACTION_CANCEL.equals(action)) {
            clearNotifications(context, id);
        } else if (NT_ACTION_CREATE.equals(action)) {
            sendNotification(context, s);
        }
    }


}
