package com.example.babysleepcontrol.database;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import static com.example.babysleepcontrol.enums.Constants.DAY_TIME_FORMAT;


public class TimestampConverter {

    static DateFormat df = DAY_TIME_FORMAT;

    @TypeConverter
    public static Date fromTimestamp(String value) {
        if (value != null) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @TypeConverter
    public static String dateToTimestamp(Date date) {
        if (date != null) {
            return df.format(date);
        } else {
            return null;
        }
    }
}
