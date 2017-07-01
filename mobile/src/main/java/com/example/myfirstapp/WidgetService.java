package com.example.myfirstapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class WidgetService extends Service {

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private CamWidget cameraWidget;
    private SettingsWidget settingsWidget;
    private QuitWidget quitWidget;
    List<Widget> togglableWidgets = new ArrayList<Widget>();

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


        // ***
        // Add widgets
        // ***
        // Quit
        quitWidget = new QuitWidget(this, windowManager);
        quitWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 16, 290);
        togglableWidgets.add(quitWidget);

        // Settings
        settingsWidget = new SettingsWidget(this, windowManager);
        settingsWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 16, 150);
        togglableWidgets.add(settingsWidget);

        // Camera (primary widget)
        cameraWidget = new CamWidget(this, windowManager, togglableWidgets.toArray(new Widget[togglableWidgets.size()]));
        cameraWidget.setPosition(Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 0);
        cameraWidget.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraWidget.hide();
        settingsWidget.hide();
        quitWidget.hide();
    }

}
