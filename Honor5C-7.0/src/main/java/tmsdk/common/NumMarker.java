package tmsdk.common;

import android.content.Context;
import android.os.Build.VERSION;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.tcc.NumMarkerConsts;
import tmsdk.common.utils.d;
import tmsdkobf.ms;

/* compiled from: Unknown */
public class NumMarker implements NumMarkerConsts {
    public static final int KEY_TAG_CALL_TIME_LENGTH = 1;
    public static final String Tag = "NumMarkerTag";
    private static NumMarker zU;
    private List<Integer> Aa;
    private List<Integer> Ab;
    private Context mContext;
    private Object mLock;
    private String mPath;
    private long zV;
    private boolean zW;
    private boolean zX;
    private List<Integer> zY;
    private List<String> zZ;

    /* compiled from: Unknown */
    public static class MarkFileInfo {
        public String md5;
        public int timeStampSecondLastDiff;
        public int timeStampSecondWhole;
        public int version;
    }

    /* compiled from: Unknown */
    public static class NativeNumMarkEntity {
        public int count;
        public String num;
        public String tagName;
        public int tagValue;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.NumMarker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.NumMarker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.NumMarker.<clinit>():void");
    }

    private NumMarker(Context context) {
        this.zV = 0;
        this.zW = true;
        this.zX = true;
        this.zY = new ArrayList();
        this.zZ = new ArrayList();
        this.Aa = new ArrayList();
        this.Ab = new ArrayList();
        this.mPath = "";
        this.mLock = new Object();
        d.d(Tag, "NumMarker()");
        this.mContext = context;
        this.zV = nNewInstance(VERSION.SDK_INT);
        if (this.zV != 0) {
            String str = "40331.sdb";
            d.d(Tag, "datafile name =" + str);
            this.mPath = ms.a(this.mContext, str, null);
            d.d(Tag, "NumMarker() mPath: " + this.mPath);
            if (this.mPath == null || "".equals(this.mPath)) {
                this.mPath = this.mContext.getFilesDir().toString() + File.separator + str;
            }
            nSetPath(this.zV, this.mPath);
        }
    }

    public static synchronized NumMarker getDefault(Context context) {
        NumMarker numMarker;
        synchronized (NumMarker.class) {
            if (zU == null) {
                zU = new NumMarker(context);
            }
            numMarker = zU;
        }
        return numMarker;
    }

    private native void nDestroyInstance(long j);

    private native String nGetDataMd5(long j, String str);

    private native boolean nGetHeaderInfo(long j, AtomicInteger atomicInteger, AtomicInteger atomicInteger2, AtomicInteger atomicInteger3, AtomicReference<String> atomicReference);

    private native boolean nGetMarkInfoByPhoneNumber(long j, String str, AtomicInteger atomicInteger, AtomicInteger atomicInteger2);

    private native boolean nGetTagList(long j, List<Integer> list, List<Integer> list2);

    private native boolean nGetTypeNameMapping(long j, List<Integer> list, List<String> list2);

    private native long nNewInstance(int i);

    private native boolean nRepack(long j);

    private native boolean nSetPath(long j, String str);

    private native int nUpdate(long j, String str, String str2);

    public synchronized void destroy() {
        this.zY.clear();
        this.zZ.clear();
        this.Aa.clear();
        this.Ab.clear();
        nDestroyInstance(this.zV);
        this.zV = 0;
        zU = null;
    }

    public void getConfigList(List<Integer> list, List<Integer> list2) {
        if (this.zX) {
            this.Aa.clear();
            this.Ab.clear();
            this.zX = false;
            nGetTagList(this.zV, this.Aa, this.Ab);
            if (this.Ab.size() >= KEY_TAG_CALL_TIME_LENGTH) {
                d.d(Tag, "getConfigList() value[0]: " + this.Ab.get(0));
            }
        }
        list.clear();
        list2.clear();
        list.addAll(this.Aa);
        list2.addAll(this.Ab);
    }

    public String getDataMd5(String str) {
        return nGetDataMd5(this.zV, str);
    }

    public NativeNumMarkEntity getInfoOfNum(String str) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicInteger atomicInteger2 = new AtomicInteger(0);
        if (!nGetMarkInfoByPhoneNumber(this.zV, str, atomicInteger, atomicInteger2)) {
            return null;
        }
        NativeNumMarkEntity nativeNumMarkEntity = new NativeNumMarkEntity();
        nativeNumMarkEntity.num = str;
        nativeNumMarkEntity.tagValue = atomicInteger.get();
        nativeNumMarkEntity.count = atomicInteger2.get();
        return nativeNumMarkEntity;
    }

    public MarkFileInfo getMarkFileInfo() {
        MarkFileInfo markFileInfo = new MarkFileInfo();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicInteger atomicInteger2 = new AtomicInteger(0);
        AtomicInteger atomicInteger3 = new AtomicInteger(0);
        AtomicReference atomicReference = new AtomicReference("");
        if (nGetHeaderInfo(this.zV, atomicInteger, atomicInteger2, atomicInteger3, atomicReference)) {
            markFileInfo.version = atomicInteger.get();
            markFileInfo.timeStampSecondWhole = atomicInteger2.get();
            markFileInfo.timeStampSecondLastDiff = atomicInteger3.get();
            markFileInfo.md5 = atomicReference.get() == null ? "" : (String) atomicReference.get();
        }
        return markFileInfo;
    }

    public void getMarkList(List<Integer> list, List<String> list2) {
        if (this.zW) {
            this.zY.clear();
            this.zZ.clear();
            this.zW = false;
            nGetTypeNameMapping(this.zV, this.zY, this.zZ);
        }
        list.clear();
        list2.clear();
        list.addAll(this.zY);
        list2.addAll(this.zZ);
    }

    public boolean refreshMarkFile() {
        return nRepack(this.zV);
    }

    public int updateMarkFile(String str, String str2) {
        int nUpdate;
        synchronized (this.mLock) {
            nUpdate = nUpdate(this.zV, str, str2);
        }
        if (nUpdate == 0) {
            this.zW = true;
            this.zX = true;
        }
        return nUpdate;
    }
}
