package com.unionpay.tsmservice.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class MessageDetail implements Parcelable {
    public static final Parcelable.Creator<MessageDetail> CREATOR = new Parcelable.Creator<MessageDetail>() {
        public final MessageDetail createFromParcel(Parcel parcel) {
            return new MessageDetail(parcel);
        }

        public final MessageDetail[] newArray(int i) {
            return new MessageDetail[i];
        }
    };
    private Bundle mDetail;

    public MessageDetail() {
    }

    public MessageDetail(Parcel parcel) {
        this.mDetail = parcel.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public Bundle getDetail() {
        return this.mDetail;
    }

    public void setDetail(Bundle bundle) {
        this.mDetail = bundle;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mDetail);
    }
}
