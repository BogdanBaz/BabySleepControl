package com.example.babysleepcontrol.data;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.babysleepcontrol.database.TimestampConverter;

import java.util.Calendar;
import java.util.Date;

import static com.example.babysleepcontrol.enums.ConstantsEnum.END_NIGHT;
import static com.example.babysleepcontrol.enums.ConstantsEnum.START_NIGHT;

@Entity(tableName = "sleep_table")
public class SleepData {
    public void setId(long id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private long id;

    @TypeConverters({TimestampConverter.class})
    private Date startTime;

    @TypeConverters({TimestampConverter.class})
    private Date endTime;

    String result;

    String notes;

    public void setResult() {
        this.result = calculateRes();
    }

    public void setDay(Boolean day) {
        isDay = day;
    }

    private Boolean isDay;

    public SleepData(Date startTime, Date endTime, String result, String notes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDay = calculateIsDay();

        if (result != null && endTime != null) {
            Log.d("SLEEP_DATA CLASS", "SET RESULT FROM CONSTRUCTOR  " + result);
            this.result = result;
        } else {
            this.result = calculateRes();
        }
        if (notes == null) {
            this.notes = "";
        } else
            this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        this.result = calculateRes();
    }

    public String getResult() {
        return result;
    }

    public Boolean getIsDay() {
        return isDay;
    }

    public void setIsDay() {
        this.isDay = calculateIsDay();
    }

    public long getId() {
        return id;
    }

    private Boolean calculateIsDay() {
        Calendar start = Calendar.getInstance();
        start.setTime(startTime);

        Boolean booleanIsDay = (start.get(Calendar.HOUR_OF_DAY) < START_NIGHT.getValue() &&
                start.get(Calendar.HOUR_OF_DAY) > END_NIGHT.getValue());
        return booleanIsDay;
    }

    @SuppressLint("DefaultLocale")
    private String calculateRes() {
        long milliseconds;
        if (endTime != null) {
            milliseconds = endTime.getTime() - startTime.getTime();
        } else milliseconds = new Date().getTime() - startTime.getTime();

        int resHours = (int) (milliseconds / (60 * 60 * 1000));
        int resMinutes = (int) (milliseconds - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);

        return String.format("%02dh. %02dm.", resHours, resMinutes);
    }
}
