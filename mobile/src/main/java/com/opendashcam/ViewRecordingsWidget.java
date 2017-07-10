package com.opendashcam;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class ViewRecordingsWidget extends Widget {
    ViewRecordingsWidget(final WidgetService service, WindowManager windowManager) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.view_recordings_widget;

        // Open View Recordings activity
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(service, ViewRecordingsActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                service.startActivity(intent);
                // hide secondary widgets
                service.hideTogglableWidgets();
            }
        });
    }
}
