package com.opendashcam.presenters;

import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.opendashcam.OpenDashApp;
import com.opendashcam.Util;
import com.opendashcam.models.Recording;

import java.io.File;
import java.util.ArrayList;

/**
 * Presenter for recordings screen
 * <p>
 * <p>
 * Created by Dmitriy V. Chernysh on 15.11.17.
 * <p>
 * https://fb.me/mobiledevpro/
 * https://github.com/dmitriy-chernysh
 * #MobileDevPro
 */

public class ViewRecordingsPresenter implements IViewRecordings.Presenter {

    private static final String LOG_TAG_DEBUG = ViewRecordingsPresenter.class.getSimpleName();

    //set constants for MediaStore to query, and show videos
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    private final static String ORDER_BY = MediaStore.Video.Media._ID + " DESC";

    private IViewRecordings.View mView;
    private Handler mUpdateListHandler = new Handler();

    public ViewRecordingsPresenter(IViewRecordings.View view) {
        mView = view;
    }

    @Override
    public void onStartView() {
        startUpdateList();
    }

    @Override
    public void onStopView() {
        stopUpdateList();
    }

    @Override
    public void onRecordingsItemPressed(Recording recordingItem) {
        if (recordingItem == null) return;
        // Play recording on position
        Util.showToast(mView.getActivity(), recordingItem.getDateSaved() +
                " - " +
                recordingItem.getTimeSaved());
        Util.openFile(
                mView.getActivity(),
                Uri.fromFile(new File(recordingItem.getFilePath())), "video/mp4");
    }

    private void startUpdateList() {
        if (mUpdateListHandler == null) mUpdateListHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mView.updateRecordingsList(
                        getDataSet()
                );
                Log.d(LOG_TAG_DEBUG, "ViewRecordingsPresenter.run(): update list");
                //update list  every 2 seconds
                if (mUpdateListHandler != null) {
                    mUpdateListHandler.postDelayed(this, 2000);
                }
            }
        };
        mUpdateListHandler.post(runnable);
    }

    private void stopUpdateList() {
        if (mUpdateListHandler != null) {
            mUpdateListHandler.removeCallbacksAndMessages(this);
            mUpdateListHandler = null;
        }
    }

    /**
     * Populates array with Recording objects
     *
     * @return ArrayList<Recording>
     */
    private ArrayList<Recording> getDataSet() {
        Cursor cursor;
        int columnIndex;
        int columnMetaIndex;
        ArrayList<Recording> results = new ArrayList<>();

        //Here we set up a string array of the thumbnail ID column we want to get back
        String[] columns = {_ID, MEDIA_DATA};
        // Now we create the cursor pointing to the external thumbnail store
        cursor = mView.getActivity().managedQuery(
                MEDIA_EXTERNAL_CONTENT_URI,
                columns, // Which columns to return
                MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                new String[]{"%OpenDashCam%"},       // WHERE clause selection arguments (none)
                ORDER_BY); // Order-by clause (descending by date added)
        int count = cursor.getCount();
        // We now get the column index of the thumbnail id
        columnIndex = cursor.getColumnIndex(_ID);
        // Meta data
        columnMetaIndex = cursor.getColumnIndex(MEDIA_DATA);
        //move position to first element
        cursor.moveToFirst();

        for (int i = 0; i < count; i++) {
            // Get id
            int id = cursor.getInt(columnIndex);

            // Get filePath
            String filePath = cursor.getString(columnMetaIndex);

            // Add recording object to the arraylist
            Recording recording = new Recording(OpenDashApp.getAppContext(), id, filePath);
            results.add(i, recording);

            cursor.moveToNext();
        }

        return results;
    }
}
