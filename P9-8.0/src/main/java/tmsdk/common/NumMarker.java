package tmsdk.common;

import android.content.Context;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.NumMarkerConsts;
import tmsdk.common.utils.f;
import tmsdk.common.utils.r;
import tmsdkobf.lu;

public class NumMarker implements NumMarkerConsts {
    public static final int KEY_TAG_CALL_TIME_LENGTH = 1;
    public static final String Tag = "NumMarkerTag";
    private static NumMarker xg = null;
    private Context mContext;
    private Object mLock = new Object();
    private String mPath = "";
    private long xh = 0;
    private boolean xi = true;
    private boolean xj = true;
    private List<Integer> xk = new ArrayList();
    private List<String> xl = new ArrayList();
    private List<Integer> xm = new ArrayList();
    private List<Integer> xn = new ArrayList();

    public static class MarkFileInfo {
        public String md5;
        public int timeStampSecondLastDiff;
        public int timeStampSecondWhole;
        public int version;
    }

    public static class NumMark {
        public int count;
        public String num;
        public String tagName;
        public int tagValue;
    }

    static {
        TMSDKContext.registerNatives(4, NumMarker.class);
    }

    /* JADX WARNING: Missing block: B:7:0x00c8, code:
            if ("".equals(r8.mPath) == false) goto L_0x00b7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private NumMarker(Context context) {
        f.f(Tag, "NumMarker()");
        this.mContext = context;
        this.xh = nNewInstance(VERSION.SDK_INT);
        if (this.xh != 0) {
            String str = "40458.sdb";
            f.f(Tag, "datafile name =" + str);
            this.mPath = lu.b(this.mContext, str, null);
            f.f(Tag, "NumMarker() mPath: " + this.mPath);
            if (this.mPath != null) {
            }
            this.mPath = this.mContext.getFilesDir().toString() + File.separator + str;
            nSetPath(this.xh, this.mPath);
        }
    }

    public static synchronized NumMarker getDefault(Context context) {
        NumMarker numMarker;
        synchronized (NumMarker.class) {
            if (xg == null) {
                xg = new NumMarker(context);
            }
            numMarker = xg;
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
        this.xk.clear();
        this.xl.clear();
        this.xm.clear();
        this.xn.clear();
        if (this.xh != 0) {
            nDestroyInstance(this.xh);
        }
        this.xh = 0;
        xg = null;
    }

    public void getConfigList(List<Integer> list, List<Integer> list2) {
        if (this.xj) {
            this.xm.clear();
            this.xn.clear();
            this.xj = false;
            if (this.xh != 0) {
                nGetTagList(this.xh, this.xm, this.xn);
            }
            if (this.xn.size() >= 1) {
                f.f(Tag, "getConfigList() value[0]: " + this.xn.get(0));
            }
        }
        list.clear();
        list2.clear();
        list.addAll(this.xm);
        list2.addAll(this.xn);
    }

    public String getDataMd5(String str) {
        return this.xh != 0 ? nGetDataMd5(this.xh, str) : null;
    }

    public NumMark getInfoOfNum(String str) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicInteger atomicInteger2 = new AtomicInteger(0);
        try {
            if (this.xh != 0) {
                if (nGetMarkInfoByPhoneNumber(this.xh, str, atomicInteger, atomicInteger2)) {
                    NumMark numMark = new NumMark();
                    numMark.num = str;
                    numMark.tagValue = atomicInteger.get();
                    numMark.count = atomicInteger2.get();
                    return numMark;
                }
            }
        } catch (Throwable th) {
            f.e(Tag, th);
        }
        return null;
    }

    public NumMark getInfoOfNumForBigFile(String str) {
        long j = 0;
        try {
            j = nNewInstance(VERSION.SDK_INT);
            if (j != 0) {
                Object k = r.k(this.mContext, UpdateConfig.getLargeMarkFileName());
                if (TextUtils.isEmpty(k)) {
                    if (j != 0) {
                        try {
                            nDestroyInstance(j);
                        } catch (Throwable th) {
                            f.e(Tag, th);
                        }
                    }
                    return null;
                }
                nSetPath(j, k);
                AtomicInteger atomicInteger = new AtomicInteger(0);
                AtomicInteger atomicInteger2 = new AtomicInteger(0);
                if (nGetMarkInfoByPhoneNumber(j, str, atomicInteger, atomicInteger2)) {
                    NumMark numMark = new NumMark();
                    numMark.num = str;
                    numMark.tagValue = atomicInteger.get();
                    numMark.count = atomicInteger2.get();
                    NumMark numMark2 = numMark;
                    if (j != 0) {
                        try {
                            nDestroyInstance(j);
                        } catch (Throwable th2) {
                            f.e(Tag, th2);
                        }
                    }
                    return numMark;
                }
            }
            if (j != 0) {
                try {
                    nDestroyInstance(j);
                } catch (Throwable th3) {
                    f.e(Tag, th3);
                }
            }
        } catch (Throwable th32) {
            f.e(Tag, th32);
        }
        return null;
    }

    public MarkFileInfo getMarkFileInfo(int i, String str) {
        MarkFileInfo markFileInfo = new MarkFileInfo();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicInteger atomicInteger2 = new AtomicInteger(0);
        AtomicInteger atomicInteger3 = new AtomicInteger(0);
        AtomicReference atomicReference = new AtomicReference("");
        long nNewInstance = nNewInstance(VERSION.SDK_INT);
        if (nNewInstance != 0) {
            nSetPath(nNewInstance, str);
        }
        if (nNewInstance != 0 && nGetHeaderInfo(nNewInstance, atomicInteger, atomicInteger2, atomicInteger3, atomicReference)) {
            markFileInfo.version = atomicInteger.get();
            markFileInfo.timeStampSecondWhole = atomicInteger2.get();
            markFileInfo.timeStampSecondLastDiff = atomicInteger3.get();
            markFileInfo.md5 = atomicReference.get() == null ? "" : (String) atomicReference.get();
        }
        return markFileInfo;
    }

    public void getMarkList(List<Integer> list, List<String> list2) {
        if (this.xi) {
            this.xk.clear();
            this.xl.clear();
            this.xi = false;
            if (this.xh != 0) {
                nGetTypeNameMapping(this.xh, this.xk, this.xl);
            }
        }
        list.clear();
        list2.clear();
        list.addAll(this.xk);
        list2.addAll(this.xl);
    }

    public boolean refreshMarkFile() {
        return this.xh != 0 ? nRepack(this.xh) : false;
    }

    public int updateMarkBigFile(String str, String str2) {
        int i = -3;
        long j = 0;
        try {
            j = nNewInstance(VERSION.SDK_INT);
            if (j != 0) {
                this.mPath = r.b(this.mContext, UpdateConfig.getLargeMarkFileId(), ".sdb");
                if (TextUtils.isEmpty(this.mPath)) {
                    this.mPath = this.mContext.getFilesDir().toString() + File.separator + UpdateConfig.getLargeMarkFileName();
                }
                nSetPath(j, this.mPath);
                synchronized (this.mLock) {
                    if (j != 0) {
                        i = nUpdate(j, str, str2);
                    }
                }
            }
            if (j != 0) {
                try {
                    nDestroyInstance(j);
                } catch (Throwable th) {
                    f.e(Tag, th);
                }
            }
        } catch (Throwable th2) {
            f.e(Tag, th2);
        }
        return i;
    }

    public int updateMarkFile(String str, String str2) {
        int i = -3;
        synchronized (this.mLock) {
            if (this.xh != 0) {
                i = nUpdate(this.xh, str, str2);
            }
        }
        if (i == 0) {
            this.xi = true;
            this.xj = true;
        }
        return i;
    }
}
