package android.net.metrics;

import android.annotation.SystemApi;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class RaEvent implements IpConnectivityLog.Event {
    public static final Parcelable.Creator<RaEvent> CREATOR = new Parcelable.Creator<RaEvent>() {
        /* class android.net.metrics.RaEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RaEvent createFromParcel(Parcel in) {
            return new RaEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public RaEvent[] newArray(int size) {
            return new RaEvent[size];
        }
    };
    private static final long NO_LIFETIME = -1;
    public final long dnsslLifetime;
    public final long prefixPreferredLifetime;
    public final long prefixValidLifetime;
    public final long rdnssLifetime;
    public final long routeInfoLifetime;
    public final long routerLifetime;

    public RaEvent(long routerLifetime2, long prefixValidLifetime2, long prefixPreferredLifetime2, long routeInfoLifetime2, long rdnssLifetime2, long dnsslLifetime2) {
        this.routerLifetime = routerLifetime2;
        this.prefixValidLifetime = prefixValidLifetime2;
        this.prefixPreferredLifetime = prefixPreferredLifetime2;
        this.routeInfoLifetime = routeInfoLifetime2;
        this.rdnssLifetime = rdnssLifetime2;
        this.dnsslLifetime = dnsslLifetime2;
    }

    private RaEvent(Parcel in) {
        this.routerLifetime = in.readLong();
        this.prefixValidLifetime = in.readLong();
        this.prefixPreferredLifetime = in.readLong();
        this.routeInfoLifetime = in.readLong();
        this.rdnssLifetime = in.readLong();
        this.dnsslLifetime = in.readLong();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.routerLifetime);
        out.writeLong(this.prefixValidLifetime);
        out.writeLong(this.prefixPreferredLifetime);
        out.writeLong(this.routeInfoLifetime);
        out.writeLong(this.rdnssLifetime);
        out.writeLong(this.dnsslLifetime);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "RaEvent(lifetimes: " + String.format("router=%ds, ", Long.valueOf(this.routerLifetime)) + String.format("prefix_valid=%ds, ", Long.valueOf(this.prefixValidLifetime)) + String.format("prefix_preferred=%ds, ", Long.valueOf(this.prefixPreferredLifetime)) + String.format("route_info=%ds, ", Long.valueOf(this.routeInfoLifetime)) + String.format("rdnss=%ds, ", Long.valueOf(this.rdnssLifetime)) + String.format("dnssl=%ds)", Long.valueOf(this.dnsslLifetime));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(RaEvent.class)) {
            return false;
        }
        RaEvent other = (RaEvent) obj;
        if (this.routerLifetime == other.routerLifetime && this.prefixValidLifetime == other.prefixValidLifetime && this.prefixPreferredLifetime == other.prefixPreferredLifetime && this.routeInfoLifetime == other.routeInfoLifetime && this.rdnssLifetime == other.rdnssLifetime && this.dnsslLifetime == other.dnsslLifetime) {
            return true;
        }
        return false;
    }

    public static final class Builder {
        long dnsslLifetime = -1;
        long prefixPreferredLifetime = -1;
        long prefixValidLifetime = -1;
        long rdnssLifetime = -1;
        long routeInfoLifetime = -1;
        long routerLifetime = -1;

        public RaEvent build() {
            return new RaEvent(this.routerLifetime, this.prefixValidLifetime, this.prefixPreferredLifetime, this.routeInfoLifetime, this.rdnssLifetime, this.dnsslLifetime);
        }

        public Builder updateRouterLifetime(long lifetime) {
            this.routerLifetime = updateLifetime(this.routerLifetime, lifetime);
            return this;
        }

        public Builder updatePrefixValidLifetime(long lifetime) {
            this.prefixValidLifetime = updateLifetime(this.prefixValidLifetime, lifetime);
            return this;
        }

        public Builder updatePrefixPreferredLifetime(long lifetime) {
            this.prefixPreferredLifetime = updateLifetime(this.prefixPreferredLifetime, lifetime);
            return this;
        }

        public Builder updateRouteInfoLifetime(long lifetime) {
            this.routeInfoLifetime = updateLifetime(this.routeInfoLifetime, lifetime);
            return this;
        }

        public Builder updateRdnssLifetime(long lifetime) {
            this.rdnssLifetime = updateLifetime(this.rdnssLifetime, lifetime);
            return this;
        }

        public Builder updateDnsslLifetime(long lifetime) {
            this.dnsslLifetime = updateLifetime(this.dnsslLifetime, lifetime);
            return this;
        }

        private long updateLifetime(long currentLifetime, long newLifetime) {
            if (currentLifetime == -1) {
                return newLifetime;
            }
            return Math.min(currentLifetime, newLifetime);
        }
    }
}
