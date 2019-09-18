package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class EncryptDataRequestParams extends RequestParams {
    public static final Parcelable.Creator<EncryptDataRequestParams> CREATOR = new Parcelable.Creator<EncryptDataRequestParams>() {
        public final EncryptDataRequestParams createFromParcel(Parcel parcel) {
            return new EncryptDataRequestParams(parcel);
        }

        public final EncryptDataRequestParams[] newArray(int i) {
            return new EncryptDataRequestParams[i];
        }
    };
    private List<String> mData;

    public EncryptDataRequestParams() {
    }

    public EncryptDataRequestParams(Parcel parcel) {
        super(parcel);
        this.mData = new ArrayList();
        parcel.readList(this.mData, ClassLoader.getSystemClassLoader());
    }

    public List<String> getData() {
        return this.mData;
    }

    public void setData(List<String> list) {
        this.mData = list;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeList(this.mData);
    }
}
