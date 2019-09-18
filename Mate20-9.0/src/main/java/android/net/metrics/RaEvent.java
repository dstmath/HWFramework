package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;

public final class RaEvent implements Parcelable {
    public static final Parcelable.Creator<RaEvent> CREATOR = new Parcelable.Creator<RaEvent>() {
        public RaEvent createFromParcel(Parcel in) {
            return new RaEvent(in);
        }

        public RaEvent[] newArray(int size) {
            return new RaEvent[size];
        }
    };
    public static final long NO_LIFETIME = -1;
    public final long dnsslLifetime;
    public final long prefixPreferredLifetime;
    public final long prefixValidLifetime;
    public final long rdnssLifetime;
    public final long routeInfoLifetime;
    public final long routerLifetime;

    public static class Builder {
        long dnsslLifetime = -1;
        long prefixPreferredLifetime = -1;
        long prefixValidLifetime = -1;
        long rdnssLifetime = -1;
        long routeInfoLifetime = -1;
        long routerLifetime = -1;

        public RaEvent build() {
            RaEvent raEvent = new RaEvent(this.routerLifetime, this.prefixValidLifetime, this.prefixPreferredLifetime, this.routeInfoLifetime, this.rdnssLifetime, this.dnsslLifetime);
            return raEvent;
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

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.routerLifetime);
        out.writeLong(this.prefixValidLifetime);
        out.writeLong(this.prefixPreferredLifetime);
        out.writeLong(this.routeInfoLifetime);
        out.writeLong(this.rdnssLifetime);
        out.writeLong(this.dnsslLifetime);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "RaEvent(lifetimes: " + String.format("router=%ds, ", new Object[]{Long.valueOf(this.routerLifetime)}) + String.format("prefix_valid=%ds, ", new Object[]{Long.valueOf(this.prefixValidLifetime)}) + String.format("prefix_preferred=%ds, ", new Object[]{Long.valueOf(this.prefixPreferredLifetime)}) + String.format("route_info=%ds, ", new Object[]{Long.valueOf(this.routeInfoLifetime)}) + String.format("rdnss=%ds, ", new Object[]{Long.valueOf(this.rdnssLifetime)}) + String.format("dnssl=%ds)", new Object[]{Long.valueOf(this.dnsslLifetime)});
    }
}
