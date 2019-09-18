package com.huawei.hiai.awareness.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ExtendAwarenessFence extends AwarenessFence implements Parcelable {
    public static final Parcelable.Creator<ExtendAwarenessFence> CREATOR = new Parcelable.Creator<ExtendAwarenessFence>() {
        public ExtendAwarenessFence createFromParcel(Parcel in) {
            return new ExtendAwarenessFence(in);
        }

        public ExtendAwarenessFence[] newArray(int size) {
            return new ExtendAwarenessFence[size];
        }
    };
    private static final String TAG = "ExtendAwarenessFence";
    protected Bundle registerBundle;

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBundle(this.registerBundle);
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        this.registerBundle = in.readBundle();
    }

    public ExtendAwarenessFence(Parcel in) {
        super(in);
        this.registerBundle = in.readBundle();
    }

    public ExtendAwarenessFence(int type, int status, int action, String secondAction) {
        super(type, status, action, secondAction);
    }

    public ExtendAwarenessFence(ExtendAwarenessFence awarenessFence) {
        super((AwarenessFence) awarenessFence);
        this.registerBundle = awarenessFence.getRegisterBundle();
    }

    public Bundle getRegisterBundle() {
        return this.registerBundle;
    }

    public void setRegisterBundle(Bundle registerBundle2) {
        this.registerBundle = registerBundle2;
    }

    public int describeContents() {
        return 0;
    }
}
