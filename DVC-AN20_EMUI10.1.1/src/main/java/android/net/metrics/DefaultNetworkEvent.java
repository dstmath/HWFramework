package android.net.metrics;

import android.net.NetworkCapabilities;
import android.security.keystore.KeyProperties;
import com.android.internal.util.BitUtils;
import java.util.StringJoiner;

public class DefaultNetworkEvent {
    public final long creationTimeMs;
    public long durationMs;
    public int finalScore;
    public int initialScore;
    public boolean ipv4;
    public boolean ipv6;
    public int netId = 0;
    public int previousTransports;
    public int transports;
    public long validatedMs;

    public DefaultNetworkEvent(long timeMs) {
        this.creationTimeMs = timeMs;
    }

    public void updateDuration(long timeMs) {
        this.durationMs = timeMs - this.creationTimeMs;
    }

    public String toString() {
        StringJoiner j = new StringJoiner(", ", "DefaultNetworkEvent(", ")");
        j.add("netId=" + this.netId);
        int[] unpackBits = BitUtils.unpackBits((long) this.transports);
        int length = unpackBits.length;
        for (int i = 0; i < length; i++) {
            j.add(NetworkCapabilities.transportNameOf(unpackBits[i]));
        }
        j.add("ip=" + ipSupport());
        if (this.initialScore > 0) {
            j.add("initial_score=" + this.initialScore);
        }
        if (this.finalScore > 0) {
            j.add("final_score=" + this.finalScore);
        }
        j.add(String.format("duration=%.0fs", Double.valueOf(((double) this.durationMs) / 1000.0d)));
        j.add(String.format("validation=%04.1f%%", Double.valueOf((((double) this.validatedMs) * 100.0d) / ((double) this.durationMs))));
        return j.toString();
    }

    private String ipSupport() {
        if (this.ipv4 && this.ipv6) {
            return "IPv4v6";
        }
        if (this.ipv6) {
            return "IPv6";
        }
        if (this.ipv4) {
            return "IPv4";
        }
        return KeyProperties.DIGEST_NONE;
    }
}
