package com.example.babysleepcontrol.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.babysleepcontrol.data.EatData;
import com.example.babysleepcontrol.data.SleepData;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface EatDao {

        @Query("DELETE FROM eat_table")
        void deleteAllNotes();

        @Insert
        long insert(EatData eatData);

        @Update
        void update(EatData eatData);

        @Delete
        void delete(EatData eatData);

        @Query("SELECT * FROM eat_table ")
        LiveData<List<EatData>> getAllEatNotes();

        @Query("SELECT * FROM eat_table WHERE startTime BETWEEN date(:day) AND date(:nextDay)")
        Flowable<List<EatData>> getTodayEatData (String day, String nextDay);

        @Query("SELECT * FROM eat_table WHERE startTime BETWEEN date(:day) AND date(:nextDay)")
        Single<List<EatData>> getPeriodEatData (String day, String nextDay);

        @Query("SELECT * FROM eat_table WHERE id = :id")
        Single<EatData> getNoteById (long id);

        @Query("SELECT * FROM eat_table WHERE   id = (SELECT MAX(ID)  FROM eat_table) ")
        Single<EatData> getMaxIdNote ();
}
