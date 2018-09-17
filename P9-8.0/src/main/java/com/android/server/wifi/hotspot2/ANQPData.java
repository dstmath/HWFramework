package com.android.server.wifi.hotspot2;

import com.android.server.wifi.Clock;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ANQPData {
    public static final long DATA_LIFETIME_MILLISECONDS = 3600000;
    private final Map<ANQPElementType, ANQPElement> mANQPElements = new HashMap();
    private final Clock mClock;
    private final long mExpiryTime;

    public ANQPData(Clock clock, Map<ANQPElementType, ANQPElement> anqpElements) {
        this.mClock = clock;
        if (anqpElements != null) {
            this.mANQPElements.putAll(anqpElements);
        }
        this.mExpiryTime = this.mClock.getElapsedSinceBootMillis() + DATA_LIFETIME_MILLISECONDS;
    }

    public Map<ANQPElementType, ANQPElement> getElements() {
        return Collections.unmodifiableMap(this.mANQPElements);
    }

    public boolean expired(long at) {
        return this.mExpiryTime <= at;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mANQPElements.size()).append(" elements, ");
        long now = this.mClock.getElapsedSinceBootMillis();
        sb.append(" expires in ").append(Utils.toHMS(this.mExpiryTime - now)).append(' ');
        sb.append(expired(now) ? 'x' : '-');
        return sb.toString();
    }
}
