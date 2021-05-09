package com.example.babysleepcontrol.adapter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.SleepData;

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

            return oldItem.getStartTime().equals(newItem.getStartTime()) && endEq &&
                    oldItem.getNotes().equals(newItem.getNotes());
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

        sleepHolder.date.setText(DAY_YEAR_ONLY_FORMAT.format(item.getStartTime()));
        sleepHolder.startTime.setText(TIME_ONLY_FORMAT.format(item.getStartTime()));

        if (item.getEndTime() != null) {
            if (item.getEndTime().getDate() != item.getStartTime().getDate()) {
                sleepHolder.endTime.setText(TIME_ONLY_FORMAT.format(item.getEndTime()) + "(" +
                        DAY_ONLY_FORMAT.format(item.getEndTime()) + ")");
            } else {
                sleepHolder.endTime.setText(TIME_ONLY_FORMAT.format(item.getEndTime()));

            }
        } else sleepHolder.endTime.setText("--:--");

        sleepHolder.result.setText(item.getResult());

        if (item.getIsDay()) {
            sleepHolder.isDay.setBackgroundResource(R.drawable.sun);
        } else sleepHolder.isDay.setBackgroundResource(R.drawable.moon);

        if (item.getNotes() != null) {
            sleepHolder.notes.setText(item.getNotes());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SleepViewHolder sleepHolder, int position, @NonNull List<Object> payloads) {
        final SleepData item = getItem(position);
        if (!payloads.isEmpty() && payloads.get(0) == PAYLOAD_RESULT) {
            sleepHolder.result.setText(item.getResult());
        } else {
            super.onBindViewHolder(sleepHolder, position, payloads);
        }
    }


    public SleepData getLastItem() {
        return getCurrentList().get(getItemCount() - 1);/*sleepData.get(sleepData.size() - 1);*/
    }


    public class SleepViewHolder extends RecyclerView.ViewHolder {

        TextView date, startTime, endTime, result, notes;
        ImageView isDay;
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

        // TODO _ kakto poigraca  s interfejsom tutaj to counter ????


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
