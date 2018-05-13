package com.example.mohgoel.nudge.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class NudgeContract {
    public static final String CONTENT_AUTHORITY = "com.example.mohgoel.nudge.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_REMINDER = ReminderEntry.TABLE_NAME;
    public static final String PATH_REMINDER_ID = ReminderEntry.TABLE_NAME + "/#";
    public static final String PATH_IMAGE = ImageEntry.TABLE_NAME;

    /* Inner class that defines the table contents of the reminder table */
    public static final class ReminderEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REMINDER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDER;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDER;

        public static final String TABLE_NAME = "reminder";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_CREATED_ON = "created_on";
        // reminder time set explicitly by user otherwise -1
        public static final String COLUMN_REMIND_ON = "remind_on";


        // Uri constructor to build for a specific reminder entry
        public static Uri buildReminderUri(long id) {
            Uri reminderUri = ContentUris.withAppendedId(CONTENT_URI, id);
            return reminderUri;
        }

        // Extract row id of reminder if Uri is given
        public static long getReminderIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

    }


    public static final class ImageEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_IMAGE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_IMAGE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_IMAGE;


        public static final String TABLE_NAME = "image";

        // Column with the foreign key into 'reminder' table
        public static final String COLUMN_REMINDER_ID = "rem_id";
        // name of the image
        public static final String COLUMN_IMAGE_NAME = "name";
        // url path of image stored in internal memory, for parent reminder
        public static final String COLUMN_IMAGE_URL = "image_url";

        // Get the Uri for all images for the given reminder id from reminder table
        public static Uri buildImageUriWithReminderId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Extract Reminder Id for the given image thumbnail
        public static long getReminderIdFromImageUri(Uri uri) {
            return Long.valueOf(uri.getLastPathSegment());
        }

    }
}


