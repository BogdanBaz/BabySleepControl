package com.example.babysleepcontrol.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.babysleepcontrol.dao.SleepDao;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.database.SleepDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class SleepRepository {

    private SleepDao sleepDao;
    private LiveData<List<SleepData>> allSleepData;

    public SleepRepository(Application application) {
        SleepDatabase database = SleepDatabase.getInstance(application);
        sleepDao = database.sleepDao();
        allSleepData = sleepDao.getAllNotes();
    }

    public void insert(SleepData sleepData) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                sleepDao.insert(sleepData);
            }
        });
    }

    public void update(SleepData sleepData) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                sleepDao.update(sleepData);
            }
        });
    }

    public void delete(SleepData sleepData) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                sleepDao.delete(sleepData);
            }
        });
    }


    public void deleteAllSleepData() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                sleepDao.deleteAllNotes();
            }
        });
    }

    public LiveData<List<SleepData>> getAllSleepData() {
        return allSleepData;
    }
}
