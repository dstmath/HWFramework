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
import tmsdk.common.utils.f;

public class QQImageScreenshortDetector {
    private int nL;
    private int nM;
    private Options nN;

    public QQImageScreenshortDetector(Context context) {
        int i;
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        if (VERSION.SDK_INT >= 17) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            defaultDisplay.getRealMetrics(displayMetrics);
            this.nL = displayMetrics.widthPixels;
            i = displayMetrics.heightPixels;
        } else if (VERSION.SDK_INT < 14) {
            this.nL = defaultDisplay.getWidth();
            i = defaultDisplay.getHeight();
        } else {
            try {
                Method method = Display.class.getMethod("getRawHeight", new Class[0]);
                this.nL = ((Integer) Display.class.getMethod("getRawWidth", new Class[0]).invoke(defaultDisplay, new Object[0])).intValue();
                this.nM = ((Integer) method.invoke(defaultDisplay, new Object[0])).intValue();
            } catch (Exception e) {
                this.nL = defaultDisplay.getWidth();
                this.nM = defaultDisplay.getHeight();
                f.e("Display Info", "Couldn't use reflection to get the real display metrics.");
            }
            this.nN = new Options();
            this.nN.inJustDecodeBounds = true;
        }
        this.nM = i;
        this.nN = new Options();
        this.nN.inJustDecodeBounds = true;
    }

    public boolean isScreenshort(String str) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (exifInterface == null || exifInterface.getAttributeInt("Orientation", 0) != 0 || exifInterface.getAttributeInt("ImageWidth", 0) != 0) {
            return false;
        }
        BitmapFactory.decodeFile(str, this.nN);
        return this.nN.outWidth == this.nL && this.nN.outHeight == this.nM;
    }
}
