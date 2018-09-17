package android.content.pm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class KeySet implements Parcelable {
    public static final Creator<KeySet> CREATOR = new Creator<KeySet>() {
        public KeySet createFromParcel(Parcel source) {
            return KeySet.readFromParcel(source);
        }

        public KeySet[] newArray(int size) {
            return new KeySet[size];
        }
    };
    private IBinder token;

    public KeySet(IBinder token) {
        if (token == null) {
            throw new NullPointerException("null value for KeySet IBinder token");
        }
        this.token = token;
    }

    public IBinder getToken() {
        return this.token;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof KeySet)) {
            return false;
        }
        if (this.token == ((KeySet) o).token) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.token.hashCode();
    }

    private static KeySet readFromParcel(Parcel in) {
        return new KeySet(in.readStrongBinder());
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.token);
    }

    public int describeContents() {
        return 0;
    }
}
