package com.android.internal.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

public class VpnInfo implements Parcelable {
    public static final Parcelable.Creator<VpnInfo> CREATOR = new Parcelable.Creator<VpnInfo>() {
        /* class com.android.internal.net.VpnInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VpnInfo createFromParcel(Parcel source) {
            VpnInfo info = new VpnInfo();
            info.ownerUid = source.readInt();
            info.vpnIface = source.readString();
            info.primaryUnderlyingIface = source.readString();
            return info;
        }

        @Override // android.os.Parcelable.Creator
        public VpnInfo[] newArray(int size) {
            return new VpnInfo[size];
        }
    };
    public int ownerUid;
    public String primaryUnderlyingIface;
    public String vpnIface;

    public String toString() {
        return "VpnInfo{ownerUid=" + this.ownerUid + ", vpnIface='" + this.vpnIface + DateFormat.QUOTE + ", primaryUnderlyingIface='" + this.primaryUnderlyingIface + DateFormat.QUOTE + '}';
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ownerUid);
        dest.writeString(this.vpnIface);
        dest.writeString(this.primaryUnderlyingIface);
    }
}
