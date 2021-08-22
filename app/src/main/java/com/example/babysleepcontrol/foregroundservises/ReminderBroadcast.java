package com.example.babysleepcontrol.foregroundservises;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.renderscript.RenderScript;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.babysleepcontrol.R;

import org.w3c.dom.ls.LSOutput;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.babysleepcontrol.enums.Constants.TIME_ONLY_FORMAT;
import static com.example.babysleepcontrol.ui.sleepfragment.SleepFragment.START_REMIND_TIME;
import static com.example.babysleepcontrol.ui.sleepfragment.SleepFragment.START_TIME;

public class ReminderBroadcast extends BroadcastReceiver {
    long start;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.start = intent.getLongExtra(START_REMIND_TIME, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "remindEveryHour")
                .setSmallIcon(R.drawable.ic_baseline_snooze_24)
                .setContentTitle("Sleep")
                .setContentText(getPassedTime() + " passed")
                .setColor(Color.MAGENTA)
                .setColorized(true)
                .setPriority(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat compat = NotificationManagerCompat.from(context);
        compat.notify(2, builder.build());
    }

    @SuppressLint("DefaultLocale")
    private String getPassedTime() {
        Date date = new Date();
        date.setTime(start);
        long res = new Date().getTime() - start;
        int resHours = (int) (res / (60 * 60 * 1000));
        int resMinutes = (int) (res - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);

        return String.format("%02dh. %02dm.", resHours, resMinutes);
    }
}
