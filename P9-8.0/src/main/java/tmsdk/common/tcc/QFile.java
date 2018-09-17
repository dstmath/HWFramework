package tmsdk.common.tcc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdkobf.py.a;
import tmsdkobf.qa;

public class QFile {
    public static final long SIZE_NOT_KNOW = -1;
    public static final long TIME_NOT_KNOW = -1;
    public static final int TYPE_DIR = 4;
    public static final int TYPE_NOT_KNOW = 0;
    public long accessTime = -1;
    public long createTime = -1;
    public String filePath;
    public DeletedCallback mDeletedCallback;
    public long modifyTime = -1;
    public long size = -1;
    public int type = 0;

    public interface DeletedCallback {
        void onDeleteProgress(long j);
    }

    static {
        System.loadLibrary("dce-1.1.17-mfr");
    }

    public QFile(String str) {
        this.filePath = str;
    }

    public QFile(String str, int i) {
        this.filePath = str;
        this.type = i;
    }

    private native int nativeDeleteAllChildren(String str);

    private native int nativeDeleteAllChildrenByDay(String str, int i);

    private native void nativeFillExtraInfo(String str);

    private native QFile[] nativeList(String str);

    private native void nativeRemoveEmptyDir(String str);

    public int deleteAllChildren() {
        return nativeDeleteAllChildren(this.filePath);
    }

    public int deleteAllChildrenByDiffDay(int i) {
        return nativeDeleteAllChildrenByDay(this.filePath, i);
    }

    public boolean deleteSelf() {
        return toFile().delete();
    }

    public void fillExtraInfo() {
        try {
            nativeFillExtraInfo(this.filePath);
        } catch (UnsatisfiedLinkError e) {
        }
    }

    public QFile[] list() {
        return nativeList(this.filePath);
    }

    public List<QFile> listAll(long j, qa qaVar) {
        final List<QFile> arrayList = new ArrayList();
        QSdcardScanner qSdcardScanner = SdcardScannerFactory.getQSdcardScanner(j, new a() {
            public void onFound(int i, QFile qFile) {
                arrayList.add(qFile);
            }
        }, qaVar);
        if (qSdcardScanner != null) {
            qSdcardScanner.startScan(this.filePath);
            qSdcardScanner.release();
        }
        return arrayList;
    }

    public void onDeleteProgress(long j) {
        if (this.mDeletedCallback != null) {
            this.mDeletedCallback.onDeleteProgress(j);
        }
    }

    public File toFile() {
        return new File(this.filePath);
    }
}
