package com.example.babysleepcontrol.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.ui.sleepfragment.SleepViewModel;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;

public class BarChartFragment extends Fragment {

    HorizontalBarChart barChart;
    BarData barData;
    DateAxisValueFormatter formatter;
    ArrayList<String> datesList;
    View view;
    Button oneDay, sevenDay, month;
    int numOfDays = 1;
    Date dateStart, dateEnd;
    SleepViewModel sleepViewModel;

    List<SleepData> dataList = new ArrayList<>();
    Map<String, Integer> mapResultSleeping = new HashMap<>();

    final float groupSpace = 0.34f;
    final float barSpace = 0.02f;
    final float barWidth = 0.2f;
    //      (0,02+0,2)*3 + 0,34 = 1,00

    final String DAY_KEY = "_day";
    final String NIGHT_KEY = "_night";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        }
        barChart = view.findViewById(R.id.idBarChart);

        oneDay = view.findViewById(R.id.one_day_bar_btn);
        sevenDay = view.findViewById(R.id.seven_day_bar_btn);
        month = view.findViewById(R.id.month_bar_btn);

        sleepViewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);
        formatter = new DateAxisValueFormatter();

        @SuppressLint({"ResourceAsColor", "NonConstantResourceId"})
        View.OnClickListener listener = (buttonView) -> {
            oneDay.setBackgroundColor(Color.LTGRAY);
            sevenDay.setBackgroundColor(Color.LTGRAY);
            month.setBackgroundColor(Color.LTGRAY);
            buttonView.setBackgroundColor(Color.DKGRAY);

            switch (buttonView.getId()) {
                case R.id.one_day_bar_btn:
                    numOfDays = 1;
                    setPeriod();
                    break;
                case R.id.seven_day_bar_btn:
                    numOfDays = 7;
                    setPeriod();
                    break;
                case R.id.month_bar_btn:
                    numOfDays = 30;
                    setPeriod();
                    break;
            }
        };

        oneDay.setOnClickListener(listener);
        sevenDay.setOnClickListener(listener);
        month.setOnClickListener(listener);

        initBarChart();
        oneDay.callOnClick();
        return view;
    }

    private void initBarChart() {
//  barChart style setting
        YAxis axisLeft = barChart.getAxisLeft();
        YAxis axisRight = barChart.getAxisRight();
        XAxis xAxis = barChart.getXAxis();

        axisRight.setValueFormatter(new TimeRightAxisValueFormatter());
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        axisRight.setLabelCount(6, true);
        axisLeft.setEnabled(false);
        axisLeft.setCenterAxisLabels(true);

        axisRight.removeAllLimitLines();
        axisLeft.removeAllLimitLines();


        axisLeft.setAxisMinimum(0);
        axisLeft.setAxisMaximum(1440);

        axisRight.setAxisMinimum(0);
        axisRight.setAxisMaximum(1440);

        barChart.getDescription().setText("Sleep statistic");

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        xAxis.setGridLineWidth(2f);
        xAxis.setGridColor(Color.GRAY);

        barChart.setVisibleXRangeMaximum(7);
    }

    private void setPeriod() {
        dateEnd = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(dateEnd);
        instance.add(Calendar.DAY_OF_MONTH, 1);
        dateEnd = instance.getTime();

        if (numOfDays == 1) {
            this.dateStart = new Date();
        } else {
            instance.add(Calendar.DAY_OF_MONTH, -(numOfDays));
            this.dateStart = instance.getTime();
        }

        Date startIterator = dateStart;
        datesList = new ArrayList<>();
        instance.setTime(startIterator);
        datesList.add(DAY_YEAR_ONLY_FORMAT.format(startIterator) + DAY_KEY);
        datesList.add(DAY_YEAR_ONLY_FORMAT.format(startIterator) + NIGHT_KEY);

// init full dates list of seted period
        if (numOfDays > 1) {
            while (startIterator.before(dateEnd)) {
                instance.add(Calendar.DAY_OF_MONTH, 1);
                startIterator = instance.getTime();
                datesList.add(DAY_YEAR_ONLY_FORMAT.format(startIterator) + DAY_KEY);
                datesList.add(DAY_YEAR_ONLY_FORMAT.format(startIterator) + NIGHT_KEY);
            }
        }

// init  dataList  by data from DB
        sleepViewModel.setDataByPeriod(dateStart, dateEnd);
        sleepViewModel.getDataByPeriod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<SleepData>>() {
                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull List<SleepData> sleepData) {
                        dataList.clear();
                        dataList.addAll(sleepData);
                        try {
                            initDataSets();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Toast.makeText(getContext(), "SOMETHING GOING WRONG?..", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void initDataSets() throws ParseException {
        mapResultSleeping.clear();
        barChart.fitScreen();

// get mapResultSleeping where key is Date + day/night , value is sum of sleeping minutes
        if (dataList.size() > 0) {
            for (SleepData sleepData : dataList) {

                String[] itemRes = sleepData.getResult().replaceAll("[^0-9?!.]", "").split("\\.");
                int minutes = (Integer.parseInt(itemRes[1]) + Integer.parseInt(itemRes[0]) * 60);
                String key = DAY_YEAR_ONLY_FORMAT.format(sleepData.getStartTime());

                int resMin;
                if (sleepData.getIsDay()) {
                    if (mapResultSleeping.containsKey(key + DAY_KEY)) {
                        resMin = mapResultSleeping.get(key + DAY_KEY) + minutes;
                        mapResultSleeping.put(key + DAY_KEY, resMin);
                    } else {
                        mapResultSleeping.put(key + DAY_KEY, minutes);
                    }
                } else {
                    if (mapResultSleeping.containsKey(key + NIGHT_KEY)) {
                        resMin = mapResultSleeping.get(key + NIGHT_KEY) + minutes;
                        mapResultSleeping.put(key + NIGHT_KEY, resMin);
                    } else {
                        mapResultSleeping.put(key + NIGHT_KEY, minutes);
                    }
                }
            }
        }

        List<BarEntry> dayGroup = new ArrayList<>();
        List<BarEntry> nightGroup = new ArrayList<>();
        List<BarEntry> activityGroup = new ArrayList<>();

// set data to day/night group sets
        for (Map.Entry<String, Integer> pair : mapResultSleeping.entrySet()) {
            String[] dateKey = pair.getKey().split("_");

            float xAxis = (float) TimeUnit.MILLISECONDS.toDays((long) Objects.requireNonNull(DAY_YEAR_ONLY_FORMAT.parse(dateKey[0])).getTime());

            if (dateKey[1].equals("day")) {
                dayGroup.add(new BarEntry(xAxis, pair.getValue()));
            } else {
                nightGroup.add(new BarEntry(xAxis, pair.getValue()));
            }
            datesList.remove(pair.getKey());
        }

// set remaining 0 values
        for (String dates : datesList) {
            float xAxis = (float) TimeUnit.MILLISECONDS.toDays((long) DAY_YEAR_ONLY_FORMAT.parse(dates.split("_")[0]).getTime());
            if (dates.split("_")[1].equals("day")) {
                dayGroup.add(new BarEntry(xAxis, 0f));
            } else {
                nightGroup.add(new BarEntry(xAxis, 0f));
            }
        }
// sort sets by Date
        Collections.sort(dayGroup, BarChartFragment.xAxisComparator);
        Collections.sort(nightGroup, BarChartFragment.xAxisComparator);

// set data to activityGroup set (24h - (day + night sleep))
        for (int i = 0; i < dayGroup.size(); i++) {
            float xAxis = dayGroup.get(i).getX();
            int sum = (int) (dayGroup.get(i).getY() + nightGroup.get(i).getY());
            activityGroup.add(new BarEntry(xAxis, 24 * 60 - sum));
        }

        BarDataSet daySet = new BarDataSet(dayGroup, "DAY");
        daySet.setColor(Color.YELLOW);
        daySet.setValueTextSize(14f);
        daySet.setValueTextColor(Color.YELLOW);

        BarDataSet nightSet = new BarDataSet(nightGroup, "NIGHT");
        nightSet.setColor(Color.DKGRAY);
        nightSet.setValueTextSize(14f);
        nightSet.setValueTextColor(Color.DKGRAY);

        BarDataSet activitySet = new BarDataSet(activityGroup, "ACTIVITY");
        activitySet.setColor(Color.WHITE);
        activitySet.setValueTextSize(14f);
        activitySet.setValueTextColor(Color.WHITE);

        showData(daySet, nightSet, activitySet);
    }

    private void showData(BarDataSet daySet, BarDataSet nightSet, BarDataSet activitySet) {
        barData = new BarData(daySet, nightSet, activitySet);
        barData.setBarWidth(barWidth);

        barChart.setData(barData);

        daySet.setValueFormatter(new TimeAxisValueFormatter());
        nightSet.setValueFormatter(new TimeAxisValueFormatter());
        activitySet.setValueFormatter(new TimeAxisValueFormatter());

        float xAxis = (float) TimeUnit.MILLISECONDS.toDays((long) dateStart.getTime());

        barChart.getXAxis().setAxisMinimum(xAxis);
        barChart.getXAxis().setAxisMaximum((float) TimeUnit.MILLISECONDS.toDays((long) dateEnd.getTime()));

        barChart.animateY(1500);
        barChart.setVisibleXRangeMaximum(7);
        barChart.groupBars(xAxis, groupSpace, barSpace);

        if (numOfDays > 20) {
            barChart.moveViewToAnimated((float) TimeUnit.MILLISECONDS.toDays((long) new Date().getTime()),
                    (float) TimeUnit.MILLISECONDS.toDays((long) new Date().getTime()), YAxis.AxisDependency.LEFT, 5000);
        } else {
            barChart.invalidate();
        }
    }

    public static Comparator<BarEntry> xAxisComparator = (o1, o2) -> {
        Float xAxis1 = o1.getX();
        Float xAxis2 = o2.getX();
        return xAxis1.compareTo(xAxis2);
    };
}

class DateAxisValueFormatter extends IndexAxisValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        long emissionsMilliSince1970Time = TimeUnit.DAYS.toMillis((long) value);
        Date timeMilliseconds = new Date(emissionsMilliSince1970Time);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        return dateTimeFormat.format(timeMilliseconds);
    }
}

class TimeAxisValueFormatter extends IndexAxisValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        String res;
        if (value == 0f) {
            res = "n/d";
        } else
            res = String.format(Locale.getDefault(), "%02dh. %02dm.", (int) value / 60, (int) value % 60);
        return res;
    }
}

class TimeRightAxisValueFormatter extends IndexAxisValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        String res;
        if (value == 0f) {
            res = "0h.";
        } else res = String.format(Locale.getDefault(), "%dh.", (int) value / 60);
        return res;
    }
}
