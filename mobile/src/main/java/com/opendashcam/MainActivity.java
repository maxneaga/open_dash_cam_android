package com.opendashcam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.opendashcam.Util.getFolderSize;
import static com.opendashcam.Util.getFreeSpaceExternalStorage;

public class MainActivity extends Activity {

    public static final int MULTIPLE_PERMISSIONS_RESPONSE_CODE = 10;

    String[] permissions= new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check permissions
        if (!checkDrawPermission()) {
            finish();
            return;
        }
        if (checkPermissions()) {
            startApp();
        }
    }

    private void startApp() {

        if (!isEnoughStorage()) {
            Util.showToastLong(this.getApplicationContext(),
                    "Not enough storage to run the app. Clean up space for recordings.");
        }
        else {
            // Launch navigation app
            launchNavigation();

            // Start recording video
            Intent videoIntent = new Intent(getApplicationContext(), BackgroundVideoRecorder.class);
            startService(videoIntent);

            // Start widget service
            Intent i = new Intent(getApplicationContext(), WidgetService.class);
            startService(i);

        }
        // Close the activity, we don't have an app window
        finish();
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

                Toast.makeText(MainActivity.this, "Draw over apps permission needed", Toast.LENGTH_LONG)
                        .show();

                Toast.makeText(MainActivity.this, "Allow and click \"Back\"", Toast.LENGTH_LONG)
                        .show();

                Toast.makeText(MainActivity.this, "Then restart the Open Dash Cam app", Toast.LENGTH_LONG)
                        .show();

                return false;
            }
        }
        return true;
    }


    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ActivityCompat.checkSelfPermission(MainActivity.this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    MULTIPLE_PERMISSIONS_RESPONSE_CODE );
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS_RESPONSE_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted
                    startApp();
                } else {
                    // permissions not granted
                    Toast.makeText(MainActivity.this, "Permissions denied. The app cannot start.", Toast.LENGTH_LONG)
                            .show();

                    Toast.makeText(MainActivity.this, "Please re-start Open Dash Cam app and grant the requested permissions.", Toast.LENGTH_LONG)
                            .show();

                    finish();
                }
                return;
            }
        }
    }

    /**
     * Checks if Android Auto is installed and starts it as a background navigation app.
     * Otherwise starts default navigation app.
     */
    private void launchNavigation() {
        String androidAutoPackage = "com.google.android.projection.gearhead";

        PackageManager packageManager = getPackageManager();
        ApplicationInfo applicationInfo = null;
        // Check if Android Auto is installed on the device
        try {
            applicationInfo = packageManager.getApplicationInfo(androidAutoPackage, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (applicationInfo == null) {
            // not installed, open default navigation app
            Uri location = Uri.parse("geo:0,0?free=1&mode=d&entry=fnls");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
            startActivity(mapIntent);
        } else {
            // Installed, open Android Auto
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(androidAutoPackage);
            startActivity(launchIntent);
        }
    }

    private boolean isEnoughStorage(){
        long appFolderSie = getFolderSize(new File(Util.getVideosDirectoryPath()));
        if(getFreeSpaceExternalStorage() + appFolderSie < (Util.getQuota() + 250)){
            return false;
        }else {
            return true;
        }
    }
}
