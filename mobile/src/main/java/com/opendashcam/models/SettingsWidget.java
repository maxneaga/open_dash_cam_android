package com.opendashcam.models;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

import com.opendashcam.R;
import com.opendashcam.SettingsActivity;
import com.opendashcam.WidgetService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class SettingsWidget extends Widget {
    public SettingsWidget(final WidgetService service, WindowManager windowManager) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.settings_widget;

        // Open Settings activity
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(service, SettingsActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                service.startActivity(intent);
                // hide secondary widgets
                service.hideTogglableWidgets();
            }
        });
    }
}
