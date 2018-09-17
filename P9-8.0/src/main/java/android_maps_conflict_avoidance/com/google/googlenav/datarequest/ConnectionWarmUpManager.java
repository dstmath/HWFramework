package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Log;

public class ConnectionWarmUpManager {
    private Clock clock;
    private DataRequestDispatcher drd;
    private Object pendingKey;
    private String pendingSource = null;
    private long pendingWarmUpTime;
    private String requestSource = null;
    private int state = 0;

    public ConnectionWarmUpManager(DataRequestDispatcher drd, Clock clock) {
        this.drd = drd;
        this.clock = clock;
    }

    public void onStartServiceRequests(Object key) {
        synchronized (this) {
            if (this.state == 1) {
                this.state = 2;
                this.pendingWarmUpTime = this.clock.relativeTimeMillis();
            } else if (this.state == 2) {
                this.state = 3;
                this.pendingKey = key;
            }
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onFinishServiceRequests(Object key, long startTime, int firstByteLatency, int lastByteLatency) {
        synchronized (this) {
            if (this.state == 3 && this.pendingKey == key) {
                this.state = 0;
                String source = this.pendingSource;
                long time = this.pendingWarmUpTime;
                this.pendingKey = null;
                logUsed(source, (int) (startTime - time), firstByteLatency, lastByteLatency);
            }
        }
    }

    private void logUsed(String source, int interval, int firstByteLatency, int lastByteLatency) {
        logWithSource("u", source, "|d=" + interval + "|fb=" + firstByteLatency + "|lb=" + lastByteLatency + "|");
    }

    private void logWithSource(String status, String source, String data) {
        Log.addEvent((short) 64, status, "|s=" + source + (data.length() != 0 ? "" : "|") + data);
    }
}
