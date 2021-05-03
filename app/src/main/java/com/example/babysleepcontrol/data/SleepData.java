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

    public void setDay(Boolean day) {
        isDay = day;
    }

    private Boolean isDay;

    public SleepData(Date startTime, Date endTime, String result) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDay = calculateIsDay();

        if (endTime == null) {
            this.result = "waiting for end of sleeping..";
        } else if (result != null) {
            Log.d("SLEEP_DATA CLASS", "SET RESULT FROM CONSTRUCTOR  " + result);
            this.result = result;
        } else {
            this.result = calculateRes();
        }
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
        long milliseconds = endTime.getTime() - startTime.getTime();
        int resHours = (int) (milliseconds / (60 * 60 * 1000));
        int resMinutes = (int) (milliseconds - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);

        return String.format("%02d", resHours) + "h. " + String.format("%02d", resMinutes) + "m.";
    }
}
