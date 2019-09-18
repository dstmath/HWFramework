package android.net.metrics;

import android.os.SystemClock;
import android.util.SparseIntArray;
import java.util.StringJoiner;

public class WakeupStats {
    private static final int NO_UID = -1;
    public long applicationWakeups = 0;
    public final long creationTimeMs = SystemClock.elapsedRealtime();
    public long durationSec = 0;
    public final SparseIntArray ethertypes = new SparseIntArray();
    public final String iface;
    public final SparseIntArray ipNextHeaders = new SparseIntArray();
    public long l2BroadcastCount = 0;
    public long l2MulticastCount = 0;
    public long l2UnicastCount = 0;
    public long noUidWakeups = 0;
    public long nonApplicationWakeups = 0;
    public long rootWakeups = 0;
    public long systemWakeups = 0;
    public long totalWakeups = 0;

    public WakeupStats(String iface2) {
        this.iface = iface2;
    }

    public void updateDuration() {
        this.durationSec = (SystemClock.elapsedRealtime() - this.creationTimeMs) / 1000;
    }

    public void countEvent(WakeupEvent ev) {
        this.totalWakeups++;
        int i = ev.uid;
        if (i != 1000) {
            switch (i) {
                case -1:
                    this.noUidWakeups++;
                    break;
                case 0:
                    this.rootWakeups++;
                    break;
                default:
                    if (ev.uid < 10000) {
                        this.nonApplicationWakeups++;
                        break;
                    } else {
                        this.applicationWakeups++;
                        break;
                    }
            }
        } else {
            this.systemWakeups++;
        }
        switch (ev.dstHwAddr.getAddressType()) {
            case 1:
                this.l2UnicastCount++;
                break;
            case 2:
                this.l2MulticastCount++;
                break;
            case 3:
                this.l2BroadcastCount++;
                break;
        }
        increment(this.ethertypes, ev.ethertype);
        if (ev.ipNextHeader >= 0) {
            increment(this.ipNextHeaders, ev.ipNextHeader);
        }
    }

    public String toString() {
        updateDuration();
        StringJoiner j = new StringJoiner(", ", "WakeupStats(", ")");
        j.add(this.iface);
        j.add("" + this.durationSec + "s");
        StringBuilder sb = new StringBuilder();
        sb.append("total: ");
        sb.append(this.totalWakeups);
        j.add(sb.toString());
        j.add("root: " + this.rootWakeups);
        j.add("system: " + this.systemWakeups);
        j.add("apps: " + this.applicationWakeups);
        j.add("non-apps: " + this.nonApplicationWakeups);
        j.add("no uid: " + this.noUidWakeups);
        j.add(String.format("l2 unicast/multicast/broadcast: %d/%d/%d", new Object[]{Long.valueOf(this.l2UnicastCount), Long.valueOf(this.l2MulticastCount), Long.valueOf(this.l2BroadcastCount)}));
        for (int i = 0; i < this.ethertypes.size(); i++) {
            j.add(String.format("ethertype 0x%x: %d", new Object[]{Integer.valueOf(this.ethertypes.keyAt(i)), Integer.valueOf(this.ethertypes.valueAt(i))}));
        }
        for (int i2 = 0; i2 < this.ipNextHeaders.size(); i2++) {
            j.add(String.format("ipNxtHdr %d: %d", new Object[]{Integer.valueOf(this.ipNextHeaders.keyAt(i2)), Integer.valueOf(this.ipNextHeaders.valueAt(i2))}));
        }
        return j.toString();
    }

    private static void increment(SparseIntArray counters, int key) {
        counters.put(key, counters.get(key, 0) + 1);
    }
}
