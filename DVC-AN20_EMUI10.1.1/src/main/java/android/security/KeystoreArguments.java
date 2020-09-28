package android.security;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class KeystoreArguments implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<KeystoreArguments> CREATOR = new Parcelable.Creator<KeystoreArguments>() {
        /* class android.security.KeystoreArguments.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KeystoreArguments createFromParcel(Parcel in) {
            return new KeystoreArguments(in);
        }

        @Override // android.os.Parcelable.Creator
        public KeystoreArguments[] newArray(int size) {
            return new KeystoreArguments[size];
        }
    };
    public byte[][] args;

    public KeystoreArguments() {
        this.args = null;
    }

    @UnsupportedAppUsage
    public KeystoreArguments(byte[][] args2) {
        this.args = args2;
    }

    private KeystoreArguments(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        byte[][] bArr = this.args;
        if (bArr == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(bArr.length);
        for (byte[] arg : this.args) {
            out.writeByteArray(arg);
        }
    }

    private void readFromParcel(Parcel in) {
        int length = in.readInt();
        this.args = new byte[length][];
        for (int i = 0; i < length; i++) {
            this.args[i] = in.createByteArray();
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
