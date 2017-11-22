package com.opendashcam;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.opendashcam.models.Recording;

/**
 * DB contract for recordings
 * <p>
 * <p>
 * Changed by: Dmitriy Chernysh
 * #MobileDevPro
 */
class DBRecordingsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBRecordingsContract() {
    }

    static void onCreate(SQLiteDatabase db) {
        db.execSQL(RecordingsTable.SQL_CREATE_TABLE);
        db.execSQL(StarredRecordingTable.SQL_CREATE_TABLE);
    }

    static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2 && newVersion > 2) {
            //create a new table for recordings list
            db.execSQL(RecordingsTable.SQL_CREATE_TABLE);
        } else {
            db.execSQL(RecordingsTable.SQL_DROP_TABLE);
            db.execSQL(StarredRecordingTable.SQL_DROP_TABLE);
            onCreate(db);
        }
    }

    /**
     * Query all recordings
     *
     * @param db SQLiteDatabase
     * @return Cursor
     */
    static Cursor queryAllRecordings(SQLiteDatabase db) {
        return db.query(
                RecordingsTable.TABLE_NAME,
                RecordingsTable.QUERY_PROJECTION,
                null,
                null,
                null,
                null,
                RecordingsTable._ID + " DESC",
                null
        );
    }

    /**
     * Delete all recordings
     *
     * @param db SQLiteDatabase
     * @return rue - deleted successfully
     */
    static boolean deleteAllRecordings(SQLiteDatabase db) {
        //delete recordings
        int result = db.delete(RecordingsTable.TABLE_NAME, null, null);
        //delete stars
        if (result > 0) {
            db.delete(StarredRecordingTable.TABLE_NAME, null, null);
        }

        return result > 0;
    }

    /**
     * Delete all recordings
     *
     * @param db SQLiteDatabase
     * @return rue - deleted successfully
     */
    static boolean deleteRecording(SQLiteDatabase db, Recording recording) {
        //delete recordings
        int result = db.delete(
                RecordingsTable.TABLE_NAME,
                RecordingsTable.COLUMN_FILE_NAME + " LIKE ?",
                new String[]{recording.getFileName()}
        );
        //delete stars if exist
        if (result > 0) {
            db.delete(
                    StarredRecordingTable.TABLE_NAME,
                    StarredRecordingTable.COLUMN_NAME_FILE + " LIKE ?",
                    new String[]{recording.getFileName()}
            );
        }

        return result > 0;
    }

    /**
     * Insert new entry
     *
     * @param db        SQLiteDatabase
     * @param recording Recording
     * @return True -  inserted successfully
     */
    static boolean insertRecording(SQLiteDatabase db, Recording recording) {
        if (recording == null) return false;
        long insertedRowId = -1;

        ContentValues cv = new ContentValues();
        cv.put(RecordingsTable.COLUMN_FILE_PATH, recording.getFilePath());
        cv.put(RecordingsTable.COLUMN_FILE_NAME, recording.getFileName());

        db.beginTransaction();
        try {
            insertedRowId = db.insert(
                    RecordingsTable.TABLE_NAME,
                    null,
                    cv
            );
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(DBRecordingsContract.class.getSimpleName(), "insertNewRecording: EXCEPTION - " + e.getLocalizedMessage(), e);
        } finally {
            db.endTransaction();
        }

        return insertedRowId > -1;
    }

    /**
     * Star recording
     *
     * @param db        SQLiteDatabase
     * @param recording Recording
     * @return True - starred successfully
     */
    static boolean insertStar(SQLiteDatabase db, Recording recording) {
        if (recording == null) return false;
        long insertedRowId = -1;

        ContentValues cv = new ContentValues();
        cv.put(StarredRecordingTable.COLUMN_NAME_FILE, recording.getFileName());

        db.beginTransaction();
        try {
            insertedRowId = db.insert(
                    StarredRecordingTable.TABLE_NAME,
                    null,
                    cv
            );
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(DBRecordingsContract.class.getSimpleName(), "insertStar: EXCEPTION - " + e.getLocalizedMessage(), e);
        } finally {
            db.endTransaction();
        }

        return insertedRowId > -1;
    }

    /**
     * Delete star
     *
     * @param db        SQLiteDatabase
     * @param recording Recording
     * @return True - deleted successfully
     */
    static boolean deleteStar(SQLiteDatabase db, Recording recording) {
        long result = 0;
        db.beginTransaction();
        try {
            result = db.delete(
                    StarredRecordingTable.TABLE_NAME,
                    StarredRecordingTable.COLUMN_NAME_FILE + " LIKE ?",
                    new String[]{recording.getFileName()}
            );

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(DBRecordingsContract.class.getSimpleName(), "deleteStar: exception - " + e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return result > 0;
    }

    /**
     * Check if recording exists
     *
     * @param db        SQLiteDatabase
     * @param recording Recording
     * @return True - exists
     */
    static boolean isRecordingExists(SQLiteDatabase db, Recording recording) {
        if (recording == null) return false;
        Cursor cursor;
        int rowCount = 0;

        cursor = db.query(
                RecordingsTable.TABLE_NAME,
                new String[]{RecordingsTable._ID},
                "CAST (" + RecordingsTable._ID + " AS TEXT) = ?",
                new String[]{String.valueOf(recording.getId())},
                null,
                null,
                null,
                null
        );

        try {
            rowCount = cursor.getCount();
        } catch (Exception e) {
            Log.e(DBRecordingsContract.class.getSimpleName(), "isRecordingExists: EXCEPTION - " + e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return rowCount > 0;
    }

    /**
     * Check is recording is starred
     *
     * @param db        SQLiteDatabase
     * @param recording Recording
     * @return True - starred
     */
    static boolean isRecordingStarred(SQLiteDatabase db, Recording recording) {
        if (recording == null) return false;
        Cursor cursor;
        int rowCount = 0;

        cursor = db.query(
                StarredRecordingTable.TABLE_NAME,
                new String[]{StarredRecordingTable._ID},
                StarredRecordingTable.COLUMN_NAME_FILE + " LIKE ?",
                new String[]{recording.getFileName()},
                null,
                null,
                null,
                null
        );

        try {
            rowCount = cursor.getCount();
        } catch (Exception e) {
            Log.e(DBRecordingsContract.class.getSimpleName(), "isRecordingStarred: EXCEPTION - " + e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return rowCount > 0;
    }


    static Recording getRecordingFromCursor(Cursor cursor) {
        if (cursor == null) return null;
        return new Recording(
                cursor.getInt(cursor.getColumnIndex(RecordingsTable._ID)),
                cursor.getString(cursor.getColumnIndex(RecordingsTable.COLUMN_FILE_PATH))
        );
    }

    /**
     * Table for starred recordings
     */
    private static class StarredRecordingTable implements BaseColumns {
        private static final String TABLE_NAME = "starred_recording";
        private static final String COLUMN_NAME_FILE = "file";

        static final String SQL_CREATE_TABLE = "create table IF NOT EXISTS "
                + TABLE_NAME
                + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME_FILE + " TEXT"
                + ");";

        static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Table for recordings list
     */
    private static class RecordingsTable implements BaseColumns {
        private static final String TABLE_NAME = "recording";
        private static final String COLUMN_FILE_PATH = "file_path";
        private static final String COLUMN_FILE_NAME = "file_name";

        static final String SQL_CREATE_TABLE = "create table IF NOT EXISTS "
                + TABLE_NAME
                + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_FILE_NAME + " TEXT"
                + ");";

        static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        private static final String[] QUERY_PROJECTION = {
                _ID,
                COLUMN_FILE_PATH,
                COLUMN_FILE_NAME
        };
    }
}
