package tmsdk.common.tcc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdkobf.qv.a;
import tmsdkobf.qx;

/* compiled from: Unknown */
public class QFile {
    public static final long SIZE_NOT_KNOW = -1;
    public static final long TIME_NOT_KNOW = -1;
    public static final int TYPE_DIR = 4;
    public static final int TYPE_NOT_KNOW = 0;
    public long accessTime;
    public long createTime;
    public String filePath;
    public DeletedCallback mDeletedCallback;
    public long modifyTime;
    public long size;
    public int type;

    /* compiled from: Unknown */
    /* renamed from: tmsdk.common.tcc.QFile.1 */
    class AnonymousClass1 implements a {
        final /* synthetic */ List val$fileList;

        AnonymousClass1(List list) {
            this.val$fileList = list;
        }

        public void onFound(int i, QFile qFile) {
            this.val$fileList.add(qFile);
        }
    }

    /* compiled from: Unknown */
    public interface DeletedCallback {
        void onDeleteProgress(long j);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.tcc.QFile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.tcc.QFile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.tcc.QFile.<clinit>():void");
    }

    public QFile(String str) {
        this.type = 0;
        this.size = TIME_NOT_KNOW;
        this.modifyTime = TIME_NOT_KNOW;
        this.accessTime = TIME_NOT_KNOW;
        this.createTime = TIME_NOT_KNOW;
        this.filePath = str;
    }

    public QFile(String str, int i) {
        this.type = 0;
        this.size = TIME_NOT_KNOW;
        this.modifyTime = TIME_NOT_KNOW;
        this.accessTime = TIME_NOT_KNOW;
        this.createTime = TIME_NOT_KNOW;
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

    public List<QFile> listAll(long j, qx qxVar) {
        List<QFile> arrayList = new ArrayList();
        QSdcardScanner qSdcardScanner = SdcardScannerFactory.getQSdcardScanner(j, new AnonymousClass1(arrayList), qxVar);
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
