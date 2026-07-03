package com.timothee.reveilflash;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class AlarmScheduler {

    public static void schedule(Context ctx, Alarm a) {
        Calendar next = computeNext(a);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("alarm_id", a.id);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, (int) a.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
    }

    public static void cancel(Context ctx, Alarm a) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, (int) a.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    // Prochaine occurrence. Si aucun jour coché = une seule fois, prochaine heure/minute.
    private static Calendar computeNext(Alarm a) {
        Calendar now = Calendar.getInstance();
        boolean anyDay = false;
        for (boolean d : a.days) if (d) anyDay = true;

        for (int i = 0; i < 8; i++) {
            Calendar c = (Calendar) now.clone();
            c.add(Calendar.DAY_OF_YEAR, i);
            c.set(Calendar.HOUR_OF_DAY, a.hour);
            c.set(Calendar.MINUTE, a.minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() <= now.getTimeInMillis()) continue;

            if (!anyDay) return c; // une seule fois : prochaine occurrence, peu importe le jour

            int javaDay = c.get(Calendar.DAY_OF_WEEK); // 1=dimanche..7=samedi
            int idx = (javaDay + 5) % 7; // 0=lundi..6=dimanche
            if (a.days[idx]) return c;
        }
        return now; // sécurité, ne devrait pas arriver
    }
}
