package com.tencent.qqimagecompare;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import java.lang.reflect.Method;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class QQImageScreenshortDetector {
    private int nc;
    private int nd;
    private Options ne;

    public QQImageScreenshortDetector(Context context) {
        int i;
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        if (VERSION.SDK_INT >= 17) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            defaultDisplay.getRealMetrics(displayMetrics);
            this.nc = displayMetrics.widthPixels;
            i = displayMetrics.heightPixels;
        } else if (VERSION.SDK_INT < 14) {
            this.nc = defaultDisplay.getWidth();
            i = defaultDisplay.getHeight();
        } else {
            try {
                Method method = Display.class.getMethod("getRawHeight", new Class[0]);
                this.nc = ((Integer) Display.class.getMethod("getRawWidth", new Class[0]).invoke(defaultDisplay, new Object[0])).intValue();
                this.nd = ((Integer) method.invoke(defaultDisplay, new Object[0])).intValue();
            } catch (Exception e) {
                this.nc = defaultDisplay.getWidth();
                this.nd = defaultDisplay.getHeight();
                d.c("Display Info", "Couldn't use reflection to get the real display metrics.");
            }
            this.ne = new Options();
            this.ne.inJustDecodeBounds = true;
        }
        this.nd = i;
        this.ne = new Options();
        this.ne.inJustDecodeBounds = true;
    }

    public boolean isScreenshort(String str) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Exception e) {
            e.printStackTrace();
            exifInterface = null;
        }
        if (exifInterface != null && exifInterface.getAttributeInt("Orientation", 0) == 0 && exifInterface.getAttributeInt("ImageWidth", 0) == 0) {
            BitmapFactory.decodeFile(str, this.ne);
            if (this.ne.outWidth == this.nc && this.ne.outHeight == this.nd) {
                return true;
            }
        }
        return false;
    }
}
