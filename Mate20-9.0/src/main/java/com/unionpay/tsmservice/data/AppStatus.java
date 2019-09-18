package com.unionpay.tsmservice.data;

import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public class AppStatus implements Parcelable {
    public static final String APPLY = "06";
    public static final String APPROVED = "00";
    public static final String APPROVING = "02";
    public static final Parcelable.Creator<AppStatus> CREATOR = new Parcelable.Creator<AppStatus>() {
        public final AppStatus createFromParcel(Parcel parcel) {
            return new AppStatus(parcel);
        }

        public final AppStatus[] newArray(int i) {
            return new AppStatus[i];
        }
    };
    public static final String NOT_APPROVED = "01";
    public static final String OPEN = "05";
    public static final String VIEW = "07";
    private String mStatus = "";

    public AppStatus() {
    }

    public AppStatus(Parcel parcel) {
        this.mStatus = parcel.readString();
    }

    public AppStatus(String str) {
        this.mStatus = str;
    }

    public int describeContents() {
        return 0;
    }

    public String getStatus() {
        return this.mStatus;
    }

    public void setStatus(String str) {
        this.mStatus = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mStatus);
    }
}
