package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;

public class PresRlmiInfo implements Parcelable {
    public static final Creator<PresRlmiInfo> CREATOR = new Creator<PresRlmiInfo>() {
        public PresRlmiInfo createFromParcel(Parcel source) {
            return new PresRlmiInfo(source, null);
        }

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

    /* synthetic */ PresRlmiInfo(Parcel source, PresRlmiInfo -this1) {
        this(source);
    }

    public String getUri() {
        return this.mUri;
    }

    public void setUri(String uri) {
        this.mUri = uri;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public boolean isFullState() {
        return this.mFullState;
    }

    public void setFullState(boolean fullState) {
        this.mFullState = fullState;
    }

    public String getListName() {
        return this.mListName;
    }

    public void setListName(String listName) {
        this.mListName = listName;
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    public PresSubscriptionState getPresSubscriptionState() {
        return this.mPresSubscriptionState;
    }

    public void setPresSubscriptionState(PresSubscriptionState presSubscriptionState) {
        this.mPresSubscriptionState = presSubscriptionState;
    }

    public int getSubscriptionExpireTime() {
        return this.mSubscriptionExpireTime;
    }

    public void setSubscriptionExpireTime(int subscriptionExpireTime) {
        this.mSubscriptionExpireTime = subscriptionExpireTime;
    }

    public String getSubscriptionTerminatedReason() {
        return this.mSubscriptionTerminatedReason;
    }

    public void setSubscriptionTerminatedReason(String subscriptionTerminatedReason) {
        this.mSubscriptionTerminatedReason = subscriptionTerminatedReason;
    }

    public PresRlmiInfo() {
        this.mUri = LogException.NO_VALUE;
        this.mListName = LogException.NO_VALUE;
    }

    public int describeContents() {
        return 0;
    }

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
        this.mUri = LogException.NO_VALUE;
        this.mListName = LogException.NO_VALUE;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        boolean z = false;
        this.mUri = source.readString();
        this.mVersion = source.readInt();
        if (source.readInt() != 0) {
            z = true;
        }
        this.mFullState = z;
        this.mListName = source.readString();
        this.mRequestId = source.readInt();
        this.mPresSubscriptionState = (PresSubscriptionState) source.readParcelable(PresSubscriptionState.class.getClassLoader());
        this.mSubscriptionExpireTime = source.readInt();
        this.mSubscriptionTerminatedReason = source.readString();
    }
}
