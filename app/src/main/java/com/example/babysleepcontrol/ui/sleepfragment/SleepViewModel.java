package com.example.babysleepcontrol.ui.sleepfragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.repositories.SleepRepository;

import java.util.List;

public class SleepViewModel extends AndroidViewModel {

    private SleepRepository repository;
    private LiveData<List<SleepData>> allSleepData;

    public SleepViewModel(@NonNull Application application) {
        super(application);
        repository = new SleepRepository(application);
        allSleepData = repository.getAllSleepData();
    }

    public void insert(SleepData sleepData) {
        repository.insert(sleepData);
    }

    public void update(SleepData sleepData) {
        repository.update(sleepData);
    }

    public void delete(SleepData sleepData) {
        repository.delete(sleepData);
    }

    public void deleteAllSleepData() {
        repository.deleteAllSleepData();
    }

    public LiveData<List<SleepData>> getAllSleepData() {
        return allSleepData;
    }
}
