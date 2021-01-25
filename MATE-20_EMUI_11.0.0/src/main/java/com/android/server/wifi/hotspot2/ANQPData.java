package com.android.server.wifi.hotspot2;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.Clock;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ANQPData {
    @VisibleForTesting
    public static final long DATA_LIFETIME_MILLISECONDS = 3600000;
    private final Map<Constants.ANQPElementType, ANQPElement> mANQPElements = new HashMap();
    private final Clock mClock;
    private final long mExpiryTime;

    public ANQPData(Clock clock, Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        this.mClock = clock;
        if (anqpElements != null) {
            this.mANQPElements.putAll(anqpElements);
        }
        this.mExpiryTime = this.mClock.getElapsedSinceBootMillis() + DATA_LIFETIME_MILLISECONDS;
    }

    public Map<Constants.ANQPElementType, ANQPElement> getElements() {
        return Collections.unmodifiableMap(this.mANQPElements);
    }

    public boolean expired(long at) {
        return this.mExpiryTime <= at;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mANQPElements.size());
        sb.append(" elements, ");
        long now = this.mClock.getElapsedSinceBootMillis();
        sb.append(" expires in ");
        sb.append(Utils.toHMS(this.mExpiryTime - now));
        sb.append(' ');
        sb.append(expired(now) ? 'x' : '-');
        sb.append("\n");
        for (Map.Entry<Constants.ANQPElementType, ANQPElement> entry : this.mANQPElements.entrySet()) {
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }
}
