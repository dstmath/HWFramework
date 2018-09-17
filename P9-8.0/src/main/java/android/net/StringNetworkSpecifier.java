package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public final class StringNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Creator<StringNetworkSpecifier> CREATOR = new Creator<StringNetworkSpecifier>() {
        public StringNetworkSpecifier createFromParcel(Parcel in) {
            return new StringNetworkSpecifier(in.readString());
        }

        public StringNetworkSpecifier[] newArray(int size) {
            return new StringNetworkSpecifier[size];
        }
    };
    public final String specifier;

    public StringNetworkSpecifier(String specifier) {
        Preconditions.checkStringNotEmpty(specifier);
        this.specifier = specifier;
    }

    public boolean satisfiedBy(NetworkSpecifier other) {
        return equals(other);
    }

    public boolean equals(Object o) {
        if (o instanceof StringNetworkSpecifier) {
            return TextUtils.equals(this.specifier, ((StringNetworkSpecifier) o).specifier);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hashCode(this.specifier);
    }

    public String toString() {
        return this.specifier;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.specifier);
    }
}
