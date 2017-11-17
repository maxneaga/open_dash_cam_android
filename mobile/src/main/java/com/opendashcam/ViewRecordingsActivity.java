package com.opendashcam;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.opendashcam.models.Recording;
import com.opendashcam.presenters.IViewRecordings;
import com.opendashcam.presenters.ViewRecordingsPresenter;

import java.util.ArrayList;

/**
 * Activity to view video recordings produced by this dash cam application
 * Displays all videos from paths matching %OpenDashCam%
 */

public class ViewRecordingsActivity extends AppCompatActivity implements IViewRecordings.View {

    private RecyclerView mRecyclerView;
    private ViewRecordingsRecyclerViewAdapter mAdapter;
    private View mLayoutListEmpty;

    private IViewRecordings.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recordings);

        initRecyclerView();

        mPresenter = new ViewRecordingsPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStartView();
    }

    @Override
    protected void onStop() {
        mPresenter.onStopView();
        super.onStop();
    }

    @Override
    public void updateRecordingsList(ArrayList<Recording> recordingsList) {
        if (mRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) return;

        if (mAdapter != null) {
            mAdapter.populateList(recordingsList);
        }

        if (mRecyclerView != null && mLayoutListEmpty != null) {
            if (recordingsList == null || recordingsList.isEmpty()) {
                //show message "no video recordings yet ..."
                mRecyclerView.setVisibility(View.GONE);
                mLayoutListEmpty.setVisibility(View.VISIBLE);
            } else {
                //show non-empty videos list
                mRecyclerView.setVisibility(View.VISIBLE);
                mLayoutListEmpty.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    /**
     * Set recycler view for gallery
     */
    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutListEmpty = findViewById(R.id.layout_list_empty);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new ViewRecordingsRecyclerViewAdapter(
                this,
                new ViewRecordingsRecyclerViewAdapter.RecordingListener() {
                    @Override
                    public void onItemClick(Recording recording) {
                        mPresenter.onRecordingsItemPressed(recording);
                    }
                });
        mRecyclerView.setAdapter(mAdapter);
    }
}
