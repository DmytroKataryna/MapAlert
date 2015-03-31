package com.example.dmytro.mapalert.DBUtils;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;

public class ImageUtil {


    public static Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;
            // Find the correct scale value. It should be the power of 2
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    public static File saveToInternalStorage(Context context, Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/MapAlert/app_data/app_imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        int rand = (int) (Math.random() * 100);
        File myPath = new File(directory, rand + "image.jpg");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myPath;
    }


    public static String getAbsolutePath(Activity activity, Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        // Get the cursor
        Cursor cursor = activity.getContentResolver().query(uri,
                filePathColumn, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        } else
            return null;
    }

//
//    private void loadImageFromStorage(Context context, final ImageView imageView, String path) {
//        try {
//            File f = new File(path);
//            final Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            imageView.post(new Runnable() {
//                @Override
//                public void run() {
//                    imageView.setImageBitmap(b);
//                }
//            });
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

//    public static byte[] bitmapToByteArray(Bitmap bm) {
//        //convert Bitmap to byte array
//        ByteArrayOutputStream blob = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
//        return blob.toByteArray();
//    }

//    public static Bitmap byteArrayToBitmap(byte[] array) {
//        //convert  byte array to Bitmap
//        return BitmapFactory.decodeByteArray(array, 0, array.length);
//    }

}
