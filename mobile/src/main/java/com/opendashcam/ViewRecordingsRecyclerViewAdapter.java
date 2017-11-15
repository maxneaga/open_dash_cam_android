package com.opendashcam;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.opendashcam.models.Recording;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter to display video mRecordingsList in ViewRecordingsActivity.
 * Supplies data to the RecyclerView for display.
 */

public class ViewRecordingsRecyclerViewAdapter extends RecyclerView
        .Adapter<ViewRecordingsRecyclerViewAdapter
        .RecordingHolder> {

    public interface RecordingListener {
        void onItemClick(Recording recording);
    }

    private RecordingListener mRecordingsListener;
    private ArrayList<Recording> mRecordingsList = new ArrayList<>();
    private int mWidth, mHeight;

    ViewRecordingsRecyclerViewAdapter(Context context, RecordingListener clickListener) {
        mRecordingsListener = clickListener;
        mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        mHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
    }

    @Override
    public RecordingHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_recordings_row, parent, false);

        return new RecordingHolder(view);
    }

    @Override
    public void onBindViewHolder(RecordingHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        final Recording recItem = mRecordingsList.get(adapterPosition);

        if (recItem == null) return;

        Log.d("ViewRecordingsPresenter", "ViewRecordingsRecyclerViewAdapter.onBindViewHolder(): item - " + recItem.getId() +
                ", date - " + recItem.getDateSaved() +
                ", time - " + recItem.getTimeSaved() +
                ", starred - " + recItem.getStarredStatus()
        );

        holder.label.setText(recItem.getDateSaved());
        holder.dateTime.setText(recItem.getTimeSaved());
        holder.starred.setChecked(recItem.getStarredStatus());

        //action on item clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecordingsListener != null) {
                    mRecordingsListener.onItemClick(recItem);
                }
            }
        });

        //Action to happen when starred/unstarred
        holder.starred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    recItem.toggleStar(OpenDashApp.getAppContext(), isChecked);
                }
            }
        });

        String sArtworkUri = Uri.withAppendedPath(VideoRequestHandler.THUMBNAIL_IDENTIFIER_URI, String.valueOf(recItem.getId())).toString();
        Picasso.with(holder.itemView.getContext())
                .load(sArtworkUri)
                .resize(mWidth, mHeight)
                .noFade().centerCrop()
                .error(R.drawable.quit_widget)
                .placeholder(R.drawable.ic_videocam_red_128dp)
                .into(holder.thumbnail);
    }

    /**
     * Populate list
     *
     */
    public void populateList(ArrayList<Recording> myDataset) {
        mRecordingsList.clear();
        mRecordingsList.addAll(myDataset);
        notifyDataSetChanged();
    }

    public void addItem(Recording recording, int index) {
        mRecordingsList.add(index, recording);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mRecordingsList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mRecordingsList.size();
    }

    static class RecordingHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView label;
        TextView dateTime;
        CheckBox starred;

        RecordingHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            label = (TextView) itemView.findViewById(R.id.recordingDate);
            dateTime = (TextView) itemView.findViewById(R.id.recordingTime);
            starred = (CheckBox) itemView.findViewById(R.id.starred);
        }
    }
}
