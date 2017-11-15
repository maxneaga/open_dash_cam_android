package com.opendashcam;

import android.app.Application;
import android.content.Context;

import com.squareup.picasso.Picasso;

/**
 * Created by ashish on 8/23/17.
 */

public class OpenDashApp extends Application {

    private static OpenDashApp sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        initPicasso();

        if (sApp == null) {
            sApp = this;
        }
    }

    /**
     * Get app context
     *
     * @return Context
     */
    public static Context getAppContext() {
        return sApp.getApplicationContext();
    }

    private void initPicasso() {
        // create Picasso.Builder object
        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
        picassoBuilder.addRequestHandler(new VideoRequestHandler(this));
        Picasso.setSingletonInstance(picassoBuilder.build());
    }
}
