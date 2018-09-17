package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ResultStorageDescriptor implements Parcelable {
    public static final Creator<ResultStorageDescriptor> CREATOR = new Creator<ResultStorageDescriptor>() {
        public ResultStorageDescriptor createFromParcel(Parcel source) {
            return new ResultStorageDescriptor(source, null);
        }

        public ResultStorageDescriptor[] newArray(int size) {
            return new ResultStorageDescriptor[size];
        }
    };
    private int mLength;
    private int mOffset;
    private int mType;

    /* synthetic */ ResultStorageDescriptor(Parcel in, ResultStorageDescriptor -this1) {
        this(in);
    }

    public int getType() {
        return this.mType;
    }

    public int getOffset() {
        return this.mOffset;
    }

    public int getLength() {
        return this.mLength;
    }

    public ResultStorageDescriptor(int type, int offset, int length) {
        this.mType = type;
        this.mOffset = offset;
        this.mLength = length;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeInt(this.mOffset);
        dest.writeInt(this.mLength);
    }

    private ResultStorageDescriptor(Parcel in) {
        ReadFromParcel(in);
    }

    private void ReadFromParcel(Parcel in) {
        this.mType = in.readInt();
        this.mOffset = in.readInt();
        this.mLength = in.readInt();
    }
}
