package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.data.Amount;

public class GetCardInfoBySpayRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetCardInfoBySpayRequestParams> CREATOR = new Parcelable.Creator<GetCardInfoBySpayRequestParams>() {
        public final GetCardInfoBySpayRequestParams createFromParcel(Parcel parcel) {
            return new GetCardInfoBySpayRequestParams(parcel);
        }

        public final GetCardInfoBySpayRequestParams[] newArray(int i) {
            return new GetCardInfoBySpayRequestParams[i];
        }
    };
    private Amount mAmount;

    public GetCardInfoBySpayRequestParams() {
    }

    public GetCardInfoBySpayRequestParams(Parcel parcel) {
        super(parcel);
        this.mAmount = (Amount) parcel.readParcelable(Amount.class.getClassLoader());
    }

    public Amount getAmount() {
        return this.mAmount;
    }

    public void setAmount(Amount amount) {
        this.mAmount = amount;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.mAmount, i);
    }
}
