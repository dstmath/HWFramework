package com.android.okhttp;

import com.android.okhttp.okio.Buffer;
import java.net.IDN;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class HttpUrl {
    static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    static final String FRAGMENT_ENCODE_SET = "";
    static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    static final String PATH_SEGMENT_ENCODE_SET_URI = "[]";
    static final String QUERY_COMPONENT_ENCODE_SET = " \"<>#&=";
    static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    static final String QUERY_ENCODE_SET = " \"<>#";
    static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    private final String fragment;
    /* access modifiers changed from: private */
    public final String host;
    private final String password;
    private final List<String> pathSegments;
    /* access modifiers changed from: private */
    public final int port;
    private final List<String> queryNamesAndValues;
    /* access modifiers changed from: private */
    public final String scheme;
    private final String url;
    private final String username;

    public static final class Builder {
        String encodedFragment;
        String encodedPassword = HttpUrl.FRAGMENT_ENCODE_SET;
        final List<String> encodedPathSegments = new ArrayList();
        List<String> encodedQueryNamesAndValues;
        String encodedUsername = HttpUrl.FRAGMENT_ENCODE_SET;
        String host;
        int port = -1;
        String scheme;

        enum ParseResult {
            SUCCESS,
            MISSING_SCHEME,
            UNSUPPORTED_SCHEME,
            INVALID_PORT,
            INVALID_HOST
        }

        public Builder() {
            this.encodedPathSegments.add(HttpUrl.FRAGMENT_ENCODE_SET);
        }

        public Builder scheme(String scheme2) {
            if (scheme2 != null) {
                if (scheme2.equalsIgnoreCase("http")) {
                    this.scheme = "http";
                } else if (scheme2.equalsIgnoreCase("https")) {
                    this.scheme = "https";
                } else {
                    throw new IllegalArgumentException("unexpected scheme: " + scheme2);
                }
                return this;
            }
            throw new IllegalArgumentException("scheme == null");
        }

        public Builder username(String username) {
            if (username != null) {
                this.encodedUsername = HttpUrl.canonicalize(username, " \"':;<=>@[]^`{}|/\\?#", false, false, false, true);
                return this;
            }
            throw new IllegalArgumentException("username == null");
        }

        public Builder encodedUsername(String encodedUsername2) {
            if (encodedUsername2 != null) {
                this.encodedUsername = HttpUrl.canonicalize(encodedUsername2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                return this;
            }
            throw new IllegalArgumentException("encodedUsername == null");
        }

        public Builder password(String password) {
            if (password != null) {
                this.encodedPassword = HttpUrl.canonicalize(password, " \"':;<=>@[]^`{}|/\\?#", false, false, false, true);
                return this;
            }
            throw new IllegalArgumentException("password == null");
        }

        public Builder encodedPassword(String encodedPassword2) {
            if (encodedPassword2 != null) {
                this.encodedPassword = HttpUrl.canonicalize(encodedPassword2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                return this;
            }
            throw new IllegalArgumentException("encodedPassword == null");
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
            throw new IllegalArgumentException("host == null");
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
            return this.port != -1 ? this.port : HttpUrl.defaultPort(this.scheme);
        }

        public Builder addPathSegment(String pathSegment) {
            if (pathSegment != null) {
                push(pathSegment, 0, pathSegment.length(), false, false);
                return this;
            }
            throw new IllegalArgumentException("pathSegment == null");
        }

        public Builder addEncodedPathSegment(String encodedPathSegment) {
            if (encodedPathSegment != null) {
                push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
                return this;
            }
            throw new IllegalArgumentException("encodedPathSegment == null");
        }

        public Builder setPathSegment(int index, String pathSegment) {
            if (pathSegment != null) {
                String canonicalPathSegment = HttpUrl.canonicalize(pathSegment, 0, pathSegment.length(), HttpUrl.PATH_SEGMENT_ENCODE_SET, false, false, false, true);
                if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
                    throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
                }
                this.encodedPathSegments.set(index, canonicalPathSegment);
                return this;
            }
            throw new IllegalArgumentException("pathSegment == null");
        }

        public Builder setEncodedPathSegment(int index, String encodedPathSegment) {
            if (encodedPathSegment != null) {
                String canonicalPathSegment = HttpUrl.canonicalize(encodedPathSegment, 0, encodedPathSegment.length(), HttpUrl.PATH_SEGMENT_ENCODE_SET, true, false, false, true);
                this.encodedPathSegments.set(index, canonicalPathSegment);
                if (!isDot(canonicalPathSegment) && !isDotDot(canonicalPathSegment)) {
                    return this;
                }
                throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
            }
            throw new IllegalArgumentException("encodedPathSegment == null");
        }

        public Builder removePathSegment(int index) {
            this.encodedPathSegments.remove(index);
            if (this.encodedPathSegments.isEmpty()) {
                this.encodedPathSegments.add(HttpUrl.FRAGMENT_ENCODE_SET);
            }
            return this;
        }

        public Builder encodedPath(String encodedPath) {
            if (encodedPath == null) {
                throw new IllegalArgumentException("encodedPath == null");
            } else if (encodedPath.startsWith("/")) {
                resolvePath(encodedPath, 0, encodedPath.length());
                return this;
            } else {
                throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
            }
        }

        public Builder query(String query) {
            List<String> list;
            if (query != null) {
                list = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(query, HttpUrl.QUERY_ENCODE_SET, false, false, true, true));
            } else {
                list = null;
            }
            this.encodedQueryNamesAndValues = list;
            return this;
        }

        public Builder encodedQuery(String encodedQuery) {
            List<String> list;
            if (encodedQuery != null) {
                list = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(encodedQuery, HttpUrl.QUERY_ENCODE_SET, true, false, true, true));
            } else {
                list = null;
            }
            this.encodedQueryNamesAndValues = list;
            return this;
        }

        public Builder addQueryParameter(String name, String value) {
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
            throw new IllegalArgumentException("name == null");
        }

        public Builder addEncodedQueryParameter(String encodedName, String encodedValue) {
            String str;
            if (encodedName != null) {
                if (this.encodedQueryNamesAndValues == null) {
                    this.encodedQueryNamesAndValues = new ArrayList();
                }
                this.encodedQueryNamesAndValues.add(HttpUrl.canonicalize(encodedName, HttpUrl.QUERY_COMPONENT_ENCODE_SET, true, false, true, true));
                List<String> list = this.encodedQueryNamesAndValues;
                if (encodedValue != null) {
                    str = HttpUrl.canonicalize(encodedValue, HttpUrl.QUERY_COMPONENT_ENCODE_SET, true, false, true, true);
                } else {
                    str = null;
                }
                list.add(str);
                return this;
            }
            throw new IllegalArgumentException("encodedName == null");
        }

        public Builder setQueryParameter(String name, String value) {
            removeAllQueryParameters(name);
            addQueryParameter(name, value);
            return this;
        }

        public Builder setEncodedQueryParameter(String encodedName, String encodedValue) {
            removeAllEncodedQueryParameters(encodedName);
            addEncodedQueryParameter(encodedName, encodedValue);
            return this;
        }

        public Builder removeAllQueryParameters(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name == null");
            } else if (this.encodedQueryNamesAndValues == null) {
                return this;
            } else {
                removeAllCanonicalQueryParameters(HttpUrl.canonicalize(name, HttpUrl.QUERY_COMPONENT_ENCODE_SET, false, false, true, true));
                return this;
            }
        }

        public Builder removeAllEncodedQueryParameters(String encodedName) {
            if (encodedName == null) {
                throw new IllegalArgumentException("encodedName == null");
            } else if (this.encodedQueryNamesAndValues == null) {
                return this;
            } else {
                removeAllCanonicalQueryParameters(HttpUrl.canonicalize(encodedName, HttpUrl.QUERY_COMPONENT_ENCODE_SET, true, false, true, true));
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

        public Builder fragment(String fragment) {
            String str;
            if (fragment != null) {
                str = HttpUrl.canonicalize(fragment, HttpUrl.FRAGMENT_ENCODE_SET, false, false, false, false);
            } else {
                str = null;
            }
            this.encodedFragment = str;
            return this;
        }

        public Builder encodedFragment(String encodedFragment2) {
            String str;
            if (encodedFragment2 != null) {
                str = HttpUrl.canonicalize(encodedFragment2, HttpUrl.FRAGMENT_ENCODE_SET, true, false, false, false);
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
            if (this.encodedQueryNamesAndValues != null) {
                int size2 = this.encodedQueryNamesAndValues.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    String component = this.encodedQueryNamesAndValues.get(i2);
                    if (component != null) {
                        this.encodedQueryNamesAndValues.set(i2, HttpUrl.canonicalize(component, HttpUrl.QUERY_COMPONENT_ENCODE_SET_URI, true, true, true, true));
                    }
                }
            }
            if (this.encodedFragment != null) {
                this.encodedFragment = HttpUrl.canonicalize(this.encodedFragment, HttpUrl.FRAGMENT_ENCODE_SET_URI, true, true, false, false);
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
            result.append(this.scheme);
            result.append("://");
            if (!this.encodedUsername.isEmpty() || !this.encodedPassword.isEmpty()) {
                result.append(this.encodedUsername);
                if (!this.encodedPassword.isEmpty()) {
                    result.append(':');
                    result.append(this.encodedPassword);
                }
                result.append('@');
            }
            if (this.host.indexOf(58) != -1) {
                result.append('[');
                result.append(this.host);
                result.append(']');
            } else {
                result.append(this.host);
            }
            int effectivePort = effectivePort();
            if (effectivePort != HttpUrl.defaultPort(this.scheme)) {
                result.append(':');
                result.append(effectivePort);
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
        public ParseResult parse(HttpUrl base, String input) {
            int componentDelimiterOffset;
            int i;
            int i2;
            int componentDelimiterOffset2;
            String str;
            String str2 = input;
            int pos = skipLeadingAsciiWhitespace(str2, 0, input.length());
            int limit = skipTrailingAsciiWhitespace(str2, pos, input.length());
            int i3 = 65535;
            if (schemeDelimiterOffset(str2, pos, limit) != -1) {
                if (str2.regionMatches(true, pos, "https:", 0, 6)) {
                    this.scheme = "https";
                    pos += "https:".length();
                } else if (!str2.regionMatches(true, pos, "http:", 0, 5)) {
                    return ParseResult.UNSUPPORTED_SCHEME;
                } else {
                    this.scheme = "http";
                    pos += "http:".length();
                }
            } else if (base == null) {
                return ParseResult.MISSING_SCHEME;
            } else {
                this.scheme = base.scheme;
            }
            int slashCount = slashCount(str2, pos, limit);
            int i4 = 35;
            if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
                boolean hasUsername = false;
                boolean hasPassword = false;
                int pos2 = pos + slashCount;
                while (true) {
                    componentDelimiterOffset = HttpUrl.delimiterOffset(str2, pos2, limit, "@/\\?#");
                    if (componentDelimiterOffset != limit) {
                        i = str2.charAt(componentDelimiterOffset);
                    } else {
                        i = i3;
                    }
                    i2 = i;
                    if (!(i2 == i3 || i2 == i4 || i2 == 47 || i2 == 92)) {
                        switch (i2) {
                            case 63:
                                break;
                            case 64:
                                if (!hasPassword) {
                                    int passwordColonOffset = HttpUrl.delimiterOffset(str2, pos2, componentDelimiterOffset, ":");
                                    int passwordColonOffset2 = passwordColonOffset;
                                    char c = i2;
                                    componentDelimiterOffset2 = componentDelimiterOffset;
                                    int i5 = pos2;
                                    String canonicalUsername = HttpUrl.canonicalize(str2, pos2, passwordColonOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                                    if (hasUsername) {
                                        str = this.encodedUsername + "%40" + canonicalUsername;
                                    } else {
                                        str = canonicalUsername;
                                    }
                                    this.encodedUsername = str;
                                    if (passwordColonOffset2 != componentDelimiterOffset2) {
                                        hasPassword = true;
                                        String str3 = canonicalUsername;
                                        this.encodedPassword = HttpUrl.canonicalize(str2, passwordColonOffset2 + 1, componentDelimiterOffset2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                                    }
                                    hasUsername = true;
                                } else {
                                    int c2 = i2;
                                    componentDelimiterOffset2 = componentDelimiterOffset;
                                    this.encodedPassword += "%40" + HttpUrl.canonicalize(str2, pos2, componentDelimiterOffset2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
                                }
                                pos2 = componentDelimiterOffset2 + 1;
                                continue;
                        }
                    }
                    i3 = 65535;
                    i4 = 35;
                }
                int i6 = i2;
                int componentDelimiterOffset3 = componentDelimiterOffset;
                int componentDelimiterOffset4 = pos2;
                int portColonOffset = portColonOffset(str2, componentDelimiterOffset4, componentDelimiterOffset3);
                if (portColonOffset + 1 < componentDelimiterOffset3) {
                    this.host = canonicalizeHost(str2, componentDelimiterOffset4, portColonOffset);
                    this.port = parsePort(str2, portColonOffset + 1, componentDelimiterOffset3);
                    if (this.port == -1) {
                        return ParseResult.INVALID_PORT;
                    }
                } else {
                    this.host = canonicalizeHost(str2, componentDelimiterOffset4, portColonOffset);
                    this.port = HttpUrl.defaultPort(this.scheme);
                }
                if (this.host == null) {
                    return ParseResult.INVALID_HOST;
                }
                pos = componentDelimiterOffset3;
            } else {
                this.encodedUsername = base.encodedUsername();
                this.encodedPassword = base.encodedPassword();
                this.host = base.host;
                this.port = base.port;
                this.encodedPathSegments.clear();
                this.encodedPathSegments.addAll(base.encodedPathSegments());
                if (pos == limit || str2.charAt(pos) == '#') {
                    encodedQuery(base.encodedQuery());
                }
            }
            int pathDelimiterOffset = HttpUrl.delimiterOffset(str2, pos, limit, "?#");
            resolvePath(str2, pos, pathDelimiterOffset);
            int pos3 = pathDelimiterOffset;
            if (pos3 < limit && str2.charAt(pos3) == '?') {
                int queryDelimiterOffset = HttpUrl.delimiterOffset(str2, pos3, limit, "#");
                this.encodedQueryNamesAndValues = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(str2, pos3 + 1, queryDelimiterOffset, HttpUrl.QUERY_ENCODE_SET, true, false, true, true));
                pos3 = queryDelimiterOffset;
            }
            if (pos3 < limit && str2.charAt(pos3) == '#') {
                this.encodedFragment = HttpUrl.canonicalize(str2, pos3 + 1, limit, HttpUrl.FRAGMENT_ENCODE_SET, true, false, false, false);
            }
            return ParseResult.SUCCESS;
        }

        private void resolvePath(String input, int pos, int limit) {
            if (pos != limit) {
                char c = input.charAt(pos);
                if (c == '/' || c == '\\') {
                    this.encodedPathSegments.clear();
                    this.encodedPathSegments.add(HttpUrl.FRAGMENT_ENCODE_SET);
                    pos++;
                } else {
                    this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, HttpUrl.FRAGMENT_ENCODE_SET);
                }
                int i = pos;
                while (i < limit) {
                    int pathSegmentDelimiterOffset = HttpUrl.delimiterOffset(input, i, limit, "/\\");
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
            String segment = HttpUrl.canonicalize(input, pos, limit, HttpUrl.PATH_SEGMENT_ENCODE_SET, alreadyEncoded, false, false, true);
            if (!isDot(segment)) {
                if (isDotDot(segment)) {
                    pop();
                    return;
                }
                if (this.encodedPathSegments.get(this.encodedPathSegments.size() - 1).isEmpty()) {
                    this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, segment);
                } else {
                    this.encodedPathSegments.add(segment);
                }
                if (addTrailingSlash) {
                    this.encodedPathSegments.add(HttpUrl.FRAGMENT_ENCODE_SET);
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
            if (!this.encodedPathSegments.remove(this.encodedPathSegments.size() - 1).isEmpty() || this.encodedPathSegments.isEmpty()) {
                this.encodedPathSegments.add(HttpUrl.FRAGMENT_ENCODE_SET);
            } else {
                this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, HttpUrl.FRAGMENT_ENCODE_SET);
            }
        }

        private int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
            int i = pos;
            while (i < limit) {
                switch (input.charAt(i)) {
                    case 9:
                    case 10:
                    case 12:
                    case 13:
                    case ' ':
                        i++;
                    default:
                        return i;
                }
            }
            return limit;
        }

        private int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
            int i = limit - 1;
            while (i >= pos) {
                switch (input.charAt(i)) {
                    case 9:
                    case 10:
                    case 12:
                    case 13:
                    case ' ':
                        i--;
                    default:
                        return i + 1;
                }
            }
            return pos;
        }

        private static int schemeDelimiterOffset(String input, int pos, int limit) {
            if (limit - pos < 2) {
                return -1;
            }
            char c0 = input.charAt(pos);
            if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z')) {
                return -1;
            }
            int i = pos + 1;
            while (i < limit) {
                char c = input.charAt(i);
                if ((c >= 'a' && c <= 'z') || ((c >= 'A' && c <= 'Z') || ((c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.'))) {
                    i++;
                } else if (c == ':') {
                    return i;
                } else {
                    return -1;
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

        private static String canonicalizeHost(String input, int pos, int limit) {
            InetAddress inetAddress;
            String percentDecoded = HttpUrl.percentDecode(input, pos, limit, false);
            if (!percentDecoded.contains(":")) {
                return domainToAscii(percentDecoded);
            }
            if (!percentDecoded.startsWith("[") || !percentDecoded.endsWith("]")) {
                inetAddress = decodeIpv6(percentDecoded, 0, percentDecoded.length());
            } else {
                inetAddress = decodeIpv6(percentDecoded, 1, percentDecoded.length() - 1);
            }
            if (inetAddress == null) {
                return null;
            }
            byte[] address = inetAddress.getAddress();
            if (address.length == 16) {
                return inet6AddressToAscii(address);
            }
            throw new AssertionError();
        }

        private static InetAddress decodeIpv6(String input, int pos, int limit) {
            byte[] address = new byte[16];
            int groupOffset = -1;
            int compress = -1;
            int b = 0;
            int i = pos;
            while (true) {
                if (i >= limit) {
                    break;
                } else if (b == address.length) {
                    return null;
                } else {
                    if (i + 2 <= limit && input.regionMatches(i, "::", 0, 2)) {
                        if (compress == -1) {
                            i += 2;
                            b += 2;
                            compress = b;
                            if (i == limit) {
                                break;
                            }
                        } else {
                            return null;
                        }
                    } else if (b != 0) {
                        if (input.regionMatches(i, ":", 0, 1)) {
                            i++;
                        } else if (!input.regionMatches(i, ".", 0, 1) || !decodeIpv4Suffix(input, groupOffset, limit, address, b - 2)) {
                            return null;
                        } else {
                            b += 2;
                        }
                    }
                    int value = 0;
                    groupOffset = i;
                    while (i < limit) {
                        int hexDigit = HttpUrl.decodeHexDigit(input.charAt(i));
                        if (hexDigit == -1) {
                            break;
                        }
                        value = (value << 4) + hexDigit;
                        i++;
                    }
                    int groupLength = i - groupOffset;
                    if (groupLength == 0 || groupLength > 4) {
                        return null;
                    }
                    int b2 = b + 1;
                    address[b] = (byte) ((value >>> 8) & 255);
                    b = b2 + 1;
                    address[b2] = (byte) (value & 255);
                }
            }
            if (b != address.length) {
                if (compress == -1) {
                    return null;
                }
                System.arraycopy(address, compress, address, address.length - (b - compress), b - compress);
                Arrays.fill(address, compress, (address.length - b) + compress, (byte) 0);
            }
            try {
                return InetAddress.getByAddress(address);
            } catch (UnknownHostException e) {
                throw new AssertionError();
            }
        }

        private static boolean decodeIpv4Suffix(String input, int pos, int limit, byte[] address, int addressOffset) {
            int b = addressOffset;
            int i = pos;
            while (i < limit) {
                if (b == address.length) {
                    return false;
                }
                if (b != addressOffset) {
                    if (input.charAt(i) != '.') {
                        return false;
                    }
                    i++;
                }
                int value = 0;
                int i2 = i;
                while (i2 < limit) {
                    char c = input.charAt(i2);
                    if (c < '0' || c > '9') {
                        break;
                    } else if (value == 0 && i != i2) {
                        return false;
                    } else {
                        value = ((value * 10) + c) - 48;
                        if (value > 255) {
                            return false;
                        }
                        i2++;
                    }
                }
                if (i2 - i == 0) {
                    return false;
                }
                address[b] = (byte) value;
                b++;
                i = i2;
            }
            if (b != addressOffset + 4) {
                return false;
            }
            return true;
        }

        private static String domainToAscii(String input) {
            try {
                String result = IDN.toASCII(input).toLowerCase(Locale.US);
                if (!result.isEmpty() && !containsInvalidHostnameAsciiCodes(result)) {
                    return result;
                }
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        private static boolean containsInvalidHostnameAsciiCodes(String hostnameAscii) {
            for (int i = 0; i < hostnameAscii.length(); i++) {
                char c = hostnameAscii.charAt(i);
                if (c <= 31 || c >= 127 || " #%/:?@[\\]".indexOf(c) != -1) {
                    return true;
                }
            }
            return false;
        }

        private static String inet6AddressToAscii(byte[] address) {
            int i = 0;
            int longestRunLength = 0;
            int longestRunOffset = -1;
            int longestRunLength2 = 0;
            while (longestRunLength2 < address.length) {
                int i2 = longestRunLength2;
                while (i2 < 16 && address[i2] == 0 && address[i2 + 1] == 0) {
                    i2 += 2;
                }
                int currentRunLength = i2 - longestRunLength2;
                if (currentRunLength > longestRunLength) {
                    longestRunOffset = longestRunLength2;
                    longestRunLength = currentRunLength;
                }
                longestRunLength2 = i2 + 2;
            }
            Buffer result = new Buffer();
            while (i < address.length) {
                if (i == longestRunOffset) {
                    result.writeByte(58);
                    i += longestRunLength;
                    if (i == 16) {
                        result.writeByte(58);
                    }
                } else {
                    if (i > 0) {
                        result.writeByte(58);
                    }
                    result.writeHexadecimalUnsignedLong((long) (((address[i] & 255) << 8) | (address[i + 1] & 255)));
                    i += 2;
                }
            }
            return result.readUtf8();
        }

        private static int parsePort(String input, int pos, int limit) {
            try {
                int i = Integer.parseInt(HttpUrl.canonicalize(input, pos, limit, HttpUrl.FRAGMENT_ENCODE_SET, false, false, false, true));
                if (i <= 0 || i > 65535) {
                    return -1;
                }
                return i;
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }

    private HttpUrl(Builder builder) {
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
                return URI.create(uri.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\p{javaWhitespace}]", FRAGMENT_ENCODE_SET));
            } catch (Exception e2) {
                throw new RuntimeException(e);
            }
        }
    }

    public String scheme() {
        return this.scheme;
    }

    public boolean isHttps() {
        return this.scheme.equals("https");
    }

    public String encodedUsername() {
        if (this.username.isEmpty()) {
            return FRAGMENT_ENCODE_SET;
        }
        int usernameStart = this.scheme.length() + 3;
        return this.url.substring(usernameStart, delimiterOffset(this.url, usernameStart, this.url.length(), ":@"));
    }

    public String username() {
        return this.username;
    }

    public String encodedPassword() {
        if (this.password.isEmpty()) {
            return FRAGMENT_ENCODE_SET;
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
        if (scheme2.equals("http")) {
            return 80;
        }
        if (scheme2.equals("https")) {
            return 443;
        }
        return -1;
    }

    public int pathSize() {
        return this.pathSegments.size();
    }

    public String encodedPath() {
        int pathStart = this.url.indexOf(47, this.scheme.length() + 3);
        return this.url.substring(pathStart, delimiterOffset(this.url, pathStart, this.url.length(), "?#"));
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
        int pathEnd = delimiterOffset(this.url, pathStart, this.url.length(), "?#");
        List<String> result = new ArrayList<>();
        int i = pathStart;
        while (i < pathEnd) {
            int i2 = i + 1;
            int segmentEnd = delimiterOffset(this.url, i2, pathEnd, "/");
            result.add(this.url.substring(i2, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

    public List<String> pathSegments() {
        return this.pathSegments;
    }

    public String encodedQuery() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        int queryStart = this.url.indexOf(63) + 1;
        return this.url.substring(queryStart, delimiterOffset(this.url, queryStart + 1, this.url.length(), "#"));
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

    public String query() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, this.queryNamesAndValues);
        return result.toString();
    }

    public int querySize() {
        if (this.queryNamesAndValues != null) {
            return this.queryNamesAndValues.size() / 2;
        }
        return 0;
    }

    public String queryParameter(String name) {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        int size = this.queryNamesAndValues.size();
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
        return this.queryNamesAndValues.get(index * 2);
    }

    public String queryParameterValue(int index) {
        return this.queryNamesAndValues.get((index * 2) + 1);
    }

    public String encodedFragment() {
        if (this.fragment == null) {
            return null;
        }
        return this.url.substring(this.url.indexOf(35) + 1);
    }

    public String fragment() {
        return this.fragment;
    }

    public HttpUrl resolve(String link) {
        Builder builder = new Builder();
        if (builder.parse(this, link) == Builder.ParseResult.SUCCESS) {
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

    public static HttpUrl parse(String url2) {
        Builder builder = new Builder();
        if (builder.parse(null, url2) == Builder.ParseResult.SUCCESS) {
            return builder.build();
        }
        return null;
    }

    public static HttpUrl get(URL url2) {
        return parse(url2.toString());
    }

    static HttpUrl getChecked(String url2) throws MalformedURLException, UnknownHostException {
        Builder builder = new Builder();
        Builder.ParseResult result = builder.parse(null, url2);
        switch (result) {
            case SUCCESS:
                return builder.build();
            case INVALID_HOST:
                throw new UnknownHostException("Invalid host: " + url2);
            default:
                throw new MalformedURLException("Invalid URL: " + result + " for " + url2);
        }
    }

    public static HttpUrl get(URI uri) {
        return parse(uri.toString());
    }

    public boolean equals(Object o) {
        return (o instanceof HttpUrl) && ((HttpUrl) o).url.equals(this.url);
    }

    public int hashCode() {
        return this.url.hashCode();
    }

    public String toString() {
        return this.url;
    }

    /* access modifiers changed from: private */
    public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1) {
                return i;
            }
        }
        return limit;
    }

    static String percentDecode(String encoded, boolean plusIsSpace) {
        return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
    }

    private List<String> percentDecode(List<String> list, boolean plusIsSpace) {
        List<String> result = new ArrayList<>(list.size());
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
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
                int d1 = decodeHexDigit(encoded.charAt(i + 1));
                int d2 = decodeHexDigit(encoded.charAt(i + 2));
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
        if (pos + 2 >= limit || encoded.charAt(pos) != '%' || decodeHexDigit(encoded.charAt(pos + 1)) == -1 || decodeHexDigit(encoded.charAt(pos + 2)) == -1) {
            return false;
        }
        return true;
    }

    static int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a') + 10;
        }
        if (c < 'A' || c > 'F') {
            return -1;
        }
        return (c - 'A') + 10;
    }

    static String canonicalize(String input, int pos, int limit, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
        int i;
        String str;
        String str2 = input;
        int i2 = limit;
        int i3 = pos;
        while (true) {
            i = i3;
            if (i < i2) {
                int codePoint = str2.codePointAt(i);
                if (codePoint >= 32 && codePoint != 127 && (codePoint < 128 || !asciiOnly)) {
                    str = encodeSet;
                    if (str.indexOf(codePoint) != -1 || ((codePoint == 37 && (!alreadyEncoded || (strict && !percentEncoded(str2, i, i2)))) || (codePoint == 43 && plusIsSpace))) {
                        break;
                    }
                    i3 = Character.charCount(codePoint) + i;
                } else {
                    str = encodeSet;
                }
            } else {
                String str3 = encodeSet;
                return input.substring(pos, limit);
            }
        }
        str = encodeSet;
        Buffer buffer = new Buffer();
        Buffer out = buffer;
        out.writeUtf8(str2, pos, i);
        int i4 = i;
        Buffer out2 = out;
        canonicalize(buffer, str2, i, i2, str, alreadyEncoded, strict, plusIsSpace, asciiOnly);
        return out2.readUtf8();
    }

    static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
        Buffer utf8Buffer = null;
        int i = pos;
        while (i < limit) {
            int codePoint = input.codePointAt(i);
            if (!alreadyEncoded || !(codePoint == 9 || codePoint == 10 || codePoint == 12 || codePoint == 13)) {
                if (codePoint == 43 && plusIsSpace) {
                    out.writeUtf8(alreadyEncoded ? "+" : "%2B");
                } else if (codePoint < 32 || codePoint == 127 || ((codePoint >= 128 && asciiOnly) || encodeSet.indexOf(codePoint) != -1 || (codePoint == 37 && (!alreadyEncoded || (strict && !percentEncoded(input, i, limit)))))) {
                    if (utf8Buffer == null) {
                        utf8Buffer = new Buffer();
                    }
                    utf8Buffer.writeUtf8CodePoint(codePoint);
                    while (!utf8Buffer.exhausted()) {
                        int b = utf8Buffer.readByte() & 255;
                        out.writeByte(37);
                        out.writeByte((int) HEX_DIGITS[(b >> 4) & 15]);
                        out.writeByte((int) HEX_DIGITS[b & 15]);
                    }
                } else {
                    out.writeUtf8CodePoint(codePoint);
                }
            }
            i += Character.charCount(codePoint);
        }
    }

    static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly);
    }
}
