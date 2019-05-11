package com.example.implicitintentdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PictureUtils {

    public static Bitmap getScaledBitmap(Context context, String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if (srcWidth > destWidth || srcHeight > destHeight) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;
            inSampleSize = (int) (widthScale > heightScale ? widthScale : heightScale);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        Bitmap scaleImage = BitmapFactory.decodeFile(path, options);

        return scaleImage;
    }

    public static Bitmap getScaledBitmap(Context context, String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(context, path, size.x, size.y);
    }

}
