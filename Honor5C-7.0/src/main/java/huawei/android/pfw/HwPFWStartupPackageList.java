package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class HwPFWStartupPackageList implements Parcelable {
    public static final Creator<HwPFWStartupPackageList> CREATOR = null;
    public static final int STARTUP_LIST_TYPE_FWK_APP_SYSTEM_BLACK = 3;
    public static final int STARTUP_LIST_TYPE_FWK_APP_THIRD_WHITE = 2;
    public static final int STARTUP_LIST_TYPE_FWK_PREBUILT_SYSTEM_BLACK = 1;
    public static final int STARTUP_LIST_TYPE_FWK_PREBUILT_THIRD_WHITE = 0;
    public static final int STARTUP_LIST_TYPE_MUST_CONTROL_APPS = 4;
    private int mListType;
    private List<String> mPackageList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.pfw.HwPFWStartupPackageList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.pfw.HwPFWStartupPackageList.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.pfw.HwPFWStartupPackageList.<clinit>():void");
    }

    public HwPFWStartupPackageList(int type) {
        this.mPackageList = new ArrayList();
        this.mListType = type;
        this.mPackageList.clear();
    }

    public void setPackageList(int type, List<String> pkgList) {
        this.mListType = type;
        this.mPackageList.clear();
        this.mPackageList.addAll(pkgList);
    }

    public void copyOutPackageList(List<String> pkgList) {
        pkgList.addAll(this.mPackageList);
    }

    public int describeContents() {
        return STARTUP_LIST_TYPE_FWK_PREBUILT_THIRD_WHITE;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mListType);
        dest.writeStringList(this.mPackageList);
    }

    private HwPFWStartupPackageList(Parcel source) {
        this.mPackageList = new ArrayList();
        this.mListType = source.readInt();
        source.readStringList(this.mPackageList);
    }
}
