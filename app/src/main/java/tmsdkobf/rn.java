package tmsdkobf;

import android.graphics.Bitmap;
import com.tencent.qqimagecompare.QQImageFeatureHSV;
import com.tencent.qqimagecompare.QQImageLoader;
import tmsdk.common.utils.d;
import tmsdkobf.ro.a;

/* compiled from: Unknown */
public class rn {
    private static boolean isLoaded;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rn.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rn.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rn.<clinit>():void");
    }

    public static QQImageFeatureHSV A(byte[] bArr) {
        if (!isLoaded) {
            loadLib();
        }
        if (bArr != null) {
            try {
                QQImageFeatureHSV qQImageFeatureHSV = new QQImageFeatureHSV();
                qQImageFeatureHSV.init();
                qQImageFeatureHSV.unserialization(bArr);
                return qQImageFeatureHSV;
            } catch (Throwable th) {
                d.f("ImageFeatureCenter", th);
            }
        }
        return null;
    }

    public static int a(QQImageFeatureHSV qQImageFeatureHSV, QQImageFeatureHSV qQImageFeatureHSV2) {
        if (!isLoaded) {
            loadLib();
        }
        try {
            return qQImageFeatureHSV.compare(qQImageFeatureHSV2);
        } catch (Throwable th) {
            d.f("ImageFeatureCenter", th);
            return 0;
        }
    }

    public static QQImageFeatureHSV a(a aVar) {
        return A(dD(aVar.mPath));
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
            d.f("ImageFeatureCenter", th);
            return bArr;
        }
    }

    private static byte[] dD(String str) {
        if (!isLoaded) {
            loadLib();
        }
        if (isLoaded) {
            Bitmap loadBitmap100x100FromFile;
            try {
                loadBitmap100x100FromFile = QQImageLoader.loadBitmap100x100FromFile(str);
            } catch (Throwable th) {
                d.f("ImageFeatureCenter", th);
                loadBitmap100x100FromFile = null;
            }
            if (loadBitmap100x100FromFile != null) {
                return a(loadBitmap100x100FromFile);
            }
        }
        return null;
    }

    private static synchronized void loadLib() {
        synchronized (rn.class) {
            try {
                System.loadLibrary("QQImageCompare-1.0-mfr");
                isLoaded = true;
            } catch (Throwable th) {
                d.f("ImageFeatureCenter", th);
            }
        }
    }
}
