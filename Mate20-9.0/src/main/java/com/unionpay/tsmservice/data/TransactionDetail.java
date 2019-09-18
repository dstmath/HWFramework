package com.unionpay.tsmservice.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class TransactionDetail implements Parcelable {
    public static final Parcelable.Creator<TransactionDetail> CREATOR = new Parcelable.Creator<TransactionDetail>() {
        public final TransactionDetail createFromParcel(Parcel parcel) {
            return new TransactionDetail(parcel);
        }

        public final TransactionDetail[] newArray(int i) {
            return new TransactionDetail[i];
        }
    };
    private Bundle mDetail;
    private MessageDetail[] mMessageDetails;

    public TransactionDetail() {
    }

    public TransactionDetail(Parcel parcel) {
        this.mDetail = parcel.readBundle();
        this.mMessageDetails = (MessageDetail[]) parcel.createTypedArray(MessageDetail.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public Bundle getDetail() {
        return this.mDetail;
    }

    public MessageDetail[] getMessageDetails() {
        return this.mMessageDetails;
    }

    public void setDetail(Bundle bundle) {
        this.mDetail = bundle;
    }

    public void setMessageDetails(MessageDetail[] messageDetailArr) {
        this.mMessageDetails = messageDetailArr;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mDetail);
        parcel.writeTypedArray(this.mMessageDetails, i);
    }
}
