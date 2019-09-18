package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.data.TransactionDetail;

public class TransactionDetailsResult implements Parcelable {
    public static final Parcelable.Creator<TransactionDetailsResult> CREATOR = new Parcelable.Creator<TransactionDetailsResult>() {
        public final TransactionDetailsResult createFromParcel(Parcel parcel) {
            return new TransactionDetailsResult(parcel);
        }

        public final TransactionDetailsResult[] newArray(int i) {
            return new TransactionDetailsResult[i];
        }
    };
    private String mLastUpdatedTag = "";
    private TransactionDetail[] mTransactionDetails;

    public TransactionDetailsResult() {
    }

    public TransactionDetailsResult(Parcel parcel) {
        this.mTransactionDetails = (TransactionDetail[]) parcel.createTypedArray(TransactionDetail.CREATOR);
        this.mLastUpdatedTag = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getLastUpdatedTag() {
        return this.mLastUpdatedTag;
    }

    public TransactionDetail[] getTransactionDetails() {
        return this.mTransactionDetails;
    }

    public void setLastUpdatedTag(String str) {
        this.mLastUpdatedTag = str;
    }

    public void setTransactionDetails(TransactionDetail[] transactionDetailArr) {
        this.mTransactionDetails = transactionDetailArr;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedArray(this.mTransactionDetails, i);
        parcel.writeString(this.mLastUpdatedTag);
    }
}
