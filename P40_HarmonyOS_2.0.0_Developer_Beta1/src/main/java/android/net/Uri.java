package android.net;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.SettingsStringUtil;
import android.telecom.PhoneAccount;
import android.util.Log;
import com.android.internal.midi.MidiConstants;
import com.android.internal.transition.EpicenterTranslateClipReveal;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;

public abstract class Uri implements Parcelable, Comparable<Uri> {
    public static final Parcelable.Creator<Uri> CREATOR = new Parcelable.Creator<Uri>() {
        /* class android.net.Uri.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Uri createFromParcel(Parcel in) {
            int type = in.readInt();
            if (type == 0) {
                return null;
            }
            if (type == 1) {
                return StringUri.readFrom(in);
            }
            if (type == 2) {
                return OpaqueUri.readFrom(in);
            }
            if (type == 3) {
                return HierarchicalUri.readFrom(in);
            }
            throw new IllegalArgumentException("Unknown URI type: " + type);
        }

        @Override // android.os.Parcelable.Creator
        public Uri[] newArray(int size) {
            return new Uri[size];
        }
    };
    private static final String DEFAULT_ENCODING = "UTF-8";
    public static final Uri EMPTY = new HierarchicalUri(null, Part.NULL, PathPart.EMPTY, Part.NULL, Part.NULL);
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private static final String LOG = Uri.class.getSimpleName();
    private static final String NOT_CACHED = new String("NOT CACHED");
    private static final int NOT_CALCULATED = -2;
    private static final int NOT_FOUND = -1;
    private static final String NOT_HIERARCHICAL = "This isn't a hierarchical URI.";
    private static final int NULL_TYPE_ID = 0;

    public abstract Builder buildUpon();

    public abstract String getAuthority();

    public abstract String getEncodedAuthority();

    public abstract String getEncodedFragment();

    public abstract String getEncodedPath();

    public abstract String getEncodedQuery();

    public abstract String getEncodedSchemeSpecificPart();

    public abstract String getEncodedUserInfo();

    public abstract String getFragment();

    public abstract String getHost();

    public abstract String getLastPathSegment();

    public abstract String getPath();

    public abstract List<String> getPathSegments();

    public abstract int getPort();

    public abstract String getQuery();

    public abstract String getScheme();

    public abstract String getSchemeSpecificPart();

    public abstract String getUserInfo();

    public abstract boolean isHierarchical();

    public abstract boolean isRelative();

    @Override // java.lang.Object
    public abstract String toString();

    @UnsupportedAppUsage
    private Uri() {
    }

    public boolean isOpaque() {
        return !isHierarchical();
    }

    public boolean isAbsolute() {
        return !isRelative();
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (!(o instanceof Uri)) {
            return false;
        }
        return toString().equals(((Uri) o).toString());
    }

    @Override // java.lang.Object
    public int hashCode() {
        return toString().hashCode();
    }

    public int compareTo(Uri other) {
        return toString().compareTo(other.toString());
    }

    @SystemApi
    public String toSafeString() {
        String scheme = getScheme();
        String ssp = getSchemeSpecificPart();
        if (scheme != null) {
            if (scheme.equalsIgnoreCase(PhoneAccount.SCHEME_TEL) || scheme.equalsIgnoreCase("sip") || scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase(PhoneAccount.SCHEME_SMSTO) || scheme.equalsIgnoreCase("mailto") || scheme.equalsIgnoreCase("nfc") || scheme.equalsIgnoreCase("geo")) {
                StringBuilder builder = new StringBuilder(64);
                builder.append(scheme);
                builder.append(':');
                if (ssp != null) {
                    for (int i = 0; i < ssp.length(); i++) {
                        char c = ssp.charAt(i);
                        if (c == '-' || c == '@' || c == '.') {
                            builder.append(c);
                        } else {
                            builder.append(EpicenterTranslateClipReveal.StateProperty.TARGET_X);
                        }
                    }
                }
                return builder.toString();
            } else if (scheme.equalsIgnoreCase(IntentFilter.SCHEME_HTTP) || scheme.equalsIgnoreCase(IntentFilter.SCHEME_HTTPS) || scheme.equalsIgnoreCase("ftp") || scheme.equalsIgnoreCase("rtsp")) {
                StringBuilder sb = new StringBuilder();
                sb.append("//");
                String str = "";
                sb.append(getHost() != null ? getHost() : str);
                if (getPort() != -1) {
                    str = SettingsStringUtil.DELIMITER + getPort();
                }
                sb.append(str);
                sb.append("/...");
                ssp = sb.toString();
            } else if (scheme.toLowerCase().contains("map")) {
                ssp = "****";
            }
        }
        StringBuilder builder2 = new StringBuilder(64);
        if (scheme != null) {
            builder2.append(scheme);
            builder2.append(':');
        }
        if (ssp != null) {
            builder2.append(ssp);
        }
        return builder2.toString();
    }

    public static Uri parse(String uriString) {
        return new StringUri(uriString);
    }

    public static Uri fromFile(File file) {
        if (file != null) {
            return new HierarchicalUri(ContentResolver.SCHEME_FILE, Part.EMPTY, PathPart.fromDecoded(file.getAbsolutePath()), Part.NULL, Part.NULL);
        }
        throw new NullPointerException(ContentResolver.SCHEME_FILE);
    }

    /* access modifiers changed from: private */
    public static class StringUri extends AbstractHierarchicalUri {
        static final int TYPE_ID = 1;
        private Part authority;
        private volatile int cachedFsi;
        private volatile int cachedSsi;
        private Part fragment;
        private PathPart path;
        private Part query;
        private volatile String scheme;
        private Part ssp;
        private final String uriString;

        private StringUri(String uriString2) {
            super();
            this.cachedSsi = -2;
            this.cachedFsi = -2;
            this.scheme = Uri.NOT_CACHED;
            if (uriString2 != null) {
                this.uriString = uriString2;
                return;
            }
            throw new NullPointerException("uriString");
        }

        static Uri readFrom(Parcel parcel) {
            return new StringUri(parcel.readString());
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(1);
            parcel.writeString(this.uriString);
        }

        private int findSchemeSeparator() {
            if (this.cachedSsi != -2) {
                return this.cachedSsi;
            }
            int indexOf = this.uriString.indexOf(58);
            this.cachedSsi = indexOf;
            return indexOf;
        }

        private int findFragmentSeparator() {
            if (this.cachedFsi != -2) {
                return this.cachedFsi;
            }
            int indexOf = this.uriString.indexOf(35, findSchemeSeparator());
            this.cachedFsi = indexOf;
            return indexOf;
        }

        @Override // android.net.Uri
        public boolean isHierarchical() {
            int ssi = findSchemeSeparator();
            if (ssi == -1) {
                return true;
            }
            if (this.uriString.length() == ssi + 1) {
                return false;
            }
            if (this.uriString.charAt(ssi + 1) == '/') {
                return true;
            }
            return false;
        }

        @Override // android.net.Uri
        public boolean isRelative() {
            return findSchemeSeparator() == -1;
        }

        @Override // android.net.Uri
        public String getScheme() {
            if (this.scheme != Uri.NOT_CACHED) {
                return this.scheme;
            }
            String parseScheme = parseScheme();
            this.scheme = parseScheme;
            return parseScheme;
        }

        private String parseScheme() {
            int ssi = findSchemeSeparator();
            if (ssi == -1) {
                return null;
            }
            return this.uriString.substring(0, ssi);
        }

        private Part getSsp() {
            Part part = this.ssp;
            if (part != null) {
                return part;
            }
            Part fromEncoded = Part.fromEncoded(parseSsp());
            this.ssp = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public String getEncodedSchemeSpecificPart() {
            return getSsp().getEncoded();
        }

        @Override // android.net.Uri
        public String getSchemeSpecificPart() {
            return getSsp().getDecoded();
        }

        private String parseSsp() {
            int ssi = findSchemeSeparator();
            int fsi = findFragmentSeparator();
            if (fsi == -1) {
                return this.uriString.substring(ssi + 1);
            }
            return this.uriString.substring(ssi + 1, fsi);
        }

        private Part getAuthorityPart() {
            Part part = this.authority;
            if (part != null) {
                return part;
            }
            Part fromEncoded = Part.fromEncoded(parseAuthority(this.uriString, findSchemeSeparator()));
            this.authority = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public String getEncodedAuthority() {
            return getAuthorityPart().getEncoded();
        }

        @Override // android.net.Uri
        public String getAuthority() {
            return getAuthorityPart().getDecoded();
        }

        private PathPart getPathPart() {
            PathPart pathPart = this.path;
            if (pathPart != null) {
                return pathPart;
            }
            PathPart fromEncoded = PathPart.fromEncoded(parsePath());
            this.path = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public String getPath() {
            return getPathPart().getDecoded();
        }

        @Override // android.net.Uri
        public String getEncodedPath() {
            return getPathPart().getEncoded();
        }

        @Override // android.net.Uri
        public List<String> getPathSegments() {
            return getPathPart().getPathSegments();
        }

        private String parsePath() {
            String uriString2 = this.uriString;
            int ssi = findSchemeSeparator();
            if (ssi > -1) {
                if ((ssi + 1 == uriString2.length()) || uriString2.charAt(ssi + 1) != '/') {
                    return null;
                }
            }
            return parsePath(uriString2, ssi);
        }

        private Part getQueryPart() {
            Part part = this.query;
            if (part != null) {
                return part;
            }
            Part fromEncoded = Part.fromEncoded(parseQuery());
            this.query = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public String getEncodedQuery() {
            return getQueryPart().getEncoded();
        }

        private String parseQuery() {
            int qsi = this.uriString.indexOf(63, findSchemeSeparator());
            if (qsi == -1) {
                return null;
            }
            int fsi = findFragmentSeparator();
            if (fsi == -1) {
                return this.uriString.substring(qsi + 1);
            }
            if (fsi < qsi) {
                return null;
            }
            return this.uriString.substring(qsi + 1, fsi);
        }

        @Override // android.net.Uri
        public String getQuery() {
            return getQueryPart().getDecoded();
        }

        private Part getFragmentPart() {
            Part part = this.fragment;
            if (part != null) {
                return part;
            }
            Part fromEncoded = Part.fromEncoded(parseFragment());
            this.fragment = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public String getEncodedFragment() {
            return getFragmentPart().getEncoded();
        }

        private String parseFragment() {
            int fsi = findFragmentSeparator();
            if (fsi == -1) {
                return null;
            }
            return this.uriString.substring(fsi + 1);
        }

        @Override // android.net.Uri
        public String getFragment() {
            return getFragmentPart().getDecoded();
        }

        @Override // android.net.Uri, java.lang.Object
        public String toString() {
            return this.uriString;
        }

        static String parseAuthority(String uriString2, int ssi) {
            int length = uriString2.length();
            if (length <= ssi + 2 || uriString2.charAt(ssi + 1) != '/' || uriString2.charAt(ssi + 2) != '/') {
                return null;
            }
            int end = ssi + 3;
            while (end < length) {
                char charAt = uriString2.charAt(end);
                if (charAt == '#' || charAt == '/' || charAt == '?' || charAt == '\\') {
                    break;
                }
                end++;
            }
            return uriString2.substring(ssi + 3, end);
        }

        static String parsePath(String uriString2, int ssi) {
            int pathStart;
            int length = uriString2.length();
            if (length > ssi + 2 && uriString2.charAt(ssi + 1) == '/' && uriString2.charAt(ssi + 2) == '/') {
                pathStart = ssi + 3;
                while (pathStart < length) {
                    char charAt = uriString2.charAt(pathStart);
                    if (charAt == '#') {
                        return "";
                    }
                    if (charAt == '/') {
                        break;
                    } else if (charAt != '?') {
                        if (charAt == '\\') {
                            break;
                        }
                        pathStart++;
                    } else {
                        return "";
                    }
                }
            } else {
                pathStart = ssi + 1;
            }
            int pathEnd = pathStart;
            while (pathEnd < length) {
                char charAt2 = uriString2.charAt(pathEnd);
                if (charAt2 == '#' || charAt2 == '?') {
                    break;
                }
                pathEnd++;
            }
            return uriString2.substring(pathStart, pathEnd);
        }

        @Override // android.net.Uri
        public Builder buildUpon() {
            if (isHierarchical()) {
                return new Builder().scheme(getScheme()).authority(getAuthorityPart()).path(getPathPart()).query(getQueryPart()).fragment(getFragmentPart());
            }
            return new Builder().scheme(getScheme()).opaquePart(getSsp()).fragment(getFragmentPart());
        }
    }

    public static Uri fromParts(String scheme, String ssp, String fragment) {
        if (scheme == null) {
            throw new NullPointerException("scheme");
        } else if (ssp != null) {
            return new OpaqueUri(scheme, Part.fromDecoded(ssp), Part.fromDecoded(fragment));
        } else {
            throw new NullPointerException("ssp");
        }
    }

    /* access modifiers changed from: private */
    public static class OpaqueUri extends Uri {
        static final int TYPE_ID = 2;
        private volatile String cachedString;
        private final Part fragment;
        private final String scheme;
        private final Part ssp;

        private OpaqueUri(String scheme2, Part ssp2, Part fragment2) {
            super();
            this.cachedString = Uri.NOT_CACHED;
            this.scheme = scheme2;
            this.ssp = ssp2;
            this.fragment = fragment2 == null ? Part.NULL : fragment2;
        }

        static Uri readFrom(Parcel parcel) {
            return new OpaqueUri(parcel.readString(), Part.readFrom(parcel), Part.readFrom(parcel));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(2);
            parcel.writeString(this.scheme);
            this.ssp.writeTo(parcel);
            this.fragment.writeTo(parcel);
        }

        @Override // android.net.Uri
        public boolean isHierarchical() {
            return false;
        }

        @Override // android.net.Uri
        public boolean isRelative() {
            return this.scheme == null;
        }

        @Override // android.net.Uri
        public String getScheme() {
            return this.scheme;
        }

        @Override // android.net.Uri
        public String getEncodedSchemeSpecificPart() {
            return this.ssp.getEncoded();
        }

        @Override // android.net.Uri
        public String getSchemeSpecificPart() {
            return this.ssp.getDecoded();
        }

        @Override // android.net.Uri
        public String getAuthority() {
            return null;
        }

        @Override // android.net.Uri
        public String getEncodedAuthority() {
            return null;
        }

        @Override // android.net.Uri
        public String getPath() {
            return null;
        }

        @Override // android.net.Uri
        public String getEncodedPath() {
            return null;
        }

        @Override // android.net.Uri
        public String getQuery() {
            return null;
        }

        @Override // android.net.Uri
        public String getEncodedQuery() {
            return null;
        }

        @Override // android.net.Uri
        public String getFragment() {
            return this.fragment.getDecoded();
        }

        @Override // android.net.Uri
        public String getEncodedFragment() {
            return this.fragment.getEncoded();
        }

        @Override // android.net.Uri
        public List<String> getPathSegments() {
            return Collections.emptyList();
        }

        @Override // android.net.Uri
        public String getLastPathSegment() {
            return null;
        }

        @Override // android.net.Uri
        public String getUserInfo() {
            return null;
        }

        @Override // android.net.Uri
        public String getEncodedUserInfo() {
            return null;
        }

        @Override // android.net.Uri
        public String getHost() {
            return null;
        }

        @Override // android.net.Uri
        public int getPort() {
            return -1;
        }

        @Override // android.net.Uri, java.lang.Object
        public String toString() {
            if (this.cachedString != Uri.NOT_CACHED) {
                return this.cachedString;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(this.scheme);
            sb.append(':');
            sb.append(getEncodedSchemeSpecificPart());
            if (!this.fragment.isEmpty()) {
                sb.append('#');
                sb.append(this.fragment.getEncoded());
            }
            String sb2 = sb.toString();
            this.cachedString = sb2;
            return sb2;
        }

        @Override // android.net.Uri
        public Builder buildUpon() {
            return new Builder().scheme(this.scheme).opaquePart(this.ssp).fragment(this.fragment);
        }
    }

    /* access modifiers changed from: package-private */
    public static class PathSegments extends AbstractList<String> implements RandomAccess {
        static final PathSegments EMPTY = new PathSegments(null, 0);
        final String[] segments;
        final int size;

        PathSegments(String[] segments2, int size2) {
            this.segments = segments2;
            this.size = size2;
        }

        @Override // java.util.AbstractList, java.util.List
        public String get(int index) {
            if (index < this.size) {
                return this.segments[index];
            }
            throw new IndexOutOfBoundsException();
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
        public int size() {
            return this.size;
        }
    }

    /* access modifiers changed from: package-private */
    public static class PathSegmentsBuilder {
        String[] segments;
        int size = 0;

        PathSegmentsBuilder() {
        }

        /* access modifiers changed from: package-private */
        public void add(String segment) {
            String[] strArr = this.segments;
            if (strArr == null) {
                this.segments = new String[4];
            } else if (this.size + 1 == strArr.length) {
                String[] expanded = new String[(strArr.length * 2)];
                System.arraycopy(strArr, 0, expanded, 0, strArr.length);
                this.segments = expanded;
            }
            String[] strArr2 = this.segments;
            int i = this.size;
            this.size = i + 1;
            strArr2[i] = segment;
        }

        /* access modifiers changed from: package-private */
        public PathSegments build() {
            String[] strArr = this.segments;
            if (strArr == null) {
                return PathSegments.EMPTY;
            }
            try {
                return new PathSegments(strArr, this.size);
            } finally {
                this.segments = null;
            }
        }
    }

    private static abstract class AbstractHierarchicalUri extends Uri {
        private volatile String host;
        private volatile int port;
        private Part userInfo;

        private AbstractHierarchicalUri() {
            super();
            this.host = Uri.NOT_CACHED;
            this.port = -2;
        }

        @Override // android.net.Uri
        public String getLastPathSegment() {
            List<String> segments = getPathSegments();
            int size = segments.size();
            if (size == 0) {
                return null;
            }
            return segments.get(size - 1);
        }

        private Part getUserInfoPart() {
            Part part = this.userInfo;
            if (part != null) {
                return part;
            }
            Part fromEncoded = Part.fromEncoded(parseUserInfo());
            this.userInfo = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public final String getEncodedUserInfo() {
            return getUserInfoPart().getEncoded();
        }

        private String parseUserInfo() {
            int end;
            String authority = getEncodedAuthority();
            if (authority == null || (end = authority.lastIndexOf(64)) == -1) {
                return null;
            }
            return authority.substring(0, end);
        }

        @Override // android.net.Uri
        public String getUserInfo() {
            return getUserInfoPart().getDecoded();
        }

        @Override // android.net.Uri
        public String getHost() {
            if (this.host != Uri.NOT_CACHED) {
                return this.host;
            }
            String parseHost = parseHost();
            this.host = parseHost;
            return parseHost;
        }

        private String parseHost() {
            String encodedHost;
            String authority = getEncodedAuthority();
            if (authority == null) {
                return null;
            }
            int userInfoSeparator = authority.lastIndexOf(64);
            int portSeparator = findPortSeparator(authority);
            if (portSeparator == -1) {
                encodedHost = authority.substring(userInfoSeparator + 1);
            } else {
                encodedHost = authority.substring(userInfoSeparator + 1, portSeparator);
            }
            return decode(encodedHost);
        }

        @Override // android.net.Uri
        public int getPort() {
            if (this.port != -2) {
                return this.port;
            }
            int parsePort = parsePort();
            this.port = parsePort;
            return parsePort;
        }

        private int parsePort() {
            String authority = getEncodedAuthority();
            int portSeparator = findPortSeparator(authority);
            if (portSeparator == -1) {
                return -1;
            }
            try {
                return Integer.parseInt(decode(authority.substring(portSeparator + 1)));
            } catch (NumberFormatException e) {
                Log.w(Uri.LOG, "Error parsing port string.", e);
                return -1;
            }
        }

        private int findPortSeparator(String authority) {
            if (authority == null) {
                return -1;
            }
            for (int i = authority.length() - 1; i >= 0; i--) {
                int character = authority.charAt(i);
                if (58 == character) {
                    return i;
                }
                if (character < 48 || character > 57) {
                    return -1;
                }
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public static class HierarchicalUri extends AbstractHierarchicalUri {
        static final int TYPE_ID = 3;
        private final Part authority;
        private final Part fragment;
        private final PathPart path;
        private final Part query;
        private final String scheme;
        private Part ssp;
        private volatile String uriString;

        private HierarchicalUri(String scheme2, Part authority2, PathPart path2, Part query2, Part fragment2) {
            super();
            this.uriString = Uri.NOT_CACHED;
            this.scheme = scheme2;
            this.authority = Part.nonNull(authority2);
            this.path = path2 == null ? PathPart.NULL : path2;
            this.query = Part.nonNull(query2);
            this.fragment = Part.nonNull(fragment2);
        }

        static Uri readFrom(Parcel parcel) {
            return new HierarchicalUri(parcel.readString(), Part.readFrom(parcel), PathPart.readFrom(parcel), Part.readFrom(parcel), Part.readFrom(parcel));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(3);
            parcel.writeString(this.scheme);
            this.authority.writeTo(parcel);
            this.path.writeTo(parcel);
            this.query.writeTo(parcel);
            this.fragment.writeTo(parcel);
        }

        @Override // android.net.Uri
        public boolean isHierarchical() {
            return true;
        }

        @Override // android.net.Uri
        public boolean isRelative() {
            return this.scheme == null;
        }

        @Override // android.net.Uri
        public String getScheme() {
            return this.scheme;
        }

        private Part getSsp() {
            Part part = this.ssp;
            if (part != null) {
                return part;
            }
            Part fromEncoded = Part.fromEncoded(makeSchemeSpecificPart());
            this.ssp = fromEncoded;
            return fromEncoded;
        }

        @Override // android.net.Uri
        public String getEncodedSchemeSpecificPart() {
            return getSsp().getEncoded();
        }

        @Override // android.net.Uri
        public String getSchemeSpecificPart() {
            return getSsp().getDecoded();
        }

        private String makeSchemeSpecificPart() {
            StringBuilder builder = new StringBuilder();
            appendSspTo(builder);
            return builder.toString();
        }

        private void appendSspTo(StringBuilder builder) {
            String encodedAuthority = this.authority.getEncoded();
            if (encodedAuthority != null) {
                builder.append("//");
                builder.append(encodedAuthority);
            }
            String encodedPath = this.path.getEncoded();
            if (encodedPath != null) {
                builder.append(encodedPath);
            }
            if (!this.query.isEmpty()) {
                builder.append('?');
                builder.append(this.query.getEncoded());
            }
        }

        @Override // android.net.Uri
        public String getAuthority() {
            return this.authority.getDecoded();
        }

        @Override // android.net.Uri
        public String getEncodedAuthority() {
            return this.authority.getEncoded();
        }

        @Override // android.net.Uri
        public String getEncodedPath() {
            return this.path.getEncoded();
        }

        @Override // android.net.Uri
        public String getPath() {
            return this.path.getDecoded();
        }

        @Override // android.net.Uri
        public String getQuery() {
            return this.query.getDecoded();
        }

        @Override // android.net.Uri
        public String getEncodedQuery() {
            return this.query.getEncoded();
        }

        @Override // android.net.Uri
        public String getFragment() {
            return this.fragment.getDecoded();
        }

        @Override // android.net.Uri
        public String getEncodedFragment() {
            return this.fragment.getEncoded();
        }

        @Override // android.net.Uri
        public List<String> getPathSegments() {
            return this.path.getPathSegments();
        }

        @Override // android.net.Uri, java.lang.Object
        public String toString() {
            if (this.uriString != Uri.NOT_CACHED) {
                return this.uriString;
            }
            String makeUriString = makeUriString();
            this.uriString = makeUriString;
            return makeUriString;
        }

        private String makeUriString() {
            StringBuilder builder = new StringBuilder();
            String str = this.scheme;
            if (str != null) {
                builder.append(str);
                builder.append(':');
            }
            appendSspTo(builder);
            if (!this.fragment.isEmpty()) {
                builder.append('#');
                builder.append(this.fragment.getEncoded());
            }
            return builder.toString();
        }

        @Override // android.net.Uri
        public Builder buildUpon() {
            return new Builder().scheme(this.scheme).authority(this.authority).path(this.path).query(this.query).fragment(this.fragment);
        }
    }

    public static final class Builder {
        private Part authority;
        private Part fragment;
        private Part opaquePart;
        private PathPart path;
        private Part query;
        private String scheme;

        public Builder scheme(String scheme2) {
            this.scheme = scheme2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder opaquePart(Part opaquePart2) {
            this.opaquePart = opaquePart2;
            return this;
        }

        public Builder opaquePart(String opaquePart2) {
            return opaquePart(Part.fromDecoded(opaquePart2));
        }

        public Builder encodedOpaquePart(String opaquePart2) {
            return opaquePart(Part.fromEncoded(opaquePart2));
        }

        /* access modifiers changed from: package-private */
        public Builder authority(Part authority2) {
            this.opaquePart = null;
            this.authority = authority2;
            return this;
        }

        public Builder authority(String authority2) {
            return authority(Part.fromDecoded(authority2));
        }

        public Builder encodedAuthority(String authority2) {
            return authority(Part.fromEncoded(authority2));
        }

        /* access modifiers changed from: package-private */
        public Builder path(PathPart path2) {
            this.opaquePart = null;
            this.path = path2;
            return this;
        }

        public Builder path(String path2) {
            return path(PathPart.fromDecoded(path2));
        }

        public Builder encodedPath(String path2) {
            return path(PathPart.fromEncoded(path2));
        }

        public Builder appendPath(String newSegment) {
            return path(PathPart.appendDecodedSegment(this.path, newSegment));
        }

        public Builder appendEncodedPath(String newSegment) {
            return path(PathPart.appendEncodedSegment(this.path, newSegment));
        }

        /* access modifiers changed from: package-private */
        public Builder query(Part query2) {
            this.opaquePart = null;
            this.query = query2;
            return this;
        }

        public Builder query(String query2) {
            return query(Part.fromDecoded(query2));
        }

        public Builder encodedQuery(String query2) {
            return query(Part.fromEncoded(query2));
        }

        /* access modifiers changed from: package-private */
        public Builder fragment(Part fragment2) {
            this.fragment = fragment2;
            return this;
        }

        public Builder fragment(String fragment2) {
            return fragment(Part.fromDecoded(fragment2));
        }

        public Builder encodedFragment(String fragment2) {
            return fragment(Part.fromEncoded(fragment2));
        }

        public Builder appendQueryParameter(String key, String value) {
            this.opaquePart = null;
            String encodedParameter = Uri.encode(key, null) + "=" + Uri.encode(value, null);
            Part part = this.query;
            if (part == null) {
                this.query = Part.fromEncoded(encodedParameter);
                return this;
            }
            String oldQuery = part.getEncoded();
            if (oldQuery == null || oldQuery.length() == 0) {
                this.query = Part.fromEncoded(encodedParameter);
            } else {
                this.query = Part.fromEncoded(oldQuery + "&" + encodedParameter);
            }
            return this;
        }

        public Builder clearQuery() {
            return query((Part) null);
        }

        public Uri build() {
            Part part = this.opaquePart;
            if (part != null) {
                String str = this.scheme;
                if (str != null) {
                    return new OpaqueUri(str, part, this.fragment);
                }
                throw new UnsupportedOperationException("An opaque URI must have a scheme.");
            }
            PathPart path2 = this.path;
            if (path2 == null || path2 == PathPart.NULL) {
                path2 = PathPart.EMPTY;
            } else if (hasSchemeOrAuthority()) {
                path2 = PathPart.makeAbsolute(path2);
            }
            return new HierarchicalUri(this.scheme, this.authority, path2, this.query, this.fragment);
        }

        private boolean hasSchemeOrAuthority() {
            Part part;
            return (this.scheme == null && ((part = this.authority) == null || part == Part.NULL)) ? false : true;
        }

        public String toString() {
            return build().toString();
        }
    }

    public Set<String> getQueryParameterNames() {
        if (!isOpaque()) {
            String query = getEncodedQuery();
            if (query == null) {
                return Collections.emptySet();
            }
            Set<String> names = new LinkedHashSet<>();
            int start = 0;
            do {
                int next = query.indexOf(38, start);
                int end = next == -1 ? query.length() : next;
                int separator = query.indexOf(61, start);
                if (separator > end || separator == -1) {
                    separator = end;
                }
                names.add(decode(query.substring(start, separator)));
                start = end + 1;
            } while (start < query.length());
            return Collections.unmodifiableSet(names);
        }
        throw new UnsupportedOperationException(NOT_HIERARCHICAL);
    }

    public List<String> getQueryParameters(String key) {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        } else if (key != null) {
            String query = getEncodedQuery();
            if (query == null) {
                return Collections.emptyList();
            }
            try {
                String encodedKey = URLEncoder.encode(key, DEFAULT_ENCODING);
                ArrayList<String> values = new ArrayList<>();
                int start = 0;
                while (true) {
                    int nextAmpersand = query.indexOf(38, start);
                    int end = nextAmpersand != -1 ? nextAmpersand : query.length();
                    int separator = query.indexOf(61, start);
                    if (separator > end || separator == -1) {
                        separator = end;
                    }
                    if (separator - start == encodedKey.length() && query.regionMatches(start, encodedKey, 0, encodedKey.length())) {
                        if (separator == end) {
                            values.add("");
                        } else {
                            values.add(decode(query.substring(separator + 1, end)));
                        }
                    }
                    if (nextAmpersand == -1) {
                        return Collections.unmodifiableList(values);
                    }
                    start = nextAmpersand + 1;
                }
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        } else {
            throw new NullPointerException("key");
        }
    }

    public String getQueryParameter(String key) {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        } else if (key != null) {
            String query = getEncodedQuery();
            if (query == null) {
                return null;
            }
            String encodedKey = encode(key, null);
            int length = query.length();
            int start = 0;
            while (true) {
                int nextAmpersand = query.indexOf(38, start);
                int end = nextAmpersand != -1 ? nextAmpersand : length;
                int separator = query.indexOf(61, start);
                if (separator > end || separator == -1) {
                    separator = end;
                }
                if (separator - start != encodedKey.length() || !query.regionMatches(start, encodedKey, 0, encodedKey.length())) {
                    if (nextAmpersand == -1) {
                        return null;
                    }
                    start = nextAmpersand + 1;
                } else if (separator == end) {
                    return "";
                } else {
                    return UriCodec.decode(query.substring(separator + 1, end), true, StandardCharsets.UTF_8, false);
                }
            }
        } else {
            throw new NullPointerException("key");
        }
    }

    public boolean getBooleanQueryParameter(String key, boolean defaultValue) {
        String flag = getQueryParameter(key);
        if (flag == null) {
            return defaultValue;
        }
        String flag2 = flag.toLowerCase(Locale.ROOT);
        return !"false".equals(flag2) && !WifiEnterpriseConfig.ENGINE_DISABLE.equals(flag2);
    }

    public Uri normalizeScheme() {
        String scheme = getScheme();
        if (scheme == null) {
            return this;
        }
        String lowerScheme = scheme.toLowerCase(Locale.ROOT);
        if (scheme.equals(lowerScheme)) {
            return this;
        }
        return buildUpon().scheme(lowerScheme).build();
    }

    public static void writeToParcel(Parcel out, Uri uri) {
        if (uri == null) {
            out.writeInt(0);
        } else {
            uri.writeToParcel(out, 0);
        }
    }

    public static String encode(String s) {
        return encode(s, null);
    }

    public static String encode(String s, String allow) {
        if (s == null) {
            return null;
        }
        StringBuilder encoded = null;
        int oldLength = s.length();
        int current = 0;
        while (current < oldLength) {
            int nextToEncode = current;
            while (nextToEncode < oldLength && isAllowed(s.charAt(nextToEncode), allow)) {
                nextToEncode++;
            }
            if (nextToEncode != oldLength) {
                if (encoded == null) {
                    encoded = new StringBuilder();
                }
                if (nextToEncode > current) {
                    encoded.append((CharSequence) s, current, nextToEncode);
                }
                int nextAllowed = nextToEncode + 1;
                while (nextAllowed < oldLength && !isAllowed(s.charAt(nextAllowed), allow)) {
                    nextAllowed++;
                }
                try {
                    byte[] bytes = s.substring(nextToEncode, nextAllowed).getBytes(DEFAULT_ENCODING);
                    int bytesLength = bytes.length;
                    for (int i = 0; i < bytesLength; i++) {
                        encoded.append('%');
                        encoded.append(HEX_DIGITS[(bytes[i] & 240) >> 4]);
                        encoded.append(HEX_DIGITS[bytes[i] & MidiConstants.STATUS_CHANNEL_MASK]);
                    }
                    current = nextAllowed;
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            } else if (current == 0) {
                return s;
            } else {
                encoded.append((CharSequence) s, current, oldLength);
                return encoded.toString();
            }
        }
        return encoded == null ? s : encoded.toString();
    }

    private static boolean isAllowed(char c, String allow) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || !((c < '0' || c > '9') && "_-!.~'()*".indexOf(c) == -1 && (allow == null || allow.indexOf(c) == -1));
    }

    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        return UriCodec.decode(s, false, StandardCharsets.UTF_8, false);
    }

    static abstract class AbstractPart {
        volatile String decoded;
        volatile String encoded;

        /* access modifiers changed from: package-private */
        public abstract String getEncoded();

        static class Representation {
            static final int BOTH = 0;
            static final int DECODED = 2;
            static final int ENCODED = 1;

            Representation() {
            }
        }

        AbstractPart(String encoded2, String decoded2) {
            this.encoded = encoded2;
            this.decoded = decoded2;
        }

        /* access modifiers changed from: package-private */
        public final String getDecoded() {
            if (this.decoded != Uri.NOT_CACHED) {
                return this.decoded;
            }
            String decode = Uri.decode(this.encoded);
            this.decoded = decode;
            return decode;
        }

        /* access modifiers changed from: package-private */
        public final void writeTo(Parcel parcel) {
            boolean hasEncoded = this.encoded != Uri.NOT_CACHED;
            boolean hasDecoded = this.decoded != Uri.NOT_CACHED;
            if (hasEncoded && hasDecoded) {
                parcel.writeInt(0);
                parcel.writeString(this.encoded);
                parcel.writeString(this.decoded);
            } else if (hasEncoded) {
                parcel.writeInt(1);
                parcel.writeString(this.encoded);
            } else if (hasDecoded) {
                parcel.writeInt(2);
                parcel.writeString(this.decoded);
            } else {
                throw new IllegalArgumentException("Neither encoded nor decoded");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class Part extends AbstractPart {
        static final Part EMPTY = new EmptyPart("");
        static final Part NULL = new EmptyPart(null);

        private Part(String encoded, String decoded) {
            super(encoded, decoded);
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return false;
        }

        /* access modifiers changed from: package-private */
        @Override // android.net.Uri.AbstractPart
        public String getEncoded() {
            if (this.encoded != Uri.NOT_CACHED) {
                return this.encoded;
            }
            String encode = Uri.encode(this.decoded);
            this.encoded = encode;
            return encode;
        }

        static Part readFrom(Parcel parcel) {
            int representation = parcel.readInt();
            if (representation == 0) {
                return from(parcel.readString(), parcel.readString());
            }
            if (representation == 1) {
                return fromEncoded(parcel.readString());
            }
            if (representation == 2) {
                return fromDecoded(parcel.readString());
            }
            throw new IllegalArgumentException("Unknown representation: " + representation);
        }

        static Part nonNull(Part part) {
            return part == null ? NULL : part;
        }

        static Part fromEncoded(String encoded) {
            return from(encoded, Uri.NOT_CACHED);
        }

        static Part fromDecoded(String decoded) {
            return from(Uri.NOT_CACHED, decoded);
        }

        static Part from(String encoded, String decoded) {
            if (encoded == null) {
                return NULL;
            }
            if (encoded.length() == 0) {
                return EMPTY;
            }
            if (decoded == null) {
                return NULL;
            }
            if (decoded.length() == 0) {
                return EMPTY;
            }
            return new Part(encoded, decoded);
        }

        private static class EmptyPart extends Part {
            public EmptyPart(String value) {
                super(value, value);
            }

            /* access modifiers changed from: package-private */
            @Override // android.net.Uri.Part
            public boolean isEmpty() {
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class PathPart extends AbstractPart {
        static final PathPart EMPTY = new PathPart("", "");
        static final PathPart NULL = new PathPart(null, null);
        private PathSegments pathSegments;

        private PathPart(String encoded, String decoded) {
            super(encoded, decoded);
        }

        /* access modifiers changed from: package-private */
        @Override // android.net.Uri.AbstractPart
        public String getEncoded() {
            if (this.encoded != Uri.NOT_CACHED) {
                return this.encoded;
            }
            String encode = Uri.encode(this.decoded, "/");
            this.encoded = encode;
            return encode;
        }

        /* access modifiers changed from: package-private */
        public PathSegments getPathSegments() {
            PathSegments pathSegments2 = this.pathSegments;
            if (pathSegments2 != null) {
                return pathSegments2;
            }
            String path = getEncoded();
            if (path == null) {
                PathSegments pathSegments3 = PathSegments.EMPTY;
                this.pathSegments = pathSegments3;
                return pathSegments3;
            }
            PathSegmentsBuilder segmentBuilder = new PathSegmentsBuilder();
            int previous = 0;
            while (true) {
                int current = path.indexOf(47, previous);
                if (current <= -1) {
                    break;
                }
                if (previous < current) {
                    segmentBuilder.add(Uri.decode(path.substring(previous, current)));
                }
                previous = current + 1;
            }
            if (previous < path.length()) {
                segmentBuilder.add(Uri.decode(path.substring(previous)));
            }
            PathSegments build = segmentBuilder.build();
            this.pathSegments = build;
            return build;
        }

        static PathPart appendEncodedSegment(PathPart oldPart, String newSegment) {
            String newPath;
            if (oldPart == null) {
                return fromEncoded("/" + newSegment);
            }
            String oldPath = oldPart.getEncoded();
            if (oldPath == null) {
                oldPath = "";
            }
            int oldPathLength = oldPath.length();
            if (oldPathLength == 0) {
                newPath = "/" + newSegment;
            } else if (oldPath.charAt(oldPathLength - 1) == '/') {
                newPath = oldPath + newSegment;
            } else {
                newPath = oldPath + "/" + newSegment;
            }
            return fromEncoded(newPath);
        }

        static PathPart appendDecodedSegment(PathPart oldPart, String decoded) {
            return appendEncodedSegment(oldPart, Uri.encode(decoded));
        }

        static PathPart readFrom(Parcel parcel) {
            int representation = parcel.readInt();
            if (representation == 0) {
                return from(parcel.readString(), parcel.readString());
            }
            if (representation == 1) {
                return fromEncoded(parcel.readString());
            }
            if (representation == 2) {
                return fromDecoded(parcel.readString());
            }
            throw new IllegalArgumentException("Bad representation: " + representation);
        }

        static PathPart fromEncoded(String encoded) {
            return from(encoded, Uri.NOT_CACHED);
        }

        static PathPart fromDecoded(String decoded) {
            return from(Uri.NOT_CACHED, decoded);
        }

        static PathPart from(String encoded, String decoded) {
            if (encoded == null) {
                return NULL;
            }
            if (encoded.length() == 0) {
                return EMPTY;
            }
            return new PathPart(encoded, decoded);
        }

        static PathPart makeAbsolute(PathPart oldPart) {
            String newEncoded;
            String newDecoded;
            boolean decodedCached = true;
            boolean encodedCached = oldPart.encoded != Uri.NOT_CACHED;
            String oldPath = encodedCached ? oldPart.encoded : oldPart.decoded;
            if (oldPath == null || oldPath.length() == 0 || oldPath.startsWith("/")) {
                return oldPart;
            }
            if (encodedCached) {
                newEncoded = "/" + oldPart.encoded;
            } else {
                newEncoded = Uri.NOT_CACHED;
            }
            if (oldPart.decoded == Uri.NOT_CACHED) {
                decodedCached = false;
            }
            if (decodedCached) {
                newDecoded = "/" + oldPart.decoded;
            } else {
                newDecoded = Uri.NOT_CACHED;
            }
            return new PathPart(newEncoded, newDecoded);
        }
    }

    public static Uri withAppendedPath(Uri baseUri, String pathSegment) {
        return baseUri.buildUpon().appendEncodedPath(pathSegment).build();
    }

    @UnsupportedAppUsage
    public Uri getCanonicalUri() {
        if (!ContentResolver.SCHEME_FILE.equals(getScheme())) {
            return this;
        }
        try {
            String canonicalPath = new File(getPath()).getCanonicalPath();
            if (Environment.isExternalStorageEmulated()) {
                String legacyPath = Environment.getLegacyExternalStorageDirectory().toString();
                if (canonicalPath.startsWith(legacyPath)) {
                    return fromFile(new File(Environment.getInternalStoragePath(), canonicalPath.substring(legacyPath.length() + 1)));
                }
            }
            return fromFile(new File(canonicalPath));
        } catch (IOException e) {
            return this;
        }
    }

    public void checkFileUriExposed(String location) {
        if (ContentResolver.SCHEME_FILE.equals(getScheme()) && getPath() != null && !getPath().startsWith("/system/")) {
            StrictMode.onFileUriExposed(this, location);
        }
    }

    public void checkContentUriWithoutPermission(String location, int flags) {
        if ("content".equals(getScheme()) && !Intent.isAccessUriMode(flags)) {
            StrictMode.onContentUriWithoutPermission(this, location);
        }
    }

    public boolean isPathPrefixMatch(Uri prefix) {
        if (!(Objects.equals(getScheme(), prefix.getScheme()) && Objects.equals(getAuthority(), prefix.getAuthority()))) {
            return false;
        }
        List<String> seg = getPathSegments();
        List<String> prefixSeg = prefix.getPathSegments();
        int prefixSize = prefixSeg.size();
        if (seg.size() < prefixSize) {
            return false;
        }
        for (int i = 0; i < prefixSize; i++) {
            if (!Objects.equals(seg.get(i), prefixSeg.get(i))) {
                return false;
            }
        }
        return true;
    }
}
