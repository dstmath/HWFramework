package android.util;

import android.net.wifi.WifiEnterpriseConfig;
import java.util.Objects;

public class Pair<F, S> {
    public final F first;
    public final S second;

    public Pair(F first2, S second2) {
        this.first = first2;
        this.second = second2;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair) o;
        if (!Objects.equals(p.first, this.first) || !Objects.equals(p.second, this.second)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        F f = this.first;
        int i = 0;
        int hashCode = f == null ? 0 : f.hashCode();
        S s = this.second;
        if (s != null) {
            i = s.hashCode();
        }
        return hashCode ^ i;
    }

    public String toString() {
        return "Pair{" + String.valueOf(this.first) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + String.valueOf(this.second) + "}";
    }

    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }
}
