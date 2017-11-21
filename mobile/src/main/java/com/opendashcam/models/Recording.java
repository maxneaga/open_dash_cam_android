package com.opendashcam.models;

import android.text.TextUtils;

import com.opendashcam.DBHelper;
import com.opendashcam.OpenDashApp;
import com.opendashcam.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model class for video recording
 */

public class Recording {
    private int id;
    private String filePath;
    private String filename;
    private String dateSaved;
    private String timeSaved;
    private DBHelper dbHelper;

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d");
    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * Constructor for selecting rows from SQLite
     *
     * @param id       Unique id
     * @param filePath String
     */
    public Recording(int id, String filePath) {
        dbHelper = DBHelper.getInstance(OpenDashApp.getAppContext());
        this.id = id;
        this.filePath = filePath;
        this.filename = new File(filePath).getName();
        getDatesFromFile();
    }

    /**
     * Constructor for create a new recording from Video Recorder
     *
     * @param filePath String
     */
    public Recording(String filePath) {
        this(-1, filePath);
    }

    public String getFilePath() {
        return !TextUtils.isEmpty(filePath) ? filePath : "";
    }

    public String getFileName() {
        return !TextUtils.isEmpty(filename) ? filename : "";
    }

    public String getDateSaved() {
        return dateSaved;
    }

    public String getTimeSaved() {
        return timeSaved;
    }

    public boolean isStarred() {
        return dbHelper.isRecordingStarred(this);
    }

    /**
     * Checks/unchecks a recording as starred in DB. Intended to be called by
     * OnCheckedChangeListener when video is starred/unstarred by the user.
     *
     * @param isChecked Whether or not checkbox was marked as checked
     * @return True when marked as checked in DB, False otherwise
     */
    public boolean toggleStar(boolean isChecked) {
        //this item will be updated in the UI when asynctask will be finished
        Util.updateStar(this);
        return true;
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

    public int getId() {
        return id;
    }

}
