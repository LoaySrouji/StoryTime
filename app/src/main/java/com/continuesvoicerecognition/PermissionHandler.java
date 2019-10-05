package com.continuesvoicerecognition;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


class PermissionHandler {

    final static int RECORD_AUDIO = 1;

    static void askForPermission(final Activity activity) {
        if (!(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) ==
                PackageManager.PERMISSION_GRANTED))
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(activity, activity.getString(R.string.record_audio_is_required),
                        Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]
                        {Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
            }
    }

    static boolean checkPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

}
