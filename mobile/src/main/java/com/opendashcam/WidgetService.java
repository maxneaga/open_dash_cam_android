package com.opendashcam;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WidgetService extends Service implements TextureView.SurfaceTextureListener, MediaRecorder.OnInfoListener {

    //Log Tag
    private static final String TAG = "WidgetService";

    private CamWidget cameraWidget;
    private SettingsWidget settingsWidget;
    private SaveRecordingWidget saveRecordingWidget;
    private ViewRecordingsWidget viewRecordingsWidget;
    private QuitWidget quitWidget;
    List<Widget> togglableWidgets = new ArrayList<Widget>();


    //Quality recorders
    private static final Boolean BEST_QUALITY = Boolean.TRUE;

    //Arquive Parameters
    private static final String FOLDER_NAME       = "OpenDashCam";
    private static final String ARQUIVE_EXTENSION = ".mp4";

    //1000*60 = 1 Minute
    private static final int DURATION = 1000*30;

    //surface width and height
    private static final int PARM_WIDTH = 20;
    private static final int PARM_HEIGT = 20;

    //To Camera Back: Camera.CameraInfo.CAMERA_FACING_BACK
    //To Camera Front: Camera.CameraInfo.CAMERA_FACING_FRONT
    private static final int CAMERA_FACE = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private static final int CAMERA_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;

    //Camera
    private static Camera mServiceCamera;

    //Media Recorder
    private MediaRecorder mMediaRecorder;

    //Texture View
    private TextureView mTextureView;

    //Windows manager dynamic
    private WindowManager mWindowManager;

    //Inflater
    public LayoutInflater minflater;

    private SurfaceTexture texture;

    @Override
    public void onCreate() {
        super.onCreate();
        //Check current camera faces
        if(!isCameraAvailable()){
            return;
        }
        //Initialize
        initializeAll();
        initCamWidget();
    }

    private void initCamWidget(){
        // ***
        // Add widgets
        // ***
        quitWidget = new QuitWidget(this, mWindowManager);
        quitWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 16, 290);
        togglableWidgets.add(quitWidget);

        // Settings
        settingsWidget = new SettingsWidget(this, mWindowManager);
        settingsWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 16, 150);
        togglableWidgets.add(settingsWidget);

        // Save recording
        saveRecordingWidget = new SaveRecordingWidget(this, mWindowManager);
        saveRecordingWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 16, -150);
        togglableWidgets.add(saveRecordingWidget);

        // View recordings
        viewRecordingsWidget = new ViewRecordingsWidget(this, mWindowManager);
        viewRecordingsWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 16, -290);
        togglableWidgets.add(viewRecordingsWidget);

        // Camera (primary widget)
        cameraWidget = new CamWidget(this, mWindowManager, togglableWidgets.toArray(new Widget[togglableWidgets.size()]));
        cameraWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 0);
        cameraWidget.show();
    }

    /*
    * Facade function
    * */
    public void initializeAll(){
        createSurfaceTexture();
        if(!startRecording()){
            Log.i(TAG, "Erros Ocurs when started the camera recorder");
        }
    }

    /*
    * Create surface and initial variables
    * */
    public void createSurfaceTexture() {

        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(this);
        minflater = (LayoutInflater)getSystemService (LAYOUT_INFLATER_SERVICE);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        FrameLayout mParentView = new FrameLayout(getApplicationContext());
        final WindowManager.LayoutParams param = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        param.width = PARM_WIDTH;
        param.height = PARM_HEIGT;
        mWindowManager.addView(mParentView, param);
        mParentView.addView(mTextureView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("OP", "ON BIND SERVICE");
        return null;
    }

    @Override
    public void onDestroy() {

        stopRecording();
        super.onDestroy();
        //Remove widget views from display
        viewRecordingsWidget.hide();
        saveRecordingWidget.hide();
        cameraWidget.hide();
        settingsWidget.hide();
        quitWidget.hide();

        Log.i(TAG, "On Destroy");
    }

    /*
    * Start Recording and set parameters
    * */
    public boolean startRecording(){
        try {

            //Set camera faces
            mServiceCamera = Camera.open(CAMERA_BACK);

            //Setting texture
            texture = new SurfaceTexture(10);
            try {
                mServiceCamera.setPreviewTexture(texture);
            } catch (IOException e) {
            }
            mServiceCamera.startPreview();

            mServiceCamera.unlock();

            //Media recorder
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);

            //Setting video and audio sources
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            //Setting listenner duration
            mMediaRecorder.setOnInfoListener(this);

            //Recorder on best Quality
            if( BEST_QUALITY ) {
                mMediaRecorder.setVideoFrameRate(30);
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                mMediaRecorder.setVideoEncodingBitRate(3000000);
            }
            mMediaRecorder.setOrientationHint(90);
            //Setting output directory
            mMediaRecorder.setOutputFile(getPathDirectory());
            //Check video duration
            if( DURATION >= 5000 ) {
                mMediaRecorder.setMaxDuration(DURATION);
            }
            //Prepare and start recorder
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    * Path Directory to Create folder
    * */
    private String getPathDirectory() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        File file   = new File(path+ File.separator+FOLDER_NAME+ File.separator);
        if( !file.exists() ) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/" + getFileName()+ARQUIVE_EXTENSION;
    }

    /*
    * Getting file name from current date
    * */
    public String getFileName(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
        return simpleDateFormat.format(date);
    }

    /*
    * Stop recorder
    * */
    public void stopRecording() {
        Log.i(TAG, "Recording Stopped");
        try {
            mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mMediaRecorder.stop();

        }catch (RuntimeException e){
            Log.d(TAG,"stop failed");
            mMediaRecorder.reset();
            mServiceCamera.stopPreview();
            mMediaRecorder.release();

            mServiceCamera.release();
            mServiceCamera = null;
        }
        mMediaRecorder.reset();

        mServiceCamera.stopPreview();
        mMediaRecorder.release();

        mServiceCamera.release();
        mServiceCamera = null;
    }

    /*
    * Check CAMERAS FACES
    * */
    private boolean isCameraAvailable() {
        int cameraCount = 0;
        boolean isFrontCameraAvailable = false;
        cameraCount = Camera.getNumberOfCameras();
        while (cameraCount > 0) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount--;
            Camera.getCameraInfo(cameraCount, cameraInfo);
            if (cameraInfo.facing == CAMERA_FACE) {
                isFrontCameraAvailable = true;
                break;
            }
        }
        return isFrontCameraAvailable;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Call all time when service is calling
        Log.i(TAG, "On Start Command");
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "SURFACE IS NOW AVAILABLE");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if( mServiceCamera != null ) {
            mServiceCamera.setPreviewCallback(null);
            mServiceCamera.stopPreview();
            mServiceCamera.release();
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecording();
            createSurfaceTexture();
            startRecording();
        }
    }

}