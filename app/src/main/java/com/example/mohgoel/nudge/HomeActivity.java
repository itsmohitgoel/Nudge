package com.example.mohgoel.nudge;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mohgoel.nudge.adapters.RemindersAdapter;
import com.example.mohgoel.nudge.beans.ReminderItem;
import com.example.mohgoel.nudge.data.NudgeContract.*;
import com.example.mohgoel.nudge.data.NudgeDbHelper;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements AddEditActivity.CustomChangeListener{
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ReminderItem> mRemindersDataList;

    // For the Reminders view, I've to show only a small subset of stored data,
    // specify the columns required and utilize the projecion.
    private static final String[] REMINDER_COLUMNS = {
            ReminderEntry.TABLE_NAME + "." + ReminderEntry._ID,
            ReminderEntry.COLUMN_NAME,
            ReminderEntry.COLUMN_DESCRIPTION,
            ReminderEntry.COLUMN_CREATED_ON,
            ReminderEntry.COLUMN_REMIND_ON
    };

    public static final int COL_REM_ID = 0;
    public static final int COL_REM_NAME = 1;
    public static final int COL_REM_DESCRIPTION = 2;
    public static final int COL_REM_CREATED_ON = 3;
    public static final int COL_REM_REMIND_ON = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton btnAddReminder = (FloatingActionButton) findViewById(R.id.btn_add_reminder);
        btnAddReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });

        mRecycleView = (RecyclerView) findViewById(R.id.reminders_recycler_view);
        mRecycleView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        getRemindersData(); // Fetch Reminders data from local db
        if (mRemindersDataList != null && mRemindersDataList.size() > 0) {
            mAdapter = new RemindersAdapter(this, mRemindersDataList);
            mRecycleView.setAdapter(mAdapter);
        } else {
            Toast.makeText(this, "No data available yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getRemindersData() {
        SQLiteOpenHelper openHelper = new NudgeDbHelper(this);
        SQLiteDatabase db = openHelper.getReadableDatabase();

        mRemindersDataList = new ArrayList<>();

        String sortOrder = ReminderEntry.COLUMN_REMIND_ON + " ASC, " + ReminderEntry.COLUMN_CREATED_ON + " ASC";

        Cursor c = db.query(ReminderEntry.TABLE_NAME, REMINDER_COLUMNS, null, null, null, null, sortOrder);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                ReminderItem item = new ReminderItem();
                item.setId(c.getString(COL_REM_ID));
                item.setName(c.getString(COL_REM_NAME));
                item.setDescription(c.getString(COL_REM_DESCRIPTION));
                item.setCreatedOn(c.getString(COL_REM_CREATED_ON));
                item.setRemindOn(c.getString(COL_REM_REMIND_ON));

                mRemindersDataList.add(item);
            }
        }
    }

    @Override
    public void onReminderItemModified() {
        getRemindersData();
        mAdapter.notifyDataSetChanged();
    }
}
