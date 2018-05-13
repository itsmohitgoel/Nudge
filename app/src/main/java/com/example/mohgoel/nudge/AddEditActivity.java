package com.example.mohgoel.nudge;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import com.example.mohgoel.nudge.data.NudgeContract.*;

public class AddEditActivity extends AppCompatActivity {
    private EditText mReminderName;
    private EditText mReminderDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mReminderName = (EditText) findViewById(R.id.add_reminder_name);
        mReminderDescription = (EditText) findViewById(R.id.add_reminder_desc);

        FloatingActionButton btnSaveReminder = (FloatingActionButton) findViewById(R.id.btn_save_reminder);
        btnSaveReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cValues = new ContentValues();
                cValues.put(ReminderEntry.COLUMN_NAME, mReminderName.getText().toString().trim());
                cValues.put(ReminderEntry.COLUMN_DESCRIPTION, mReminderDescription.getText().toString().trim());
                cValues.put(ReminderEntry.COLUMN_CREATED_ON, System.currentTimeMillis());

                Uri reminderUri = getContentResolver().insert(ReminderEntry.CONTENT_URI, cValues);
                long reminderRowID = ContentUris.parseId(reminderUri);

                if (reminderRowID > -1) {
                    Snackbar.make(view, "Reminder Created", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

}
