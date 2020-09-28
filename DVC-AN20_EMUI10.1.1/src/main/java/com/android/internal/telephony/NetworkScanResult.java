package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.CellInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NetworkScanResult implements Parcelable {
    public static final Parcelable.Creator<NetworkScanResult> CREATOR = new Parcelable.Creator<NetworkScanResult>() {
        /* class com.android.internal.telephony.NetworkScanResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkScanResult createFromParcel(Parcel in) {
            return new NetworkScanResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public NetworkScanResult[] newArray(int size) {
            return new NetworkScanResult[size];
        }
    };
    public static final int SCAN_STATUS_COMPLETE = 2;
    public static final int SCAN_STATUS_PARTIAL = 1;
    public List<CellInfo> networkInfos;
    public int scanError;
    public int scanStatus;

    public NetworkScanResult(int scanStatus2, int scanError2, List<CellInfo> networkInfos2) {
        this.scanStatus = scanStatus2;
        this.scanError = scanError2;
        this.networkInfos = networkInfos2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.scanStatus);
        dest.writeInt(this.scanError);
        dest.writeParcelableList(this.networkInfos, flags);
    }

    private NetworkScanResult(Parcel in) {
        this.scanStatus = in.readInt();
        this.scanError = in.readInt();
        ArrayList arrayList = new ArrayList();
        in.readParcelableList(arrayList, Object.class.getClassLoader());
        this.networkInfos = arrayList;
    }

    public boolean equals(Object o) {
        try {
            NetworkScanResult nsr = (NetworkScanResult) o;
            if (o != null && this.scanStatus == nsr.scanStatus && this.scanError == nsr.scanError && this.networkInfos.equals(nsr.networkInfos)) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("scanStatus=" + this.scanStatus);
        sb.append(", scanError=" + this.scanError);
        sb.append(", networkInfos=" + this.networkInfos);
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return (this.scanStatus * 31) + (this.scanError * 23) + (Objects.hashCode(this.networkInfos) * 37);
    }
}
