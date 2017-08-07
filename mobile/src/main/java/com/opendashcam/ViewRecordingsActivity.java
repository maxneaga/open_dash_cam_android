package com.opendashcam;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.opendashcam.models.Recording;

import java.io.File;
import java.util.ArrayList;

/**
 * Activity to view video recordings produced by this dash cam application
 * Displays all videos from paths matching %OpenDashCam%
 */

public class ViewRecordingsActivity extends AppCompatActivity {

    //set constants for MediaStore to query, and show videos
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    private final static String ORDER_BY = MediaStore.Video.Media._ID + " DESC";


    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static String LOG_TAG = "CardViewActivity";

    private Cursor cursor;
    private int columnIndex;
    private int columnMetaIndex;
    private Uri contentUri;
    ArrayList<Recording> recordings;

    private Context context;

    /**
     * Sets RecyclerView for gallery
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_view_recordings);

        // Run garbage collection
        System.gc();

        // set path
        contentUri = MEDIA_EXTERNAL_CONTENT_URI;

        // set RecyclerView for gallery
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recordings = getDataSet();
        adapter = new ViewRecordingsRecyclerViewAdapter(context, recordings);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Adds onClick listener to play the recording
     */
    @Override
    protected void onResume() {
        super.onResume();
        ((ViewRecordingsRecyclerViewAdapter) adapter).setOnItemClickListener(new ViewRecordingsRecyclerViewAdapter.RecordingClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);

                // Play recording on position
                Util.showToast(context, recordings.get(position).getDateSaved() +
                        " - " +
                        recordings.get(position).getTimeSaved());
                openFile(Uri.fromFile(new File(recordings.get(position).getFilePath())), "video/mp4");
            }
        });
    }

    /**
     * Populates array with Recording objects
     * @return ArrayList<Recording>
     */
    private ArrayList<Recording> getDataSet() {
        ArrayList results = new ArrayList<Recording>();

        //Here we set up a string array of the thumbnail ID column we want to get back
        String [] columns={_ID, MEDIA_DATA};
        // Now we create the cursor pointing to the external thumbnail store
        cursor = managedQuery(contentUri,
                columns, // Which columns to return
                MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                new String[] {"%OpenDashCam%"},       // WHERE clause selection arguments (none)
                ORDER_BY); // Order-by clause (descending by date added)
        int count= cursor.getCount();
        // We now get the column index of the thumbnail id
        columnIndex = cursor.getColumnIndex(_ID);
        // Meta data
        columnMetaIndex = cursor.getColumnIndex(MEDIA_DATA);
        //move position to first element
        cursor.moveToFirst();

        for(int i=0;i<count;i++)
        {
            // Get id
            int id = cursor.getInt(columnIndex);

            // Get filePath
            String filePath = cursor.getString(columnMetaIndex);

            // Add recording object to the arraylist
            Recording recording = new Recording(context, id, filePath);
            results.add(i, recording);

            cursor.moveToNext();
        }

        return results;
    }

    /**
     * Starts new activity to open speicified file
     * @param file  File to open
     * @param mimeType  Mime type of the file to open
     */
    private void openFile(Uri file, String mimeType) {
        Intent openFile = new Intent(Intent.ACTION_VIEW);
        openFile.setDataAndType(file, mimeType);
        openFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(openFile);
        } catch (ActivityNotFoundException e) {
            Log.i(LOG_TAG, "Cannot open file.");
        }
    }
}
