package com.example.babysleepcontrol.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.ui.sleepfragment.SleepFragment;
import com.example.babysleepcontrol.ui.sleepfragment.SleepViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

import static com.example.babysleepcontrol.enums.Constants.DAY_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.YEAR_ONLY_FORMAT;

public class PieChartFragment extends Fragment {
    PieChartView chartView;
    SleepViewModel sleepViewModel;
    List<SleepData> dataList = new ArrayList<>();
    Map<String, Integer> mapResultSleeping = new HashMap<>();
    final String DAY_KEY = "day";
    final String NIGHT_KEY = "night";
    TextView dataTxt, periodTxt;
    Button oneDay, sevenDay, month ;
    int numOfDays = 1;
    Date dateStart;
    SleepFragment sleepFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);


        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                startFragment();
                return true;
            }
            return false;
        });


        chartView = view.findViewById(R.id.pie_chart);
        dataTxt = view.findViewById(R.id.data_statistic_txt);
        periodTxt = view.findViewById(R.id.period_txt);
        oneDay = view.findViewById(R.id.one_day_btn);
        sevenDay = view.findViewById(R.id.seven_day_btn);
        month = view.findViewById(R.id.month_btn);


        oneDay.setOnClickListener(v -> {
            numOfDays = 1;
            setPeriod();
        });

        sevenDay.setOnClickListener(v -> {
            numOfDays = 7;
            setPeriod();
        });

        month.setOnClickListener(v -> {
            numOfDays = 30;
            setPeriod();
        });

        sleepViewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);

        initData();
        return view;
    }

    private void initData() {
        List<Fragment> fragments = getParentFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof SleepFragment) {
                sleepFragment = (SleepFragment) fragment;
            }
        }
        setPeriod();
    }

    private void setPeriod() {
        Calendar instance = Calendar.getInstance();
        Date dateEnd = new Date();
        instance.setTime(dateEnd);
        if (numOfDays == 1) {
            instance.add(Calendar.DAY_OF_MONTH, 1);
            this.dateStart = new Date();
            dateEnd = instance.getTime();
        } else {
            instance.add(Calendar.DAY_OF_MONTH, -(numOfDays - 1));
            this.dateStart = instance.getTime();
        }
        sleepViewModel.setDataByPeriod(dateStart, dateEnd);

        sleepViewModel.getDataByPeriod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<SleepData>>() {
                    @Override
                    public void onSuccess(@NonNull List<SleepData> sleepData) {
                        dataList.clear();
                        dataList.addAll(sleepData);
                        Log.d("TAG ", "OBSERVER , size - " + dataList.size());
                        showData();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(getContext(), "SOMETHING GOING WRONG?..", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void showData() {
        mapResultSleeping.put(DAY_KEY, 0);
        mapResultSleeping.put(NIGHT_KEY, 0);

        Log.d("TAG ", "LIST_SIZE " + dataList.size());

        if (dataList.size() > 0) {
            for (SleepData sleepData : dataList) {

                String[] itemRes = sleepData.getResult().replaceAll("[^0-9?!.]", "").split("\\.");
                int minutes = (Integer.parseInt(itemRes[1]) + Integer.parseInt(itemRes[0]) * 60);
                int resMin;
                if (sleepData.getIsDay()) {
                    resMin = mapResultSleeping.get(DAY_KEY) + minutes;
                    mapResultSleeping.put(DAY_KEY, resMin);
                } else {
                    resMin = mapResultSleeping.get(NIGHT_KEY) + minutes;
                    mapResultSleeping.put(NIGHT_KEY, resMin);
                }
            }
            int averageDay = mapResultSleeping.get(DAY_KEY) / numOfDays;
            int averageNight = mapResultSleeping.get(NIGHT_KEY) / numOfDays;
            setPie(averageDay, averageNight);
            @SuppressLint("DefaultLocale")
            String resStr = "Средний дневной сон = " +
                    String.format("%02dh. %02dm.", averageDay / 60, averageDay % 60) +
                    "\n\n Средний ночной сон = " + String.format("%02dh. %02dm.", averageNight / 60, averageNight % 60) +
                    "\n\n period = " + numOfDays;
            dataTxt.setText(resStr);
        } else {
            dataTxt.setText("NO DATA YET...");
            setPie(0, 0);
        }

        periodTxt.setText("period " + DAY_ONLY_FORMAT.format(dateStart) + " : " + DAY_ONLY_FORMAT.format(new Date())
                + " (" + YEAR_ONLY_FORMAT.format(new Date()) + ")");

    }

    private void setPie(int averageDay, int averageNight) {
        List<SliceValue> pieData = new ArrayList<>();
        pieData.add(new SliceValue(averageDay, Color.YELLOW));
        pieData.add(new SliceValue(averageNight, Color.DKGRAY));
        pieData.add(new SliceValue(1440 - (averageDay + averageNight), Color.WHITE));
        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(7);
        pieChartData.setHasCenterCircle(true).setCenterText1("STAT. PIE CHART")
                .setCenterText1FontSize(15)
                .setCenterText1Color(Color.parseColor("#FFFFFF"));
        chartView.setPieChartData(pieChartData);
    }

    private void startFragment() {

        //sleepFragment.hideButtons(false, STATISTIC_BTN);
        sleepFragment.hideContainerButtons(true);
        getParentFragmentManager().popBackStackImmediate();
    }
}