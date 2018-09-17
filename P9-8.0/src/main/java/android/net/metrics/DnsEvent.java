package android.net.metrics;

import android.net.NetworkCapabilities;
import com.android.internal.util.BitUtils;
import java.util.Arrays;

public final class DnsEvent {
    private static final int SIZE_LIMIT = 20000;
    public int eventCount;
    public byte[] eventTypes;
    public int[] latenciesMs;
    public final int netId;
    public byte[] returnCodes;
    public final long transports;

    public DnsEvent(int netId, long transports, int initialCapacity) {
        this.netId = netId;
        this.transports = transports;
        this.eventTypes = new byte[initialCapacity];
        this.returnCodes = new byte[initialCapacity];
        this.latenciesMs = new int[initialCapacity];
    }

    public void addResult(byte eventType, byte returnCode, int latencyMs) {
        if (this.eventCount < 20000) {
            if (this.eventCount == this.eventTypes.length) {
                resize((int) (((double) this.eventCount) * 1.4d));
            }
            this.eventTypes[this.eventCount] = eventType;
            this.returnCodes[this.eventCount] = returnCode;
            this.latenciesMs[this.eventCount] = latencyMs;
            this.eventCount++;
        }
    }

    public void resize(int newLength) {
        this.eventTypes = Arrays.copyOf(this.eventTypes, newLength);
        this.returnCodes = Arrays.copyOf(this.returnCodes, newLength);
        this.latenciesMs = Arrays.copyOf(this.latenciesMs, newLength);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("DnsEvent(").append(this.netId).append(", ");
        for (int t : BitUtils.unpackBits(this.transports)) {
            builder.append(NetworkCapabilities.transportNameOf(t)).append(", ");
        }
        return builder.append(this.eventCount).append(" events)").toString();
    }
}
