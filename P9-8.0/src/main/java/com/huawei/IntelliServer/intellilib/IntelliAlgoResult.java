package com.huawei.IntelliServer.intellilib;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IntelliAlgoResult implements Parcelable {
    public static final Creator<IntelliAlgoResult> CREATOR = new Creator<IntelliAlgoResult>() {
        public IntelliAlgoResult createFromParcel(Parcel in) {
            return new IntelliAlgoResult(in);
        }

        public IntelliAlgoResult[] newArray(int size) {
            return new IntelliAlgoResult[size];
        }
    };
    public static final int INTELLI_DETECT_FACE_PRESENCE = 1;
    public static final int INTELLI_DETECT_NO_FACE = 0;
    public static final int INTELLI_FACE_BAD_LIGHT = 26;
    public static final int INTELLI_FACE_COMPARE_FAILURE = 12;
    public static final int INTELLI_FACE_DARKLIGHT = 30;
    public static final int INTELLI_FACE_FACE_BLUR = 28;
    public static final int INTELLI_FACE_FACE_DOWN = 18;
    public static final int INTELLI_FACE_FACE_MULTI = 27;
    public static final int INTELLI_FACE_FACE_NOT_COMPLETE = 29;
    public static final int INTELLI_FACE_FACE_NOT_FOUND = 5;
    public static final int INTELLI_FACE_FACE_OFFSET_BOTTOM = 11;
    public static final int INTELLI_FACE_FACE_OFFSET_LEFT = 8;
    public static final int INTELLI_FACE_FACE_OFFSET_RIGHT = 10;
    public static final int INTELLI_FACE_FACE_OFFSET_TOP = 9;
    public static final int INTELLI_FACE_FACE_QUALITY = 4;
    public static final int INTELLI_FACE_FACE_RISE = 16;
    public static final int INTELLI_FACE_FACE_ROTATED_LEFT = 15;
    public static final int INTELLI_FACE_FACE_ROTATED_RIGHT = 17;
    public static final int INTELLI_FACE_FACE_SCALE_TOO_LARGE = 7;
    public static final int INTELLI_FACE_FACE_SCALE_TOO_SMALL = 6;
    public static final int INTELLI_FACE_FAILED = 3;
    public static final int INTELLI_FACE_FEATURE_MISS = 24;
    public static final int INTELLI_FACE_FEATURE_VERSION_ERROR = 25;
    public static final int INTELLI_FACE_HALF_SHADOW = 32;
    public static final int INTELLI_FACE_HIGHLIGHT = 31;
    public static final int INTELLI_FACE_INVALID_ARGUMENT = 33;
    public static final int INTELLI_FACE_INVALID_HANDLE = 2;
    public static final int INTELLI_FACE_KEEP = 19;
    public static final int INTELLI_FACE_LIVENESS_FAILURE = 14;
    public static final int INTELLI_FACE_LIVENESS_WARNING = 13;
    public static final int INTELLI_FACE_OK = 0;
    public static final int INTELLI_FACE_ORIENTION_0 = 0;
    public static final int INTELLI_FACE_ORIENTION_270 = 3;
    public static final int INTELLI_FACE_ORIENTION_90 = 1;
    public static final int INTELLI_REGISITER_COMPLETE = 1;
    public static final int INTELLI_VERIFY_FALSE = 0;
    public static final int INTELLI_VERIFY_TURE = 1;
    public static final int MG_ATTR_BLUR = 20;
    public static final int MG_ATTR_EYE_CLOSE = 22;
    public static final int MG_ATTR_EYE_OCCLUSION = 21;
    public static final int MG_ATTR_MOUTH_OCCLUSION = 23;
    private int presenceResult;
    private int regisiterProgess;
    private int rotation;
    private int verifyResult;

    public int getRegisiterProgess() {
        return this.regisiterProgess;
    }

    public int getPrecenseStatus() {
        return this.presenceResult;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getVerifyResult() {
        return this.verifyResult;
    }

    public void setRegisiterStatus(int result) {
        this.regisiterProgess = result;
    }

    public void setPrecenseStatus(int result) {
        this.presenceResult = result;
    }

    public void setOriention(int result) {
        this.rotation = result;
    }

    public void setVerifyResult(int result) {
        this.verifyResult = result;
    }

    public IntelliAlgoResult() {
        this.regisiterProgess = -1;
        this.presenceResult = -1;
        this.rotation = -1;
        this.verifyResult = -1;
    }

    public IntelliAlgoResult(Parcel in) {
        this.regisiterProgess = in.readInt();
        this.presenceResult = in.readInt();
        this.rotation = in.readInt();
        this.verifyResult = in.readInt();
    }

    public void readFromParcel(Parcel parcel) {
        this.regisiterProgess = parcel.readInt();
        this.presenceResult = parcel.readInt();
        this.rotation = parcel.readInt();
        this.verifyResult = parcel.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeInt(this.regisiterProgess);
        parcel.writeInt(this.presenceResult);
        parcel.writeInt(this.rotation);
        parcel.writeInt(this.verifyResult);
    }

    public String toString() {
        return "result-regisiterProgess: " + this.regisiterProgess + "presenceResult: " + this.presenceResult + "rotation: " + this.rotation + "verifyResult: " + this.verifyResult;
    }
}
