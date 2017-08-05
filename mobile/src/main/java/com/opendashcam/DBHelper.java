package com.opendashcam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Max on 8/5/2017.
 */

public final class DBHelper extends SQLiteOpenHelper {
    // Make it a singleton
    private static DBHelper INSTANCE = null;

    private DBHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (INSTANCE == null) {
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            INSTANCE = new DBHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.StarredRecording.SQL_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since we have only one version, upgrade policy is to simply to discard the data
        // and start over
        db.execSQL(DBContract.StarredRecording.SQL_DROP_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
