package com.opendashcam.models;

import android.app.Service;
import android.view.View;
import android.view.WindowManager;

import com.opendashcam.R;


public class SaveRecordingWidget extends Widget {
    public SaveRecordingWidget(Service service, WindowManager windowManager) {
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
