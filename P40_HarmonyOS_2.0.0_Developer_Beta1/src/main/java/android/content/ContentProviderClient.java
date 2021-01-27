package android.content;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetFileDescriptor;
import android.database.CrossProcessCursorWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.ICancellationSignal;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;

public class ContentProviderClient implements ContentInterface, AutoCloseable {
    private static final String TAG = "ContentProviderClient";
    @GuardedBy({"ContentProviderClient.class"})
    private static Handler sAnrHandler;
    private NotRespondingRunnable mAnrRunnable;
    private long mAnrTimeout;
    private final String mAuthority;
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
    @UnsupportedAppUsage
    private final IContentProvider mContentProvider;
    private final ContentResolver mContentResolver;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final String mPackageName;
    private final boolean mStable;

    @VisibleForTesting
    public ContentProviderClient(ContentResolver contentResolver, IContentProvider contentProvider, boolean stable) {
        this(contentResolver, contentProvider, "unknown", stable);
    }

    public ContentProviderClient(ContentResolver contentResolver, IContentProvider contentProvider, String authority, boolean stable) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mContentResolver = contentResolver;
        this.mContentProvider = contentProvider;
        this.mPackageName = contentResolver.mPackageName;
        this.mAuthority = authority;
        this.mStable = stable;
        this.mCloseGuard.open("close");
    }

    @SystemApi
    public void setDetectNotResponding(long timeoutMillis) {
        synchronized (ContentProviderClient.class) {
            this.mAnrTimeout = timeoutMillis;
            if (timeoutMillis > 0) {
                if (this.mAnrRunnable == null) {
                    this.mAnrRunnable = new NotRespondingRunnable();
                }
                if (sAnrHandler == null) {
                    sAnrHandler = new Handler(Looper.getMainLooper(), null, true);
                }
                Binder.allowBlocking(this.mContentProvider.asBinder());
            } else {
                this.mAnrRunnable = null;
                Binder.defaultBlocking(this.mContentProvider.asBinder());
            }
        }
    }

    private void beforeRemote() {
        NotRespondingRunnable notRespondingRunnable = this.mAnrRunnable;
        if (notRespondingRunnable != null) {
            sAnrHandler.postDelayed(notRespondingRunnable, this.mAnrTimeout);
        }
    }

    private void afterRemote() {
        NotRespondingRunnable notRespondingRunnable = this.mAnrRunnable;
        if (notRespondingRunnable != null) {
            sAnrHandler.removeCallbacks(notRespondingRunnable);
        }
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws RemoteException {
        return query(url, projection, selection, selectionArgs, sortOrder, null);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) throws RemoteException {
        return query(uri, projection, ContentResolver.createSqlQueryBundle(selection, selectionArgs, sortOrder), cancellationSignal);
    }

    @Override // android.content.ContentInterface
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) throws RemoteException {
        Preconditions.checkNotNull(uri, "url");
        beforeRemote();
        ICancellationSignal remoteCancellationSignal = null;
        if (cancellationSignal != null) {
            try {
                cancellationSignal.throwIfCanceled();
                remoteCancellationSignal = this.mContentProvider.createCancellationSignal();
                cancellationSignal.setRemote(remoteCancellationSignal);
            } catch (DeadObjectException e) {
                if (!this.mStable) {
                    this.mContentResolver.unstableProviderDied(this.mContentProvider);
                }
                throw e;
            } catch (Throwable th) {
                afterRemote();
                throw th;
            }
        }
        Cursor cursor = this.mContentProvider.query(this.mPackageName, uri, projection, queryArgs, remoteCancellationSignal);
        if (cursor == null) {
            afterRemote();
            return null;
        }
        CursorWrapperInner cursorWrapperInner = new CursorWrapperInner(cursor);
        afterRemote();
        return cursorWrapperInner;
    }

    @Override // android.content.ContentInterface
    public String getType(Uri url) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        try {
            String type = this.mContentProvider.getType(url);
            afterRemote();
            return type;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public String[] getStreamTypes(Uri url, String mimeTypeFilter) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(mimeTypeFilter, "mimeTypeFilter");
        beforeRemote();
        try {
            String[] streamTypes = this.mContentProvider.getStreamTypes(url, mimeTypeFilter);
            afterRemote();
            return streamTypes;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public final Uri canonicalize(Uri url) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        try {
            Uri canonicalize = this.mContentProvider.canonicalize(this.mPackageName, url);
            afterRemote();
            return canonicalize;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public final Uri uncanonicalize(Uri url) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        try {
            Uri uncanonicalize = this.mContentProvider.uncanonicalize(this.mPackageName, url);
            afterRemote();
            return uncanonicalize;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public boolean refresh(Uri url, Bundle args, CancellationSignal cancellationSignal) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        ICancellationSignal remoteCancellationSignal = null;
        if (cancellationSignal != null) {
            try {
                cancellationSignal.throwIfCanceled();
                remoteCancellationSignal = this.mContentProvider.createCancellationSignal();
                cancellationSignal.setRemote(remoteCancellationSignal);
            } catch (DeadObjectException e) {
                if (!this.mStable) {
                    this.mContentResolver.unstableProviderDied(this.mContentProvider);
                }
                throw e;
            } catch (Throwable th) {
                afterRemote();
                throw th;
            }
        }
        boolean refresh = this.mContentProvider.refresh(this.mPackageName, url, args, remoteCancellationSignal);
        afterRemote();
        return refresh;
    }

    @Override // android.content.ContentInterface
    public Uri insert(Uri url, ContentValues initialValues) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        try {
            Uri insert = this.mContentProvider.insert(this.mPackageName, url, initialValues);
            afterRemote();
            return insert;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public int bulkInsert(Uri url, ContentValues[] initialValues) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(initialValues, "initialValues");
        beforeRemote();
        try {
            int bulkInsert = this.mContentProvider.bulkInsert(this.mPackageName, url, initialValues);
            afterRemote();
            return bulkInsert;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public int delete(Uri url, String selection, String[] selectionArgs) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        try {
            int delete = this.mContentProvider.delete(this.mPackageName, url, selection, selectionArgs);
            afterRemote();
            return delete;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // android.content.ContentInterface
    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException {
        Preconditions.checkNotNull(url, "url");
        beforeRemote();
        try {
            int update = this.mContentProvider.update(this.mPackageName, url, values, selection, selectionArgs);
            afterRemote();
            return update;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    public ParcelFileDescriptor openFile(Uri url, String mode) throws RemoteException, FileNotFoundException {
        return openFile(url, mode, null);
    }

    @Override // android.content.ContentInterface
    public ParcelFileDescriptor openFile(Uri url, String mode, CancellationSignal signal) throws RemoteException, FileNotFoundException {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(mode, "mode");
        beforeRemote();
        ICancellationSignal remoteSignal = null;
        if (signal != null) {
            try {
                signal.throwIfCanceled();
                remoteSignal = this.mContentProvider.createCancellationSignal();
                signal.setRemote(remoteSignal);
            } catch (DeadObjectException e) {
                if (!this.mStable) {
                    this.mContentResolver.unstableProviderDied(this.mContentProvider);
                }
                throw e;
            } catch (Throwable th) {
                afterRemote();
                throw th;
            }
        }
        ParcelFileDescriptor openFile = this.mContentProvider.openFile(this.mPackageName, url, mode, remoteSignal, null);
        afterRemote();
        return openFile;
    }

    public AssetFileDescriptor openAssetFile(Uri url, String mode) throws RemoteException, FileNotFoundException {
        return openAssetFile(url, mode, null);
    }

    @Override // android.content.ContentInterface
    public AssetFileDescriptor openAssetFile(Uri url, String mode, CancellationSignal signal) throws RemoteException, FileNotFoundException {
        Preconditions.checkNotNull(url, "url");
        Preconditions.checkNotNull(mode, "mode");
        beforeRemote();
        ICancellationSignal remoteSignal = null;
        if (signal != null) {
            try {
                signal.throwIfCanceled();
                remoteSignal = this.mContentProvider.createCancellationSignal();
                signal.setRemote(remoteSignal);
            } catch (DeadObjectException e) {
                if (!this.mStable) {
                    this.mContentResolver.unstableProviderDied(this.mContentProvider);
                }
                throw e;
            } catch (Throwable th) {
                afterRemote();
                throw th;
            }
        }
        AssetFileDescriptor openAssetFile = this.mContentProvider.openAssetFile(this.mPackageName, url, mode, remoteSignal);
        afterRemote();
        return openAssetFile;
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) throws RemoteException, FileNotFoundException {
        return openTypedAssetFileDescriptor(uri, mimeType, opts, null);
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts, CancellationSignal signal) throws RemoteException, FileNotFoundException {
        return openTypedAssetFile(uri, mimeType, opts, signal);
    }

    @Override // android.content.ContentInterface
    public final AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws RemoteException, FileNotFoundException {
        Preconditions.checkNotNull(uri, "uri");
        Preconditions.checkNotNull(mimeTypeFilter, "mimeTypeFilter");
        beforeRemote();
        ICancellationSignal remoteSignal = null;
        if (signal != null) {
            try {
                signal.throwIfCanceled();
                remoteSignal = this.mContentProvider.createCancellationSignal();
                signal.setRemote(remoteSignal);
            } catch (DeadObjectException e) {
                if (!this.mStable) {
                    this.mContentResolver.unstableProviderDied(this.mContentProvider);
                }
                throw e;
            } catch (Throwable th) {
                afterRemote();
                throw th;
            }
        }
        AssetFileDescriptor openTypedAssetFile = this.mContentProvider.openTypedAssetFile(this.mPackageName, uri, mimeTypeFilter, opts, remoteSignal);
        afterRemote();
        return openTypedAssetFile;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        return applyBatch(this.mAuthority, operations);
    }

    @Override // android.content.ContentInterface
    public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Preconditions.checkNotNull(operations, "operations");
        beforeRemote();
        try {
            ContentProviderResult[] applyBatch = this.mContentProvider.applyBatch(this.mPackageName, authority, operations);
            afterRemote();
            return applyBatch;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    public Bundle call(String method, String arg, Bundle extras) throws RemoteException {
        return call(this.mAuthority, method, arg, extras);
    }

    @Override // android.content.ContentInterface
    public Bundle call(String authority, String method, String arg, Bundle extras) throws RemoteException {
        Preconditions.checkNotNull(authority, ContactsContract.Directory.DIRECTORY_AUTHORITY);
        Preconditions.checkNotNull(method, CalendarContract.RemindersColumns.METHOD);
        beforeRemote();
        try {
            Bundle call = this.mContentProvider.call(this.mPackageName, authority, method, arg, extras);
            afterRemote();
            return call;
        } catch (DeadObjectException e) {
            if (!this.mStable) {
                this.mContentResolver.unstableProviderDied(this.mContentProvider);
            }
            throw e;
        } catch (Throwable th) {
            afterRemote();
            throw th;
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        closeInternal();
    }

    @Deprecated
    public boolean release() {
        return closeInternal();
    }

    private boolean closeInternal() {
        this.mCloseGuard.close();
        if (!this.mClosed.compareAndSet(false, true)) {
            return false;
        }
        setDetectNotResponding(0);
        if (this.mStable) {
            return this.mContentResolver.releaseProvider(this.mContentProvider);
        }
        return this.mContentResolver.releaseUnstableProvider(this.mContentProvider);
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
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

    public ContentProvider getLocalContentProvider() {
        return ContentProvider.coerceToLocalContentProvider(this.mContentProvider);
    }

    @Deprecated
    public static void closeQuietly(ContentProviderClient client) {
        IoUtils.closeQuietly(client);
    }

    @Deprecated
    public static void releaseQuietly(ContentProviderClient client) {
        IoUtils.closeQuietly(client);
    }

    /* access modifiers changed from: private */
    public class NotRespondingRunnable implements Runnable {
        private NotRespondingRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Log.w(ContentProviderClient.TAG, "Detected provider not responding: " + ContentProviderClient.this.mContentProvider);
            ContentProviderClient.this.mContentResolver.appNotRespondingViaProvider(ContentProviderClient.this.mContentProvider);
        }
    }

    /* access modifiers changed from: private */
    public final class CursorWrapperInner extends CrossProcessCursorWrapper {
        private final CloseGuard mCloseGuard = CloseGuard.get();

        CursorWrapperInner(Cursor cursor) {
            super(cursor);
            this.mCloseGuard.open("close");
        }

        @Override // android.database.CursorWrapper, android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            this.mCloseGuard.close();
            super.close();
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
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
}
