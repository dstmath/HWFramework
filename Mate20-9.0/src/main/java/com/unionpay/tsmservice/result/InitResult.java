package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.data.UpdateInfo;

public class InitResult implements Parcelable {
    public static final Parcelable.Creator<InitResult> CREATOR = new Parcelable.Creator<InitResult>() {
        public final InitResult createFromParcel(Parcel parcel) {
            return new InitResult(parcel);
        }

        public final InitResult[] newArray(int i) {
            return new InitResult[i];
        }
    };
    private UpdateInfo mUpdateInfo;

    public InitResult() {
    }

    public InitResult(Parcel parcel) {
        this.mUpdateInfo = (UpdateInfo) parcel.readParcelable(UpdateInfo.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public UpdateInfo getUpdateInfo() {
        return this.mUpdateInfo;
    }

    public void setUpdateInfo(UpdateInfo updateInfo) {
        this.mUpdateInfo = updateInfo;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mUpdateInfo, i);
    }
}
