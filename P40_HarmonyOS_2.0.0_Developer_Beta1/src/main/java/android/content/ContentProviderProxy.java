package android.content;

import android.annotation.UnsupportedAppUsage;
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
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

/* access modifiers changed from: package-private */
/* compiled from: ContentProviderNative */
public final class ContentProviderProxy implements IContentProvider {
    @UnsupportedAppUsage
    private IBinder mRemote;

    public ContentProviderProxy(IBinder remote) {
        this.mRemote = remote;
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this.mRemote;
    }

    @Override // android.content.IContentProvider
    public Cursor query(String callingPkg, Uri url, String[] projection, Bundle queryArgs, ICancellationSignal cancellationSignal) throws RemoteException {
        BulkCursorToCursorAdaptor adaptor = new BulkCursorToCursorAdaptor();
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
            IBinder iBinder = null;
            data.writeStrongBinder(cancellationSignal != null ? cancellationSignal.asBinder() : null);
            this.mRemote.transact(1, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            if (reply.readInt() != 0) {
                BulkCursorDescriptor d = BulkCursorDescriptor.CREATOR.createFromParcel(reply);
                IBinder iBinder2 = this.mRemote;
                if (d.cursor != null) {
                    iBinder = d.cursor.asBinder();
                }
                Binder.copyAllowBlocking(iBinder2, iBinder);
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
            throw th;
        }
    }

    @Override // android.content.IContentProvider
    public String getType(Uri url) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            url.writeToParcel(data, 0);
            this.mRemote.transact(2, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return reply.readString();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
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
            return Uri.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
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
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public ContentProviderResult[] applyBatch(String callingPkg, String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            data.writeString(authority);
            data.writeInt(operations.size());
            Iterator<ContentProviderOperation> it = operations.iterator();
            while (it.hasNext()) {
                it.next().writeToParcel(data, 0);
            }
            this.mRemote.transact(20, data, reply, 0);
            DatabaseUtils.readExceptionWithOperationApplicationExceptionFromParcel(reply);
            return (ContentProviderResult[]) reply.createTypedArray(ContentProviderResult.CREATOR);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
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
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
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
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public ParcelFileDescriptor openFile(String callingPkg, Uri url, String mode, ICancellationSignal signal, IBinder token) throws RemoteException, FileNotFoundException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(mode);
            ParcelFileDescriptor fd = null;
            data.writeStrongBinder(signal != null ? signal.asBinder() : null);
            data.writeStrongBinder(token);
            this.mRemote.transact(14, data, reply, 0);
            DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
            if (reply.readInt() != 0) {
                fd = ParcelFileDescriptor.CREATOR.createFromParcel(reply);
            }
            return fd;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public AssetFileDescriptor openAssetFile(String callingPkg, Uri url, String mode, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(mode);
            AssetFileDescriptor fd = null;
            data.writeStrongBinder(signal != null ? signal.asBinder() : null);
            this.mRemote.transact(15, data, reply, 0);
            DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
            if (reply.readInt() != 0) {
                fd = AssetFileDescriptor.CREATOR.createFromParcel(reply);
            }
            return fd;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public Bundle call(String callingPkg, String authority, String method, String request, Bundle args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            data.writeString(authority);
            data.writeString(method);
            data.writeString(request);
            data.writeBundle(args);
            this.mRemote.transact(21, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return reply.readBundle();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public String[] getStreamTypes(Uri url, String mimeTypeFilter) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            url.writeToParcel(data, 0);
            data.writeString(mimeTypeFilter);
            this.mRemote.transact(22, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return reply.createStringArray();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public AssetFileDescriptor openTypedAssetFile(String callingPkg, Uri url, String mimeType, Bundle opts, ICancellationSignal signal) throws RemoteException, FileNotFoundException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            data.writeString(mimeType);
            data.writeBundle(opts);
            AssetFileDescriptor fd = null;
            data.writeStrongBinder(signal != null ? signal.asBinder() : null);
            this.mRemote.transact(23, data, reply, 0);
            DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(reply);
            if (reply.readInt() != 0) {
                fd = AssetFileDescriptor.CREATOR.createFromParcel(reply);
            }
            return fd;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public ICancellationSignal createCancellationSignal() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            this.mRemote.transact(24, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return ICancellationSignal.Stub.asInterface(reply.readStrongBinder());
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public Uri canonicalize(String callingPkg, Uri url) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            this.mRemote.transact(25, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return Uri.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public Uri uncanonicalize(String callingPkg, Uri url) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            url.writeToParcel(data, 0);
            this.mRemote.transact(26, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return Uri.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.IContentProvider
    public boolean refresh(String callingPkg, Uri url, Bundle args, ICancellationSignal signal) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IContentProvider.descriptor);
            data.writeString(callingPkg);
            boolean z = false;
            url.writeToParcel(data, 0);
            data.writeBundle(args);
            data.writeStrongBinder(signal != null ? signal.asBinder() : null);
            this.mRemote.transact(27, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            if (reply.readInt() == 0) {
                z = true;
            }
            return z;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
