package com.timothee.reveilflash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        List<Alarm> all = AlarmStore.loadAll(ctx);
        for (Alarm a : all) if (a.enabled) AlarmScheduler.schedule(ctx, a);
    }
}
