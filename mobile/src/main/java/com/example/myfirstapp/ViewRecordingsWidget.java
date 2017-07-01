package com.example.myfirstapp;

import android.app.Service;
import android.view.View;
import android.view.WindowManager;


public class ViewRecordingsWidget extends Widget {
    ViewRecordingsWidget(Service service, WindowManager windowManager) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.view_recordings_widget;

        // @TODO: View recordings functionality
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
