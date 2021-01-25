package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class SmsRawData implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<SmsRawData> CREATOR = new Parcelable.Creator<SmsRawData>() {
        /* class com.android.internal.telephony.SmsRawData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SmsRawData createFromParcel(Parcel source) {
            int size = source.readInt();
            if (size > 255 || size <= 0) {
                return null;
            }
            byte[] data = new byte[size];
            source.readByteArray(data);
            return new SmsRawData(data);
        }

        @Override // android.os.Parcelable.Creator
        public SmsRawData[] newArray(int size) {
            return new SmsRawData[size];
        }
    };
    private static final int MAX_SMSRAW_DATA_LENGTH = 255;
    byte[] data;

    @UnsupportedAppUsage
    public SmsRawData(byte[] data2) {
        this.data = data2;
    }

    @UnsupportedAppUsage
    public byte[] getBytes() {
        return this.data;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.data.length);
        dest.writeByteArray(this.data);
    }
}
