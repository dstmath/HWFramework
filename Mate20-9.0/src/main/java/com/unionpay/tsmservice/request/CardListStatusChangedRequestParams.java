package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class CardListStatusChangedRequestParams extends RequestParams {
    public static final Parcelable.Creator<CardListStatusChangedRequestParams> CREATOR = new Parcelable.Creator<CardListStatusChangedRequestParams>() {
        public final CardListStatusChangedRequestParams createFromParcel(Parcel parcel) {
            return new CardListStatusChangedRequestParams(parcel);
        }

        public final CardListStatusChangedRequestParams[] newArray(int i) {
            return new CardListStatusChangedRequestParams[i];
        }
    };

    public CardListStatusChangedRequestParams() {
    }

    public CardListStatusChangedRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
