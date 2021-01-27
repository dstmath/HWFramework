package com.huawei.okhttp3.internal.cache;

import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.okhttp3.CacheControl;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.http.HttpDate;
import com.huawei.okhttp3.internal.http.HttpHeaders;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@Deprecated
public final class CacheStrategy {
    @Nullable
    public final Response cacheResponse;
    @Nullable
    public final Request networkRequest;

    CacheStrategy(Request networkRequest2, Response cacheResponse2) {
        this.networkRequest = networkRequest2;
        this.cacheResponse = cacheResponse2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0056, code lost:
        if (r3.cacheControl().isPrivate() == false) goto L_0x0059;
     */
    public static boolean isCacheable(Response response, Request request) {
        int code = response.code();
        if (!(code == 200 || code == 410 || code == 414 || code == 501 || code == 203 || code == 204)) {
            if (code != 307) {
                if (!(code == 308 || code == 404 || code == 405)) {
                    switch (code) {
                        case 300:
                        case MotionTypeApps.TYPE_PROXIMITY_ANSWER /* 301 */:
                            break;
                        case MotionTypeApps.TYPE_PROXIMITY_DIAL /* 302 */:
                            break;
                        default:
                            return false;
                    }
                }
            }
            if (response.header("Expires") == null) {
                if (response.cacheControl().maxAgeSeconds() == -1) {
                    if (!response.cacheControl().isPublic()) {
                    }
                }
            }
        }
        if (response.cacheControl().noStore() || request.cacheControl().noStore()) {
            return false;
        }
        return true;
    }

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
                this.sentRequestMillis = cacheResponse2.sentRequestAtMillis();
                this.receivedResponseMillis = cacheResponse2.receivedResponseAtMillis();
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
                        this.ageSeconds = HttpHeaders.parseSeconds(value, -1);
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

        private CacheStrategy getCandidate() {
            String conditionValue;
            String conditionName;
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
            if (!requestCaching.noCache()) {
                if (!hasConditions(this.request)) {
                    CacheControl responseCaching = this.cacheResponse.cacheControl();
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
                    if (!responseCaching.mustRevalidate() && requestCaching.maxStaleSeconds() != -1) {
                        maxStaleMillis = TimeUnit.SECONDS.toMillis((long) requestCaching.maxStaleSeconds());
                    }
                    if (responseCaching.noCache() || ageMillis + minFreshMillis >= freshMillis + maxStaleMillis) {
                        if (this.etag != null) {
                            conditionName = "If-None-Match";
                            conditionValue = this.etag;
                        } else if (this.lastModified != null) {
                            conditionName = "If-Modified-Since";
                            conditionValue = this.lastModifiedString;
                        } else if (this.servedDate == null) {
                            return new CacheStrategy(this.request, null);
                        } else {
                            conditionName = "If-Modified-Since";
                            conditionValue = this.servedDateString;
                        }
                        Headers.Builder conditionalRequestHeaders = this.request.headers().newBuilder();
                        Internal.instance.addLenient(conditionalRequestHeaders, conditionName, conditionValue);
                        return new CacheStrategy(this.request.newBuilder().headers(conditionalRequestHeaders.build()).build(), this.cacheResponse);
                    }
                    Response.Builder builder = this.cacheResponse.newBuilder();
                    if (ageMillis + minFreshMillis >= freshMillis) {
                        builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
                    }
                    if (ageMillis > 86400000 && isFreshnessLifetimeHeuristic()) {
                        builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
                    }
                    return new CacheStrategy(null, builder.build());
                }
            }
            return new CacheStrategy(this.request, null);
        }

        private long computeFreshnessLifetime() {
            long servedMillis;
            long servedMillis2;
            CacheControl responseCaching = this.cacheResponse.cacheControl();
            if (responseCaching.maxAgeSeconds() != -1) {
                return TimeUnit.SECONDS.toMillis((long) responseCaching.maxAgeSeconds());
            }
            if (this.expires != null) {
                Date date = this.servedDate;
                if (date != null) {
                    servedMillis2 = date.getTime();
                } else {
                    servedMillis2 = this.receivedResponseMillis;
                }
                long delta = this.expires.getTime() - servedMillis2;
                if (delta > 0) {
                    return delta;
                }
                return 0;
            } else if (this.lastModified == null || this.cacheResponse.request().url().query() != null) {
                return 0;
            } else {
                Date date2 = this.servedDate;
                if (date2 != null) {
                    servedMillis = date2.getTime();
                } else {
                    servedMillis = this.sentRequestMillis;
                }
                long delta2 = servedMillis - this.lastModified.getTime();
                if (delta2 > 0) {
                    return delta2 / 10;
                }
                return 0;
            }
        }

        private long cacheResponseAge() {
            long receivedAge;
            Date date = this.servedDate;
            long apparentReceivedAge = 0;
            if (date != null) {
                apparentReceivedAge = Math.max(0L, this.receivedResponseMillis - date.getTime());
            }
            if (this.ageSeconds != -1) {
                receivedAge = Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis((long) this.ageSeconds));
            } else {
                receivedAge = apparentReceivedAge;
            }
            long j = this.receivedResponseMillis;
            return receivedAge + (j - this.sentRequestMillis) + (this.nowMillis - j);
        }

        private boolean isFreshnessLifetimeHeuristic() {
            return this.cacheResponse.cacheControl().maxAgeSeconds() == -1 && this.expires == null;
        }

        private static boolean hasConditions(Request request2) {
            return (request2.header("If-Modified-Since") == null && request2.header("If-None-Match") == null) ? false : true;
        }
    }
}
