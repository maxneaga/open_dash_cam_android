package com.opendashcam.presenters;

import android.app.Activity;

import com.opendashcam.models.Recording;

import java.util.ArrayList;

/**
 * Interface for recordings screen
 * <p>
 * <p>
 * Created by Dmitriy V. Chernysh on 15.11.17.
 * <p>
 * https://fb.me/mobiledevpro/
 * https://github.com/dmitriy-chernysh
 * #MobileDevPro
 */

public interface IViewRecordings {
    interface View {
        Activity getActivity();

        /**
         * Populate list
         *
         * @param recordingsList List of recordings
         */
        void updateRecordingsList(ArrayList<Recording> recordingsList);
    }

    interface Presenter {
        void onStartView();

        void onStopView();

        void onRecordingsItemPressed(Recording recordingItem);
    }
}
