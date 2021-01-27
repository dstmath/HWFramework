package com.huawei.okhttp3;

import com.huawei.android.content.IntentFilterEx;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.publicsuffix.PublicSuffixDatabase;
import com.huawei.okio.Buffer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

@Deprecated
public final class HttpUrl {
    static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    static final String FRAGMENT_ENCODE_SET = "";
    static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    static final String PATH_SEGMENT_ENCODE_SET_URI = "[]";
    static final String QUERY_COMPONENT_ENCODE_SET = " !\"#$&'(),/:;<=>?@[]\\^`{|}~";
    static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    static final String QUERY_COMPONENT_REENCODE_SET = " \"'<>#&=";
    static final String QUERY_ENCODE_SET = " \"'<>#";
    static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    @Nullable
    private final String fragment;
    final String host;
    private final String password;
    private final List<String> pathSegments;
    final int port;
    @Nullable
    private final List<String> queryNamesAndValues;
    final String scheme;
    private final String url;
    private final String username;

    HttpUrl(Builder builder) {
        List<String> list;
        this.scheme = builder.scheme;
        this.username = percentDecode(builder.encodedUsername, false);
        this.password = percentDecode(builder.encodedPassword, false);
        this.host = builder.host;
        this.port = builder.effectivePort();
        this.pathSegments = percentDecode(builder.encodedPathSegments, false);
        String str = null;
        if (builder.encodedQueryNamesAndValues != null) {
            list = percentDecode(builder.encodedQueryNamesAndValues, true);
        } else {
            list = null;
        }
        this.queryNamesAndValues = list;
        this.fragment = builder.encodedFragment != null ? percentDecode(builder.encodedFragment, false) : str;
        this.url = builder.toString();
    }

    public URL url() {
        try {
            return new URL(this.url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public URI uri() {
        String uri = newBuilder().reencodeForUri().toString();
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            try {
                return URI.create(uri.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\p{javaWhitespace}]", ""));
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    public String scheme() {
        return this.scheme;
    }

    public boolean isHttps() {
        return this.scheme.equals(IntentFilterEx.SCHEME_HTTPS);
    }

    public String encodedUsername() {
        if (this.username.isEmpty()) {
            return "";
        }
        int usernameStart = this.scheme.length() + 3;
        String str = this.url;
        return this.url.substring(usernameStart, Util.delimiterOffset(str, usernameStart, str.length(), ":@"));
    }

    public String username() {
        return this.username;
    }

    public String encodedPassword() {
        if (this.password.isEmpty()) {
            return "";
        }
        int passwordEnd = this.url.indexOf(64);
        return this.url.substring(this.url.indexOf(58, this.scheme.length() + 3) + 1, passwordEnd);
    }

    public String password() {
        return this.password;
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public static int defaultPort(String scheme2) {
        if (scheme2.equals(IntentFilterEx.SCHEME_HTTP)) {
            return 80;
        }
        if (scheme2.equals(IntentFilterEx.SCHEME_HTTPS)) {
            return 443;
        }
        return -1;
    }

    public int pathSize() {
        return this.pathSegments.size();
    }

    public String encodedPath() {
        int pathStart = this.url.indexOf(47, this.scheme.length() + 3);
        String str = this.url;
        return this.url.substring(pathStart, Util.delimiterOffset(str, pathStart, str.length(), "?#"));
    }

    static void pathSegmentsToString(StringBuilder out, List<String> pathSegments2) {
        int size = pathSegments2.size();
        for (int i = 0; i < size; i++) {
            out.append('/');
            out.append(pathSegments2.get(i));
        }
    }

    public List<String> encodedPathSegments() {
        int pathStart = this.url.indexOf(47, this.scheme.length() + 3);
        String str = this.url;
        int pathEnd = Util.delimiterOffset(str, pathStart, str.length(), "?#");
        List<String> result = new ArrayList<>();
        int i = pathStart;
        while (i < pathEnd) {
            int i2 = i + 1;
            int segmentEnd = Util.delimiterOffset(this.url, i2, pathEnd, '/');
            result.add(this.url.substring(i2, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    public List<String> pathSegments() {
        return this.pathSegments;
    }

    @Nullable
    public String encodedQuery() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        int queryStart = this.url.indexOf(63) + 1;
        String str = this.url;
        return this.url.substring(queryStart, Util.delimiterOffset(str, queryStart, str.length(), '#'));
    }

    static void namesAndValuesToQueryString(StringBuilder out, List<String> namesAndValues) {
        int size = namesAndValues.size();
        for (int i = 0; i < size; i += 2) {
            String name = namesAndValues.get(i);
            String value = namesAndValues.get(i + 1);
            if (i > 0) {
                out.append('&');
            }
            out.append(name);
            if (value != null) {
                out.append('=');
                out.append(value);
            }
        }
    }

    static List<String> queryStringToNamesAndValues(String encodedQuery) {
        List<String> result = new ArrayList<>();
        int pos = 0;
        while (pos <= encodedQuery.length()) {
            int ampersandOffset = encodedQuery.indexOf(38, pos);
            if (ampersandOffset == -1) {
                ampersandOffset = encodedQuery.length();
            }
            int equalsOffset = encodedQuery.indexOf(61, pos);
            if (equalsOffset == -1 || equalsOffset > ampersandOffset) {
                result.add(encodedQuery.substring(pos, ampersandOffset));
                result.add(null);
            } else {
                result.add(encodedQuery.substring(pos, equalsOffset));
                result.add(encodedQuery.substring(equalsOffset + 1, ampersandOffset));
            }
            pos = ampersandOffset + 1;
        }
        return result;
    }

    @Nullable
    public String query() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, this.queryNamesAndValues);
        return result.toString();
    }

    public int querySize() {
        List<String> list = this.queryNamesAndValues;
        if (list != null) {
            return list.size() / 2;
        }
        return 0;
    }

    @Nullable
    public String queryParameter(String name) {
        List<String> list = this.queryNamesAndValues;
        if (list == null) {
            return null;
        }
        int size = list.size();
        for (int i = 0; i < size; i += 2) {
            if (name.equals(this.queryNamesAndValues.get(i))) {
                return this.queryNamesAndValues.get(i + 1);
            }
        }
        return null;
    }

    public Set<String> queryParameterNames() {
        if (this.queryNamesAndValues == null) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        int size = this.queryNamesAndValues.size();
        for (int i = 0; i < size; i += 2) {
            result.add(this.queryNamesAndValues.get(i));
        }
        return Collections.unmodifiableSet(result);
    }

    public List<String> queryParameterValues(String name) {
        if (this.queryNamesAndValues == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        int size = this.queryNamesAndValues.size();
        for (int i = 0; i < size; i += 2) {
            if (name.equals(this.queryNamesAndValues.get(i))) {
                result.add(this.queryNamesAndValues.get(i + 1));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public String queryParameterName(int index) {
        List<String> list = this.queryNamesAndValues;
        if (list != null) {
            return list.get(index * 2);
        }
        throw new IndexOutOfBoundsException();
    }

    public String queryParameterValue(int index) {
        List<String> list = this.queryNamesAndValues;
        if (list != null) {
            return list.get((index * 2) + 1);
        }
        throw new IndexOutOfBoundsException();
    }

    @Nullable
    public String encodedFragment() {
        if (this.fragment == null) {
            return null;
        }
        return this.url.substring(this.url.indexOf(35) + 1);
    }

    @Nullable
    public String fragment() {
        return this.fragment;
    }

    public String redact() {
        return newBuilder("/...").username("").password("").build().toString();
    }

    @Nullable
    public HttpUrl resolve(String link) {
        Builder builder = newBuilder(link);
        if (builder != null) {
            return builder.build();
        }
        return null;
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        result.scheme = this.scheme;
        result.encodedUsername = encodedUsername();
        result.encodedPassword = encodedPassword();
        result.host = this.host;
        result.port = this.port != defaultPort(this.scheme) ? this.port : -1;
        result.encodedPathSegments.clear();
        result.encodedPathSegments.addAll(encodedPathSegments());
        result.encodedQuery(encodedQuery());
        result.encodedFragment = encodedFragment();
        return result;
    }

    @Nullable
    public Builder newBuilder(String link) {
        try {
            return new Builder().parse(this, link);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public static HttpUrl parse(String url2) {
        try {
            return get(url2);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static HttpUrl get(String url2) {
        return new Builder().parse(null, url2).build();
    }

    @Nullable
    public static HttpUrl get(URL url2) {
        return parse(url2.toString());
    }

    @Nullable
    public static HttpUrl get(URI uri) {
        return parse(uri.toString());
    }

    public boolean equals(@Nullable Object other) {
        return (other instanceof HttpUrl) && ((HttpUrl) other).url.equals(this.url);
    }

    public int hashCode() {
        return this.url.hashCode();
    }

    public String toString() {
        return this.url;
    }

    @Nullable
    public String topPrivateDomain() {
        if (Util.verifyAsIpAddress(this.host)) {
            return null;
        }
        return PublicSuffixDatabase.get().getEffectiveTldPlusOne(this.host);
    }

    public static final class Builder {
        static final String INVALID_HOST = "Invalid URL host";
        @Nullable
        String encodedFragment;
        String encodedPassword = "";
        final List<String> encodedPathSegments = new ArrayList();
        @Nullable
        List<String> encodedQueryNamesAndValues;
        String encodedUsername = "";
        @Nullable
        String host;
        int port = -1;
        @Nullable
        String scheme;

        public Builder() {
            this.encodedPathSegments.add("");
        }

        public Builder scheme(String scheme2) {
            if (scheme2 != null) {
                if (scheme2.equalsIgnoreCase(IntentFilterEx.SCHEME_HTTP)) {
                    this.scheme = IntentFilterEx.SCHEME_HTTP;
                } else if (scheme2.equalsIgnoreCase(IntentFilterEx.SCHEME_HTTPS)) {
                    this.scheme = IntentFilterEx.SCHEME_HTTPS;
                } else {
                    throw new IllegalArgumentException("unexpected scheme: " + scheme2);
                }
                return this;
            }
            throw new NullPointerException("scheme == null");
        }

        public Builder username(String username) {
            if (username != null) {
                this.encodedUsername = HttpUrl.canonicalize(username, " \"':;<=>@[]^`{}|/\\?#", false, false, false, true);
                return this;
            }
            throw new NullPointerException("username == null");
        }

        public Builder encodedUsername(String encodedUsername2) {
            if (encodedUsername2 != null) {
                this.encodedUsername = HttpUrl.canonicalize(encodedUsername2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                return this;
            }
            throw new NullPointerException("encodedUsername == null");
        }

        public Builder password(String password) {
            if (password != null) {
                this.encodedPassword = HttpUrl.canonicalize(password, " \"':;<=>@[]^`{}|/\\?#", false, false, false, true);
                return this;
            }
            throw new NullPointerException("password == null");
        }

        public Builder encodedPassword(String encodedPassword2) {
            if (encodedPassword2 != null) {
                this.encodedPassword = HttpUrl.canonicalize(encodedPassword2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                return this;
            }
            throw new NullPointerException("encodedPassword == null");
        }

        public Builder host(String host2) {
            if (host2 != null) {
                String encoded = canonicalizeHost(host2, 0, host2.length());
                if (encoded != null) {
                    this.host = encoded;
                    return this;
                }
                throw new IllegalArgumentException("unexpected host: " + host2);
            }
            throw new NullPointerException("host == null");
        }

        public Builder port(int port2) {
            if (port2 <= 0 || port2 > 65535) {
                throw new IllegalArgumentException("unexpected port: " + port2);
            }
            this.port = port2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public int effectivePort() {
            int i = this.port;
            return i != -1 ? i : HttpUrl.defaultPort(this.scheme);
        }

        public Builder addPathSegment(String pathSegment) {
            if (pathSegment != null) {
                push(pathSegment, 0, pathSegment.length(), false, false);
                return this;
            }
            throw new NullPointerException("pathSegment == null");
        }

        public Builder addPathSegments(String pathSegments) {
            if (pathSegments != null) {
                return addPathSegments(pathSegments, false);
            }
            throw new NullPointerException("pathSegments == null");
        }

        public Builder addEncodedPathSegment(String encodedPathSegment) {
            if (encodedPathSegment != null) {
                push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
                return this;
            }
            throw new NullPointerException("encodedPathSegment == null");
        }

        public Builder addEncodedPathSegments(String encodedPathSegments2) {
            if (encodedPathSegments2 != null) {
                return addPathSegments(encodedPathSegments2, true);
            }
            throw new NullPointerException("encodedPathSegments == null");
        }

        private Builder addPathSegments(String pathSegments, boolean alreadyEncoded) {
            int offset = 0;
            do {
                int segmentEnd = Util.delimiterOffset(pathSegments, offset, pathSegments.length(), "/\\");
                push(pathSegments, offset, segmentEnd, segmentEnd < pathSegments.length(), alreadyEncoded);
                offset = segmentEnd + 1;
            } while (offset <= pathSegments.length());
            return this;
        }

        public Builder setPathSegment(int index, String pathSegment) {
            if (pathSegment != null) {
                String canonicalPathSegment = HttpUrl.canonicalize(pathSegment, 0, pathSegment.length(), HttpUrl.PATH_SEGMENT_ENCODE_SET, false, false, false, true, null);
                if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                    throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
                }
                this.encodedPathSegments.set(index, canonicalPathSegment);
                return this;
            }
            throw new NullPointerException("pathSegment == null");
        }

        public Builder setEncodedPathSegment(int index, String encodedPathSegment) {
            if (encodedPathSegment != null) {
                String canonicalPathSegment = HttpUrl.canonicalize(encodedPathSegment, 0, encodedPathSegment.length(), HttpUrl.PATH_SEGMENT_ENCODE_SET, true, false, false, true, null);
                this.encodedPathSegments.set(index, canonicalPathSegment);
                if (!isDot(canonicalPathSegment) && !isDotDot(canonicalPathSegment)) {
                    return this;
                }
                throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
            }
            throw new NullPointerException("encodedPathSegment == null");
        }

        public Builder removePathSegment(int index) {
            this.encodedPathSegments.remove(index);
            if (this.encodedPathSegments.isEmpty()) {
                this.encodedPathSegments.add("");
            }
            return this;
        }

        public Builder encodedPath(String encodedPath) {
            if (encodedPath == null) {
                throw new NullPointerException("encodedPath == null");
            } else if (encodedPath.startsWith("/")) {
                resolvePath(encodedPath, 0, encodedPath.length());
                return this;
            } else {
                throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
            }
        }

        public Builder query(@Nullable String query) {
            List<String> list;
            if (query != null) {
                list = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(query, HttpUrl.QUERY_ENCODE_SET, false, false, true, true));
            } else {
                list = null;
            }
            this.encodedQueryNamesAndValues = list;
            return this;
        }

        public Builder encodedQuery(@Nullable String encodedQuery) {
            List<String> list;
            if (encodedQuery != null) {
                list = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(encodedQuery, HttpUrl.QUERY_ENCODE_SET, true, false, true, true));
            } else {
                list = null;
            }
            this.encodedQueryNamesAndValues = list;
            return this;
        }

        public Builder addQueryParameter(String name, @Nullable String value) {
            String str;
            if (name != null) {
                if (this.encodedQueryNamesAndValues == null) {
                    this.encodedQueryNamesAndValues = new ArrayList();
                }
                this.encodedQueryNamesAndValues.add(HttpUrl.canonicalize(name, HttpUrl.QUERY_COMPONENT_ENCODE_SET, false, false, true, true));
                List<String> list = this.encodedQueryNamesAndValues;
                if (value != null) {
                    str = HttpUrl.canonicalize(value, HttpUrl.QUERY_COMPONENT_ENCODE_SET, false, false, true, true);
                } else {
                    str = null;
                }
                list.add(str);
                return this;
            }
            throw new NullPointerException("name == null");
        }

        public Builder addEncodedQueryParameter(String encodedName, @Nullable String encodedValue) {
            String str;
            if (encodedName != null) {
                if (this.encodedQueryNamesAndValues == null) {
                    this.encodedQueryNamesAndValues = new ArrayList();
                }
                this.encodedQueryNamesAndValues.add(HttpUrl.canonicalize(encodedName, HttpUrl.QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
                List<String> list = this.encodedQueryNamesAndValues;
                if (encodedValue != null) {
                    str = HttpUrl.canonicalize(encodedValue, HttpUrl.QUERY_COMPONENT_REENCODE_SET, true, false, true, true);
                } else {
                    str = null;
                }
                list.add(str);
                return this;
            }
            throw new NullPointerException("encodedName == null");
        }

        public Builder setQueryParameter(String name, @Nullable String value) {
            removeAllQueryParameters(name);
            addQueryParameter(name, value);
            return this;
        }

        public Builder setEncodedQueryParameter(String encodedName, @Nullable String encodedValue) {
            removeAllEncodedQueryParameters(encodedName);
            addEncodedQueryParameter(encodedName, encodedValue);
            return this;
        }

        public Builder removeAllQueryParameters(String name) {
            if (name == null) {
                throw new NullPointerException("name == null");
            } else if (this.encodedQueryNamesAndValues == null) {
                return this;
            } else {
                removeAllCanonicalQueryParameters(HttpUrl.canonicalize(name, HttpUrl.QUERY_COMPONENT_ENCODE_SET, false, false, true, true));
                return this;
            }
        }

        public Builder removeAllEncodedQueryParameters(String encodedName) {
            if (encodedName == null) {
                throw new NullPointerException("encodedName == null");
            } else if (this.encodedQueryNamesAndValues == null) {
                return this;
            } else {
                removeAllCanonicalQueryParameters(HttpUrl.canonicalize(encodedName, HttpUrl.QUERY_COMPONENT_REENCODE_SET, true, false, true, true));
                return this;
            }
        }

        private void removeAllCanonicalQueryParameters(String canonicalName) {
            for (int i = this.encodedQueryNamesAndValues.size() - 2; i >= 0; i -= 2) {
                if (canonicalName.equals(this.encodedQueryNamesAndValues.get(i))) {
                    this.encodedQueryNamesAndValues.remove(i + 1);
                    this.encodedQueryNamesAndValues.remove(i);
                    if (this.encodedQueryNamesAndValues.isEmpty()) {
                        this.encodedQueryNamesAndValues = null;
                        return;
                    }
                }
            }
        }

        public Builder fragment(@Nullable String fragment) {
            String str;
            if (fragment != null) {
                str = HttpUrl.canonicalize(fragment, "", false, false, false, false);
            } else {
                str = null;
            }
            this.encodedFragment = str;
            return this;
        }

        public Builder encodedFragment(@Nullable String encodedFragment2) {
            String str;
            if (encodedFragment2 != null) {
                str = HttpUrl.canonicalize(encodedFragment2, "", true, false, false, false);
            } else {
                str = null;
            }
            this.encodedFragment = str;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder reencodeForUri() {
            int size = this.encodedPathSegments.size();
            for (int i = 0; i < size; i++) {
                this.encodedPathSegments.set(i, HttpUrl.canonicalize(this.encodedPathSegments.get(i), HttpUrl.PATH_SEGMENT_ENCODE_SET_URI, true, true, false, true));
            }
            List<String> list = this.encodedQueryNamesAndValues;
            if (list != null) {
                int size2 = list.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    String component = this.encodedQueryNamesAndValues.get(i2);
                    if (component != null) {
                        this.encodedQueryNamesAndValues.set(i2, HttpUrl.canonicalize(component, HttpUrl.QUERY_COMPONENT_ENCODE_SET_URI, true, true, true, true));
                    }
                }
            }
            String str = this.encodedFragment;
            if (str != null) {
                this.encodedFragment = HttpUrl.canonicalize(str, HttpUrl.FRAGMENT_ENCODE_SET_URI, true, true, false, false);
            }
            return this;
        }

        public HttpUrl build() {
            if (this.scheme == null) {
                throw new IllegalStateException("scheme == null");
            } else if (this.host != null) {
                return new HttpUrl(this);
            } else {
                throw new IllegalStateException("host == null");
            }
        }

        public String toString() {
            StringBuilder result = new StringBuilder();
            String str = this.scheme;
            if (str != null) {
                result.append(str);
                result.append("://");
            } else {
                result.append("//");
            }
            if (!this.encodedUsername.isEmpty() || !this.encodedPassword.isEmpty()) {
                result.append(this.encodedUsername);
                if (!this.encodedPassword.isEmpty()) {
                    result.append(':');
                    result.append(this.encodedPassword);
                }
                result.append('@');
            }
            String str2 = this.host;
            if (str2 != null) {
                if (str2.indexOf(58) != -1) {
                    result.append('[');
                    result.append(this.host);
                    result.append(']');
                } else {
                    result.append(this.host);
                }
            }
            if (!(this.port == -1 && this.scheme == null)) {
                int effectivePort = effectivePort();
                String str3 = this.scheme;
                if (str3 == null || effectivePort != HttpUrl.defaultPort(str3)) {
                    result.append(':');
                    result.append(effectivePort);
                }
            }
            HttpUrl.pathSegmentsToString(result, this.encodedPathSegments);
            if (this.encodedQueryNamesAndValues != null) {
                result.append('?');
                HttpUrl.namesAndValuesToQueryString(result, this.encodedQueryNamesAndValues);
            }
            if (this.encodedFragment != null) {
                result.append('#');
                result.append(this.encodedFragment);
            }
            return result.toString();
        }

        /* access modifiers changed from: package-private */
        public Builder parse(@Nullable HttpUrl base, String input) {
            int pathDelimiterOffset;
            int componentDelimiterOffset;
            int c;
            int componentDelimiterOffset2;
            String str;
            int pos = Util.skipLeadingAsciiWhitespace(input, 0, input.length());
            int limit = Util.skipTrailingAsciiWhitespace(input, pos, input.length());
            int schemeDelimiterOffset = schemeDelimiterOffset(input, pos, limit);
            int i = -1;
            if (schemeDelimiterOffset != -1) {
                if (input.regionMatches(true, pos, "https:", 0, 6)) {
                    this.scheme = IntentFilterEx.SCHEME_HTTPS;
                    pos += "https:".length();
                } else if (input.regionMatches(true, pos, "http:", 0, 5)) {
                    this.scheme = IntentFilterEx.SCHEME_HTTP;
                    pos += "http:".length();
                } else {
                    throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but was '" + input.substring(0, schemeDelimiterOffset) + "'");
                }
            } else if (base != null) {
                this.scheme = base.scheme;
            } else {
                throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but no colon was found");
            }
            int slashCount = slashCount(input, pos, limit);
            int i2 = 63;
            int i3 = 35;
            if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
                int pos2 = pos + slashCount;
                boolean hasUsername = false;
                boolean hasPassword = false;
                while (true) {
                    componentDelimiterOffset = Util.delimiterOffset(input, pos2, limit, "@/\\?#");
                    if (componentDelimiterOffset != limit) {
                        c = input.charAt(componentDelimiterOffset);
                    } else {
                        c = i;
                    }
                    if (c == i || c == i3 || c == 47 || c == 92 || c == i2) {
                        break;
                    }
                    if (c == 64) {
                        if (!hasPassword) {
                            int passwordColonOffset = Util.delimiterOffset(input, pos2, componentDelimiterOffset, ':');
                            String canonicalUsername = HttpUrl.canonicalize(input, pos2, passwordColonOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true, null);
                            if (hasUsername) {
                                str = this.encodedUsername + "%40" + canonicalUsername;
                            } else {
                                str = canonicalUsername;
                            }
                            this.encodedUsername = str;
                            componentDelimiterOffset2 = componentDelimiterOffset;
                            if (passwordColonOffset != componentDelimiterOffset2) {
                                hasPassword = true;
                                this.encodedPassword = HttpUrl.canonicalize(input, passwordColonOffset + 1, componentDelimiterOffset2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true, null);
                            }
                            hasUsername = true;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append(this.encodedPassword);
                            sb.append("%40");
                            componentDelimiterOffset2 = componentDelimiterOffset;
                            sb.append(HttpUrl.canonicalize(input, pos2, componentDelimiterOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true, null));
                            this.encodedPassword = sb.toString();
                        }
                        pos2 = componentDelimiterOffset2 + 1;
                    }
                    i3 = 35;
                    i2 = 63;
                    i = -1;
                }
                int portColonOffset = portColonOffset(input, pos2, componentDelimiterOffset);
                if (portColonOffset + 1 < componentDelimiterOffset) {
                    this.host = canonicalizeHost(input, pos2, portColonOffset);
                    this.port = parsePort(input, portColonOffset + 1, componentDelimiterOffset);
                    if (this.port == -1) {
                        throw new IllegalArgumentException("Invalid URL port: \"" + input.substring(portColonOffset + 1, componentDelimiterOffset) + '\"');
                    }
                } else {
                    this.host = canonicalizeHost(input, pos2, portColonOffset);
                    this.port = HttpUrl.defaultPort(this.scheme);
                }
                if (this.host != null) {
                    pos = componentDelimiterOffset;
                } else {
                    throw new IllegalArgumentException("Invalid URL host: \"" + input.substring(pos2, portColonOffset) + '\"');
                }
            } else {
                this.encodedUsername = base.encodedUsername();
                this.encodedPassword = base.encodedPassword();
                this.host = base.host;
                this.port = base.port;
                this.encodedPathSegments.clear();
                this.encodedPathSegments.addAll(base.encodedPathSegments());
                if (pos == limit || input.charAt(pos) == '#') {
                    encodedQuery(base.encodedQuery());
                }
            }
            int pathDelimiterOffset2 = Util.delimiterOffset(input, pos, limit, "?#");
            resolvePath(input, pos, pathDelimiterOffset2);
            int pos3 = pathDelimiterOffset2;
            if (pos3 >= limit || input.charAt(pos3) != '?') {
                pathDelimiterOffset = 35;
            } else {
                int queryDelimiterOffset = Util.delimiterOffset(input, pos3, limit, '#');
                pathDelimiterOffset = 35;
                this.encodedQueryNamesAndValues = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(input, pos3 + 1, queryDelimiterOffset, HttpUrl.QUERY_ENCODE_SET, true, false, true, true, null));
                pos3 = queryDelimiterOffset;
            }
            if (pos3 < limit && input.charAt(pos3) == pathDelimiterOffset) {
                this.encodedFragment = HttpUrl.canonicalize(input, pos3 + 1, limit, "", true, false, false, false, null);
            }
            return this;
        }

        private void resolvePath(String input, int pos, int limit) {
            if (pos != limit) {
                char c = input.charAt(pos);
                if (c == '/' || c == '\\') {
                    this.encodedPathSegments.clear();
                    this.encodedPathSegments.add("");
                    pos++;
                } else {
                    List<String> list = this.encodedPathSegments;
                    list.set(list.size() - 1, "");
                }
                int i = pos;
                while (i < limit) {
                    int pathSegmentDelimiterOffset = Util.delimiterOffset(input, i, limit, "/\\");
                    boolean segmentHasTrailingSlash = pathSegmentDelimiterOffset < limit;
                    push(input, i, pathSegmentDelimiterOffset, segmentHasTrailingSlash, true);
                    i = pathSegmentDelimiterOffset;
                    if (segmentHasTrailingSlash) {
                        i++;
                    }
                }
            }
        }

        private void push(String input, int pos, int limit, boolean addTrailingSlash, boolean alreadyEncoded) {
            String segment = HttpUrl.canonicalize(input, pos, limit, HttpUrl.PATH_SEGMENT_ENCODE_SET, alreadyEncoded, false, false, true, null);
            if (!isDot(segment)) {
                if (isDotDot(segment)) {
                    pop();
                    return;
                }
                List<String> list = this.encodedPathSegments;
                if (list.get(list.size() - 1).isEmpty()) {
                    List<String> list2 = this.encodedPathSegments;
                    list2.set(list2.size() - 1, segment);
                } else {
                    this.encodedPathSegments.add(segment);
                }
                if (addTrailingSlash) {
                    this.encodedPathSegments.add("");
                }
            }
        }

        private boolean isDot(String input) {
            return input.equals(".") || input.equalsIgnoreCase("%2e");
        }

        private boolean isDotDot(String input) {
            return input.equals("..") || input.equalsIgnoreCase("%2e.") || input.equalsIgnoreCase(".%2e") || input.equalsIgnoreCase("%2e%2e");
        }

        private void pop() {
            List<String> list = this.encodedPathSegments;
            if (!list.remove(list.size() - 1).isEmpty() || this.encodedPathSegments.isEmpty()) {
                this.encodedPathSegments.add("");
                return;
            }
            List<String> list2 = this.encodedPathSegments;
            list2.set(list2.size() - 1, "");
        }

        private static int schemeDelimiterOffset(String input, int pos, int limit) {
            if (limit - pos < 2) {
                return -1;
            }
            char c0 = input.charAt(pos);
            if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z')) {
                return -1;
            }
            for (int i = pos + 1; i < limit; i++) {
                char c = input.charAt(i);
                if ((c < 'a' || c > 'z') && ((c < 'A' || c > 'Z') && ((c < '0' || c > '9') && c != '+' && c != '-' && c != '.'))) {
                    if (c == ':') {
                        return i;
                    } else {
                        return -1;
                    }
                }
            }
            return -1;
        }

        private static int slashCount(String input, int pos, int limit) {
            int slashCount = 0;
            while (pos < limit) {
                char c = input.charAt(pos);
                if (c != '\\' && c != '/') {
                    break;
                }
                slashCount++;
                pos++;
            }
            return slashCount;
        }

        private static int portColonOffset(String input, int pos, int limit) {
            int i = pos;
            while (i < limit) {
                char charAt = input.charAt(i);
                if (charAt == ':') {
                    return i;
                }
                if (charAt == '[') {
                    do {
                        i++;
                        if (i >= limit) {
                            break;
                        }
                    } while (input.charAt(i) != ']');
                }
                i++;
            }
            return limit;
        }

        @Nullable
        private static String canonicalizeHost(String input, int pos, int limit) {
            return Util.canonicalizeHost(HttpUrl.percentDecode(input, pos, limit, false));
        }

        private static int parsePort(String input, int pos, int limit) {
            try {
                int i = Integer.parseInt(HttpUrl.canonicalize(input, pos, limit, "", false, false, false, true, null));
                if (i <= 0 || i > 65535) {
                    return -1;
                }
                return i;
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }

    static String percentDecode(String encoded, boolean plusIsSpace) {
        return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
    }

    private List<String> percentDecode(List<String> list, boolean plusIsSpace) {
        int size = list.size();
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String s = list.get(i);
            result.add(s != null ? percentDecode(s, plusIsSpace) : null);
        }
        return Collections.unmodifiableList(result);
    }

    static String percentDecode(String encoded, int pos, int limit, boolean plusIsSpace) {
        for (int i = pos; i < limit; i++) {
            char c = encoded.charAt(i);
            if (c == '%' || (c == '+' && plusIsSpace)) {
                Buffer out = new Buffer();
                out.writeUtf8(encoded, pos, i);
                percentDecode(out, encoded, i, limit, plusIsSpace);
                return out.readUtf8();
            }
        }
        return encoded.substring(pos, limit);
    }

    static void percentDecode(Buffer out, String encoded, int pos, int limit, boolean plusIsSpace) {
        int i = pos;
        while (i < limit) {
            int codePoint = encoded.codePointAt(i);
            if (codePoint == 37 && i + 2 < limit) {
                int d1 = Util.decodeHexDigit(encoded.charAt(i + 1));
                int d2 = Util.decodeHexDigit(encoded.charAt(i + 2));
                if (!(d1 == -1 || d2 == -1)) {
                    out.writeByte((d1 << 4) + d2);
                    i += 2;
                    i += Character.charCount(codePoint);
                }
            } else if (codePoint == 43 && plusIsSpace) {
                out.writeByte(32);
                i += Character.charCount(codePoint);
            }
            out.writeUtf8CodePoint(codePoint);
            i += Character.charCount(codePoint);
        }
    }

    static boolean percentEncoded(String encoded, int pos, int limit) {
        return pos + 2 < limit && encoded.charAt(pos) == '%' && Util.decodeHexDigit(encoded.charAt(pos + 1)) != -1 && Util.decodeHexDigit(encoded.charAt(pos + 2)) != -1;
    }

    static String canonicalize(String input, int pos, int limit, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly, @Nullable Charset charset) {
        int i = pos;
        while (i < limit) {
            int codePoint = input.codePointAt(i);
            if (codePoint >= 32 && codePoint != 127) {
                if (codePoint < 128 || !asciiOnly) {
                    if (encodeSet.indexOf(codePoint) == -1 && ((codePoint != 37 || (alreadyEncoded && (!strict || percentEncoded(input, i, limit)))) && (codePoint != 43 || !plusIsSpace))) {
                        i += Character.charCount(codePoint);
                    }
                }
            }
            Buffer out = new Buffer();
            out.writeUtf8(input, pos, i);
            canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, charset);
            return out.readUtf8();
        }
        return input.substring(pos, limit);
    }

    static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly, @Nullable Charset charset) {
        Buffer encodedCharBuffer = null;
        int i = pos;
        while (i < limit) {
            int codePoint = input.codePointAt(i);
            if (!alreadyEncoded || !(codePoint == 9 || codePoint == 10 || codePoint == 12 || codePoint == 13)) {
                if (codePoint != 43 || !plusIsSpace) {
                    if (codePoint >= 32 && codePoint != 127) {
                        if (codePoint < 128 || !asciiOnly) {
                            if (encodeSet.indexOf(codePoint) == -1 && (codePoint != 37 || (alreadyEncoded && (!strict || percentEncoded(input, i, limit))))) {
                                out.writeUtf8CodePoint(codePoint);
                            }
                        }
                    }
                    if (encodedCharBuffer == null) {
                        encodedCharBuffer = new Buffer();
                    }
                    if (charset == null || charset.equals(StandardCharsets.UTF_8)) {
                        encodedCharBuffer.writeUtf8CodePoint(codePoint);
                    } else {
                        encodedCharBuffer.writeString(input, i, Character.charCount(codePoint) + i, charset);
                    }
                    while (!encodedCharBuffer.exhausted()) {
                        int b = encodedCharBuffer.readByte() & 255;
                        out.writeByte(37);
                        out.writeByte((int) HEX_DIGITS[(b >> 4) & 15]);
                        out.writeByte((int) HEX_DIGITS[b & 15]);
                    }
                } else {
                    out.writeUtf8(alreadyEncoded ? "+" : "%2B");
                }
            }
            i += Character.charCount(codePoint);
        }
    }

    static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly, @Nullable Charset charset) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, charset);
    }

    static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, null);
    }
}
