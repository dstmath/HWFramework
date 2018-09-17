package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class MatchAllNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Creator<MatchAllNetworkSpecifier> CREATOR = new Creator<MatchAllNetworkSpecifier>() {
        public MatchAllNetworkSpecifier createFromParcel(Parcel in) {
            return new MatchAllNetworkSpecifier();
        }

        public MatchAllNetworkSpecifier[] newArray(int size) {
            return new MatchAllNetworkSpecifier[size];
        }
    };

    public static void checkNotMatchAllNetworkSpecifier(NetworkSpecifier ns) {
        if (ns instanceof MatchAllNetworkSpecifier) {
            throw new IllegalArgumentException("A MatchAllNetworkSpecifier is not permitted");
        }
    }

    public boolean satisfiedBy(NetworkSpecifier other) {
        throw new IllegalStateException("MatchAllNetworkSpecifier must not be used in NetworkRequests");
    }

    public boolean equals(Object o) {
        return o instanceof MatchAllNetworkSpecifier;
    }

    public int hashCode() {
        return 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }
}
