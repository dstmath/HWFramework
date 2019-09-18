package android.test.mock;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.EntityIterator;
import android.content.IContentProvider;
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

public class MockIContentProvider implements IContentProvider {
    public int bulkInsert(String callingPackage, Uri url, ContentValues[] initialValues) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public int delete(String callingPackage, Uri url, String selection, String[] selectionArgs) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public String getType(Uri url) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Uri insert(String callingPackage, Uri url, ContentValues initialValues) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public ParcelFileDescriptor openFile(String callingPackage, Uri url, String mode, ICancellationSignal signal, IBinder callerToken) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public AssetFileDescriptor openAssetFile(String callingPackage, Uri uri, String mode, ICancellationSignal signal) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public ContentProviderResult[] applyBatch(String callingPackage, ArrayList<ContentProviderOperation> arrayList) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Cursor query(String callingPackage, Uri url, String[] projection, Bundle queryArgs, ICancellationSignal cancellationSignal) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public EntityIterator queryEntities(Uri url, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public int update(String callingPackage, Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Bundle call(String callingPackage, String method, String request, Bundle args) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public IBinder asBinder() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public AssetFileDescriptor openTypedAssetFile(String callingPackage, Uri url, String mimeType, Bundle opts, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public ICancellationSignal createCancellationSignal() throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Uri canonicalize(String callingPkg, Uri uri) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public Uri uncanonicalize(String callingPkg, Uri uri) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    public boolean refresh(String callingPkg, Uri url, Bundle args, ICancellationSignal cancellationSignal) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
}
