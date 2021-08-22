package com.example.babysleepcontrol.ui.eatfragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.babysleepcontrol.data.EatData;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.repositories.EatRepository;
import com.example.babysleepcontrol.repositories.SleepRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import io.reactivex.Single;

public class EatViewModel  extends AndroidViewModel {

    private final EatRepository  repository;
    private final MutableLiveData<List<EatData>> dayOnlyEatData;
    private Single<EatData> eatDataSingle;
    private Single<List<EatData>> dataByPeriod;

    public void setDataByPeriod(Date dateStart, Date dateEnd) {
        repository.setDataByPeriod(dateStart, dateEnd);
        this.dataByPeriod = repository.getDataByPeriod();
    }

    public Single<List<EatData>> getDataByPeriod() {
        return dataByPeriod;
    }

    public Single<EatData> getEatDataSingle() {
        return eatDataSingle;
    }

    public EatViewModel(@NonNull Application application) {
        super(application);
        repository = new EatRepository(application);
        dayOnlyEatData = repository.getDayOnlyEatData();
    }

    public long insert(EatData eatData) {
        try {
            return repository.insert(eatData);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void update(EatData eatData) {
        repository.update(eatData);
    }

    public void delete(EatData eatData) {
        repository.delete(eatData);
    }

    public LiveData<List<EatData>> getDayOnlyEatData() {
        return dayOnlyEatData;
    }

    public void setNewDate(Date newDate) {
        repository.setNewDate(newDate);
    }


    public void getNoteById(long id) {
        repository.getNotesById(id);
        this.eatDataSingle = repository.getEatDataSingle();
    }

    public void getMaxIdNote() {
        repository.getMaxIdNote();
        this.eatDataSingle = repository.getEatDataSingle();
    }

    public void deleteAllEatData() {
        repository.deleteAllEatData();
    }



}
