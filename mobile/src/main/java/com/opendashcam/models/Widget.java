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
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

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
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

//        rootView.setImageResource(widgetDrawableResource);

        // Set position on screen
        layoutParams.gravity = this.gravity;
        layoutParams.x = this.x;
        layoutParams.y = this.y;

        windowManager.addView(viewHolder.rootViewMenu, layoutParams);
        windowManager.addView(viewHolder.rootView, layoutParams);
    }

    /**
     * Removes the rootView from screen
     */
    public void hide() {
        //widget for "rec" button
        windowManager.removeView(viewHolder.rootView);
        //widget for menu
        windowManager.removeView(viewHolder.rootViewMenu);
    }

    /**
     * Toggles the visibility of the rootView on screen
     */
    public void toggle() {
        viewHolder.toggleSecondaryWidgets();
    }

    private class WidgetViewHolder implements View.OnClickListener {
        View rootView;
        View rootViewMenu;
        View viewRecView;
        View saveRecView;
        View recView;
        View settingsView;
        View stopAndQuitView;
        View layoutMenu;
        boolean areSecondaryWidgetsShown = false;

        WidgetViewHolder(Context context) {

            rootView = LayoutInflater.from(context).inflate(R.layout.layout_widgets, null);
            recView = rootView.findViewById(R.id.rec_button);

            rootViewMenu = LayoutInflater.from(context).inflate(R.layout.layout_widget_menu, null);
            viewRecView = rootViewMenu.findViewById(R.id.view_recordings_button);
            saveRecView = rootViewMenu.findViewById(R.id.save_recording_button);
            settingsView = rootViewMenu.findViewById(R.id.settings_button);
            stopAndQuitView = rootViewMenu.findViewById(R.id.stop_and_quit_button);
            layoutMenu = rootViewMenu.findViewById(R.id.layout_menu);

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
                        Recording recording = new Recording(currentVideoRecording);
                        recording.toggleStar(true);
                    }

                    // Save the oldest (previous) recording
                    String previousVideoRecording = sharedPref.
                            getString(service.getString(R.string.previous_recording_preferences_key),
                                    "null");

                    if (previousVideoRecording != "null") {
                        // star previous recording
                        Recording recording = new Recording( 0, previousVideoRecording);
                        recording.toggleStar(true);
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
            rootViewMenu.setVisibility(View.VISIBLE);

            //show menu layout with animation
            Animation animation = new ScaleAnimation(
                    0f, 1f,
                    0f, 1f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            animation.setFillAfter(true);
            animation.setDuration(200);
            layoutMenu.startAnimation(animation);

            areSecondaryWidgetsShown = true;
        }

        private void hideSecondaryWidgets() {
            //hide menu layout with animation
            Animation animation = new ScaleAnimation(
                    1f, 0f,
                    1f, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            //on the first start no need to show animation, set 0
            animation.setDuration(areSecondaryWidgetsShown ? 200 : 0);
            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rootViewMenu.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            layoutMenu.startAnimation(animation);

            areSecondaryWidgetsShown = false;
        }
    }
}
