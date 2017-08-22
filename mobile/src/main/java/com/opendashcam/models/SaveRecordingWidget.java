package com.opendashcam.models;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;

import com.opendashcam.R;
import com.opendashcam.Util;


public class SaveRecordingWidget extends Widget {
    public SaveRecordingWidget(final Service service, WindowManager windowManager) {
        super(service, windowManager);

        // Set image for the widget
        widgetDrawableResource = R.drawable.save_recording_widget;

        // Save current and previous recordings
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Access shared references file
                SharedPreferences sharedPref = service.getApplicationContext().getSharedPreferences(
                        service.getString(R.string.current_recordings_preferences_key),
                        Context.MODE_PRIVATE);

                // Save video that is being recorded now
                String currentVideoRecording = sharedPref.
                        getString(service.getString(R.string.current_recording_preferences_key),
                        "null");

                if (currentVideoRecording != "null") {
                    // star current recording
                    Recording recording = new Recording(service.getApplicationContext(), 0, currentVideoRecording);
                    recording.toggleStar(service.getApplicationContext(), true);
                }

                // Save the oldest (previous) recording
                String previousVideoRecording = sharedPref.
                        getString(service.getString(R.string.previous_recording_preferences_key),
                                "null");

                if (previousVideoRecording != "null") {
                    // star previous recording
                    Recording recording = new Recording(service.getApplicationContext(), 0, previousVideoRecording);
                    recording.toggleStar(service.getApplicationContext(), true);
                }

                // Show success message
                Util.showToastLong(service, service.getString(R.string.save_recording_success_msg));
            }
        });
    }
}
