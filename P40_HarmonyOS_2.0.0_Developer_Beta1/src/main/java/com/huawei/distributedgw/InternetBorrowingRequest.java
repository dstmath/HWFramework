package com.huawei.distributedgw;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class InternetBorrowingRequest implements Parcelable {
    public static final Parcelable.Creator<InternetBorrowingRequest> CREATOR = new Parcelable.Creator<InternetBorrowingRequest>() {
        /* class com.huawei.distributedgw.InternetBorrowingRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InternetBorrowingRequest createFromParcel(Parcel in) {
            return new InternetBorrowingRequest(in);
        }

        @Override // android.os.Parcelable.Creator
        public InternetBorrowingRequest[] newArray(int size) {
            return new InternetBorrowingRequest[size];
        }
    };
    private String mIfaceName;
    private String mRouteIp;
    private String mServiceName;

    protected InternetBorrowingRequest() {
        this(null, null);
    }

    protected InternetBorrowingRequest(String ifaceName, String routeIp) {
        this.mIfaceName = ifaceName;
        this.mRouteIp = routeIp;
    }

    public InternetBorrowingRequest(Parcel in) {
        if (in != null) {
            this.mIfaceName = in.readString();
            this.mRouteIp = in.readString();
            this.mServiceName = in.readString();
        }
    }

    public String getIfaceName() {
        return this.mIfaceName;
    }

    public String getRouteIp() {
        return this.mRouteIp;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public static class Builder {
        private InternetBorrowingRequest mObject = new InternetBorrowingRequest();

        public Builder setEntryIfaceName(String ifaceName) {
            getObject().mIfaceName = ifaceName;
            return this;
        }

        public Builder setRouteIp(String routeIp) {
            getObject().mRouteIp = routeIp;
            return this;
        }

        public Builder setServiceName(String serviceName) {
            getObject().mServiceName = serviceName;
            return this;
        }

        /* access modifiers changed from: protected */
        public InternetBorrowingRequest getObject() {
            return this.mObject;
        }

        public InternetBorrowingRequest build() {
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
            parcel.writeString(this.mIfaceName);
            parcel.writeString(this.mRouteIp);
            parcel.writeString(this.mServiceName);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "gateway borrow [ mIfaceName=" + this.mIfaceName + " mServiceName=" + this.mServiceName + " ]";
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof InternetBorrowingRequest)) {
            return false;
        }
        InternetBorrowingRequest that = (InternetBorrowingRequest) obj;
        if (!Objects.equals(that.mIfaceName, this.mIfaceName) || !Objects.equals(that.mRouteIp, this.mRouteIp)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.mIfaceName, this.mRouteIp);
    }
}
