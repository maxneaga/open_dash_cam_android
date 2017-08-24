package com.opendashcam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import com.opendashcam.models.Widget;

public class WidgetService extends Service {

    private WindowManager windowManager;
    private Widget overlayWidget;

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
        overlayWidget = new Widget(this, windowManager);
        overlayWidget.show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove rootView views from display
        overlayWidget.hide();

        // Close DB connection
        DBHelper dbHelper = DBHelper.getInstance(this);
        dbHelper.close();

        // Return to home screen
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
