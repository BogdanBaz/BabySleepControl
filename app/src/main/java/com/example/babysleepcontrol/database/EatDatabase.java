package com.example.babysleepcontrol.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.babysleepcontrol.dao.EatDao;
import com.example.babysleepcontrol.dao.SleepDao;
import com.example.babysleepcontrol.data.EatData;

@Database(entities = {EatData.class}, version = 1)
@TypeConverters({TimestampConverter.class})
public abstract class EatDatabase extends RoomDatabase {

    private static EatDatabase instance;
    public abstract EatDao eatDao();
    public static synchronized EatDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    EatDatabase.class, "eat_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}