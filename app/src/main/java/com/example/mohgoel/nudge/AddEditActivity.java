package com.example.mohgoel.nudge;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohgoel.nudge.beans.ReminderItem;
import com.example.mohgoel.nudge.data.NudgeContract.*;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AddEditActivity extends AppCompatActivity {
    private EditText mViewReminderName;
    private EditText mViewReminderDescription;
    private FloatingActionButton mBtnSaveReminder;
    private LinearLayout mImagesContainer;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int IMAGES_LOADER = 0;
    private String mCurrentPhotoPath = "";
    private String mCurrentImageName = "";

    public static final String REMINDER_PARCELABLE = "reminder";
    private ReminderItem mReminderItem;
    private boolean isEditMode;
    private ArrayList<ContentValues> imagesList = new ArrayList<>();

    //specify the columns for Images and define respecitive Projections
    private static final String[] IMAGE_COLUMNS = {
            ImageEntry.TABLE_NAME + "." + ImageEntry._ID,
            ImageEntry.COLUMN_REMINDER_ID,
            ImageEntry.COLUMN_IMAGE_NAME,
            ImageEntry.COLUMN_IMAGE_URL,
    };
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

        mReminderItem = getIntent().getParcelableExtra(REMINDER_PARCELABLE);
        mViewReminderName = (EditText) findViewById(R.id.add_reminder_name);
        mViewReminderDescription = (EditText) findViewById(R.id.add_reminder_desc);
        mImagesContainer = (LinearLayout) findViewById(R.id.images_container);

        mBtnSaveReminder = (FloatingActionButton) findViewById(R.id.btn_save_reminder);
        mBtnSaveReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cValues = new ContentValues();
                cValues.put(ReminderEntry.COLUMN_NAME, mViewReminderName.getText().toString().trim());
                cValues.put(ReminderEntry.COLUMN_DESCRIPTION, mViewReminderDescription.getText().toString().trim());
                cValues.put(ReminderEntry.COLUMN_CREATED_ON, System.currentTimeMillis());

                if (!isEditMode) {

                    Uri reminderUri = getContentResolver().insert(ReminderEntry.CONTENT_URI, cValues);
                    long reminderRowID = ContentUris.parseId(reminderUri);
                    int insertCount = insertImages(reminderRowID);


                    if (insertCount > 0) {
                        Toast.makeText(AddEditActivity.this, "Reminder Task Created with " + insertCount + " images.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //update the existing record

                    int updateCount = 0;
                    updateCount = getContentResolver().update(ReminderEntry.CONTENT_URI,
                            cValues,
                            ReminderEntry._ID + " = ?",
                            new String[]{mReminderItem.getId()});

                    int insertCount = insertImages(Long.parseLong(mReminderItem.getId()));
                }

                finish();
            }

            private int insertImages(long reminderRowID) {
                //add all images details to database via bulkInsert call
                int insertCount = 0;
                if (imagesList.size() > 0) {
                    // append rowid of reminder as foreign key value
                    for (ContentValues values : imagesList) {
                        values.put(ImageEntry.COLUMN_REMINDER_ID, reminderRowID);
                    }

                    ContentValues[] cvArray = new ContentValues[imagesList.size()];
                    imagesList.toArray(cvArray);
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mReminderItem != null) {
            isEditMode = true;
        }
        // Display relevant data ,if reminder is being edited
        if (isEditMode) {
            mViewReminderName.setText(mReminderItem.getName());
            mViewReminderDescription.setText(mReminderItem.getDescription());
            mBtnSaveReminder.setImageResource(R.drawable.ic_pen);

            ImageLoaderCallbacks imageLoaderCallback = new ImageLoaderCallbacks();
            getSupportLoaderManager().initLoader(IMAGES_LOADER, null, imageLoaderCallback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isEditMode = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //savefilepath in local db
            ContentValues cv = new ContentValues();
            cv.put(ImageEntry.COLUMN_IMAGE_URL, mCurrentPhotoPath);
            cv.put(ImageEntry.COLUMN_IMAGE_NAME, mCurrentImageName);
            cv.put(ImageEntry.COLUMN_REMINDER_ID , 1);
            imagesList.add(cv);
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

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
