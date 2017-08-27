package com.opendashcam.models;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.opendashcam.BackgroundVideoRecorder;
import com.opendashcam.R;
import com.opendashcam.SettingsActivity;
import com.opendashcam.Util;
import com.opendashcam.ViewRecordingsActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Abstract class for all specific rootView classes to extend from
 */

public class Widget {
    protected Service service;
    protected WindowManager windowManager;
    private WidgetViewHolder viewHolder;

    private WindowManager.LayoutParams layoutParams;
    private int gravity = Gravity.CENTER_VERTICAL | Gravity.START;
    private int x = 0;
    private int y = 0;

    public Widget(Service service, WindowManager windowManager) {
        this.service = service;
        this.windowManager = windowManager;
        this.viewHolder = new WidgetViewHolder(service);
    }

    public void setPosition(int gravity, int x, int y) {
        this.gravity = gravity;
        this.x = x;
        this.y = y;
    }

    /**
     * Displays the rootView on screen
     */
    public void show() {
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

//        rootView.setImageResource(widgetDrawableResource);

        // Set position on screen
        layoutParams.gravity = this.gravity;
        layoutParams.x = this.x;
        layoutParams.y = this.y;

        windowManager.addView(viewHolder.rootView, layoutParams);
    }

    /**
     * Removes the rootView from screen
     */
    public void hide() {
        windowManager.removeView(viewHolder.rootView);
    }

    /**
     * Toggles the visibility of the rootView on screen
     */
    public void toggle() {
        viewHolder.toggleSecondaryWidgets();
    }

    private class WidgetViewHolder implements View.OnClickListener {
        View rootView;
        View viewRecView;
        View saveRecView;
        View recView;
        View settingsView;
        View stopAndQuitView;
        boolean areSecondaryWidgetsShown = false;

        public WidgetViewHolder(Context context) {

            rootView = LayoutInflater.from(context).inflate(R.layout.layout_widgets, null);
            viewRecView = rootView.findViewById(R.id.view_recordings_button);
            saveRecView = rootView.findViewById(R.id.save_recording_button);
            recView = rootView.findViewById(R.id.rec_button);
            settingsView = rootView.findViewById(R.id.settings_button);
            stopAndQuitView = rootView.findViewById(R.id.stop_and_quit_button);
            viewRecView.setOnClickListener(this);
            saveRecView.setOnClickListener(this);
            recView.setOnClickListener(this);
            settingsView.setOnClickListener(this);
            stopAndQuitView.setOnClickListener(this);
            hideSecondaryWidgets();
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.view_recordings_button:
                    Intent viewRecordingsIntent = new Intent(service, ViewRecordingsActivity.class);
                    viewRecordingsIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    service.startActivity(viewRecordingsIntent);
                    hideSecondaryWidgets();
                    break;
                case R.id.save_recording_button:
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
                    break;
                case R.id.rec_button:
                    toggleSecondaryWidgets();
                    break;
                case R.id.settings_button:
                    Intent settingsIntent = new Intent(service, SettingsActivity.class);
                    settingsIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    service.startActivity(settingsIntent);
                    // hide secondary widgets
                    hideSecondaryWidgets();
                    break;
                case R.id.stop_and_quit_button:
                    // Stop video recording service
                    service.stopService(new Intent(service, BackgroundVideoRecorder.class));
                    // Stop the rootView service
                    service.stopSelf();
                    break;
            }
        }

        private void toggleSecondaryWidgets() {
            if (areSecondaryWidgetsShown) {
                hideSecondaryWidgets();
            } else {
                showSecondaryWidgets();
            }
        }

        private void showSecondaryWidgets() {
            viewRecView.setVisibility(View.VISIBLE);
            saveRecView.setVisibility(View.VISIBLE);
            settingsView.setVisibility(View.VISIBLE);
            stopAndQuitView.setVisibility(View.VISIBLE);
            areSecondaryWidgetsShown = true;
        }

        private void hideSecondaryWidgets() {
            viewRecView.setVisibility(View.GONE);
            saveRecView.setVisibility(View.GONE);
            settingsView.setVisibility(View.GONE);
            stopAndQuitView.setVisibility(View.GONE);
            areSecondaryWidgetsShown = false;
        }
    }
}
