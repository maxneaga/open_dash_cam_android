package com.example.myfirstapp;

import android.app.Service;
import android.view.View;
import android.view.WindowManager;


public class CamWidget extends Widget {

    CamWidget(final Service service, WindowManager windowManager, final Widget[] togglableWidgets) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.cam_widget;

        // Toggle visibility of other widgets on click
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < togglableWidgets.length; i++) {
                    togglableWidgets[i].toggle();
                }
            }
        });
    }

}
