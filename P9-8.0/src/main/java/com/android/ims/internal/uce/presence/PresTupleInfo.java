package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;

public class PresTupleInfo implements Parcelable {
    public static final Creator<PresTupleInfo> CREATOR = new Creator<PresTupleInfo>() {
        public PresTupleInfo createFromParcel(Parcel source) {
            return new PresTupleInfo(source, null);
        }

        public PresTupleInfo[] newArray(int size) {
            return new PresTupleInfo[size];
        }
    };
    private String mContactUri;
    private String mFeatureTag;
    private String mTimestamp;

    /* synthetic */ PresTupleInfo(Parcel source, PresTupleInfo -this1) {
        this(source);
    }

    public String getFeatureTag() {
        return this.mFeatureTag;
    }

    public void setFeatureTag(String featureTag) {
        this.mFeatureTag = featureTag;
    }

    public String getContactUri() {
        return this.mContactUri;
    }

    public void setContactUri(String contactUri) {
        this.mContactUri = contactUri;
    }

    public String getTimestamp() {
        return this.mTimestamp;
    }

    public void setTimestamp(String timestamp) {
        this.mTimestamp = timestamp;
    }

    public PresTupleInfo() {
        this.mFeatureTag = LogException.NO_VALUE;
        this.mContactUri = LogException.NO_VALUE;
        this.mTimestamp = LogException.NO_VALUE;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFeatureTag);
        dest.writeString(this.mContactUri);
        dest.writeString(this.mTimestamp);
    }

    private PresTupleInfo(Parcel source) {
        this.mFeatureTag = LogException.NO_VALUE;
        this.mContactUri = LogException.NO_VALUE;
        this.mTimestamp = LogException.NO_VALUE;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mFeatureTag = source.readString();
        this.mContactUri = source.readString();
        this.mTimestamp = source.readString();
    }
}
