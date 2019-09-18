package com.android.server.slice;

import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.slice.DirtyTracker;
import com.android.server.slice.SlicePermissionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SliceClientPermissions implements DirtyTracker, DirtyTracker.Persistable {
    private static final String ATTR_AUTHORITY = "authority";
    private static final String ATTR_FULL_ACCESS = "fullAccess";
    private static final String ATTR_PKG = "pkg";
    /* access modifiers changed from: private */
    public static final String NAMESPACE = null;
    private static final String TAG = "SliceClientPermissions";
    private static final String TAG_AUTHORITY = "authority";
    static final String TAG_CLIENT = "client";
    private static final String TAG_PATH = "path";
    private final ArrayMap<SlicePermissionManager.PkgUser, SliceAuthority> mAuths = new ArrayMap<>();
    private boolean mHasFullAccess;
    private final SlicePermissionManager.PkgUser mPkg;
    private final DirtyTracker mTracker;

    public static class SliceAuthority implements DirtyTracker.Persistable {
        public static final String DELIMITER = "/";
        /* access modifiers changed from: private */
        public final String mAuthority;
        private final ArraySet<String[]> mPaths = new ArraySet<>();
        /* access modifiers changed from: private */
        public final SlicePermissionManager.PkgUser mPkg;
        private final DirtyTracker mTracker;

        public SliceAuthority(String authority, SlicePermissionManager.PkgUser pkg, DirtyTracker tracker) {
            this.mAuthority = authority;
            this.mPkg = pkg;
            this.mTracker = tracker;
        }

        public String getAuthority() {
            return this.mAuthority;
        }

        public SlicePermissionManager.PkgUser getPkg() {
            return this.mPkg;
        }

        /* access modifiers changed from: package-private */
        public void addPath(List<String> path) {
            String[] pathSegs = (String[]) path.toArray(new String[path.size()]);
            int i = this.mPaths.size() - 1;
            while (i >= 0) {
                String[] existing = this.mPaths.valueAt(i);
                if (!isPathPrefixMatch(existing, pathSegs)) {
                    if (isPathPrefixMatch(pathSegs, existing)) {
                        this.mPaths.removeAt(i);
                    }
                    i--;
                } else {
                    return;
                }
            }
            this.mPaths.add(pathSegs);
            this.mTracker.onPersistableDirty(this);
        }

        /* access modifiers changed from: package-private */
        public void removePath(List<String> path) {
            boolean changed = false;
            String[] pathSegs = (String[]) path.toArray(new String[path.size()]);
            for (int i = this.mPaths.size() - 1; i >= 0; i--) {
                if (isPathPrefixMatch(pathSegs, this.mPaths.valueAt(i))) {
                    changed = true;
                    this.mPaths.removeAt(i);
                }
            }
            if (changed) {
                this.mTracker.onPersistableDirty(this);
            }
        }

        public synchronized Collection<String[]> getPaths() {
            return new ArraySet(this.mPaths);
        }

        public boolean hasPermission(List<String> path) {
            Iterator<String[]> it = this.mPaths.iterator();
            while (it.hasNext()) {
                if (isPathPrefixMatch(it.next(), (String[]) path.toArray(new String[path.size()]))) {
                    return true;
                }
            }
            return false;
        }

        private boolean isPathPrefixMatch(String[] prefix, String[] path) {
            int prefixSize = prefix.length;
            if (path.length < prefixSize) {
                return false;
            }
            for (int i = 0; i < prefixSize; i++) {
                if (!Objects.equals(path[i], prefix[i])) {
                    return false;
                }
            }
            return true;
        }

        public String getFileName() {
            return null;
        }

        public synchronized void writeTo(XmlSerializer out) throws IOException {
            int N = this.mPaths.size();
            for (int i = 0; i < N; i++) {
                out.startTag(SliceClientPermissions.NAMESPACE, SliceClientPermissions.TAG_PATH);
                out.text(encodeSegments(this.mPaths.valueAt(i)));
                out.endTag(SliceClientPermissions.NAMESPACE, SliceClientPermissions.TAG_PATH);
            }
        }

        public synchronized void readFrom(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.next();
            int depth = parser.getDepth();
            while (parser.getDepth() >= depth) {
                if (parser.getEventType() == 2 && SliceClientPermissions.TAG_PATH.equals(parser.getName())) {
                    this.mPaths.add(decodeSegments(parser.nextText()));
                }
                parser.next();
            }
        }

        private String encodeSegments(String[] s) {
            String[] out = new String[s.length];
            for (int i = 0; i < s.length; i++) {
                out[i] = Uri.encode(s[i]);
            }
            return TextUtils.join(DELIMITER, out);
        }

        private String[] decodeSegments(String s) {
            String[] sets = s.split(DELIMITER, -1);
            for (int i = 0; i < sets.length; i++) {
                sets[i] = Uri.decode(sets[i]);
            }
            return sets;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!getClass().equals(obj != null ? obj.getClass() : null)) {
                return false;
            }
            SliceAuthority other = (SliceAuthority) obj;
            if (this.mPaths.size() != other.mPaths.size()) {
                return false;
            }
            ArrayList<String[]> p1 = new ArrayList<>(this.mPaths);
            ArrayList<String[]> p2 = new ArrayList<>(other.mPaths);
            p1.sort(Comparator.comparing($$Lambda$SliceClientPermissions$SliceAuthority$gfIfSC_15op1dWInvxEKC7DlOkg.INSTANCE));
            p2.sort(Comparator.comparing($$Lambda$SliceClientPermissions$SliceAuthority$czFcrdPWpaFU7_jx7xCl0wMHBps.INSTANCE));
            for (int i = 0; i < p1.size(); i++) {
                String[] a1 = p1.get(i);
                String[] a2 = p2.get(i);
                if (a1.length != a2.length) {
                    return false;
                }
                for (int j = 0; j < a1.length; j++) {
                    if (!Objects.equals(a1[j], a2[j])) {
                        return false;
                    }
                }
            }
            if (Objects.equals(this.mAuthority, other.mAuthority) && Objects.equals(this.mPkg, other.mPkg)) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return String.format("(%s, %s: %s)", new Object[]{this.mAuthority, this.mPkg.toString(), pathToString(this.mPaths)});
        }

        private String pathToString(ArraySet<String[]> paths) {
            return TextUtils.join(", ", (Iterable) paths.stream().map($$Lambda$SliceClientPermissions$SliceAuthority$lvjy01xuWTQLCsbGw02qqI7DYDM.INSTANCE).collect(Collectors.toList()));
        }
    }

    public SliceClientPermissions(SlicePermissionManager.PkgUser pkg, DirtyTracker tracker) {
        this.mPkg = pkg;
        this.mTracker = tracker;
    }

    public SlicePermissionManager.PkgUser getPkg() {
        return this.mPkg;
    }

    public synchronized Collection<SliceAuthority> getAuthorities() {
        return new ArrayList(this.mAuths.values());
    }

    public synchronized SliceAuthority getOrCreateAuthority(SlicePermissionManager.PkgUser authority, SlicePermissionManager.PkgUser provider) {
        SliceAuthority ret;
        ret = this.mAuths.get(authority);
        if (ret == null) {
            ret = new SliceAuthority(authority.getPkg(), provider, this);
            this.mAuths.put(authority, ret);
            onPersistableDirty(ret);
        }
        return ret;
    }

    public synchronized SliceAuthority getAuthority(SlicePermissionManager.PkgUser authority) {
        return this.mAuths.get(authority);
    }

    public boolean hasFullAccess() {
        return this.mHasFullAccess;
    }

    public void setHasFullAccess(boolean hasFullAccess) {
        if (this.mHasFullAccess != hasFullAccess) {
            this.mHasFullAccess = hasFullAccess;
            this.mTracker.onPersistableDirty(this);
        }
    }

    public void removeAuthority(String authority, int userId) {
        if (this.mAuths.remove(new SlicePermissionManager.PkgUser(authority, userId)) != null) {
            this.mTracker.onPersistableDirty(this);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        return r1;
     */
    public synchronized boolean hasPermission(Uri uri, int userId) {
        boolean z = false;
        if (!Objects.equals("content", uri.getScheme())) {
            return false;
        }
        SliceAuthority authority = getAuthority(new SlicePermissionManager.PkgUser(uri.getAuthority(), userId));
        if (authority != null && authority.hasPermission(uri.getPathSegments())) {
            z = true;
        }
    }

    public void grantUri(Uri uri, SlicePermissionManager.PkgUser providerPkg) {
        getOrCreateAuthority(new SlicePermissionManager.PkgUser(uri.getAuthority(), providerPkg.getUserId()), providerPkg).addPath(uri.getPathSegments());
    }

    public void revokeUri(Uri uri, SlicePermissionManager.PkgUser providerPkg) {
        getOrCreateAuthority(new SlicePermissionManager.PkgUser(uri.getAuthority(), providerPkg.getUserId()), providerPkg).removePath(uri.getPathSegments());
    }

    public void clear() {
        if (this.mHasFullAccess || !this.mAuths.isEmpty()) {
            this.mHasFullAccess = false;
            this.mAuths.clear();
            onPersistableDirty(this);
        }
    }

    public void onPersistableDirty(DirtyTracker.Persistable obj) {
        this.mTracker.onPersistableDirty(this);
    }

    public String getFileName() {
        return getFileName(this.mPkg);
    }

    public synchronized void writeTo(XmlSerializer out) throws IOException {
        out.startTag(NAMESPACE, TAG_CLIENT);
        out.attribute(NAMESPACE, "pkg", this.mPkg.toString());
        out.attribute(NAMESPACE, ATTR_FULL_ACCESS, this.mHasFullAccess ? "1" : "0");
        int N = this.mAuths.size();
        for (int i = 0; i < N; i++) {
            out.startTag(NAMESPACE, "authority");
            out.attribute(NAMESPACE, "authority", this.mAuths.valueAt(i).mAuthority);
            out.attribute(NAMESPACE, "pkg", this.mAuths.valueAt(i).mPkg.toString());
            this.mAuths.valueAt(i).writeTo(out);
            out.endTag(NAMESPACE, "authority");
        }
        out.endTag(NAMESPACE, TAG_CLIENT);
    }

    public static SliceClientPermissions createFrom(XmlPullParser parser, DirtyTracker tracker) throws XmlPullParserException, IOException {
        while (true) {
            if (parser.getEventType() == 2 && TAG_CLIENT.equals(parser.getName())) {
                break;
            }
            parser.next();
        }
        int depth = parser.getDepth();
        SliceClientPermissions provider = new SliceClientPermissions(new SlicePermissionManager.PkgUser(parser.getAttributeValue(NAMESPACE, "pkg")), tracker);
        String fullAccess = parser.getAttributeValue(NAMESPACE, ATTR_FULL_ACCESS);
        if (fullAccess == null) {
            fullAccess = "0";
        }
        provider.mHasFullAccess = Integer.parseInt(fullAccess) != 0;
        parser.next();
        while (parser.getDepth() > depth) {
            if (parser.getEventType() == 2 && "authority".equals(parser.getName())) {
                try {
                    SlicePermissionManager.PkgUser pkg = new SlicePermissionManager.PkgUser(parser.getAttributeValue(NAMESPACE, "pkg"));
                    SliceAuthority authority = new SliceAuthority(parser.getAttributeValue(NAMESPACE, "authority"), pkg, provider);
                    authority.readFrom(parser);
                    provider.mAuths.put(new SlicePermissionManager.PkgUser(authority.getAuthority(), pkg.getUserId()), authority);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Couldn't read PkgUser", e);
                }
            }
            parser.next();
        }
        return provider;
    }

    public static String getFileName(SlicePermissionManager.PkgUser pkg) {
        return String.format("client_%s", new Object[]{pkg.toString()});
    }
}
