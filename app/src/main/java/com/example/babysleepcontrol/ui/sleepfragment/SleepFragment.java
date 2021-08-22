package com.example.babysleepcontrol.ui.sleepfragment;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.adapter.SleepFragmentAdapter;
import com.example.babysleepcontrol.data.SleepData;
import com.example.babysleepcontrol.foregroundservises.ReminderBroadcast;
import com.example.babysleepcontrol.ui.BarChartFragment;
import com.example.babysleepcontrol.ui.CalendarFragment;
import com.example.babysleepcontrol.ui.EditSleepItem;
import com.example.babysleepcontrol.foregroundservises.ForegroundNotificationService;
import com.example.babysleepcontrol.ui.PieChartFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.babysleepcontrol.enums.Constants.DAY_TIME_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.TIME_ONLY_FORMAT;


public class SleepFragment extends Fragment implements View.OnClickListener {

    RecyclerView recyclerView;
    ToggleButton startStopBtn;
    Button deleteBtn;
    ImageButton statisticBtn, calendarBtn, previousBtn, nextBtn, closeInContainer, barChartBtn, pieChartBtn;
    LinearLayout linearLayoutTxtView, btnsInContainer, layoutOfNavigationToggleBtns;
    private TextView currentData, sumTxt;
    SleepFragmentAdapter sleepFragmentAdapter;
    LinearLayoutManager layoutManager;
    Date selectedDate;
    View view;
    SleepViewModel sleepViewModel;
    private Disposable disposable;
    public static final String CALENDAR_BTN = "calendar";
    public static final String STATISTIC_BTN = "statistic";
    public static final String CHANNEL_ID = "ChannelId";
    public static final String START_TIME = "StartTime";
    public static final String START_REMIND_TIME = "StartTime";

    public static final String PIE_TAG = "PieChart";
    public static final String BAR_CHART_TAG = "BarChart";

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_sleep, container, false);
        }

        recyclerView = view.findViewById(R.id.sleep_recycler_view);
        sleepFragmentAdapter = new SleepFragmentAdapter();
        recyclerView.setAdapter(sleepFragmentAdapter);

        layoutManager = (new LinearLayoutManager(this.getContext()) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                sumTxt.setText("Бодрствование \n" + calculateActivityPerDay());
            }
        });
        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        startStopBtn = view.findViewById(R.id.start_stop_btn);
        deleteBtn = view.findViewById(R.id.delete_btn);
        calendarBtn = view.findViewById(R.id.calendar_btn);
        statisticBtn = view.findViewById(R.id.statistic_btn);
        barChartBtn = view.findViewById(R.id.bar_chart_btn);
        pieChartBtn = view.findViewById(R.id.pie_chart_btn);

        barChartBtn.setOnClickListener(v -> startBarFragment());
        pieChartBtn.setOnClickListener(v -> startPieFragment());


        previousBtn = view.findViewById(R.id.previous_btn);
        nextBtn = view.findViewById(R.id.next_btn);
        linearLayoutTxtView = view.findViewById(R.id.linearLayout_txtv);
        layoutOfNavigationToggleBtns = view.findViewById(R.id.layout);
        btnsInContainer = view.findViewById(R.id.buttons_in_frame_container);

        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        calendarBtn.setOnClickListener(this);
        statisticBtn.setOnClickListener(this);
        // deleteBtn.setOnClickListener(this);
        startStopBtn.setOnClickListener(this);

        closeInContainer = view.findViewById(R.id.close_btn_in_container);

        closeInContainer.setOnClickListener(v -> {
            removeFragmentByTag(PIE_TAG);
            removeFragmentByTag(BAR_CHART_TAG);
            hideContainerButtons(true);
        });

        currentData = view.findViewById(R.id.current_data);
        sumTxt = view.findViewById(R.id.sum_activity_txt);

        sleepViewModel = new ViewModelProvider(requireActivity()).get(SleepViewModel.class);

        startStopBtn.setChecked(loadInstance());

        initData();

        return view;
    }


    private void initData() {

        setCurrentDate(new Date());

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

        Observer<List<SleepData>> observer = sleepData -> {
            sleepFragmentAdapter.submitList(sleepData);
        };
        sleepViewModel.getDayOnlySleepData().observe(getViewLifecycleOwner(), observer);

        sleepFragmentAdapter.setOnDeleteClickListener(this::showAlert);
    }

    public void setCurrentDate(Date selectedDay) {
        this.selectedDate = selectedDay;
        currentData.setText(DAY_YEAR_ONLY_FORMAT.format(selectedDay));
        sleepViewModel.setNewDate(selectedDay);
    }

    @SuppressLint("DefaultLocale")
    private String calculateActivityPerDay() {
        List<SleepData> data = sleepFragmentAdapter.getCurrentList();
        if (data.size() > 0) {
            int h = 0;
            int m = 0;
            for (SleepData item : data) {
                if (item.getEndTime() != null) {
                    String[] itemRes = item.getResult().replaceAll("[^0-9?!.]", "").split("\\.");
                    h += Integer.parseInt(itemRes[0]);
                    m += Integer.parseInt(itemRes[1]);
                }
            }
            int resMin = 24 * 60 - h * 60 - m;
            return String.format("%02dh. %02dm.", resMin / 60, resMin % 60);
        } else return ("NO DATA");
    }

    private void showAlert(SleepData sleepData) {
        AlertDialog.Builder diag = new AlertDialog.Builder(requireContext());
        diag.setMessage("Do you want to DELETE recording?");
        diag.setPositiveButton("Yes", (dialog, which) -> {
            sleepViewModel.delete(sleepData);
            if (sleepData.getEndTime() == null && startStopBtn.isChecked()) {
                startStopBtn.setChecked(false);
            }
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
            case R.id.statistic_btn:
                Log.d("TAG", "Start PIE");

                startPieFragment();
                break;
            case R.id.calendar_btn:
                startCalendarFragment();
                break;
            case R.id.previous_btn:
                Calendar instance = Calendar.getInstance();
                instance.setTime(selectedDate);
                instance.add(Calendar.DAY_OF_MONTH, -1);
                Date previousDay = instance.getTime();
                setCurrentDate(previousDay);
                break;
            case R.id.next_btn:
                Calendar instance2 = Calendar.getInstance();
                instance2.setTime(selectedDate);
                instance2.add(Calendar.DAY_OF_MONTH, 1);
                Date nextDay = instance2.getTime();
                setCurrentDate(nextDay);
                break;
        }
    }

    @SuppressLint("ResourceAsColor")
    private void startPieFragment() {
        pieChartBtn.setClickable(false);
        pieChartBtn.setBackgroundResource(R.color.purple_500);
        barChartBtn.setClickable(true);
        barChartBtn.setBackgroundResource(R.color.darkRed);

        Fragment pieFragment = new PieChartFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.sleep_frame_container, pieFragment, PIE_TAG).addToBackStack(null).commit();
        removeFragmentByTag(BAR_CHART_TAG);
        hideContainerButtons(false);
    }

    @SuppressLint("ResourceAsColor")
    private void startBarFragment() {
        pieChartBtn.setClickable(true);
        barChartBtn.setBackgroundResource(R.color.purple_500);
        barChartBtn.setClickable(false);
        pieChartBtn.setBackgroundResource(R.color.darkRed);

        Fragment barFragment = new BarChartFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.sleep_frame_container, barFragment, BAR_CHART_TAG).addToBackStack(null).commit();
        removeFragmentByTag(PIE_TAG);

        hideContainerButtons(false);
    }


    public void removeFragmentByTag(String tag) {
        Fragment fragment = getParentFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getParentFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void startCalendarFragment() {
        Fragment calendarFragment = new CalendarFragment();
        Bundle bundle = new Bundle();
        bundle.putString("CurrentDate", DAY_YEAR_ONLY_FORMAT.format(selectedDate));
        calendarFragment.setArguments(bundle);
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.sleep_frame_container, calendarFragment).addToBackStack(null).commit();
        hideButtons(true, CALENDAR_BTN);
    }

    public void hideButtons(boolean hide, String btnKey) {
        if (hide) {
            if (btnKey.equals(CALENDAR_BTN)) {
                Log.d("TAG", " CALENDAR_BTN  ");
                calendarBtn.setVisibility(View.INVISIBLE);
            } else if (btnKey.equals(STATISTIC_BTN)) {
                Log.d("TAG", " STATISTIC_BTN ");

                linearLayoutTxtView.setVisibility(View.INVISIBLE);
                statisticBtn.setVisibility(View.INVISIBLE);
            }
        } else {
            linearLayoutTxtView.setVisibility(View.VISIBLE);
            calendarBtn.setVisibility(View.VISIBLE);
            statisticBtn.setVisibility(View.VISIBLE);
        }
    }

    private void deleteData() {
        startStopBtn.setChecked(false);
        sleepViewModel.deleteAllSleepData();
        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
    }


    private void addNewData(boolean isStop) {
        if (!isStop) {
            Date start = new Date();
            SleepData sleepData = new SleepData(start, null, null, null);
            this.id = sleepViewModel.insert(sleepData);
            if (!DAY_YEAR_ONLY_FORMAT.format(selectedDate).equals(DAY_YEAR_ONLY_FORMAT.format(start))) {
                setCurrentDate(start);
            }
            startTimer();
            startStopForegroundServices(start, true);

        } else {
        //    if (id != 0) {
                // sleepViewModel.getNoteById(id);
                sleepViewModel.getMaxIdNote();
                sleepViewModel.getSleepDataSingle()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<SleepData>() {
                            @Override
                            public void onSuccess(@NotNull SleepData item) {
                                item.setEndTime(new Date());
                                sleepViewModel.update(item);
                                if (!DAY_YEAR_ONLY_FORMAT.format(selectedDate).equals(DAY_YEAR_ONLY_FORMAT.format(item.getStartTime()))) {
                                    setCurrentDate(item.getStartTime());
                                }
                                stopTimer();
                                startStopForegroundServices(new Date(), false);
                            }

                            @Override
                            public void onError(Throwable e) {
                            //    Toast.makeText(getContext(), "SOMETHING GOING WRONG?..", Toast.LENGTH_LONG).show();
                                Toast.makeText(getContext(), "!! ERROR - " + e.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        });
       //     }
        }

        saveInstance(startStopBtn.isChecked());
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.smoothScrollToPosition(recyclerView.getBottom());
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void startStopForegroundServices(Date start, boolean isStart) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && isStart) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sleep Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationChannel reminderChannel = new NotificationChannel(
                    "remindEveryHour",
                    "Hour Reminder Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(reminderChannel);
        }
        Intent serviceIntent = new Intent(getContext(), ForegroundNotificationService.class);
        serviceIntent.putExtra(START_TIME, TIME_ONLY_FORMAT.format(start));

        Intent remindIntent = new Intent(getContext(), ReminderBroadcast.class);
        remindIntent.putExtra(START_REMIND_TIME, start.getTime());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, remindIntent, 0);
        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);

        if (isStart) {
            ContextCompat.startForegroundService(getContext(), serviceIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, pendingIntent);
        } else {
            requireActivity().stopService(new Intent(getContext(), ForegroundNotificationService.class));
            requireActivity().stopService(new Intent(getContext(), ReminderBroadcast.class));
            alarmManager.cancel(pendingIntent);
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
                        }, e -> Log.d("TAG", "ERROR in START TIMER"));
    }

    private void stopTimer() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

   /* @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        saveInstance(startStopBtn.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }*/

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
        startStopBtn.setChecked(loadInstance());
        if (disposable == null && startStopBtn.isChecked()) {
            startTimer();
        }
        /// TODO NEWWWWWWWWWW!!!!!!!!1
        sleepFragmentAdapter.notifyDataSetChanged();
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
        return sp.getBoolean("StartStopBtn", false);
    }

    public void updateAfterEdit(long id, Date start, Date end, String result, String notes) {
        SleepData sleepData = new SleepData(start, end, result, notes);
        sleepData.setId(id);
        sleepViewModel.update(sleepData);
    }

    public void hideContainerButtons(boolean isHide) {
        if (isHide) {
            btnsInContainer.setVisibility(View.INVISIBLE);
            layoutOfNavigationToggleBtns.setVisibility(View.VISIBLE);
        } else {
            btnsInContainer.setVisibility(View.VISIBLE);
            layoutOfNavigationToggleBtns.setVisibility(View.INVISIBLE);
        }
    }
}