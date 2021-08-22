package com.example.babysleepcontrol.ui.eatfragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.EatData;
import com.example.babysleepcontrol.data.SleepData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLOutput;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.babysleepcontrol.enums.Constants.DAY_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.TIME_ONLY_FORMAT;

public class EatFragment extends Fragment implements View.OnClickListener {
    Button stop;
    Button leftBreast, rightBreast;
    FloatingActionButton stat;
    EatViewModel eatViewModel;
    TextView eatTxt, withoutEatTxt;
    LinearLayout chronometerLayout;
    Chronometer chronometer;
    View view;
    ScrollView scrollView;

    private boolean isRightBreast, isEatRunning;
    private long id;
    private List<EatData> eatResults;
    static String LEFT_BREAST = "ЛЕВАЯ";
    static String RIGHT_BREAST = "ПРАВАЯ";
    private long chronometerTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_eat, container, false);
        }
        this.isEatRunning = loadInstance();

        stop = view.findViewById(R.id.eat_stop_btn);
        leftBreast = view.findViewById(R.id.left_breast_btn);
        rightBreast = view.findViewById(R.id.right_breast_btn);
        stat = view.findViewById(R.id.eat_stat_btn);
        eatTxt = view.findViewById(R.id.eat_res_txt);
        withoutEatTxt = view.findViewById(R.id.without_eat_txt);
        scrollView = view.findViewById(R.id.scroll_view);
        chronometer = view.findViewById(R.id.chronometer);
        chronometerLayout = view.findViewById(R.id.chronometer_layout);

        stop.setOnClickListener(this);
        leftBreast.setOnClickListener(this);
        rightBreast.setOnClickListener(this);
        stat.setOnClickListener(this);

        eatResults = new ArrayList<>();
        eatViewModel = new ViewModelProvider(requireActivity()).get(EatViewModel.class);

        //eatViewModel.deleteAllEatData();
        init();
        return view;
    }

    private void init() {
        deactivateBreastBtns(isEatRunning);
        if (isEatRunning) {
            chronometer.setBase(chronometerTime);
            chronometer.start();
        }

        eatViewModel.setNewDate(new Date());
        Observer<List<EatData>> observer = eatData -> {
            eatResults = eatData;
            setTextResults();
        };
        eatViewModel.getDayOnlyEatData().observe(getViewLifecycleOwner(), observer);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setWithoutEatRes(boolean isNullResults) {
        if (!isNullResults && eatResults.size() > 0) {
            Date endDate = eatResults.get(eatResults.size() - 1).getEndTime();
            printTextToTxtview(endDate);
        } else {
            eatViewModel.getMaxIdNote();
            eatViewModel.getEatDataSingle()
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<EatData>() {
                        @Override
                        public void onSuccess(@NotNull EatData item) {
                            setWaitTxt(item);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        }
    }

    private void printTextToTxtview(Date endDate) {
        if (endDate != null) {
            long milliseconds = new Date().getTime() - endDate.getTime();
            int resHours = (int) (milliseconds / (60 * 60 * 1000));
            int resMinutes = (int) (milliseconds - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);
            withoutEatTxt.setText("На подсосе:  " + String.format("%02dh. %02dm.", resHours, resMinutes));
        }
    }

    private void setWaitTxt(EatData item) {
        Date endDate = null;
        if (item != null) {
            endDate = item.getEndTime();
        }
        if (item == null || endDate == null) {
            try {
                endDate = new Date();
                endDate = DAY_YEAR_ONLY_FORMAT.parse(DAY_YEAR_ONLY_FORMAT.format(endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert endDate != null;
        }
        printTextToTxtview(endDate);
    }


    private void setTextResults() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        Date firstDay = new Date();
        if (eatResults.size() > 0) {
            for (EatData data : eatResults) {
                if (i < 1) {
                    firstDay = data.getStartTime();
                    String firstDayStr = DAY_ONLY_FORMAT.format(firstDay);
                    builder.append("--==  " + firstDayStr + "  ==-- \n");
                }
                i++;
                if (data.getStartTime().getDate() != firstDay.getDate()) {
                    firstDay = data.getStartTime();
                    String firstDayStr = DAY_ONLY_FORMAT.format(firstDay);
                    builder.append("--==  " + firstDayStr + "  ==-- \n");
                }
                String breast = data.getRightBreast() ? RIGHT_BREAST : LEFT_BREAST;
                String endTime = data.getEndTime() == null ? " --:--" : TIME_ONLY_FORMAT.format(data.getEndTime());
                String startTime = TIME_ONLY_FORMAT.format(data.getStartTime());
                String resStr = data.getResult();
                builder.append(breast + " , " + "Interval: " + startTime
                        + " - " + endTime + " , res: " + resStr + "\n");
            }
        } else {
            builder.append("No data yet...");
        }
        eatTxt.setText(builder.toString());

        if (!isEatRunning) {
            setWithoutEatRes(eatResults.size() <= 0);
        } else withoutEatTxt.setText("-= Едим !!!! =-");
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_breast_btn:
                startEat(false);
                break;
            case R.id.right_breast_btn:
                startEat(true);
                break;
            case R.id.eat_stop_btn:
                stopEat();
                break;
            case R.id.eat_stat_btn:
                showStatistic();
                break;
        }
    }

    private void showStatistic() {

        Date dateStart;
        Calendar instance = Calendar.getInstance();
        Date dateEnd = new Date();
        instance.setTime(dateEnd);
        instance.add(Calendar.DAY_OF_MONTH, 1);
        dateEnd = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -(7));
        dateStart = instance.getTime();

        eatViewModel.setDataByPeriod(dateStart, dateEnd);

        eatViewModel.getDataByPeriod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<EatData>>() {
                    @Override
                    public void onSuccess(@NonNull List<EatData> eatData) {
                        eatResults.clear();
                        eatResults.addAll(eatData);
                        setTextResults();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "SOMETHING GOING WRONG?..", Toast.LENGTH_LONG).show();
                    }
                });

    }


    private void startEat(boolean isRightBreast) {
        this.isEatRunning = true;
        this.isRightBreast = isRightBreast;
        saveInstance(true);

        Date start = new Date();
        EatData eatData = new EatData(start, null, null, null, isRightBreast);
        this.id = eatViewModel.insert(eatData);
        // startForegroundService(start);
        deactivateBreastBtns(true);

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        scrollView.post(() -> scrollView.smoothScrollTo(0, eatTxt.getBottom()));
    }


    private void stopEat() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();
        this.isEatRunning = false;
        saveInstance(false);
       // if (id != 0) {
            //  eatViewModel.getNoteById(id);
            eatViewModel.getMaxIdNote();
            eatViewModel.getEatDataSingle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<EatData>() {
                        @Override
                        public void onSuccess(@NotNull EatData item) {
                            item.setEndTime(new Date());
                            eatViewModel.update(item);
//                             requireActivity().stopService(new Intent(getContext(), ForegroundNotificationService.class));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "SOMETHING GOING WRONG?..", Toast.LENGTH_LONG).show();
                        }
                    });
      //  }
        deactivateBreastBtns(false);
    }


    private void deactivateBreastBtns(boolean isDeactivate) {
        if (isDeactivate) {
            leftBreast.setClickable(false);
            rightBreast.setClickable(false);
            leftBreast.setAlpha(0.15f);
            rightBreast.setAlpha(0.15f);
            stop.setClickable(true);
            stop.setAlpha(1);
            chronometerLayout.setAlpha(0.9f);
            chronometerLayout.setBackground(isRightBreast ? rightBreast.getBackground() : leftBreast.getBackground());
        } else {
            leftBreast.setClickable(true);
            rightBreast.setClickable(true);
            stop.setClickable(false);
            stop.setAlpha(0.15f);
            leftBreast.setAlpha(0.65f);
            rightBreast.setAlpha(0.65f);
            chronometerLayout.setAlpha(0.05f);
            chronometerLayout.setBackground(null);
        }
    }

    private void saveInstance(boolean isEatRunning) {
        SharedPreferences sp = requireContext().getSharedPreferences("2", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isEatRunning", isEatRunning);
        editor.putBoolean("isRightBeast", isRightBreast);

        editor.putLong("Id2", id);
        editor.putLong("ChronometerTime", chronometer.getBase());
        editor.apply();
    }

    private boolean loadInstance() {
        SharedPreferences sp = requireContext().getSharedPreferences("2", Context.MODE_PRIVATE);
        this.id = sp.getLong("Id2", 0);
        this.isRightBreast = sp.getBoolean("isRightBeast", false);
        this.chronometerTime = sp.getLong("ChronometerTime", 0);
        return sp.getBoolean("isEatRunning", false);
    }

    @Override
    public void onStop() {
        saveInstance(isEatRunning);
        super.onStop();
    }

    @Override
    public void onResume() {
        System.out.println("On resume");
        this.isEatRunning = loadInstance();
        init();
        super.onResume();
    }


}