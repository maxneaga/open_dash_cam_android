package com.opendashcam;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.util.Date;

/**
 * Background video recording service.
 * Inspired by
 * https://stackoverflow.com/questions/15049041/background-video-recording-in-android-4-0
 * https://stackoverflow.com/questions/21264592/android-split-video-during-capture
 * Parts contributed by Toshio Azuma
 */
public class BackgroundVideoRecorder extends Service implements SurfaceHolder.Callback {
    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private static String VIDEOS_DIRECTORY_NAME = "OpenDashCam";
    private static String VIDEOS_DIRECTORY_PATH = Environment.getExternalStorageDirectory()+"/"+VIDEOS_DIRECTORY_NAME+"/";
    private String currentVideoFile;
    private static int QUOTA = 50;
    private static int MAX_DURATION = 5000; // 5 seconds

    @Override
    public void onCreate() {
        // Start in foreground to avoid unexpected kill
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Open Dash Cam recording")
                .setContentText("Video recording in progress")
                .setSmallIcon(R.drawable.ic_videocam_red_128dp)
                .build();
        startForeground(51288, notification);

        // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);

    }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {

        // Initialize Media Recorder
        initMediaRecorder(surfaceHolder);

        // Prepare
        try { mediaRecorder.prepare(); } catch (Exception e) {}
        mediaRecorder.start();

    }

    private void initMediaRecorder(final SurfaceHolder surfaceHolder) {
        // Create directory for recordings if not exists
        File RecordingsPath = new File(VIDEOS_DIRECTORY_PATH);
        if (!RecordingsPath.isDirectory()) {
            RecordingsPath.mkdir();
        }

        rotateRecordings(QUOTA);

        camera = Camera.open();
        mediaRecorder = new MediaRecorder();
        camera.unlock();

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Path to the file with the recording to be created
        currentVideoFile = VIDEOS_DIRECTORY_PATH+
                DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime())+
                ".mp4";

        mediaRecorder.setOutputFile(currentVideoFile);

        mediaRecorder.setMaxDuration(MAX_DURATION);

        // When maximum video length reached
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.v("VIDEOCAPTURE","Maximum Duration Reached");
                    mediaRecorder.stop();
                    mediaRecorder.reset();

                    // Let MediaStore Content Provider know about the new file
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(currentVideoFile))));

                    initMediaRecorder(surfaceHolder);

                    try { mediaRecorder.prepare(); } catch (Exception e) {}
                    mediaRecorder.start();
                }
            }
        });
    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {

        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        // Let MediaStore Content Provider know about the new file
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(currentVideoFile))));

        camera.lock();
        camera.release();

        windowManager.removeView(surfaceView);

    }

    /**
     * Removes old recordings to create space for the new ones in order to stay withing the
     * set app quota.
     * @param quota Maximum size the recordings directory may reach in megabytes
     */
    private void rotateRecordings(int quota) {
        File RecordingsPath = new File(VIDEOS_DIRECTORY_PATH);
        File oldestFile = null;

        // Quota exceeded?
        if (getFolderSize(RecordingsPath) >= quota) {
            // Remove the oldest file in the directory
            for (File fileInDirectory : RecordingsPath.listFiles()) {
                // If this is the first run, assign the first file as the oldest
                if (oldestFile == null
                        || oldestFile.lastModified() > fileInDirectory.lastModified())
                {
                    oldestFile = fileInDirectory;
                }
            }
            oldestFile.delete();

            // Let MediaStore Content Provider know about the deleted file
            sendBroadcast(
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(oldestFile))
            );

            // See if we need to delete more files to fit the quota
            rotateRecordings(quota);
        }
    }

    /**
     * Calculates the size of a directory in megabytes
     * @param file    The directory to calculate the size of
     * @return          size of a directory in megabytes
     */
    private long getFolderSize(File file) {
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

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }
}