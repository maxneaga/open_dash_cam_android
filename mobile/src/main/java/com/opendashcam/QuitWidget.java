package com.opendashcam;

import android.app.Service;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

/**
 * Stops the service and closes the application
 */

public class QuitWidget extends Widget {
    QuitWidget(final Service service, WindowManager windowManager) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.quit_widget;

        // Stop the application service on click
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop video recording service
                service.stopService(new Intent(service, BackgroundVideoRecorder.class));
                // Stop the widget service
                service.stopSelf();
            }
        });
    }
}
