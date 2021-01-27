package android.security.net.config;

import com.android.internal.logging.nano.MetricsProto;
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
        return this.hostname.hashCode() ^ (this.subdomainsIncluded ? MetricsProto.MetricsEvent.AUTOFILL_SERVICE_DISABLED_APP : MetricsProto.MetricsEvent.ANOMALY_TYPE_UNOPTIMIZED_BT);
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Domain)) {
            return false;
        }
        Domain otherDomain = (Domain) other;
        if (otherDomain.subdomainsIncluded != this.subdomainsIncluded || !otherDomain.hostname.equals(this.hostname)) {
            return false;
        }
        return true;
    }
}
