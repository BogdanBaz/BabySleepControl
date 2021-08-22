package com.example.babysleepcontrol.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.babysleepcontrol.data.SleepData;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface  SleepDao{
    @Insert
    long insert(SleepData sleepData);

    @Update
    void update(SleepData sleepData);

    @Delete
    void delete(SleepData sleepData);

    @Query("DELETE FROM sleep_table")
    void deleteAllNotes();

    @Query("SELECT * FROM sleep_table ")
    LiveData<List<SleepData>> getAllNotes();

   @Query("SELECT * FROM sleep_table WHERE startTime BETWEEN date(:day) AND date(:nextDay)")
    Flowable<List<SleepData>> getTodaySleepData (String day, String nextDay);

    @Query("SELECT * FROM sleep_table WHERE startTime BETWEEN date(:day) AND date(:nextDay)")
    Single<List<SleepData>> getSleepDataByPeriod(String day, String nextDay);

    @Query("SELECT * FROM sleep_table WHERE id = :id")
    Single<SleepData> getNoteById (long id);

    @Query("SELECT * FROM sleep_table WHERE   id = (SELECT MAX(ID)  FROM sleep_table) ")
    Single<SleepData> getMaxIdNote ();
}
