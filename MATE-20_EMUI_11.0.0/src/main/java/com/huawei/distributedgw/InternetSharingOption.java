package com.huawei.distributedgw;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class InternetSharingOption implements Parcelable {
    public static final Parcelable.Creator<InternetSharingOption> CREATOR = new Parcelable.Creator<InternetSharingOption>() {
        /* class com.huawei.distributedgw.InternetSharingOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InternetSharingOption createFromParcel(Parcel in) {
            return new InternetSharingOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public InternetSharingOption[] newArray(int size) {
            return new InternetSharingOption[size];
        }
    };
    private boolean mProxyArp;
    private boolean mProxyDns;

    public InternetSharingOption() {
        this(false, false);
    }

    public InternetSharingOption(boolean proxyArp, boolean proxyDns) {
        this.mProxyArp = proxyArp;
        this.mProxyDns = proxyDns;
    }

    public InternetSharingOption(Parcel in) {
        if (in != null) {
            boolean z = false;
            this.mProxyArp = in.readInt() == 1;
            this.mProxyDns = in.readInt() == 1 ? true : z;
        }
    }

    public boolean getProxyArp() {
        return this.mProxyArp;
    }

    public boolean getProxyDns() {
        return this.mProxyDns;
    }

    public InternetSharingOption setProxyArp(boolean proxyArp) {
        this.mProxyArp = proxyArp;
        return this;
    }

    public InternetSharingOption setProxyDns(boolean proxyDns) {
        this.mProxyDns = proxyDns;
        return this;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mProxyArp ? 1 : 0);
        parcel.writeInt(this.mProxyDns ? 1 : 0);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof InternetSharingOption)) {
            return false;
        }
        InternetSharingOption that = (InternetSharingOption) obj;
        if (that.mProxyArp == this.mProxyArp && that.mProxyDns == this.mProxyDns) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.mProxyArp), Boolean.valueOf(this.mProxyDns));
    }

    @Override // java.lang.Object
    public String toString() {
        return "[ mProxyArp=" + this.mProxyArp + " mProxyDns=" + this.mProxyDns + " ]";
    }
}
