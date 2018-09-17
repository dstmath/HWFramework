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
    public static final Creator<AntiMalComponentInfo> CREATOR = new Creator<AntiMalComponentInfo>() {
        public AntiMalComponentInfo createFromParcel(Parcel source) {
            return new AntiMalComponentInfo(source);
        }

        public AntiMalComponentInfo[] newArray(int size) {
            return new AntiMalComponentInfo[size];
        }
    };
    private static final boolean HW_DEBUG;
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
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    public boolean isVerifyStatusValid() {
        if (HW_DEBUG) {
            Log.d(TAG, "isVerifyStatusValid name = " + this.mName + " mVerifyStatus = " + this.mVerifyStatus);
        }
        if (this.mVerifyStatus == 0) {
            return true;
        }
        return false;
    }

    public boolean isNormal() {
        if (this.mVerifyStatus == 0 && this.mAntimalTypeMask == 0) {
            return true;
        }
        return false;
    }

    private String getComponentName(String WhiteListPath) {
        if (TextUtils.isEmpty(WhiteListPath)) {
            return null;
        }
        int subBegin = 0;
        String sub = "/";
        if (WhiteListPath.startsWith("/")) {
            subBegin = 1;
        }
        String subPath = WhiteListPath.substring(subBegin, WhiteListPath.length());
        int index = subPath.indexOf("/");
        String name = index > 0 ? subPath.substring(0, index + 1) : subPath;
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
        this.mVerifyStatus = 0;
        this.mAntimalTypeMask = 0;
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
        return 0;
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
