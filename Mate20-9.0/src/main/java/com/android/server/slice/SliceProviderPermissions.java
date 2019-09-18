package com.android.server.slice;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.AbsLocationManagerService;
import com.android.server.slice.DirtyTracker;
import com.android.server.slice.SlicePermissionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SliceProviderPermissions implements DirtyTracker, DirtyTracker.Persistable {
    private static final String ATTR_AUTHORITY = "authority";
    private static final String ATTR_PKG = "pkg";
    /* access modifiers changed from: private */
    public static final String NAMESPACE = null;
    private static final String TAG = "SliceProviderPermissions";
    private static final String TAG_AUTHORITY = "authority";
    private static final String TAG_PKG = "pkg";
    static final String TAG_PROVIDER = "provider";
    private final ArrayMap<String, SliceAuthority> mAuths = new ArrayMap<>();
    private final SlicePermissionManager.PkgUser mPkg;
    private final DirtyTracker mTracker;

    public static class SliceAuthority implements DirtyTracker.Persistable {
        /* access modifiers changed from: private */
        public final String mAuthority;
        private final ArraySet<SlicePermissionManager.PkgUser> mPkgs = new ArraySet<>();
        private final DirtyTracker mTracker;

        public SliceAuthority(String authority, DirtyTracker tracker) {
            this.mAuthority = authority;
            this.mTracker = tracker;
        }

        public String getAuthority() {
            return this.mAuthority;
        }

        public synchronized void addPkg(SlicePermissionManager.PkgUser pkg) {
            if (this.mPkgs.add(pkg)) {
                this.mTracker.onPersistableDirty(this);
            }
        }

        public synchronized void removePkg(SlicePermissionManager.PkgUser pkg) {
            if (this.mPkgs.remove(pkg)) {
                this.mTracker.onPersistableDirty(this);
            }
        }

        public synchronized Collection<SlicePermissionManager.PkgUser> getPkgs() {
            return new ArraySet(this.mPkgs);
        }

        public String getFileName() {
            return null;
        }

        public synchronized void writeTo(XmlSerializer out) throws IOException {
            int N = this.mPkgs.size();
            for (int i = 0; i < N; i++) {
                out.startTag(SliceProviderPermissions.NAMESPACE, AbsLocationManagerService.DEL_PKG);
                out.text(this.mPkgs.valueAt(i).toString());
                out.endTag(SliceProviderPermissions.NAMESPACE, AbsLocationManagerService.DEL_PKG);
            }
        }

        public synchronized void readFrom(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.next();
            int depth = parser.getDepth();
            while (parser.getDepth() >= depth) {
                if (parser.getEventType() == 2 && AbsLocationManagerService.DEL_PKG.equals(parser.getName())) {
                    this.mPkgs.add(new SlicePermissionManager.PkgUser(parser.nextText()));
                }
                parser.next();
            }
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!getClass().equals(obj != null ? obj.getClass() : null)) {
                return false;
            }
            SliceAuthority other = (SliceAuthority) obj;
            if (Objects.equals(this.mAuthority, other.mAuthority) && Objects.equals(this.mPkgs, other.mPkgs)) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return String.format("(%s: %s)", new Object[]{this.mAuthority, this.mPkgs.toString()});
        }
    }

    public SliceProviderPermissions(SlicePermissionManager.PkgUser pkg, DirtyTracker tracker) {
        this.mPkg = pkg;
        this.mTracker = tracker;
    }

    public SlicePermissionManager.PkgUser getPkg() {
        return this.mPkg;
    }

    public synchronized Collection<SliceAuthority> getAuthorities() {
        return new ArrayList(this.mAuths.values());
    }

    public synchronized SliceAuthority getOrCreateAuthority(String authority) {
        SliceAuthority ret;
        ret = this.mAuths.get(authority);
        if (ret == null) {
            ret = new SliceAuthority(authority, this);
            this.mAuths.put(authority, ret);
            onPersistableDirty(ret);
        }
        return ret;
    }

    public void onPersistableDirty(DirtyTracker.Persistable obj) {
        this.mTracker.onPersistableDirty(this);
    }

    public String getFileName() {
        return getFileName(this.mPkg);
    }

    public synchronized void writeTo(XmlSerializer out) throws IOException {
        out.startTag(NAMESPACE, TAG_PROVIDER);
        out.attribute(NAMESPACE, AbsLocationManagerService.DEL_PKG, this.mPkg.toString());
        int N = this.mAuths.size();
        for (int i = 0; i < N; i++) {
            out.startTag(NAMESPACE, "authority");
            out.attribute(NAMESPACE, "authority", this.mAuths.valueAt(i).mAuthority);
            this.mAuths.valueAt(i).writeTo(out);
            out.endTag(NAMESPACE, "authority");
        }
        out.endTag(NAMESPACE, TAG_PROVIDER);
    }

    public static SliceProviderPermissions createFrom(XmlPullParser parser, DirtyTracker tracker) throws XmlPullParserException, IOException {
        while (true) {
            if (parser.getEventType() == 2 && TAG_PROVIDER.equals(parser.getName())) {
                break;
            }
            parser.next();
        }
        int depth = parser.getDepth();
        SliceProviderPermissions provider = new SliceProviderPermissions(new SlicePermissionManager.PkgUser(parser.getAttributeValue(NAMESPACE, AbsLocationManagerService.DEL_PKG)), tracker);
        parser.next();
        while (parser.getDepth() > depth) {
            if (parser.getEventType() == 2 && "authority".equals(parser.getName())) {
                try {
                    SliceAuthority authority = new SliceAuthority(parser.getAttributeValue(NAMESPACE, "authority"), provider);
                    authority.readFrom(parser);
                    provider.mAuths.put(authority.getAuthority(), authority);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Couldn't read PkgUser", e);
                }
            }
            parser.next();
        }
        return provider;
    }

    public static String getFileName(SlicePermissionManager.PkgUser pkg) {
        return String.format("provider_%s", new Object[]{pkg.toString()});
    }
}
