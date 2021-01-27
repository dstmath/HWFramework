package com.android.ims.internal.uce.presence;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class PresTupleInfo implements Parcelable {
    public static final Parcelable.Creator<PresTupleInfo> CREATOR = new Parcelable.Creator<PresTupleInfo>() {
        /* class com.android.ims.internal.uce.presence.PresTupleInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PresTupleInfo createFromParcel(Parcel source) {
            return new PresTupleInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public PresTupleInfo[] newArray(int size) {
            return new PresTupleInfo[size];
        }
    };
    private String mContactUri;
    private String mFeatureTag;
    private String mTimestamp;

    public String getFeatureTag() {
        return this.mFeatureTag;
    }

    @UnsupportedAppUsage
    public void setFeatureTag(String featureTag) {
        this.mFeatureTag = featureTag;
    }

    public String getContactUri() {
        return this.mContactUri;
    }

    @UnsupportedAppUsage
    public void setContactUri(String contactUri) {
        this.mContactUri = contactUri;
    }

    public String getTimestamp() {
        return this.mTimestamp;
    }

    @UnsupportedAppUsage
    public void setTimestamp(String timestamp) {
        this.mTimestamp = timestamp;
    }

    @UnsupportedAppUsage
    public PresTupleInfo() {
        this.mFeatureTag = "";
        this.mContactUri = "";
        this.mTimestamp = "";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFeatureTag);
        dest.writeString(this.mContactUri);
        dest.writeString(this.mTimestamp);
    }

    private PresTupleInfo(Parcel source) {
        this.mFeatureTag = "";
        this.mContactUri = "";
        this.mTimestamp = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mFeatureTag = source.readString();
        this.mContactUri = source.readString();
        this.mTimestamp = source.readString();
    }
}
