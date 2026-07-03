package com.timothee.reveilflash;

import android.app.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmReceiver.createChannel(this);

        listContainer = findViewById(R.id.list_container);
        findViewById(R.id.btn_add).setOnClickListener(v -> showAddDialog());
        refreshList();
    }

    @Override
    protected void onResume() { super.onResume(); refreshList(); }

    private void refreshList() {
        listContainer.removeAllViews();
        List<Alarm> alarms = AlarmStore.loadAll(this);
        for (Alarm a : alarms) {
            View row = getLayoutInflater().inflate(R.layout.item_alarm, listContainer, false);
            TextView time = row.findViewById(R.id.txt_time);
            TextView days = row.findViewById(R.id.txt_days);
            Switch sw = row.findViewById(R.id.switch_enabled);
            Button del = row.findViewById(R.id.btn_delete);

            time.setText(String.format("%02d:%02d", a.hour, a.minute));
            days.setText(daysLabel(a.days));
            sw.setChecked(a.enabled);

            sw.setOnCheckedChangeListener((btn, checked) -> {
                a.enabled = checked;
                AlarmStore.update(this, a);
                if (checked) AlarmScheduler.schedule(this, a);
                else AlarmScheduler.cancel(this, a);
            });

            del.setOnClickListener(v -> {
                AlarmScheduler.cancel(this, a);
                List<Alarm> all = AlarmStore.loadAll(this);
                all.removeIf(x -> x.id == a.id);
                AlarmStore.saveAll(this, all);
                refreshList();
            });

            listContainer.addView(row);
        }
    }

    private String daysLabel(boolean[] d) {
        String[] labels = {"L","M","M","J","V","S","D"};
        boolean any = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) { if (d[i]) { any = true; sb.append(labels[i]).append(" "); } }
        return any ? sb.toString().trim() : "Une seule fois";
    }

    private void showAddDialog() {
    View v = getLayoutInflater().inflate(R.layout.dialog_add_alarm, null);
    NumberPicker pickerHour = v.findViewById(R.id.picker_hour);
    NumberPicker pickerMinute = v.findViewById(R.id.picker_minute);
    pickerHour.setMinValue(0);
    pickerHour.setMaxValue(23);
    pickerMinute.setMinValue(0);
    pickerMinute.setMaxValue(59);
    pickerMinute.setFormatter(n -> String.format("%02d", n));

    CheckBox[] boxes = {
        v.findViewById(R.id.cb_day0), v.findViewById(R.id.cb_day1),
        v.findViewById(R.id.cb_day2), v.findViewById(R.id.cb_day3),
        v.findViewById(R.id.cb_day4), v.findViewById(R.id.cb_day5),
        v.findViewById(R.id.cb_day6)
    };

    new AlertDialog.Builder(this)
        .setTitle("Nouveau réveil")
        .setView(v)
        .setPositiveButton("Enregistrer", (dialog, which) -> {
            Alarm a = new Alarm();
            a.id = System.currentTimeMillis();
            a.hour = pickerHour.getValue();
            a.minute = pickerMinute.getValue();
            for (int i = 0; i < 7; i++) a.days[i] = boxes[i].isChecked();
            a.enabled = true;

            List<Alarm> all = AlarmStore.loadAll(this);
            all.add(a);
            AlarmStore.saveAll(this, all);
            AlarmScheduler.schedule(this, a);
            refreshList();
        })
        .setNegativeButton("Annuler", null)
        .show();
    }
}
