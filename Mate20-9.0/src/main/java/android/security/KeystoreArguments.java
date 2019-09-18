package android.security;

import android.os.Parcel;
import android.os.Parcelable;

public class KeystoreArguments implements Parcelable {
    public static final Parcelable.Creator<KeystoreArguments> CREATOR = new Parcelable.Creator<KeystoreArguments>() {
        public KeystoreArguments createFromParcel(Parcel in) {
            return new KeystoreArguments(in);
        }

        public KeystoreArguments[] newArray(int size) {
            return new KeystoreArguments[size];
        }
    };
    public byte[][] args;

    public KeystoreArguments() {
        this.args = null;
    }

    public KeystoreArguments(byte[][] args2) {
        this.args = args2;
    }

    private KeystoreArguments(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.args == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.args.length);
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

    public int describeContents() {
        return 0;
    }
}
