package com.timothee.reveilflash;

import android.app.*;
import android.content.*;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "alarms_channel";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        long id = intent.getLongExtra("alarm_id", -1);
        Alarm a = AlarmStore.findById(ctx, id);
        if (a == null || !a.enabled) return;

        createChannel(ctx);

        Intent full = new Intent(ctx, FlashActivity.class);
        full.putExtra("alarm_id", a.id);
        full.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fullPi = PendingIntent.getActivity(ctx, (int) a.id, full,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new Notification.Builder(ctx, CHANNEL_ID)
                .setContentTitle("Réveil")
                .setContentText(String.format("%02d:%02d", a.hour, a.minute))
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setFullScreenIntent(fullPi, true)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) a.id, notif);

        boolean anyDay = false;
        for (boolean d : a.days) if (d) anyDay = true;
        if (!anyDay) {
            a.enabled = false; // ponctuel -> se désactive
        }
        AlarmStore.update(ctx, a);
        if (a.enabled) AlarmScheduler.schedule(ctx, a); // reprogramme la semaine suivante
    }

    static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Réveils",
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Alertes de réveil flash");
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }
}
