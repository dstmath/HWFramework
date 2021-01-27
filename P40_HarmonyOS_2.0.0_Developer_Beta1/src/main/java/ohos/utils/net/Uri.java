package ohos.utils.net;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import ohos.light.bean.LightEffect;
import ohos.miscservices.httpaccess.HttpConstant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public abstract class Uri implements Sequenceable, Comparable<Uri> {
    private static final int DEFAULT_PORT = -1;
    public static final Uri EMPTY_URI = new Builder().build();
    private static final int FIRST_POS = 0;
    private static final char FRAGMENT_SEPARATOR = '#';
    private static final int HEXADECIMAL = 16;
    private static final char[] HEXADECIMAL_DIGITS = "0123456789ABCDEF".toCharArray();
    private static final int HEXVALUE_BEGIN = 10;
    private static final int HIERARCHICAL_URI = 2;
    private static final int HIGH_MASK = 240;
    private static final String INVALID_INPUT_CHARACTER = "�";
    private static final char LOWER_CASE_BEGIN = 'a';
    private static final char LOWER_CASE_F = 'f';
    private static final int LOW_MASK = 15;
    private static final int NOT_FOUND = -1;
    private static final char NUMBER_BEGIN = '0';
    private static final char NUMBER_END = '9';
    private static final int OPAQUE_URI = 1;
    private static final String PATH_ALLOW = "/";
    private static final char PERCENT_SIGN = '%';
    private static final int POS_INC = 1;
    private static final int POS_INC_MORE = 2;
    public static final Sequenceable.Producer<Uri> PRODUCER = $$Lambda$Uri$O0J_9aJ8RDHfuPrKQFNLVSdtYo.INSTANCE;
    private static final char QUERY_FLAG = '?';
    private static final char RIGHT_SEPARATOR = '\\';
    private static final char SCHEME_FRAGMENT = '#';
    private static final char SCHEME_SEPARATOR = ':';
    private static final int SECOND_POS = 1;
    private static final char SLASH_SEPARATOR = '/';
    private static final char UPPER_CASE_BEGIN = 'A';
    private static final char UPPER_CASE_F = 'F';
    private static final Pattern URI_SCHEME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9|\\+|\\-|\\.]*$");
    String scheme;

    private static int hexCharToValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - NUMBER_BEGIN;
        }
        int i = 97;
        if (c < 'a' || c > 'f') {
            i = 65;
            if (c < 'A' || c > 'F') {
                return -1;
            }
        }
        return (c + '\n') - i;
    }

    public abstract String getDecodedAuthority();

    public abstract String getDecodedFragment();

    public abstract String getDecodedHost();

    public abstract String getDecodedPath();

    public abstract List<String> getDecodedPathList();

    public abstract String getDecodedQuery();

    public abstract Map<String, List<String>> getDecodedQueryParams();

    public abstract String getDecodedSchemeSpecificPart();

    public abstract String getDecodedUserInfo();

    public abstract String getEncodedAuthority();

    public abstract String getEncodedFragment();

    public abstract String getEncodedHost();

    public abstract String getEncodedPath();

    public abstract String getEncodedQuery();

    public abstract String getEncodedSchemeSpecificPart();

    public abstract String getEncodedUserInfo();

    public abstract int getPort();

    public abstract boolean isHierarchical();

    public abstract Builder makeBuilder();

    @Override // java.lang.Object
    public abstract String toString();

    static /* synthetic */ Uri lambda$static$0(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == 1) {
            return OpaqueUri.makeFromParcel(parcel);
        }
        if (readInt == 2) {
            return HierarchicalUri.makeFromParcel(parcel);
        }
        throw new IllegalArgumentException("unsupported URI type.");
    }

    private Uri(String str) {
        this.scheme = str;
    }

    public boolean isOpaque() {
        return !isHierarchical();
    }

    public boolean isRelative() {
        return this.scheme == null;
    }

    public boolean isAbsolute() {
        return !isRelative();
    }

    public String getScheme() {
        return this.scheme;
    }

    public static final class Builder {
        private StrPart authorityPart;
        private StrPart fragmentPart;
        private StrPart pathPart;
        private StrPart queryPart;
        private String schemeStr;
        private StrPart sspPart;

        public Builder scheme(String str) {
            this.schemeStr = str;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder opaqueSsp(StrPart strPart) {
            this.sspPart = strPart;
            return this;
        }

        public Builder decodedOpaqueSsp(String str) {
            return opaqueSsp(StrPart.getStrPart(str, null));
        }

        public Builder encodedOpaqueSsp(String str) {
            return opaqueSsp(StrPart.getStrPart(null, str));
        }

        /* access modifiers changed from: package-private */
        public Builder authority(StrPart strPart) {
            this.authorityPart = strPart;
            return this;
        }

        public Builder decodedAuthority(String str) {
            return authority(StrPart.getStrPart(str, null));
        }

        public Builder encodedAuthority(String str) {
            return authority(StrPart.getStrPart(null, str));
        }

        /* access modifiers changed from: package-private */
        public Builder path(StrPart strPart) {
            this.pathPart = strPart;
            return this;
        }

        public Builder decodedPath(String str) {
            return path(StrPart.getStrPart(str, null));
        }

        public Builder encodedPath(String str) {
            return path(StrPart.getStrPart(null, str));
        }

        public Builder appendDecodedPath(String str) {
            StrPart strPart = this.pathPart;
            if (strPart == null) {
                return decodedPath(Uri.PATH_ALLOW + str);
            }
            String dePart = strPart.getDePart();
            if (!dePart.endsWith(Uri.PATH_ALLOW)) {
                dePart = dePart + Uri.PATH_ALLOW;
            }
            return decodedPath(dePart + str);
        }

        public Builder appendEncodedPath(String str) {
            StrPart strPart = this.pathPart;
            if (strPart == null) {
                return encodedPath(Uri.PATH_ALLOW + str);
            }
            String enPart = strPart.getEnPart(Uri.PATH_ALLOW);
            if (!enPart.endsWith(Uri.PATH_ALLOW)) {
                enPart = enPart + Uri.PATH_ALLOW;
            }
            return encodedPath(enPart + str);
        }

        /* access modifiers changed from: package-private */
        public Builder query(StrPart strPart) {
            this.queryPart = strPart;
            return this;
        }

        public Builder decodedQuery(String str) {
            return query(StrPart.getStrPart(str, null));
        }

        public Builder encodedQuery(String str) {
            return query(StrPart.getStrPart(null, str));
        }

        public Builder appendDecodedQueryParam(String str, String str2) {
            String encode = Uri.encode(str);
            if (str2 != null) {
                encode = encode + "=" + Uri.encode(str2);
            }
            if (this.queryPart == null) {
                return encodedQuery(encode);
            }
            this.queryPart = StrPart.getStrPart(null, this.queryPart.getEnPart(null) + HttpConstant.URL_PARAM_DELIMITER + encode);
            return this;
        }

        public Builder clearQuery() {
            this.queryPart = null;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder fragment(StrPart strPart) {
            this.fragmentPart = strPart;
            return this;
        }

        public Builder decodedFragment(String str) {
            return fragment(StrPart.getStrPart(str, null));
        }

        public Builder encodedFragment(String str) {
            return fragment(StrPart.getStrPart(null, str));
        }

        public Uri build() {
            StrPart strPart;
            StrPart strPart2 = this.sspPart;
            if (strPart2 != null) {
                String str = this.schemeStr;
                if (str != null) {
                    return new OpaqueUri(str, strPart2, this.fragmentPart);
                }
                throw new UnsupportedOperationException("The scheme part can't be null.");
            }
            if (!((this.schemeStr == null && this.authorityPart == null) || (strPart = this.pathPart) == null)) {
                strPart.generateAbsolutePath();
            }
            return new HierarchicalUri(this.schemeStr, this.authorityPart, this.pathPart, this.queryPart, this.fragmentPart);
        }

        public String toString() {
            return build().toString();
        }
    }

    /* access modifiers changed from: private */
    public static class HierarchicalUri extends Uri {
        private StrPart authority;
        private StrPart fragment;
        private StrPart host;
        private final Object lock;
        private StrPart path;
        private volatile List<String> pathList;
        private Integer port;
        private StrPart query;
        private volatile Map<String, List<String>> queryParamMap;
        private StrPart ssp;
        private String uriStrCache;
        private StrPart userInfo;

        @Override // ohos.utils.net.Uri
        public boolean isHierarchical() {
            return true;
        }

        /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
        @Override // ohos.utils.net.Uri, java.lang.Comparable
        public /* bridge */ /* synthetic */ int compareTo(Uri uri) {
            return Uri.super.compareTo(uri);
        }

        private HierarchicalUri(String str, StrPart strPart, StrPart strPart2, StrPart strPart3, StrPart strPart4) {
            super(str);
            this.lock = new Object();
            this.authority = strPart;
            this.path = strPart2;
            this.query = strPart3;
            this.fragment = strPart4;
        }

        @Override // ohos.utils.Sequenceable
        public boolean marshalling(Parcel parcel) {
            if (parcel != null && parcel.writeInt(2) && parcel.writeString(this.scheme) && StrPart.marshalling(this.authority, parcel, null) && StrPart.marshalling(this.path, parcel, Uri.PATH_ALLOW) && StrPart.marshalling(this.query, parcel, null) && StrPart.marshalling(this.fragment, parcel, null)) {
                return true;
            }
            return false;
        }

        public static HierarchicalUri makeFromParcel(Parcel parcel) {
            return new HierarchicalUri(parcel.readString(), StrPart.unmarshalling(parcel), StrPart.unmarshalling(parcel), StrPart.unmarshalling(parcel), StrPart.unmarshalling(parcel));
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedSchemeSpecificPart() {
            return StrPart.getDeStrOrNull(generateSsp());
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedSchemeSpecificPart() {
            return StrPart.getEnStrOrNull(generateSsp(), Uri.PATH_ALLOW);
        }

        private StrPart generateSsp() {
            StrPart strPart = this.ssp;
            if (strPart != null) {
                return strPart;
            }
            StringBuilder sb = new StringBuilder();
            Optional.ofNullable(this.authority).ifPresent(new Consumer(sb) {
                /* class ohos.utils.net.$$Lambda$Uri$HierarchicalUri$jzIwnM_mhg8ZZoUKeptZDpUSk0I */
                private final /* synthetic */ StringBuilder f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Uri.HierarchicalUri.lambda$generateSsp$0(this.f$0, (Uri.StrPart) obj);
                }
            });
            Optional.ofNullable(this.path).ifPresent(new Consumer(sb) {
                /* class ohos.utils.net.$$Lambda$Uri$HierarchicalUri$Ti8ni1WpU3RFzrk0OhsMrBACBws */
                private final /* synthetic */ StringBuilder f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.append(((Uri.StrPart) obj).getEnPart(Uri.PATH_ALLOW));
                }
            });
            Optional.ofNullable(this.query).ifPresent(new Consumer(sb) {
                /* class ohos.utils.net.$$Lambda$Uri$HierarchicalUri$vLo5T8fWb5H_miw0BCZitl8nrX8 */
                private final /* synthetic */ StringBuilder f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Uri.HierarchicalUri.lambda$generateSsp$2(this.f$0, (Uri.StrPart) obj);
                }
            });
            if (sb.length() == 0) {
                return null;
            }
            StrPart strPart2 = StrPart.getStrPart(null, sb.toString());
            this.ssp = strPart2;
            return strPart2;
        }

        static /* synthetic */ void lambda$generateSsp$0(StringBuilder sb, StrPart strPart) {
            sb.append("//");
            sb.append(strPart.getEnPart(null));
        }

        static /* synthetic */ void lambda$generateSsp$2(StringBuilder sb, StrPart strPart) {
            sb.append(HttpConstant.URL_PARAM_SEPARATOR);
            sb.append(strPart.getEnPart(null));
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedAuthority() {
            return StrPart.getDeStrOrNull(this.authority);
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedAuthority() {
            return StrPart.getEnStrOrNull(this.authority, null);
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedUserInfo() {
            return StrPart.getDeStrOrNull(generateUserInfo(false));
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedUserInfo() {
            return StrPart.getEnStrOrNull(generateUserInfo(true), null);
        }

        private StrPart generateUserInfo(boolean z) {
            if (this.userInfo == null) {
                generateAuthority();
            }
            return this.userInfo;
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedHost() {
            return StrPart.getDeStrOrNull(generateHost());
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedHost() {
            return StrPart.getEnStrOrNull(generateHost(), null);
        }

        private StrPart generateHost() {
            if (this.host == null) {
                generateAuthority();
            }
            return this.host;
        }

        @Override // ohos.utils.net.Uri
        public int getPort() {
            return generatePort().intValue();
        }

        private Integer generatePort() {
            if (this.port == null) {
                generateAuthority();
            }
            return this.port;
        }

        private void generateAuthority() {
            StrPart strPart;
            String str;
            String encodedAuthority = getEncodedAuthority();
            if (encodedAuthority == null) {
                this.port = -1;
                return;
            }
            int lastIndexOf = encodedAuthority.lastIndexOf(64);
            if (lastIndexOf == -1) {
                strPart = null;
            } else {
                strPart = StrPart.getStrPart(null, encodedAuthority.substring(0, lastIndexOf));
            }
            this.userInfo = strPart;
            int findPortIndex = findPortIndex(encodedAuthority);
            if (findPortIndex == -1) {
                str = encodedAuthority.substring(lastIndexOf + 1);
            } else {
                str = encodedAuthority.substring(lastIndexOf + 1, findPortIndex);
            }
            this.host = StrPart.getStrPart(null, str);
            if (findPortIndex == -1) {
                this.port = -1;
            }
            try {
                this.port = Integer.valueOf(Integer.parseInt(decode(encodedAuthority.substring(findPortIndex + 1))));
            } catch (NumberFormatException unused) {
                this.port = -1;
            }
        }

        private int findPortIndex(String str) {
            if (str != null && !str.isEmpty()) {
                for (int length = str.length() - 1; length >= 0; length--) {
                    char charAt = str.charAt(length);
                    if (charAt == ':') {
                        return length;
                    }
                    if (charAt < '0' || charAt > '9') {
                        break;
                    }
                }
            }
            return -1;
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedPath() {
            return StrPart.getDeStrOrNull(this.path);
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedPath() {
            return StrPart.getEnStrOrNull(this.path, Uri.PATH_ALLOW);
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedQuery() {
            return StrPart.getDeStrOrNull(this.query);
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedQuery() {
            return StrPart.getEnStrOrNull(this.query, null);
        }

        @Override // ohos.utils.net.Uri
        public List<String> getDecodedPathList() {
            if (this.pathList != null) {
                return this.pathList;
            }
            String encodedPath = getEncodedPath();
            int i = 0;
            if (encodedPath == null) {
                ArrayList arrayList = new ArrayList(0);
                this.pathList = arrayList;
                return arrayList;
            }
            synchronized (this.lock) {
                if (this.pathList != null) {
                    return this.pathList;
                }
                ArrayList arrayList2 = new ArrayList();
                while (true) {
                    int indexOf = encodedPath.indexOf(47, i);
                    if (indexOf <= -1) {
                        break;
                    }
                    if (i < indexOf) {
                        arrayList2.add(decode(encodedPath.substring(i, indexOf)));
                    }
                    i = indexOf + 1;
                }
                if (i < encodedPath.length()) {
                    arrayList2.add(decode(encodedPath.substring(i)));
                }
                this.pathList = arrayList2;
                return arrayList2;
            }
        }

        @Override // ohos.utils.net.Uri
        public Map<String, List<String>> getDecodedQueryParams() {
            int i;
            if (this.queryParamMap != null) {
                return this.queryParamMap;
            }
            String encodedQuery = getEncodedQuery();
            if (encodedQuery == null) {
                HashMap hashMap = new HashMap();
                this.queryParamMap = hashMap;
                return hashMap;
            }
            synchronized (this.lock) {
                if (this.queryParamMap != null) {
                    return this.queryParamMap;
                }
                HashMap hashMap2 = new HashMap();
                int i2 = 0;
                while (true) {
                    int indexOf = encodedQuery.indexOf(38, i2);
                    if (indexOf != -1) {
                        i = indexOf;
                    } else {
                        i = encodedQuery.length();
                    }
                    int indexOf2 = encodedQuery.indexOf(61, i2);
                    if (indexOf2 > i || indexOf2 == -1) {
                        indexOf2 = i;
                    }
                    String decode = decode(encodedQuery.substring(i2, indexOf2));
                    String str = "";
                    if (indexOf2 != i) {
                        str = decode(encodedQuery.substring(indexOf2 + 1, i));
                    }
                    if (!hashMap2.containsKey(decode)) {
                        hashMap2.put(decode, new ArrayList());
                    }
                    ((List) hashMap2.get(decode)).add(str);
                    if (indexOf != -1) {
                        i2 = indexOf + 1;
                    } else {
                        this.queryParamMap = hashMap2;
                        return hashMap2;
                    }
                }
            }
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedFragment() {
            return StrPart.getDeStrOrNull(this.fragment);
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedFragment() {
            return StrPart.getEnStrOrNull(this.fragment, null);
        }

        @Override // ohos.utils.net.Uri
        public Builder makeBuilder() {
            return new Builder().scheme(this.scheme).authority(this.authority).path(this.path).query(this.query).fragment(this.fragment);
        }

        @Override // ohos.utils.net.Uri, java.lang.Object
        public String toString() {
            return (String) Optional.ofNullable(this.uriStrCache).orElseGet(new Supplier() {
                /* class ohos.utils.net.$$Lambda$Uri$HierarchicalUri$tijR91Zp3qYns27oK_RlbFnBs1g */

                @Override // java.util.function.Supplier
                public final Object get() {
                    return Uri.HierarchicalUri.this.lambda$toString$3$Uri$HierarchicalUri();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: getUriStr */
        public String lambda$toString$3$Uri$HierarchicalUri() {
            StringBuilder sb = new StringBuilder();
            if (this.scheme != null) {
                sb.append(this.scheme);
                sb.append(Uri.SCHEME_SEPARATOR);
            }
            if (this.ssp == null) {
                generateSsp();
            }
            if (this.ssp != null) {
                sb.append(getEncodedSchemeSpecificPart());
            }
            if (this.fragment != null) {
                sb.append('#');
                sb.append(getEncodedFragment());
            }
            String sb2 = sb.toString();
            this.uriStrCache = sb2;
            return sb2;
        }
    }

    /* access modifiers changed from: private */
    public static class OpaqueUri extends Uri {
        private StrPart fragment;
        private StrPart opaqueSsp;
        private String uriStrCache;

        @Override // ohos.utils.net.Uri
        public String getDecodedAuthority() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedHost() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedPath() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedQuery() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedUserInfo() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedAuthority() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedHost() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedPath() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedQuery() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedUserInfo() {
            return null;
        }

        @Override // ohos.utils.net.Uri
        public int getPort() {
            return -1;
        }

        @Override // ohos.utils.net.Uri
        public boolean isHierarchical() {
            return false;
        }

        /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
        @Override // ohos.utils.net.Uri, java.lang.Comparable
        public /* bridge */ /* synthetic */ int compareTo(Uri uri) {
            return Uri.super.compareTo(uri);
        }

        private OpaqueUri(String str, StrPart strPart, StrPart strPart2) {
            super(str);
            this.opaqueSsp = (StrPart) Optional.ofNullable(strPart).orElseThrow($$Lambda$Uri$OpaqueUri$tvCStsjZ9gjO06Bvv7AM5BlbhgU.INSTANCE);
            this.fragment = strPart2;
        }

        static /* synthetic */ UnsupportedOperationException lambda$new$0() {
            return new UnsupportedOperationException("The ssp part can't be null.");
        }

        @Override // ohos.utils.Sequenceable
        public boolean marshalling(Parcel parcel) {
            return parcel != null && parcel.writeInt(1) && parcel.writeString(this.scheme) && StrPart.marshalling(this.opaqueSsp, parcel, null) && StrPart.marshalling(this.fragment, parcel, null);
        }

        public static OpaqueUri makeFromParcel(Parcel parcel) {
            return new OpaqueUri(parcel.readString(), StrPart.unmarshalling(parcel), StrPart.unmarshalling(parcel));
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedSchemeSpecificPart() {
            return this.opaqueSsp.getDePart();
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedSchemeSpecificPart() {
            return this.opaqueSsp.getEnPart(null);
        }

        @Override // ohos.utils.net.Uri
        public List<String> getDecodedPathList() {
            return new ArrayList(0);
        }

        @Override // ohos.utils.net.Uri
        public Map<String, List<String>> getDecodedQueryParams() {
            return new HashMap();
        }

        @Override // ohos.utils.net.Uri
        public String getDecodedFragment() {
            return StrPart.getDeStrOrNull(this.fragment);
        }

        @Override // ohos.utils.net.Uri
        public String getEncodedFragment() {
            return StrPart.getEnStrOrNull(this.fragment, null);
        }

        @Override // ohos.utils.net.Uri
        public Builder makeBuilder() {
            return new Builder().scheme(this.scheme).opaqueSsp(this.opaqueSsp).fragment(this.fragment);
        }

        @Override // ohos.utils.net.Uri, java.lang.Object
        public String toString() {
            return (String) Optional.ofNullable(this.uriStrCache).orElseGet(new Supplier() {
                /* class ohos.utils.net.$$Lambda$Uri$OpaqueUri$iro1q3H3YLBYeNfGpWAtS3wHKR4 */

                @Override // java.util.function.Supplier
                public final Object get() {
                    return Uri.OpaqueUri.this.lambda$toString$1$Uri$OpaqueUri();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: getUriStr */
        public String lambda$toString$1$Uri$OpaqueUri() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.scheme);
            sb.append(Uri.SCHEME_SEPARATOR);
            sb.append(getEncodedSchemeSpecificPart());
            if (this.fragment != null) {
                sb.append('#');
                sb.append(getEncodedFragment());
            }
            String sb2 = sb.toString();
            this.uriStrCache = sb2;
            return sb2;
        }
    }

    /* access modifiers changed from: private */
    public static class StrPart {
        private volatile String dePart;
        private volatile String enPart;

        private StrPart(String str, String str2) {
            this.dePart = str;
            this.enPart = str2;
        }

        static StrPart getStrPart(String str, String str2) {
            if (str == null && str2 == null) {
                return null;
            }
            return new StrPart(str, str2);
        }

        static StrPart unmarshalling(Parcel parcel) {
            return getStrPart(parcel.readString(), parcel.readString());
        }

        static boolean marshalling(StrPart strPart, Parcel parcel, String str) {
            return strPart != null ? parcel.writeString(strPart.getDePart()) && parcel.writeString(strPart.getEnPart(str)) : parcel.writeString(null) && parcel.writeString(null);
        }

        static String getDeStrOrNull(StrPart strPart) {
            if (strPart != null) {
                return strPart.getDePart();
            }
            return null;
        }

        static String getEnStrOrNull(StrPart strPart, String str) {
            if (strPart != null) {
                return strPart.getEnPart(str);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public String getDePart() {
            return (String) Optional.ofNullable(this.dePart).orElseGet(new Supplier() {
                /* class ohos.utils.net.$$Lambda$Uri$StrPart$oehPCNu54S2CgAPZkHBUuyPDKaM */

                @Override // java.util.function.Supplier
                public final Object get() {
                    return Uri.StrPart.this.lambda$getDePart$0$Uri$StrPart();
                }
            });
        }

        public /* synthetic */ String lambda$getDePart$0$Uri$StrPart() {
            String decode = Uri.decode(this.enPart);
            this.dePart = decode;
            return decode;
        }

        /* access modifiers changed from: package-private */
        public String getEnPart(String str) {
            return (String) Optional.ofNullable(this.enPart).orElseGet(new Supplier(str) {
                /* class ohos.utils.net.$$Lambda$Uri$StrPart$meLhItyoinsEwMUFZnm7t2LayLs */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Supplier
                public final Object get() {
                    return Uri.StrPart.this.lambda$getEnPart$1$Uri$StrPart(this.f$1);
                }
            });
        }

        public /* synthetic */ String lambda$getEnPart$1$Uri$StrPart(String str) {
            String encode = Uri.encode(this.dePart, str);
            this.enPart = encode;
            return encode;
        }

        /* access modifiers changed from: package-private */
        public void generateAbsolutePath() {
            Optional.ofNullable(this.dePart).ifPresent(new Consumer() {
                /* class ohos.utils.net.$$Lambda$Uri$StrPart$i6oXelh6pag_VUf3j53KyUXc78 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Uri.StrPart.this.lambda$generateAbsolutePath$2$Uri$StrPart((String) obj);
                }
            });
            Optional.ofNullable(this.enPart).ifPresent(new Consumer() {
                /* class ohos.utils.net.$$Lambda$Uri$StrPart$t2eEN1jCbV_QY8kZXCEGuv35E7A */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Uri.StrPart.this.lambda$generateAbsolutePath$3$Uri$StrPart((String) obj);
                }
            });
        }

        public /* synthetic */ void lambda$generateAbsolutePath$2$Uri$StrPart(String str) {
            if (!str.startsWith(Uri.PATH_ALLOW)) {
                this.dePart = Uri.PATH_ALLOW + str;
            }
        }

        public /* synthetic */ void lambda$generateAbsolutePath$3$Uri$StrPart(String str) {
            if (!str.startsWith(Uri.PATH_ALLOW)) {
                this.enPart = Uri.PATH_ALLOW + str;
            }
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        throw new UnsupportedOperationException("unsupported unmarshalling, please use CREATOR.");
    }

    public static String encode(String str) {
        return encode(str, null);
    }

    public static String encode(String str, String str2) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (isAllowed(charAt, str2)) {
                sb.append(charAt);
            } else {
                byte[] bytes = String.valueOf(charAt).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append(PERCENT_SIGN);
                    sb.append(HEXADECIMAL_DIGITS[(b & 240) >> 4]);
                    sb.append(HEXADECIMAL_DIGITS[b & 15]);
                }
            }
        }
        return sb.toString();
    }

    private static boolean isAllowed(char c, String str) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || !((c < '0' || c > '9') && "_-!.~'()*".indexOf(c) == -1 && (str == null || str.indexOf(c) == -1));
    }

    public static String decode(String str) {
        if (str == null || str.isEmpty() || str.indexOf(37) < 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        CharsetDecoder onUnmappableCharacter = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).replaceWith(INVALID_INPUT_CHARACTER).onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer allocate = ByteBuffer.allocate(str.length());
        int i = 0;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            i++;
            if (charAt == '%') {
                byte b = 0;
                int i2 = i;
                int i3 = 0;
                while (true) {
                    if (i3 >= 2) {
                        break;
                    } else if (i2 >= str.length()) {
                        return sb.toString();
                    } else {
                        char charAt2 = str.charAt(i2);
                        i2++;
                        int hexCharToValue = hexCharToValue(charAt2);
                        if (hexCharToValue < 0) {
                            flushDecodingByteAccumulator(sb, onUnmappableCharacter, allocate);
                            sb.append(INVALID_INPUT_CHARACTER);
                            break;
                        }
                        b = (byte) ((b * 16) + hexCharToValue);
                        i3++;
                    }
                }
                allocate.put(b);
                i = i2;
            } else {
                flushDecodingByteAccumulator(sb, onUnmappableCharacter, allocate);
                sb.append(charAt);
            }
        }
        flushDecodingByteAccumulator(sb, onUnmappableCharacter, allocate);
        return sb.toString();
    }

    private static void flushDecodingByteAccumulator(StringBuilder sb, CharsetDecoder charsetDecoder, ByteBuffer byteBuffer) {
        if (byteBuffer.position() != 0) {
            byteBuffer.flip();
            try {
                sb.append((CharSequence) charsetDecoder.decode(byteBuffer));
            } catch (CharacterCodingException unused) {
                sb.append(INVALID_INPUT_CHARACTER);
            } catch (Throwable th) {
                byteBuffer.flip();
                byteBuffer.limit(byteBuffer.capacity());
                throw th;
            }
            byteBuffer.flip();
            byteBuffer.limit(byteBuffer.capacity());
        }
    }

    public static Uri getUriFromParts(String str, String str2, String str3) {
        if (str == null) {
            throw new NullPointerException("scheme can't be null");
        } else if (str2 != null) {
            return new OpaqueUri(str, StrPart.getStrPart(str2, null), StrPart.getStrPart(str3, null));
        } else {
            throw new NullPointerException("ssp can't be null");
        }
    }

    public static Uri getUriFromFile(File file) {
        if (file != null) {
            return new HierarchicalUri("file", StrPart.getStrPart("", null), StrPart.getStrPart(file.getAbsolutePath(), null), null, null);
        }
        throw new NullPointerException("file can't be null");
    }

    public static Uri getUriFromFileCanonicalPath(File file) throws IOException {
        if (file != null) {
            return new HierarchicalUri("file", StrPart.getStrPart("", null), StrPart.getStrPart(file.getCanonicalPath(), null), null, null);
        }
        throw new NullPointerException("file can't be null");
    }

    public static Uri appendEncodedPathToUri(Uri uri, String str) {
        if (!uri.isOpaque()) {
            return uri.makeBuilder().appendEncodedPath(str).build();
        }
        throw new UnsupportedOperationException("opaque uri can't append path");
    }

    public static Uri parse(String str) {
        if (str == null || str.isEmpty()) {
            throw new NullPointerException("uriStr is null or empty.");
        }
        Builder builder = new Builder();
        String parseScheme = parseScheme(str);
        builder.scheme(parseScheme);
        builder.encodedFragment(parseFragment(str));
        String parseSchemeSpecificPart = parseSchemeSpecificPart(str);
        if (parseScheme == null || parseSchemeSpecificPart == null || parseSchemeSpecificPart.startsWith(PATH_ALLOW)) {
            builder.encodedAuthority(parseAuthority(parseSchemeSpecificPart));
            builder.encodedPath(parsePath(str));
            builder.encodedQuery(parseQuery(parseSchemeSpecificPart));
            return builder.build();
        }
        builder.encodedOpaqueSsp(parseSchemeSpecificPart);
        return builder.build();
    }

    public static Uri readFromParcel(Parcel parcel) {
        if (parcel != null && parcel.readInt() == 1) {
            return PRODUCER.createFromParcel(parcel);
        }
        throw new IllegalArgumentException("wrong input");
    }

    private static String parseScheme(String str) {
        int indexOf;
        if (str == null || str.isEmpty() || (indexOf = str.indexOf(58)) <= 0) {
            return null;
        }
        String substring = str.substring(0, indexOf);
        if (URI_SCHEME_PATTERN.matcher(substring).matches()) {
            return substring;
        }
        throw new IllegalArgumentException("scheme is not illegal.");
    }

    private static String parseSchemeSpecificPart(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        int indexOf = str.indexOf(58);
        int indexOf2 = str.indexOf(35);
        if (indexOf2 == -1) {
            return str.substring(indexOf + 1);
        }
        return str.substring(indexOf + 1, indexOf2);
    }

    private static String parseFragment(String str) {
        int indexOf;
        if (str == null || str.isEmpty() || (indexOf = str.indexOf(35)) == -1) {
            return null;
        }
        return str.substring(indexOf + 1);
    }

    private static String parseAuthority(String str) {
        int length;
        if (str == null || str.isEmpty() || (length = str.length()) <= 2 || str.charAt(0) != '/' || str.charAt(1) != '/') {
            return null;
        }
        int i = 2;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '/' || charAt == '\\' || charAt == '?' || charAt == '#') {
                break;
            }
            i++;
        }
        return str.substring(2, i);
    }

    private static String parsePath(String str) {
        int indexOf = str.indexOf(58);
        int length = str.length();
        int i = indexOf == -1 ? 0 : indexOf + 1;
        int i2 = i + 1;
        if (length > i2 && str.charAt(i) == '/' && str.charAt(i2) == '/') {
            i += 2;
            while (i < length) {
                char charAt = str.charAt(i);
                if (charAt != '?' && charAt != '#') {
                    if (charAt == '/' || charAt == '\\') {
                        break;
                    }
                    i++;
                } else {
                    return null;
                }
            }
        }
        int i3 = i;
        while (i3 < length) {
            char charAt2 = str.charAt(i3);
            if (charAt2 == '?' || charAt2 == '#') {
                break;
            }
            i3++;
        }
        if (i3 == i) {
            return null;
        }
        return str.substring(i, i3);
    }

    private static String parseQuery(String str) {
        int indexOf;
        if (str == null || str.isEmpty() || (indexOf = str.indexOf(63)) == -1) {
            return null;
        }
        return str.substring(indexOf + 1);
    }

    public String getLastPath() {
        List<String> decodedPathList = getDecodedPathList();
        if (decodedPathList.isEmpty()) {
            return null;
        }
        return decodedPathList.get(decodedPathList.size() - 1);
    }

    public List<String> getQueryParamsByKey(String str) {
        return getDecodedQueryParams().getOrDefault(str, new ArrayList(0));
    }

    public String getFirstQueryParamByKey(String str) {
        List<String> queryParamsByKey = getQueryParamsByKey(str);
        if (queryParamsByKey.isEmpty()) {
            return "";
        }
        return queryParamsByKey.get(0);
    }

    public Set<String> getQueryParamNames() {
        return getDecodedQueryParams().keySet();
    }

    public boolean getBooleanQueryParam(String str, boolean z) {
        String firstQueryParamByKey = getFirstQueryParamByKey(str);
        if (firstQueryParamByKey == null) {
            return z;
        }
        String lowerCase = firstQueryParamByKey.toLowerCase(Locale.ROOT);
        return !"false".equals(lowerCase) && !LightEffect.LIGHT_ID_LED.equals(lowerCase);
    }

    public Uri getLowerCaseScheme() {
        String scheme2 = getScheme();
        if (scheme2 == null) {
            return this;
        }
        String lowerCase = scheme2.toLowerCase(Locale.ROOT);
        if (scheme2.equals(lowerCase)) {
            return this;
        }
        return makeBuilder().scheme(lowerCase).build();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Uri) {
            return ((Uri) obj).toString().equals(toString());
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hashCode(toString());
    }

    public int compareTo(Uri uri) {
        return toString().compareTo(uri.toString());
    }

    @Override // ohos.utils.Sequenceable
    public boolean hasFileDescriptor() {
        return super.hasFileDescriptor();
    }
}
