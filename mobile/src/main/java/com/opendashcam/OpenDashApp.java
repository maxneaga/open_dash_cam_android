package com.opendashcam;

import android.app.Application;

import com.squareup.picasso.Picasso;

/**
 * Created by ashish on 8/23/17.
 */

public class OpenDashApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initPicasso();
    }

    private void initPicasso() {
        // create Picasso.Builder object
        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
        picassoBuilder.addRequestHandler(new VideoRequestHandler(this));
        Picasso.setSingletonInstance(picassoBuilder.build());
    }
}
