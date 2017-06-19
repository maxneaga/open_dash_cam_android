package com.example.myfirstapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;

public class MainActivity extends Activity {
    //Button showcamWidget;

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

        // for Marshmallow (SDK 23) and newer versions, get overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivity(intent);
            }
        }


        // Start the widget service
        Intent i = new Intent(getApplicationContext(), WidgetService.class);
        startService(i);

        // Close the activity, we don't have an app window
        finish();
    }
}
