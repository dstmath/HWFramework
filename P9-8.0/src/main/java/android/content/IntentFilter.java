package android.content;

import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.Log;
import android.util.Printer;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class IntentFilter implements Parcelable {
    private static final String ACTION_STR = "action";
    private static final String AGLOB_STR = "aglob";
    private static final String AUTH_STR = "auth";
    private static final String AUTO_VERIFY_STR = "autoVerify";
    private static final String CAT_STR = "cat";
    public static final Creator<IntentFilter> CREATOR = new Creator<IntentFilter>() {
        public IntentFilter createFromParcel(Parcel source) {
            return new IntentFilter(source);
        }

        public IntentFilter[] newArray(int size) {
            return new IntentFilter[size];
        }
    };
    private static final String HOST_STR = "host";
    private static final String LITERAL_STR = "literal";
    public static final int MATCH_ADJUSTMENT_MASK = 65535;
    public static final int MATCH_ADJUSTMENT_NORMAL = 32768;
    public static final int MATCH_CATEGORY_EMPTY = 1048576;
    public static final int MATCH_CATEGORY_HOST = 3145728;
    public static final int MATCH_CATEGORY_MASK = 268369920;
    public static final int MATCH_CATEGORY_PATH = 5242880;
    public static final int MATCH_CATEGORY_PORT = 4194304;
    public static final int MATCH_CATEGORY_SCHEME = 2097152;
    public static final int MATCH_CATEGORY_SCHEME_SPECIFIC_PART = 5767168;
    public static final int MATCH_CATEGORY_TYPE = 6291456;
    private static final String NAME_STR = "name";
    public static final int NO_MATCH_ACTION = -3;
    public static final int NO_MATCH_CATEGORY = -4;
    public static final int NO_MATCH_DATA = -2;
    public static final int NO_MATCH_TYPE = -1;
    private static final String PATH_STR = "path";
    private static final String PORT_STR = "port";
    private static final String PREFIX_STR = "prefix";
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";
    private static final String SCHEME_STR = "scheme";
    private static final String SGLOB_STR = "sglob";
    private static final String SSP_STR = "ssp";
    private static final int STATE_NEED_VERIFY = 16;
    private static final int STATE_NEED_VERIFY_CHECKED = 256;
    private static final int STATE_VERIFIED = 4096;
    private static final int STATE_VERIFY_AUTO = 1;
    public static final int SYSTEM_HIGH_PRIORITY = 1000;
    public static final int SYSTEM_LOW_PRIORITY = -1000;
    private static final String TYPE_STR = "type";
    public static final int VISIBILITY_EXPLICIT = 1;
    public static final int VISIBILITY_IMPLICIT = 2;
    public static final int VISIBILITY_NONE = 0;
    private ArrayList<ActionFilterEntry> mActionFilter;
    private final ArrayList<String> mActions;
    private ArrayList<String> mCategories;
    private ArrayList<AuthorityEntry> mDataAuthorities;
    private ArrayList<PatternMatcher> mDataPaths;
    private ArrayList<PatternMatcher> mDataSchemeSpecificParts;
    private ArrayList<String> mDataSchemes;
    private ArrayList<String> mDataTypes;
    private boolean mHasPartialTypes;
    private int mInstantAppVisibility;
    private int mOrder;
    private int mPriority;
    private int mVerifyState;

    public static final class ActionFilterEntry {
        private String mAction;
        private String mFilterName;
        private String mFilterValue;

        public ActionFilterEntry(String action, String filterName, String filterValue) {
            this.mAction = action;
            this.mFilterName = filterName;
            this.mFilterValue = filterValue;
        }

        ActionFilterEntry(Parcel src) {
            this.mAction = src.readString();
            this.mFilterName = src.readString();
            this.mFilterValue = src.readString();
        }

        void writeToParcel(Parcel dest) {
            dest.writeString(this.mAction);
            dest.writeString(this.mFilterName);
            dest.writeString(this.mFilterValue);
        }

        public String getAction() {
            return this.mAction;
        }

        public String getFilterName() {
            return this.mFilterName;
        }

        public String getFilterValue() {
            return this.mFilterValue;
        }

        public String toString() {
            return "actionFilter[action = " + this.mAction + ", filterName = " + this.mFilterName + ", filterValue = " + this.mFilterValue + "]";
        }
    }

    public static final class AuthorityEntry {
        private final String mHost;
        private final String mOrigHost;
        private final int mPort;
        private final boolean mWild;

        public AuthorityEntry(String host, String port) {
            boolean z = false;
            this.mOrigHost = host;
            if (host.length() > 0 && host.charAt(0) == '*') {
                z = true;
            }
            this.mWild = z;
            if (this.mWild) {
                host = host.substring(1).intern();
            }
            this.mHost = host;
            this.mPort = port != null ? Integer.parseInt(port) : -1;
        }

        AuthorityEntry(Parcel src) {
            boolean z = false;
            this.mOrigHost = src.readString();
            this.mHost = src.readString();
            if (src.readInt() != 0) {
                z = true;
            }
            this.mWild = z;
            this.mPort = src.readInt();
        }

        void writeToParcel(Parcel dest) {
            dest.writeString(this.mOrigHost);
            dest.writeString(this.mHost);
            dest.writeInt(this.mWild ? 1 : 0);
            dest.writeInt(this.mPort);
        }

        public String getHost() {
            return this.mOrigHost;
        }

        public int getPort() {
            return this.mPort;
        }

        public boolean match(AuthorityEntry other) {
            if (this.mWild == other.mWild && this.mHost.equals(other.mHost) && this.mPort == other.mPort) {
                return true;
            }
            return false;
        }

        public boolean equals(Object obj) {
            if (obj instanceof AuthorityEntry) {
                return match((AuthorityEntry) obj);
            }
            return false;
        }

        public int match(Uri data) {
            String host = data.getHost();
            if (host == null) {
                return -2;
            }
            if (this.mWild) {
                if (host.length() < this.mHost.length()) {
                    return -2;
                }
                host = host.substring(host.length() - this.mHost.length());
            }
            if (host.compareToIgnoreCase(this.mHost) != 0) {
                return -2;
            }
            if (this.mPort < 0) {
                return IntentFilter.MATCH_CATEGORY_HOST;
            }
            if (this.mPort != data.getPort()) {
                return -2;
            }
            return 4194304;
        }
    }

    public static class MalformedMimeTypeException extends AndroidException {
        public MalformedMimeTypeException(String name) {
            super(name);
        }
    }

    private static int findStringInSet(String[] set, String string, int[] lengths, int lenPos) {
        if (set == null) {
            return -1;
        }
        int N = lengths[lenPos];
        for (int i = 0; i < N; i++) {
            if (set[i].equals(string)) {
                return i;
            }
        }
        return -1;
    }

    private static String[] addStringToSet(String[] set, String string, int[] lengths, int lenPos) {
        if (findStringInSet(set, string, lengths, lenPos) >= 0) {
            return set;
        }
        if (set == null) {
            return new String[]{string, 1};
        }
        int N = lengths[lenPos];
        if (N < set.length) {
            set[N] = string;
            lengths[lenPos] = N + 1;
            return set;
        }
        String[] newSet = new String[(((N * 3) / 2) + 2)];
        System.arraycopy(set, 0, newSet, 0, N);
        set = newSet;
        newSet[N] = string;
        lengths[lenPos] = N + 1;
        return newSet;
    }

    private static String[] removeStringFromSet(String[] set, String string, int[] lengths, int lenPos) {
        int pos = findStringInSet(set, string, lengths, lenPos);
        if (pos < 0) {
            return set;
        }
        int N = lengths[lenPos];
        if (N > set.length / 4) {
            int copyLen = N - (pos + 1);
            if (copyLen > 0) {
                System.arraycopy(set, pos + 1, set, pos, copyLen);
            }
            set[N - 1] = null;
            lengths[lenPos] = N - 1;
            return set;
        }
        String[] newSet = new String[(set.length / 3)];
        if (pos > 0) {
            System.arraycopy(set, 0, newSet, 0, pos);
        }
        if (pos + 1 < N) {
            System.arraycopy(set, pos + 1, newSet, pos, N - (pos + 1));
        }
        return newSet;
    }

    public static IntentFilter create(String action, String dataType) {
        try {
            return new IntentFilter(action, dataType);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Bad MIME type", e);
        }
    }

    public IntentFilter() {
        this.mCategories = null;
        this.mDataSchemes = null;
        this.mActionFilter = null;
        this.mDataSchemeSpecificParts = null;
        this.mDataAuthorities = null;
        this.mDataPaths = null;
        this.mDataTypes = null;
        this.mHasPartialTypes = false;
        this.mPriority = 0;
        this.mActions = new ArrayList();
    }

    public IntentFilter(String action) {
        this.mCategories = null;
        this.mDataSchemes = null;
        this.mActionFilter = null;
        this.mDataSchemeSpecificParts = null;
        this.mDataAuthorities = null;
        this.mDataPaths = null;
        this.mDataTypes = null;
        this.mHasPartialTypes = false;
        this.mPriority = 0;
        this.mActions = new ArrayList();
        addAction(action);
    }

    public IntentFilter(String action, String dataType) throws MalformedMimeTypeException {
        this.mCategories = null;
        this.mDataSchemes = null;
        this.mActionFilter = null;
        this.mDataSchemeSpecificParts = null;
        this.mDataAuthorities = null;
        this.mDataPaths = null;
        this.mDataTypes = null;
        this.mHasPartialTypes = false;
        this.mPriority = 0;
        this.mActions = new ArrayList();
        addAction(action);
        addDataType(dataType);
    }

    public IntentFilter(IntentFilter o) {
        this.mCategories = null;
        this.mDataSchemes = null;
        this.mActionFilter = null;
        this.mDataSchemeSpecificParts = null;
        this.mDataAuthorities = null;
        this.mDataPaths = null;
        this.mDataTypes = null;
        this.mHasPartialTypes = false;
        this.mPriority = o.mPriority;
        this.mOrder = o.mOrder;
        this.mActions = new ArrayList(o.mActions);
        if (o.mActionFilter != null) {
            this.mActionFilter = new ArrayList(o.mActionFilter);
        }
        if (o.mCategories != null) {
            this.mCategories = new ArrayList(o.mCategories);
        }
        if (o.mDataTypes != null) {
            this.mDataTypes = new ArrayList(o.mDataTypes);
        }
        if (o.mDataSchemes != null) {
            this.mDataSchemes = new ArrayList(o.mDataSchemes);
        }
        if (o.mDataSchemeSpecificParts != null) {
            this.mDataSchemeSpecificParts = new ArrayList(o.mDataSchemeSpecificParts);
        }
        if (o.mDataAuthorities != null) {
            this.mDataAuthorities = new ArrayList(o.mDataAuthorities);
        }
        if (o.mDataPaths != null) {
            this.mDataPaths = new ArrayList(o.mDataPaths);
        }
        this.mHasPartialTypes = o.mHasPartialTypes;
        this.mVerifyState = o.mVerifyState;
        this.mInstantAppVisibility = o.mInstantAppVisibility;
    }

    public final void setPriority(int priority) {
        this.mPriority = priority;
    }

    public final int getPriority() {
        return this.mPriority;
    }

    public final void setOrder(int order) {
        this.mOrder = order;
    }

    public final int getOrder() {
        return this.mOrder;
    }

    public final void setAutoVerify(boolean autoVerify) {
        this.mVerifyState &= -2;
        if (autoVerify) {
            this.mVerifyState |= 1;
        }
    }

    public final boolean getAutoVerify() {
        return (this.mVerifyState & 1) == 1;
    }

    public final boolean handleAllWebDataURI() {
        if (hasCategory(Intent.CATEGORY_APP_BROWSER)) {
            return true;
        }
        return handlesWebUris(false) && countDataAuthorities() == 0;
    }

    public final boolean handlesWebUris(boolean onlyWebSchemes) {
        if (!hasAction("android.intent.action.VIEW") || (hasCategory(Intent.CATEGORY_BROWSABLE) ^ 1) != 0 || this.mDataSchemes == null || this.mDataSchemes.size() == 0) {
            return false;
        }
        int N = this.mDataSchemes.size();
        for (int i = 0; i < N; i++) {
            String scheme = (String) this.mDataSchemes.get(i);
            boolean isWebScheme = !SCHEME_HTTP.equals(scheme) ? SCHEME_HTTPS.equals(scheme) : true;
            if (onlyWebSchemes) {
                if (!isWebScheme) {
                    return false;
                }
            } else if (isWebScheme) {
                return true;
            }
        }
        return onlyWebSchemes;
    }

    public final boolean needsVerification() {
        return getAutoVerify() ? handlesWebUris(true) : false;
    }

    public final boolean isVerified() {
        boolean z = false;
        if ((this.mVerifyState & 256) != 256) {
            return false;
        }
        if ((this.mVerifyState & 16) == 16) {
            z = true;
        }
        return z;
    }

    public void setVerified(boolean verified) {
        this.mVerifyState |= 256;
        this.mVerifyState &= -4097;
        if (verified) {
            this.mVerifyState |= 4096;
        }
    }

    public void setVisibilityToInstantApp(int visibility) {
        this.mInstantAppVisibility = visibility;
    }

    public int getVisibilityToInstantApp() {
        return this.mInstantAppVisibility;
    }

    public boolean isVisibleToInstantApp() {
        return this.mInstantAppVisibility != 0;
    }

    public boolean isExplicitlyVisibleToInstantApp() {
        return this.mInstantAppVisibility == 1;
    }

    public boolean isImplicitlyVisibleToInstantApp() {
        return this.mInstantAppVisibility == 2;
    }

    public final void addAction(String action) {
        if (!this.mActions.contains(action)) {
            this.mActions.add(action.intern());
        }
    }

    public final int countActions() {
        return this.mActions.size();
    }

    public final String getAction(int index) {
        return (String) this.mActions.get(index);
    }

    public final boolean hasAction(String action) {
        return action != null ? this.mActions.contains(action) : false;
    }

    public final boolean matchAction(String action) {
        return hasAction(action);
    }

    public final Iterator<String> actionsIterator() {
        return this.mActions != null ? this.mActions.iterator() : null;
    }

    public final void addDataType(String type) throws MalformedMimeTypeException {
        int slashpos = type.indexOf(47);
        int typelen = type.length();
        if (slashpos <= 0 || typelen < slashpos + 2) {
            throw new MalformedMimeTypeException(type);
        }
        if (this.mDataTypes == null) {
            this.mDataTypes = new ArrayList();
        }
        if (typelen == slashpos + 2 && type.charAt(slashpos + 1) == '*') {
            String str = type.substring(0, slashpos);
            if (!this.mDataTypes.contains(str)) {
                this.mDataTypes.add(str.intern());
            }
            this.mHasPartialTypes = true;
        } else if (!this.mDataTypes.contains(type)) {
            this.mDataTypes.add(type.intern());
        }
    }

    public final boolean hasDataType(String type) {
        return this.mDataTypes != null ? findMimeType(type) : false;
    }

    public final boolean hasExactDataType(String type) {
        return this.mDataTypes != null ? this.mDataTypes.contains(type) : false;
    }

    public final int countDataTypes() {
        return this.mDataTypes != null ? this.mDataTypes.size() : 0;
    }

    public final String getDataType(int index) {
        return (String) this.mDataTypes.get(index);
    }

    public final Iterator<String> typesIterator() {
        return this.mDataTypes != null ? this.mDataTypes.iterator() : null;
    }

    public final void addDataScheme(String scheme) {
        if (this.mDataSchemes == null) {
            this.mDataSchemes = new ArrayList();
        }
        if (!this.mDataSchemes.contains(scheme)) {
            this.mDataSchemes.add(scheme.intern());
        }
    }

    public final int countDataSchemes() {
        return this.mDataSchemes != null ? this.mDataSchemes.size() : 0;
    }

    public final String getDataScheme(int index) {
        return (String) this.mDataSchemes.get(index);
    }

    public final boolean hasDataScheme(String scheme) {
        return this.mDataSchemes != null ? this.mDataSchemes.contains(scheme) : false;
    }

    public final Iterator<String> schemesIterator() {
        return this.mDataSchemes != null ? this.mDataSchemes.iterator() : null;
    }

    public final void addDataSchemeSpecificPart(String ssp, int type) {
        addDataSchemeSpecificPart(new PatternMatcher(ssp, type));
    }

    public final void addDataSchemeSpecificPart(PatternMatcher ssp) {
        if (this.mDataSchemeSpecificParts == null) {
            this.mDataSchemeSpecificParts = new ArrayList();
        }
        this.mDataSchemeSpecificParts.add(ssp);
    }

    public final int countDataSchemeSpecificParts() {
        return this.mDataSchemeSpecificParts != null ? this.mDataSchemeSpecificParts.size() : 0;
    }

    public final PatternMatcher getDataSchemeSpecificPart(int index) {
        return (PatternMatcher) this.mDataSchemeSpecificParts.get(index);
    }

    public final boolean hasDataSchemeSpecificPart(String data) {
        if (this.mDataSchemeSpecificParts == null) {
            return false;
        }
        int numDataSchemeSpecificParts = this.mDataSchemeSpecificParts.size();
        for (int i = 0; i < numDataSchemeSpecificParts; i++) {
            if (((PatternMatcher) this.mDataSchemeSpecificParts.get(i)).match(data)) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasDataSchemeSpecificPart(PatternMatcher ssp) {
        if (this.mDataSchemeSpecificParts == null) {
            return false;
        }
        int numDataSchemeSpecificParts = this.mDataSchemeSpecificParts.size();
        for (int i = 0; i < numDataSchemeSpecificParts; i++) {
            PatternMatcher pe = (PatternMatcher) this.mDataSchemeSpecificParts.get(i);
            if (pe.getType() == ssp.getType() && pe.getPath().equals(ssp.getPath())) {
                return true;
            }
        }
        return false;
    }

    public final Iterator<PatternMatcher> schemeSpecificPartsIterator() {
        return this.mDataSchemeSpecificParts != null ? this.mDataSchemeSpecificParts.iterator() : null;
    }

    public final void addDataAuthority(String host, String port) {
        if (port != null) {
            port = port.intern();
        }
        addDataAuthority(new AuthorityEntry(host.intern(), port));
    }

    public final void addDataAuthority(AuthorityEntry ent) {
        if (this.mDataAuthorities == null) {
            this.mDataAuthorities = new ArrayList();
        }
        this.mDataAuthorities.add(ent);
    }

    public final int countDataAuthorities() {
        return this.mDataAuthorities != null ? this.mDataAuthorities.size() : 0;
    }

    public final AuthorityEntry getDataAuthority(int index) {
        return (AuthorityEntry) this.mDataAuthorities.get(index);
    }

    public final boolean hasDataAuthority(Uri data) {
        return matchDataAuthority(data) >= 0;
    }

    public final boolean hasDataAuthority(AuthorityEntry auth) {
        if (this.mDataAuthorities == null) {
            return false;
        }
        int numDataAuthorities = this.mDataAuthorities.size();
        for (int i = 0; i < numDataAuthorities; i++) {
            if (((AuthorityEntry) this.mDataAuthorities.get(i)).match(auth)) {
                return true;
            }
        }
        return false;
    }

    public final Iterator<AuthorityEntry> authoritiesIterator() {
        return this.mDataAuthorities != null ? this.mDataAuthorities.iterator() : null;
    }

    public final void addDataPath(String path, int type) {
        addDataPath(new PatternMatcher(path.intern(), type));
    }

    public final void addDataPath(PatternMatcher path) {
        if (this.mDataPaths == null) {
            this.mDataPaths = new ArrayList();
        }
        this.mDataPaths.add(path);
    }

    public final int countDataPaths() {
        return this.mDataPaths != null ? this.mDataPaths.size() : 0;
    }

    public final PatternMatcher getDataPath(int index) {
        return (PatternMatcher) this.mDataPaths.get(index);
    }

    public final boolean hasDataPath(String data) {
        if (this.mDataPaths == null) {
            return false;
        }
        int numDataPaths = this.mDataPaths.size();
        for (int i = 0; i < numDataPaths; i++) {
            if (((PatternMatcher) this.mDataPaths.get(i)).match(data)) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasDataPath(PatternMatcher path) {
        if (this.mDataPaths == null) {
            return false;
        }
        int numDataPaths = this.mDataPaths.size();
        for (int i = 0; i < numDataPaths; i++) {
            PatternMatcher pe = (PatternMatcher) this.mDataPaths.get(i);
            if (pe.getType() == path.getType() && pe.getPath().equals(path.getPath())) {
                return true;
            }
        }
        return false;
    }

    public final Iterator<PatternMatcher> pathsIterator() {
        return this.mDataPaths != null ? this.mDataPaths.iterator() : null;
    }

    public final int matchDataAuthority(Uri data) {
        if (this.mDataAuthorities == null || data == null) {
            return -2;
        }
        int numDataAuthorities = this.mDataAuthorities.size();
        for (int i = 0; i < numDataAuthorities; i++) {
            int match = ((AuthorityEntry) this.mDataAuthorities.get(i)).match(data);
            if (match >= 0) {
                return match;
            }
        }
        return -2;
    }

    public final int matchData(String type, String scheme, Uri data) {
        int i = -2;
        ArrayList<String> types = this.mDataTypes;
        ArrayList<String> schemes = this.mDataSchemes;
        int match = 1048576;
        if (types == null && schemes == null) {
            if (type == null && data == null) {
                i = 1081344;
            }
            return i;
        }
        if (schemes != null) {
            if (scheme == null) {
                scheme = ProxyInfo.LOCAL_EXCL_LIST;
            }
            if (!schemes.contains(scheme)) {
                return -2;
            }
            match = 2097152;
            if (!(this.mDataSchemeSpecificParts == null || data == null)) {
                match = hasDataSchemeSpecificPart(data.getSchemeSpecificPart()) ? MATCH_CATEGORY_SCHEME_SPECIFIC_PART : -2;
            }
            if (!(match == MATCH_CATEGORY_SCHEME_SPECIFIC_PART || this.mDataAuthorities == null)) {
                int authMatch = matchDataAuthority(data);
                if (authMatch < 0) {
                    return -2;
                }
                if (this.mDataPaths == null) {
                    match = authMatch;
                } else if (!hasDataPath(data.getPath())) {
                    return -2;
                } else {
                    match = MATCH_CATEGORY_PATH;
                }
            }
            if (match == -2) {
                return -2;
            }
        } else if (!(scheme == null || (ProxyInfo.LOCAL_EXCL_LIST.equals(scheme) ^ 1) == 0 || ("content".equals(scheme) ^ 1) == 0 || (ContentResolver.SCHEME_FILE.equals(scheme) ^ 1) == 0)) {
            return -2;
        }
        if (types != null) {
            if (!findMimeType(type)) {
                return -1;
            }
            match = MATCH_CATEGORY_TYPE;
        } else if (type != null) {
            return -1;
        }
        return 32768 + match;
    }

    public final void addCategory(String category) {
        if (this.mCategories == null) {
            this.mCategories = new ArrayList();
        }
        if (!this.mCategories.contains(category)) {
            this.mCategories.add(category.intern());
        }
    }

    public final int countCategories() {
        return this.mCategories != null ? this.mCategories.size() : 0;
    }

    public final String getCategory(int index) {
        return (String) this.mCategories.get(index);
    }

    public final boolean hasCategory(String category) {
        return this.mCategories != null ? this.mCategories.contains(category) : false;
    }

    public final Iterator<String> categoriesIterator() {
        return this.mCategories != null ? this.mCategories.iterator() : null;
    }

    public final String matchCategories(Set<String> categories) {
        String str = null;
        if (categories == null) {
            return null;
        }
        Iterator<String> it = categories.iterator();
        if (this.mCategories == null) {
            if (it.hasNext()) {
                str = (String) it.next();
            }
            return str;
        }
        while (it.hasNext()) {
            String category = (String) it.next();
            if (!this.mCategories.contains(category)) {
                return category;
            }
        }
        return null;
    }

    public final int match(ContentResolver resolver, Intent intent, boolean resolve, String logTag) {
        return match(intent.getAction(), resolve ? intent.resolveType(resolver) : intent.getType(), intent.getScheme(), intent.getData(), intent.getCategories(), logTag);
    }

    public final int match(String action, String type, String scheme, Uri data, Set<String> categories, String logTag) {
        if (action != null && (matchAction(action) ^ 1) != 0) {
            return -3;
        }
        int dataMatch = matchData(type, scheme, data);
        if (dataMatch >= 0 && matchCategories(categories) != null) {
            return -4;
        }
        return dataMatch;
    }

    public void writeToXml(XmlSerializer serializer) throws IOException {
        int i;
        PatternMatcher pe;
        if (getAutoVerify()) {
            serializer.attribute(null, AUTO_VERIFY_STR, Boolean.toString(true));
        }
        int N = countActions();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, "action");
            serializer.attribute(null, "name", (String) this.mActions.get(i));
            serializer.endTag(null, "action");
        }
        N = countCategories();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, CAT_STR);
            serializer.attribute(null, "name", (String) this.mCategories.get(i));
            serializer.endTag(null, CAT_STR);
        }
        N = countDataTypes();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, "type");
            String type = (String) this.mDataTypes.get(i);
            if (type.indexOf(47) < 0) {
                type = type + "/*";
            }
            serializer.attribute(null, "name", type);
            serializer.endTag(null, "type");
        }
        N = countDataSchemes();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, SCHEME_STR);
            serializer.attribute(null, "name", (String) this.mDataSchemes.get(i));
            serializer.endTag(null, SCHEME_STR);
        }
        N = countDataSchemeSpecificParts();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, SSP_STR);
            pe = (PatternMatcher) this.mDataSchemeSpecificParts.get(i);
            switch (pe.getType()) {
                case 0:
                    serializer.attribute(null, LITERAL_STR, pe.getPath());
                    break;
                case 1:
                    serializer.attribute(null, PREFIX_STR, pe.getPath());
                    break;
                case 2:
                    serializer.attribute(null, SGLOB_STR, pe.getPath());
                    break;
                case 3:
                    serializer.attribute(null, AGLOB_STR, pe.getPath());
                    break;
                default:
                    break;
            }
            serializer.endTag(null, SSP_STR);
        }
        N = countDataAuthorities();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, AUTH_STR);
            AuthorityEntry ae = (AuthorityEntry) this.mDataAuthorities.get(i);
            serializer.attribute(null, HOST_STR, ae.getHost());
            if (ae.getPort() >= 0) {
                serializer.attribute(null, "port", Integer.toString(ae.getPort()));
            }
            serializer.endTag(null, AUTH_STR);
        }
        N = countDataPaths();
        for (i = 0; i < N; i++) {
            serializer.startTag(null, PATH_STR);
            pe = (PatternMatcher) this.mDataPaths.get(i);
            switch (pe.getType()) {
                case 0:
                    serializer.attribute(null, LITERAL_STR, pe.getPath());
                    break;
                case 1:
                    serializer.attribute(null, PREFIX_STR, pe.getPath());
                    break;
                case 2:
                    serializer.attribute(null, SGLOB_STR, pe.getPath());
                    break;
                case 3:
                    serializer.attribute(null, AGLOB_STR, pe.getPath());
                    break;
                default:
                    break;
            }
            serializer.endTag(null, PATH_STR);
        }
    }

    public void readFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        String autoVerify = parser.getAttributeValue(null, AUTO_VERIFY_STR);
        setAutoVerify(TextUtils.isEmpty(autoVerify) ? false : Boolean.getBoolean(autoVerify));
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                String name;
                if (tagName.equals("action")) {
                    name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        addAction(name);
                    }
                } else if (tagName.equals(CAT_STR)) {
                    name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        addCategory(name);
                    }
                } else if (tagName.equals("type")) {
                    name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        try {
                            addDataType(name);
                        } catch (MalformedMimeTypeException e) {
                        }
                    }
                } else if (tagName.equals(SCHEME_STR)) {
                    name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        addDataScheme(name);
                    }
                } else if (tagName.equals(SSP_STR)) {
                    String ssp = parser.getAttributeValue(null, LITERAL_STR);
                    if (ssp != null) {
                        addDataSchemeSpecificPart(ssp, 0);
                    } else {
                        ssp = parser.getAttributeValue(null, PREFIX_STR);
                        if (ssp != null) {
                            addDataSchemeSpecificPart(ssp, 1);
                        } else {
                            ssp = parser.getAttributeValue(null, SGLOB_STR);
                            if (ssp != null) {
                                addDataSchemeSpecificPart(ssp, 2);
                            } else {
                                ssp = parser.getAttributeValue(null, AGLOB_STR);
                                if (ssp != null) {
                                    addDataSchemeSpecificPart(ssp, 3);
                                }
                            }
                        }
                    }
                } else if (tagName.equals(AUTH_STR)) {
                    String host = parser.getAttributeValue(null, HOST_STR);
                    String port = parser.getAttributeValue(null, "port");
                    if (host != null) {
                        addDataAuthority(host, port);
                    }
                } else if (tagName.equals(PATH_STR)) {
                    String path = parser.getAttributeValue(null, LITERAL_STR);
                    if (path != null) {
                        addDataPath(path, 0);
                    } else {
                        path = parser.getAttributeValue(null, PREFIX_STR);
                        if (path != null) {
                            addDataPath(path, 1);
                        } else {
                            path = parser.getAttributeValue(null, SGLOB_STR);
                            if (path != null) {
                                addDataPath(path, 2);
                            } else {
                                path = parser.getAttributeValue(null, AGLOB_STR);
                                if (path != null) {
                                    addDataPath(path, 3);
                                }
                            }
                        }
                    }
                } else {
                    Log.w("IntentFilter", "Unknown tag parsing IntentFilter: " + tagName);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    public void dump(Printer du, String prefix) {
        Iterator<String> it;
        Iterator<PatternMatcher> it2;
        PatternMatcher pe;
        StringBuilder sb = new StringBuilder(256);
        if (this.mActions.size() > 0) {
            it = this.mActions.iterator();
            while (it.hasNext()) {
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Action: \"");
                sb.append((String) it.next());
                sb.append("\"");
                du.println(sb.toString());
            }
        }
        if (this.mCategories != null) {
            it = this.mCategories.iterator();
            while (it.hasNext()) {
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Category: \"");
                sb.append((String) it.next());
                sb.append("\"");
                du.println(sb.toString());
            }
        }
        if (this.mDataSchemes != null) {
            it = this.mDataSchemes.iterator();
            while (it.hasNext()) {
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Scheme: \"");
                sb.append((String) it.next());
                sb.append("\"");
                du.println(sb.toString());
            }
        }
        if (this.mDataSchemeSpecificParts != null) {
            it2 = this.mDataSchemeSpecificParts.iterator();
            while (it2.hasNext()) {
                pe = (PatternMatcher) it2.next();
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Ssp: \"");
                sb.append(pe);
                sb.append("\"");
                du.println(sb.toString());
            }
        }
        if (this.mDataAuthorities != null) {
            Iterator<AuthorityEntry> it3 = this.mDataAuthorities.iterator();
            while (it3.hasNext()) {
                AuthorityEntry ae = (AuthorityEntry) it3.next();
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Authority: \"");
                sb.append(ae.mHost);
                sb.append("\": ");
                sb.append(ae.mPort);
                if (ae.mWild) {
                    sb.append(" WILD");
                }
                du.println(sb.toString());
            }
        }
        if (this.mDataPaths != null) {
            it2 = this.mDataPaths.iterator();
            while (it2.hasNext()) {
                pe = (PatternMatcher) it2.next();
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Path: \"");
                sb.append(pe);
                sb.append("\"");
                du.println(sb.toString());
            }
        }
        if (this.mDataTypes != null) {
            it = this.mDataTypes.iterator();
            while (it.hasNext()) {
                sb.setLength(0);
                sb.append(prefix);
                sb.append("Type: \"");
                sb.append((String) it.next());
                sb.append("\"");
                du.println(sb.toString());
            }
        }
        if (this.mPriority != 0 || this.mHasPartialTypes) {
            sb.setLength(0);
            sb.append(prefix);
            sb.append("mPriority=");
            sb.append(this.mPriority);
            sb.append(", mHasPartialTypes=");
            sb.append(this.mHasPartialTypes);
            du.println(sb.toString());
        }
        if (getAutoVerify()) {
            sb.setLength(0);
            sb.append(prefix);
            sb.append("AutoVerify=");
            sb.append(getAutoVerify());
            du.println(sb.toString());
        }
    }

    public final int describeContents() {
        return 0;
    }

    public final void writeToParcel(Parcel dest, int flags) {
        int N;
        int i;
        int i2;
        int i3 = 1;
        dest.writeStringList(this.mActions);
        if (this.mCategories != null) {
            dest.writeInt(1);
            dest.writeStringList(this.mCategories);
        } else {
            dest.writeInt(0);
        }
        if (this.mDataSchemes != null) {
            dest.writeInt(1);
            dest.writeStringList(this.mDataSchemes);
        } else {
            dest.writeInt(0);
        }
        if (this.mDataTypes != null) {
            dest.writeInt(1);
            dest.writeStringList(this.mDataTypes);
        } else {
            dest.writeInt(0);
        }
        if (this.mDataSchemeSpecificParts != null) {
            N = this.mDataSchemeSpecificParts.size();
            dest.writeInt(N);
            for (i = 0; i < N; i++) {
                ((PatternMatcher) this.mDataSchemeSpecificParts.get(i)).writeToParcel(dest, flags);
            }
        } else {
            dest.writeInt(0);
        }
        if (this.mDataAuthorities != null) {
            N = this.mDataAuthorities.size();
            dest.writeInt(N);
            for (i = 0; i < N; i++) {
                ((AuthorityEntry) this.mDataAuthorities.get(i)).writeToParcel(dest);
            }
        } else {
            dest.writeInt(0);
        }
        if (this.mDataPaths != null) {
            N = this.mDataPaths.size();
            dest.writeInt(N);
            for (i = 0; i < N; i++) {
                ((PatternMatcher) this.mDataPaths.get(i)).writeToParcel(dest, flags);
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mPriority);
        if (this.mHasPartialTypes) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (!getAutoVerify()) {
            i3 = 0;
        }
        dest.writeInt(i3);
        dest.writeInt(this.mInstantAppVisibility);
        if (this.mActionFilter != null) {
            N = this.mActionFilter.size();
            dest.writeInt(N);
            for (i = 0; i < N; i++) {
                ((ActionFilterEntry) this.mActionFilter.get(i)).writeToParcel(dest);
            }
            return;
        }
        dest.writeInt(0);
    }

    public boolean debugCheck() {
        return true;
    }

    public IntentFilter(Parcel source) {
        int i;
        boolean z;
        boolean z2 = true;
        this.mCategories = null;
        this.mDataSchemes = null;
        this.mActionFilter = null;
        this.mDataSchemeSpecificParts = null;
        this.mDataAuthorities = null;
        this.mDataPaths = null;
        this.mDataTypes = null;
        this.mHasPartialTypes = false;
        this.mActions = new ArrayList();
        source.readStringList(this.mActions);
        if (source.readInt() != 0) {
            this.mCategories = new ArrayList();
            source.readStringList(this.mCategories);
        }
        if (source.readInt() != 0) {
            this.mDataSchemes = new ArrayList();
            source.readStringList(this.mDataSchemes);
        }
        if (source.readInt() != 0) {
            this.mDataTypes = new ArrayList();
            source.readStringList(this.mDataTypes);
        }
        int N = source.readInt();
        if (N > 0) {
            this.mDataSchemeSpecificParts = new ArrayList(N);
            for (i = 0; i < N; i++) {
                this.mDataSchemeSpecificParts.add(new PatternMatcher(source));
            }
        }
        N = source.readInt();
        if (N > 0) {
            this.mDataAuthorities = new ArrayList(N);
            for (i = 0; i < N; i++) {
                this.mDataAuthorities.add(new AuthorityEntry(source));
            }
        }
        N = source.readInt();
        if (N > 0) {
            this.mDataPaths = new ArrayList(N);
            for (i = 0; i < N; i++) {
                this.mDataPaths.add(new PatternMatcher(source));
            }
        }
        this.mPriority = source.readInt();
        if (source.readInt() > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasPartialTypes = z;
        if (source.readInt() <= 0) {
            z2 = false;
        }
        setAutoVerify(z2);
        setVisibilityToInstantApp(source.readInt());
        N = source.readInt();
        if (N > 0) {
            this.mActionFilter = new ArrayList(N);
            for (i = 0; i < N; i++) {
                this.mActionFilter.add(new ActionFilterEntry(source));
            }
        }
    }

    private final boolean findMimeType(String type) {
        ArrayList<String> t = this.mDataTypes;
        if (type == null) {
            return false;
        }
        if (t.contains(type)) {
            return true;
        }
        int typeLength = type.length();
        if (typeLength == 3 && type.equals("*/*")) {
            return t.isEmpty() ^ 1;
        }
        if (this.mHasPartialTypes && t.contains("*")) {
            return true;
        }
        int slashpos = type.indexOf(47);
        if (slashpos > 0) {
            if (this.mHasPartialTypes && t.contains(type.substring(0, slashpos))) {
                return true;
            }
            if (typeLength == slashpos + 2 && type.charAt(slashpos + 1) == '*') {
                int numTypes = t.size();
                for (int i = 0; i < numTypes; i++) {
                    if (type.regionMatches(0, (String) t.get(i), 0, slashpos + 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<String> getHostsList() {
        ArrayList<String> result = new ArrayList();
        Iterator<AuthorityEntry> it = authoritiesIterator();
        if (it != null) {
            while (it.hasNext()) {
                result.add(((AuthorityEntry) it.next()).getHost());
            }
        }
        return result;
    }

    public String[] getHosts() {
        ArrayList<String> list = getHostsList();
        return (String[]) list.toArray(new String[list.size()]);
    }

    public final void addActionFilter(String action, String actionFilterName, String actionFilterValue) {
        addActionFilter(new ActionFilterEntry(action, actionFilterName, actionFilterValue));
    }

    private final void addActionFilter(ActionFilterEntry actionFilter) {
        if (this.mActionFilter == null) {
            this.mActionFilter = new ArrayList();
        }
        this.mActionFilter.add(actionFilter);
    }

    public final int countActionFilters() {
        return this.mActionFilter != null ? this.mActionFilter.size() : 0;
    }

    public final Iterator<ActionFilterEntry> actionFilterIterator() {
        return this.mActionFilter != null ? this.mActionFilter.iterator() : null;
    }
}
