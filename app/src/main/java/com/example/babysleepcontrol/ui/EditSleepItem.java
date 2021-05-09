package com.example.babysleepcontrol.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.ui.sleepfragment.SleepFragment;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.babysleepcontrol.enums.Constants.DAY_TIME_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.TIME_ONLY_FORMAT;

public class EditSleepItem extends Fragment implements View.OnClickListener {

    private Button cancelBtn, saveBtn;
    private EditText startDate, startTime, endDate, endTime, notes;
    private TextView result;
    private long id;
    private Date itemStart, itemEnd;
    private Date start, end;
    private SleepFragment sleepFragment = null;
    private Boolean isEnd;
    private String noteStart,noteEnd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            id = this.getArguments().getLong("id");
            String endArg = this.getArguments().getString("end");
            if (endArg != null) {
                end = DAY_TIME_FORMAT.parse(this.getArguments().getString("end"));
                isEnd = true;
            } else {
                end = new Date();
                this.isEnd = false;
            }

            this.noteStart = this.getArguments().getString("notes");


            start = DAY_TIME_FORMAT.parse(this.getArguments().getString("start"));
            itemStart = start;
            itemEnd = end;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_sleep, container, false);

        cancelBtn = view.findViewById(R.id.cancel_btn_edit);
        saveBtn = view.findViewById(R.id.save_btn_edit);
        startDate = view.findViewById(R.id.start_date_edit);
        endDate = view.findViewById(R.id.end_date_edit);
        startTime = view.findViewById(R.id.start_time_edit);
        endTime = view.findViewById(R.id.end_time_edit);
        result = view.findViewById(R.id.res_txt_edit);
        notes = view.findViewById(R.id.notes_txt_edit);

        cancelBtn.setOnClickListener(v -> getParentFragmentManager().popBackStackImmediate());

        saveBtn.setOnClickListener(v -> {
            saveReturnSleepFragment();
        });

        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
        notes.setImeOptions(EditorInfo.IME_ACTION_DONE);

        if (!isEnd) {
            endDate.setVisibility(View.INVISIBLE);
            endTime.setVisibility(View.INVISIBLE);
        }
        initData();
        return view;
    }

    private void initData() {
        startDate.setText(DAY_YEAR_ONLY_FORMAT.format(start));
        startTime.setText(TIME_ONLY_FORMAT.format(start));
        endDate.setText(DAY_YEAR_ONLY_FORMAT.format(end));
        endTime.setText(TIME_ONLY_FORMAT.format(end));
        if (noteStart != null) {
            notes.setText(noteStart);
        }

        long milliseconds = end.getTime() - start.getTime();
        int resHours = (int) (milliseconds / (60 * 60 * 1000));
        int resMinutes = (int) (milliseconds - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);

        result.setText(String.format("%02dh. %02dm.", resHours, resMinutes));

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_time_edit:
                editTime(true);
                break;
            case R.id.start_date_edit:
                editDate(true);
                break;
            case R.id.end_time_edit:
                editTime(false);
                break;
            case R.id.end_date_edit:
                editDate(false);
                break;
        }
    }

    private void editDate(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(isStart ? start : end);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (isStart)
                this.start = new Date(calendar.getTimeInMillis());
            else
                this.end = new Date(calendar.getTimeInMillis());
            if (itemStart != start || itemEnd != end)
                initData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void editTime(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(isStart ? start : end);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            if (isStart)
                this.start = new Date(calendar.getTimeInMillis());
            else
                this.end = new Date(calendar.getTimeInMillis());
            if (itemStart != start || itemEnd != end)
                initData();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        timePickerDialog.show();
    }

    private void saveReturnSleepFragment() {
        this.noteEnd = notes.getText().toString();
        if (itemStart == start && itemEnd == end && noteStart.equals(noteEnd)) {
            Toast.makeText(getContext(), "No changes provided... ", Toast.LENGTH_SHORT).show();
            return;
        } else {
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof SleepFragment) {
                    sleepFragment = (SleepFragment) fragment;
                }
            }
            if (sleepFragment != null)
                showAlert();
        }
    }

    private void showAlert() {
        AlertDialog.Builder diag = new AlertDialog.Builder(requireContext());
        diag.setMessage("Do you want to SAVE changes?");
        diag.setPositiveButton("Yes", (dialog, which) -> {
            if (!isEnd)
                sleepFragment.updateAfterEdit(id, start, null, result.getText().toString(), noteEnd);
            else
                sleepFragment.updateAfterEdit(id, start, end, result.getText().toString(), noteEnd);

            getParentFragmentManager().popBackStackImmediate();
        });
        diag.setNegativeButton("No", (dialog, which) -> {
            return;
        });
        diag.create().show();
    }
    // TODO MAKE NOTES txt!!!!
}