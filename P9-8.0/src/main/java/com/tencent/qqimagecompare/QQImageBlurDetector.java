package com.tencent.qqimagecompare;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.tencent.qqimagecompare.QQImageBitmap.eColorFormat;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;

public class QQImageBlurDetector extends QQImageNativeObject {
    private native int Detect1j1bmpC(long j, Bitmap bitmap);

    private native int Detect1j1jC(long j, long j2);

    public static int aliginInt(int -l_2_I, int i) {
        int i2 = -l_2_I % i;
        return i2 == 0 ? -l_2_I : -l_2_I + (i - i2);
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

    /* JADX WARNING: Missing block: B:37:0x0172, code:
            if (r35[0] >= 0) goto L_0x0174;
     */
    /* JADX WARNING: Missing block: B:44:0x019f, code:
            if (r20 >= 2) goto L_0x01a1;
     */
    /* JADX WARNING: Missing block: B:45:0x01a1, code:
            r28 = -1;
     */
    /* JADX WARNING: Missing block: B:56:0x025e, code:
            if (r35[0] >= 0) goto L_0x0260;
     */
    /* JADX WARNING: Missing block: B:63:0x02ab, code:
            if (r26 >= 2) goto L_0x02ad;
     */
    /* JADX WARNING: Missing block: B:64:0x02ad, code:
            r28 = -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int detect(String str) {
        f.f("QQImageBlurDetector", str);
        int i = 0;
        QQImageLoaderHeadInfo GetImageHeadInfo = QQImageLoader.GetImageHeadInfo(str);
        int i2 = GetImageHeadInfo.width;
        int i3 = GetImageHeadInfo.height;
        int max = Math.max(i2, i3);
        int min = Math.min(i2, i3);
        if (min >= 200) {
            int i4;
            if (max < 1800) {
                i4 = 0;
            } else if (max >= 1800 && max < 3600) {
                i4 = 1;
                min >>= 1;
            } else if (max >= 3600 && max < 7200) {
                i4 = 2;
                min >>= 2;
            } else {
                i4 = 3;
                min >>= 3;
            }
            if (min >= 200) {
                int[] iArr = new int[3];
                int width;
                int i5;
                int i6;
                int i7;
                if (GetImageHeadInfo.bJpeg) {
                    QQImageBitmap qQImageBitmap = new QQImageBitmap();
                    if (QQImageLoader.DecodeJpegFileScale(str, i4, qQImageBitmap) == 0) {
                        width = qQImageBitmap.getWidth();
                        int height = (qQImageBitmap.getHeight() >> 1) - 100;
                        int i8 = (width / 3) - 100;
                        i5 = (width >> 1) - 100;
                        i6 = ((width << 1) / 3) - 100;
                        QQImageBitmap qQImageBitmap2 = new QQImageBitmap(eColorFormat.QQIMAGE_CLR_RGBA8888, SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200, 4);
                        if (qQImageBitmap2 != null) {
                            qQImageBitmap.clip(qQImageBitmap2, i8, height);
                            iArr[0] = detect(qQImageBitmap2);
                            qQImageBitmap.clip(qQImageBitmap2, i5, height);
                            iArr[1] = detect(qQImageBitmap2);
                            if (iArr[0] != iArr[1]) {
                                qQImageBitmap.clip(qQImageBitmap2, i6, height);
                                iArr[2] = detect(qQImageBitmap2);
                                i7 = 0;
                                for (int i9 = 0; i9 < 3; i9++) {
                                    if (iArr[i9] < 0) {
                                        i7++;
                                    }
                                }
                            }
                            i = 1;
                            qQImageBitmap2.delete();
                        }
                        qQImageBitmap.delete();
                    }
                } else {
                    int i10 = i2;
                    width = i3;
                    switch (i4) {
                        case 1:
                            i10 = i2 >> 1;
                            width = i3 >> 1;
                            break;
                        case 2:
                            i10 = i2 >> 2;
                            width = i3 >> 2;
                            break;
                        case 3:
                            i10 = i2 >> 3;
                            width = i3 >> 3;
                            break;
                    }
                    Bitmap decodeSampledBitmapFromFile = QQImageLoader.decodeSampledBitmapFromFile(str, i10, width);
                    if (decodeSampledBitmapFromFile != null) {
                        int width2 = decodeSampledBitmapFromFile.getWidth();
                        i5 = (decodeSampledBitmapFromFile.getHeight() >> 1) - 100;
                        i6 = (width2 / 3) - 100;
                        int i11 = (width2 >> 1) - 100;
                        i7 = ((width2 << 1) / 3) - 100;
                        Bitmap createBitmap = Bitmap.createBitmap(SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200, Config.ARGB_8888);
                        if (createBitmap != null) {
                            Canvas canvas = new Canvas(createBitmap);
                            Rect rect = new Rect();
                            rect.set(i6, i5, i6 + SmsCheckResult.ESCT_200, i5 + SmsCheckResult.ESCT_200);
                            Rect rect2 = new Rect();
                            rect2.set(0, 0, SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200);
                            canvas.drawBitmap(decodeSampledBitmapFromFile, rect, rect2, null);
                            iArr[0] = detect(createBitmap);
                            rect.set(i11, i5, i11 + SmsCheckResult.ESCT_200, i5 + SmsCheckResult.ESCT_200);
                            canvas.drawBitmap(decodeSampledBitmapFromFile, rect, rect2, null);
                            iArr[1] = detect(createBitmap);
                            if (iArr[0] != iArr[1]) {
                                rect.set(i7, i5, i7 + SmsCheckResult.ESCT_200, i5 + SmsCheckResult.ESCT_200);
                                canvas.drawBitmap(decodeSampledBitmapFromFile, rect, rect2, null);
                                iArr[2] = detect(createBitmap);
                                int i12 = 0;
                                for (int i13 = 0; i13 < 3; i13++) {
                                    if (iArr[i13] < 0) {
                                        i12++;
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
        f.f("QQImageBlurDetector", "nRet = " + i);
        f.f("QQImageBlurDetector", "detect out");
        return i;
    }
}
