package com.huawei.permission;

import android.os.Parcel;
import android.os.Parcelable;

public class MaliInfoBean implements Parcelable {
    public static final Parcelable.Creator<MaliInfoBean> CREATOR = new Parcelable.Creator<MaliInfoBean>() {
        /* class com.huawei.permission.MaliInfoBean.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MaliInfoBean createFromParcel(Parcel in) {
            return new MaliInfoBean(in);
        }

        @Override // android.os.Parcelable.Creator
        public MaliInfoBean[] newArray(int size) {
            return new MaliInfoBean[size];
        }
    };
    public static final int NOT_RESTRICTED = 1;
    public static final int RESTRICTED = 0;
    public static final int RISK_HIGH = 3;
    public static final int RISK_LOW = 1;
    public static final int RISK_MEDIUM = 2;
    public static final int RISK_NONE = 0;
    public static final int RISK_UNKNOWN = 4;
    public String appId;
    public int category;
    public String reportSource;
    public int restrictStatus;
    public int riskLevel;

    public MaliInfoBean(String appId2, String reportSource2, int riskLevel2, int category2, int restrictStatus2) {
        this.appId = appId2;
        this.reportSource = reportSource2;
        this.riskLevel = riskLevel2;
        this.category = category2;
        this.restrictStatus = restrictStatus2;
    }

    private MaliInfoBean(Parcel in) {
        this.appId = in.readString();
        this.reportSource = in.readString();
        this.riskLevel = in.readInt();
        this.category = in.readInt();
        this.restrictStatus = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.appId);
        out.writeString(this.reportSource);
        out.writeInt(this.riskLevel);
        out.writeInt(this.category);
        out.writeInt(this.restrictStatus);
    }

    public String toString() {
        return "{ appId = " + this.appId + ", reportSource = " + this.reportSource + ", riskLevel = " + this.riskLevel + ", category = " + this.category + ", restrict = " + this.restrictStatus + " }";
    }
}
