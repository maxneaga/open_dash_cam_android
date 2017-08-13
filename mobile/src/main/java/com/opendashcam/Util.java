package com.opendashcam;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Global utility methods
 */

public final class Util {
    private static String VIDEOS_DIRECTORY_NAME = "OpenDashCam";
    private static String VIDEOS_DIRECTORY_PATH = Environment.getExternalStorageDirectory()+"/"+VIDEOS_DIRECTORY_NAME+"/";
    private static int QUOTA = 200; // megabytes
    private static int QUOTA_WARNING_THRESHOLD = 20; // megabytes
    private static int MAX_DURATION = 10000; // 10 seconds

    public static String getVideosDirectoryPath() {
        return VIDEOS_DIRECTORY_PATH;
    }

    public static int getQuota() {
        return QUOTA;
    }

    public static int getQuotaWarningThreshold() {
        return QUOTA_WARNING_THRESHOLD;
    }

    public static int getMaxDuration() {
        return MAX_DURATION;
    }

    /**
     * Displays toast message of LONG length
     * @param context   Application context
     * @param msg       Message to display
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Display a 9-seconds-long toast.
     * Inspired by https://stackoverflow.com/a/7173248
     * @param context   Application context
     * @param msg       Message to display
     */
    public static void showToastLong(Context context, String msg) {
        final Toast tag = Toast.makeText(context, msg,Toast.LENGTH_SHORT);

        tag.show();

        new CountDownTimer(9000, 1000)
        {

            public void onTick(long millisUntilFinished) {tag.show();}
            public void onFinish() {tag.show();}

        }.start();
    }

    /**
     * Starts new activity to open speicified file
     * @param file  File to open
     * @param mimeType  Mime type of the file to open
     */
    public static void openFile(Context context, Uri file, String mimeType) {
        Intent openFile = new Intent(Intent.ACTION_VIEW);
        openFile.setDataAndType(file, mimeType);
        openFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(openFile);
        } catch (ActivityNotFoundException e) {
            Log.i("OpenDashCam", "Cannot open file.");
        }
    }

    /**
     * Calculates the size of a directory in megabytes
     * @param file    The directory to calculate the size of
     * @return          size of a directory in megabytes
     */
    public static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File fileInDirectory : file.listFiles()) {
                size += getFolderSize(fileInDirectory);
            }
        } else {
            size=file.length();
        }
        return size/1024;
    }

    /**
     * Get available space on the device
     * @return
     */
    public static long getFreeSpaceExternalStorage() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        long free = externalStorageDir.getFreeSpace() / 1024 / 1024;
        return free;
    }

}
