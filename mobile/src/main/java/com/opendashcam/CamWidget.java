package com.opendashcam;

import android.app.Service;
import android.view.View;
import android.view.WindowManager;


/**
 * Camera is the primary widget that toggles the visibility of the other application widgets
 */

public class CamWidget extends Widget {

    /**
     *
     * @param service Context service
     * @param windowManager windowManager for the widget display
     * @param togglableWidgets List of widgets to toggle visibility of when this widget is touched
     */
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