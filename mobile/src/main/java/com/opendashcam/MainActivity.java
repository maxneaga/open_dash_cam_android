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
import android.widget.Toast;

public class MainActivity extends Activity {

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



        // Check permissions
        if (!checkDrawPermission() || !checkCameraPermission()) {
            finish();
            return;
        }

        // Start widget service
        Intent i = new Intent(getApplicationContext(), WidgetService.class);
        startService(i);

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

                Toast.makeText(MainActivity.this, "Draw over apps permission needed. Allow and re-start the app", Toast.LENGTH_LONG)
                        .show();

                return false;
            }
        }
        return true;
    }

    private boolean checkCameraPermission() {
        // Check for camera permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Camera permission needed. Allow and re-start the app", Toast.LENGTH_LONG)
                    .show();

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 777);

            return false;
        } else {
            return true;
        }
    }
}
