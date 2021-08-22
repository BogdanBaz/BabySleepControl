package com.example.babysleepcontrol.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.babysleepcontrol.dao.EatDao;
import com.example.babysleepcontrol.dao.SleepDao;
import com.example.babysleepcontrol.data.EatData;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.database.EatDatabase;
import com.example.babysleepcontrol.database.SleepDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;

public class EatRepository {

    private final EatDao eatDao;
    private final MutableLiveData<List<EatData>> dayOnlyEatData;
    private Single<List<EatData>> eatDataByPeriod;
    private Single<EatData> eatDataSingle;
    private String newDate;
    private String nextDate;
    private Disposable mDisposable;


    public EatRepository(Application application) {
        EatDatabase database = EatDatabase.getInstance(application);
        eatDao = database.eatDao();
        dayOnlyEatData = new MutableLiveData<>();
    }

    public void setNewDate(Date newDate) {
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        Calendar instance = Calendar.getInstance();
        instance.setTime(newDate);
        instance.add(Calendar.DAY_OF_MONTH, 1);
        Date nextDay = instance.getTime();

        this.newDate = DAY_YEAR_ONLY_FORMAT.format(newDate);
        this.nextDate = DAY_YEAR_ONLY_FORMAT.format(nextDay);
        init();
    }

    private void init() {
        mDisposable = eatDao.getTodayEatData(newDate, nextDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<EatData>>() {
                    @Override
                    public void accept(List<EatData> eatData) throws Exception {
                        dayOnlyEatData.postValue(eatData);
                    }
                });
    }

    public MutableLiveData<List<EatData>> getDayOnlyEatData() {
        return dayOnlyEatData;
    }

    public long insert(EatData eatData) throws ExecutionException, InterruptedException {

        Callable<Long> callable = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return eatDao.insert(eatData);
            }
        };

        Future<Long> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public void update(EatData eatData) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                eatDao.update(eatData);
            }
        });
    }

    public void delete(EatData eatData) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                eatDao.delete(eatData);
            }
        });
    }

    public void getNotesById(long id) {
        this.eatDataSingle = eatDao.getNoteById(id);
    }

    public void getMaxIdNote () {
        this.eatDataSingle = eatDao.getMaxIdNote();
    }

    public void setDataByPeriod(Date dateStart, Date dateEnd) {
        newDate = DAY_YEAR_ONLY_FORMAT.format(dateStart);
        nextDate = DAY_YEAR_ONLY_FORMAT.format(dateEnd);
        this.eatDataByPeriod = eatDao.getPeriodEatData(newDate, nextDate);
    }

    public Single<List<EatData>> getDataByPeriod() {
        return eatDataByPeriod;
    }

    public Single<EatData> getEatDataSingle() {
        return eatDataSingle;
    }

    public void deleteAllEatData() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                eatDao.deleteAllNotes();
            }
        });
    }

}
