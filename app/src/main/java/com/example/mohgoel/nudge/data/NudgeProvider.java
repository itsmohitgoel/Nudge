package com.example.mohgoel.nudge.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.example.mohgoel.nudge.data.NudgeContract.*;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class NudgeProvider extends ContentProvider {
    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private NudgeDbHelper mNudgeDbHelper;

    static final int REMINDER = 100;
    static final int REMINDER_WITH_ID = 101;
    static final int IMAGE = 200;
    static final int IMAGE_WITH_REMINDER_ID = 201;

    private static final SQLiteQueryBuilder mReminderQueryBuilder;
    private static final SQLiteQueryBuilder mReminderAndImageQueryBuilder;

    static {
        mReminderQueryBuilder = new SQLiteQueryBuilder();

        // This is a simple reminder query builder, without images informartion
        mReminderQueryBuilder.setTables(ReminderEntry.TABLE_NAME);

        // This query builder must perform inner join b/w two tables
        // NOTE: for each image only one reminder is present, but for single
        // reminder, there can be multiple images availaible
        mReminderAndImageQueryBuilder = new SQLiteQueryBuilder();
        mReminderAndImageQueryBuilder.setTables(
                ImageEntry.TABLE_NAME + " INNER JOIN " + ReminderEntry.TABLE_NAME +
                        " ON " + ImageEntry.TABLE_NAME + "." + ImageEntry.COLUMN_REMINDER_ID +
                        " = " + ReminderEntry.TABLE_NAME + "." + ReminderEntry._ID);
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(NudgeContract.CONTENT_AUTHORITY, NudgeContract.PATH_REMINDER, REMINDER);
        matcher.addURI(NudgeContract.CONTENT_AUTHORITY, NudgeContract.PATH_REMINDER_ID, REMINDER_WITH_ID);
        matcher.addURI(NudgeContract.CONTENT_AUTHORITY, NudgeContract.PATH_IMAGE, IMAGE);
        matcher.addURI(NudgeContract.CONTENT_AUTHORITY, NudgeContract.PATH_IMAGE + "/#", IMAGE_WITH_REMINDER_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mNudgeDbHelper = new NudgeDbHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case REMINDER:
                return ReminderEntry.CONTENT_TYPE;
            case REMINDER_WITH_ID:
                return ReminderEntry.CONTENT_ITEM_TYPE;
            case IMAGE:
                return ImageEntry.CONTENT_TYPE;
            case IMAGE_WITH_REMINDER_ID:
                return ImageEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = mUriMatcher.match(uri);
        SQLiteDatabase db = mNudgeDbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (match) {
            case REMINDER:
                cursor = mReminderQueryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            case REMINDER_WITH_ID:
                String reminderRowID = uri.getPathSegments().get(1);
                String reminderSelection = ReminderEntry.TABLE_NAME + "." + ReminderEntry._ID + " = ?";
                cursor = mReminderQueryBuilder.query(db,
                        projection,
                        reminderSelection,
                        new String[]{reminderRowID},
                        null, null,
                        sortOrder);
                break;
            case IMAGE:
                cursor = db.query(ImageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case IMAGE_WITH_REMINDER_ID:
                long reminderID = ImageEntry.getReminderIdFromImageUri(uri);
                String imageSelection = ImageEntry.TABLE_NAME + "." + ImageEntry.COLUMN_REMINDER_ID + " = ?";
                cursor = db.query(ImageEntry.TABLE_NAME,
                        projection,
                        imageSelection,
                        new String[]{Long.toString(reminderID)},
                        null, null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("unknown uri in query() method: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mNudgeDbHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);

        Uri returnUri = null;
        long _id = 0;
        switch (match) {
            case REMINDER:
                _id = db.insert(ReminderEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    //Inserted correctly
                    returnUri = ReminderEntry.buildReminderUri(_id);
                } else {
                    throw new SQLException("Failed to insert reminder row into: " + uri);
                }
                break;
            case IMAGE:
                _id = db.insert(ImageEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    returnUri = ImageEntry.buildImageUriWithReminderId(_id);
                } else {
                    throw new SQLException("Failed to insert image row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(returnUri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mNudgeDbHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int deleteCount = 0;

        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case REMINDER:
                deleteCount = db.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case IMAGE:
                deleteCount = db.delete(ImageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("unknown uri: " + uri);
        }

        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mNudgeDbHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);

        int updateCount = 0;

        switch (match) {
            case REMINDER:
                updateCount = db.update(ReminderEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case IMAGE:
                updateCount = db.update(ImageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri : " + uri);
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mNudgeDbHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case REMINDER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReminderEntry.TABLE_NAME, null, value);

                        if (_id > 0) {
                            //Inserted correctly
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case IMAGE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ImageEntry.TABLE_NAME, null, value);

                        if (_id > 0) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }
}
