package com.android.server.slice;

import android.content.ContentProvider;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.XmlUtils;
import com.android.server.slice.DirtyTracker;
import com.android.server.slice.SliceProviderPermissions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class SlicePermissionManager implements DirtyTracker {
    static final int DB_VERSION = 2;
    private static final long PERMISSION_CACHE_PERIOD = 300000;
    private static final String SLICE_DIR = "slice";
    private static final String TAG = "SlicePermissionManager";
    private static final String TAG_LIST = "slice-access-list";
    private static final long WRITE_GRACE_PERIOD = 500;
    private final String ATT_VERSION;
    private final ArrayMap<PkgUser, SliceClientPermissions> mCachedClients;
    private final ArrayMap<PkgUser, SliceProviderPermissions> mCachedProviders;
    private final Context mContext;
    private final ArraySet<DirtyTracker.Persistable> mDirty;
    private final Handler mHandler;
    private final File mSliceDir;

    @VisibleForTesting
    SlicePermissionManager(Context context, Looper looper, File sliceDir) {
        this.ATT_VERSION = "version";
        this.mCachedProviders = new ArrayMap<>();
        this.mCachedClients = new ArrayMap<>();
        this.mDirty = new ArraySet<>();
        this.mContext = context;
        this.mHandler = new H(looper);
        this.mSliceDir = sliceDir;
    }

    public SlicePermissionManager(Context context, Looper looper) {
        this(context, looper, new File(Environment.getDataDirectory(), "system/slice"));
    }

    public void grantFullAccess(String pkg, int userId) {
        getClient(new PkgUser(pkg, userId)).setHasFullAccess(true);
    }

    public void grantSliceAccess(String pkg, int userId, String providerPkg, int providerUser, Uri uri) {
        PkgUser pkgUser = new PkgUser(pkg, userId);
        PkgUser providerPkgUser = new PkgUser(providerPkg, providerUser);
        getClient(pkgUser).grantUri(uri, providerPkgUser);
        getProvider(providerPkgUser).getOrCreateAuthority(ContentProvider.getUriWithoutUserId(uri).getAuthority()).addPkg(pkgUser);
    }

    public void revokeSliceAccess(String pkg, int userId, String providerPkg, int providerUser, Uri uri) {
        PkgUser pkgUser = new PkgUser(pkg, userId);
        getClient(pkgUser).revokeUri(uri, new PkgUser(providerPkg, providerUser));
    }

    public void removePkg(String pkg, int userId) {
        PkgUser pkgUser = new PkgUser(pkg, userId);
        for (SliceProviderPermissions.SliceAuthority authority : getProvider(pkgUser).getAuthorities()) {
            for (PkgUser p : authority.getPkgs()) {
                getClient(p).removeAuthority(authority.getAuthority(), userId);
            }
        }
        getClient(pkgUser).clear();
        this.mHandler.obtainMessage(3, pkgUser);
    }

    public String[] getAllPackagesGranted(String pkg) {
        ArraySet<String> ret = new ArraySet<>();
        for (SliceProviderPermissions.SliceAuthority authority : getProvider(new PkgUser(pkg, 0)).getAuthorities()) {
            for (PkgUser pkgUser : authority.getPkgs()) {
                ret.add(pkgUser.mPkg);
            }
        }
        return (String[]) ret.toArray(new String[ret.size()]);
    }

    public boolean hasFullAccess(String pkg, int userId) {
        return getClient(new PkgUser(pkg, userId)).hasFullAccess();
    }

    public boolean hasPermission(String pkg, int userId, Uri uri) {
        SliceClientPermissions client = getClient(new PkgUser(pkg, userId));
        return client.hasFullAccess() || client.hasPermission(ContentProvider.getUriWithoutUserId(uri), ContentProvider.getUserIdFromUri(uri, userId));
    }

    @Override // com.android.server.slice.DirtyTracker
    public void onPersistableDirty(DirtyTracker.Persistable obj) {
        this.mHandler.removeMessages(2);
        this.mHandler.obtainMessage(1, obj).sendToTarget();
        this.mHandler.sendEmptyMessageDelayed(2, 500);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a6, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a7, code lost:
        if (r7 != null) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a9, code lost:
        $closeResource(r1, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ac, code lost:
        throw r2;
     */
    public void writeBackup(XmlSerializer out) throws IOException, XmlPullParserException {
        synchronized (this) {
            out.startTag(null, TAG_LIST);
            out.attribute(null, "version", String.valueOf(2));
            DirtyTracker tracker = $$Lambda$SlicePermissionManager$y3Tun5dTftw8s8sky62syeWR34U.INSTANCE;
            if (this.mHandler.hasMessages(2)) {
                this.mHandler.removeMessages(2);
                handlePersist();
            }
            String[] list = new File(this.mSliceDir.getAbsolutePath()).list();
            for (String file : list) {
                ParserHolder parser = getParser(file);
                DirtyTracker.Persistable p = null;
                while (true) {
                    if (parser.parser.getEventType() == 1) {
                        break;
                    } else if (parser.parser.getEventType() != 2) {
                        parser.parser.next();
                    } else if ("client".equals(parser.parser.getName())) {
                        p = SliceClientPermissions.createFrom(parser.parser, tracker);
                    } else {
                        p = SliceProviderPermissions.createFrom(parser.parser, tracker);
                    }
                }
                if (p != null) {
                    p.writeTo(out);
                } else {
                    Slog.w(TAG, "Invalid or empty slice permissions file: " + file);
                }
                if (parser != null) {
                    $closeResource(null, parser);
                }
            }
            out.endTag(null, TAG_LIST);
        }
    }

    static /* synthetic */ void lambda$writeBackup$0(DirtyTracker.Persistable obj) {
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public void readRestore(XmlPullParser parser) throws IOException, XmlPullParserException {
        synchronized (this) {
            while (true) {
                if ((parser.getEventType() != 2 || !TAG_LIST.equals(parser.getName())) && parser.getEventType() != 1) {
                    parser.next();
                }
            }
            if (XmlUtils.readIntAttribute(parser, "version", 0) >= 2) {
                while (parser.getEventType() != 1) {
                    if (parser.getEventType() != 2) {
                        parser.next();
                    } else if ("client".equals(parser.getName())) {
                        SliceClientPermissions client = SliceClientPermissions.createFrom(parser, this);
                        synchronized (this.mCachedClients) {
                            this.mCachedClients.put(client.getPkg(), client);
                        }
                        onPersistableDirty(client);
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, client.getPkg()), 300000);
                    } else if ("provider".equals(parser.getName())) {
                        SliceProviderPermissions provider = SliceProviderPermissions.createFrom(parser, this);
                        synchronized (this.mCachedProviders) {
                            this.mCachedProviders.put(provider.getPkg(), provider);
                        }
                        onPersistableDirty(provider);
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, provider.getPkg()), 300000);
                    } else {
                        parser.next();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0045, code lost:
        if (r0 != null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0047, code lost:
        $closeResource(r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004a, code lost:
        throw r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0067 A[SYNTHETIC] */
    private SliceClientPermissions getClient(PkgUser pkgUser) {
        SliceClientPermissions client;
        synchronized (this.mCachedClients) {
            client = this.mCachedClients.get(pkgUser);
        }
        if (client != null) {
            return client;
        }
        try {
            ParserHolder parser = getParser(SliceClientPermissions.getFileName(pkgUser));
            SliceClientPermissions client2 = SliceClientPermissions.createFrom(parser.parser, this);
            synchronized (this.mCachedClients) {
                this.mCachedClients.put(pkgUser, client2);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, pkgUser), 300000);
            if (parser != null) {
                $closeResource(null, parser);
            }
            return client2;
        } catch (FileNotFoundException e) {
            SliceClientPermissions client3 = new SliceClientPermissions(pkgUser, this);
            synchronized (this.mCachedClients) {
                this.mCachedClients.put(pkgUser, client3);
            }
            return client3;
        } catch (IOException e2) {
            Log.e(TAG, "Can't read client", e2);
            SliceClientPermissions client32 = new SliceClientPermissions(pkgUser, this);
            synchronized (this.mCachedClients) {
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "Can't read client", e3);
            SliceClientPermissions client322 = new SliceClientPermissions(pkgUser, this);
            synchronized (this.mCachedClients) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0045, code lost:
        if (r0 != null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0047, code lost:
        $closeResource(r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004a, code lost:
        throw r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0067 A[SYNTHETIC] */
    private SliceProviderPermissions getProvider(PkgUser pkgUser) {
        SliceProviderPermissions provider;
        synchronized (this.mCachedProviders) {
            provider = this.mCachedProviders.get(pkgUser);
        }
        if (provider != null) {
            return provider;
        }
        try {
            ParserHolder parser = getParser(SliceProviderPermissions.getFileName(pkgUser));
            SliceProviderPermissions provider2 = SliceProviderPermissions.createFrom(parser.parser, this);
            synchronized (this.mCachedProviders) {
                this.mCachedProviders.put(pkgUser, provider2);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, pkgUser), 300000);
            if (parser != null) {
                $closeResource(null, parser);
            }
            return provider2;
        } catch (FileNotFoundException e) {
            SliceProviderPermissions provider3 = new SliceProviderPermissions(pkgUser, this);
            synchronized (this.mCachedProviders) {
                this.mCachedProviders.put(pkgUser, provider3);
            }
            return provider3;
        } catch (IOException e2) {
            Log.e(TAG, "Can't read provider", e2);
            SliceProviderPermissions provider32 = new SliceProviderPermissions(pkgUser, this);
            synchronized (this.mCachedProviders) {
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "Can't read provider", e3);
            SliceProviderPermissions provider322 = new SliceProviderPermissions(pkgUser, this);
            synchronized (this.mCachedProviders) {
            }
        }
    }

    private ParserHolder getParser(String fileName) throws FileNotFoundException, XmlPullParserException {
        AtomicFile file = getFile(fileName);
        ParserHolder holder = new ParserHolder();
        holder.input = file.openRead();
        holder.parser = XmlPullParserFactory.newInstance().newPullParser();
        holder.parser.setInput(holder.input, Xml.Encoding.UTF_8.name());
        return holder;
    }

    private AtomicFile getFile(String fileName) {
        if (!this.mSliceDir.exists()) {
            this.mSliceDir.mkdir();
        }
        return new AtomicFile(new File(this.mSliceDir, fileName));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handlePersist() {
        synchronized (this) {
            Iterator<DirtyTracker.Persistable> it = this.mDirty.iterator();
            while (it.hasNext()) {
                DirtyTracker.Persistable persistable = it.next();
                AtomicFile file = getFile(persistable.getFileName());
                try {
                    FileOutputStream stream = file.startWrite();
                    try {
                        XmlSerializer out = XmlPullParserFactory.newInstance().newSerializer();
                        out.setOutput(stream, Xml.Encoding.UTF_8.name());
                        persistable.writeTo(out);
                        out.flush();
                        file.finishWrite(stream);
                    } catch (IOException | RuntimeException | XmlPullParserException e) {
                        Slog.w(TAG, "Failed to save access file, restoring backup", e);
                        file.failWrite(stream);
                    }
                } catch (IOException e2) {
                    Slog.w(TAG, "Failed to save access file", e2);
                    return;
                }
            }
            this.mDirty.clear();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addDirtyImmediate(DirtyTracker.Persistable obj) {
        this.mDirty.add(obj);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemove(PkgUser pkgUser) {
        getFile(SliceClientPermissions.getFileName(pkgUser)).delete();
        getFile(SliceProviderPermissions.getFileName(pkgUser)).delete();
        this.mDirty.remove(this.mCachedClients.remove(pkgUser));
        this.mDirty.remove(this.mCachedProviders.remove(pkgUser));
    }

    private final class H extends Handler {
        private static final int MSG_ADD_DIRTY = 1;
        private static final int MSG_CLEAR_CLIENT = 4;
        private static final int MSG_CLEAR_PROVIDER = 5;
        private static final int MSG_PERSIST = 2;
        private static final int MSG_REMOVE = 3;

        public H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                SlicePermissionManager.this.mDirty.add((DirtyTracker.Persistable) msg.obj);
            } else if (i == 2) {
                SlicePermissionManager.this.handlePersist();
            } else if (i == 3) {
                SlicePermissionManager.this.handleRemove((PkgUser) msg.obj);
            } else if (i == 4) {
                synchronized (SlicePermissionManager.this.mCachedClients) {
                    SlicePermissionManager.this.mCachedClients.remove(msg.obj);
                }
            } else if (i == 5) {
                synchronized (SlicePermissionManager.this.mCachedProviders) {
                    SlicePermissionManager.this.mCachedProviders.remove(msg.obj);
                }
            }
        }
    }

    public static class PkgUser {
        private static final String FORMAT = "%s@%d";
        private static final String SEPARATOR = "@";
        private final String mPkg;
        private final int mUserId;

        public PkgUser(String pkg, int userId) {
            this.mPkg = pkg;
            this.mUserId = userId;
        }

        public PkgUser(String pkgUserStr) throws IllegalArgumentException {
            try {
                String[] vals = pkgUserStr.split(SEPARATOR, 2);
                this.mPkg = vals[0];
                this.mUserId = Integer.parseInt(vals[1]);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        public String getPkg() {
            return this.mPkg;
        }

        public int getUserId() {
            return this.mUserId;
        }

        public int hashCode() {
            return this.mPkg.hashCode() + this.mUserId;
        }

        public boolean equals(Object obj) {
            if (!getClass().equals(obj != null ? obj.getClass() : null)) {
                return false;
            }
            PkgUser other = (PkgUser) obj;
            if (!Objects.equals(other.mPkg, this.mPkg) || other.mUserId != this.mUserId) {
                return false;
            }
            return true;
        }

        public String toString() {
            return String.format(FORMAT, this.mPkg, Integer.valueOf(this.mUserId));
        }
    }

    /* access modifiers changed from: private */
    public class ParserHolder implements AutoCloseable {
        private InputStream input;
        private XmlPullParser parser;

        private ParserHolder() {
        }

        @Override // java.lang.AutoCloseable
        public void close() throws IOException {
            this.input.close();
        }
    }
}
