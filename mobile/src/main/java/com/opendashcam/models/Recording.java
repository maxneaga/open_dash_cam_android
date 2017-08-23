package com.opendashcam.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.opendashcam.DBContract;
import com.opendashcam.DBHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model class for video recording
 */

public class Recording {
    public static final String ACTION_DATA_LOADED = "recording_data_loaded";
    private String id;
    private String filePath;
    private String filename;
    private String dateSaved;
    private String timeSaved;
    //    private Bitmap thumbnail;
    private boolean starred;
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d");
    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static HandlerThread thread = new HandlerThread("recording_data_fetcher");
    private static final Handler backgroundThread;
    private static final Handler mainThread = new Handler(Looper.getMainLooper());

    static {
        thread.start();
        backgroundThread = new Handler(thread.getLooper());
    }

    private static long lastSentTime = 0;

    public Recording(final Context context, int id, String filePath, boolean lazyLoad) {
        this.id = Integer.toString(id);
        this.filePath = filePath;
        this.filename = new File(filePath).getName();
        if (lazyLoad) {
            backgroundThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    init(context);
                    if (System.currentTimeMillis() - lastSentTime > 2000) {
                        lastSentTime = System.currentTimeMillis();
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DATA_LOADED));
                    } else {
                        mainThread.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (System.currentTimeMillis() - lastSentTime > 2000) {
                                    lastSentTime = System.currentTimeMillis();
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DATA_LOADED));
                                }
                            }
                        }, 1000);
                    }
                }
            }, 1000);
        } else {
            init(context);
        }
    }

    private void init(Context context) {
        // Get dates for display
        getDatesFromFile();

        // Check if starred
        starred = isStarred(context);
    }

    public Recording(Context context, int id, String filePath) {
        this.id = Integer.toString(id);
        this.filePath = filePath;
        this.filename = new File(filePath).getName();
        init(context);
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

//    public Bitmap getThumbnail() {
//        return thumbnail;
//    }

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
                new String[]{filename}
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
     *
     * @param context   Application context
     * @param isChecked Whether or not checkbox was marked as checked
     * @return True when marked as checked in DB, False otherwise
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
            String[] selectionArgs = {filename};

            starred = false;

            // Delete
            db.delete(DBContract.StarredRecording.TABLE_NAME, selection, selectionArgs);

            return false;
        }
    }

    private void getDatesFromFile() {
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            Date lastModDate = new Date(file.lastModified());
            dateSaved = DATE_FORMAT.format(lastModDate);
            timeSaved = TIME_FORMAT.format(lastModDate);
        } else {
            dateSaved = "Video " + id;
            timeSaved = "";
        }
    }

    public String getId() {
        return id;
    }
}
