package com.example.babysleepcontrol.ui.sleepfragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.repositories.SleepRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Single;

public class SleepViewModel extends AndroidViewModel {

    private final SleepRepository repository;
    private final MutableLiveData<List<SleepData>> dayOnlySleepData;
    private Single<SleepData> sleepDataSingle;
    private Single<List<SleepData>> dataByPeriod;

    public void setDataByPeriod(Date dateStart, Date dateEnd) {
        repository.setDataByPeriod(dateStart, dateEnd);
        this.dataByPeriod = repository.getDataByPeriod();
    }

    public Single<List<SleepData>> getDataByPeriod() {
        return dataByPeriod;
    }

    public Single<SleepData> getSleepDataSingle() {
        return sleepDataSingle;
    }

    public SleepViewModel(@NonNull Application application) {
        super(application);
        repository = new SleepRepository(application);
        dayOnlySleepData = repository.getDayOnlySleepData();
    }

    public long insert(SleepData sleepData) {
        try {
            return repository.insert(sleepData);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
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


    public LiveData<List<SleepData>> getDayOnlySleepData() {
        return dayOnlySleepData;
    }

    public void setNewDate(Date newDate) {
        repository.setNewDate(newDate);
    }


    public void getNoteById(long id) {
        repository.getNotesById(id);
        this.sleepDataSingle = repository.getSleepDataSingle();
    }

    //////////////////////////
    public void getMaxIdNote() {
        repository.getMaxIdNote();
        this.sleepDataSingle = repository.getSleepDataSingle();
    }

}
