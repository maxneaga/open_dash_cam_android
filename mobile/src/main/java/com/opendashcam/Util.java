package com.opendashcam;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.opendashcam.models.Recording;

import java.io.File;
import java.util.List;

/**
 * Global utility methods
 */

public final class Util {
    public static final String ACTION_UPDATE_RECORDINGS_LIST = "update.recordings.list";

    private static int QUOTA = 1000; // megabytes
    private static int QUOTA_WARNING_THRESHOLD = 200; // megabytes
    private static int MAX_DURATION = 45000; // 45 seconds

    public static File getVideosDirectoryPath() {
        //remove an old directory if exists
        File oldDirectory = new File(Environment.getExternalStorageDirectory() + "/OpenDashCam/");
        removeNonEmptyDirectory(oldDirectory);

        //New directory
        File appVideosFolder = getAppPrivateVideosFolder(OpenDashApp.getAppContext());

        if (appVideosFolder != null) {
            //create app-private folder if not exists
            if (!appVideosFolder.exists()) appVideosFolder.mkdir();
            return appVideosFolder;
        }

        return null;
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
     *
     * @param context Application context
     * @param msg     Message to display
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Display a 9-seconds-long toast.
     * Inspired by https://stackoverflow.com/a/7173248
     *
     * @param context Application context
     * @param msg     Message to display
     */
    public static void showToastLong(Context context, String msg) {
        final Toast tag = Toast.makeText(context, msg, Toast.LENGTH_SHORT);

        tag.show();

        new CountDownTimer(9000, 1000) {

            public void onTick(long millisUntilFinished) {
                tag.show();
            }

            public void onFinish() {
                tag.show();
            }

        }.start();
    }

    /**
     * Starts new activity to open speicified file
     *
     * @param file     File to open
     * @param mimeType Mime type of the file to open
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
     *
     * @param file The directory to calculate the size of
     * @return size of a directory in megabytes
     */
    public static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File fileInDirectory : file.listFiles()) {
                size += getFolderSize(fileInDirectory);
            }
        } else {
            size = file.length();
        }
        return size / 1024;
    }

    /**
     * Get available space on the device
     *
     * @return
     */
    public static long getFreeSpaceExternalStorage(File storagePath) {
        if (storagePath == null || !storagePath.isDirectory()) return 0;
        return storagePath.getFreeSpace() / 1024 / 1024;
    }

    /**
     * Delete all recordings from storage and sqlite
     * <p>
     * NOTE: called from UI settings (here uses asynctask for background operation)
     */
    public static void deleteRecordings() {
        AsyncTaskCompat.executeParallel(new DeleteRecordingsTask());
    }

    /**
     * Star/unstar recording
     * <p>
     * NOTE: called from UI (uses asynctasks)
     *
     * @param recording
     */
    public static void updateStar(Recording recording) {
        AsyncTaskCompat.executeParallel(new UpdateStarTask(recording));
    }

    /**
     * Delete single recording from storage and SQLite
     * <p>
     * NOTE: called from background thread (BackgroundVideoRecorder)
     *
     * @param recording Recording
     */
    public static void deleteSingleRecording(Recording recording) {
        if (recording == null) return;
        //delete from storage
        new File(recording.getFilePath()).delete();

        //delete from db
        DBHelper.getInstance(OpenDashApp.getAppContext()).deleteRecording(
                new Recording(recording.getFilePath())
        );

        //broadcast for updating videos list in UI
        LocalBroadcastManager.getInstance(OpenDashApp.getAppContext()).sendBroadcast(
                new Intent(ACTION_UPDATE_RECORDINGS_LIST)
        );
    }

    /**
     * Insert new recording to SQLite
     * <p>
     * NOTE: called from background thread (BackgroundVideoRecorder)
     *
     * @param recording Recording
     */
    public static void insertNewRecording(Recording recording) {
        if (recording == null) return;
        DBHelper.getInstance(OpenDashApp.getAppContext()).insertNewRecording(recording);

        //broadcast for updating videos list in UI
        LocalBroadcastManager.getInstance(OpenDashApp.getAppContext()).sendBroadcast(
                new Intent(ACTION_UPDATE_RECORDINGS_LIST)
        );
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

    /**
     * Get path to app-private folder (Android/data/[app name]/files)
     *
     * @param context Context
     * @return Folder
     */
    private static File getAppPrivateVideosFolder(Context context) {
        try {
            File[] extAppFolders = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_MOVIES);
            if (extAppFolders == null) return null;

            for (File file : extAppFolders) {
                if (file != null) {
                    //find external app-private folder (emulated - it's internal storage)
                    if (!file.getAbsolutePath().toLowerCase().contains("emulated") && isStorageMounted(file)) {
                        return file;
                    }
                }
            }

            //if external storage is not found
            if (extAppFolders.length > 0) {
                File appFolder;
                //get available app-private folder form the list
                for (int i = extAppFolders.length - 1, j = 0; i >= j; i--) {
                    appFolder = extAppFolders[i];
                    if (appFolder != null && isStorageMounted(appFolder)) {
                        return appFolder;
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(Util.class.getSimpleName(), "getAppPrivateVideosFolder: Exception - " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Check if storage mounted and has read/write access.
     *
     * @param storagePath Storage path
     * @return True - can write data
     */
    private static boolean isStorageMounted(File storagePath) {
        String storageState = EnvironmentCompat.getStorageState(storagePath);
        return storageState.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Remove non-empty directory
     *
     * @param path Directory path
     * @return True - Removed
     */
    private static boolean removeNonEmptyDirectory(File path) {
        if (path.exists()) {
            for (File file : path.listFiles()) {
                if (file.isDirectory()) {
                    removeNonEmptyDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }


    /**
     * AsyncTask for delete recordings from storage and SQLite
     */
    private static class DeleteRecordingsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            File recordingsDirectory = getVideosDirectoryPath();
            //remove items from SQLite database
            boolean result = DBHelper.getInstance(OpenDashApp.getAppContext()).deleteAllRecordings();

            if (result) {
                //remove files from storage
                for (File fileInDirectory : recordingsDirectory.listFiles()) {
                    fileInDirectory.delete();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Context context = OpenDashApp.getAppContext();
                Util.showToastLong(
                        context,
                        context.getResources().getString(R.string.pref_delete_recordings_confirmation)
                );
            }
        }
    }

    /**
     * AsyncTask for star/unstar
     */
    private static class UpdateStarTask extends AsyncTask<Void, Void, Void> {
        private Recording mRecording;

        UpdateStarTask(Recording recording) {
            mRecording = recording;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBHelper dbHelper = DBHelper.getInstance(OpenDashApp.getAppContext());
            //insert or delete star
            dbHelper.updateStar(mRecording);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //broadcast for updating videos list in UI
            LocalBroadcastManager.getInstance(OpenDashApp.getAppContext()).sendBroadcast(
                    new Intent(Util.ACTION_UPDATE_RECORDINGS_LIST)
            );
        }
    }

}
