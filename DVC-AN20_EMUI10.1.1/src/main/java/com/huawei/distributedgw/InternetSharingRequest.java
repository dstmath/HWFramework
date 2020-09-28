package com.huawei.distributedgw;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class InternetSharingRequest implements Parcelable {
    public static final Parcelable.Creator<InternetSharingRequest> CREATOR = new Parcelable.Creator<InternetSharingRequest>() {
        /* class com.huawei.distributedgw.InternetSharingRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InternetSharingRequest createFromParcel(Parcel in) {
            return new InternetSharingRequest(in);
        }

        @Override // android.os.Parcelable.Creator
        public InternetSharingRequest[] newArray(int size) {
            return new InternetSharingRequest[size];
        }
    };
    private int mDeviceType;
    private String mEntryIfaceName;
    private InternetSharingOption mOption;

    protected InternetSharingRequest() {
        this(2, null);
    }

    protected InternetSharingRequest(int deviceType, String entryIfaceName) {
        this.mDeviceType = deviceType;
        this.mEntryIfaceName = entryIfaceName;
        this.mOption = new InternetSharingOption();
        int i = this.mDeviceType;
        if (i == 2) {
            this.mOption.setProxyArp(true).setProxyDns(true);
        } else if (i == 1) {
            this.mOption.setProxyArp(false).setProxyDns(true);
        } else {
            this.mOption.setProxyArp(false).setProxyDns(false);
        }
    }

    public InternetSharingRequest(Parcel in) {
        if (in != null) {
            this.mDeviceType = in.readInt();
            this.mEntryIfaceName = in.readString();
            this.mOption = InternetSharingOption.CREATOR.createFromParcel(in);
        }
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public String getEntryIfaceName() {
        return this.mEntryIfaceName;
    }

    public InternetSharingOption getInternetSharingOption() {
        return this.mOption;
    }

    public static class Builder {
        private InternetSharingRequest mObject = new InternetSharingRequest();

        public Builder setDeviceType(int deviceType) {
            getObject().mDeviceType = deviceType;
            return this;
        }

        public Builder setEntryIfaceName(String entryIfaceName) {
            getObject().mEntryIfaceName = entryIfaceName;
            return this;
        }

        /* access modifiers changed from: protected */
        public InternetSharingRequest getObject() {
            return this.mObject;
        }

        public InternetSharingRequest build() {
            return getObject();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mDeviceType);
        parcel.writeString(this.mEntryIfaceName);
        this.mOption.writeToParcel(parcel, flags);
    }

    public String toString() {
        return "GatewayRequest [ mDeviceType=" + this.mDeviceType + " mEntryIfaceName=" + this.mEntryIfaceName + " mOption=" + this.mOption.toString() + " ]";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InternetSharingRequest)) {
            return false;
        }
        InternetSharingRequest that = (InternetSharingRequest) obj;
        if (that.mDeviceType != this.mDeviceType || !Objects.equals(that.mEntryIfaceName, this.mEntryIfaceName) || !Objects.equals(that.mOption, this.mOption)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mDeviceType), this.mEntryIfaceName, this.mOption);
    }
}
