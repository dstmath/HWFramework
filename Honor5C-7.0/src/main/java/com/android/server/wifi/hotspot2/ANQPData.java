package com.android.server.wifi.hotspot2;

import com.android.server.wifi.Clock;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ANQPData {
    private static final long ANQP_HOLDOFF_TIME = 10000;
    private static final long ANQP_QUALIFIED_CACHE_TIMEOUT = 3600000;
    private static final long ANQP_UNQUALIFIED_CACHE_TIMEOUT = 300000;
    private static final int MAX_RETRY = 6;
    private final Map<ANQPElementType, ANQPElement> mANQPElements;
    private final Clock mClock;
    private final long mCtime;
    private final long mExpiry;
    private final NetworkDetail mNetwork;
    private final int mRetry;

    public ANQPData(Clock clock, NetworkDetail network, Map<ANQPElementType, ANQPElement> anqpElements) {
        Map map = null;
        this.mClock = clock;
        this.mNetwork = network;
        if (anqpElements != null) {
            map = new HashMap(anqpElements);
        }
        this.mANQPElements = map;
        this.mCtime = this.mClock.currentTimeMillis();
        this.mRetry = 0;
        if (anqpElements == null) {
            this.mExpiry = this.mCtime + ANQP_HOLDOFF_TIME;
        } else if (network.getAnqpDomainID() == 0) {
            this.mExpiry = this.mCtime + ANQP_UNQUALIFIED_CACHE_TIMEOUT;
        } else {
            this.mExpiry = this.mCtime + ANQP_QUALIFIED_CACHE_TIMEOUT;
        }
    }

    public ANQPData(Clock clock, NetworkDetail network, ANQPData existing) {
        this.mClock = clock;
        this.mNetwork = network;
        this.mANQPElements = null;
        this.mCtime = this.mClock.currentTimeMillis();
        if (existing == null) {
            this.mRetry = 0;
            this.mExpiry = this.mCtime + ANQP_HOLDOFF_TIME;
            return;
        }
        this.mRetry = Math.max(existing.getRetry() + 1, MAX_RETRY);
        this.mExpiry = ((long) (1 << this.mRetry)) * ANQP_HOLDOFF_TIME;
    }

    public List<ANQPElementType> disjoint(List<ANQPElementType> querySet) {
        if (this.mANQPElements == null) {
            return null;
        }
        List<ANQPElementType> additions = new ArrayList();
        for (ANQPElementType element : querySet) {
            if (!this.mANQPElements.containsKey(element)) {
                additions.add(element);
            }
        }
        if (additions.isEmpty()) {
            additions = null;
        }
        return additions;
    }

    public Map<ANQPElementType, ANQPElement> getANQPElements() {
        return Collections.unmodifiableMap(this.mANQPElements);
    }

    public NetworkDetail getNetwork() {
        return this.mNetwork;
    }

    public boolean expired() {
        return expired(this.mClock.currentTimeMillis());
    }

    public boolean expired(long at) {
        return this.mExpiry <= at;
    }

    protected boolean hasData() {
        return this.mANQPElements != null;
    }

    protected void merge(Map<ANQPElementType, ANQPElement> data) {
        if (data != null) {
            this.mANQPElements.putAll(data);
        }
    }

    protected boolean isValid(NetworkDetail nwk) {
        if (this.mANQPElements == null || nwk.getAnqpDomainID() != this.mNetwork.getAnqpDomainID() || this.mExpiry <= this.mClock.currentTimeMillis()) {
            return false;
        }
        return true;
    }

    private int getRetry() {
        return this.mRetry;
    }

    public String toString(boolean brief) {
        char c = '-';
        StringBuilder sb = new StringBuilder();
        sb.append(this.mNetwork.toKeyString()).append(", domid ").append(this.mNetwork.getAnqpDomainID());
        if (this.mANQPElements == null) {
            sb.append(", unresolved, ");
        } else {
            sb.append(", ").append(this.mANQPElements.size()).append(" elements, ");
        }
        long now = this.mClock.currentTimeMillis();
        sb.append(Utils.toHMS(now - this.mCtime)).append(" old, expires in ").append(Utils.toHMS(this.mExpiry - now)).append(' ');
        if (brief) {
            char c2;
            if (expired(now)) {
                c2 = 'x';
            } else {
                c2 = '-';
            }
            sb.append(c2);
            if (this.mANQPElements == null) {
                c = 'u';
            }
            sb.append(c);
        } else if (this.mANQPElements != null) {
            sb.append(" data=").append(this.mANQPElements);
        }
        return sb.toString();
    }

    public String toString() {
        return toString(true);
    }
}
