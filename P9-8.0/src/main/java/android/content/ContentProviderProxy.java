package android.content;

import android.content.res.AssetFileDescriptor;
import android.database.BulkCursorDescriptor;
import android.database.BulkCursorToCursorAdaptor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.ICancellationSignal.Stub;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/* compiled from: ContentProviderNative */
final class ContentProviderProxy implements IContentProvider {
    private IBinder mRemote;

    public ContentProviderProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public Cursor query(String callingPkg, Uri url, String[] projection, Bundle queryArgs, ICancellationSignal cancellationSignal) throws RemoteException {
        Cursor adaptor = new BulkCursorToCursorAdaptor();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            int length = 0;
            if (projection != null) {
                length = projection.length;
            }
            data.writeInt(length);
            for (int i = 0; i < length; i++) {
                data.writeString(projection[i]);
            }
            data.writeBundle(queryArgs);
            data.writeStrongBinder(adaptor.getObserver().asBinder());
            data.writeStrongBinder(cancellationSignal != null ? cancellationSignal.asBinder() : null);
            this.mRemote.transact(1, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            if (reply.readInt() != 0) {
                BulkCursorDescriptor d = (BulkCursorDescriptor) BulkCursorDescriptor.CREATOR.createFromParcel(reply);
                Binder.copyAllowBlocking(this.mRemote, d.cursor != null ? d.cursor.asBinder() : null);
                adaptor.initialize(d);
            } else {
                adaptor.close();
                adaptor = null;
            }
            data.recycle();
            reply.recycle();
            return adaptor;
        } catch (RemoteException ex) {
            adaptor.close();
            throw ex;
        } catch (RuntimeException ex2) {
            adaptor.close();
            throw ex2;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public String getType(Uri url) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            url.writeToParcel(data, 0);
            this.mRemote.transact(2, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            String out = reply.readString();
            return out;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public Uri insert(String callingPkg, Uri url, ContentValues values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            values.writeToParcel(data, 0);
            this.mRemote.transact(3, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            Uri out = (Uri) Uri.CREATOR.createFromParcel(reply);
            return out;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int bulkInsert(String callingPkg, Uri url, ContentValues[] values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeTypedArray(values, 0);
            this.mRemote.transact(13, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            int count = reply.readInt();
            return count;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ContentProviderResult[] applyBatch(String callingPkg, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            data.writeInt(operations.size());
            for (ContentProviderOperation operation : operations) {
                operation.writeToParcel(data, 0);
            }
            this.mRemote.transact(20, data, reply, 0);
            DatabaseUtils.readExceptionWithOperationApplicationExceptionFromParcel(reply);
            ContentProviderResult[] results = (ContentProviderResult[]) reply.createTypedArray(ContentProviderResult.CREATOR);
            return results;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int delete(String callingPkg, Uri url, String selection, String[] selectionArgs) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(selection);
            data.writeStringArray(selectionArgs);
            this.mRemote.transact(4, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            int count = reply.readInt();
            return count;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int update(String callingPkg, Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            values.writeToParcel(data, 0);
            data.writeString(selection);
            data.writeStringArray(selectionArgs);
            this.mRemote.transact(10, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            int count = reply.readInt();
            return count;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ParcelFileDescriptor openFile(String callingPkg, Uri url, String mode, ICancellationSignal signal, IBinder token) throws RemoteException, FileNotFoundException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            ParcelFileDescriptor fd;
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(mode);
            if (signal != null) {
                iBinder = signal.asBinder();
            }
            data.writeStrongBinder(iBinder);
            data.writeStrongBinder(token);
            this.mRemote.transact(14, data, reply, 0);
            DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
            if (reply.readInt() != 0) {
                fd = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(reply);
            } else {
                fd = null;
            }
            data.recycle();
            reply.recycle();
            return fd;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public AssetFileDescriptor openAssetFile(String callingPkg, Uri url, String mode, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(mode);
            if (signal != null) {
                iBinder = signal.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(15, data, reply, 0);
            DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
            AssetFileDescriptor fd = reply.readInt() != 0 ? (AssetFileDescriptor) AssetFileDescriptor.CREATOR.createFromParcel(reply) : null;
            data.recycle();
            reply.recycle();
            return fd;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public Bundle call(String callingPkg, String method, String request, Bundle args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            data.writeString(method);
            data.writeString(request);
            data.writeBundle(args);
            this.mRemote.transact(21, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            Bundle bundle = reply.readBundle();
            return bundle;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            url.writeToParcel(data, 0);
            data.writeString(mimeTypeFilter);
            this.mRemote.transact(22, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            String[] out = reply.createStringArray();
            return out;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public AssetFileDescriptor openTypedAssetFile(String callingPkg, Uri url, String mimeType, Bundle opts, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(mimeType);
            data.writeBundle(opts);
            if (signal != null) {
                iBinder = signal.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(23, data, reply, 0);
            DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
            AssetFileDescriptor fd = reply.readInt() != 0 ? (AssetFileDescriptor) AssetFileDescriptor.CREATOR.createFromParcel(reply) : null;
            data.recycle();
            reply.recycle();
            return fd;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public ICancellationSignal createCancellationSignal() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            this.mRemote.transact(24, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            ICancellationSignal cancellationSignal = Stub.asInterface(reply.readStrongBinder());
            return cancellationSignal;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public Uri canonicalize(String callingPkg, Uri url) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            this.mRemote.transact(25, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            Uri out = (Uri) Uri.CREATOR.createFromParcel(reply);
            return out;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public Uri uncanonicalize(String callingPkg, Uri url) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            this.mRemote.transact(26, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            Uri out = (Uri) Uri.CREATOR.createFromParcel(reply);
            return out;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean refresh(String callingPkg, Uri url, Bundle args, ICancellationSignal signal) throws RemoteException {
        IBinder iBinder = null;
        boolean z = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeBundle(args);
            if (signal != null) {
                iBinder = signal.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(27, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            if (reply.readInt() == 0) {
                z = true;
            }
            data.recycle();
            reply.recycle();
            return z;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }
}
