package tmsdkobf;

import android.graphics.Bitmap;
import com.tencent.qqimagecompare.QQImageBlur;
import com.tencent.qqimagecompare.QQImageFeatureHSV;
import com.tencent.qqimagecompare.QQImageLoader;
import tmsdk.common.utils.f;
import tmsdkobf.ry.a;

public class rx {
    private static boolean isLoaded = false;

    public static int a(QQImageFeatureHSV qQImageFeatureHSV, QQImageFeatureHSV qQImageFeatureHSV2) {
        if (!isLoaded) {
            loadLib();
        }
        try {
            return qQImageFeatureHSV.compare(qQImageFeatureHSV2);
        } catch (Throwable th) {
            f.g("ImageFeatureCenter", th);
            return 0;
        }
    }

    public static QQImageFeatureHSV a(a aVar) {
        return x(dz(aVar.mPath));
    }

    private static byte[] a(Bitmap bitmap) {
        byte[] bArr = null;
        try {
            QQImageFeatureHSV qQImageFeatureHSV = new QQImageFeatureHSV();
            qQImageFeatureHSV.init();
            qQImageFeatureHSV.getImageFeature(bitmap);
            bitmap.recycle();
            bArr = qQImageFeatureHSV.serialization();
            qQImageFeatureHSV.finish();
            return bArr;
        } catch (Throwable th) {
            f.g("ImageFeatureCenter", th);
            return bArr;
        }
    }

    public static double detectBlur(String str) {
        if (!isLoaded) {
            loadLib();
        }
        return new QQImageBlur().detectBlur(str);
    }

    private static byte[] dz(String str) {
        if (!isLoaded) {
            loadLib();
        }
        if (isLoaded) {
            Bitmap loadBitmap100x100FromFile;
            try {
                loadBitmap100x100FromFile = QQImageLoader.loadBitmap100x100FromFile(str);
            } catch (Throwable th) {
                f.g("ImageFeatureCenter", th);
                loadBitmap100x100FromFile = null;
            }
            if (loadBitmap100x100FromFile != null) {
                return a(loadBitmap100x100FromFile);
            }
        }
        return null;
    }

    private static synchronized void loadLib() {
        synchronized (rx.class) {
            try {
                System.loadLibrary("QQImageCompare-1.5-mfr");
                isLoaded = true;
            } catch (Throwable th) {
                f.g("ImageFeatureCenter", th);
            }
        }
        return;
    }

    public static QQImageFeatureHSV x(byte[] bArr) {
        if (!isLoaded) {
            loadLib();
        }
        if (bArr != null) {
            try {
                QQImageFeatureHSV qQImageFeatureHSV = new QQImageFeatureHSV();
                qQImageFeatureHSV.init();
                return qQImageFeatureHSV.unserialization(bArr) == 0 ? qQImageFeatureHSV : null;
            } catch (Throwable th) {
                f.g("ImageFeatureCenter", th);
            }
        }
        return null;
    }
}
