package android.net.metrics;

import android.net.MacAddress;
import java.util.StringJoiner;

public class WakeupEvent {
    public MacAddress dstHwAddr;
    public String dstIp;
    public int dstPort;
    public int ethertype;
    public String iface;
    public int ipNextHeader;
    public String srcIp;
    public int srcPort;
    public long timestampMs;
    public int uid;

    public String toString() {
        StringJoiner j = new StringJoiner(", ", "WakeupEvent(", ")");
        j.add(String.format("%tT.%tL", new Object[]{Long.valueOf(this.timestampMs), Long.valueOf(this.timestampMs)}));
        j.add(this.iface);
        j.add("uid: " + Integer.toString(this.uid));
        j.add("eth=0x" + Integer.toHexString(this.ethertype));
        j.add("dstHw=" + this.dstHwAddr);
        if (this.ipNextHeader > 0) {
            j.add("ipNxtHdr=" + this.ipNextHeader);
            j.add("srcIp=" + this.srcIp);
            j.add("dstIp=" + this.dstIp);
            if (this.srcPort > -1) {
                j.add("srcPort=" + this.srcPort);
            }
            if (this.dstPort > -1) {
                j.add("dstPort=" + this.dstPort);
            }
        }
        return j.toString();
    }
}
