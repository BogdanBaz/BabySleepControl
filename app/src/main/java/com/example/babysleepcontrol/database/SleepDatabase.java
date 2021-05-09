package com.example.babysleepcontrol.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.babysleepcontrol.dao.SleepDao;
import com.example.babysleepcontrol.data.SleepData;

@Database(entities = {SleepData.class}, version = 3)
@TypeConverters({TimestampConverter.class})
public abstract class SleepDatabase extends RoomDatabase {

        private static SleepDatabase instance;
        public abstract SleepDao sleepDao();
        public static synchronized SleepDatabase getInstance(Context context) {
            if (instance == null) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        SleepDatabase.class, "sleep_database")
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return instance;
        }
}
