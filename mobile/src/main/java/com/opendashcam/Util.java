package com.opendashcam;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

/**
 * Global utility methods
 */

final class Util {
    /**
     * Displays toast message of LONG length
     * @param context   Application context
     * @param msg       Message to display
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Display a 9-seconds-long toast.
     * Inspired by https://stackoverflow.com/a/7173248
     * @param context   Application context
     * @param msg       Message to display
     */
    public static void showToastLong(Context context, String msg) {
        final Toast tag = Toast.makeText(context, msg,Toast.LENGTH_SHORT);

        tag.show();

        new CountDownTimer(9000, 1000)
        {

            public void onTick(long millisUntilFinished) {tag.show();}
            public void onFinish() {tag.show();}

        }.start();
    }
}
