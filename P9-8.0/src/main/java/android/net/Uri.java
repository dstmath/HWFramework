package android.net;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.StrictMode;
import android.util.Log;
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
import libcore.net.UriCodec;

public abstract class Uri implements Parcelable, Comparable<Uri> {
    public static final Creator<Uri> CREATOR = new Creator<Uri>() {
        public Uri createFromParcel(Parcel in) {
            int type = in.readInt();
            switch (type) {
                case 0:
                    return null;
                case 1:
                    return StringUri.readFrom(in);
                case 2:
                    return OpaqueUri.readFrom(in);
                case 3:
                    return HierarchicalUri.readFrom(in);
                default:
                    throw new IllegalArgumentException("Unknown URI type: " + type);
            }
        }

        public Uri[] newArray(int size) {
            return new Uri[size];
        }
    };
    private static final String DEFAULT_ENCODING = "UTF-8";
    public static final Uri EMPTY = new HierarchicalUri(null, Part.NULL, PathPart.EMPTY, Part.NULL, Part.NULL, null);
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private static final String LOG = Uri.class.getSimpleName();
    private static final String NOT_CACHED = new String("NOT CACHED");
    private static final int NOT_CALCULATED = -2;
    private static final int NOT_FOUND = -1;
    private static final String NOT_HIERARCHICAL = "This isn't a hierarchical URI.";
    private static final int NULL_TYPE_ID = 0;

    private static abstract class AbstractHierarchicalUri extends Uri {
        private volatile String host;
        private volatile int port;
        private Part userInfo;

        /* synthetic */ AbstractHierarchicalUri(AbstractHierarchicalUri -this0) {
            this();
        }

        private AbstractHierarchicalUri() {
            super();
            this.host = Uri.NOT_CACHED;
            this.port = -2;
        }

        public String getLastPathSegment() {
            List<String> segments = getPathSegments();
            int size = segments.size();
            if (size == 0) {
                return null;
            }
            return (String) segments.get(size - 1);
        }

        private Part getUserInfoPart() {
            if (this.userInfo != null) {
                return this.userInfo;
            }
            Part fromEncoded = Part.fromEncoded(parseUserInfo());
            this.userInfo = fromEncoded;
            return fromEncoded;
        }

        public final String getEncodedUserInfo() {
            return getUserInfoPart().getEncoded();
        }

        private String parseUserInfo() {
            String str = null;
            String authority = getEncodedAuthority();
            if (authority == null) {
                return null;
            }
            int end = authority.lastIndexOf(64);
            if (end != -1) {
                str = authority.substring(0, end);
            }
            return str;
        }

        public String getUserInfo() {
            return getUserInfoPart().getDecoded();
        }

        public String getHost() {
            if (this.host != Uri.NOT_CACHED) {
                return this.host;
            }
            String parseHost = parseHost();
            this.host = parseHost;
            return parseHost;
        }

        private String parseHost() {
            String authority = getEncodedAuthority();
            if (authority == null) {
                return null;
            }
            String encodedHost;
            int userInfoSeparator = authority.lastIndexOf(64);
            int portSeparator = authority.indexOf(58, userInfoSeparator);
            if (portSeparator == -1) {
                encodedHost = authority.substring(userInfoSeparator + 1);
            } else {
                encodedHost = authority.substring(userInfoSeparator + 1, portSeparator);
            }
            return Uri.decode(encodedHost);
        }

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
            if (authority == null) {
                return -1;
            }
            int portSeparator = authority.indexOf(58, authority.lastIndexOf(64));
            if (portSeparator == -1) {
                return -1;
            }
            try {
                return Integer.parseInt(Uri.decode(authority.substring(portSeparator + 1)));
            } catch (NumberFormatException e) {
                Log.w(Uri.LOG, "Error parsing port string.", e);
                return -1;
            }
        }
    }

    static abstract class AbstractPart {
        volatile String decoded;
        volatile String encoded;

        static class Representation {
            static final int BOTH = 0;
            static final int DECODED = 2;
            static final int ENCODED = 1;

            Representation() {
            }
        }

        abstract String getEncoded();

        AbstractPart(String encoded, String decoded) {
            this.encoded = encoded;
            this.decoded = decoded;
        }

        final String getDecoded() {
            if (this.decoded != Uri.NOT_CACHED) {
                return this.decoded;
            }
            String decode = Uri.decode(this.encoded);
            this.decoded = decode;
            return decode;
        }

        final void writeTo(Parcel parcel) {
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

    public static final class Builder {
        private Part authority;
        private Part fragment;
        private Part opaquePart;
        private PathPart path;
        private Part query;
        private String scheme;

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        Builder opaquePart(Part opaquePart) {
            this.opaquePart = opaquePart;
            return this;
        }

        public Builder opaquePart(String opaquePart) {
            return opaquePart(Part.fromDecoded(opaquePart));
        }

        public Builder encodedOpaquePart(String opaquePart) {
            return opaquePart(Part.fromEncoded(opaquePart));
        }

        Builder authority(Part authority) {
            this.opaquePart = null;
            this.authority = authority;
            return this;
        }

        public Builder authority(String authority) {
            return authority(Part.fromDecoded(authority));
        }

        public Builder encodedAuthority(String authority) {
            return authority(Part.fromEncoded(authority));
        }

        Builder path(PathPart path) {
            this.opaquePart = null;
            this.path = path;
            return this;
        }

        public Builder path(String path) {
            return path(PathPart.fromDecoded(path));
        }

        public Builder encodedPath(String path) {
            return path(PathPart.fromEncoded(path));
        }

        public Builder appendPath(String newSegment) {
            return path(PathPart.appendDecodedSegment(this.path, newSegment));
        }

        public Builder appendEncodedPath(String newSegment) {
            return path(PathPart.appendEncodedSegment(this.path, newSegment));
        }

        Builder query(Part query) {
            this.opaquePart = null;
            this.query = query;
            return this;
        }

        public Builder query(String query) {
            return query(Part.fromDecoded(query));
        }

        public Builder encodedQuery(String query) {
            return query(Part.fromEncoded(query));
        }

        Builder fragment(Part fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder fragment(String fragment) {
            return fragment(Part.fromDecoded(fragment));
        }

        public Builder encodedFragment(String fragment) {
            return fragment(Part.fromEncoded(fragment));
        }

        public Builder appendQueryParameter(String key, String value) {
            this.opaquePart = null;
            String encodedParameter = Uri.encode(key, null) + "=" + Uri.encode(value, null);
            if (this.query == null) {
                this.query = Part.fromEncoded(encodedParameter);
                return this;
            }
            String oldQuery = this.query.getEncoded();
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
            if (this.opaquePart == null) {
                PathPart path = this.path;
                if (path == null || path == PathPart.NULL) {
                    path = PathPart.EMPTY;
                } else if (hasSchemeOrAuthority()) {
                    path = PathPart.makeAbsolute(path);
                }
                return new HierarchicalUri(this.scheme, this.authority, path, this.query, this.fragment, null);
            } else if (this.scheme != null) {
                return new OpaqueUri(this.scheme, this.opaquePart, this.fragment, null);
            } else {
                throw new UnsupportedOperationException("An opaque URI must have a scheme.");
            }
        }

        private boolean hasSchemeOrAuthority() {
            if (this.scheme == null) {
                return (this.authority == null || this.authority == Part.NULL) ? false : true;
            } else {
                return true;
            }
        }

        public String toString() {
            return build().toString();
        }
    }

    private static class HierarchicalUri extends AbstractHierarchicalUri {
        static final int TYPE_ID = 3;
        private final Part authority;
        private final Part fragment;
        private final PathPart path;
        private final Part query;
        private final String scheme;
        private Part ssp;
        private volatile String uriString;

        /* synthetic */ HierarchicalUri(String scheme, Part authority, PathPart path, Part query, Part fragment, HierarchicalUri -this5) {
            this(scheme, authority, path, query, fragment);
        }

        private HierarchicalUri(String scheme, Part authority, PathPart path, Part query, Part fragment) {
            super();
            this.uriString = Uri.NOT_CACHED;
            this.scheme = scheme;
            this.authority = Part.nonNull(authority);
            if (path == null) {
                path = PathPart.NULL;
            }
            this.path = path;
            this.query = Part.nonNull(query);
            this.fragment = Part.nonNull(fragment);
        }

        static Uri readFrom(Parcel parcel) {
            return new HierarchicalUri(parcel.readString(), Part.readFrom(parcel), PathPart.readFrom(parcel), Part.readFrom(parcel), Part.readFrom(parcel));
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(3);
            parcel.writeString(this.scheme);
            this.authority.writeTo(parcel);
            this.path.writeTo(parcel);
            this.query.writeTo(parcel);
            this.fragment.writeTo(parcel);
        }

        public boolean isHierarchical() {
            return true;
        }

        public boolean isRelative() {
            return this.scheme == null;
        }

        public String getScheme() {
            return this.scheme;
        }

        private Part getSsp() {
            if (this.ssp != null) {
                return this.ssp;
            }
            Part fromEncoded = Part.fromEncoded(makeSchemeSpecificPart());
            this.ssp = fromEncoded;
            return fromEncoded;
        }

        public String getEncodedSchemeSpecificPart() {
            return getSsp().getEncoded();
        }

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
                builder.append("//").append(encodedAuthority);
            }
            String encodedPath = this.path.getEncoded();
            if (encodedPath != null) {
                builder.append(encodedPath);
            }
            if (!this.query.isEmpty()) {
                builder.append('?').append(this.query.getEncoded());
            }
        }

        public String getAuthority() {
            return this.authority.getDecoded();
        }

        public String getEncodedAuthority() {
            return this.authority.getEncoded();
        }

        public String getEncodedPath() {
            return this.path.getEncoded();
        }

        public String getPath() {
            return this.path.getDecoded();
        }

        public String getQuery() {
            return this.query.getDecoded();
        }

        public String getEncodedQuery() {
            return this.query.getEncoded();
        }

        public String getFragment() {
            return this.fragment.getDecoded();
        }

        public String getEncodedFragment() {
            return this.fragment.getEncoded();
        }

        public List<String> getPathSegments() {
            return this.path.getPathSegments();
        }

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
            if (this.scheme != null) {
                builder.append(this.scheme).append(':');
            }
            appendSspTo(builder);
            if (!this.fragment.isEmpty()) {
                builder.append('#').append(this.fragment.getEncoded());
            }
            return builder.toString();
        }

        public Builder buildUpon() {
            return new Builder().scheme(this.scheme).authority(this.authority).path(this.path).query(this.query).fragment(this.fragment);
        }
    }

    private static class OpaqueUri extends Uri {
        static final int TYPE_ID = 2;
        private volatile String cachedString;
        private final Part fragment;
        private final String scheme;
        private final Part ssp;

        /* synthetic */ OpaqueUri(String scheme, Part ssp, Part fragment, OpaqueUri -this3) {
            this(scheme, ssp, fragment);
        }

        private OpaqueUri(String scheme, Part ssp, Part fragment) {
            super();
            this.cachedString = Uri.NOT_CACHED;
            this.scheme = scheme;
            this.ssp = ssp;
            if (fragment == null) {
                fragment = Part.NULL;
            }
            this.fragment = fragment;
        }

        static Uri readFrom(Parcel parcel) {
            return new OpaqueUri(parcel.readString(), Part.readFrom(parcel), Part.readFrom(parcel));
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(2);
            parcel.writeString(this.scheme);
            this.ssp.writeTo(parcel);
            this.fragment.writeTo(parcel);
        }

        public boolean isHierarchical() {
            return false;
        }

        public boolean isRelative() {
            return this.scheme == null;
        }

        public String getScheme() {
            return this.scheme;
        }

        public String getEncodedSchemeSpecificPart() {
            return this.ssp.getEncoded();
        }

        public String getSchemeSpecificPart() {
            return this.ssp.getDecoded();
        }

        public String getAuthority() {
            return null;
        }

        public String getEncodedAuthority() {
            return null;
        }

        public String getPath() {
            return null;
        }

        public String getEncodedPath() {
            return null;
        }

        public String getQuery() {
            return null;
        }

        public String getEncodedQuery() {
            return null;
        }

        public String getFragment() {
            return this.fragment.getDecoded();
        }

        public String getEncodedFragment() {
            return this.fragment.getEncoded();
        }

        public List<String> getPathSegments() {
            return Collections.emptyList();
        }

        public String getLastPathSegment() {
            return null;
        }

        public String getUserInfo() {
            return null;
        }

        public String getEncodedUserInfo() {
            return null;
        }

        public String getHost() {
            return null;
        }

        public int getPort() {
            return -1;
        }

        public String toString() {
            if (this.cachedString != Uri.NOT_CACHED) {
                return this.cachedString;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(this.scheme).append(':');
            sb.append(getEncodedSchemeSpecificPart());
            if (!this.fragment.isEmpty()) {
                sb.append('#').append(this.fragment.getEncoded());
            }
            String stringBuilder = sb.toString();
            this.cachedString = stringBuilder;
            return stringBuilder;
        }

        public Builder buildUpon() {
            return new Builder().scheme(this.scheme).opaquePart(this.ssp).fragment(this.fragment);
        }
    }

    static class Part extends AbstractPart {
        static final Part EMPTY = new EmptyPart(ProxyInfo.LOCAL_EXCL_LIST);
        static final Part NULL = new EmptyPart(null);

        private static class EmptyPart extends Part {
            public EmptyPart(String value) {
                super(value, value, null);
            }

            boolean isEmpty() {
                return true;
            }
        }

        /* synthetic */ Part(String encoded, String decoded, Part -this2) {
            this(encoded, decoded);
        }

        private Part(String encoded, String decoded) {
            super(encoded, decoded);
        }

        boolean isEmpty() {
            return false;
        }

        String getEncoded() {
            if (this.encoded != Uri.NOT_CACHED) {
                return this.encoded;
            }
            String encode = Uri.encode(this.decoded);
            this.encoded = encode;
            return encode;
        }

        static Part readFrom(Parcel parcel) {
            int representation = parcel.readInt();
            switch (representation) {
                case 0:
                    return from(parcel.readString(), parcel.readString());
                case 1:
                    return fromEncoded(parcel.readString());
                case 2:
                    return fromDecoded(parcel.readString());
                default:
                    throw new IllegalArgumentException("Unknown representation: " + representation);
            }
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
    }

    static class PathPart extends AbstractPart {
        static final PathPart EMPTY = new PathPart(ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST);
        static final PathPart NULL = new PathPart(null, null);
        private PathSegments pathSegments;

        private PathPart(String encoded, String decoded) {
            super(encoded, decoded);
        }

        String getEncoded() {
            if (this.encoded != Uri.NOT_CACHED) {
                return this.encoded;
            }
            String encode = Uri.encode(this.decoded, "/");
            this.encoded = encode;
            return encode;
        }

        PathSegments getPathSegments() {
            if (this.pathSegments != null) {
                return this.pathSegments;
            }
            String path = getEncoded();
            PathSegments pathSegments;
            if (path == null) {
                pathSegments = PathSegments.EMPTY;
                this.pathSegments = pathSegments;
                return pathSegments;
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
            pathSegments = segmentBuilder.build();
            this.pathSegments = pathSegments;
            return pathSegments;
        }

        static PathPart appendEncodedSegment(PathPart oldPart, String newSegment) {
            if (oldPart == null) {
                return fromEncoded("/" + newSegment);
            }
            String newPath;
            String oldPath = oldPart.getEncoded();
            if (oldPath == null) {
                oldPath = ProxyInfo.LOCAL_EXCL_LIST;
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
            switch (representation) {
                case 0:
                    return from(parcel.readString(), parcel.readString());
                case 1:
                    return fromEncoded(parcel.readString());
                case 2:
                    return fromDecoded(parcel.readString());
                default:
                    throw new IllegalArgumentException("Bad representation: " + representation);
            }
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
            boolean encodedCached = oldPart.encoded != Uri.NOT_CACHED;
            String oldPath = encodedCached ? oldPart.encoded : oldPart.decoded;
            if (oldPath == null || oldPath.length() == 0 || oldPath.startsWith("/")) {
                return oldPart;
            }
            String newDecoded;
            String newEncoded = encodedCached ? "/" + oldPart.encoded : Uri.NOT_CACHED;
            if (oldPart.decoded != Uri.NOT_CACHED) {
                newDecoded = "/" + oldPart.decoded;
            } else {
                newDecoded = Uri.NOT_CACHED;
            }
            return new PathPart(newEncoded, newDecoded);
        }
    }

    static class PathSegments extends AbstractList<String> implements RandomAccess {
        static final PathSegments EMPTY = new PathSegments(null, 0);
        final String[] segments;
        final int size;

        PathSegments(String[] segments, int size) {
            this.segments = segments;
            this.size = size;
        }

        public String get(int index) {
            if (index < this.size) {
                return this.segments[index];
            }
            throw new IndexOutOfBoundsException();
        }

        public int size() {
            return this.size;
        }
    }

    static class PathSegmentsBuilder {
        String[] segments;
        int size = 0;

        PathSegmentsBuilder() {
        }

        void add(String segment) {
            if (this.segments == null) {
                this.segments = new String[4];
            } else if (this.size + 1 == this.segments.length) {
                String[] expanded = new String[(this.segments.length * 2)];
                System.arraycopy(this.segments, 0, expanded, 0, this.segments.length);
                this.segments = expanded;
            }
            String[] strArr = this.segments;
            int i = this.size;
            this.size = i + 1;
            strArr[i] = segment;
        }

        PathSegments build() {
            if (this.segments == null) {
                return PathSegments.EMPTY;
            }
            try {
                PathSegments pathSegments = new PathSegments(this.segments, this.size);
                return pathSegments;
            } finally {
                this.segments = null;
            }
        }
    }

    private static class StringUri extends AbstractHierarchicalUri {
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

        /* synthetic */ StringUri(String uriString, StringUri -this1) {
            this(uriString);
        }

        private StringUri(String uriString) {
            super();
            this.cachedSsi = -2;
            this.cachedFsi = -2;
            this.scheme = Uri.NOT_CACHED;
            if (uriString == null) {
                throw new NullPointerException("uriString");
            }
            this.uriString = uriString;
        }

        static Uri readFrom(Parcel parcel) {
            return new StringUri(parcel.readString());
        }

        public int describeContents() {
            return 0;
        }

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

        public boolean isHierarchical() {
            boolean z = true;
            int ssi = findSchemeSeparator();
            if (ssi == -1) {
                return true;
            }
            if (this.uriString.length() == ssi + 1) {
                return false;
            }
            if (this.uriString.charAt(ssi + 1) != '/') {
                z = false;
            }
            return z;
        }

        public boolean isRelative() {
            return findSchemeSeparator() == -1;
        }

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
            return ssi == -1 ? null : this.uriString.substring(0, ssi);
        }

        private Part getSsp() {
            if (this.ssp != null) {
                return this.ssp;
            }
            Part fromEncoded = Part.fromEncoded(parseSsp());
            this.ssp = fromEncoded;
            return fromEncoded;
        }

        public String getEncodedSchemeSpecificPart() {
            return getSsp().getEncoded();
        }

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
            if (this.authority != null) {
                return this.authority;
            }
            Part fromEncoded = Part.fromEncoded(parseAuthority(this.uriString, findSchemeSeparator()));
            this.authority = fromEncoded;
            return fromEncoded;
        }

        public String getEncodedAuthority() {
            return getAuthorityPart().getEncoded();
        }

        public String getAuthority() {
            return getAuthorityPart().getDecoded();
        }

        private PathPart getPathPart() {
            if (this.path != null) {
                return this.path;
            }
            PathPart fromEncoded = PathPart.fromEncoded(parsePath());
            this.path = fromEncoded;
            return fromEncoded;
        }

        public String getPath() {
            return getPathPart().getDecoded();
        }

        public String getEncodedPath() {
            return getPathPart().getEncoded();
        }

        public List<String> getPathSegments() {
            return getPathPart().getPathSegments();
        }

        private String parsePath() {
            String uriString = this.uriString;
            int ssi = findSchemeSeparator();
            if (ssi > -1) {
                if ((ssi + 1 == uriString.length()) || uriString.charAt(ssi + 1) != '/') {
                    return null;
                }
            }
            return parsePath(uriString, ssi);
        }

        private Part getQueryPart() {
            if (this.query != null) {
                return this.query;
            }
            Part fromEncoded = Part.fromEncoded(parseQuery());
            this.query = fromEncoded;
            return fromEncoded;
        }

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

        public String getQuery() {
            return getQueryPart().getDecoded();
        }

        private Part getFragmentPart() {
            if (this.fragment != null) {
                return this.fragment;
            }
            Part fromEncoded = Part.fromEncoded(parseFragment());
            this.fragment = fromEncoded;
            return fromEncoded;
        }

        public String getEncodedFragment() {
            return getFragmentPart().getEncoded();
        }

        private String parseFragment() {
            int fsi = findFragmentSeparator();
            return fsi == -1 ? null : this.uriString.substring(fsi + 1);
        }

        public String getFragment() {
            return getFragmentPart().getDecoded();
        }

        public String toString() {
            return this.uriString;
        }

        static String parseAuthority(String uriString, int ssi) {
            int length = uriString.length();
            if (length <= ssi + 2 || uriString.charAt(ssi + 1) != '/' || uriString.charAt(ssi + 2) != '/') {
                return null;
            }
            int end = ssi + 3;
            while (end < length) {
                switch (uriString.charAt(end)) {
                    case '#':
                    case '/':
                    case '?':
                    case '\\':
                        break;
                    default:
                        end++;
                }
                return uriString.substring(ssi + 3, end);
            }
            return uriString.substring(ssi + 3, end);
        }

        static String parsePath(String uriString, int ssi) {
            int pathStart;
            int length = uriString.length();
            if (length > ssi + 2 && uriString.charAt(ssi + 1) == '/' && uriString.charAt(ssi + 2) == '/') {
                pathStart = ssi + 3;
                while (pathStart < length) {
                    switch (uriString.charAt(pathStart)) {
                        case '#':
                        case '?':
                            return ProxyInfo.LOCAL_EXCL_LIST;
                        case '/':
                        case '\\':
                            break;
                        default:
                            pathStart++;
                    }
                }
            } else {
                pathStart = ssi + 1;
            }
            int pathEnd = pathStart;
            while (pathEnd < length) {
                switch (uriString.charAt(pathEnd)) {
                    case '#':
                    case '?':
                        break;
                    default:
                        pathEnd++;
                }
                return uriString.substring(pathStart, pathEnd);
            }
            return uriString.substring(pathStart, pathEnd);
        }

        public Builder buildUpon() {
            if (isHierarchical()) {
                return new Builder().scheme(getScheme()).authority(getAuthorityPart()).path(getPathPart()).query(getQueryPart()).fragment(getFragmentPart());
            }
            return new Builder().scheme(getScheme()).opaquePart(getSsp()).fragment(getFragmentPart());
        }
    }

    /* synthetic */ Uri(Uri -this0) {
        this();
    }

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

    public abstract String toString();

    private Uri() {
    }

    public boolean isOpaque() {
        return isHierarchical() ^ 1;
    }

    public boolean isAbsolute() {
        return isRelative() ^ 1;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Uri)) {
            return false;
        }
        return toString().equals(((Uri) o).toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public int compareTo(Uri other) {
        return toString().compareTo(other.toString());
    }

    public String toSafeString() {
        StringBuilder builder;
        String scheme = getScheme();
        String ssp = getSchemeSpecificPart();
        if (scheme != null) {
            if (scheme.equalsIgnoreCase("tel") || scheme.equalsIgnoreCase(Context.SIP_SERVICE) || scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase("smsto") || scheme.equalsIgnoreCase("mailto") || scheme.equalsIgnoreCase(Context.NFC_SERVICE) || scheme.equalsIgnoreCase("geo")) {
                builder = new StringBuilder(64);
                builder.append(scheme);
                builder.append(':');
                if (ssp != null) {
                    for (int i = 0; i < ssp.length(); i++) {
                        char c = ssp.charAt(i);
                        if (c == '-' || c == '@' || c == '.') {
                            builder.append(c);
                        } else {
                            builder.append('x');
                        }
                    }
                }
                return builder.toString();
            } else if (scheme.equalsIgnoreCase(IntentFilter.SCHEME_HTTP) || scheme.equalsIgnoreCase(IntentFilter.SCHEME_HTTPS) || scheme.equalsIgnoreCase("ftp")) {
                ssp = "//" + (getHost() != null ? getHost() : ProxyInfo.LOCAL_EXCL_LIST) + (getPort() != -1 ? ":" + getPort() : ProxyInfo.LOCAL_EXCL_LIST) + "/...";
            }
        }
        builder = new StringBuilder(64);
        if (scheme != null) {
            builder.append(scheme);
            builder.append(':');
        }
        if (ssp != null) {
            builder.append(ssp);
        }
        return builder.toString();
    }

    public static Uri parse(String uriString) {
        return new StringUri(uriString, null);
    }

    public static Uri fromFile(File file) {
        if (file == null) {
            throw new NullPointerException(ContentResolver.SCHEME_FILE);
        }
        return new HierarchicalUri(ContentResolver.SCHEME_FILE, Part.EMPTY, PathPart.fromDecoded(file.getAbsolutePath()), Part.NULL, Part.NULL, null);
    }

    public static Uri fromParts(String scheme, String ssp, String fragment) {
        if (scheme == null) {
            throw new NullPointerException("scheme");
        } else if (ssp != null) {
            return new OpaqueUri(scheme, Part.fromDecoded(ssp), Part.fromDecoded(fragment), null);
        } else {
            throw new NullPointerException("ssp");
        }
    }

    public Set<String> getQueryParameterNames() {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        }
        String query = getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }
        Set<String> names = new LinkedHashSet();
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

    public List<String> getQueryParameters(String key) {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        } else if (key == null) {
            throw new NullPointerException("key");
        } else {
            String query = getEncodedQuery();
            if (query == null) {
                return Collections.emptyList();
            }
            try {
                String encodedKey = URLEncoder.encode(key, DEFAULT_ENCODING);
                ArrayList<String> values = new ArrayList();
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
                            values.add(ProxyInfo.LOCAL_EXCL_LIST);
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
        }
    }

    public String getQueryParameter(String key) {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        } else if (key == null) {
            throw new NullPointerException("key");
        } else {
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
                if (separator - start == encodedKey.length() && query.regionMatches(start, encodedKey, 0, encodedKey.length())) {
                    if (separator == end) {
                        return ProxyInfo.LOCAL_EXCL_LIST;
                    }
                    return UriCodec.decode(query.substring(separator + 1, end), true, StandardCharsets.UTF_8, false);
                } else if (nextAmpersand == -1) {
                    return null;
                } else {
                    start = nextAmpersand + 1;
                }
            }
        }
    }

    public boolean getBooleanQueryParameter(String key, boolean defaultValue) {
        String flag = getQueryParameter(key);
        if (flag == null) {
            return defaultValue;
        }
        flag = flag.toLowerCase(Locale.ROOT);
        return !"false".equals(flag) ? WifiEnterpriseConfig.ENGINE_DISABLE.equals(flag) ^ 1 : false;
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
                    encoded.append(s, current, nextToEncode);
                }
                current = nextToEncode;
                int nextAllowed = nextToEncode + 1;
                while (nextAllowed < oldLength && (isAllowed(s.charAt(nextAllowed), allow) ^ 1) != 0) {
                    nextAllowed++;
                }
                try {
                    byte[] bytes = s.substring(current, nextAllowed).getBytes(DEFAULT_ENCODING);
                    int bytesLength = bytes.length;
                    for (int i = 0; i < bytesLength; i++) {
                        encoded.append('%');
                        encoded.append(HEX_DIGITS[(bytes[i] & 240) >> 4]);
                        encoded.append(HEX_DIGITS[bytes[i] & 15]);
                    }
                    current = nextAllowed;
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            } else if (current == 0) {
                return s;
            } else {
                encoded.append(s, current, oldLength);
                return encoded.toString();
            }
        }
        if (encoded != null) {
            s = encoded.toString();
        }
        return s;
    }

    private static boolean isAllowed(char c, String allow) {
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if ((c >= '0' && c <= '9') || "_-!.~'()*".indexOf(c) != -1) {
            return true;
        }
        if (allow == null || allow.indexOf(c) == -1) {
            return false;
        }
        return true;
    }

    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        return UriCodec.decode(s, false, StandardCharsets.UTF_8, false);
    }

    public static Uri withAppendedPath(Uri baseUri, String pathSegment) {
        return baseUri.buildUpon().appendEncodedPath(pathSegment).build();
    }

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
        if (ContentResolver.SCHEME_FILE.equals(getScheme()) && getPath() != null && (getPath().startsWith("/system/") ^ 1) != 0) {
            StrictMode.onFileUriExposed(this, location);
        }
    }

    public void checkContentUriWithoutPermission(String location, int flags) {
        if ("content".equals(getScheme()) && (Intent.isAccessUriMode(flags) ^ 1) != 0) {
            StrictMode.onContentUriWithoutPermission(this, location);
        }
    }

    public boolean isPathPrefixMatch(Uri prefix) {
        if (!Objects.equals(getScheme(), prefix.getScheme()) || !Objects.equals(getAuthority(), prefix.getAuthority())) {
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
