package android.security;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class KeystoreArguments implements Parcelable {
    public static final Creator<KeystoreArguments> CREATOR = new Creator<KeystoreArguments>() {
        public KeystoreArguments createFromParcel(Parcel in) {
            return new KeystoreArguments(in, null);
        }

        public KeystoreArguments[] newArray(int size) {
            return new KeystoreArguments[size];
        }
    };
    public byte[][] args;

    /* synthetic */ KeystoreArguments(Parcel in, KeystoreArguments -this1) {
        this(in);
    }

    public KeystoreArguments() {
        this.args = null;
    }

    public KeystoreArguments(byte[][] args) {
        this.args = args;
    }

    private KeystoreArguments(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 0;
        if (this.args == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.args.length);
        byte[][] bArr = this.args;
        int length = bArr.length;
        while (i < length) {
            out.writeByteArray(bArr[i]);
            i++;
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
