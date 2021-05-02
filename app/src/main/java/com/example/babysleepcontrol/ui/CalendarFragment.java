package com.example.babysleepcontrol.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.ui.sleepfragment.SleepFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements View.OnClickListener {
    Button showBtn, cancelBtn,gotoBtn;
    CalendarView calendarView;
     String selectedDay;
     ViewPager viewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = view.findViewById(R.id.calendar_view);

        showBtn = view.findViewById(R.id.show_btn_calendar);
        cancelBtn = view.findViewById(R.id.cancel_btn_calendar);
        gotoBtn = view.findViewById(R.id.goto_btn_calendar);
        showBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        gotoBtn.setOnClickListener(this);

        init();
        return view;
    }

    private void init() {
        initPresentDay();
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDay = String.format("%02d",dayOfMonth) + "." +  String.format("%02d",month+1)+ "." + year ;
                System.out.println(selectedDay);
            }
        });
    }

    private void initPresentDay() {
        selectedDay = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_btn_calendar:
                Toast.makeText(getContext(),"Showing data of  " + selectedDay,Toast.LENGTH_SHORT).show();
                startFragment();
                break;
            case R.id.cancel_btn_calendar:
                getFragmentManager().popBackStackImmediate();
                break;
            case R.id.goto_btn_calendar:
                calendarView.setDate(Calendar.getInstance().getTimeInMillis(), false, true);
                initPresentDay();
                Toast.makeText(getContext(),"Goto present day",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startFragment() {
        List<Fragment> fragments = getFragmentManager().getFragments();
        SleepFragment sleepFragment = (SleepFragment) fragments.get(0);
        sleepFragment.setCurrentDate(selectedDay);
        getFragmentManager().popBackStackImmediate();
    }
}