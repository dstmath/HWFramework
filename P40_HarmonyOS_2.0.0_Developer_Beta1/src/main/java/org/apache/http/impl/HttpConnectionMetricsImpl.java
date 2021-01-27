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

    public HttpConnectionMetricsImpl(HttpTransportMetrics inTransportMetric2, HttpTransportMetrics outTransportMetric2) {
        this.inTransportMetric = inTransportMetric2;
        this.outTransportMetric = outTransportMetric2;
    }

    @Override // org.apache.http.HttpConnectionMetrics
    public long getReceivedBytesCount() {
        HttpTransportMetrics httpTransportMetrics = this.inTransportMetric;
        if (httpTransportMetrics != null) {
            return httpTransportMetrics.getBytesTransferred();
        }
        return -1;
    }

    @Override // org.apache.http.HttpConnectionMetrics
    public long getSentBytesCount() {
        HttpTransportMetrics httpTransportMetrics = this.outTransportMetric;
        if (httpTransportMetrics != null) {
            return httpTransportMetrics.getBytesTransferred();
        }
        return -1;
    }

    @Override // org.apache.http.HttpConnectionMetrics
    public long getRequestCount() {
        return this.requestCount;
    }

    public void incrementRequestCount() {
        this.requestCount++;
    }

    @Override // org.apache.http.HttpConnectionMetrics
    public long getResponseCount() {
        return this.responseCount;
    }

    public void incrementResponseCount() {
        this.responseCount++;
    }

    @Override // org.apache.http.HttpConnectionMetrics
    public Object getMetric(String metricName) {
        Object value = null;
        HashMap hashMap = this.metricsCache;
        if (hashMap != null) {
            value = hashMap.get(metricName);
        }
        if (value != null) {
            return value;
        }
        if (REQUEST_COUNT.equals(metricName)) {
            return new Long(this.requestCount);
        }
        if (RESPONSE_COUNT.equals(metricName)) {
            return new Long(this.responseCount);
        }
        if (RECEIVED_BYTES_COUNT.equals(metricName)) {
            HttpTransportMetrics httpTransportMetrics = this.inTransportMetric;
            if (httpTransportMetrics != null) {
                return new Long(httpTransportMetrics.getBytesTransferred());
            }
            return null;
        } else if (!SENT_BYTES_COUNT.equals(metricName)) {
            return value;
        } else {
            HttpTransportMetrics httpTransportMetrics2 = this.outTransportMetric;
            if (httpTransportMetrics2 != null) {
                return new Long(httpTransportMetrics2.getBytesTransferred());
            }
            return null;
        }
    }

    public void setMetric(String metricName, Object obj) {
        if (this.metricsCache == null) {
            this.metricsCache = new HashMap();
        }
        this.metricsCache.put(metricName, obj);
    }

    @Override // org.apache.http.HttpConnectionMetrics
    public void reset() {
        HttpTransportMetrics httpTransportMetrics = this.outTransportMetric;
        if (httpTransportMetrics != null) {
            httpTransportMetrics.reset();
        }
        HttpTransportMetrics httpTransportMetrics2 = this.inTransportMetric;
        if (httpTransportMetrics2 != null) {
            httpTransportMetrics2.reset();
        }
        this.requestCount = 0;
        this.responseCount = 0;
        this.metricsCache = null;
    }
}
