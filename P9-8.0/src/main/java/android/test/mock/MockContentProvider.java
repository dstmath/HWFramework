package android.test.mock;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.OperationApplicationException;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MockContentProvider extends ContentProvider {
    private final InversionIContentProvider mIContentProvider = new InversionIContentProvider(this, null);

    private class InversionIContentProvider implements IContentProvider {
        /* synthetic */ InversionIContentProvider(MockContentProvider this$0, InversionIContentProvider -this1) {
            this();
        }

        private InversionIContentProvider() {
        }

        public ContentProviderResult[] applyBatch(String callingPackage, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
            return MockContentProvider.this.applyBatch(operations);
        }

        public int bulkInsert(String callingPackage, Uri url, ContentValues[] initialValues) throws RemoteException {
            return MockContentProvider.this.bulkInsert(url, initialValues);
        }

        public int delete(String callingPackage, Uri url, String selection, String[] selectionArgs) throws RemoteException {
            return MockContentProvider.this.delete(url, selection, selectionArgs);
        }

        public String getType(Uri url) throws RemoteException {
            return MockContentProvider.this.getType(url);
        }

        public Uri insert(String callingPackage, Uri url, ContentValues initialValues) throws RemoteException {
            return MockContentProvider.this.insert(url, initialValues);
        }

        public AssetFileDescriptor openAssetFile(String callingPackage, Uri url, String mode, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
            return MockContentProvider.this.openAssetFile(url, mode);
        }

        public ParcelFileDescriptor openFile(String callingPackage, Uri url, String mode, ICancellationSignal signal, IBinder callerToken) throws RemoteException, FileNotFoundException {
            return MockContentProvider.this.openFile(url, mode);
        }

        public Cursor query(String callingPackage, Uri url, String[] projection, Bundle queryArgs, ICancellationSignal cancellationSignal) throws RemoteException {
            return MockContentProvider.this.query(url, projection, queryArgs, null);
        }

        public int update(String callingPackage, Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException {
            return MockContentProvider.this.update(url, values, selection, selectionArgs);
        }

        public Bundle call(String callingPackage, String method, String request, Bundle args) throws RemoteException {
            return MockContentProvider.this.call(method, request, args);
        }

        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }

        public String[] getStreamTypes(Uri url, String mimeTypeFilter) throws RemoteException {
            return MockContentProvider.this.getStreamTypes(url, mimeTypeFilter);
        }

        public AssetFileDescriptor openTypedAssetFile(String callingPackage, Uri url, String mimeType, Bundle opts, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
            return MockContentProvider.this.openTypedAssetFile(url, mimeType, opts);
        }

        public ICancellationSignal createCancellationSignal() throws RemoteException {
            return null;
        }

        public Uri canonicalize(String callingPkg, Uri uri) throws RemoteException {
            return MockContentProvider.this.canonicalize(uri);
        }

        public Uri uncanonicalize(String callingPkg, Uri uri) throws RemoteException {
            return MockContentProvider.this.uncanonicalize(uri);
        }

        public boolean refresh(String callingPkg, Uri url, Bundle args, ICancellationSignal cancellationSignal) throws RemoteException {
            return MockContentProvider.this.refresh(url, args);
        }
    }

    protected MockContentProvider() {
        super(new MockContext(), "", "", null);
    }

    public MockContentProvider(Context context) {
        super(context, "", "", null);
    }

    public MockContentProvider(Context context, String readPermission, String writePermission, PathPermission[] pathPermissions) {
        super(context, readPermission, writePermission, pathPermissions);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public boolean onCreate() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public void attachInfo(Context context, ProviderInfo info) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Bundle call(String method, String request, Bundle args) {
        throw new UnsupportedOperationException("unimplemented mock method call");
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) {
        throw new UnsupportedOperationException("unimplemented mock method call");
    }

    public AssetFileDescriptor openTypedAssetFile(Uri url, String mimeType, Bundle opts) {
        throw new UnsupportedOperationException("unimplemented mock method call");
    }

    public boolean refresh(Uri url, Bundle args) {
        throw new UnsupportedOperationException("unimplemented mock method call");
    }

    public final IContentProvider getIContentProvider() {
        return this.mIContentProvider;
    }
}
