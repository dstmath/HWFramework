package android.content;

import android.accounts.Account;
import android.annotation.RequiresPermission;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.DownloadManager;
import android.app.backup.FullBackup;
import android.content.IContentService;
import android.content.ISyncStatusObserver;
import android.content.SyncRequest;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
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
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.MimeIconUtils;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    private static final String[] SYNC_ERROR_NAMES = {"already-in-progress", "authentication-error", "io-error", "parse-error", "conflict", "too-many-deletions", "too-many-retries", "internal-error"};
    public static final int SYNC_ERROR_PARSE = 4;
    public static final int SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS = 1;
    public static final int SYNC_ERROR_TOO_MANY_DELETIONS = 6;
    public static final int SYNC_ERROR_TOO_MANY_RETRIES = 7;
    public static final int SYNC_EXEMPTION_NONE = 0;
    public static final int SYNC_EXEMPTION_PROMOTE_BUCKET = 1;
    public static final int SYNC_EXEMPTION_PROMOTE_BUCKET_WITH_TEMP = 2;
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
    public static final String SYNC_VIRTUAL_EXTRAS_EXEMPTION_FLAG = "v_exemption";
    private static final String TAG = "ContentResolver";
    private static volatile IContentService sContentService;
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

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mCloseGuard != null) {
                    this.mCloseGuard.warnIfOpen();
                }
                close();
            } finally {
                super.finalize();
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface NotifyFlags {
    }

    public class OpenResourceIdResult {
        public int id;
        public Resources r;

        public OpenResourceIdResult() {
        }
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface QueryCollator {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SortDirection {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncExemption {
    }

    /* access modifiers changed from: protected */
    public abstract IContentProvider acquireProvider(Context context, String str);

    /* access modifiers changed from: protected */
    public abstract IContentProvider acquireUnstableProvider(Context context, String str);

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
        this.mContext = context != null ? context : ActivityThread.currentApplication();
        this.mPackageName = this.mContext.getOpPackageName();
        this.mTargetSdkVersion = this.mContext.getApplicationInfo().targetSdkVersion;
    }

    /* access modifiers changed from: protected */
    public IContentProvider acquireExistingProvider(Context c, String name) {
        return acquireProvider(c, name);
    }

    public void appNotRespondingViaProvider(IContentProvider icp) {
        throw new UnsupportedOperationException("appNotRespondingViaProvider");
    }

    public final String getType(Uri url) {
        Preconditions.checkNotNull(url, "url");
        IContentProvider provider = acquireExistingProvider(url);
        if (provider != null) {
            try {
                return provider.getType(url);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: provider.getType");
                return null;
            } catch (Exception e2) {
                Log.w(TAG, "Failed to get type for: " + url + " (" + e2.getMessage() + ")");
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
                throw e3.rethrowFromSystemServer();
            } catch (Exception e4) {
                Log.w(TAG, "Failed to get type for: " + url + " (" + e4.getMessage() + ")");
                return null;
            }
        }
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(mimeTypeFilter, "mimeTypeFilter");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            return provider.getStreamTypes(url, mimeTypeFilter);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: provider.getStreamTypes");
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Cursor query(@RequiresPermission.Read Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(uri, projection, selection, selectionArgs, sortOrder, null);
    }

    public final Cursor query(@RequiresPermission.Read Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        if (!HwSystemManager.allowOp(uri, 1)) {
            return HwSystemManager.getDummyCursor(this, uri, projection, selection, selectionArgs, sortOrder);
        }
        return query(uri, projection, createSqlQueryBundle(selection, selectionArgs, sortOrder), cancellationSignal);
    }

    public final Cursor query(@RequiresPermission.Read Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Cursor qCursor;
        CancellationSignal cancellationSignal2 = cancellationSignal;
        Uri uri2 = uri;
        Preconditions.checkNotNull(uri2, DownloadManager.COLUMN_URI);
        IContentProvider unstableProvider = acquireUnstableProvider(uri);
        if (unstableProvider == null) {
            return null;
        }
        IContentProvider stableProvider = null;
        Cursor qCursor2 = null;
        try {
            long startTime = SystemClock.uptimeMillis();
            ICancellationSignal remoteCancellationSignal = null;
            if (cancellationSignal2 != null) {
                cancellationSignal.throwIfCanceled();
                remoteCancellationSignal = unstableProvider.createCancellationSignal();
                cancellationSignal2.setRemote(remoteCancellationSignal);
            }
            ICancellationSignal remoteCancellationSignal2 = remoteCancellationSignal;
            try {
                qCursor = unstableProvider.query(this.mPackageName, uri2, projection, queryArgs, remoteCancellationSignal2);
            } catch (DeadObjectException e) {
                unstableProviderDied(unstableProvider);
                stableProvider = acquireProvider(uri);
                if (stableProvider == null) {
                    if (qCursor2 != null) {
                        qCursor2.close();
                    }
                    if (cancellationSignal2 != null) {
                        cancellationSignal2.setRemote(null);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                    if (stableProvider != null) {
                        releaseProvider(stableProvider);
                    }
                    return null;
                }
                qCursor = stableProvider.query(this.mPackageName, uri2, projection, queryArgs, remoteCancellationSignal2);
            }
            qCursor2 = qCursor;
            if (qCursor2 == null) {
                if (qCursor2 != null) {
                    qCursor2.close();
                }
                if (cancellationSignal2 != null) {
                    cancellationSignal2.setRemote(null);
                }
                if (unstableProvider != null) {
                    releaseUnstableProvider(unstableProvider);
                }
                if (stableProvider != null) {
                    releaseProvider(stableProvider);
                }
                return null;
            }
            qCursor2.getCount();
            maybeLogQueryToEventLog(SystemClock.uptimeMillis() - startTime, uri2, projection, queryArgs);
            CursorWrapperInner wrapper = new CursorWrapperInner(qCursor2, stableProvider != null ? stableProvider : acquireProvider(uri));
            Cursor qCursor3 = null;
            if (qCursor3 != null) {
                qCursor3.close();
            }
            if (cancellationSignal2 != null) {
                cancellationSignal2.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            if (0 != 0) {
                releaseProvider(null);
            }
            return wrapper;
        } catch (RemoteException e2) {
            Log.w(TAG, "RemoteException: query");
            if (qCursor2 != null) {
                qCursor2.close();
            }
            if (cancellationSignal2 != null) {
                cancellationSignal2.setRemote(null);
            }
            if (unstableProvider != null) {
                releaseUnstableProvider(unstableProvider);
            }
            if (stableProvider != null) {
                releaseProvider(stableProvider);
            }
            return null;
        } catch (Throwable th) {
            if (qCursor2 != null) {
                qCursor2.close();
            }
            if (cancellationSignal2 != null) {
                cancellationSignal2.setRemote(null);
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
        Preconditions.checkNotNull(url, "url");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            return provider.canonicalize(this.mPackageName, url);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: provider.canonicalize");
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    public final Uri uncanonicalize(Uri url) {
        Preconditions.checkNotNull(url, "url");
        IContentProvider provider = acquireProvider(url);
        if (provider == null) {
            return null;
        }
        try {
            return provider.uncanonicalize(this.mPackageName, url);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: provider.uncanonicalize");
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
                Log.w(TAG, "RemoteException: provider.refresh");
                releaseProvider(provider);
                return false;
            } catch (Throwable th) {
                releaseProvider(provider);
                throw th;
            }
        }
        boolean refresh = provider.refresh(this.mPackageName, url, args, remoteCancellationSignal);
        releaseProvider(provider);
        return refresh;
    }

    public final InputStream openInputStream(Uri uri) throws FileNotFoundException {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            OpenResourceIdResult r = getResourceId(uri);
            try {
                return r.r.openRawResource(r.id);
            } catch (Resources.NotFoundException e) {
                throw new FileNotFoundException("Resource does not exist: " + uri);
            }
        } else if (SCHEME_FILE.equals(scheme)) {
            return new FileInputStream(uri.getPath());
        } else {
            FileInputStream fileInputStream = null;
            AssetFileDescriptor fd = openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN, null);
            if (fd != null) {
                try {
                    fileInputStream = fd.createInputStream();
                } catch (IOException e2) {
                    throw new FileNotFoundException("Unable to create stream");
                }
            }
            return fileInputStream;
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
        ICancellationSignal remoteCancellationSignal;
        AssetFileDescriptor fd;
        Uri uri2 = uri;
        String str = mode;
        CancellationSignal cancellationSignal2 = cancellationSignal;
        Preconditions.checkNotNull(uri2, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(str, "mode");
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            if (FullBackup.ROOT_TREE_TOKEN.equals(str)) {
                OpenResourceIdResult r = getResourceId(uri);
                try {
                    return r.r.openRawResourceFd(r.id);
                } catch (Resources.NotFoundException e) {
                    throw new FileNotFoundException("Resource does not exist: " + uri2);
                }
            } else {
                throw new FileNotFoundException("Can't write resources: " + uri2);
            }
        } else if (SCHEME_FILE.equals(scheme)) {
            AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(ParcelFileDescriptor.open(new File(uri.getPath()), ParcelFileDescriptor.parseMode(mode)), 0, -1);
            return assetFileDescriptor;
        } else if (FullBackup.ROOT_TREE_TOKEN.equals(str)) {
            return openTypedAssetFileDescriptor(uri2, "*/*", null, cancellationSignal2);
        } else {
            IContentProvider unstableProvider = acquireUnstableProvider(uri);
            if (unstableProvider != null) {
                IContentProvider stableProvider = null;
                ICancellationSignal remoteCancellationSignal2 = null;
                if (cancellationSignal2 != null) {
                    try {
                        cancellationSignal.throwIfCanceled();
                        remoteCancellationSignal2 = unstableProvider.createCancellationSignal();
                        cancellationSignal2.setRemote(remoteCancellationSignal2);
                    } catch (DeadObjectException e2) {
                        unstableProviderDied(unstableProvider);
                        stableProvider = acquireProvider(uri);
                        if (stableProvider != null) {
                            fd = stableProvider.openAssetFile(this.mPackageName, uri2, str, remoteCancellationSignal);
                            if (fd == null) {
                                if (cancellationSignal2 != null) {
                                    cancellationSignal2.setRemote(null);
                                }
                                if (stableProvider != null) {
                                    releaseProvider(stableProvider);
                                }
                                if (unstableProvider != null) {
                                    releaseUnstableProvider(unstableProvider);
                                }
                                return null;
                            }
                        } else {
                            throw new FileNotFoundException("No content provider: " + uri2);
                        }
                    } catch (RemoteException e3) {
                        throw new FileNotFoundException("Failed opening content provider: " + uri2);
                    } catch (FileNotFoundException e4) {
                        throw e4;
                    } catch (Throwable remoteCancellationSignal3) {
                        if (cancellationSignal2 != null) {
                            cancellationSignal2.setRemote(null);
                        }
                        if (stableProvider != null) {
                            releaseProvider(stableProvider);
                        }
                        if (unstableProvider != null) {
                            releaseUnstableProvider(unstableProvider);
                        }
                        throw remoteCancellationSignal3;
                    }
                }
                remoteCancellationSignal = remoteCancellationSignal2;
                AssetFileDescriptor fd2 = unstableProvider.openAssetFile(this.mPackageName, uri2, str, remoteCancellationSignal);
                if (fd2 == null) {
                    if (cancellationSignal2 != null) {
                        cancellationSignal2.setRemote(null);
                    }
                    if (0 != 0) {
                        releaseProvider(null);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                    return null;
                }
                fd = fd2;
                if (stableProvider == null) {
                    stableProvider = acquireProvider(uri);
                }
                releaseUnstableProvider(unstableProvider);
                AssetFileDescriptor assetFileDescriptor2 = new AssetFileDescriptor(new ParcelFileDescriptorInner(fd.getParcelFileDescriptor(), stableProvider), fd.getStartOffset(), fd.getDeclaredLength());
                if (cancellationSignal2 != null) {
                    cancellationSignal2.setRemote(null);
                }
                if (0 != 0) {
                    releaseProvider(null);
                }
                if (0 != 0) {
                    releaseUnstableProvider(null);
                }
                return assetFileDescriptor2;
            }
            throw new FileNotFoundException("No content provider: " + uri2);
        }
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) throws FileNotFoundException {
        return openTypedAssetFileDescriptor(uri, mimeType, opts, null);
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts, CancellationSignal cancellationSignal) throws FileNotFoundException {
        ICancellationSignal remoteCancellationSignal;
        AssetFileDescriptor fd;
        Uri uri2 = uri;
        CancellationSignal cancellationSignal2 = cancellationSignal;
        Preconditions.checkNotNull(uri2, DownloadManager.COLUMN_URI);
        String str = mimeType;
        Preconditions.checkNotNull(str, "mimeType");
        IContentProvider unstableProvider = acquireUnstableProvider(uri);
        if (unstableProvider != null) {
            IContentProvider stableProvider = null;
            ICancellationSignal remoteCancellationSignal2 = null;
            if (cancellationSignal2 != null) {
                try {
                    cancellationSignal.throwIfCanceled();
                    remoteCancellationSignal2 = unstableProvider.createCancellationSignal();
                    cancellationSignal2.setRemote(remoteCancellationSignal2);
                } catch (DeadObjectException e) {
                    unstableProviderDied(unstableProvider);
                    stableProvider = acquireProvider(uri);
                    if (stableProvider != null) {
                        AssetFileDescriptor fd2 = stableProvider.openTypedAssetFile(this.mPackageName, uri2, str, opts, remoteCancellationSignal);
                        if (fd2 == null) {
                            if (cancellationSignal2 != null) {
                                cancellationSignal2.setRemote(null);
                            }
                            if (stableProvider != null) {
                                releaseProvider(stableProvider);
                            }
                            if (unstableProvider != null) {
                                releaseUnstableProvider(unstableProvider);
                            }
                            return null;
                        }
                        fd = fd2;
                    } else {
                        throw new FileNotFoundException("No content provider: " + uri2);
                    }
                } catch (RemoteException e2) {
                    throw new FileNotFoundException("Failed opening content provider: " + uri2);
                } catch (FileNotFoundException e3) {
                    throw e3;
                } catch (Throwable remoteCancellationSignal3) {
                    if (cancellationSignal2 != null) {
                        cancellationSignal2.setRemote(null);
                    }
                    if (stableProvider != null) {
                        releaseProvider(stableProvider);
                    }
                    if (unstableProvider != null) {
                        releaseUnstableProvider(unstableProvider);
                    }
                    throw remoteCancellationSignal3;
                }
            }
            remoteCancellationSignal = remoteCancellationSignal2;
            AssetFileDescriptor fd3 = unstableProvider.openTypedAssetFile(this.mPackageName, uri2, str, opts, remoteCancellationSignal);
            if (fd3 == null) {
                if (cancellationSignal2 != null) {
                    cancellationSignal2.setRemote(null);
                }
                if (0 != 0) {
                    releaseProvider(null);
                }
                if (unstableProvider != null) {
                    releaseUnstableProvider(unstableProvider);
                }
                return null;
            }
            fd = fd3;
            if (stableProvider == null) {
                stableProvider = acquireProvider(uri);
            }
            releaseUnstableProvider(unstableProvider);
            AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(new ParcelFileDescriptorInner(fd.getParcelFileDescriptor(), stableProvider), fd.getStartOffset(), fd.getDeclaredLength());
            if (cancellationSignal2 != null) {
                cancellationSignal2.setRemote(null);
            }
            if (0 != 0) {
                releaseProvider(null);
            }
            if (0 != 0) {
                releaseUnstableProvider(null);
            }
            return assetFileDescriptor;
        }
        throw new FileNotFoundException("No content provider: " + uri2);
    }

    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        int id;
        String authority = uri.getAuthority();
        if (!TextUtils.isEmpty(authority)) {
            try {
                Resources r = this.mContext.getPackageManager().getResourcesForApplication(authority);
                List<String> path = uri.getPathSegments();
                if (path != null) {
                    int len = path.size();
                    if (len == 1) {
                        try {
                            id = Integer.parseInt(path.get(0));
                        } catch (NumberFormatException e) {
                            throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
                        }
                    } else if (len == 2) {
                        id = r.getIdentifier(path.get(1), path.get(0), authority);
                    } else {
                        throw new FileNotFoundException("More than two path segments: " + uri);
                    }
                    if (id != 0) {
                        OpenResourceIdResult res = new OpenResourceIdResult();
                        res.r = r;
                        res.id = id;
                        return res;
                    }
                    throw new FileNotFoundException("No resource found for: " + uri);
                }
                throw new FileNotFoundException("No path: " + uri);
            } catch (PackageManager.NameNotFoundException e2) {
                throw new FileNotFoundException("No package found for authority: " + uri);
            }
        } else {
            throw new FileNotFoundException("No authority: " + uri);
        }
    }

    public final Uri insert(@RequiresPermission.Write Uri url, ContentValues values) {
        Preconditions.checkNotNull(url, "url");
        if (!HwSystemManager.allowOp(url, 2)) {
            return null;
        }
        IContentProvider provider = acquireProvider(url);
        if (provider != null) {
            try {
                long startTime = SystemClock.uptimeMillis();
                Uri createdRow = provider.insert(this.mPackageName, url, values);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "insert", null);
                return createdRow;
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: provider.insert");
                return null;
            } finally {
                releaseProvider(provider);
            }
        } else {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Preconditions.checkNotNull(authority, "authority");
        Preconditions.checkNotNull(operations, "operations");
        ContentProviderClient provider = acquireContentProviderClient(authority);
        if (provider != null) {
            try {
                return provider.applyBatch(HwSystemManager.getAllowedApplyBatchOp(authority, operations));
            } finally {
                provider.release();
            }
        } else {
            throw new IllegalArgumentException("Unknown authority " + authority);
        }
    }

    public final int bulkInsert(@RequiresPermission.Write Uri url, ContentValues[] values) {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(values, "values");
        IContentProvider provider = acquireProvider(url);
        if (provider != null) {
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
                Log.w(TAG, "RemoteException: provider.bulkInsert");
                return 0;
            } finally {
                releaseProvider(provider);
            }
        } else {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public final int delete(@RequiresPermission.Write Uri url, String where, String[] selectionArgs) {
        Preconditions.checkNotNull(url, "url");
        IContentProvider provider = acquireProvider(url);
        if (provider != null) {
            try {
                if (!HwSystemManager.allowOp(url, 3)) {
                    return 0;
                }
                long startTime = SystemClock.uptimeMillis();
                int rowsDeleted = provider.delete(this.mPackageName, url, where, selectionArgs);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, url, "delete", where);
                releaseProvider(provider);
                return rowsDeleted;
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: provider.delete");
                return -1;
            } finally {
                releaseProvider(provider);
            }
        } else {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public final int update(@RequiresPermission.Write Uri uri, ContentValues values, String where, String[] selectionArgs) {
        Uri uri2 = uri;
        Preconditions.checkNotNull(uri2, DownloadManager.COLUMN_URI);
        IContentProvider provider = acquireProvider(uri2);
        if (provider != null) {
            try {
                if (!HwSystemManager.allowOp(uri2, 2)) {
                    return 0;
                }
                long startTime = SystemClock.uptimeMillis();
                int rowsUpdated = provider.update(this.mPackageName, uri2, values, where, selectionArgs);
                maybeLogUpdateToEventLog(SystemClock.uptimeMillis() - startTime, uri2, "update", where);
                releaseProvider(provider);
                return rowsUpdated;
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: provider.update");
                return -1;
            } finally {
                releaseProvider(provider);
            }
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri2);
        }
    }

    public final Bundle call(Uri uri, String method, String arg, Bundle extras) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(method, "method");
        IContentProvider provider = acquireProvider(uri);
        if (provider != null) {
            try {
                Bundle res = provider.call(this.mPackageName, method, arg, extras);
                Bundle.setDefusable(res, true);
                return res;
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: provider.call");
                return null;
            } finally {
                releaseProvider(provider);
            }
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
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
            Log.w(TAG, "RemoteException: getContentService().registerContentObserver");
            throw e.rethrowFromSystemServer();
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
            Log.w(TAG, "RemoteException: getContentService().unregisterContentObserver");
            throw e.rethrowFromSystemServer();
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
        try {
            getContentService().notifyChange(uri, observer == null ? null : observer.getContentObserver(), observer != null && observer.deliverSelfNotifications(), syncToNetwork ? 1 : 0, userHandle, this.mTargetSdkVersion);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().notifyChange");
            throw e.rethrowFromSystemServer();
        }
    }

    public void notifyChange(Uri uri, ContentObserver observer, int flags, int userHandle) {
        try {
            getContentService().notifyChange(uri, observer == null ? null : observer.getContentObserver(), observer != null && observer.deliverSelfNotifications(), flags, userHandle, this.mTargetSdkVersion);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().notifyChange");
            throw e.rethrowFromSystemServer();
        }
    }

    public void takePersistableUriPermission(Uri uri, int modeFlags) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        try {
            ActivityManager.getService().takePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, null, resolveUserId(uri));
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: ActivityManager.getService().takePersistableUriPermission");
            throw e.rethrowFromSystemServer();
        }
    }

    public void takePersistableUriPermission(String toPackage, Uri uri, int modeFlags) {
        Preconditions.checkNotNull(toPackage, "toPackage");
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        try {
            ActivityManager.getService().takePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, toPackage, resolveUserId(uri));
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: ActivityManager.getService().takePersistableUriPermission");
            throw e.rethrowFromSystemServer();
        }
    }

    public void releasePersistableUriPermission(Uri uri, int modeFlags) {
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        try {
            ActivityManager.getService().releasePersistableUriPermission(ContentProvider.getUriWithoutUserId(uri), modeFlags, null, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
            throw e.rethrowFromSystemServer();
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
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void startSync(Uri uri, Bundle extras) {
        Account account = null;
        if (extras != null) {
            String accountName = extras.getString("account");
            if (!TextUtils.isEmpty(accountName)) {
                account = new Account(accountName, "com.google");
            }
            extras.remove("account");
        }
        requestSync(account, uri != null ? uri.getAuthority() : null, extras);
    }

    public static void requestSync(Account account, String authority, Bundle extras) {
        requestSyncAsUser(account, authority, UserHandle.myUserId(), extras);
    }

    public static void requestSyncAsUser(Account account, String authority, int userId, Bundle extras) {
        if (extras != null) {
            try {
                getContentService().syncAsUser(new SyncRequest.Builder().setSyncAdapter(account, authority).setExtras(extras).syncOnce().build(), userId);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: getContentService().syncAsUser");
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Must specify extras.");
        }
    }

    public static void requestSync(SyncRequest request) {
        try {
            getContentService().sync(request);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().sync");
            throw e.rethrowFromSystemServer();
        }
    }

    public static void validateSyncExtrasBundle(Bundle extras) {
        try {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value != null) {
                    if (!(value instanceof Long)) {
                        if (!(value instanceof Integer)) {
                            if (!(value instanceof Boolean)) {
                                if (!(value instanceof Float)) {
                                    if (!(value instanceof Double)) {
                                        if (!(value instanceof String)) {
                                            if (!(value instanceof Account)) {
                                                throw new IllegalArgumentException("unexpected value type: " + value.getClass().getName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
        cancelSync(null, uri != null ? uri.getAuthority() : null);
    }

    public static void cancelSync(Account account, String authority) {
        try {
            getContentService().cancelSync(account, authority, null);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().cancelSync");
            throw e.rethrowFromSystemServer();
        }
    }

    public static void cancelSyncAsUser(Account account, String authority, int userId) {
        try {
            getContentService().cancelSyncAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().cancelSyncAsUser");
            throw e.rethrowFromSystemServer();
        }
    }

    public static SyncAdapterType[] getSyncAdapterTypes() {
        try {
            return getContentService().getSyncAdapterTypes();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) {
        try {
            return getContentService().getSyncAdapterTypesAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        try {
            return getContentService().getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().getSyncAdapterPackagesForAuthorityAsUser");
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean getSyncAutomatically(Account account, String authority) {
        try {
            return getContentService().getSyncAutomatically(account, authority);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean getSyncAutomaticallyAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getSyncAutomaticallyAsUser(account, authority, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void setSyncAutomatically(Account account, String authority, boolean sync) {
        setSyncAutomaticallyAsUser(account, authority, sync, UserHandle.myUserId());
    }

    public static void setSyncAutomaticallyAsUser(Account account, String authority, boolean sync, int userId) {
        try {
            getContentService().setSyncAutomaticallyAsUser(account, authority, sync, userId);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().setSyncAutomaticallyAsUser");
            throw e.rethrowFromSystemServer();
        }
    }

    public static void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
        validateSyncExtrasBundle(extras);
        if (!invalidPeriodicExtras(extras)) {
            try {
                getContentService().addPeriodicSync(account, authority, extras, pollFrequency);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: getContentService().addPeriodicSync");
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("illegal extras were set");
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
            throw e.rethrowFromSystemServer();
        }
    }

    public static void cancelSync(SyncRequest request) {
        if (request != null) {
            try {
                getContentService().cancelRequest(request);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: getContentService().cancelRequest");
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("request cannot be null");
        }
    }

    public static List<PeriodicSync> getPeriodicSyncs(Account account, String authority) {
        try {
            return getContentService().getPeriodicSyncs(account, authority, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getIsSyncable(Account account, String authority) {
        try {
            return getContentService().getIsSyncable(account, authority);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getIsSyncableAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getIsSyncableAsUser(account, authority, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void setIsSyncable(Account account, String authority, int syncable) {
        try {
            getContentService().setIsSyncable(account, authority, syncable);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().setIsSyncable");
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean getMasterSyncAutomatically() {
        try {
            return getContentService().getMasterSyncAutomatically();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean getMasterSyncAutomaticallyAsUser(int userId) {
        try {
            return getContentService().getMasterSyncAutomaticallyAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void setMasterSyncAutomatically(boolean sync) {
        setMasterSyncAutomaticallyAsUser(sync, UserHandle.myUserId());
    }

    public static void setMasterSyncAutomaticallyAsUser(boolean sync, int userId) {
        try {
            getContentService().setMasterSyncAutomaticallyAsUser(sync, userId);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: getContentService().setMasterSyncAutomaticallyAsUser");
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isSyncActive(Account account, String authority) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        } else if (authority != null) {
            try {
                return getContentService().isSyncActive(account, authority, null);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("authority must not be null");
        }
    }

    @Deprecated
    public static SyncInfo getCurrentSync() {
        try {
            List<SyncInfo> syncs = getContentService().getCurrentSyncs();
            if (syncs.isEmpty()) {
                return null;
            }
            return syncs.get(0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static List<SyncInfo> getCurrentSyncs() {
        try {
            return getContentService().getCurrentSyncs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static List<SyncInfo> getCurrentSyncsAsUser(int userId) {
        try {
            return getContentService().getCurrentSyncsAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static SyncStatusInfo getSyncStatus(Account account, String authority) {
        try {
            return getContentService().getSyncStatus(account, authority, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static SyncStatusInfo getSyncStatusAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().getSyncStatusAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isSyncPending(Account account, String authority) {
        return isSyncPendingAsUser(account, authority, UserHandle.myUserId());
    }

    public static boolean isSyncPendingAsUser(Account account, String authority, int userId) {
        try {
            return getContentService().isSyncPendingAsUser(account, authority, null, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static Object addStatusChangeListener(int mask, final SyncStatusObserver callback) {
        if (callback != null) {
            try {
                ISyncStatusObserver.Stub observer = new ISyncStatusObserver.Stub() {
                    public void onStatusChanged(int which) throws RemoteException {
                        SyncStatusObserver.this.onStatusChanged(which);
                    }
                };
                getContentService().addStatusChangeListener(mask, observer);
                return observer;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("you passed in a null callback");
        }
    }

    public static void removeStatusChangeListener(Object handle) {
        if (handle != null) {
            try {
                getContentService().removeStatusChangeListener((ISyncStatusObserver.Stub) handle);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException: getContentService().removeStatusChangeListener");
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("you passed in a null handle");
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

    public int getUserId() {
        return this.mContext.getUserId();
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
