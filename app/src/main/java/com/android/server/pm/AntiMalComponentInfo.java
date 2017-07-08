package com.android.server.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;

public class AntiMalComponentInfo implements Parcelable {
    public static final String ANTIMAL_TYPE_MASK = "antimal_type_mask";
    static final int BIT_ANTIMAL_TYPE_ADD = 1;
    static final int BIT_ANTIMAL_TYPE_DELETE = 4;
    static final int BIT_ANTIMAL_TYPE_MODIFY = 2;
    static final int BIT_ANTIMAL_TYPE_NORMAL = 0;
    public static final Creator<AntiMalComponentInfo> CREATOR = null;
    private static final boolean HW_DEBUG = false;
    public static final String NAME = "name";
    private static final String TAG = "AntiMalComponentInfo";
    public static final String VERIFY_STATUS = "verify_status";
    int mAntimalTypeMask;
    public final String mName;
    int mVerifyStatus;

    public interface VerifyStatus {
        public static final int PARSE_WHITE_LIST_FAILED = 4;
        public static final int SIGN_FILE_NOT_EXIST = 2;
        public static final int VERIFY_FAILED = 3;
        public static final int VERIFY_SECCUSS = 0;
        public static final int WHITE_LIST_NOT_EXIST = 1;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.AntiMalComponentInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.AntiMalComponentInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.AntiMalComponentInfo.<clinit>():void");
    }

    public boolean isVerifyStatusValid() {
        if (HW_DEBUG) {
            Log.d(TAG, "isVerifyStatusValid name = " + this.mName + " mVerifyStatus = " + this.mVerifyStatus);
        }
        if (this.mVerifyStatus == 0) {
            return true;
        }
        return HW_DEBUG;
    }

    public boolean isNormal() {
        if (this.mVerifyStatus == 0 && this.mAntimalTypeMask == 0) {
            return true;
        }
        return HW_DEBUG;
    }

    private String getComponentName(String WhiteListPath) {
        if (TextUtils.isEmpty(WhiteListPath)) {
            return null;
        }
        int subBegin = BIT_ANTIMAL_TYPE_NORMAL;
        String sub = "/";
        if (WhiteListPath.startsWith("/")) {
            subBegin = BIT_ANTIMAL_TYPE_ADD;
        }
        String subPath = WhiteListPath.substring(subBegin, WhiteListPath.length());
        int index = subPath.indexOf("/");
        String name = index > 0 ? subPath.substring(BIT_ANTIMAL_TYPE_NORMAL, index + BIT_ANTIMAL_TYPE_ADD) : subPath;
        if (HW_DEBUG) {
            Log.d(TAG, "getComponentName path = " + name + "index = " + index);
        }
        return name;
    }

    public AntiMalComponentInfo(String WhiteListPath) {
        this.mName = getComponentName(WhiteListPath);
    }

    public AntiMalComponentInfo(String name, int verifyStatus, int antimalType) {
        this.mName = name;
        this.mVerifyStatus = verifyStatus;
        this.mAntimalTypeMask = antimalType;
    }

    public AntiMalComponentInfo(Parcel source) {
        if (source != null) {
            this.mName = source.readString();
            this.mVerifyStatus = source.readInt();
            this.mAntimalTypeMask = source.readInt();
            return;
        }
        this.mName = null;
        this.mVerifyStatus = BIT_ANTIMAL_TYPE_NORMAL;
        this.mAntimalTypeMask = BIT_ANTIMAL_TYPE_NORMAL;
    }

    public void setAntiMalStatus(int bitMask) {
        this.mAntimalTypeMask |= bitMask;
    }

    public void setVerifyStatus(int status) {
        this.mVerifyStatus = status;
    }

    public int getVerifyStatus() {
        return this.mVerifyStatus;
    }

    public int describeContents() {
        return BIT_ANTIMAL_TYPE_NORMAL;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeString(this.mName);
            dest.writeInt(this.mVerifyStatus);
            dest.writeInt(this.mAntimalTypeMask);
        }
    }

    public String toString() {
        return "ComponetName : " + this.mName + " Verify Status : " + this.mVerifyStatus + " Antimal Type mask : " + this.mAntimalTypeMask;
    }
}
