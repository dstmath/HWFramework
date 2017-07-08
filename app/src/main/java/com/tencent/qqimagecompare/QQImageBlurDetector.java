package com.tencent.qqimagecompare;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.tencent.qqimagecompare.QQImageBitmap.eColorFormat;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class QQImageBlurDetector extends QQImageNativeObject {
    private native int Detect1j1bmpC(long j, Bitmap bitmap);

    private native int Detect1j1jC(long j, long j2);

    public static int aliginInt(int i, int i2) {
        int i3 = i % i2;
        return i3 == 0 ? i : i + (i2 - i3);
    }

    public static int pad_n(int i, int i2) {
        return ((i + i2) - 1) & ((i2 - 1) ^ -1);
    }

    protected native long createNativeObject();

    protected native void destroyNativeObject(long j);

    public int detect(Bitmap bitmap) {
        return Detect1j1bmpC(this.mThisC, bitmap);
    }

    public int detect(QQImageBitmap qQImageBitmap) {
        return Detect1j1jC(this.mThisC, qQImageBitmap.mThisC);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int detect(String str) {
        d.d("QQImageBlurDetector", str);
        int i = 0;
        QQImageLoaderHeadInfo GetImageHeadInfo = QQImageLoader.GetImageHeadInfo(str);
        int i2 = GetImageHeadInfo.width;
        int i3 = GetImageHeadInfo.height;
        int max = Math.max(i2, i3);
        int min = Math.min(i2, i3);
        if (min >= SmsCheckResult.ESCT_200) {
            if (max < 1800) {
                max = 0;
            } else if (max >= 1800 && max < 3600) {
                max = 1;
                min >>= 1;
            } else if (max >= 3600 && max < 7200) {
                max = 2;
                min >>= 2;
            } else {
                max = 3;
                min >>= 3;
            }
            if (min >= SmsCheckResult.ESCT_200) {
                int[] iArr = new int[3];
                int i4;
                int i5;
                if (GetImageHeadInfo.bJpeg) {
                    QQImageBitmap qQImageBitmap = new QQImageBitmap();
                    if (QQImageLoader.DecodeJpegFileScale(str, max, qQImageBitmap) == 0) {
                        i2 = qQImageBitmap.getWidth();
                        max = (qQImageBitmap.getHeight() >> 1) - 100;
                        i4 = (i2 / 3) - 100;
                        i5 = (i2 >> 1) - 100;
                        i2 = ((i2 << 1) / 3) - 100;
                        QQImageBitmap qQImageBitmap2 = new QQImageBitmap(eColorFormat.QQIMAGE_CLR_RGBA8888, SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200, 4);
                        if (qQImageBitmap2 != null) {
                            qQImageBitmap.clip(qQImageBitmap2, i4, max);
                            iArr[0] = detect(qQImageBitmap2);
                            qQImageBitmap.clip(qQImageBitmap2, i5, max);
                            iArr[1] = detect(qQImageBitmap2);
                            if (iArr[0] != iArr[1]) {
                                qQImageBitmap.clip(qQImageBitmap2, i2, max);
                                iArr[2] = detect(qQImageBitmap2);
                                i = 0;
                                for (i2 = 0; i2 < 3; i2++) {
                                    if (iArr[i2] < 0) {
                                        i++;
                                    }
                                }
                            }
                            i = 1;
                            qQImageBitmap2.delete();
                        }
                        qQImageBitmap.delete();
                    }
                } else {
                    switch (max) {
                        case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                            i2 >>= 1;
                            i3 >>= 1;
                            break;
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            i2 >>= 2;
                            i3 >>= 2;
                            break;
                        case FileInfo.TYPE_BIGFILE /*3*/:
                            i2 >>= 3;
                            i3 >>= 3;
                            break;
                    }
                    Bitmap decodeSampledBitmapFromFile = QQImageLoader.decodeSampledBitmapFromFile(str, i2, i3);
                    if (decodeSampledBitmapFromFile != null) {
                        i2 = decodeSampledBitmapFromFile.getWidth();
                        max = (decodeSampledBitmapFromFile.getHeight() >> 1) - 100;
                        i4 = (i2 / 3) - 100;
                        i5 = (i2 >> 1) - 100;
                        i2 = ((i2 << 1) / 3) - 100;
                        Bitmap createBitmap = Bitmap.createBitmap(SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200, Config.ARGB_8888);
                        if (createBitmap != null) {
                            Canvas canvas = new Canvas(createBitmap);
                            Rect rect = new Rect();
                            rect.set(i4, max, i4 + SmsCheckResult.ESCT_200, max + SmsCheckResult.ESCT_200);
                            Rect rect2 = new Rect();
                            rect2.set(0, 0, SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200);
                            canvas.drawBitmap(decodeSampledBitmapFromFile, rect, rect2, null);
                            iArr[0] = detect(createBitmap);
                            rect.set(i5, max, i5 + SmsCheckResult.ESCT_200, max + SmsCheckResult.ESCT_200);
                            canvas.drawBitmap(decodeSampledBitmapFromFile, rect, rect2, null);
                            iArr[1] = detect(createBitmap);
                            if (iArr[0] != iArr[1]) {
                                rect.set(i2, max, i2 + SmsCheckResult.ESCT_200, max + SmsCheckResult.ESCT_200);
                                canvas.drawBitmap(decodeSampledBitmapFromFile, rect, rect2, null);
                                iArr[2] = detect(createBitmap);
                                i = 0;
                                for (i2 = 0; i2 < 3; i2++) {
                                    if (iArr[i2] < 0) {
                                        i++;
                                    }
                                }
                            }
                            i = 1;
                            createBitmap.recycle();
                        }
                        decodeSampledBitmapFromFile.recycle();
                    }
                }
            }
        }
        d.d("QQImageBlurDetector", "nRet = " + i);
        d.d("QQImageBlurDetector", "detect out");
        return i;
    }
}
