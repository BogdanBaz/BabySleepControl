package com.example.babysleepcontrol.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.babysleepcontrol.dao.SleepDao;
import com.example.babysleepcontrol.data.SleepData;
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

public class SleepRepository {

    private final SleepDao sleepDao;
    private final MutableLiveData<List<SleepData>> dayOnlySleepData;
    private Single<SleepData> sleepDataSingle;
    private Single<List<SleepData>> dataByPeriod;
    private String newDate;
    private String nextDate;
    private Disposable mDisposable;


    public SleepRepository(Application application) {
        SleepDatabase database = SleepDatabase.getInstance(application);
        sleepDao = database.sleepDao();
        dayOnlySleepData = new MutableLiveData<>();
    }

    public MutableLiveData<List<SleepData>> getDayOnlySleepData() {
        return dayOnlySleepData;
    }

    public void setNewDate(Date newDate) {
        if (mDisposable != null) {
            Log.d("TAG", "DISPOSE ");
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
        mDisposable = sleepDao.getTodaySleepData(newDate, nextDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<SleepData>>() {
                    @Override
                    public void accept(List<SleepData> sleepData) throws Exception {
                        dayOnlySleepData.postValue(sleepData);
                    }
                });
    }

    public long insert(SleepData sleepData) throws ExecutionException, InterruptedException {

        Callable<Long> callable = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return sleepDao.insert(sleepData);
            }
        };

        Future<Long> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
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

    public void getNotesById(long id) {
        this.sleepDataSingle = sleepDao.getNoteById(id);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    public void getMaxIdNote() {
        this.sleepDataSingle = sleepDao.getMaxIdNote();
    }

    public void setDataByPeriod(Date dateStart, Date dateEnd) {
        newDate = DAY_YEAR_ONLY_FORMAT.format(dateStart);
        nextDate = DAY_YEAR_ONLY_FORMAT.format(dateEnd);
        Log.d("TAG ", "IN REPOSITORY newDate " + newDate + "nextDate " + nextDate);
        this.dataByPeriod = sleepDao.getSleepDataByPeriod(newDate, nextDate);
    }

    public Single<List<SleepData>> getDataByPeriod() {
        return dataByPeriod;
    }

    public Single<SleepData> getSleepDataSingle() {
        return sleepDataSingle;
    }
}
