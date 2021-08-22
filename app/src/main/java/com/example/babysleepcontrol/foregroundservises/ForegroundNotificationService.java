package com.example.babysleepcontrol.foregroundservises;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.babysleepcontrol.R;
import com.example.babysleepcontrol.ui.MainActivity;

import static com.example.babysleepcontrol.ui.sleepfragment.SleepFragment.CHANNEL_ID;
import static com.example.babysleepcontrol.ui.sleepfragment.SleepFragment.START_TIME;

public class ForegroundNotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       String start = intent.getStringExtra(START_TIME);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        @SuppressLint("ResourceAsColor") Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sleep is running...")
                .setContentText("          <<___  Started at " + start + "  ___>>")
                .setSmallIcon(R.drawable.baby_icon)
               //.addAction(R.drawable.baby_icon , "STOP??", pendingIntent)
                .setUsesChronometer(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setColor(R.color.teal_200)
                .setColorized(true)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }
}
