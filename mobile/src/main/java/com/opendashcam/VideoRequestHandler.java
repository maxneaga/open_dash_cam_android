package com.opendashcam;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

/**
 * Created by ashish on 8/23/17.
 */

public class VideoRequestHandler extends RequestHandler {
    public static final Uri THUMBNAIL_IDENTIFIER_URI = Uri.withAppendedPath(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, "thumbnail");
    private final Context mContext;

    public VideoRequestHandler(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        if (null == data.uri) {
            return false;
        }
        return data.uri.toString().startsWith(THUMBNAIL_IDENTIFIER_URI.toString());
    }

    @Override
    public Result load(Request data, int arg1) throws IOException {
        int id = Integer.parseInt(data.uri.getLastPathSegment());
        Bitmap bm = MediaStore.Video.Thumbnails.getThumbnail(
                mContext.getContentResolver(),
                id, MediaStore.Video.Thumbnails.MINI_KIND, null);

        return new Result(bm, Picasso.LoadedFrom.DISK);
    }
}
