package android.net;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public final class StringNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Parcelable.Creator<StringNetworkSpecifier> CREATOR = new Parcelable.Creator<StringNetworkSpecifier>() {
        /* class android.net.StringNetworkSpecifier.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StringNetworkSpecifier createFromParcel(Parcel in) {
            return new StringNetworkSpecifier(in.readString());
        }

        @Override // android.os.Parcelable.Creator
        public StringNetworkSpecifier[] newArray(int size) {
            return new StringNetworkSpecifier[size];
        }
    };
    @UnsupportedAppUsage
    public final String specifier;

    public StringNetworkSpecifier(String specifier2) {
        Preconditions.checkStringNotEmpty(specifier2);
        this.specifier = specifier2;
    }

    @Override // android.net.NetworkSpecifier
    public boolean satisfiedBy(NetworkSpecifier other) {
        return equals(other);
    }

    public boolean equals(Object o) {
        if (!(o instanceof StringNetworkSpecifier)) {
            return false;
        }
        return TextUtils.equals(this.specifier, ((StringNetworkSpecifier) o).specifier);
    }

    public int hashCode() {
        return Objects.hashCode(this.specifier);
    }

    public String toString() {
        return this.specifier;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.specifier);
    }
}
