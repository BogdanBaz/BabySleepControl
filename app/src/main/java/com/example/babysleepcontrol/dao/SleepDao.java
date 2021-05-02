package com.example.babysleepcontrol.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.babysleepcontrol.data.SleepData;

import java.util.List;

@Dao
public interface  SleepDao{
    @Insert
    void insert(SleepData sleepData);

    @Update
    void update(SleepData sleepData);

    @Delete
    void delete(SleepData sleepData);

    @Query("DELETE FROM sleep_table")
    void deleteAllNotes();

    @Query("SELECT * FROM sleep_table ")
    LiveData<List<SleepData>> getAllNotes();
}
