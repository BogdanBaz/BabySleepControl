package com.example.babysleepcontrol.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.adapter.SleepFragmentAdapter;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.database.DbHelper;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class SleepFragment extends Fragment implements View.OnClickListener {

    List<SleepData> sleepData;
    RecyclerView recyclerView;
    ToggleButton startStopBtn;
    Button delete, calendar_btn;
    private TextView currentData;
    SleepFragmentAdapter sleepFragmentAdapter;
    String selectedData;
    View view;


    DbHelper dbHelper;
    private long id;


    public SleepFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DbHelper(getContext());
        sleepData = new ArrayList<>();

    }

    private void initData() {
        Log.d("TAG", "INITdatA____ ");
        Cursor cursor = dbHelper.readAllData();
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String startTime = cursor.getString(1);
                String endTime = cursor.getString(2);
                String startDate = cursor.getString(3);
                String endDate = cursor.getString(4);
                String res = cursor.getString(5);

                sleepFragmentAdapter.addSleepFragments(new SleepData(startTime, endTime, startDate, endDate, res));
            }
        }
        cursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TAG", " onCreateView____ ");
        if (view == null){
        view = inflater.inflate(R.layout.fragment_sleep, container, false);
        }



        recyclerView = view.findViewById(R.id.sleep_container);
        sleepFragmentAdapter = new SleepFragmentAdapter(this.getContext(), sleepData);
        recyclerView.setAdapter(sleepFragmentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        startStopBtn = view.findViewById(R.id.start_stop_btn);
        delete = view.findViewById(R.id.delete_btn);
        calendar_btn = view.findViewById(R.id.calendar_btn);
        calendar_btn.setOnClickListener(this);
        delete.setOnClickListener(this);
        startStopBtn.setOnClickListener(this);

        currentData = view.findViewById(R.id.current_data);


        if (selectedData != null) {
            currentData.setText(selectedData);
        } else
            currentData.setText("* " + new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()) + " *");


        startStopBtn.setChecked(loadInstance());

        initData();
        return view;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start_stop_btn:
                if (startStopBtn.isChecked()) {
                    Toast.makeText(getContext(), "START_SLEEPING", Toast.LENGTH_SHORT).show();
                    addNewData(false);
                } else {
                    Toast.makeText(getContext(), "STOP_SLEEPING", Toast.LENGTH_SHORT).show();
                    addNewData(true);
                }
                break;
            case R.id.delete_btn:
                deleteData();
                break;
            case R.id.calendar_btn:
                startCalendarFragment();
                break;
        }
    }

    private void startCalendarFragment() {
        Fragment calendarFragment = new CalendarFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_layout, calendarFragment).addToBackStack(null).commit();
    }

    private void deleteData() {
        startStopBtn.setChecked(false);
        sleepFragmentAdapter.clearData();
        int nums = dbHelper.ClearDb();
        Log.d("SLEEP_FRAGMENT", "Delete DB , num of columns = " + nums);
        Toast.makeText(getContext(), "Delete in DB , num of columns= " + nums + "!", Toast.LENGTH_LONG).show();
    }
// TODO - close all DB Threads???

    private void addNewData(boolean isStop) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        String date = currentDateAndTime.split(" ")[0];
        String time = currentDateAndTime.split(" ")[1];

        if (!isStop) {
            this.id = dbHelper.addData(time, "--:--", date, null);

            Log.d("SLEEP_FRAGMENT", "Add new data id= " + id + " , " + date + " __ " + time + "--:--");

            //TODO DRY? - add new sqlData Automaticly? MAYBE IN SleepData class constructor??
            sleepFragmentAdapter.addSleepFragments(new SleepData(time, "--:--", date, null, null));

        } else {

            Log.d("SLEEP_FRAGMENT", "Refresh data by id= " + id + " , stop time is _ " + time);
            String res = sleepFragmentAdapter.refreshSleepFragment(time, date);
            dbHelper.refreshData(id, time, date, res, true);
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
     /*   Log.d("SLEEP_FRAGMENT", "onSaveInstanceState");
        savedInstanceState.putBoolean("startStopBtn", startStopBtn.isChecked());*/
        saveInstance(startStopBtn.isChecked());
    }

    @Override
    public void onDestroyView() {
        dbHelper.close();
        saveInstance(startStopBtn.isChecked());
        super.onDestroyView();
    }

    private void saveInstance(boolean isChecked) {
        SharedPreferences sp = Objects.requireNonNull(getContext()).getSharedPreferences("1", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("StartStopBtn", isChecked);
        editor.putLong("Id", id);
        editor.apply();
        Log.d("SLEEP_FRAGMENT", "SAVE   IS_CHECKED = " + isChecked);
    }

    private boolean loadInstance() {
        SharedPreferences sp = Objects.requireNonNull(getContext()).getSharedPreferences("1", Context.MODE_PRIVATE);
        this.id = sp.getLong("Id", 0);
        Log.d("SLEEP_FRAGMENT", "LOAD   IS_CHECKED = ");
        return sp.getBoolean("StartStopBtn", true);
    }


// TODO - UPDATE LIST ACCORDING NEW DATE!!!!
    public void setCurrentDate(String selectedDay) {
        this.selectedData = selectedDay;
        currentData.setText(selectedData);
    }
}