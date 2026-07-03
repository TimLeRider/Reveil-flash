package com.timothee.reveilflash;

public class Alarm {
    public long id;
    public int hour;
    public int minute;
    public boolean[] days = new boolean[7]; // 0=Lundi ... 6=Dimanche
    public boolean enabled = true;
}
