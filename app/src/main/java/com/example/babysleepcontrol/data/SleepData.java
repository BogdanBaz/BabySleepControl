package com.example.babysleepcontrol.data;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.enums.ConstantsEnum.END_NIGHT;
import static com.example.enums.ConstantsEnum.START_NIGHT;

public class SleepData {
    String startTime, endTime, startDate, endDate, result;
    Boolean isDay;

    public SleepData(String startTime, String endTime, String startDate, String endDate, String result) {

        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isDay = calculateIsDay();

        if (endTime.equals("--:--")) {
            this.result = "waiting for end of sleeping..";
        } else if (result != null) {
            Log.d("SLEEP_DATA CLASS", "SET RESULT FROM CONSTRUCTOR  " + result);
            this.result = result;
        } else {
            this.result = calculateRes();
        }
    }

    private Boolean calculateIsDay() {
        int start = 12;
        try {
            start = Integer.parseInt(startTime.split(":")[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Boolean booleanIsDay = (start <= START_NIGHT.getValue() && start >= END_NIGHT.getValue());
        return booleanIsDay;
    }

    @SuppressLint("DefaultLocale")
    private String calculateRes() {

        String res = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        try {
            Date date1 = sdf.parse(startDate + " " + startTime);
            Date date2;
            if (endDate == null) {
                date2 = sdf.parse(startDate + " " + endTime);
            } else {
                date2 = sdf.parse(endDate + " " + endTime);
            }
            assert date2 != null;
            assert date1 != null;
            long milliseconds = date2.getTime() - date1.getTime();
            int resHours = (int) (milliseconds / (60 * 60 * 1000));
            int resMinutes = (int) (milliseconds - (long) resHours * (60 * 60 * 1000)) / (60 * 1000);

            res = String.format("%02d", resHours) + "h. " + String.format("%02d", resMinutes) + "m.";
            return res;
        } catch (ParseException e) {
            Log.d("SLEEP_DATA CLASS", "Exception -  " + e.toString());
            e.printStackTrace();
        }
        return res;
    }


    public String getStartTime() {
        return startTime;
    }


    public String getEndTime() {
        if (endDate != null) {
            if (!endDate.equals(startDate)) {
                System.out.println("ENDDATA = " + endDate);
                return (endTime + " (" + endDate.split("\\.")[0] + "." + endDate.split("\\.")[1] + ")");
            } else return endTime;
        } else
            return endTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult() {
        this.result = calculateRes();
    }

    public String getStartDate() {
        return startDate;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsDay() {
        return isDay;
    }
}
