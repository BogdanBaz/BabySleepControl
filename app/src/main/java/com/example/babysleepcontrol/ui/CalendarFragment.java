package com.example.babysleepcontrol.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.ui.sleepfragment.SleepFragment;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;
import static com.example.babysleepcontrol.ui.sleepfragment.SleepFragment.CALENDAR_BTN;

public class CalendarFragment extends Fragment implements View.OnClickListener {
    Button showBtn, cancelBtn, gotoBtn;
    CalendarView calendarView;
    String selectedDay;
    SleepFragment sleepFragment;
    Date selectedDate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.selectedDate =  DAY_YEAR_ONLY_FORMAT.parse(this.getArguments().getString("CurrentDate"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK )
            {
                startFragment();
                return true;
            }
            return false;
        });

        calendarView = view.findViewById(R.id.calendar_view);
        calendarView.setDate(selectedDate.getTime());

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
                selectedDay = String.format(year + "-" + String.format("%02d", month + 1) +  "-" + "%02d", dayOfMonth);
            }
        });

        List<Fragment> fragments = getParentFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof SleepFragment) {
                sleepFragment = (SleepFragment) fragment;
            }
        }


    }

    private void initPresentDay() {
        selectedDay = DAY_YEAR_ONLY_FORMAT.format(new Date());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_btn_calendar:
                Toast.makeText(getContext(), "Showing data of  " + selectedDay, Toast.LENGTH_SHORT).show();
                startFragment();
                break;
            case R.id.cancel_btn_calendar:
                sleepFragment.hideButtons(false,CALENDAR_BTN);
                getParentFragmentManager().popBackStackImmediate();
                break;
            case R.id.goto_btn_calendar:
                calendarView.setDate(Calendar.getInstance().getTimeInMillis(), false, true);
                initPresentDay();
                Toast.makeText(getContext(), "Goto present day", Toast.LENGTH_SHORT).show();
                startFragment();
                break;
        }
    }



    private void startFragment() {
        try {
            Date selectedDate = DAY_YEAR_ONLY_FORMAT.parse(selectedDay);
            System.out.println("SELECTED DATE " + selectedDate);
            sleepFragment.setCurrentDate(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
            sleepFragment.hideButtons(false,CALENDAR_BTN);
        getParentFragmentManager().popBackStackImmediate();
    }
}