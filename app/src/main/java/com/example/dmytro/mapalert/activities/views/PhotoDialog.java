package com.example.dmytro.mapalert.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dmytro.mapalert.R;

import java.io.File;

/////----------------------------------Photo Dialog -------------------------------
public class PhotoDialog {

    public Activity activity;

    private static final int SELECT_FILE = 1111;
    private static final int REQUEST_CAMERA = 9999;
    private static final int FIRST = 0, SECOND = 1, THIRD = 2;

    public PhotoDialog(Activity activity) {
        this.activity = activity;
    }

    public void createPhotoDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        new MaterialDialog.Builder(activity)
                .title("Add Photo!")
                .titleGravity(GravityEnum.CENTER)
                .itemColorRes(R.color.dialog_blue)
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                        switch (which) {
                            case FIRST:
                                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                activity.startActivityForResult(i, REQUEST_CAMERA);
                                break;
                            case SECOND:
                                Intent intent = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                activity.startActivityForResult(
                                        Intent.createChooser(intent, "Select File"),
                                        SELECT_FILE);
                                break;
                            case THIRD:
                                materialDialog.dismiss();
                                break;
                        }
                    }
                }).show();
    }
}
