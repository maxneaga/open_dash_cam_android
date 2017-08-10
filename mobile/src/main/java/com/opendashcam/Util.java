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

    public static long getFolderSize(File directory) {
        long length = 0;
        if(!directory.exists())
            return 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += getFolderSize(file);
        }
        return length/1024;
    }

    public static long getFreeSpaceExternalStorage() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        long free = externalStorageDir.getFreeSpace() / 1024 / 1024;
        return free;
    }

}
