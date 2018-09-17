package com.huawei.IntelliServer.intellilib;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IntelliImgDescriptor implements Parcelable {
    public static final Creator<IntelliImgDescriptor> CREATOR = new Creator<IntelliImgDescriptor>() {
        public IntelliImgDescriptor createFromParcel(Parcel in) {
            return new IntelliImgDescriptor(in);
        }

        public IntelliImgDescriptor[] newArray(int size) {
            return new IntelliImgDescriptor[size];
        }
    };
    public byte[] data;
    public int format;
    public int height;
    public int size;
    public int width;

    public IntelliImgDescriptor(int w, int h, int imgFormat, byte[] img) {
        this.format = imgFormat;
        this.width = w;
        this.height = h;
        this.data = img;
    }

    public IntelliImgDescriptor(Parcel in) {
        this.format = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.size = in.readInt();
        this.data = new byte[this.size];
        in.readByteArray(this.data);
    }

    public void setTimestamp(long timestamp) {
        throw new RuntimeException("Stub!");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeInt(this.format);
        parcel.writeInt(this.width);
        parcel.writeInt(this.height);
        parcel.writeInt(this.data.length);
        parcel.writeByteArray(this.data);
    }
}
