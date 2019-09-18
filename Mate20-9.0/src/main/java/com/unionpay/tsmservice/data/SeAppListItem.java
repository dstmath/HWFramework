package com.unionpay.tsmservice.data;

import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public class SeAppListItem implements Parcelable {
    public static final Parcelable.Creator<SeAppListItem> CREATOR = new Parcelable.Creator<SeAppListItem>() {
        public final SeAppListItem createFromParcel(Parcel parcel) {
            return new SeAppListItem(parcel);
        }

        public final SeAppListItem[] newArray(int i) {
            return new SeAppListItem[i];
        }
    };
    private AppDetail mAppDetail;

    public SeAppListItem() {
    }

    public SeAppListItem(Parcel parcel) {
        this.mAppDetail = (AppDetail) parcel.readParcelable(AppDetail.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public AppDetail getAppDetail() {
        return this.mAppDetail;
    }

    public void setAppDetail(AppDetail appDetail) {
        this.mAppDetail = appDetail;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mAppDetail, i);
    }
}
