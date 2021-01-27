package com.huawei.internal.telephony;

import com.android.internal.telephony.InboundSmsTracker;

public class InboundSmsTrackerEx {
    private InboundSmsTracker mInboundSmsTracker;

    public void setInboundSmsTracker(InboundSmsTracker inboundSmsTracker) {
        this.mInboundSmsTracker = inboundSmsTracker;
    }

    public int getMessageCount() {
        InboundSmsTracker inboundSmsTracker = this.mInboundSmsTracker;
        if (inboundSmsTracker != null) {
            return inboundSmsTracker.getMessageCount();
        }
        return 0;
    }

    public String getAddress() {
        InboundSmsTracker inboundSmsTracker = this.mInboundSmsTracker;
        if (inboundSmsTracker != null) {
            return inboundSmsTracker.getAddress();
        }
        return null;
    }

    public int getReferenceNumber() {
        InboundSmsTracker inboundSmsTracker = this.mInboundSmsTracker;
        if (inboundSmsTracker != null) {
            return inboundSmsTracker.getReferenceNumber();
        }
        return 0;
    }

    public long getTimestamp() {
        InboundSmsTracker inboundSmsTracker = this.mInboundSmsTracker;
        if (inboundSmsTracker != null) {
            return inboundSmsTracker.getTimestamp();
        }
        return 0;
    }
}
