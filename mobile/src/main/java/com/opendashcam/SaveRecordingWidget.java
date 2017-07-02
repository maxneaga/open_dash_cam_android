package com.opendashcam;

import android.app.Service;
import android.view.View;
import android.view.WindowManager;


public class SaveRecordingWidget extends Widget {
    SaveRecordingWidget(Service service, WindowManager windowManager) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.save_recording_widget;

        // @TODO: Add save recording functionality
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
