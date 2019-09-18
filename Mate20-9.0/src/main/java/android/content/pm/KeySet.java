package android.content.pm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class KeySet implements Parcelable {
    public static final Parcelable.Creator<KeySet> CREATOR = new Parcelable.Creator<KeySet>() {
        public KeySet createFromParcel(Parcel source) {
            return KeySet.readFromParcel(source);
        }

        public KeySet[] newArray(int size) {
            return new KeySet[size];
        }
    };
    private IBinder token;

    public KeySet(IBinder token2) {
        if (token2 != null) {
            this.token = token2;
            return;
        }
        throw new NullPointerException("null value for KeySet IBinder token");
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

    /* access modifiers changed from: private */
    public static KeySet readFromParcel(Parcel in) {
        return new KeySet(in.readStrongBinder());
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.token);
    }

    public int describeContents() {
        return 0;
    }
}
