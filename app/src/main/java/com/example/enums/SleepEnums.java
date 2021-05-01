package com.example.enums;

public enum SleepEnums {
    TABLE_NAME("sleep_table1"),
    ID_COLUMN("ID"),
    START_DATE_COLUMN("start_date"),
    END_DATE_COLUMN("end_date"),
    START_TIME_COLUMN("start"),
    END_TIME_COLUMN("stop"),
    RESULT_COLUMN("result");


    private final String title;

    SleepEnums(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
