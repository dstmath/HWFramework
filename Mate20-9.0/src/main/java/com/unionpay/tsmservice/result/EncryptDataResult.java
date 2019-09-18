package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class EncryptDataResult implements Parcelable {
    public static final Parcelable.Creator<EncryptDataResult> CREATOR = new Parcelable.Creator<EncryptDataResult>() {
        public final EncryptDataResult createFromParcel(Parcel parcel) {
            return new EncryptDataResult(parcel);
        }

        public final EncryptDataResult[] newArray(int i) {
            return new EncryptDataResult[i];
        }
    };
    private List<String> mEncryptData;

    public EncryptDataResult() {
    }

    public EncryptDataResult(Parcel parcel) {
        this.mEncryptData = new ArrayList();
        parcel.readList(this.mEncryptData, ClassLoader.getSystemClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public List<String> getEncryptData() {
        return this.mEncryptData;
    }

    public void setEncryptData(List<String> list) {
        this.mEncryptData = list;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(this.mEncryptData);
    }
}
