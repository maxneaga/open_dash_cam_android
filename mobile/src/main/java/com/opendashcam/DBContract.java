package com.opendashcam;

import android.provider.BaseColumns;

/**
 * Created by Max on 8/5/2017.
 */

public final class DBContract {
    public static final String DATABASE_NAME = "OpenDashCam.db";
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBContract() {}

    /* Inner class that defines the table contents */
    public static class StarredRecording implements BaseColumns {
        public static final String TABLE_NAME = "starred_recording";
        public static final String COLUMN_NAME_FILE = "file";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + StarredRecording.TABLE_NAME + " (" +
                        StarredRecording._ID + " INTEGER PRIMARY KEY," +
                        StarredRecording.COLUMN_NAME_FILE + " TEXT)";

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + StarredRecording.TABLE_NAME;
    }
}
