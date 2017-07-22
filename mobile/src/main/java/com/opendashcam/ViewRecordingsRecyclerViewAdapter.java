package com.opendashcam;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public static class RecordingHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        ImageView thumbnail;
        TextView label;
        TextView dateTime;

        public RecordingHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            label = (TextView) itemView.findViewById(R.id.recordingDate);
            dateTime = (TextView) itemView.findViewById(R.id.recordingTime);
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

    public ViewRecordingsRecyclerViewAdapter(ArrayList<Recording> myDataset) {
        recordings = myDataset;
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
    }

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
