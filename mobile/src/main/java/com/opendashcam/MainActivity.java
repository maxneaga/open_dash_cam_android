package com.opendashcam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_CAMERA = 1;

    //Request Audio
    private static final int REQUEST_AUDIO = 2;

    //External Storage
    private static final int REQUEST_STORAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // **
        // Google Maps BEGIN
        // **
        //Uri location = Uri.parse("geo:0,0?free=1&mode=d&entry=fnls");
        //Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
        //startActivity(mapIntent);
        // **
        // Google Maps ENDW
        // **
        setContentView(R.layout.activity_main);
        if (!checkDrawPermission()) {
            finish();
            return;
        }
        if(checkPermissionsForRecord()){
            startWidgetService();
        }else {
            askPermissionCamera();
        }
    }

    private void startWidgetService(){
        Intent intent = new Intent(MainActivity.this, WidgetService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
        finish();
    }


    private void askPermissionCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }else
            askPermissionAudio();
    }

    private void askPermissionAudio(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO);
        }
        else
            askPermissionStorage();
    }

    private void askPermissionStorage(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        }else
            startWidgetService();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    askPermissionAudio();
                } else {
                    //Denied
                    askPermissionCamera();
                    Toast.makeText(getApplicationContext(),
                            "Permission Camera Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    askPermissionStorage();
                } else {
                    //Denied
                    askPermissionAudio();
                    Toast.makeText(getApplicationContext(),
                            "Permission Audio Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    askPermissionCamera();
                } else {
                    //Denied
                    askPermissionStorage();
                    Toast.makeText(getApplicationContext(),
                            "Permission Storage Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    private boolean checkDrawPermission() {
        // for Marshmallow (SDK 23) and newer versions, get overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivity(intent);

                Toast.makeText(MainActivity.this, "Draw over apps permission needed. Allow and re-start the app", Toast.LENGTH_LONG)
                        .show();

                return false;
            }
        }
        return true;
    }

    private boolean checkPermissionsForRecord(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED|
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED|
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
}
