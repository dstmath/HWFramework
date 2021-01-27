package com.android.internal.telephony;

public class LinkCapacityEstimate {
    public static final int INVALID = -1;
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_SUSPENDED = 1;
    public final int confidence;
    public final int downlinkCapacityKbps;
    public final int status;
    public final int uplinkCapacityKbps;

    public LinkCapacityEstimate(int downlinkCapacityKbps2, int confidence2, int status2) {
        this.downlinkCapacityKbps = downlinkCapacityKbps2;
        this.confidence = confidence2;
        this.status = status2;
        this.uplinkCapacityKbps = -1;
    }

    public LinkCapacityEstimate(int downlinkCapacityKbps2, int uplinkCapacityKbps2) {
        this.downlinkCapacityKbps = downlinkCapacityKbps2;
        this.uplinkCapacityKbps = uplinkCapacityKbps2;
        this.confidence = -1;
        this.status = -1;
    }

    public String toString() {
        return "{downlinkCapacityKbps=" + this.downlinkCapacityKbps + ", uplinkCapacityKbps=" + this.uplinkCapacityKbps + ", confidence=" + this.confidence + ", status=" + this.status;
    }
}
