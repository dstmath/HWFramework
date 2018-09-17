package com.tencent.qqimagecompare;

import java.util.ArrayList;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class QQImageFeaturesAccessmentHSV extends QQImageNativeObject {

    /* compiled from: Unknown */
    /* renamed from: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] na = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.1.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.1.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.1.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    public enum eDimensionType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.eDimensionType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.eDimensionType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV.eDimensionType.<clinit>():void");
        }
    }

    public QQImageFeaturesAccessmentHSV() {
    }

    private static native void AddDimensionC(long j, int i, int i2);

    private static native int GetFeaturesRankC(long j, long[] jArr, int[] iArr);

    public void addDimension(eDimensionType com_tencent_qqimagecompare_QQImageFeaturesAccessmentHSV_eDimensionType, int i) {
        int i2 = 0;
        switch (AnonymousClass1.na[com_tencent_qqimagecompare_QQImageFeaturesAccessmentHSV_eDimensionType.ordinal()]) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                i2 = 1;
                break;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                i2 = 2;
                break;
        }
        AddDimensionC(this.mThisC, i2, i);
    }

    protected native long createNativeObject();

    protected native void destroyNativeObject(long j);

    public int[] getFeaturesRanks(ArrayList<QQImageFeatureHSV> arrayList) {
        int size = arrayList.size();
        int[] iArr = new int[size];
        long[] jArr = new long[size];
        for (int i = 0; i < size; i++) {
            jArr[i] = ((QQImageFeatureHSV) arrayList.get(i)).mThisC;
        }
        GetFeaturesRankC(this.mThisC, jArr, iArr);
        return iArr;
    }
}
