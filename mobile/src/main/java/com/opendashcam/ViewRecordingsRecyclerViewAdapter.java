package com.opendashcam;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.opendashcam.models.Recording;

import java.io.File;
import java.util.ArrayList;

/**
 * Adapter to display video recordings in ViewRecordingsActivity.
 * Supplies data to the RecyclerView for display.
 */

public class ViewRecordingsRecyclerViewAdapter extends RecyclerView
        .Adapter<ViewRecordingsRecyclerViewAdapter
        .RecordingHolder> {

    private static String LOG_TAG = "ViewRecordingsRecycl...";
    private ArrayList<Recording> recordings;
    private static RecordingClickListener recordingClickListener;
    private Context context;

    public ViewRecordingsRecyclerViewAdapter(Context appContext, ArrayList<Recording> myDataset) {
        recordings = myDataset;
        context = appContext;
    }

    public static class RecordingHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        ImageView thumbnail;
        TextView label;
        TextView dateTime;
        CheckBox starred;

        public RecordingHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            label = (TextView) itemView.findViewById(R.id.recordingDate);
            dateTime = (TextView) itemView.findViewById(R.id.recordingTime);
            starred =  (CheckBox) itemView.findViewById(R.id.starred);
            Log.i(LOG_TAG, "Add Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recordingClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(RecordingClickListener recordingClickListener) {
        this.recordingClickListener = recordingClickListener;
    }

    @Override
    public RecordingHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_recordings_row, parent, false);

        RecordingHolder recordingHolder = new RecordingHolder(view);
        return recordingHolder;
    }

    @Override
    public void onBindViewHolder(RecordingHolder holder, int position) {
        holder.thumbnail.setImageBitmap(recordings.get(position).thumbnail);
        holder.label.setText(recordings.get(position).dateSaved);
        holder.dateTime.setText(recordings.get(position).timeSaved);
        holder.starred.setTag(position);
        holder.starred.setChecked(isStarred(position));
        holder.starred.setOnCheckedChangeListener(starredListener);
    }

    /**
     * Check if recording is starred
     */
    private boolean isStarred(int position) {
        // Get filename
        File file = new File(recordings.get(position).filename);
        String filename = file.getName();

        // Get DB helper
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        long numRowsWithFilename = DatabaseUtils.queryNumEntries(
                db,
                DBContract.StarredRecording.TABLE_NAME,
                DBContract.StarredRecording.COLUMN_NAME_FILE + " LIKE ?",
                new String[] {filename}
            );

        if (numRowsWithFilename > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Action to happen when starred/unstarred
     */
    CompoundButton.OnCheckedChangeListener starredListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Get filename
            File fileToStar = new File(recordings.get((Integer)buttonView.getTag()).filename);
            String filename = fileToStar.getName();
            // Get DB helper
            DBHelper dbHelper = DBHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // If checked, add to the starred recording table in DB
            if (isChecked) {
                // Prepare for insertion to DB
                ContentValues values = new ContentValues();
                values.put(DBContract.StarredRecording.COLUMN_NAME_FILE, filename);
                // Insert
                db.insert(DBContract.StarredRecording.TABLE_NAME, null, values);
            } else {
                // Define "where" DB query
                String selection = DBContract.StarredRecording.COLUMN_NAME_FILE + " LIKE ?";
                String[] selectionArgs = { filename };
                // Delete
                db.delete(DBContract.StarredRecording.TABLE_NAME, selection, selectionArgs);
            }
        }
    };

    public void addItem(Recording recording, int index) {
        recordings.add(index, recording);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        recordings.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public interface RecordingClickListener {
        void onItemClick(int position, View v);
    }
}
