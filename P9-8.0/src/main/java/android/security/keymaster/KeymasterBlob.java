package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class KeymasterBlob implements Parcelable {
    public static final Creator<KeymasterBlob> CREATOR = new Creator<KeymasterBlob>() {
        public KeymasterBlob createFromParcel(Parcel in) {
            return new KeymasterBlob(in);
        }

        public KeymasterBlob[] newArray(int length) {
            return new KeymasterBlob[length];
        }
    };
    public byte[] blob;

    public KeymasterBlob(byte[] blob) {
        this.blob = blob;
    }

    protected KeymasterBlob(Parcel in) {
        this.blob = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.blob);
    }
}
