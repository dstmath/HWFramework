package org.apache.http.impl;

import java.util.HashMap;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.io.HttpTransportMetrics;

@Deprecated
public class HttpConnectionMetricsImpl implements HttpConnectionMetrics {
    public static final String RECEIVED_BYTES_COUNT = "http.received-bytes-count";
    public static final String REQUEST_COUNT = "http.request-count";
    public static final String RESPONSE_COUNT = "http.response-count";
    public static final String SENT_BYTES_COUNT = "http.sent-bytes-count";
    private final HttpTransportMetrics inTransportMetric;
    private HashMap metricsCache;
    private final HttpTransportMetrics outTransportMetric;
    private long requestCount = 0;
    private long responseCount = 0;

    public HttpConnectionMetricsImpl(HttpTransportMetrics inTransportMetric, HttpTransportMetrics outTransportMetric) {
        this.inTransportMetric = inTransportMetric;
        this.outTransportMetric = outTransportMetric;
    }

    public long getReceivedBytesCount() {
        if (this.inTransportMetric != null) {
            return this.inTransportMetric.getBytesTransferred();
        }
        return -1;
    }

    public long getSentBytesCount() {
        if (this.outTransportMetric != null) {
            return this.outTransportMetric.getBytesTransferred();
        }
        return -1;
    }

    public long getRequestCount() {
        return this.requestCount;
    }

    public void incrementRequestCount() {
        this.requestCount++;
    }

    public long getResponseCount() {
        return this.responseCount;
    }

    public void incrementResponseCount() {
        this.responseCount++;
    }

    public Object getMetric(String metricName) {
        Object value = null;
        if (this.metricsCache != null) {
            value = this.metricsCache.get(metricName);
        }
        if (value == null) {
            if (REQUEST_COUNT.equals(metricName)) {
                value = new Long(this.requestCount);
            } else if (RESPONSE_COUNT.equals(metricName)) {
                value = new Long(this.responseCount);
            } else if (RECEIVED_BYTES_COUNT.equals(metricName)) {
                if (this.inTransportMetric != null) {
                    return new Long(this.inTransportMetric.getBytesTransferred());
                }
                return null;
            } else if (SENT_BYTES_COUNT.equals(metricName)) {
                if (this.outTransportMetric != null) {
                    return new Long(this.outTransportMetric.getBytesTransferred());
                }
                return null;
            }
        }
        return value;
    }

    public void setMetric(String metricName, Object obj) {
        if (this.metricsCache == null) {
            this.metricsCache = new HashMap();
        }
        this.metricsCache.put(metricName, obj);
    }

    public void reset() {
        if (this.outTransportMetric != null) {
            this.outTransportMetric.reset();
        }
        if (this.inTransportMetric != null) {
            this.inTransportMetric.reset();
        }
        this.requestCount = 0;
        this.responseCount = 0;
        this.metricsCache = null;
    }
}
