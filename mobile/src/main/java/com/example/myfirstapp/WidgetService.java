package com.example.myfirstapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class WidgetService extends Service {

    private WindowManager windowManager;
    private ImageView camWidget;
    private ImageView settingsWidget;
    private ImageView quitWidget;
    private WindowManager.LayoutParams layoutParams;

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Add widgets
        addCameraWidget();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camWidget != null) windowManager.removeView(camWidget);
    }

    /**
     * Draws Camera widget on the screen and sets its onClick listener
     * Camera widget is the primary application widget
     */
    private void addCameraWidget() {
        camWidget = new ImageView(this);
        camWidget.setImageResource(R.drawable.ic_cam_widget);

        // Set position on screen
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        layoutParams.x = 10;
        layoutParams.y = 0;

        camWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle all application widgets
                if (settingsWidget == null && quitWidget == null) {
                    addSettingsWidget();
                    addQuitWidget();
                } else {
                    // Remove all but cam widgets
                    windowManager.removeView(settingsWidget);
                    settingsWidget = null;

                    windowManager.removeView(quitWidget);
                    quitWidget = null;
                }
            }
        });

        windowManager.addView(camWidget, layoutParams);
    }

    /**
     * Draws Settings widget on the screen and sets its onClick listener
     */
    private void addSettingsWidget() {
        settingsWidget = new ImageView(this);
        settingsWidget.setImageResource(R.drawable.ic_settings_widget);

        // Set position on screen
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        layoutParams.x = 16;
        layoutParams.y = 150;

        settingsWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On touch, remove icon
                if (settingsWidget != null) {
                    windowManager.removeView(settingsWidget);
                    settingsWidget = null;
                }
            }
        });

        windowManager.addView(settingsWidget, layoutParams);
    }

    /**
     * Draws Stop and Quit widget on the screen and sets its onClick listener
     */
    private void addQuitWidget() {
        quitWidget = new ImageView(this);
        quitWidget.setImageResource(R.drawable.ic_quit_widget);

        // Set position on screen
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        layoutParams.x = 16;
        layoutParams.y = 300;

        // On touch, stop the service
        quitWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove all widgets first
                if (camWidget != null) {
                    windowManager.removeView(camWidget);
                    camWidget = null;
                }

                if (settingsWidget != null) {
                    windowManager.removeView(settingsWidget);
                    settingsWidget = null;
                }

                if (quitWidget != null) {
                    windowManager.removeView(quitWidget);
                    quitWidget = null;
                }

                stopSelf();
            }
        });

        windowManager.addView(quitWidget, layoutParams);
    }
}
