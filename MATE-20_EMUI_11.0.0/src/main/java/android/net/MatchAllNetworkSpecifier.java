package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public final class MatchAllNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Parcelable.Creator<MatchAllNetworkSpecifier> CREATOR = new Parcelable.Creator<MatchAllNetworkSpecifier>() {
        /* class android.net.MatchAllNetworkSpecifier.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MatchAllNetworkSpecifier createFromParcel(Parcel in) {
            return new MatchAllNetworkSpecifier();
        }

        @Override // android.os.Parcelable.Creator
        public MatchAllNetworkSpecifier[] newArray(int size) {
            return new MatchAllNetworkSpecifier[size];
        }
    };

    public static void checkNotMatchAllNetworkSpecifier(NetworkSpecifier ns) {
        if (ns instanceof MatchAllNetworkSpecifier) {
            throw new IllegalArgumentException("A MatchAllNetworkSpecifier is not permitted");
        }
    }

    @Override // android.net.NetworkSpecifier
    public boolean satisfiedBy(NetworkSpecifier other) {
        throw new IllegalStateException("MatchAllNetworkSpecifier must not be used in NetworkRequests");
    }

    public boolean equals(Object o) {
        return o instanceof MatchAllNetworkSpecifier;
    }

    public int hashCode() {
        return 0;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
    }
}
