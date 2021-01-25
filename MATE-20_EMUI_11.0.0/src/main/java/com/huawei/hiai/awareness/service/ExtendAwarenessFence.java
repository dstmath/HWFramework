package com.huawei.hiai.awareness.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ExtendAwarenessFence extends AwarenessFence implements Parcelable {
    public static final Parcelable.Creator<ExtendAwarenessFence> CREATOR = new Parcelable.Creator<ExtendAwarenessFence>() {
        /* class com.huawei.hiai.awareness.service.ExtendAwarenessFence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ExtendAwarenessFence createFromParcel(Parcel in) {
            return new ExtendAwarenessFence(in);
        }

        @Override // android.os.Parcelable.Creator
        public ExtendAwarenessFence[] newArray(int size) {
            return new ExtendAwarenessFence[size];
        }
    };
    private static final String TAG = "ExtendAwarenessFence";
    protected Bundle mRegisterBundle;

    public ExtendAwarenessFence(Parcel in) {
        super(in);
        this.mRegisterBundle = in.readBundle();
    }

    public ExtendAwarenessFence(int type, int status, int action, String secondAction) {
        super(type, status, action, secondAction);
    }

    @Override // com.huawei.hiai.awareness.service.AwarenessFence, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBundle(this.mRegisterBundle);
    }

    @Override // com.huawei.hiai.awareness.service.AwarenessFence
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        this.mRegisterBundle = in.readBundle();
    }

    public Bundle getRegisterBundle() {
        return this.mRegisterBundle;
    }

    public void setRegisterBundle(Bundle registerBundle) {
        this.mRegisterBundle = registerBundle;
    }

    @Override // com.huawei.hiai.awareness.service.AwarenessFence, android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
