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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.adapter.SleepFragmentAdapter;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.ui.CalendarFragment;
import com.example.babysleepcontrol.ui.EditSleepItem;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.example.babysleepcontrol.enums.Constants.DAY_TIME_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;


public class SleepFragment extends Fragment implements View.OnClickListener {

    RecyclerView recyclerView;
    ToggleButton startStopBtn;
    Button deleteBtn;
    ImageButton calendarBtn;
    private TextView currentData;
    SleepFragmentAdapter sleepFragmentAdapter;
    String selectedData;
    View view;
    SleepViewModel sleepViewModel;
    private Disposable disposable;

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TAG", " onCreateView____ " );
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_sleep, container, false);
        }

        recyclerView = view.findViewById(R.id.sleep_recycler_view);
        sleepFragmentAdapter = new SleepFragmentAdapter();
        recyclerView.setAdapter(sleepFragmentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        startStopBtn = view.findViewById(R.id.start_stop_btn);
        deleteBtn = view.findViewById(R.id.delete_btn);
        calendarBtn = view.findViewById(R.id.calendar_btn);

        calendarBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        startStopBtn.setOnClickListener(this);

        currentData = view.findViewById(R.id.current_data);

        sleepViewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);

        if (selectedData != null) {
            currentData.setText(selectedData);
        } else
            currentData.setText(DAY_YEAR_ONLY_FORMAT.format(new Date()));
        startStopBtn.setChecked(loadInstance());

        initData();
        return view;
    }

    private void initData() {

        sleepViewModel.getAllSleepData().observe(getViewLifecycleOwner(), sleepData ->
                sleepFragmentAdapter.submitList(sleepData));

        sleepFragmentAdapter.setOnEditClickListener(sleepData -> {
            Fragment editSleepItem = new EditSleepItem();
            Bundle bundle = new Bundle();
            bundle.putLong("id", sleepData.getId());
            bundle.putString("start", DAY_TIME_FORMAT.format(sleepData.getStartTime()));
            if (sleepData.getEndTime() != null) {
                bundle.putString("end", DAY_TIME_FORMAT.format(sleepData.getEndTime()));
            } else bundle.putString("end", null);

            bundle.putString("notes", sleepData.getNotes());
            editSleepItem.setArguments(bundle);
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.sleep_frame_container, editSleepItem).addToBackStack(null).commit();
        });

        sleepFragmentAdapter.setOnDeleteClickListener(this::showAlert);
    }

    private void showAlert(SleepData sleepData) {
        AlertDialog.Builder diag = new AlertDialog.Builder(requireContext());
        diag.setMessage("Do you want to DELETE recording?");
        diag.setPositiveButton("Yes", (dialog, which) -> {
            sleepViewModel.delete(sleepData);
            Toast.makeText(getContext(), "Deleting", Toast.LENGTH_SHORT).show();
        });
        diag.setNegativeButton("No", (dialog, which) -> {
            return;
        });
        diag.create().show();
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
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.sleep_frame_container, calendarFragment).addToBackStack(null).commit();
        hideButtons(true);
    }

    public void hideButtons(boolean hide) {
        if (hide) {
            deleteBtn.setVisibility(View.INVISIBLE);
            calendarBtn.setVisibility(View.INVISIBLE);
        } else {
            deleteBtn.setVisibility(View.VISIBLE);
            calendarBtn.setVisibility(View.VISIBLE);
        }
    }

    private void deleteData() {
        startStopBtn.setChecked(false);
        sleepViewModel.deleteAllSleepData();
        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
    }

    private void addNewData(boolean isStop) {
        if (!isStop) {
            SleepData sleepData = new SleepData(new Date(), null, null, null);
            sleepViewModel.insert(sleepData);
            this.id = sleepData.getId();
            startTimer();

        } else {
            SleepData item = sleepFragmentAdapter.getLastItem();
            item.setEndTime(new Date());
            sleepViewModel.update(item);
            stopTimer();
        }
    }

    @SuppressLint("DefaultLocale")
    private void startTimer() {
        Log.d("TAG", " START TIMER  ");
        disposable =
                Observable.interval(1, TimeUnit.MINUTES)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(v -> {
                            sleepFragmentAdapter.getLastItem().setResult();
                            sleepFragmentAdapter.notifyItemChanged(sleepFragmentAdapter.getItemCount() - 1, SleepFragmentAdapter.PAYLOAD_RESULT);
                        }, e -> Log.d("TAG", "ERROR"));
    }

    private void stopTimer() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        saveInstance(startStopBtn.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TAG", "ON PAUSE");
        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "ON RESUME , button - " + startStopBtn.isChecked());
        if (disposable == null && startStopBtn.isChecked()) {
            startTimer();
        }
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
        Log.d("TAG", " selected data set -  " + selectedData);
    }

    public void updateAfterEdit(long id, Date start, Date end, String result , String notes) {
        SleepData sleepData = new SleepData(start, end, result, notes);
        sleepData.setId(id);
        sleepViewModel.update(sleepData);
    }
}