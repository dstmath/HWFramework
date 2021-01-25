package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class CallForwardInfo implements Parcelable {
    public static final Parcelable.Creator<CallForwardInfo> CREATOR = new Parcelable.Creator<CallForwardInfo>() {
        /* class com.android.internal.telephony.CallForwardInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CallForwardInfo createFromParcel(Parcel source) {
            CallForwardInfo cFInfor = new CallForwardInfo();
            cFInfor.readFromParcel(source);
            return cFInfor;
        }

        @Override // android.os.Parcelable.Creator
        public CallForwardInfo[] newArray(int size) {
            return new CallForwardInfo[size];
        }
    };
    private static final String TAG = "CallForwardInfo";
    public int endHour;
    public int endMinute;
    @UnsupportedAppUsage
    public String number;
    @UnsupportedAppUsage
    public int reason;
    @UnsupportedAppUsage
    public int serviceClass;
    public int startHour;
    public int startMinute;
    @UnsupportedAppUsage
    public int status;
    @UnsupportedAppUsage
    public int timeSeconds;
    @UnsupportedAppUsage
    public int toa;

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[CallForwardInfo: status=");
        sb.append(this.status == 0 ? " not active " : " active ");
        sb.append(" reason: ");
        sb.append(this.reason);
        sb.append(" serviceClass: ");
        sb.append(this.serviceClass);
        sb.append(this.timeSeconds);
        sb.append(" seconds, startHour=");
        sb.append(this.startHour);
        sb.append(", startMinute=");
        sb.append(this.startMinute);
        sb.append(", endHour=");
        sb.append(this.endHour);
        sb.append(", endMinute=");
        sb.append(this.endMinute);
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.reason);
        dest.writeInt(this.serviceClass);
        dest.writeInt(this.toa);
        dest.writeString(this.number);
        dest.writeInt(this.timeSeconds);
        dest.writeInt(this.startHour);
        dest.writeInt(this.startMinute);
        dest.writeInt(this.endHour);
        dest.writeInt(this.endMinute);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readFromParcel(Parcel source) {
        this.status = source.readInt();
        this.reason = source.readInt();
        this.serviceClass = source.readInt();
        this.toa = source.readInt();
        this.number = source.readString();
        this.timeSeconds = source.readInt();
        this.startHour = source.readInt();
        this.startMinute = source.readInt();
        this.endHour = source.readInt();
        this.endMinute = source.readInt();
    }
}
