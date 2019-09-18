package com.huawei.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;

public class HwExportResult implements Parcelable {
    public static final Parcelable.Creator<HwExportResult> CREATOR = new Parcelable.Creator<HwExportResult>() {
        public HwExportResult createFromParcel(Parcel in) {
            return new HwExportResult(in);
        }

        public HwExportResult[] newArray(int length) {
            return new HwExportResult[length];
        }
    };
    public final byte[] exportData;
    public final int resultCode;

    protected HwExportResult(Parcel in) {
        this.resultCode = in.readInt();
        this.exportData = in.createByteArray();
    }

    public HwExportResult(int i, byte[] b) {
        this.resultCode = i;
        this.exportData = b;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.resultCode);
        out.writeByteArray(this.exportData);
    }
}
