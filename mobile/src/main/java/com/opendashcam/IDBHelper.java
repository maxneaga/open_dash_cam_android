package com.opendashcam;

import com.opendashcam.models.Recording;

import java.util.ArrayList;

/**
 * Interface for DBHelper
 * <p>
 * Created by Dmitriy V. Chernysh on 20.11.17.
 * <p>
 * https://fb.com/mobiledevpro/
 * https://github.com/dmitriy-chernysh
 * #MobileDevPro
 */

interface IDBHelper {

    ArrayList<Recording> selectAllRecordingsList();

    boolean insertNewRecording(Recording recording);

    boolean deleteRecording(Recording recording);

    boolean deleteAllRecordings();

    boolean updateStar(Recording recording);

    boolean isRecordingStarred(Recording recording);

}
