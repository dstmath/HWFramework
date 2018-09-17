package com.tencent.qqimagecompare;

import android.graphics.Bitmap;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class QQImageBitmap {
    long mThisC;

    /* compiled from: Unknown */
    /* renamed from: com.tencent.qqimagecompare.QQImageBitmap.1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] mU = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.tencent.qqimagecompare.QQImageBitmap.1.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.tencent.qqimagecompare.QQImageBitmap.1.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.tencent.qqimagecompare.QQImageBitmap.1.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    public enum eColorFormat {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.tencent.qqimagecompare.QQImageBitmap.eColorFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.tencent.qqimagecompare.QQImageBitmap.eColorFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.tencent.qqimagecompare.QQImageBitmap.eColorFormat.<clinit>():void");
        }
    }

    QQImageBitmap() {
        this.mThisC = createNativeObject0();
    }

    public QQImageBitmap(eColorFormat com_tencent_qqimagecompare_QQImageBitmap_eColorFormat, int i, int i2, int i3) {
        int i4 = 0;
        switch (AnonymousClass1.mU[com_tencent_qqimagecompare_QQImageBitmap_eColorFormat.ordinal()]) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                i4 = 1;
                break;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                i4 = 2;
                break;
        }
        this.mThisC = createNativeObject4i(i4, i, i2, i3);
    }

    QQImageBitmap(boolean z) {
        if (!z) {
            this.mThisC = createNativeObject0();
        }
    }

    private native int ClipBitmapC(long j, Bitmap bitmap, int i, int i2);

    private native int ClipC(long j, long j2, int i, int i2);

    private native int GetHeightC(long j);

    private native int GetWidthC(long j);

    public static long createNativeObject() {
        return createNativeObject0();
    }

    private static native long createNativeObject0();

    private native long createNativeObject4i(int i, int i2, int i3, int i4);

    private native void destroyNativeObject(long j);

    public int clip(Bitmap bitmap, int i, int i2) {
        return ClipBitmapC(this.mThisC, bitmap, i, i2);
    }

    public int clip(QQImageBitmap qQImageBitmap, int i, int i2) {
        return ClipC(this.mThisC, qQImageBitmap.mThisC, i, i2);
    }

    public void delete() {
        if (this.mThisC != 0) {
            destroyNativeObject(this.mThisC);
            this.mThisC = 0;
        }
    }

    public int getHeight() {
        return GetHeightC(this.mThisC);
    }

    public int getWidth() {
        return GetWidthC(this.mThisC);
    }
}
