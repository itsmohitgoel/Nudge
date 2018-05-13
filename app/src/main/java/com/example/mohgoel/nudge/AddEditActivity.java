package com.example.mohgoel.nudge;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mohgoel.nudge.data.NudgeContract.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AddEditActivity extends AppCompatActivity {
    private EditText mReminderName;
    private EditText mReminderDescription;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath ="";
    private String mCurrentImageName ="";

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

        FloatingActionButton btnCamera = (FloatingActionButton) findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Log.e(AddEditActivity.class.getSimpleName(), "Error occured while creating File Dir");
                        e.printStackTrace();
                    }

                    //continue, only if file was sucessfully created
                    if (photoFile != null) {
                        Uri photoUri = FileProvider.getUriForFile(AddEditActivity.this,
                                getString(R.string.fileprovider_authority), photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //savefilepath in local db
            ContentValues cv = new ContentValues();
            cv.put(ImageEntry.COLUMN_IMAGE_URL, mCurrentPhotoPath);
            cv.put(ImageEntry.COLUMN_IMAGE_NAME, mCurrentImageName);
            cv.put(ImageEntry.COLUMN_REMINDER_ID , 1);
            Uri imageUri = getContentResolver().insert(ImageEntry.CONTENT_URI, cv);

            long imageRowID = ContentUris.parseId(imageUri);
            if (imageRowID > -1) {
                Toast.makeText(AddEditActivity.this,"Image Saved", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentImageName = imageFileName;
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
