package com.android.ims.internal.uce.presence;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class PresRlmiInfo implements Parcelable {
    public static final Parcelable.Creator<PresRlmiInfo> CREATOR = new Parcelable.Creator<PresRlmiInfo>() {
        /* class com.android.ims.internal.uce.presence.PresRlmiInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PresRlmiInfo createFromParcel(Parcel source) {
            return new PresRlmiInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public PresRlmiInfo[] newArray(int size) {
            return new PresRlmiInfo[size];
        }
    };
    private boolean mFullState;
    private String mListName;
    private PresSubscriptionState mPresSubscriptionState;
    private int mRequestId;
    private int mSubscriptionExpireTime;
    private String mSubscriptionTerminatedReason;
    private String mUri;
    private int mVersion;

    public String getUri() {
        return this.mUri;
    }

    @UnsupportedAppUsage
    public void setUri(String uri) {
        this.mUri = uri;
    }

    public int getVersion() {
        return this.mVersion;
    }

    @UnsupportedAppUsage
    public void setVersion(int version) {
        this.mVersion = version;
    }

    public boolean isFullState() {
        return this.mFullState;
    }

    @UnsupportedAppUsage
    public void setFullState(boolean fullState) {
        this.mFullState = fullState;
    }

    public String getListName() {
        return this.mListName;
    }

    @UnsupportedAppUsage
    public void setListName(String listName) {
        this.mListName = listName;
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    @UnsupportedAppUsage
    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    public PresSubscriptionState getPresSubscriptionState() {
        return this.mPresSubscriptionState;
    }

    @UnsupportedAppUsage
    public void setPresSubscriptionState(PresSubscriptionState presSubscriptionState) {
        this.mPresSubscriptionState = presSubscriptionState;
    }

    public int getSubscriptionExpireTime() {
        return this.mSubscriptionExpireTime;
    }

    @UnsupportedAppUsage
    public void setSubscriptionExpireTime(int subscriptionExpireTime) {
        this.mSubscriptionExpireTime = subscriptionExpireTime;
    }

    public String getSubscriptionTerminatedReason() {
        return this.mSubscriptionTerminatedReason;
    }

    @UnsupportedAppUsage
    public void setSubscriptionTerminatedReason(String subscriptionTerminatedReason) {
        this.mSubscriptionTerminatedReason = subscriptionTerminatedReason;
    }

    @UnsupportedAppUsage
    public PresRlmiInfo() {
        this.mUri = "";
        this.mListName = "";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUri);
        dest.writeInt(this.mVersion);
        dest.writeInt(this.mFullState ? 1 : 0);
        dest.writeString(this.mListName);
        dest.writeInt(this.mRequestId);
        dest.writeParcelable(this.mPresSubscriptionState, flags);
        dest.writeInt(this.mSubscriptionExpireTime);
        dest.writeString(this.mSubscriptionTerminatedReason);
    }

    private PresRlmiInfo(Parcel source) {
        this.mUri = "";
        this.mListName = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mUri = source.readString();
        this.mVersion = source.readInt();
        this.mFullState = source.readInt() != 0;
        this.mListName = source.readString();
        this.mRequestId = source.readInt();
        this.mPresSubscriptionState = (PresSubscriptionState) source.readParcelable(PresSubscriptionState.class.getClassLoader());
        this.mSubscriptionExpireTime = source.readInt();
        this.mSubscriptionTerminatedReason = source.readString();
    }
}
