package com.huawei.android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;

public class HwWifiDisplayParameters implements Parcelable {
    private static final int BASE = 1;
    public static final Parcelable.Creator<HwWifiDisplayParameters> CREATOR = new Parcelable.Creator<HwWifiDisplayParameters>() {
        /* class com.huawei.android.hardware.display.HwWifiDisplayParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwWifiDisplayParameters[] newArray(int size) {
            return new HwWifiDisplayParameters[size];
        }

        @Override // android.os.Parcelable.Creator
        public HwWifiDisplayParameters createFromParcel(Parcel source) {
            return new HwWifiDisplayParameters(source.readString(), source.readInt());
        }
    };
    public static final int SCENE_GENERIC = 1;
    public static final int SCENE_MSDP_GENERIC = 4;
    public static final int SCENE_MSDP_WITH_SERVERPORT = 2;
    public static final int SCENE_VERIFICATION_CODE = 8;
    private int mProjectionScene;
    private String mVerificationCode;

    public HwWifiDisplayParameters(String verificationCode) {
        this(verificationCode, 8);
    }

    public HwWifiDisplayParameters(int projectionScene) {
        this("", projectionScene);
    }

    public HwWifiDisplayParameters(String verificationCode, int projectionScene) {
        this.mVerificationCode = "";
        this.mProjectionScene = 1;
        this.mVerificationCode = verificationCode;
        this.mProjectionScene = projectionScene;
    }

    public HwWifiDisplayParameters() {
        this.mVerificationCode = "";
        this.mProjectionScene = 1;
    }

    public String getVerificaitonCode() {
        return this.mVerificationCode;
    }

    public void setVerificaitonCode(String verificationCode) {
        this.mVerificationCode = verificationCode;
    }

    public int getProjectionScene() {
        return this.mProjectionScene;
    }

    public void setProjectionScene(int projectionScene) {
        this.mProjectionScene = projectionScene;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mVerificationCode);
        dest.writeInt(this.mProjectionScene);
    }
}
