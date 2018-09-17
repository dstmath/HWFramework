package android.content;

import android.accounts.Account;
import android.annotation.RequiresPermission.Read;
import android.annotation.RequiresPermission.Write;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.DownloadManager;
import android.app.backup.FullBackup;
import android.content.ISyncStatusObserver.Stub;
import android.content.SyncRequest.Builder;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.CrossProcessCursorWrapper;
import android.database.Cursor;
import android.database.IContentObserver;
import android.graphics.drawable.Drawable;
import android.hsm.HwSystemManager;
import android.media.midi.MidiDeviceInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.DeadObjectException;
import android.os.ICancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.MimeIconUtils;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ContentResolver {
    public static final Intent ACTION_SYNC_CONN_STATUS_CHANGED = new Intent("com.android.sync.SYNC_CONN_STATUS_CHANGED");
    public static final String ANY_CURSOR_ITEM_TYPE = "vnd.android.cursor.item/*";
    public static final String CONTENT_SERVICE_NAME = "content";
    public static final String CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir";
    public static final String CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item";
    private static final boolean ENABLE_CONTENT_SAMPLE = false;
    public static final String EXTRA_HONORED_ARGS = "android.content.extra.HONORED_ARGS";
    public static final String EXTRA_REFRESH_SUPPORTED = "android.content.extra.REFRESH_SUPPORTED";
    public static final String EXTRA_SIZE = "android.content.extra.SIZE";
    public static final String EXTRA_TOTAL_COUNT = "android.content.extra.TOTAL_COUNT";
    public static final int NOTIFY_SKIP_NOTIFY_FOR_DESCENDANTS = 2;
    public static final int NOTIFY_SYNC_TO_NETWORK = 1;
    public static final String QUERY_ARG_LIMIT = "android:query-arg-limit";
    public static final String QUERY_ARG_OFFSET = "android:query-arg-offset";
    public static final String QUERY_ARG_SORT_COLLATION = "android:query-arg-sort-collation";
    public static final String QUERY_ARG_SORT_COLUMNS = "android:query-arg-sort-columns";
    public static final String QUERY_ARG_SORT_DIRECTION = "android:query-arg-sort-direction";
    public static final String QUERY_ARG_SQL_SELECTION = "android:query-arg-sql-selection";
    public static final String QUERY_ARG_SQL_SELECTION_ARGS = "android:query-arg-sql-selection-args";
    public static final String QUERY_ARG_SQL_SORT_ORDER = "android:query-arg-sql-sort-order";
    public static final int QUERY_SORT_DIRECTION_ASCENDING = 0;
    public static final int QUERY_SORT_DIRECTION_DESCENDING = 1;
    public static final String SCHEME_ANDROID_RESOURCE = "android.resource";
    public static final String SCHEME_CONTENT = "content";
    public static final String SCHEME_FILE = "file";
    private static final int SLOW_THRESHOLD_MILLIS = 500;
    public static final int SYNC_ERROR_AUTHENTICATION = 2;
    public static final int SYNC_ERROR_CONFLICT = 5;
    public static final int SYNC_ERROR_INTERNAL = 8;
    public static final int SYNC_ERROR_IO = 3;
    private static final String[] SYNC_ERROR_NAMES = new String[]{"already-in-progress", "authentication-error", "io-error", "parse-error", "conflict", "too-many-deletions", "too-many-retries", "internal-error"};
    public static final int SYNC_ERROR_PARSE = 4;
    public static final int SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS = 1;
    public static final int SYNC_ERROR_TOO_MANY_DELETIONS = 6;
    public static final int SYNC_ERROR_TOO_MANY_RETRIES = 7;
    @Deprecated
    public static final String SYNC_EXTRAS_ACCOUNT = "account";
    public static final String SYNC_EXTRAS_DISALLOW_METERED = "allow_metered";
    public static final String SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS = "discard_deletions";
    public static final String SYNC_EXTRAS_DO_NOT_RETRY = "do_not_retry";
    public static final String SYNC_EXTRAS_EXPECTED_DOWNLOAD = "expected_download";
    public static final String SYNC_EXTRAS_EXPECTED_UPLOAD = "expected_upload";
    public static final String SYNC_EXTRAS_EXPEDITED = "expedited";
    @Deprecated
    public static final String SYNC_EXTRAS_FORCE = "force";
    public static final String SYNC_EXTRAS_IGNORE_BACKOFF = "ignore_backoff";
    public static final String SYNC_EXTRAS_IGNORE_SETTINGS = "ignore_settings";
    public static final String SYNC_EXTRAS_INITIALIZE = "initialize";
    public static final String SYNC_EXTRAS_MANUAL = "force";
    public static final String SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS = "deletions_override";
    public static final String SYNC_EXTRAS_PRIORITY = "sync_priority";
    public static final String SYNC_EXTRAS_REQUIRE_CHARGING = "require_charging";
    public static final String SYNC_EXTRAS_UPLOAD = "upload";
    public static final int SYNC_OBSERVER_TYPE_ACTIVE = 4;
    public static final int SYNC_OBSERVER_TYPE_ALL = Integer.MAX_VALUE;
    public static final int SYNC_OBSERVER_TYPE_PENDING = 2;
    public static final int SYNC_OBSERVER_TYPE_SETTINGS = 1;
    public static final int SYNC_OBSERVER_TYPE_STATUS = 8;
    private static final String TAG = "ContentResolver";
    private static IContentService sContentService;
    private final Context mContext;
    final String mPackageName;
    private final Random mRandom = new Random();
    final int mTargetSdkVersion;

    private final class CursorWrapperInner extends CrossProcessCursorWrapper {
        private final CloseGuard mCloseGuard = CloseGuard.get();
        private final IContentProvider mContentProvider;
        private final AtomicBoolean mProviderReleased = new AtomicBoolean();

        CursorWrapperInner(Cursor cursor, IContentProvider contentProvider) {
            super(cursor);
            this.mContentProvider = contentProvider;
            this.mCloseGuard.open("close");
        }

        public void close() {
            this.mCloseGuard.close();
            super.close();
            if (this.mProviderReleased.compareAndSet(false, true)) {
                ContentResolver.this.releaseProvider(this.mContentProvider);
            }
        }

        protected void finalize() throws Throwable {
            try {
                this.mCloseGuard.warnIfOpen();
                close();
            } finally {
                super.finalize();
            }
        }
    }

    public class OpenResourceIdResult {
        public int id;
        public Resources r;
    }

    private final class ParcelFileDescriptorInner extends ParcelFileDescriptor {
        private final IContentProvider mContentProvider;
        private final AtomicBoolean mProviderReleased = new AtomicBoolean();

        ParcelFileDescriptorInner(ParcelFileDescriptor pfd, IContentProvider icp) {
            super(pfd);
            this.mContentProvider = icp;
        }

        public void releaseResources() {
            if (this.mProviderReleased.compareAndSet(false, true)) {
                ContentResolver.this.releaseProvider(this.mContentProvider);
            }
        }
    }

    protected abstract IContentProvider acquireProvider(Context context, String str);

    protected abstract IContentProvider acquireUnstableProvider(Context context, String str);

    public abstract boolean releaseProvider(IContentProvider iContentProvider);

    public abstract boolean releaseUnstableProvider(IContentProvider iContentProvider);

    public abstract void unstableProviderDied(IContentProvider iContentProvider);

    public static String syncErrorToString(int error) {
        if (error < 1 || error > SYNC_ERROR_NAMES.length) {
            return String.valueOf(error);
        }
        return SYNC_ERROR_NAMES[error - 1];
    }

    public static int syncErrorStringToInt(String error) {
        int n = SYNC_ERROR_NAMES.length;
        for (int i = 0; i < n; i++) {
            if (SYNC_ERROR_NAMES[i].equals(error)) {
                return i + 1;
            }
        }
        if (error != null) {
            try {
                return Integer.parseInt(error);
            } catch (NumberFormatException e) {
                Log.d(TAG, "error parsing sync error: " + error);
            }
        }
        return 0;
    }

    public ContentResolver(Context context) {
        if (context == null) {
            context = ActivityThread.currentApplication();
        }
        this.mContext = context;
        this.mPackageName = this.mContext.getOpPackageName();
        this.mTargetSdkVersion = this.mContext.getApplicationInfo().targetSdkVersion;
    }

    protected IContentProvider acquireExistingProvider(Context c, String name) {
        return acquireProvider(c, name);
    }

    public void appNotRespondingViaProvider(IContentProvider icp) {
        throw new UnsupportedOperationException("appNotRespondingViaProvider");
    }

    public final String getType(Uri url) {
        String str = "url";
        Preconditions.checkNotNull(url, str);
        IContentProvider provider = acquireExistingProvider(url);
        if (provider != null) {
            try {
                str = provider.getType(url);
                return str;
            } catch (RemoteException e) {
                return null;
            } catch (Exception e2) {
                str = TAG;
                Log.w(str, "Failed to get type for: " + url + " (" + e2.getMessage() + ")");
                return null;
            } finally {
                releaseProvider(provider);
            }
        } else if (!"content".equals(url.getScheme())) {
            return null;
        } else {
            try {
                return ActivityManager.getService().getProviderMimeType(ContentProvider.getUriWithoutUserId(url), resolveUserId(url));
            } catch (RemoteException e3) {
                return null;
            } catch (Exception e22) {
                Log.w(TAG, "Failed to get type for: " + url + " (" + e22.getMessage() + ")");
                return null;
            }
        }
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) {
        Preconditions.checkNotNull(url, "url");
        String[] strArr = "mimeTypeFilter";
        Preconditions.checkNotNull(mimeTypeFilter, strArr);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            strArr = provider.getStreamTypes(url, mimeTypeFilter);
            return strArr;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Cursor query(@Read Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(uri, projection, selection, selectionArgs, sortOrder, null);
    }

    public final Cursor query(@Read Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        if (HwSystemManager.allowOp(uri, 1)) {
            return query(uri, projection, createSqlQueryBundle(selection, selectionArgs, sortOrder), cancellationSignal);
        }
        return HwSystemManager.getDummyCursor(this, uri, projection, selection, selectionArgs, sortOrder);
    }

    public final Cursor query(@Read Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        IContentProvider unstableProvider = acquireUnstableProvider(uri);
        if (unstableProvider == null) {
            return null;
        }
        IContentProvider stableProvider = null;
        Cursor qCursor = null;
        try {
            long startTime = SystemClock.uptimeMillis();
            ICancellationSignal remoteCancellationSignal = null;
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
                remoteCancellationSignal = unstableProvider.createCancellationSignal();
                cancellationSignal.setRemote(remoteCancellationSignal);
            }
            try {
                qCursor = unstableProvider.query(this.mPackageName, uri, projection, queryArgs, remoteCancellationSignal);
            } catch (DeadObjectException e) {
                unstableProviderDied(unstableProvider);
                stableProvider = acquireProvider(uri);
                if (stableProvider == null) {
                    if (cancellationSignal != null) {
                        cancellationSignal.setRemote(null);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                    if (stableProvider != null) {
                        releaseProvider(stableProvider);
                    }
                    return null;
                }
                qCursor = stableProvider.query(this.mPackageName, uri, projection, queryArgs, remoteCancellationSignal);
            }
            if (qCursor == null) {
                if (qCursor != null) {
                    qCursor.close();
                }
                if (cancellationSignal != null) {
                    cancellationSignal.setRemote(null);
                }
                if (unstableProvider != null) {
                    releaseUnstableProvider(unstableProvider);
                }
                if (stableProvider != null) {
                    releaseProvider(stableProvider);
                }
                return null;
            }
            IContentProvider provider;
            qCursor.getCount();
            maybeLogQueryToEventLog(SystemClock.uptimeMillis() - startTime, uri, projection, queryArgs);
            if (stableProvider != null) {
                provider = stableProvider;
            } else {
                provider = acquireProvider(uri);
            }
            CursorWrapperInner cursorWrapperInner = new CursorWrapperInner(qCursor, provider);
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            return cursorWrapperInner;
        } catch (RemoteException e2) {
            if (qCursor != null) {
                qCursor.close();
            }
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            if (stableProvider != null) {
                releaseProvider(stableProvider);
            }
            return null;
        } catch (Throwable th) {
            if (qCursor != null) {
                qCursor.close();
            }
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            if (stableProvider != null) {
                releaseProvider(stableProvider);
            }
            throw th;
        }
    }

    public final Uri canonicalize(Uri url) {
        Uri uri = "url";
        Preconditions.checkNotNull(url, uri);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            uri = provider.canonicalize(this.mPackageName, url);
            return uri;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Uri uncanonicalize(Uri url) {
        Uri uri = "url";
        Preconditions.checkNotNull(url, uri);
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            uri = provider.uncanonicalize(this.mPackageName, url);
            return uri;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final boolean refresh(Uri url, Bundle args, CancellationSignal cancellationSignal) {
        Preconditions.checkNotNull(url, "url");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return false;
        }
        ICancellationSignal remoteCancellationSignal = null;
        if (cancellationSignal != null) {
            try {
                cancellationSignal.throwIfCanceled();
                remoteCancellationSignal = provider.createCancellationSignal();
                cancellationSignal.setRemote(remoteCancellationSignal);
            } catch (RemoteException e) {
                releaseProvider(provider);
                return false;
            } catch (Throwable th) {
                releaseProvider(provider);
            }
        }
        boolean refresh = provider.refresh(this.mPackageName, url, args, remoteCancellationSignal);
        releaseProvider(provider);
        return refresh;
    }

    public final InputStream openInputStream(Uri uri) throws FileNotFoundException {
        InputStream inputStream = null;
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            OpenResourceIdResult r = getResourceId(uri);
            try {
                return r.r.openRawResource(r.id);
            } catch (NotFoundException e) {
                throw new FileNotFoundException("Resource does not exist: " + uri);
            }
        } else if (SCHEME_FILE.equals(scheme)) {
            return new FileInputStream(uri.getPath());
        } else {
            AssetFileDescriptor fd = openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN, null);
            if (fd != null) {
                try {
                    inputStream = fd.createInputStream();
                } catch (IOException e2) {
                    throw new FileNotFoundException("Unable to create stream");
                }
            }
            return inputStream;
        }
    }

    public final OutputStream openOutputStream(Uri uri) throws FileNotFoundException {
        return openOutputStream(uri, "w");
    }

    public final OutputStream openOutputStream(Uri uri, String mode) throws FileNotFoundException {
        AssetFileDescriptor fd = openAssetFileDescriptor(uri, mode, null);
        if (fd == null) {
            return null;
        }
        try {
            return fd.createOutputStream();
        } catch (IOException e) {
            throw new FileNotFoundException("Unable to create stream");
        }
    }

    public final ParcelFileDescriptor openFileDescriptor(Uri uri, String mode) throws FileNotFoundException {
        return openFileDescriptor(uri, mode, null);
    }

    public final ParcelFileDescriptor openFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal) throws FileNotFoundException {
        AssetFileDescriptor afd = openAssetFileDescriptor(uri, mode, cancellationSignal);
        if (afd == null) {
            return null;
        }
        if (afd.getDeclaredLength() < 0) {
            return afd.getParcelFileDescriptor();
        }
        try {
            afd.close();
        } catch (IOException e) {
        }
        throw new FileNotFoundException("Not a whole file");
    }

    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode) throws FileNotFoundException {
        return openAssetFileDescriptor(uri, mode, null);
    }

    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal) throws FileNotFoundException {
        AssetFileDescriptor fd;
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(mode, PowerManager.EXTRA_POWER_SAVE_MODE);
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            if (FullBackup.ROOT_TREE_TOKEN.equals(mode)) {
                OpenResourceIdResult r = getResourceId(uri);
                try {
                    return r.r.openRawResourceFd(r.id);
                } catch (NotFoundException e) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            }
            throw new FileNotFoundException("Can't write resources: " + uri);
        } else if (SCHEME_FILE.equals(scheme)) {
            return new AssetFileDescriptor(ParcelFileDescriptor.open(new File(uri.getPath()), ParcelFileDescriptor.parseMode(mode)), 0, -1);
        } else {
            if (FullBackup.ROOT_TREE_TOKEN.equals(mode)) {
                return openTypedAssetFileDescriptor(uri, "*/*", null, cancellationSignal);
            }
            IContentProvider unstableProvider = acquireUnstableProvider(uri);
            if (unstableProvider == null) {
                throw new FileNotFoundException("No content provider: " + uri);
            }
            IContentProvider iContentProvider = null;
            ICancellationSignal remoteCancellationSignal = null;
            if (cancellationSignal != null) {
                try {
                    cancellationSignal.throwIfCanceled();
                    remoteCancellationSignal = unstableProvider.createCancellationSignal();
                    cancellationSignal.setRemote(remoteCancellationSignal);
                } catch (DeadObjectException e2) {
                    unstableProviderDied(unstableProvider);
                    iContentProvider = acquireProvider(uri);
                    if (iContentProvider == null) {
                        throw new FileNotFoundException("No content provider: " + uri);
                    }
                    fd = iContentProvider.openAssetFile(this.mPackageName, uri, mode, remoteCancellationSignal);
                    if (fd == null) {
                        if (cancellationSignal != null) {
                            cancellationSignal.setRemote(null);
                        }
                        if (iContentProvider != null) {
                            releaseProvider(iContentProvider);
                        }
                        if (unstableProvider != null) {
                            releaseUnstableProvider(unstableProvider);
                        }
                        return null;
                    }
                } catch (RemoteException e3) {
                    try {
                        throw new FileNotFoundException("Failed opening content provider: " + uri);
                    } catch (Throwable th) {
                        if (cancellationSignal != null) {
                            cancellationSignal.setRemote(null);
                        }
                        if (iContentProvider != null) {
                            releaseProvider(iContentProvider);
                        }
                        if (unstableProvider != null) {
                            releaseUnstableProvider(unstableProvider);
                        }
                    }
                } catch (FileNotFoundException e4) {
                    throw e4;
                }
            }
            fd = unstableProvider.openAssetFile(this.mPackageName, uri, mode, remoteCancellationSignal);
            if (fd == null) {
                if (cancellationSignal != null) {
                    cancellationSignal.setRemote(null);
                }
                if (unstableProvider != null) {
                    releaseUnstableProvider(unstableProvider);
                }
                return null;
            }
            if (iContentProvider == null) {
                iContentProvider = acquireProvider(uri);
            }
            releaseUnstableProvider(unstableProvider);
            AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(new ParcelFileDescriptorInner(fd.getParcelFileDescriptor(), iContentProvider), fd.getStartOffset(), fd.getDeclaredLength());
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            return assetFileDescriptor;
        }
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) throws FileNotFoundException {
        return openTypedAssetFileDescriptor(uri, mimeType, opts, null);
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts, CancellationSignal cancellationSignal) throws FileNotFoundException {
        AssetFileDescriptor fd;
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(mimeType, "mimeType");
        IContentProvider unstableProvider = acquireUnstableProvider(uri);
        if (unstableProvider == null) {
            throw new FileNotFoundException("No content provider: " + uri);
        }
        IContentProvider iContentProvider = null;
        ICancellationSignal remoteCancellationSignal = null;
        if (cancellationSignal != null) {
            try {
                cancellationSignal.throwIfCanceled();
                remoteCancellationSignal = unstableProvider.createCancellationSignal();
                cancellationSignal.setRemote(remoteCancellationSignal);
            } catch (DeadObjectException e) {
                unstableProviderDied(unstableProvider);
                iContentProvider = acquireProvider(uri);
                if (iContentProvider == null) {
                    throw new FileNotFoundException("No content provider: " + uri);
                }
                fd = iContentProvider.openTypedAssetFile(this.mPackageName, uri, mimeType, opts, remoteCancellationSignal);
                if (fd == null) {
                    if (cancellationSignal != null) {
                        cancellationSignal.setRemote(null);
                    }
                    if (iContentProvider != null) {
                        releaseProvider(iContentProvider);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                    return null;
                }
            } catch (RemoteException e2) {
                try {
                    throw new FileNotFoundException("Failed opening content provider: " + uri);
                } catch (Throwable th) {
                    if (cancellationSignal != null) {
                        cancellationSignal.setRemote(null);
                    }
                    if (iContentProvider != null) {
                        releaseProvider(iContentProvider);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                }
            } catch (FileNotFoundException e3) {
                throw e3;
            }
        }
        fd = unstableProvider.openTypedAssetFile(this.mPackageName, uri, mimeType, opts, remoteCancellationSignal);
        if (fd == null) {
            if (cancellationSignal != null) {
                cancellationSignal.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            return null;
        }
        if (iContentProvider == null) {
            iContentProvider = acquireProvider(uri);
        }
        releaseUnstableProvider(unstableProvider);
        AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(new ParcelFileDescriptorInner(fd.getParcelFileDescriptor(), iContentProvider), fd.getStartOffset(), fd.getDeclaredLength());
        if (cancellationSignal != null) {
            cancellationSignal.setRemote(null);
        }
        return assetFileDescriptor;
    }

    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        }
        try {
            Resources r = this.mContext.getPackageManager().getResourcesForApplication(authority);
            List<String> path = uri.getPathSegments();
            if (path == null) {
                throw new FileNotFoundException("No path: " + uri);
            }
            int id;
            int len = path.size();
            if (len == 1) {
                try {
                    id = Integer.parseInt((String) path.get(0));
                } catch (NumberFormatException e) {
                    throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
                }
            } else if (len == 2) {
                id = r.getIdentifier((String) path.get(1), (String) path.get(0), authority);
            } else {
                throw new FileNotFoundException("More than two path segments: " + uri);
            }
            if (id == 0) {
                throw new FileNotFoundException("No resource found for: " + uri);
            }
            OpenResourceIdResult res = new OpenResourceIdResult();
            res.r = r;
            res.id = id;
            return res;
        } catch (NameNotFoundException e2) {
            throw new FileNotFoundException("No package found for authority: " + uri);
        }
    }

    public final Uri insert(@Write Uri url, ContentValues values) {
        Preconditions.checkNotNull(url, "url");
        if (!HwSystemManager.allowOp(url, 2)) {
            return null;
        }
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + url);
        }
        try {
            long startTime = SystemClock.uptimeMillis();
            Uri createdRow = provider.insert(this.mPackageName, url, values);
            maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "insert", null);
            return createdRow;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Preconditions.checkNotNull(authority, "authority");
        Preconditions.checkNotNull(operations, "operations");
        ContentProviderClient provider = acquireContentProviderClient(authority);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown authority " + authority);
        }
        try {
            ContentProviderResult[] applyBatch = provider.applyBatch(HwSystemManager.getAllowedApplyBatchOp(authority, operations));
            return applyBatch;
        } finally {
            provider.release();
        }
    }

    public final int bulkInsert(@Write Uri url, ContentValues[] values) {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(values, "values");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + url);
        }
        try {
            if (!HwSystemManager.allowOp(url, 2)) {
                return 0;
            }
            long startTime = SystemClock.uptimeMillis();
            int rowsCreated = provider.bulkInsert(this.mPackageName, url, values);
            maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "bulkinsert", null);
            releaseProvider(provider);
            return rowsCreated;
        } catch (RemoteException e) {
            return 0;
        } finally {
            releaseProvider(provider);
        }
    }

    public final int delete(@Write Uri url, String where, String[] selectionArgs) {
        Preconditions.checkNotNull(url, "url");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + url);
        }
        int i;
        try {
            if (HwSystemManager.allowOp(url, 3)) {
                long startTime = SystemClock.uptimeMillis();
                int rowsDeleted = provider.delete(this.mPackageName, url, where, selectionArgs);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "delete", where);
                releaseProvider(provider);
                return rowsDeleted;
            }
            i = 0;
            return i;
        } catch (RemoteException e) {
            i = -1;
            return i;
        } finally {
            releaseProvider(provider);
        }
    }

    public final int update(@Write Uri uri, ContentValues values, String where, String[] selectionArgs) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        IContentProvider provider = acquireProvider(uri);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int i;
        try {
            if (HwSystemManager.allowOp(uri, 2)) {
                long startTime = SystemClock.uptimeMillis();
                int rowsUpdated = provider.update(this.mPackageName, uri, values, where, selectionArgs);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, uri, "update", where);
                releaseProvider(provider);
                return rowsUpdated;
            }
            i = 0;
            return i;
        } catch (RemoteException e) {
            i = -1;
            return i;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Bundle call(Uri uri, String method, String arg, Bundle extras) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(method, "method");
        IContentProvider provider = acquireProvider(uri);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Bundle res = provider.call(this.mPackageName, method, arg, extras);
            Bundle.setDefusable(res, true);
            return res;
        } catch (RemoteException e) {
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final IContentProvider acquireProvider(Uri uri) {
        if (!"content".equals(uri.getScheme())) {
            return null;
        }
        String auth = uri.getAuthority();
        if (auth != null) {
            return acquireProvider(this.mContext, auth);
        }
        return null;
    }

    public final IContentProvider acquireExistingProvider(Uri uri) {
        if (!"content".equals(uri.getScheme())) {
            return null;
        }
        String auth = uri.getAuthority();
        if (auth != null) {
            return acquireExistingProvider(this.mContext, auth);
        }
        return null;
    }

    public final IContentProvider acquireProvider(String name) {
        if (name == null) {
            return null;
        }
        return acquireProvider(this.mContext, name);
    }

    public final IContentProvider acquireUnstableProvider(Uri uri) {
        if ("content".equals(uri.getScheme()) && uri.getAuthority() != null) {
            return acquireUnstableProvider(this.mContext, uri.getAuthority());
        }
        return null;
    }

    public final IContentProvider acquireUnstableProvider(String name) {
        if (name == null) {
            return null;
        }
        return acquireUnstableProvider(this.mContext, name);
    }

    public final ContentProviderClient acquireContentProviderClient(Uri uri) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        IContentProvider provider = acquireProvider(uri);
        if (provider != null) {
            return new ContentProviderClient(this, provider, true);
        }
        return null;
    }

    public final ContentProviderClient acquireContentProviderClient(String name) {
        Preconditions.checkNotNull(name, MidiDeviceInfo.PROPERTY_NAME);
        IContentProvider provider = acquireProvider(name);
        if (provider != null) {
            return new ContentProviderClient(this, provider, true);
        }
        return null;
    }

    public final ContentProviderClient acquireUnstableContentProviderClient(Uri uri) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        IContentProvider provider = acquireUnstableProvider(uri);
        if (provider != null) {
            return new ContentProviderClient(this, provider, false);
        }
        return null;
    }

    public final ContentProviderClient acquireUnstableContentProviderClient(String name) {
        Preconditions.checkNotNull(name, MidiDeviceInfo.PROPERTY_NAME);
        IContentProvider provider = acquireUnstableProvider(name);
        if (provider != null) {
            return new ContentProviderClient(this, provider, false);
        }
        return null;
    }

    public final void registerContentObserver(Uri uri, boolean notifyForDescendants, ContentObserver observer) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(observer, "observer");
        registerContentObserver(ContentProvider.getUriWithoutUserId(uri), notifyForDescendants, observer, ContentProvider.getUserIdFromUri(uri, this.mContext.getUserId()));
    }

    public final void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
        try {
            getContentService().registerContentObserver(uri, notifyForDescendents, observer.getContentObserver(), userHandle, this.mTargetSdkVersion);
        } catch (RemoteException e) {
        }
    }

    public final void unregisterContentObserver(ContentObserver observer) {
        Preconditions.checkNotNull(observer, "observer");
        try {
            IContentObserver contentObserver = observer.releaseContentObserver();
            if (contentObserver != null) {
                getContentService().unregisterContentObserver(contentObserver);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyChange(Uri uri, ContentObserver observer) {
        notifyChange(uri, observer, true);
    }

    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        notifyChange(ContentProvider.getUriWithoutUserId(uri), observer, syncToNetwork, ContentProvider.getUserIdFromUri(uri, this.mContext.getUserId()));
    }

    public void notifyChange(Uri uri, ContentObserver observer, int flags) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        notifyChange(ContentProvider.getUriWithoutUserId(uri), observer, flags, ContentProvider.getUserIdFromUri(uri, this.mContext.getUserId()));
    }

    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork, int userHandle) {
        int i = 0;
        IContentObserver iContentObserver = null;
        try {
            boolean deliverSelfNotifications;
            IContentService contentService = getContentService();
            if (observer != null) {
                iContentObserver = observer.getContentObserver();
            }
            if (observer != null) {
                deliverSelfNotifications = observer.deliverSelfNotifications();
            } else {
                deliverSelfNotifications = false;
            }
            if (syncToNetwork) {
                i = 1;
            }
            contentService.notifyChange(uri, iContentObserver, deliverSelfNotifications, i, userHandle, this.mTargetSdkVersion);
        } catch (RemoteException e) {
        }
    }

    public void notifyChange(Uri uri, ContentObserver observer, int flags, int userHandle) {
        IContentObserver iContentObserver = null;
        try {
            IContentService contentService = getContentService();
            if (observer != null) {
                iContentObserver = observer.getContentObserver();
            }
            contentService.notifyChange(uri, iContentObserver, observer != null ? observer.deliverSelfNotifications() : false, flags, userHandle, this.mTargetSdkVersion);
        } catch (RemoteException e) {
        }
    }

    public void takePersistableUriPermission(Uri uri, int modeFlags) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        try {
            ActivityManager.getService().takePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
        }
    }

    public void releasePersistableUriPermission(Uri uri, int modeFlags) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        try {
            ActivityManager.getService().releasePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
        }
    }

    public List<UriPermission> getPersistedUriPermissions() {
        try {
            ParceledListSlice result = ActivityManager.getService().getPersistedUriPermissions(this.mPackageName, true);
            if (result != null) {
                return result.getList();
            }
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Activity manager has died", e);
        }
    }

    public List<UriPermission> getOutgoingPersistedUriPermissions() {
        try {
            ParceledListSlice result = ActivityManager.getService().getPersistedUriPermissions(this.mPackageName, false);
            if (result != null) {
                return result.getList();
            }
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Activity manager has died", e);
        }
    }

    @Deprecated
    public void startSync(Uri uri, Bundle extras) {
        String str = null;
        Account account = null;
        if (extras != null) {
            String accountName = extras.getString("account");
            if (!TextUtils.isEmpty(accountName)) {
                account = new Account(accountName, "com.google");
            }
            extras.remove("account");
        }
        if (uri != null) {
            str = uri.getAuthority();
        }
        requestSync(account, str, extras);
    }

    public static void requestSync(Account account, String authority, Bundle extras) {
        requestSyncAsUser(account, authority, UserHandle.myUserId(), extras);
    }

    public static void requestSyncAsUser(Account account, String authority, int userId, Bundle extras) {
        if (extras == null) {
            throw new IllegalArgumentException("Must specify extras.");
        }
        try {
            getContentService().syncAsUser(new Builder().setSyncAdapter(account, authority).setExtras(extras).syncOnce().build(), userId);
        } catch (RemoteException e) {
        }
    }

    public static void requestSync(SyncRequest request) {
        try {
            getContentService().sync(request);
        } catch (RemoteException e) {
        }
    }

    public static void validateSyncExtrasBundle(Bundle extras) {
        try {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value != null && !(value instanceof Long) && !(value instanceof Integer) && !(value instanceof Boolean) && !(value instanceof Float) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Account)) {
                    throw new IllegalArgumentException("unexpected value type: " + value.getClass().getName());
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException exc) {
            throw new IllegalArgumentException("error unparceling Bundle", exc);
        }
    }

    @Deprecated
    public void cancelSync(Uri uri) {
        String authority;
        if (uri != null) {
            authority = uri.getAuthority();
        } else {
            authority = null;
        }
        cancelSync(null, authority);
    }

    public static void cancelSync(Account account, String authority) {
        try {
            getContentService().cancelSync(account, authority, null);
        } catch (RemoteException e) {
        }
    }

    public static void cancelSyncAsUser(Account account, String authority, int userId) {
        try {
            getContentService().cancelSyncAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
        }
    }

    public static SyncAdapterType[] getSyncAdapterTypes() {
        try {
            return getContentService().getSyncAdapterTypes();
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) {
        try {
            return getContentService().getSyncAdapterTypesAsUser(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        try {
            return getContentService().getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
        } catch (RemoteException e) {
            return (String[]) ArrayUtils.emptyArray(String.class);
        }
    }

    public static boolean getSyncAutomatically(Account account, String authority) {
        try {
            return getContentService().getSyncAutomatically(account, authority);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static boolean getSyncAutomaticallyAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getSyncAutomaticallyAsUser(account, authority, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void setSyncAutomatically(Account account, String authority, boolean sync) {
        setSyncAutomaticallyAsUser(account, authority, sync, UserHandle.myUserId());
    }

    public static void setSyncAutomaticallyAsUser(Account account, String authority, boolean sync, int userId) {
        try {
            getContentService().setSyncAutomaticallyAsUser(account, authority, sync, userId);
        } catch (RemoteException e) {
        }
    }

    public static void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
        validateSyncExtrasBundle(extras);
        if (extras.getBoolean("force", false) || extras.getBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false) || extras.getBoolean(SYNC_EXTRAS_IGNORE_BACKOFF, false) || extras.getBoolean(SYNC_EXTRAS_IGNORE_SETTINGS, false) || extras.getBoolean(SYNC_EXTRAS_INITIALIZE, false) || extras.getBoolean("force", false) || extras.getBoolean(SYNC_EXTRAS_EXPEDITED, false)) {
            throw new IllegalArgumentException("illegal extras were set");
        }
        try {
            getContentService().addPeriodicSync(account, authority, extras, pollFrequency);
        } catch (RemoteException e) {
        }
    }

    public static boolean invalidPeriodicExtras(Bundle extras) {
        if (extras.getBoolean("force", false) || extras.getBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false) || extras.getBoolean(SYNC_EXTRAS_IGNORE_BACKOFF, false) || extras.getBoolean(SYNC_EXTRAS_IGNORE_SETTINGS, false) || extras.getBoolean(SYNC_EXTRAS_INITIALIZE, false) || extras.getBoolean("force", false) || extras.getBoolean(SYNC_EXTRAS_EXPEDITED, false)) {
            return true;
        }
        return false;
    }

    public static void removePeriodicSync(Account account, String authority, Bundle extras) {
        validateSyncExtrasBundle(extras);
        try {
            getContentService().removePeriodicSync(account, authority, extras);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void cancelSync(SyncRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        try {
            getContentService().cancelRequest(request);
        } catch (RemoteException e) {
        }
    }

    public static List<PeriodicSync> getPeriodicSyncs(Account account, String authority) {
        try {
            return getContentService().getPeriodicSyncs(account, authority, null);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static int getIsSyncable(Account account, String authority) {
        try {
            return getContentService().getIsSyncable(account, authority);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static int getIsSyncableAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getIsSyncableAsUser(account, authority, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void setIsSyncable(Account account, String authority, int syncable) {
        try {
            getContentService().setIsSyncable(account, authority, syncable);
        } catch (RemoteException e) {
        }
    }

    public static boolean getMasterSyncAutomatically() {
        try {
            return getContentService().getMasterSyncAutomatically();
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static boolean getMasterSyncAutomaticallyAsUser(int userId) {
        try {
            return getContentService().getMasterSyncAutomaticallyAsUser(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void setMasterSyncAutomatically(boolean sync) {
        setMasterSyncAutomaticallyAsUser(sync, UserHandle.myUserId());
    }

    public static void setMasterSyncAutomaticallyAsUser(boolean sync, int userId) {
        try {
            getContentService().setMasterSyncAutomaticallyAsUser(sync, userId);
        } catch (RemoteException e) {
        }
    }

    public static boolean isSyncActive(Account account, String authority) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        } else if (authority == null) {
            throw new IllegalArgumentException("authority must not be null");
        } else {
            try {
                return getContentService().isSyncActive(account, authority, null);
            } catch (RemoteException e) {
                throw new RuntimeException("the ContentService should always be reachable", e);
            }
        }
    }

    @Deprecated
    public static SyncInfo getCurrentSync() {
        try {
            List<SyncInfo> syncs = getContentService().getCurrentSyncs();
            if (syncs.isEmpty()) {
                return null;
            }
            return (SyncInfo) syncs.get(0);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static List<SyncInfo> getCurrentSyncs() {
        try {
            return getContentService().getCurrentSyncs();
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static List<SyncInfo> getCurrentSyncsAsUser(int userId) {
        try {
            return getContentService().getCurrentSyncsAsUser(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static SyncStatusInfo getSyncStatus(Account account, String authority) {
        try {
            return getContentService().getSyncStatus(account, authority, null);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static SyncStatusInfo getSyncStatusAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getSyncStatusAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static boolean isSyncPending(Account account, String authority) {
        return isSyncPendingAsUser(account, authority, UserHandle.myUserId());
    }

    public static boolean isSyncPendingAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().isSyncPendingAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static Object addStatusChangeListener(int mask, final SyncStatusObserver callback) {
        if (callback == null) {
            throw new IllegalArgumentException("you passed in a null callback");
        }
        try {
            Stub observer = new Stub() {
                public void onStatusChanged(int which) throws RemoteException {
                    callback.onStatusChanged(which);
                }
            };
            getContentService().addStatusChangeListener(mask, observer);
            return observer;
        } catch (RemoteException e) {
            throw new RuntimeException("the ContentService should always be reachable", e);
        }
    }

    public static void removeStatusChangeListener(Object handle) {
        if (handle == null) {
            throw new IllegalArgumentException("you passed in a null handle");
        }
        try {
            getContentService().removeStatusChangeListener((Stub) handle);
        } catch (RemoteException e) {
        }
    }

    public void putCache(Uri key, Bundle value) {
        try {
            getContentService().putCache(this.mContext.getPackageName(), key, value, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Bundle getCache(Uri key) {
        try {
            Bundle bundle = getContentService().getCache(this.mContext.getPackageName(), key, this.mContext.getUserId());
            if (bundle != null) {
                bundle.setClassLoader(this.mContext.getClassLoader());
            }
            return bundle;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getTargetSdkVersion() {
        return this.mTargetSdkVersion;
    }

    private int samplePercentForDuration(long durationMillis) {
        if (durationMillis >= 500) {
            return 100;
        }
        return ((int) ((100 * durationMillis) / 500)) + 1;
    }

    private void maybeLogQueryToEventLog(long durationMillis, Uri uri, String[] projection, Bundle queryArgs) {
    }

    private void maybeLogUpdateToEventLog(long durationMillis, Uri uri, String operation, String selection) {
    }

    public static IContentService getContentService() {
        if (sContentService != null) {
            return sContentService;
        }
        sContentService = IContentService.Stub.asInterface(ServiceManager.getService("content"));
        return sContentService;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int resolveUserId(Uri uri) {
        return ContentProvider.getUserIdFromUri(uri, this.mContext.getUserId());
    }

    public Drawable getTypeDrawable(String mimeType) {
        return MimeIconUtils.loadMimeIcon(this.mContext, mimeType);
    }

    public static Bundle createSqlQueryBundle(String selection, String[] selectionArgs, String sortOrder) {
        if (selection == null && selectionArgs == null && sortOrder == null) {
            return null;
        }
        Bundle queryArgs = new Bundle();
        if (selection != null) {
            queryArgs.putString(QUERY_ARG_SQL_SELECTION, selection);
        }
        if (selectionArgs != null) {
            queryArgs.putStringArray(QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
        }
        if (sortOrder != null) {
            queryArgs.putString(QUERY_ARG_SQL_SORT_ORDER, sortOrder);
        }
        return queryArgs;
    }

    public static String createSqlSortClause(Bundle queryArgs) {
        String[] columns = queryArgs.getStringArray(QUERY_ARG_SORT_COLUMNS);
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("Can't create sort clause without columns.");
        }
        String query = TextUtils.join(", ", columns);
        int collation = queryArgs.getInt(QUERY_ARG_SORT_COLLATION, 3);
        if (collation == 0 || collation == 1) {
            query = query + " COLLATE NOCASE";
        }
        int sortDir = queryArgs.getInt(QUERY_ARG_SORT_DIRECTION, Integer.MIN_VALUE);
        if (sortDir == Integer.MIN_VALUE) {
            return query;
        }
        switch (sortDir) {
            case 0:
                return query + " ASC";
            case 1:
                return query + " DESC";
            default:
                throw new IllegalArgumentException("Unsupported sort direction value. See ContentResolver documentation for details.");
        }
    }
}
