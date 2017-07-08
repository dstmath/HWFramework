package com.tencent.qqimagecompare;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.ExifInterface;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class QQImageLoader {
    public static int DecodeJpegFileScale(String str, int i, QQImageBitmap qQImageBitmap) {
        return DecodeJpegFileScaleC1s1i1l(str, i, qQImageBitmap.mThisC);
    }

    private static native int DecodeJpegFileScaleC(String str, int i, Bitmap bitmap);

    private static native int DecodeJpegFileScaleC1s1i1l(String str, int i, long j);

    private static native int DecodeJpegFileScaleMemC(byte[] bArr, int i, Bitmap bitmap);

    private static native int DecodeJpegFileSubImageC(String str, int i, int i2, int i3, int i4, int i5, int i6, Bitmap bitmap);

    public static QQImageLoaderHeadInfo GetImageHeadInfo(String str) {
        QQImageLoaderHeadInfo qQImageLoaderHeadInfo = new QQImageLoaderHeadInfo();
        if (IsJpegFileC(str)) {
            GetJpegHeadInfoC(str, qQImageLoaderHeadInfo);
            qQImageLoaderHeadInfo.bJpeg = true;
        } else {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(str, options);
            qQImageLoaderHeadInfo.width = options.outWidth;
            qQImageLoaderHeadInfo.height = options.outHeight;
            qQImageLoaderHeadInfo.bJpeg = false;
        }
        return qQImageLoaderHeadInfo;
    }

    private static native int GetJpegHeadInfoC(String str, QQImageLoaderHeadInfo qQImageLoaderHeadInfo);

    private static native int GetJpegHeadInfoMemC(byte[] bArr, QQImageLoaderHeadInfo qQImageLoaderHeadInfo);

    static native boolean IsJpegFileC(String str);

    private static int a(int i, int i2) {
        return ((i + i2) - 1) / i2;
    }

    private static Bitmap a(String str, int i, int i2) {
        int i3 = 1;
        QQImageLoaderHeadInfo qQImageLoaderHeadInfo = new QQImageLoaderHeadInfo();
        if (GetJpegHeadInfoC(str, qQImageLoaderHeadInfo) == 0) {
            int i4;
            qQImageLoaderHeadInfo.bJpeg = true;
            int calculateInSampleSize_1_8_max = calculateInSampleSize_1_8_max(qQImageLoaderHeadInfo.width, qQImageLoaderHeadInfo.height, i, i2);
            int i5 = qQImageLoaderHeadInfo.width;
            int i6 = qQImageLoaderHeadInfo.height;
            switch (calculateInSampleSize_1_8_max) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    i3 = 0;
                    i4 = i5;
                    i5 = i6;
                    break;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    i4 = a(qQImageLoaderHeadInfo.width << 2, 8);
                    i5 = a(qQImageLoaderHeadInfo.height << 2, 8);
                    break;
                case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                    i3 = 2;
                    i4 = a(qQImageLoaderHeadInfo.width << 1, 8);
                    i5 = a(qQImageLoaderHeadInfo.height << 1, 8);
                    break;
                case RubbishType.SCAN_FLAG_APK /*8*/:
                    i3 = 3;
                    i4 = a(qQImageLoaderHeadInfo.width, 8);
                    i5 = a(qQImageLoaderHeadInfo.height, 8);
                    break;
                default:
                    i4 = i5;
                    i5 = i6;
                    break;
            }
            Bitmap createBitmap = Bitmap.createBitmap(i4, i5, Config.ARGB_8888);
            if (DecodeJpegFileScaleC(str, i3, createBitmap) == 0) {
                return createBitmap;
            }
            createBitmap.recycle();
        }
        return null;
    }

    private static Bitmap a(byte[] bArr, int i, int i2) {
        int i3 = 1;
        QQImageLoaderHeadInfo qQImageLoaderHeadInfo = new QQImageLoaderHeadInfo();
        if (GetJpegHeadInfoMemC(bArr, qQImageLoaderHeadInfo) == 0) {
            int i4;
            qQImageLoaderHeadInfo.bJpeg = true;
            int calculateInSampleSize_1_8_max = calculateInSampleSize_1_8_max(qQImageLoaderHeadInfo.width, qQImageLoaderHeadInfo.height, i, i2);
            int i5 = qQImageLoaderHeadInfo.width;
            int i6 = qQImageLoaderHeadInfo.height;
            switch (calculateInSampleSize_1_8_max) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    i3 = 0;
                    i4 = i5;
                    i5 = i6;
                    break;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    i4 = a(qQImageLoaderHeadInfo.width << 2, 8);
                    i5 = a(qQImageLoaderHeadInfo.height << 2, 8);
                    break;
                case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                    i3 = 2;
                    i4 = a(qQImageLoaderHeadInfo.width << 1, 8);
                    i5 = a(qQImageLoaderHeadInfo.height << 1, 8);
                    break;
                case RubbishType.SCAN_FLAG_APK /*8*/:
                    i3 = 3;
                    i4 = a(qQImageLoaderHeadInfo.width, 8);
                    i5 = a(qQImageLoaderHeadInfo.height, 8);
                    break;
                default:
                    i4 = i5;
                    i5 = i6;
                    break;
            }
            Bitmap createBitmap = Bitmap.createBitmap(i4, i5, Config.ARGB_8888);
            if (DecodeJpegFileScaleMemC(bArr, i3, createBitmap) == 0) {
                return createBitmap;
            }
            createBitmap.recycle();
        }
        return null;
    }

    private static QQImageLoaderHeadInfo ah(String str) {
        QQImageLoaderHeadInfo qQImageLoaderHeadInfo = new QQImageLoaderHeadInfo();
        return GetJpegHeadInfoC(str, qQImageLoaderHeadInfo) == 0 ? qQImageLoaderHeadInfo : null;
    }

    private static Bitmap ai(String str) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Exception e) {
            e.printStackTrace();
            exifInterface = null;
        }
        if (exifInterface != null) {
            byte[] thumbnail = exifInterface.getThumbnail();
            if (thumbnail != null) {
                return a(thumbnail, 100, 100);
            }
        }
        return null;
    }

    public static int calculateInSampleSize(Options options, int i, int i2) {
        int i3 = options.outHeight;
        int i4 = options.outWidth;
        int i5 = 1;
        if (i3 > i2 || i4 > i) {
            i3 /= 2;
            i4 /= 2;
            while (i3 / i5 > i2 && i4 / i5 > i) {
                i5 *= 2;
            }
        }
        return i5;
    }

    public static int calculateInSampleSize_1_8_max(int i, int i2, int i3, int i4) {
        int i5 = 1;
        if (i > i4 || i2 > i3) {
            int i6 = i / 2;
            int i7 = i2 / 2;
            while (i6 / i5 > i4 && i7 / i5 > i3) {
                i5 *= 2;
                if (i5 == 8) {
                    break;
                }
            }
        }
        return i5;
    }

    public static int calculateInSampleSize_1_8_max(Options options, int i, int i2) {
        int i3 = options.outHeight;
        int i4 = options.outWidth;
        int i5 = 1;
        if (i3 > i2 || i4 > i) {
            i3 /= 2;
            i4 /= 2;
            while (i3 / i5 > i2 && i4 / i5 > i) {
                i5 *= 2;
                if (i5 == 8) {
                    break;
                }
            }
        }
        return i5;
    }

    public static Bitmap decodeSampledBitmapFromFile(String str, int i, int i2) {
        Bitmap bitmap = null;
        Options options = new Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(str, options);
            options.inSampleSize = calculateInSampleSize(options, i, i2);
            options.inJustDecodeBounds = false;
            long currentTimeMillis = System.currentTimeMillis();
            bitmap = BitmapFactory.decodeFile(str, options);
            d.d("QQImageCompare", "decode file time: " + (System.currentTimeMillis() - currentTimeMillis));
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private static QQImageLoaderHeadInfo e(byte[] bArr) {
        QQImageLoaderHeadInfo qQImageLoaderHeadInfo = new QQImageLoaderHeadInfo();
        return GetJpegHeadInfoMemC(bArr, qQImageLoaderHeadInfo) == 0 ? qQImageLoaderHeadInfo : null;
    }

    public static Bitmap loadBitmap100x100FromFile(String str) {
        Bitmap ai;
        Bitmap bitmap = null;
        long currentTimeMillis = System.currentTimeMillis();
        if (IsJpegFileC(str)) {
            ai = ai(str);
            if (ai == null) {
                ai = a(str, 100, 100);
            }
        } else {
            ai = decodeSampledBitmapFromFile(str, 100, 100);
        }
        if (ai != null) {
            if (ai.getWidth() == 100 && ai.getHeight() == 100) {
                bitmap = ai;
            } else {
                Bitmap createBitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                Rect rect = new Rect();
                rect.set(0, 0, ai.getWidth(), ai.getHeight());
                Rect rect2 = new Rect();
                rect2.set(0, 0, 100, 100);
                canvas.drawBitmap(ai, rect, rect2, null);
                ai.recycle();
                bitmap = createBitmap;
            }
        }
        d.d("QQImageCompare", "loadBitmap100x100FromFile t = " + (System.currentTimeMillis() - currentTimeMillis));
        return bitmap;
    }

    public static Bitmap loadBitmapFromFile(String str, int i, int i2) {
        if (!IsJpegFileC(str)) {
            return decodeSampledBitmapFromFile(str, i, i2);
        }
        ExifInterface exifInterface;
        Bitmap decodeSampledBitmapFromFile;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Exception e) {
            e.printStackTrace();
            exifInterface = null;
        }
        if (exifInterface == null) {
            decodeSampledBitmapFromFile = decodeSampledBitmapFromFile(str, i, i2);
        } else {
            byte[] thumbnail = exifInterface.getThumbnail();
            if (thumbnail == null) {
                QQImageLoaderHeadInfo ah = ah(str);
                decodeSampledBitmapFromFile = (i <= (ah.width >> 3) && i2 <= (ah.height >> 3)) ? a(str, i, i2) : decodeSampledBitmapFromFile(str, i, i2);
            } else {
                QQImageLoaderHeadInfo e2 = e(thumbnail);
                decodeSampledBitmapFromFile = e2 == null ? decodeSampledBitmapFromFile(str, i, i2) : (i <= e2.width && i2 <= e2.height) ? a(thumbnail, i, i2) : (i <= (e2.width >> 3) && i2 <= (e2.height >> 3)) ? a(str, i, i2) : decodeSampledBitmapFromFile(str, i, i2);
            }
        }
        return decodeSampledBitmapFromFile == null ? decodeSampledBitmapFromFile(str, i, i2) : decodeSampledBitmapFromFile;
    }

    public static Bitmap loadBitmapSubImage(String str, int i, int i2, int i3, int i4, QQImageLoaderHeadInfo qQImageLoaderHeadInfo) {
        Bitmap createBitmap;
        if (qQImageLoaderHeadInfo.bJpeg) {
            createBitmap = Bitmap.createBitmap(i3, i4, Config.ARGB_8888);
            if (DecodeJpegFileSubImageC(str, i, i2, i3, i4, qQImageLoaderHeadInfo.mMCUWidth, qQImageLoaderHeadInfo.mMCUHeight, createBitmap) == 0) {
                return createBitmap;
            }
            createBitmap.recycle();
        } else {
            BitmapRegionDecoder newInstance;
            try {
                newInstance = BitmapRegionDecoder.newInstance(str, true);
            } catch (Exception e) {
                e.printStackTrace();
                newInstance = null;
            }
            if (newInstance != null) {
                Rect rect = new Rect();
                rect.set(i, i2, i + i3, i2 + i4);
                createBitmap = newInstance.decodeRegion(rect, null);
                newInstance.recycle();
                return createBitmap;
            }
        }
        return null;
    }
}
