package com.example.mohgoel.nudge.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mohgoel.nudge.data.NudgeContract.*;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class NudgeDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "nudge.db";

    public NudgeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_REMINDER_TABLE = "CREATE TABLE " + ReminderEntry.TABLE_NAME + " (" +
                ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                ReminderEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ReminderEntry.COLUMN_DESCRIPTION + " TEXT , " +
                ReminderEntry.COLUMN_CREATED_ON + " INTEGER DEFAULT 0, " +
                ReminderEntry.COLUMN_REMIND_ON + " INTEGER DEFAULT 0, " +
                " UNIQUE (" + ReminderEntry.COLUMN_NAME + ", " +
                ReminderEntry.COLUMN_DESCRIPTION + ") ON CONFLICT IGNORE" +
                " ); ";

        db.execSQL(SQL_CREATE_REMINDER_TABLE);

        final String SQL_CREATE_IMAGE_TABLE = "CREATE TABLE " + ImageEntry.TABLE_NAME + " (" +
                ImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                ImageEntry.COLUMN_REMINDER_ID + " INTEGER NOT NULL, " +
                ImageEntry.COLUMN_IMAGE_NAME + " TEXT NOT NULL, " +
                ImageEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                // Set up the reminder_id column as foreign key to reminder table
                " FOREIGN KEY (" + ImageEntry.COLUMN_REMINDER_ID + ") REFERENCES " +
                ReminderEntry.TABLE_NAME + " (" + ReminderEntry._ID + "), " +
                " UNIQUE (" + ImageEntry.COLUMN_IMAGE_NAME + ", " +
                ImageEntry.COLUMN_IMAGE_URL + ") ON CONFLICT IGNORE" +
                " ); ";

        db.execSQL(SQL_CREATE_IMAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update policy is drop already existing tables
        db.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImageEntry.TABLE_NAME);

        //Now recreate fresh empty database
        onCreate(db);
    }
}

