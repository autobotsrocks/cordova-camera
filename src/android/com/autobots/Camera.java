package com.autobots;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.soundcloud.android.crop.Crop;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

public class Camera extends CordovaPlugin {

    private static final int REQUEST_CAPTURE = 0;

    private CallbackContext callbackContext;
    private Uri capturedPictureUri;
    private Uri cropedPictureUri;
    private boolean allowCrop;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("getPicture")) {
            long currentTimeMillis = System.currentTimeMillis();
            String tempDirectoryPath = getTempDirectoryPath();
            this.callbackContext = callbackContext;
            this.capturedPictureUri = Uri.fromFile(new File(tempDirectoryPath, currentTimeMillis + ".jpg"));
            this.allowCrop = args.getBoolean(0);
            if (this.allowCrop) {
                this.cropedPictureUri = Uri.fromFile(new File(tempDirectoryPath, currentTimeMillis + ".crop.jpg"));
            }
            this.capturePicture();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            this.callbackContext.error("User cancelled");
        } else {
            if (requestCode == REQUEST_CAPTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (this.allowCrop) {
                        this.cordova.setActivityResultCallback(this);
                        Crop.of(this.capturedPictureUri, this.cropedPictureUri).asSquare().start(this.cordova.getActivity());
                    } else {
                        this.callbackContext.success(this.capturedPictureUri.toString());
                    }
                } else {
                    this.callbackContext.error("Can't capture picture");
                }
            } else if (requestCode == Crop.REQUEST_CROP) {
                if (resultCode == Activity.RESULT_OK) {
                    File capturedPicture = new File(this.capturedPictureUri.getPath());
                    if (capturedPicture.exists()) {
                        capturedPicture.delete();
                    }
                    this.callbackContext.success(Crop.getOutput(intent).toString());
                } else if (resultCode == Crop.RESULT_ERROR) {
                    this.callbackContext.error("Can't crop picture");
                }
            }
        }
    }

    private void capturePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.capturedPictureUri);
        PackageManager packageManager = this.cordova.getActivity().getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CAPTURE);
        } else {
            this.callbackContext.error("You don't have a default camera.  Your device may not be CTS complaint");
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