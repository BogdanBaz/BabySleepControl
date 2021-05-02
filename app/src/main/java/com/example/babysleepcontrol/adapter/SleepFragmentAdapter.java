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
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.SleepData;

import java.util.ArrayList;
import java.util.List;

import static com.example.babysleepcontrol.enums.Constants.DAY_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.DAY_YEAR_ONLY_FORMAT;
import static com.example.babysleepcontrol.enums.Constants.TIME_ONLY_FORMAT;

public class SleepFragmentAdapter extends RecyclerView.Adapter<SleepFragmentAdapter.SleepViewHolder> {

    private List<SleepData> sleepData = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public SleepViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sleep_recycler_fragment, viewGroup, false);
        return new SleepViewHolder(layout);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SleepViewHolder sleepHolder, int position) {
        SleepData item = sleepData.get(position);

        sleepHolder.date.setText(DAY_YEAR_ONLY_FORMAT.format(item.getStartTime()));
        sleepHolder.startTime.setText(TIME_ONLY_FORMAT.format(item.getStartTime()));

        if (item.getEndTime() == null) {
            sleepHolder.endTime.setText("--:--");
        } else if (item.getEndTime() != null && item.getEndTime().getDate() != item.getStartTime().getDate()) {
            sleepHolder.endTime.setText(TIME_ONLY_FORMAT.format(item.getEndTime()) + "("+
                    DAY_ONLY_FORMAT.format(item.getEndTime()) + ")");
        } else
            sleepHolder.endTime.setText(TIME_ONLY_FORMAT.format(item.getEndTime()));

        sleepHolder.result.setText(item.getResult());

        if (item.getIsDay()) {
            sleepHolder.isDay.setBackgroundResource(R.drawable.sun);
        } else sleepHolder.isDay.setBackgroundResource(R.drawable.moon);
    }

    @Override
    public int getItemCount() {
        return sleepData.size();
    }

    public void addSleepData(List<SleepData> data) {
        this.sleepData = data;
        notifyDataSetChanged();
    }

    public void clearData() {
        sleepData.clear();
        notifyDataSetChanged();
    }

    public SleepData getLastItem() {
        return sleepData.get(sleepData.size() - 1);
    }


    public class SleepViewHolder extends RecyclerView.ViewHolder {

        TextView date, startTime, endTime, result;
        ImageView isDay;
        RelativeLayout container;
        Button editBtn;

        public SleepViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_txt);
            startTime = itemView.findViewById(R.id.start_txt);
            endTime = itemView.findViewById(R.id.end_txt);
            isDay = itemView.findViewById(R.id.isDay_image);
            result = itemView.findViewById(R.id.result_txt);
            container = itemView.findViewById(R.id.container);
            editBtn = itemView.findViewById(R.id.edit_button);

            editBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(sleepData.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(SleepData sleepData);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
