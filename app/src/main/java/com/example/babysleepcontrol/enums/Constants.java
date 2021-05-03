package com.example.babysleepcontrol.enums;

import java.text.SimpleDateFormat;
import java.util.Locale;

public  class Constants {
    public static  SimpleDateFormat DAY_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    public static  SimpleDateFormat DAY_YEAR_ONLY_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    public static  SimpleDateFormat DAY_ONLY_FORMAT = new SimpleDateFormat("dd.MM", Locale.getDefault());
    public static  SimpleDateFormat TIME_ONLY_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static  SimpleDateFormat HOUR_ONLY_FORMAT = new SimpleDateFormat("HH", Locale.getDefault());
    public static  SimpleDateFormat MINUTE_ONLY_FORMAT = new SimpleDateFormat("mm", Locale.getDefault());
}
