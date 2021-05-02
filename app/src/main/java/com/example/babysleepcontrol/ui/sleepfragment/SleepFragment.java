package com.example.babysleepcontrol.ui.sleepfragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.adapter.SleepFragmentAdapter;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.ui.CalendarFragment;
import com.example.babysleepcontrol.ui.EditSleepItem;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SleepFragment extends Fragment implements View.OnClickListener {

    RecyclerView recyclerView;
    ToggleButton startStopBtn;
    Button delete, calendar_btn;
    private TextView currentData;
    SleepFragmentAdapter sleepFragmentAdapter;
    String selectedData;
    View view;
    SleepViewModel sleepViewModel;

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TAG", " onCreateView____ ");
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_sleep, container, false);
        }

        recyclerView = view.findViewById(R.id.sleep_container);
        sleepFragmentAdapter = new SleepFragmentAdapter();
        recyclerView.setAdapter(sleepFragmentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        startStopBtn = view.findViewById(R.id.start_stop_btn);
        delete = view.findViewById(R.id.delete_btn);
        calendar_btn = view.findViewById(R.id.calendar_btn);
        calendar_btn.setOnClickListener(this);
        delete.setOnClickListener(this);
        startStopBtn.setOnClickListener(this);

        currentData = view.findViewById(R.id.current_data);

        sleepViewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);

        if (selectedData != null) {
            currentData.setText(selectedData);
        } else
            currentData.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
        startStopBtn.setChecked(loadInstance());

        initData();
        return view;
    }

    private void initData() {
        sleepViewModel.getAllSleepData().observe(getViewLifecycleOwner(), new Observer<List<SleepData>>() {
            @Override
            public void onChanged(List<SleepData> sleepData) {
                sleepFragmentAdapter.addSleepData(sleepData);
            }
        });

        sleepFragmentAdapter.setOnItemClickListener(new SleepFragmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SleepData sleepData) {
                Toast.makeText(getContext(), "EDIT_BTN ", Toast.LENGTH_SHORT).show();
                Fragment editSleepItem = new EditSleepItem();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_layout, editSleepItem).addToBackStack(null).commit();
            }
        });
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
        sleepViewModel.deleteAllSleepData();
        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
    }

    private void addNewData(boolean isStop) {
        if (!isStop) {
            SleepData sleepData = new SleepData(new Date(), null, null);
            sleepViewModel.insert(sleepData);
            this.id = sleepData.getId();
        } else {
            SleepData item = sleepFragmentAdapter.getLastItem();
            item.setEndTime(new Date());
            sleepViewModel.update(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        saveInstance(startStopBtn.isChecked());
    }

    @Override
    public void onDestroyView() {
        saveInstance(startStopBtn.isChecked());
        super.onDestroyView();
    }

    private void saveInstance(boolean isChecked) {
        SharedPreferences sp = requireContext().getSharedPreferences("1", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("StartStopBtn", isChecked);
        editor.putLong("Id", id);
        editor.apply();
    }

    private boolean loadInstance() {
        SharedPreferences sp = requireContext().getSharedPreferences("1", Context.MODE_PRIVATE);
        this.id = sp.getLong("Id", 0);
        return sp.getBoolean("StartStopBtn", true);
    }


    // TODO - UPDATE LIST ACCORDING NEW DATE!!!!
    public void setCurrentDate(String selectedDay) {
        this.selectedData = selectedDay;
        currentData.setText(selectedData);
    }
}