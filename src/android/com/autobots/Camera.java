package com.autobots;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

public class Camera extends CordovaPlugin {

    private static final int TAKE_PICTURE = 0;

    private CallbackContext callbackContext;
    private Uri capturedPictureUri;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("getPicture")) {
            this.callbackContext = callbackContext;
            this.capturedPictureUri = Uri.fromFile(new File(getTempDirectoryPath(), System.currentTimeMillis() + ".jpg"));
            this.capturePicture();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                this.callbackContext.success(this.capturedPictureUri.toString());
            }
        }
    }

    private void capturePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.capturedPictureUri);
        PackageManager packageManager = this.cordova.getActivity().getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, TAKE_PICTURE);
        } else {
            this.callbackContext.error("You don't have a default camera.  Your device may not be CTS complaint.");
        }
    }

    private String getTempDirectoryPath() {
        File cache = null;

        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cache = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + cordova.getActivity().getPackageName() + "/cache/"
            );
        } else {
            // Use internal storage
            cache = cordova.getActivity().getCacheDir();
        }
        cache.mkdirs();
        return cache.getAbsolutePath();
    }
}