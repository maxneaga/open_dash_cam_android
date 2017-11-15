package com.opendashcam;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * Global utility methods
 */

public final class Util {
    private static String VIDEOS_DIRECTORY_NAME = "OpenDashCam";
    private static String VIDEOS_DIRECTORY_PATH = Environment.getExternalStorageDirectory()+"/"+VIDEOS_DIRECTORY_NAME+"/";
    private static int QUOTA = 750; // megabytes
    private static int QUOTA_WARNING_THRESHOLD = 200; // megabytes
    private static int MAX_DURATION = 45000; // 45 seconds

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

    /**
     * Delete all recordings created by the app
     */
    public static void deleteRecordings(Context context) {
        File recordings_directory = new File(VIDEOS_DIRECTORY_PATH);
        for (File fileInDirectory : recordings_directory.listFiles()) {
            fileInDirectory.delete();
            // Let MediaStore Content Provider know about the deleted file
            context.getApplicationContext().sendBroadcast(
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(fileInDirectory))
            );
        }
    }

    /**
     * Iterate over supported camera video sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param supportedVideoSizes Supported camera video sizes.
     * @param previewSizes        Supported camera preview sizes.
     * @param w                   The width of the view.
     * @param h                   The height of the view.
     * @return Best match camera video size to fit in the view.
     */
    public static Camera.Size getOptimalVideoSize(List<Camera.Size> supportedVideoSizes,
                                                  List<Camera.Size> previewSizes, int w, int h) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) 16 / 9;//(double) w / h;

        // Supported video sizes list might be null, it means that we are allowed to use the preview
        // sizes
        List<Camera.Size> videoSizes;
        if (supportedVideoSizes != null) {
            videoSizes = supportedVideoSizes;
        } else {
            videoSizes = previewSizes;
        }
        Camera.Size optimalSize = null;

        // Start with max value and refine as we iterate over available video sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a video size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : videoSizes) {
            //we need max size 1280x720
            if (size.width == 1920) continue;

            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find video size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : videoSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}
