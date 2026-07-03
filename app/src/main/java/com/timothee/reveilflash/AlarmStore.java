package com.timothee.reveilflash;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AlarmStore {
    private static final String PREFS = "reveil_flash_prefs";
    private static final String KEY = "alarms_json";

    public static List<Alarm> loadAll(Context ctx) {
        List<Alarm> list = new ArrayList<>();
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Alarm a = new Alarm();
                a.id = o.getLong("id");
                a.hour = o.getInt("hour");
                a.minute = o.getInt("minute");
                a.enabled = o.getBoolean("enabled");
                String days = o.getString("days");
                for (int d = 0; d < 7; d++) a.days[d] = days.charAt(d) == '1';
                list.add(a);
            }
        } catch (Exception e) { /* ignore */ }
        return list;
    }

    public static void saveAll(Context ctx, List<Alarm> alarms) {
        JSONArray arr = new JSONArray();
        try {
            for (Alarm a : alarms) {
                JSONObject o = new JSONObject();
                o.put("id", a.id);
                o.put("hour", a.hour);
                o.put("minute", a.minute);
                o.put("enabled", a.enabled);
                StringBuilder sb = new StringBuilder();
                for (boolean d : a.days) sb.append(d ? '1' : '0');
                o.put("days", sb.toString());
                arr.put(o);
            }
        } catch (Exception e) { /* ignore */ }
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, arr.toString()).apply();
    }

    public static Alarm findById(Context ctx, long id) {
        for (Alarm a : loadAll(ctx)) if (a.id == id) return a;
        return null;
    }

    public static void update(Context ctx, Alarm updated) {
        List<Alarm> all = loadAll(ctx);
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id == updated.id) { all.set(i, updated); break; }
        }
        saveAll(ctx, all);
    }
}
