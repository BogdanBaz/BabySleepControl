package com.example.babysleepcontrol.data;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.babysleepcontrol.database.TimestampConverter;

import java.util.Date;

@Entity(tableName = "eat_table")
public class EatData {
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

    Boolean isRightBreast;

    public EatData(Date startTime, Date endTime, String result, String notes, Boolean isRightBreast) {
        this.startTime = startTime;
        this.endTime = endTime;

        if (result != null && endTime != null) {
            this.result = result;
        } else {
            this.result = calculateRes();
        }
        if (notes == null) {
            this.notes = "";
        } else
            this.notes = notes;

        this.isRightBreast = isRightBreast;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
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

    public void setResult(String result) {
        this.result = result;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getRightBreast() {
        return isRightBreast;
    }

    public void setRightBreast(Boolean rightBreast) {
        isRightBreast = rightBreast;
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

    public long getId() {
        return id;
    }
}
