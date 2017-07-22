package com.opendashcam;

import android.graphics.Bitmap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Max on 7/22/2017.
 */

public class Recording {
    public String id;
    public String filename;
    public String dateSaved;
    public String timeSaved;
    public Bitmap thumbnail;

    public void getDatesFromFile() {
        if(filename != null && !filename.isEmpty()) {
            File file = new File(filename);
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
