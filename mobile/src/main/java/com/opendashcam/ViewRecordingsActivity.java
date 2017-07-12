package com.opendashcam;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Activity to view video recordings produced by the dash cam
 * Based on https://stackoverflow.com/a/19178939
 */

public class ViewRecordingsActivity extends AppCompatActivity {

    //set constants for MediaStore to query, and show videos
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    private final static String ORDER_BY = MediaStore.Video.Media._ID + " DESC";

    private GridView gallery;
    private Cursor cursor;
    private int columnIndex;
    private int[] videosId;
    private Uri contentUri;
    private String filename;
    private int flag = 0;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_view_recordings);

        // Run garbage collection
        System.gc();

        // set GridView for gallery
        gallery = (GridView) findViewById(R.id.activity_view_recordings);
        // set path
        contentUri = MEDIA_EXTERNAL_CONTENT_URI;

        initVideosId();

        //set gallery adapter
        setGalleryAdapter();
    }

    private void initVideosId() {
        try {
            //Here we set up a string array of the thumbnail ID column we want to get back
            String [] columns={_ID};
            // Now we create the cursor pointing to the external thumbnail store
            cursor = managedQuery(contentUri,
                    columns, // Which columns to return
                    MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                    new String[] {"%OpenDashCam%"},       // WHERE clause selection arguments (none)
                    ORDER_BY); // Order-by clause (descending by date added)
            int count= cursor.getCount();
            // We now get the column index of the thumbnail id
            columnIndex = cursor.getColumnIndex(_ID);
            //initialize
            videosId = new int[count];
            //move position to first element
            cursor.moveToFirst();
            for(int i=0;i<count;i++)
            {
                int id = cursor.getInt(columnIndex);
                //
                videosId[i]= id;
                //
                cursor.moveToNext();
                //
            }
        } catch(Exception ex) {
            showToast(ex.getMessage().toString());
        }

    }

    private void setGalleryAdapter() {
        gallery.setAdapter(new VideoGalleryAdapter(context));
        gallery.setOnItemClickListener(_itemClickLis);
        flag = 1;
    }

    private AdapterView.OnItemClickListener _itemClickLis = new AdapterView.OnItemClickListener() {
        @SuppressWarnings({ "deprecation", "unused", "rawtypes" })
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // Now we want to actually get the data location of the file
            String [] columns={MEDIA_DATA};
            // We request our cursor again
            cursor = managedQuery(contentUri,
                    columns, // Which columns to return
                    MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                    new String[] {"%OpenDashCam%"},       // WHERE clause selection arguments (none)
                    ORDER_BY); // Order-by clause (ascending by name)
            // We want to get the column index for the data uri
            int count = cursor.getCount();
            //
            cursor.moveToFirst();
            //
            columnIndex = cursor.getColumnIndex(MEDIA_DATA);
            // Lets move to the selected item in the cursor
            cursor.moveToPosition(position);
            // And here we get the filename
            filename = cursor.getString(columnIndex);
            //*********** You can do anything when you know the file path :-)
            showToast(filename);

            openFile(Uri.fromFile(new File(filename)), "video/mp4");
        }
    };


    protected void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    private class VideoGalleryAdapter extends BaseAdapter {
        public VideoGalleryAdapter(Context c) {
            context = c;
        }
        public int getCount() {
            return videosId.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);;
            try {
                if(convertView!=null) {
                    imageView = (ImageView) convertView;
                }
                imageView.setImageBitmap(getImage(videosId[position]));
                imageView.setPadding(18, 48, 18, 0);
            }
            catch(Exception ex) {
                System.out.println("MainActivity:getView()-135: ex " + ex.getClass() +", "+ ex.getMessage());
            }
            return imageView;
        }

        // Create the thumbnail on the fly
        private Bitmap getImage(int id) {
            Bitmap thumb = MediaStore.Video.Thumbnails.getThumbnail(
                    getContentResolver(),
                    id, MediaStore.Video.Thumbnails.MINI_KIND, null);
            return thumb;
        }
    }

    private void openFile(Uri file, String mimeType) {
        Intent openFile = new Intent(Intent.ACTION_VIEW);
        openFile.setDataAndType(file, mimeType);
        openFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(openFile);
        } catch (ActivityNotFoundException e) {
            Log.i("DashCam", "Cannot open file.");
        }
    }
}
