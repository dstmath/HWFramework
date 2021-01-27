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
    private String mExitIfaceName;
    private int mExitIfaceType;
    private InternetSharingOption mOption;
    private String mRequestIp;

    protected InternetSharingRequest() {
        this(2, null);
    }

    protected InternetSharingRequest(int deviceType, String entryIfaceName) {
        this.mExitIfaceType = -1;
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
        this.mExitIfaceType = -1;
        if (in != null) {
            this.mDeviceType = in.readInt();
            this.mEntryIfaceName = in.readString();
            this.mExitIfaceName = in.readString();
            this.mExitIfaceType = in.readInt();
            this.mRequestIp = in.readString();
            this.mOption = InternetSharingOption.CREATOR.createFromParcel(in);
        }
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public String getEntryIfaceName() {
        return this.mEntryIfaceName;
    }

    public void setExitIfaceName(String exitIfaceName) {
        this.mExitIfaceName = exitIfaceName;
    }

    public String getExitIfaceName() {
        return this.mExitIfaceName;
    }

    public int getExitIfaceType() {
        return this.mExitIfaceType;
    }

    public String getRequestIp() {
        return this.mRequestIp;
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

        public Builder setExitIfaceName(String exitIfaceName) {
            getObject().mExitIfaceName = exitIfaceName;
            return this;
        }

        public Builder setExitIfaceType(int exitIfaceType) {
            getObject().mExitIfaceType = exitIfaceType;
            return this;
        }

        public Builder setRequestIp(String requestIp) {
            getObject().mRequestIp = requestIp;
            return this;
        }

        public Builder setOption(InternetSharingOption option) {
            getObject().mOption = option;
            return this;
        }

        public Builder setOption(int deviceType) {
            getObject().mOption = new InternetSharingOption();
            if (deviceType == 2) {
                getObject().mOption.setProxyArp(true).setProxyDns(true);
            } else if (deviceType == 1) {
                getObject().mOption.setProxyArp(false).setProxyDns(true);
            } else {
                getObject().mOption.setProxyArp(false).setProxyDns(false);
            }
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        if (parcel != null) {
            parcel.writeInt(this.mDeviceType);
            parcel.writeString(this.mEntryIfaceName);
            parcel.writeString(this.mExitIfaceName);
            parcel.writeInt(this.mExitIfaceType);
            parcel.writeString(this.mRequestIp);
            this.mOption.writeToParcel(parcel, flags);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "GatewayRequest [ mDeviceType=" + this.mDeviceType + " mEntryIfaceName=" + this.mEntryIfaceName + " mExitIfaceName=" + this.mExitIfaceName + " mOption=" + this.mOption.toString() + " ]";
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof InternetSharingRequest)) {
            return false;
        }
        InternetSharingRequest that = (InternetSharingRequest) obj;
        if (that.mDeviceType != this.mDeviceType || !Objects.equals(that.mEntryIfaceName, this.mEntryIfaceName) || !Objects.equals(that.mExitIfaceName, this.mExitIfaceName) || that.mExitIfaceType != this.mExitIfaceType || !Objects.equals(that.mRequestIp, this.mRequestIp) || !Objects.equals(that.mOption, this.mOption)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mDeviceType), this.mEntryIfaceName, this.mExitIfaceName, Integer.valueOf(this.mExitIfaceType), this.mRequestIp, this.mOption);
    }
}
