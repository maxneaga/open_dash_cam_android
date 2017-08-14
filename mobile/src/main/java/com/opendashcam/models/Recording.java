package com.opendashcam.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.opendashcam.DBContract;
import com.opendashcam.DBHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model class for video recording
 */

public class Recording {
    private String id;
    private String filePath;
    private String filename;
    private String dateSaved;
    private String timeSaved;
    private Bitmap thumbnail;
    private boolean starred;

    public Recording(Context context, int id, String filePath) {
        this.id = Integer.toString(id);
        this.filePath = filePath;
        this.filename = new File(filePath).getName();

        // Get video thumbnail
        thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                context.getContentResolver(),
                id, MediaStore.Video.Thumbnails.MINI_KIND, null);

        // Get dates for display
        getDatesFromFile();

        // Check if starred
        starred = isStarred(context);
    }

    //
    // Getters
    //
    public String getFilePath() {
        return filePath;
    }

    public String getDateSaved() {
        return dateSaved;
    }

    public String getTimeSaved() {
        return timeSaved;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public boolean getStarredStatus() {
        return starred;
    }

    /**
     * Check if recording is starred
     */
    private boolean isStarred(Context context) {
        // Get DB helper
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        long numRowsWithFilename = DatabaseUtils.queryNumEntries(
                db,
                DBContract.StarredRecording.TABLE_NAME,
                DBContract.StarredRecording.COLUMN_NAME_FILE + " = ?",
                new String[] {filename}
        );

        if (numRowsWithFilename > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks/unchecks a recording as starred in DB. Intended to be called by
     * OnCheckedChangeListener when video is starred/unstarred by the user.
     * @param context       Application context
     * @param isChecked     Whether or not checkbox was marked as checked
     * @return              True when marked as checked in DB, False otherwise
     */
    public boolean toggleStar(Context context, boolean isChecked) {
        // Get DB helper
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // If checked, add to the starred recording table in DB
        if (isChecked) {
            // Make sure not yet starred
            if (!isStarred(context)) {
                // Prepare for insertion to DB
                ContentValues values = new ContentValues();
                values.put(DBContract.StarredRecording.COLUMN_NAME_FILE, filename);
                // Insert
                db.insert(DBContract.StarredRecording.TABLE_NAME, null, values);

                starred = true;
            }
            return true;
        } else {
            // Define "where" DB query
            String selection = DBContract.StarredRecording.COLUMN_NAME_FILE + " LIKE ?";
            String[] selectionArgs = { filename };

            starred = false;

            // Delete
            db.delete(DBContract.StarredRecording.TABLE_NAME, selection, selectionArgs);

            return false;
        }
    }

    private void getDatesFromFile() {
        if(filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d");
            SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
            Date lastModDate = new Date(file.lastModified());
            dateSaved = DATE_FORMAT.format(lastModDate);
            timeSaved = TIME_FORMAT.format(lastModDate);
        } else {
            dateSaved = "Video " + id;
            timeSaved = "";
        }
    }
}
