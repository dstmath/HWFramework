package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PresPublishTriggerType implements Parcelable {
    public static final Creator<PresPublishTriggerType> CREATOR = new Creator<PresPublishTriggerType>() {
        public PresPublishTriggerType createFromParcel(Parcel source) {
            return new PresPublishTriggerType(source, null);
        }

        public PresPublishTriggerType[] newArray(int size) {
            return new PresPublishTriggerType[size];
        }
    };
    public static final int UCE_PRES_PUBLISH_TRIGGER_ETAG_EXPIRED = 0;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_2G = 6;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_3G = 5;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_EHRPD = 3;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_HSPAPLUS = 4;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_IWLAN = 8;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_LTE_VOPS_DISABLED = 1;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_LTE_VOPS_ENABLED = 2;
    public static final int UCE_PRES_PUBLISH_TRIGGER_MOVE_TO_WLAN = 7;
    public static final int UCE_PRES_PUBLISH_TRIGGER_UNKNOWN = 9;
    private int mPublishTriggerType;

    /* synthetic */ PresPublishTriggerType(Parcel source, PresPublishTriggerType -this1) {
        this(source);
    }

    public int getPublishTrigeerType() {
        return this.mPublishTriggerType;
    }

    public void setPublishTrigeerType(int nPublishTriggerType) {
        this.mPublishTriggerType = nPublishTriggerType;
    }

    public PresPublishTriggerType() {
        this.mPublishTriggerType = 9;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPublishTriggerType);
    }

    private PresPublishTriggerType(Parcel source) {
        this.mPublishTriggerType = 9;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mPublishTriggerType = source.readInt();
    }
}
