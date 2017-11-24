package com.opendashcam;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.opendashcam.models.Recording;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private volatile Camera camera = null;
    private volatile MediaRecorder mediaRecorder = null;
    private String currentVideoFile = "null";
    private SharedPreferences sharedPref;
    private HandlerThread thread;
    private Handler backgroundThread;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Handler mainThread = new Handler(Looper.getMainLooper());
    private File mRecordingsDirectory;

    @Override
    public void onCreate() {
        //long startTime = System.currentTimeMillis();
        thread = new HandlerThread("io_processor_thread");
        thread.start();
        backgroundThread = new Handler(thread.getLooper());

        // Start in foreground to avoid unexpected kill
        startForeground(
                Util.FOREGROUND_NOTIFICATION_ID,
                Util.createStatusBarNotification(this)
        );

        sharedPref = this.getApplicationContext().getSharedPreferences(
                getString(R.string.current_recordings_preferences_key),
                Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        settings = PreferenceManager.getDefaultSharedPreferences(this);

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

        // Set shutter sound based on preferences
        disableSound(editor);

        // Create directory for recordings if not exists
        mRecordingsDirectory = Util.getVideosDirectoryPath();
        if (!mRecordingsDirectory.isDirectory() || !mRecordingsDirectory.exists()) {
            mRecordingsDirectory.mkdir();
        }

        //long elapsedTime = System.currentTimeMillis() - startTime;
        //Log.i("DEBUG", "onCreate Time: " + (TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.MILLISECONDS)) + " milliseconds");
    }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        backgroundThread.post(new Runnable() {
            @Override
            public void run() {
                // Initialize Media Recorder
                initMediaRecorder(surfaceHolder);

                // Prepare
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    Log.d("VIDEOCAPTURE", "BackgroundVideoRecorder.run(): start recording");
                } catch (Exception e) {
                    Log.e("VIDEOCAPTURE", "mediaRecorder.prepare() threw exception for some reason!", e);
                }
            }
        });

    }

    private void initMediaRecorder(final SurfaceHolder surfaceHolder) {
        rotateRecordings(BackgroundVideoRecorder.this, Util.getQuota());
        camera = Camera.open();
        Camera.Parameters cameraParams = camera != null ? camera.getParameters() : null;
        camera.unlock();

        //define video quality
        int videoQuality;
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            videoQuality = CamcorderProfile.QUALITY_720P;
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            videoQuality = CamcorderProfile.QUALITY_480P;
        } else {
            videoQuality = CamcorderProfile.QUALITY_HIGH;
        }

        // Log.d("VIDEOCAPTURE", "BackgroundVideoRecorder.initMediaRecorder(): quality " + videoQuality);

        //create camcorder profile and set optimal video size
        CamcorderProfile camcorderProfile = CamcorderProfile.get(videoQuality);
        if (cameraParams != null) {
            List<Camera.Size> previewSizes = cameraParams.getSupportedPreviewSizes();
            List<Camera.Size> videoSizes = cameraParams.getSupportedVideoSizes();
            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            if (window != null && previewSizes != null && videoSizes != null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                window.getDefaultDisplay().getMetrics(displayMetrics);

                //get and set optimal video size
                Camera.Size videoSize = Util.getOptimalVideoSize(videoSizes, previewSizes, displayMetrics.widthPixels, displayMetrics.heightPixels);
                //  Log.d("VIDEOCAPTURE", "BackgroundVideoRecorder.initMediaRecorder(): optimal video size - " + videoSize.width + "x" + videoSize.height);

                camcorderProfile.videoFrameWidth = videoSize.width;
                camcorderProfile.videoFrameHeight = videoSize.height;
            }
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera); // TODO See if we can remove this line. We can't, because media recorder should know what camera object will be used
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(camcorderProfile);
        mediaRecorder.setVideoEncodingBitRate(3000000);
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        // Store previous and current recording filenames, so that they may be retrieved by the
        // SaveRecording button

        // previous recording = currentVideoFile
        editor.putString(
                getString(R.string.previous_recording_preferences_key),
                currentVideoFile);
        editor.apply();

        // Path to the file with the recording to be created
        currentVideoFile = mRecordingsDirectory.getAbsolutePath() + File.separator +
                DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime()) +
                ".mp4";

        // // current recording = currentVideoFile (after updated)
        editor.putString(
                getString(R.string.current_recording_preferences_key),
                currentVideoFile);
        editor.apply();

        mediaRecorder.setOutputFile(currentVideoFile);
        mediaRecorder.setMaxDuration(Util.getMaxDuration());

        // When maximum video length reached
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED && null != mediaRecorder) {
                    mediaRecorder.setOnInfoListener(null);
                    Log.d("VIDEOCAPTURE", "Maximum Duration Reached. Stop recording.");
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    if (null != camera) {
                        camera.lock();
                        camera.release();
                        camera = null;
                    }

                    //insert new entry to SQLite
                    Util.insertNewRecording(
                            new Recording(currentVideoFile)
                    );

                    surfaceCreated(surfaceHolder);
                }
            }
        });
    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {
        backgroundThread.post(new Runnable() {
            @Override
            public void run() {
                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder.setOnInfoListener(null);
                    mediaRecorder = null;
                }
                if (null != camera) {
                    camera.lock();
                    camera.release();
                    camera = null;
                }
                backgroundThread.removeCallbacksAndMessages(null);
                mainThread.removeCallbacksAndMessages(null);
                thread.quit();
                thread = null;
                backgroundThread = null;
                mainThread = null;

                //insert new entry to SQLite
                Util.insertNewRecording(
                        new Recording(currentVideoFile)
                );

                reEnableSound();
            }
        });

        windowManager.removeView(surfaceView);
        stopForeground(true);
    }

    /**
     * Removes old recordings to create space for the new ones in order to stay withing the
     * set app quota.
     *
     * @param quota Maximum size the recordings directory may reach in megabytes
     */
    private void rotateRecordings(Context context, int quota) {
        long startTime = System.currentTimeMillis();
        // Quota exceeded?
        while (Util.getFolderSize(mRecordingsDirectory) >= quota) {
            File oldestFile = null;
            int starred_videos_total_size = 0;

            // Remove the oldest file in the directory
            for (File fileInDirectory : mRecordingsDirectory.listFiles()) {
                // If this is the first run, assign the first file as the oldest
                if (oldestFile == null
                        || oldestFile.lastModified() > fileInDirectory.lastModified()) {
                    // Skip starred recordings, we don't want to rotate those
                    Recording recording = new Recording(fileInDirectory.getAbsolutePath());
                    if (recording.isStarred()) {
                        starred_videos_total_size += fileInDirectory.length() / (1024 * 1024);
                        continue;
                    }

                    // Otherwise if not starred
                    oldestFile = fileInDirectory;
                }
            }

            if ((quota - starred_videos_total_size) < Util.getQuotaWarningThreshold()) {
                Util.showToastLong(
                        context.getApplicationContext(),
                        "WARNING: Low on space quota.\n" +
                                "Un-star videos to free up space.");
            }

            if (oldestFile == null) {
                return;
            }

            //delete recording from storage and sqlite
            Util.deleteSingleRecording(
                    new Recording(oldestFile.getAbsolutePath())
            );

        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        Log.d("DEBUG", "rotateRecordings Time: " + (TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.MILLISECONDS)) + " milliseconds");
    }

    /**
     * Disable system sounds if set in preferences
     *
     * @param editor Editor for current recordings preference
     */
    private void disableSound(SharedPreferences.Editor editor) {
//        long startTime = System.currentTimeMillis();
        if (settings.getBoolean("disable_sound", true)) {
            // Record system volume before app was started
            AudioManager audio = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int volume = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
            editor.putInt(
                    getString(R.string.pre_start_volume),
                    volume);
            editor.apply();
            // Only make change if not in silent
            if (volume > 0) {
                // Set to silent & vibrate
                audio.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }
//        long elapsedTime = System.currentTimeMillis() - startTime;
//        Log.i("DEBUG", "disableSound Time: " + (TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.MILLISECONDS)) + " milliseconds");
    }

    private void reEnableSound() {
//        long startTime = System.currentTimeMillis();
        if (settings.getBoolean("disable_sound", false)) {
            // Record system volume before app was started
            AudioManager audio = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int volume = sharedPref.getInt(this.getString(R.string.pre_start_volume), 0);
            // Only make change if not in silent
            if (volume > 0) {
                // Set to silent & vibrate
                audio.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }
//        long elapsedTime = System.currentTimeMillis() - startTime;
//        Log.i("DEBUG", "reEnableSound Time: " + (TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.MILLISECONDS)) + " milliseconds");
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}