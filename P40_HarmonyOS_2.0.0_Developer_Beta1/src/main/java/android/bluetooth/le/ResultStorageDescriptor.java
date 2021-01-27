package android.bluetooth.le;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class ResultStorageDescriptor implements Parcelable {
    public static final Parcelable.Creator<ResultStorageDescriptor> CREATOR = new Parcelable.Creator<ResultStorageDescriptor>() {
        /* class android.bluetooth.le.ResultStorageDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResultStorageDescriptor createFromParcel(Parcel source) {
            return new ResultStorageDescriptor(source);
        }

        @Override // android.os.Parcelable.Creator
        public ResultStorageDescriptor[] newArray(int size) {
            return new ResultStorageDescriptor[size];
        }
    };
    private int mLength;
    private int mOffset;
    private int mType;

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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
