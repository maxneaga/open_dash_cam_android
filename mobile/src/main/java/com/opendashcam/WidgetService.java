package com.opendashcam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.WindowManager;

import com.opendashcam.models.Widget;

public class WidgetService extends Service {

    private WindowManager windowManager;
    private Widget overlayWidget;
    private PowerManager.WakeLock mWakeLock;

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

        // Start in foreground to avoid unexpected kill
        startForeground(
                Util.FOREGROUND_NOTIFICATION_ID,
                Util.createStatusBarNotification(this)
        );

        //Prevent going to sleep mode while service is working
        //https://developer.android.com/reference/android/os/PowerManager.html
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    WidgetService.class.getSimpleName()
            );
            mWakeLock.acquire();
        }
    }

    @Override
    public void onDestroy() {

        // Remove rootView views from display
        if (overlayWidget != null) {
            overlayWidget.hide();
        }

        // Close DB connection
        DBHelper dbHelper = DBHelper.getInstance(this);
        dbHelper.close();

        // Return to home screen
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

        //remove wakelock
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        stopForeground(true);

    }
}
