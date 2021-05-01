package com.example.babysleepcontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.data.SleepData;

import java.util.ArrayList;
import java.util.List;

public class SleepFragmentAdapter extends RecyclerView.Adapter<SleepFragmentAdapter.SleepViewHolder> {

    Context context;
    List<SleepData> sleepData;

    public SleepFragmentAdapter(Context context, List<SleepData> sleepData) {
        this.context = context;
        this.sleepData = new ArrayList<>();
    }

    @NonNull
    @Override
    public SleepViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View layout;
        layout = LayoutInflater.from(context).inflate(R.layout.sleep_recycler_fragment, viewGroup, false);
        return new SleepViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull SleepViewHolder sleepHolder, int position) {
        SleepData item = sleepData.get(position);
        sleepHolder.date.setText(item.getStartDate());
        sleepHolder.startTime.setText(item.getStartTime());
        sleepHolder.endTime.setText(item.getEndTime());
        sleepHolder.result.setText(item.getResult());

        if (item.getIsDay()) {
            sleepHolder.isDay.setBackgroundResource(R.drawable.sun);
        } else sleepHolder.isDay.setBackgroundResource(R.drawable.moon);
    }

    @Override
    public int getItemCount() {
        return sleepData.size();
    }

    public void addSleepFragments(SleepData data) {
        int size = sleepData.size();
        sleepData.add(data);
        if (size == 0) {
            notifyDataSetChanged();
        } else
            notifyItemRangeChanged(size, sleepData.size());
    }

    public String refreshSleepFragment(String endTime , String endDate) {
        SleepData item = sleepData.get(sleepData.size() - 1);
        item.setEndTime(endTime);
        item.setEndDate(endDate);
        item.setResult();
        notifyDataSetChanged();
        return item.getResult();
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


        public SleepViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_txt);
            startTime = itemView.findViewById(R.id.start_txt);
            endTime = itemView.findViewById(R.id.end_txt);
            isDay = itemView.findViewById(R.id.isDay_image);
            result = itemView.findViewById(R.id.result_txt);
            container = itemView.findViewById(R.id.container);
        }
    }
}
