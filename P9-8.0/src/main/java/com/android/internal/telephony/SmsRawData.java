package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmsRawData implements Parcelable {
    public static final Creator<SmsRawData> CREATOR = new Creator<SmsRawData>() {
        public SmsRawData createFromParcel(Parcel source) {
            byte[] data = new byte[source.readInt()];
            source.readByteArray(data);
            return new SmsRawData(data);
        }

        public SmsRawData[] newArray(int size) {
            return new SmsRawData[size];
        }
    };
    byte[] data;

    public SmsRawData(byte[] data) {
        this.data = data;
    }

    public byte[] getBytes() {
        return this.data;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.data.length);
        dest.writeByteArray(this.data);
    }
}
