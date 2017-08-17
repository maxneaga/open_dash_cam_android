package com.opendashcam;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.opendashcam.models.Recording;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    //    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private String currentVideoFile = "null";


    //Camera 2 Dev Harsh Patel

    CameraDevice mCamera;
    CameraCaptureSession mSession;
    CaptureRequest mCaptureRequest;

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

    }

    private void initMediaRecorder(final SurfaceHolder surfaceHolder) {

        mediaRecorder = new MediaRecorder();

        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        String[] cameras = new String[0];
        try {
            cameras = manager.getCameraIdList();
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameras[0]);
            StreamConfigurationMap configs = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = configs.getOutputSizes(MediaCodec.class);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            manager.openCamera(cameras[0], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCamera = camera;

                    // Create directory for recordings if not exists
                    File RecordingsPath = new File(Util.getVideosDirectoryPath());
                    if (!RecordingsPath.isDirectory()) {
                        RecordingsPath.mkdir();
                    }

                    rotateRecordings(Util.getQuota());

                    mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

                    // Store previous and current recording filenames, so that they may be retrieved by the
                    // SaveRecordingWidget
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                            getString(R.string.current_recordings_preferences_key),
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    // previous recording = currentVideoFile
                    editor.putString(
                            getString(R.string.previous_recording_preferences_key),
                            currentVideoFile);
                    editor.commit();

                    // Path to the file with the recording to be created
                    currentVideoFile = Util.getVideosDirectoryPath() +
                            DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime()) +
                            ".mp4";

                    // // current recording = currentVideoFile (after updated)
                    editor.putString(
                            getString(R.string.current_recording_preferences_key),
                            currentVideoFile);
                    editor.commit();

                    mediaRecorder.setOutputFile(currentVideoFile);
                    mediaRecorder.setMaxDuration(Util.getMaxDuration());

                    // Set shutter sound based on preferences
                    disableSound(editor);

                    try {
                        mediaRecorder.setOutputFile(currentVideoFile);
                        mediaRecorder.setMaxDuration(Util.getMaxDuration());

                        mediaRecorder.prepare();
                        List<Surface> list = new ArrayList<>();
                        list.add(mediaRecorder.getSurface());
                        final CaptureRequest.Builder captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        captureRequest.addTarget(mediaRecorder.getSurface());
                        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                        mCaptureRequest = captureRequest.build();

                        mCamera.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mSession = session;
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                                mSession = session;
                            }
                        }, null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // When maximum video length reached
                    mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                        @Override
                        public void onInfo(MediaRecorder mr, int what, int extra) {
                            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                                Log.v("VIDEOCAPTURE", "Maximum Duration Reached");
                                mediaRecorder.stop();
                                mediaRecorder.reset();

                                // Let MediaStore Content Provider know about the new file
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(currentVideoFile))));
                                initMediaRecorder(surfaceHolder);
                            }
                        }
                    });

                    // Prepare
                    try {
                        mediaRecorder.prepare();
                    } catch (Exception e) {
                    }
                    mediaRecorder.start();

                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {

        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        // Let MediaStore Content Provider know about the new file
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(currentVideoFile))));

//        camera.lock();
//        camera.release();

        mCamera.close();
        reEnableSound();

        windowManager.removeView(surfaceView);

    }

    /**
     * Removes old recordings to create space for the new ones in order to stay withing the
     * set app quota.
     *
     * @param quota Maximum size the recordings directory may reach in megabytes
     */
    private void rotateRecordings(int quota) {
        File RecordingsPath = new File(Util.getVideosDirectoryPath());

        // Quota exceeded?
        while (Util.getFolderSize(RecordingsPath) >= quota) {
            File oldestFile = null;
            int starred_videos_total_size = 0;

            // Remove the oldest file in the directory
            for (File fileInDirectory : RecordingsPath.listFiles()) {
                // If this is the first run, assign the first file as the oldest
                if (oldestFile == null
                        || oldestFile.lastModified() > fileInDirectory.lastModified()) {
                    // Skip starred recordings, we don't want to rotate those
                    Recording recording = new Recording(this.getApplicationContext(), 0, fileInDirectory.getAbsolutePath());
                    if (recording.getStarredStatus()) {
                        starred_videos_total_size += fileInDirectory.length() / (1024 * 1024);
                        continue;
                    }

                    // Otherwise if not starred
                    oldestFile = fileInDirectory;
                }
            }

            if ((quota - starred_videos_total_size) < Util.getQuotaWarningThreshold()) {
                Util.showToastLong(
                        this.getApplicationContext(),
                        "WARNING: Low on space quota.\n" +
                                "Un-star videos to free up space.");
            }

            if (oldestFile == null) {
                return;
            }

            oldestFile.delete();

            // Let MediaStore Content Provider know about the deleted file
            sendBroadcast(
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(oldestFile))
            );
        }
    }

    /**
     * Disable system sounds if set in preferences
     *
     * @param editor Editor for current recordings preference
     */
    private void disableSound(SharedPreferences.Editor editor) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("disable_sound", true)) {
            // Record system volume before app was started
            AudioManager audio = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int volume = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
            editor.putInt(
                    getString(R.string.pre_start_volume),
                    volume);
            editor.commit();
            // Set to silent & vibrate
            audio.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }

    private void reEnableSound() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                getString(R.string.current_recordings_preferences_key),
                Context.MODE_PRIVATE);

        if (settings.getBoolean("disable_sound", false)) {
            // Record system volume before app was started
            AudioManager audio = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int volume = sharedPref.getInt(this.getString(R.string.pre_start_volume), 0);
            // Set to silent & vibrate
            audio.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
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