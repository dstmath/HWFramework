package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.data.SeAppListItem;

public class GetSeAppListResult implements Parcelable {
    public static final Parcelable.Creator<GetSeAppListResult> CREATOR = new Parcelable.Creator<GetSeAppListResult>() {
        public final GetSeAppListResult createFromParcel(Parcel parcel) {
            return new GetSeAppListResult(parcel);
        }

        public final GetSeAppListResult[] newArray(int i) {
            return new GetSeAppListResult[i];
        }
    };
    private String mSeAliasType = "";
    private SeAppListItem[] mSeAppList;

    public GetSeAppListResult() {
    }

    public GetSeAppListResult(Parcel parcel) {
        this.mSeAppList = (SeAppListItem[]) parcel.createTypedArray(SeAppListItem.CREATOR);
        this.mSeAliasType = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getSeAliasType() {
        return this.mSeAliasType;
    }

    public SeAppListItem[] getSeAppList() {
        return this.mSeAppList;
    }

    public void setSeAliasType(String str) {
        this.mSeAliasType = str;
    }

    public void setSeAppList(SeAppListItem[] seAppListItemArr) {
        this.mSeAppList = seAppListItemArr;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedArray(this.mSeAppList, i);
        parcel.writeString(this.mSeAliasType);
    }
}
