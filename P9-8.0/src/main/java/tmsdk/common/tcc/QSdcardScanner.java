package tmsdk.common.tcc;

import tmsdkobf.py;
import tmsdkobf.pz;
import tmsdkobf.qa;

public class QSdcardScanner extends py {
    private long mNative;
    private ProgressListener mProgressListener;

    public interface ProgressListener {
        boolean onScanPathChange(String str);
    }

    public QSdcardScanner(long j, int i, long j2, Object obj) {
        super(i, j2);
        this.mNative = j;
        if (this.mNative != 0) {
            try {
                nativeInit(this.mNative);
                if (obj instanceof qa) {
                    setRules((qa) obj);
                }
            } catch (NoSuchMethodError e) {
                nativeRelease(this.mNative);
                this.mNative = 0;
            }
        }
    }

    private native void nativeCancle(long j);

    private native void nativeInit(long j);

    private native void nativeRelease(long j);

    private native void nativeScan(long j, String str);

    private native void nativeSetProgressListenLevel(long j, int i);

    private native void nativeSetRule(long j, int i, String[] strArr, String[] strArr2);

    private native void nativeSetWhiteList(long j, String[] strArr);

    private void onFound(int i, String str, int i2, long j, long j2, long j3, long j4) {
        if (this.mListener != null) {
            QFile qFile = new QFile(str);
            qFile.type = i2;
            qFile.size = j;
            qFile.modifyTime = j2;
            qFile.accessTime = j3;
            qFile.createTime = j4;
            this.mListener.onFound(i, qFile);
        }
    }

    protected void doCancleScan() {
        if (this.mNative != 0) {
            nativeCancle(this.mNative);
        }
    }

    protected void doStartScan(String str) {
        if (this.mNative != 0) {
            nativeScan(this.mNative, str);
        }
    }

    protected boolean onProgressChanger(String str) {
        return this.mProgressListener == null ? true : this.mProgressListener.onScanPathChange(str);
    }

    public void registerProgressListener(int i, ProgressListener progressListener) {
        if (this.mNative != 0) {
            this.mProgressListener = progressListener;
            nativeSetProgressListenLevel(this.mNative, i);
        }
    }

    public void release() {
        if (this.mNative != 0) {
            nativeRelease(this.mNative);
            this.mNative = 0;
        }
    }

    public void setRules(qa qaVar) {
        if (!(this.mNative == 0 || qaVar == null)) {
            if (!(qaVar.Lg == null || qaVar.Lg.length == 0)) {
                nativeSetWhiteList(this.mNative, qaVar.Lg);
            }
            if (!(qaVar.Lf == null || qaVar.Lf.size() == 0)) {
                for (pz pzVar : qaVar.Lf) {
                    nativeSetRule(this.mNative, pzVar.id, pzVar.Ld, pzVar.Le);
                }
            }
        }
    }
}
