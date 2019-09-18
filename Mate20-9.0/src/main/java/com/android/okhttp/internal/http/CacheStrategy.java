package com.android.okhttp.internal.http;

import com.android.okhttp.CacheControl;
import com.android.okhttp.Headers;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class CacheStrategy {
    public final Response cacheResponse;
    public final Request networkRequest;

    public static class Factory {
        private int ageSeconds = -1;
        final Response cacheResponse;
        private String etag;
        private Date expires;
        private Date lastModified;
        private String lastModifiedString;
        final long nowMillis;
        private long receivedResponseMillis;
        final Request request;
        private long sentRequestMillis;
        private Date servedDate;
        private String servedDateString;

        public Factory(long nowMillis2, Request request2, Response cacheResponse2) {
            this.nowMillis = nowMillis2;
            this.request = request2;
            this.cacheResponse = cacheResponse2;
            if (cacheResponse2 != null) {
                Headers headers = cacheResponse2.headers();
                int size = headers.size();
                for (int i = 0; i < size; i++) {
                    String fieldName = headers.name(i);
                    String value = headers.value(i);
                    if ("Date".equalsIgnoreCase(fieldName)) {
                        this.servedDate = HttpDate.parse(value);
                        this.servedDateString = value;
                    } else if ("Expires".equalsIgnoreCase(fieldName)) {
                        this.expires = HttpDate.parse(value);
                    } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
                        this.lastModified = HttpDate.parse(value);
                        this.lastModifiedString = value;
                    } else if ("ETag".equalsIgnoreCase(fieldName)) {
                        this.etag = value;
                    } else if ("Age".equalsIgnoreCase(fieldName)) {
                        this.ageSeconds = HeaderParser.parseSeconds(value, -1);
                    } else if (OkHeaders.SENT_MILLIS.equalsIgnoreCase(fieldName)) {
                        this.sentRequestMillis = Long.parseLong(value);
                    } else if (OkHeaders.RECEIVED_MILLIS.equalsIgnoreCase(fieldName)) {
                        this.receivedResponseMillis = Long.parseLong(value);
                    }
                }
            }
        }

        public CacheStrategy get() {
            CacheStrategy candidate = getCandidate();
            if (candidate.networkRequest == null || !this.request.cacheControl().onlyIfCached()) {
                return candidate;
            }
            return new CacheStrategy(null, null);
        }

        /* JADX WARNING: type inference failed for: r14v0, types: [com.android.okhttp.Response, com.android.okhttp.internal.http.CacheStrategy$1] */
        /* JADX WARNING: type inference failed for: r14v1 */
        /* JADX WARNING: type inference failed for: r14v5 */
        private CacheStrategy getCandidate() {
            ? r14;
            CacheStrategy cacheStrategy;
            if (this.cacheResponse == null) {
                return new CacheStrategy(this.request, null);
            }
            if (this.request.isHttps() && this.cacheResponse.handshake() == null) {
                return new CacheStrategy(this.request, null);
            }
            if (!CacheStrategy.isCacheable(this.cacheResponse, this.request)) {
                return new CacheStrategy(this.request, null);
            }
            CacheControl requestCaching = this.request.cacheControl();
            if (requestCaching.noCache()) {
                r14 = 0;
            } else if (hasConditions(this.request)) {
                CacheControl cacheControl = requestCaching;
                r14 = 0;
            } else {
                long ageMillis = cacheResponseAge();
                long freshMillis = computeFreshnessLifetime();
                if (requestCaching.maxAgeSeconds() != -1) {
                    freshMillis = Math.min(freshMillis, TimeUnit.SECONDS.toMillis((long) requestCaching.maxAgeSeconds()));
                }
                long minFreshMillis = 0;
                if (requestCaching.minFreshSeconds() != -1) {
                    minFreshMillis = TimeUnit.SECONDS.toMillis((long) requestCaching.minFreshSeconds());
                }
                long maxStaleMillis = 0;
                CacheControl responseCaching = this.cacheResponse.cacheControl();
                if (!responseCaching.mustRevalidate() && requestCaching.maxStaleSeconds() != -1) {
                    maxStaleMillis = TimeUnit.SECONDS.toMillis((long) requestCaching.maxStaleSeconds());
                }
                if (responseCaching.noCache() || ageMillis + minFreshMillis >= freshMillis + maxStaleMillis) {
                    Request.Builder conditionalRequestBuilder = this.request.newBuilder();
                    if (this.etag != null) {
                        conditionalRequestBuilder.header("If-None-Match", this.etag);
                    } else if (this.lastModified != null) {
                        conditionalRequestBuilder.header("If-Modified-Since", this.lastModifiedString);
                    } else if (this.servedDate != null) {
                        conditionalRequestBuilder.header("If-Modified-Since", this.servedDateString);
                    }
                    Request conditionalRequest = conditionalRequestBuilder.build();
                    if (hasConditions(conditionalRequest)) {
                        cacheStrategy = new CacheStrategy(conditionalRequest, this.cacheResponse);
                    } else {
                        cacheStrategy = new CacheStrategy(conditionalRequest, null);
                    }
                    return cacheStrategy;
                }
                Response.Builder builder = this.cacheResponse.newBuilder();
                if (ageMillis + minFreshMillis >= freshMillis) {
                    builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
                }
                if (ageMillis > 86400000 && isFreshnessLifetimeHeuristic()) {
                    builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
                }
                CacheControl cacheControl2 = requestCaching;
                return new CacheStrategy(null, builder.build());
            }
            return new CacheStrategy(this.request, r14);
        }

        private long computeFreshnessLifetime() {
            long servedMillis;
            long servedMillis2;
            CacheControl responseCaching = this.cacheResponse.cacheControl();
            if (responseCaching.maxAgeSeconds() != -1) {
                return TimeUnit.SECONDS.toMillis((long) responseCaching.maxAgeSeconds());
            }
            long j = 0;
            if (this.expires != null) {
                if (this.servedDate != null) {
                    servedMillis2 = this.servedDate.getTime();
                } else {
                    servedMillis2 = this.receivedResponseMillis;
                }
                long delta = this.expires.getTime() - servedMillis2;
                if (delta > 0) {
                    j = delta;
                }
                return j;
            } else if (this.lastModified == null || this.cacheResponse.request().httpUrl().query() != null) {
                return 0;
            } else {
                if (this.servedDate != null) {
                    servedMillis = this.servedDate.getTime();
                } else {
                    servedMillis = this.sentRequestMillis;
                }
                long delta2 = servedMillis - this.lastModified.getTime();
                if (delta2 > 0) {
                    j = delta2 / 10;
                }
                return j;
            }
        }

        private long cacheResponseAge() {
            long receivedAge;
            long j = 0;
            if (this.servedDate != null) {
                j = Math.max(0, this.receivedResponseMillis - this.servedDate.getTime());
            }
            long apparentReceivedAge = j;
            if (this.ageSeconds != -1) {
                receivedAge = Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis((long) this.ageSeconds));
            } else {
                receivedAge = apparentReceivedAge;
            }
            long responseDuration = this.receivedResponseMillis - this.sentRequestMillis;
            return receivedAge + responseDuration + (this.nowMillis - this.receivedResponseMillis);
        }

        private boolean isFreshnessLifetimeHeuristic() {
            return this.cacheResponse.cacheControl().maxAgeSeconds() == -1 && this.expires == null;
        }

        private static boolean hasConditions(Request request2) {
            return (request2.header("If-Modified-Since") == null && request2.header("If-None-Match") == null) ? false : true;
        }
    }

    private CacheStrategy(Request networkRequest2, Response cacheResponse2) {
        this.networkRequest = networkRequest2;
        this.cacheResponse = cacheResponse2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003a, code lost:
        if (r3.cacheControl().noStore() != false) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0044, code lost:
        if (r4.cacheControl().noStore() != false) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0046, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0048, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002e, code lost:
        if (r3.cacheControl().isPrivate() == false) goto L_0x0049;
     */
    public static boolean isCacheable(Response response, Request request) {
        boolean z = false;
        switch (response.code()) {
            case 200:
            case 203:
            case 204:
            case 300:
            case 301:
            case StatusLine.HTTP_PERM_REDIRECT /*308*/:
            case 404:
            case 405:
            case 410:
            case 414:
            case 501:
                break;
            case 302:
            case StatusLine.HTTP_TEMP_REDIRECT /*307*/:
                if (response.header("Expires") == null) {
                    if (response.cacheControl().maxAgeSeconds() == -1) {
                        if (!response.cacheControl().isPublic()) {
                            break;
                        }
                    }
                }
                break;
        }
    }
}
