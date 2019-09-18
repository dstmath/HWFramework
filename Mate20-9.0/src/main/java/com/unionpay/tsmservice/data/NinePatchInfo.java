package com.unionpay.tsmservice.data;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class NinePatchInfo implements Parcelable {
    public static final Parcelable.Creator<NinePatchInfo> CREATOR = new Parcelable.Creator<NinePatchInfo>() {
        public final NinePatchInfo createFromParcel(Parcel parcel) {
            return new NinePatchInfo(parcel);
        }

        public final NinePatchInfo[] newArray(int i) {
            return new NinePatchInfo[i];
        }
    };
    private Bitmap mBitmap;
    private byte[] mChunk;
    private Rect mPadding;

    public NinePatchInfo() {
    }

    public NinePatchInfo(Parcel parcel) {
        this.mBitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.mPadding = (Rect) parcel.readParcelable(Rect.class.getClassLoader());
        int readInt = parcel.readInt();
        if (readInt > 0) {
            this.mChunk = new byte[readInt];
            parcel.readByteArray(this.mChunk);
            return;
        }
        this.mChunk = null;
    }

    public int describeContents() {
        return 0;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public byte[] getChunk() {
        return this.mChunk;
    }

    public Rect getPadding() {
        return this.mPadding;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void setChunk(byte[] bArr) {
        this.mChunk = bArr;
    }

    public void setPadding(Rect rect) {
        this.mPadding = rect;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mBitmap, i);
        parcel.writeParcelable(this.mPadding, i);
        if (this.mChunk != null) {
            parcel.writeInt(this.mChunk.length);
            parcel.writeByteArray(this.mChunk);
        }
    }
}
