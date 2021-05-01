package com.example.babysleepcontrol.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import static com.example.enums.SleepEnums.END_DATE_COLUMN;
import static com.example.enums.SleepEnums.END_TIME_COLUMN;
import static com.example.enums.SleepEnums.ID_COLUMN;
import static com.example.enums.SleepEnums.RESULT_COLUMN;
import static com.example.enums.SleepEnums.START_DATE_COLUMN;
import static com.example.enums.SleepEnums.START_TIME_COLUMN;
import static com.example.enums.SleepEnums.TABLE_NAME;

public class DbHelper extends SQLiteOpenHelper {


    public DbHelper(@Nullable Context context) {
        super(context, TABLE_NAME.getTitle(), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "create table " + TABLE_NAME.getTitle() + " ("
                + "id integer primary key autoincrement,"
                + START_TIME_COLUMN.getTitle() + " text,"
                + END_TIME_COLUMN.getTitle() + " text,"
                + START_DATE_COLUMN.getTitle() + " text,"
                + END_DATE_COLUMN.getTitle() + " text,"
                + RESULT_COLUMN.getTitle() + " text" + ");";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME.getTitle());
        onCreate(db);
    }

    public long addData(String startTime, String endTime, String startDate, String endDate) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(START_TIME_COLUMN.getTitle(), startTime);
        contentValues.put(END_TIME_COLUMN.getTitle(), endTime);
        contentValues.put(START_DATE_COLUMN.getTitle(), startDate);
        contentValues.put(END_DATE_COLUMN.getTitle(), endDate);

        long res = db.insert(TABLE_NAME.getTitle(), null, contentValues);
        db.close();
        Log.d("DB", "Put data in db, id= " + res);

        return res;
    }

    public boolean refreshData(long id, String endTime, String endDate, String result, boolean isStop) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (isStop) {
            String query = "UPDATE " + TABLE_NAME.getTitle() + " SET "
                    + END_TIME_COLUMN.getTitle() + " = '" + endTime + "' ,"
                    + END_DATE_COLUMN.getTitle() + " = '" + endDate + "' ,"
                    + RESULT_COLUMN.getTitle() + " = '" + result +
                    "' WHERE " + ID_COLUMN.getTitle() + " = '" + id + "'";
            db.execSQL(query);
            db.close();
            Log.d("DB", "UPDATE data in db, id= " + id);

            return true;
        }
        return false;
    }

    public Cursor readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME.getTitle();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public int ClearDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = " + "'" + TABLE_NAME.getTitle() + "'");
        int b = db.delete(TABLE_NAME.getTitle(), "1", null);
        db.close();
        return b;
    }
}
