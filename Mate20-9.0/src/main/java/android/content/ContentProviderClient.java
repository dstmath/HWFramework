package android.content;

import android.app.DownloadManager;
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
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContentProviderClient implements AutoCloseable {
    private static final String TAG = "ContentProviderClient";
    @GuardedBy("ContentProviderClient.class")
    private static Handler sAnrHandler;
    private NotRespondingRunnable mAnrRunnable;
    private long mAnrTimeout;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    /* access modifiers changed from: private */
    public final IContentProvider mContentProvider;
    /* access modifiers changed from: private */
    public final ContentResolver mContentResolver;
    private final String mPackageName;
    private final boolean mStable;

    private final class CursorWrapperInner extends CrossProcessCursorWrapper {
        private final CloseGuard mCloseGuard = CloseGuard.get();

        CursorWrapperInner(Cursor cursor) {
            super(cursor);
            this.mCloseGuard.open("close");
        }

        public void close() {
            this.mCloseGuard.close();
            super.close();
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

    private class NotRespondingRunnable implements Runnable {
        private NotRespondingRunnable() {
        }

        public void run() {
            Log.w(ContentProviderClient.TAG, "Detected provider not responding: " + ContentProviderClient.this.mContentProvider);
            ContentProviderClient.this.mContentResolver.appNotRespondingViaProvider(ContentProviderClient.this.mContentProvider);
        }
    }

    @VisibleForTesting
    public ContentProviderClient(ContentResolver contentResolver, IContentProvider contentProvider, boolean stable) {
        this.mContentResolver = contentResolver;
        this.mContentProvider = contentProvider;
        this.mPackageName = contentResolver.mPackageName;
        this.mStable = stable;
        this.mCloseGuard.open("close");
    }

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
        if (this.mAnrRunnable != null) {
            sAnrHandler.postDelayed(this.mAnrRunnable, this.mAnrTimeout);
        }
    }

    private void afterRemote() {
        if (this.mAnrRunnable != null) {
            sAnrHandler.removeCallbacks(this.mAnrRunnable);
        }
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws RemoteException {
        return query(url, projection, selection, selectionArgs, sortOrder, null);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) throws RemoteException {
        return query(uri, projection, ContentResolver.createSqlQueryBundle(selection, selectionArgs, sortOrder), cancellationSignal);
    }

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
            } catch (Throwable remoteCancellationSignal2) {
                afterRemote();
                throw remoteCancellationSignal2;
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
            } catch (Throwable remoteCancellationSignal2) {
                afterRemote();
                throw remoteCancellationSignal2;
            }
        }
        boolean refresh = this.mContentProvider.refresh(this.mPackageName, url, args, remoteCancellationSignal);
        afterRemote();
        return refresh;
    }

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
            } catch (Throwable remoteSignal2) {
                afterRemote();
                throw remoteSignal2;
            }
        }
        ParcelFileDescriptor openFile = this.mContentProvider.openFile(this.mPackageName, url, mode, remoteSignal, null);
        afterRemote();
        return openFile;
    }

    public AssetFileDescriptor openAssetFile(Uri url, String mode) throws RemoteException, FileNotFoundException {
        return openAssetFile(url, mode, null);
    }

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
            } catch (Throwable remoteSignal2) {
                afterRemote();
                throw remoteSignal2;
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
        Preconditions.checkNotNull(uri, DownloadManager.COLUMN_URI);
        Preconditions.checkNotNull(mimeType, "mimeType");
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
            } catch (Throwable remoteSignal2) {
                afterRemote();
                throw remoteSignal2;
            }
        }
        AssetFileDescriptor openTypedAssetFile = this.mContentProvider.openTypedAssetFile(this.mPackageName, uri, mimeType, opts, remoteSignal);
        afterRemote();
        return openTypedAssetFile;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Preconditions.checkNotNull(operations, "operations");
        beforeRemote();
        try {
            ContentProviderResult[] applyBatch = this.mContentProvider.applyBatch(this.mPackageName, operations);
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
        Preconditions.checkNotNull(method, "method");
        beforeRemote();
        try {
            Bundle call = this.mContentProvider.call(this.mPackageName, method, arg, extras);
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

    public static void releaseQuietly(ContentProviderClient client) {
        if (client != null) {
            try {
                client.release();
            } catch (Exception e) {
            }
        }
    }
}
