package com.android.okhttp;

import com.android.okhttp.internal.http.HeaderParser;
import java.util.concurrent.TimeUnit;

public final class CacheControl {
    public static final CacheControl FORCE_CACHE = new Builder().onlyIfCached().maxStale(Integer.MAX_VALUE, TimeUnit.SECONDS).build();
    public static final CacheControl FORCE_NETWORK = new Builder().noCache().build();
    String headerValue;
    private final boolean isPrivate;
    private final boolean isPublic;
    private final int maxAgeSeconds;
    private final int maxStaleSeconds;
    private final int minFreshSeconds;
    private final boolean mustRevalidate;
    private final boolean noCache;
    private final boolean noStore;
    private final boolean noTransform;
    private final boolean onlyIfCached;
    private final int sMaxAgeSeconds;

    public static final class Builder {
        int maxAgeSeconds = -1;
        int maxStaleSeconds = -1;
        int minFreshSeconds = -1;
        boolean noCache;
        boolean noStore;
        boolean noTransform;
        boolean onlyIfCached;

        public Builder noCache() {
            this.noCache = true;
            return this;
        }

        public Builder noStore() {
            this.noStore = true;
            return this;
        }

        public Builder maxAge(int maxAge, TimeUnit timeUnit) {
            int i;
            if (maxAge >= 0) {
                long maxAgeSecondsLong = timeUnit.toSeconds((long) maxAge);
                if (maxAgeSecondsLong > 2147483647L) {
                    i = Integer.MAX_VALUE;
                } else {
                    i = (int) maxAgeSecondsLong;
                }
                this.maxAgeSeconds = i;
                return this;
            }
            throw new IllegalArgumentException("maxAge < 0: " + maxAge);
        }

        public Builder maxStale(int maxStale, TimeUnit timeUnit) {
            int i;
            if (maxStale >= 0) {
                long maxStaleSecondsLong = timeUnit.toSeconds((long) maxStale);
                if (maxStaleSecondsLong > 2147483647L) {
                    i = Integer.MAX_VALUE;
                } else {
                    i = (int) maxStaleSecondsLong;
                }
                this.maxStaleSeconds = i;
                return this;
            }
            throw new IllegalArgumentException("maxStale < 0: " + maxStale);
        }

        public Builder minFresh(int minFresh, TimeUnit timeUnit) {
            int i;
            if (minFresh >= 0) {
                long minFreshSecondsLong = timeUnit.toSeconds((long) minFresh);
                if (minFreshSecondsLong > 2147483647L) {
                    i = Integer.MAX_VALUE;
                } else {
                    i = (int) minFreshSecondsLong;
                }
                this.minFreshSeconds = i;
                return this;
            }
            throw new IllegalArgumentException("minFresh < 0: " + minFresh);
        }

        public Builder onlyIfCached() {
            this.onlyIfCached = true;
            return this;
        }

        public Builder noTransform() {
            this.noTransform = true;
            return this;
        }

        public CacheControl build() {
            return new CacheControl(this);
        }
    }

    private CacheControl(boolean noCache2, boolean noStore2, int maxAgeSeconds2, int sMaxAgeSeconds2, boolean isPrivate2, boolean isPublic2, boolean mustRevalidate2, int maxStaleSeconds2, int minFreshSeconds2, boolean onlyIfCached2, boolean noTransform2, String headerValue2) {
        this.noCache = noCache2;
        this.noStore = noStore2;
        this.maxAgeSeconds = maxAgeSeconds2;
        this.sMaxAgeSeconds = sMaxAgeSeconds2;
        this.isPrivate = isPrivate2;
        this.isPublic = isPublic2;
        this.mustRevalidate = mustRevalidate2;
        this.maxStaleSeconds = maxStaleSeconds2;
        this.minFreshSeconds = minFreshSeconds2;
        this.onlyIfCached = onlyIfCached2;
        this.noTransform = noTransform2;
        this.headerValue = headerValue2;
    }

    private CacheControl(Builder builder) {
        this.noCache = builder.noCache;
        this.noStore = builder.noStore;
        this.maxAgeSeconds = builder.maxAgeSeconds;
        this.sMaxAgeSeconds = -1;
        this.isPrivate = false;
        this.isPublic = false;
        this.mustRevalidate = false;
        this.maxStaleSeconds = builder.maxStaleSeconds;
        this.minFreshSeconds = builder.minFreshSeconds;
        this.onlyIfCached = builder.onlyIfCached;
        this.noTransform = builder.noTransform;
    }

    public boolean noCache() {
        return this.noCache;
    }

    public boolean noStore() {
        return this.noStore;
    }

    public int maxAgeSeconds() {
        return this.maxAgeSeconds;
    }

    public int sMaxAgeSeconds() {
        return this.sMaxAgeSeconds;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public boolean mustRevalidate() {
        return this.mustRevalidate;
    }

    public int maxStaleSeconds() {
        return this.maxStaleSeconds;
    }

    public int minFreshSeconds() {
        return this.minFreshSeconds;
    }

    public boolean onlyIfCached() {
        return this.onlyIfCached;
    }

    public boolean noTransform() {
        return this.noTransform;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00ba  */
    public static CacheControl parse(Headers headers) {
        boolean noCache2;
        int maxAgeSeconds2;
        String parameter;
        int pos;
        int pos2;
        Headers headers2 = headers;
        boolean noCache3 = false;
        boolean noStore2 = false;
        int maxAgeSeconds3 = -1;
        int sMaxAgeSeconds2 = -1;
        boolean isPrivate2 = false;
        boolean isPublic2 = false;
        boolean mustRevalidate2 = false;
        int maxStaleSeconds2 = -1;
        int minFreshSeconds2 = -1;
        boolean onlyIfCached2 = false;
        boolean noTransform2 = false;
        boolean canUseHeaderValue = true;
        String headerValue2 = null;
        int i = 0;
        int size = headers.size();
        while (i < size) {
            int size2 = size;
            String name = headers2.name(i);
            boolean noTransform3 = noTransform2;
            String value = headers2.value(i);
            if (name.equalsIgnoreCase("Cache-Control")) {
                if (headerValue2 != null) {
                    canUseHeaderValue = false;
                } else {
                    headerValue2 = value;
                }
            } else if (name.equalsIgnoreCase("Pragma")) {
                canUseHeaderValue = false;
            } else {
                noTransform2 = noTransform3;
                i++;
                size = size2;
                headers2 = headers;
            }
            int pos3 = 0;
            while (true) {
                noCache2 = noCache3;
                if (pos3 >= value.length()) {
                    break;
                }
                int tokenStart = pos3;
                boolean noStore3 = noStore2;
                int pos4 = HeaderParser.skipUntil(value, pos3, "=,;");
                String directive = value.substring(tokenStart, pos4).trim();
                int i2 = tokenStart;
                if (pos4 != value.length()) {
                    maxAgeSeconds2 = maxAgeSeconds3;
                    if (!(value.charAt(pos4) == ',' || value.charAt(pos4) == ';')) {
                        int pos5 = HeaderParser.skipWhitespace(value, pos4 + 1);
                        if (pos5 >= value.length() || value.charAt(pos5) != '\"') {
                            int parameterStart = pos5;
                            pos = HeaderParser.skipUntil(value, pos5, ",;");
                            parameter = value.substring(parameterStart, pos).trim();
                        } else {
                            int pos6 = pos5 + 1;
                            int parameterStart2 = pos6;
                            int pos7 = HeaderParser.skipUntil(value, pos6, "\"");
                            parameter = value.substring(parameterStart2, pos7);
                            pos = pos7 + 1;
                        }
                        String parameter2 = parameter;
                        if (!"no-cache".equalsIgnoreCase(directive)) {
                            pos2 = pos;
                            noCache3 = true;
                        } else if ("no-store".equalsIgnoreCase(directive)) {
                            pos2 = pos;
                            noStore2 = true;
                            noCache3 = noCache2;
                            maxAgeSeconds3 = maxAgeSeconds2;
                            pos3 = pos2;
                        } else {
                            pos2 = pos;
                            if ("max-age".equalsIgnoreCase(directive)) {
                                maxAgeSeconds3 = HeaderParser.parseSeconds(parameter2, -1);
                                noCache3 = noCache2;
                                noStore2 = noStore3;
                                pos3 = pos2;
                            } else {
                                if ("s-maxage".equalsIgnoreCase(directive)) {
                                    sMaxAgeSeconds2 = HeaderParser.parseSeconds(parameter2, -1);
                                } else if ("private".equalsIgnoreCase(directive)) {
                                    isPrivate2 = true;
                                } else if ("public".equalsIgnoreCase(directive)) {
                                    isPublic2 = true;
                                } else if ("must-revalidate".equalsIgnoreCase(directive)) {
                                    mustRevalidate2 = true;
                                } else if ("max-stale".equalsIgnoreCase(directive)) {
                                    maxStaleSeconds2 = HeaderParser.parseSeconds(parameter2, Integer.MAX_VALUE);
                                } else if ("min-fresh".equalsIgnoreCase(directive)) {
                                    minFreshSeconds2 = HeaderParser.parseSeconds(parameter2, -1);
                                } else if ("only-if-cached".equalsIgnoreCase(directive)) {
                                    onlyIfCached2 = true;
                                } else if ("no-transform".equalsIgnoreCase(directive)) {
                                    noTransform3 = true;
                                }
                                noCache3 = noCache2;
                            }
                        }
                        noStore2 = noStore3;
                        maxAgeSeconds3 = maxAgeSeconds2;
                        pos3 = pos2;
                    }
                } else {
                    maxAgeSeconds2 = maxAgeSeconds3;
                }
                pos = pos4 + 1;
                parameter = null;
                String parameter22 = parameter;
                if (!"no-cache".equalsIgnoreCase(directive)) {
                }
                noStore2 = noStore3;
                maxAgeSeconds3 = maxAgeSeconds2;
                pos3 = pos2;
            }
            boolean z = noStore2;
            int i3 = maxAgeSeconds3;
            noTransform2 = noTransform3;
            noCache3 = noCache2;
            i++;
            size = size2;
            headers2 = headers;
        }
        boolean noTransform4 = noTransform2;
        if (!canUseHeaderValue) {
            headerValue2 = null;
        }
        CacheControl cacheControl = new CacheControl(noCache3, noStore2, maxAgeSeconds3, sMaxAgeSeconds2, isPrivate2, isPublic2, mustRevalidate2, maxStaleSeconds2, minFreshSeconds2, onlyIfCached2, noTransform4, headerValue2);
        return cacheControl;
    }

    public String toString() {
        String result = this.headerValue;
        if (result != null) {
            return result;
        }
        String headerValue2 = headerValue();
        this.headerValue = headerValue2;
        return headerValue2;
    }

    private String headerValue() {
        StringBuilder result = new StringBuilder();
        if (this.noCache) {
            result.append("no-cache, ");
        }
        if (this.noStore) {
            result.append("no-store, ");
        }
        if (this.maxAgeSeconds != -1) {
            result.append("max-age=");
            result.append(this.maxAgeSeconds);
            result.append(", ");
        }
        if (this.sMaxAgeSeconds != -1) {
            result.append("s-maxage=");
            result.append(this.sMaxAgeSeconds);
            result.append(", ");
        }
        if (this.isPrivate) {
            result.append("private, ");
        }
        if (this.isPublic) {
            result.append("public, ");
        }
        if (this.mustRevalidate) {
            result.append("must-revalidate, ");
        }
        if (this.maxStaleSeconds != -1) {
            result.append("max-stale=");
            result.append(this.maxStaleSeconds);
            result.append(", ");
        }
        if (this.minFreshSeconds != -1) {
            result.append("min-fresh=");
            result.append(this.minFreshSeconds);
            result.append(", ");
        }
        if (this.onlyIfCached) {
            result.append("only-if-cached, ");
        }
        if (this.noTransform) {
            result.append("no-transform, ");
        }
        if (result.length() == 0) {
            return "";
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }
}
