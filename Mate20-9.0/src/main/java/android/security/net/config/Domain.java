package android.security.net.config;

import java.util.Locale;

public final class Domain {
    public final String hostname;
    public final boolean subdomainsIncluded;

    public Domain(String hostname2, boolean subdomainsIncluded2) {
        if (hostname2 != null) {
            this.hostname = hostname2.toLowerCase(Locale.US);
            this.subdomainsIncluded = subdomainsIncluded2;
            return;
        }
        throw new NullPointerException("Hostname must not be null");
    }

    public int hashCode() {
        return this.hostname.hashCode() ^ (this.subdomainsIncluded ? 1231 : 1237);
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof Domain)) {
            return false;
        }
        Domain otherDomain = (Domain) other;
        if (otherDomain.subdomainsIncluded != this.subdomainsIncluded || !otherDomain.hostname.equals(this.hostname)) {
            z = false;
        }
        return z;
    }
}
