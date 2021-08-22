package com.example.babysleepcontrol.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.SleepData;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.babysleepcontrol.enums.Constants.DAY_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.TIME_ONLY_FORMAT;

public class SleepFragmentAdapter extends ListAdapter<SleepData, SleepFragmentAdapter.SleepViewHolder> {

    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    public static final String PAYLOAD_RESULT = "PAYLOAD_RESULT";

    public SleepFragmentAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SleepData> DIFF_CALLBACK = new DiffUtil.ItemCallback<SleepData>() {
        @Override
        public boolean areItemsTheSame(@NonNull SleepData oldItem, @NonNull SleepData newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SleepData oldItem, @NonNull SleepData newItem) {
            boolean endEq = oldItem.getEndTime() == null && newItem.getEndTime() == null ||
                    oldItem.getEndTime() != null && oldItem.getEndTime().equals(newItem.getEndTime());
            boolean res = oldItem.getStartTime().equals(newItem.getStartTime()) && endEq &&
                    oldItem.getNotes().equals(newItem.getNotes());
            return res;
        }
    };

    @NonNull
    @Override
    public SleepViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sleep_recycler_fragment, viewGroup, false);
        return new SleepViewHolder(layout);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SleepViewHolder sleepHolder, int position) {
        final SleepData item = getItem(position);

        String dateStr = DAY_YEAR_ONLY_FORMAT.format(item.getStartTime());
        String timeStr = TIME_ONLY_FORMAT.format(item.getStartTime());
        sleepHolder.date.setText(dateStr);
        sleepHolder.startTime.setText(timeStr);

        if (item.getEndTime() != null) {
            if (item.getEndTime().getDate() != item.getStartTime().getDate()) {
                String endTimeStr = TIME_ONLY_FORMAT.format(item.getEndTime()) + "(" +
                        DAY_ONLY_FORMAT.format(item.getEndTime()) + ")";
                sleepHolder.endTime.setText(endTimeStr);
            } else {
                String endTimeStr =TIME_ONLY_FORMAT.format(item.getEndTime());
                sleepHolder.endTime.setText(endTimeStr);
            }
        } else sleepHolder.endTime.setText("--:--");

        String resStr = item.getResult();
        sleepHolder.result.setText(resStr);

        if (item.getIsDay()) {
            sleepHolder.isDay.setBackgroundResource(R.drawable.sun);
        } else sleepHolder.isDay.setBackgroundResource(R.drawable.moon);

        if (item.getNotes() != null) {
            sleepHolder.notes.setText(item.getNotes());
        }

        sleepHolder.cardViewActivity.setVisibility(View.VISIBLE);
        if (position + 1 < getItemCount()) {
            SleepData nextItem = getItem(position + 1);
            if (item.getEndTime() != null)
                sleepHolder.activityTxt.setText(calculateActiv(nextItem.getStartTime().getTime(), item.getEndTime().getTime()));
            else sleepHolder.activityTxt.setText("ERROR - item.getEndTime() == null ??");

        } else if (item.getEndTime() != null && DAY_YEAR_ONLY_FORMAT.format(item.getEndTime()).equals(DAY_YEAR_ONLY_FORMAT.format(new Date()))) {
            sleepHolder.activityTxt.setText(calculateActiv(new Date().getTime(), item.getEndTime().getTime()));
        } else
            sleepHolder.cardViewActivity.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBindViewHolder(@NonNull SleepViewHolder sleepHolder, int position, @NonNull List<Object> payloads) {
        final SleepData item = getItem(position);
        if (!payloads.isEmpty() && payloads.get(0) == PAYLOAD_RESULT) {
            String resStr = item.getResult();
            sleepHolder.result.setText(resStr);
        } else {
            super.onBindViewHolder(sleepHolder, position, payloads);
        }
    }

    public SleepData getLastItem() {
        return getCurrentList().get(getItemCount() - 1);
    }

    @SuppressLint("DefaultLocale")
    private String calculateActiv(long timeNext, long timePrev) {
        long milliseconds = timeNext - timePrev;
        int resHours = (int) (milliseconds / (60 * 60 * 1000));
        int resMinutes = (int) (milliseconds - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);
        return (String.format("%02dh. %02dm.", resHours, resMinutes));
    }

    public class SleepViewHolder extends RecyclerView.ViewHolder {

        TextView date, startTime, endTime, result, notes, activityTxt;
        ImageView isDay;
        CardView cardViewActivity;
        RelativeLayout container;
        Button editBtn, deleteBtn;

        public SleepViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_txt);
            startTime = itemView.findViewById(R.id.start_txt);
            endTime = itemView.findViewById(R.id.end_txt);
            isDay = itemView.findViewById(R.id.isDay_image);
            result = itemView.findViewById(R.id.result_txt);
            notes = itemView.findViewById(R.id.notes_txt);
            container = itemView.findViewById(R.id.container);
            editBtn = itemView.findViewById(R.id.edit_button);
            deleteBtn = itemView.findViewById(R.id.delete_button);
            activityTxt = itemView.findViewById(R.id.child_activity_txt);
            cardViewActivity = itemView.findViewById(R.id.card_view_activ);

            deleteBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onDeleteClickListener != null && position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(getItem(position));
                }
            });

            editBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onEditClickListener != null && position != RecyclerView.NO_POSITION) {
                    onEditClickListener.onEditClick(getItem(position));
                }
            });
        }
    }

    public interface OnEditClickListener {
        void onEditClick(SleepData sleepData);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(SleepData sleepData);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }
}
