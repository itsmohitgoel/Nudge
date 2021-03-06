package com.example.mohgoel.nudge;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mohgoel.nudge.beans.ReminderItem;
import com.example.mohgoel.nudge.data.NudgeContract.*;
import com.example.mohgoel.nudge.utils.Utility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddEditActivity extends AppCompatActivity {
    public static final String LOG_TAG = AddEditActivity.class.getSimpleName();

    public final int ALARM_REQUEST_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText mViewReminderName;
    private EditText mViewReminderDescription;
    private TextView mCurrentReminder;
    private FloatingActionButton mBtnSaveReminder;
    private LinearLayout mImagesContainer;
    private Button mBtnSetAlarm;

    static final int IMAGES_LOADER = 0;
    private String mCurrentPhotoPath = "";
    private String mCurrentImageName = "";

    // date-time pickers along with reminder's epoch time value(if any)
    private TimePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Calendar mAlarmDateTime;

    public static final String REMINDER_PARCELABLE = "reminder";
    private ReminderItem mReminderItem;
    private boolean isEditMode;
    private ArrayList<ContentValues> mImageUrlsList = new ArrayList<>();

    //specify the columns for Images and define respecitive Projections
    private static final String[] IMAGE_COLUMNS = {
            ImageEntry.TABLE_NAME + "." + ImageEntry._ID,
            ImageEntry.COLUMN_REMINDER_ID,
            ImageEntry.COLUMN_IMAGE_NAME,
            ImageEntry.COLUMN_IMAGE_URL,
    };

    // Below indices for columns must be in exact order define above.
    private static final int COL_IMAGE_ID = 0;
    private static final int COL_IMAGE_REMINDER_ID = 1;
    private static final int COL_IMAGE_NAME = 2;
    private static final int COL_IMAGE_URL = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null && getIntent().hasExtra(REMINDER_PARCELABLE)) {
            mReminderItem = getIntent().getParcelableExtra(REMINDER_PARCELABLE);
        }
        mViewReminderName = (EditText) findViewById(R.id.add_reminder_name);
        mViewReminderDescription = (EditText) findViewById(R.id.add_reminder_desc);
        mImagesContainer = (LinearLayout) findViewById(R.id.images_container);
        mCurrentReminder = (TextView) findViewById(R.id.tv_current_reminder);

        mBtnSaveReminder = (FloatingActionButton) findViewById(R.id.btn_save_reminder);
        mBtnSaveReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // empty title validation
                if (mViewReminderName.getText().toString().trim().equals("")) {
                    Toast.makeText(AddEditActivity.this, R.string.error_msg_no_title, Toast.LENGTH_LONG).show();
                    return;
                }

                // Read name, description of task from fields
                ContentValues cValues = new ContentValues();
                cValues.put(ReminderEntry.COLUMN_NAME, mViewReminderName.getText().toString().trim());
                cValues.put(ReminderEntry.COLUMN_DESCRIPTION, mViewReminderDescription.getText().toString().trim());

                if (!isEditMode) {
                    // If New Task , then insert blindly new record with no reminder value set
                    cValues.put(ReminderEntry.COLUMN_CREATED_ON, (System.currentTimeMillis() / 1000));
                    Uri reminderUri = getContentResolver().insert(ReminderEntry.CONTENT_URI, cValues);
                    long reminderRowID = ContentUris.parseId(reminderUri);
                    int insertCount = insertImages(reminderRowID);


                    if (insertCount > 0) {
                        Toast.makeText(AddEditActivity.this, "Reminder Task Created with " + insertCount + " images.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //update the existing record
                    if (mAlarmDateTime != null) { //update alarm or reminder time if set, in DB
                        cValues.put(ReminderEntry.COLUMN_REMIND_ON, (mAlarmDateTime.getTimeInMillis() / 1000));
                        setAlarm(mAlarmDateTime);
                    }
                    int updateCount = 0;
                    updateCount = getContentResolver().update(ReminderEntry.CONTENT_URI,
                            cValues,
                            ReminderEntry._ID + " = ?",
                            new String[]{mReminderItem.getId()});

                    int insertCount = insertImages(Long.parseLong(mReminderItem.getId()));
                }

                finish();
            }

            // Helper method to insert image urls(if any), in image table
            private int insertImages(long reminderRowID) {
                //add all images details to database via bulkInsert call
                int insertCount = 0;
                if (mImageUrlsList.size() > 0) {
                    // append rowid of reminder as foreign key value
                    for (ContentValues values : mImageUrlsList) {
                        values.put(ImageEntry.COLUMN_REMINDER_ID, reminderRowID);
                    }

                    ContentValues[] cvArray = new ContentValues[mImageUrlsList.size()];
                    mImageUrlsList.toArray(cvArray);
                    insertCount = getContentResolver().bulkInsert(ImageEntry.CONTENT_URI, cvArray);
                }
                return insertCount;
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

        mBtnSetAlarm = (Button) findViewById(R.id.set_alarm_btn);
        mBtnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerDialog();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mReminderItem != null) { // set editMode value before resuming activity
            isEditMode = true;
        }
        // Display relevant data ,if reminder is being edited
        if (isEditMode) {
            mViewReminderName.setText(mReminderItem.getName());
            mViewReminderDescription.setText(mReminderItem.getDescription());
            if (mAlarmDateTime != null) {
                mAlarmDateTime.setTimeInMillis(Long.parseLong(mReminderItem.getRemindOn()) * 1000);
            }
            mBtnSaveReminder.setImageResource(R.drawable.ic_pen);

            ImageLoaderCallbacks imageLoaderCallback = new ImageLoaderCallbacks();
            getSupportLoaderManager().initLoader(IMAGES_LOADER, null, imageLoaderCallback);
        }
    }

    /**
     * While leaving the activity, we'll always unset the edit mode, as activity
     * can be restarted from stopped state , with New Task Mode.
     */
    @Override
    protected void onStop() {
        super.onStop();
        isEditMode = false;
    }

    /**
     * Enable set Reminder button only in Edit mode of task.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isEditMode) {
            mBtnSetAlarm.setEnabled(true);

            // If task has reminder on it, then display full date with time in Detail View of task.
            if (Long.parseLong(mReminderItem.getRemindOn()) > 0) {
                mCurrentReminder.setText("Remind On: " + Utility.getSimpleDateWithTime(mReminderItem.getRemindOn()));
                mCurrentReminder.setVisibility(View.VISIBLE);
            }
        } else {
            mBtnSetAlarm.setEnabled(false);
        }
    }

    /**
     * If image is clicked and stored from camera app.
     * then maintain a list of URIs of all images clicked yet.
     * While saving we'll insert these uri's in our local DB.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //savefilepath in local db
            ContentValues cv = new ContentValues();
            cv.put(ImageEntry.COLUMN_IMAGE_URL, mCurrentPhotoPath);
            cv.put(ImageEntry.COLUMN_IMAGE_NAME, mCurrentImageName);
            cv.put(ImageEntry.COLUMN_REMINDER_ID, 1);
            mImageUrlsList.add(cv);
        }
    }

    /**
     * Helper method to create File instance depicting DIR location
     * in internal storage, where Camera App will store our
     * clicked image and update the relevant URI.
     *
     * @return File instance of storage directory.
     * @throws IOException
     */
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

    /**
     * Inner Class of loader callbacks, for interaction with LoaderManager.
     * This will take care of asynchronously loading all the associated images with
     * reminder, with help of Picasso Library
     */
    private class ImageLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cLoader = null;
            //Create Uri for images
            if (mReminderItem != null) {
                long reminderID = Long.parseLong(mReminderItem.getId());
                Uri imageUri = ImageEntry.buildImageUriWithReminderId(reminderID);

                cLoader = new CursorLoader(AddEditActivity.this, imageUri,
                        IMAGE_COLUMNS,
                        null,
                        null,
                        null);
            }
            return cLoader;
        }

        // Asynchronously load images if any, for the current task.
        // and update the UI.
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.getCount() == 0) {
                return;
            }

            int countImages = 0;
            while (data.moveToNext()) {
                View imagesLayout = getLayoutInflater().inflate(R.layout.view_image_item, null);
                ImageView imageView = (ImageView) imagesLayout.findViewById(R.id.reminder_pic);
                TextView textView = (TextView) imagesLayout.findViewById(R.id.reminer_image_name);

                String imageUrl = data.getString(COL_IMAGE_URL);
                String imageName = data.getString(COL_IMAGE_NAME);

                if (imageUrl != null) {
                    Picasso.get().load(imageUrl).into(imageView);
                    textView.setText("Image " + ++countImages);
                }

                mImagesContainer.addView(imagesLayout);
            }
        }

        // Ignore reset as we are not implementing cursor adapter.
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    /**
     * Helper method to open Date Picker Dialog
     */
    private void openDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(AddEditActivity.this,
                onDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Set Alarm Date");

        datePickerDialog.show();

    }

    /**
     * DatesetListener to receiver date input from user via dialog box
     */
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            mYear = i;
            mMonth = i1;
            mDay = i2;

            openTimePickerDialog(false);
        }
    };

    /**
     * Helper method to open time picker dialog, immediately after user selected
     * the date for reminder
     *
     * @param is24r whether the date is given in 24Hh format or 12Hr format
     */
    private void openTimePickerDialog(boolean is24r) {
        Calendar calendar = Calendar.getInstance();

        timePickerDialog = new TimePickerDialog(AddEditActivity.this,
                onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), is24r);
        timePickerDialog.setTitle("Set Alarm Time");

        timePickerDialog.show();

    }

    /**
     * Anonymous implementation of TimeSetListener to receive reminder time of particular date,
     * to be set on current Reminder task.
     */
    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            mHour = hourOfDay;
            mMinute = minute;

            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();

            calSet.set(Calendar.YEAR, mYear);
            calSet.set(Calendar.MONTH, mMonth);
            calSet.set(Calendar.DAY_OF_MONTH, mDay);
            calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calSet.set(Calendar.MINUTE, minute);
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);

            if (calSet.compareTo(calNow) <= 0) {
                // Today Set time passed, count to tomorrow
                calSet.add(Calendar.DATE, 1);
            }

            setAlarm(calSet);
        }
    };


    /**
     * registers the selected date-time reminder value with the Android's AlarmManager
     * as soon as user edit and save the existing task.
     * NOTE: The reminder can't be set until user has created the task.
     * so this option is enabled only in edit mode.
     *
     * @param targetCal date-time value of reminder
     */
    private void setAlarm(Calendar targetCal) {
        Intent alarmIntent = new Intent(getBaseContext(), AlarmReceiver.class);
        alarmIntent.putExtra(AlarmReceiver.TASK_NAME, mReminderItem.getName());
        // Wrap in a pending intent , which fires only once
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                AddEditActivity.this, ALARM_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Set the alarm manager to wake the system
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),
                pendingIntent);

        Toast.makeText(this, "Alarm is set to: " + targetCal.getTime(), Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG, "alarm is set: " + targetCal.getTime() + ", " + targetCal.getTimeInMillis());
    }
}
