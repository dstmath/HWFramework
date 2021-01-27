package com.huawei.security;

import android.os.Parcel;
import android.os.Parcelable;

public class ControlWordEntity implements Parcelable {
    public static final Parcelable.Creator<ControlWordEntity> CREATOR = new Parcelable.Creator<ControlWordEntity>() {
        /* class com.huawei.security.ControlWordEntity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ControlWordEntity createFromParcel(Parcel source) {
            return new ControlWordEntity(source);
        }

        @Override // android.os.Parcelable.Creator
        public ControlWordEntity[] newArray(int size) {
            return new ControlWordEntity[size];
        }
    };
    private int encryptionMethod;
    private int transportCounter;

    public ControlWordEntity() {
    }

    private ControlWordEntity(Parcel source) {
        this.transportCounter = source.readInt();
        this.encryptionMethod = source.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.transportCounter = source.readInt();
        this.encryptionMethod = source.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.transportCounter);
        dest.writeInt(this.encryptionMethod);
    }

    public int getTransportCounter() {
        return this.transportCounter;
    }

    public void setTransportCounter(int transportCounter2) {
        this.transportCounter = transportCounter2;
    }

    public int getEncryptionMethod() {
        return this.encryptionMethod;
    }

    public void setEncryptionMethod(int encryptionMethod2) {
        this.encryptionMethod = encryptionMethod2;
    }
}
