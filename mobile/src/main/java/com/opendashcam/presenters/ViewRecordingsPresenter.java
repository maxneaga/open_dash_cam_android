package com.opendashcam.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.opendashcam.DBHelper;
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

    private IViewRecordings.View mView;
    private Handler mUpdateListHandler = new Handler();
    private BroadcastReceiver mBroadcastReceiver;

    public ViewRecordingsPresenter(IViewRecordings.View view) {
        mView = view;
    }

    @Override
    public void onStartView() {
        mView.updateRecordingsList(
                getDataSet()
        );
        //receive broadcasts when videos list are changed in sqlite
        registerBroadcastReceiver();
    }

    @Override
    public void onStopView() {
        stopUpdateList();
        unRegisterBroadcastReceiver();
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
        return DBHelper.getInstance(OpenDashApp.getAppContext()).selectAllRecordingsList();
    }

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent != null ? intent.getAction() : null;

                //update recordings list when video has been recorded
                if (action != null && action.equals(Util.ACTION_UPDATE_RECORDINGS_LIST)) {
                    mView.updateRecordingsList(
                            getDataSet()
                    );
                    Log.d(LOG_TAG_DEBUG, "ViewRecordingsPresenter.onReceive(): update recordings list");
                }
            }
        };
        LocalBroadcastManager.getInstance(mView.getActivity()).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(Util.ACTION_UPDATE_RECORDINGS_LIST)
        );
    }

    private void unRegisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mView.getActivity()).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }
}
