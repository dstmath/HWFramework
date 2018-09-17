package android.security.net.config;

import java.util.Locale;

public final class Domain {
    public final String hostname;
    public final boolean subdomainsIncluded;

    public Domain(String hostname, boolean subdomainsIncluded) {
        if (hostname == null) {
            throw new NullPointerException("Hostname must not be null");
        }
        this.hostname = hostname.toLowerCase(Locale.US);
        this.subdomainsIncluded = subdomainsIncluded;
    }

    public int hashCode() {
        return (this.subdomainsIncluded ? 1231 : 1237) ^ this.hostname.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == this) {
            return true;
        }
        if (!(other instanceof Domain)) {
            return false;
        }
        Domain otherDomain = (Domain) other;
        if (otherDomain.subdomainsIncluded == this.subdomainsIncluded) {
            z = otherDomain.hostname.equals(this.hostname);
        }
        return z;
    }
}
