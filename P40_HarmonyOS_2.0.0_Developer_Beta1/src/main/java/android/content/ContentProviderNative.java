package android.content;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetFileDescriptor;
import android.database.BulkCursorDescriptor;
import android.database.Cursor;
import android.database.CursorToBulkCursorAdaptor;
import android.database.DatabaseUtils;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.util.ArrayList;

public abstract class ContentProviderNative extends Binder implements IContentProvider {
    public abstract String getProviderName();

    public ContentProviderNative() {
        attachInterface(this, IContentProvider.descriptor);
    }

    @UnsupportedAppUsage
    public static IContentProvider asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IContentProvider in = (IContentProvider) obj.queryLocalInterface(IContentProvider.descriptor);
        if (in != null) {
            return in;
        }
        return new ContentProviderProxy(obj);
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        String[] projection;
        Cursor cursor;
        int i = 0;
        if (code == 1) {
            data.enforceInterface(IContentProvider.descriptor);
            String callingPkg = data.readString();
            Uri url = Uri.CREATOR.createFromParcel(data);
            int num = data.readInt();
            if (num > 0) {
                String[] projection2 = new String[num];
                for (int i2 = 0; i2 < num; i2++) {
                    projection2[i2] = data.readString();
                }
                projection = projection2;
            } else {
                projection = null;
            }
            Bundle queryArgs = data.readBundle();
            IContentObserver observer = IContentObserver.Stub.asInterface(data.readStrongBinder());
            Cursor cursor2 = query(callingPkg, url, projection, queryArgs, ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
            if (cursor2 != null) {
                CursorToBulkCursorAdaptor adaptor = null;
                try {
                    CursorToBulkCursorAdaptor adaptor2 = new CursorToBulkCursorAdaptor(cursor2, observer, getProviderName());
                    cursor = null;
                    BulkCursorDescriptor d = adaptor2.getBulkCursorDescriptor();
                    adaptor = null;
                    reply.writeNoException();
                    reply.writeInt(1);
                    d.writeToParcel(reply, 1);
                } finally {
                    if (adaptor != null) {
                        adaptor.close();
                    }
                    if (0 != 0) {
                        cursor.close();
                    }
                }
            } else {
                reply.writeNoException();
                reply.writeInt(0);
            }
            return true;
        } else if (code == 2) {
            data.enforceInterface(IContentProvider.descriptor);
            String type = getType(Uri.CREATOR.createFromParcel(data));
            reply.writeNoException();
            reply.writeString(type);
            return true;
        } else if (code == 3) {
            data.enforceInterface(IContentProvider.descriptor);
            Uri out = insert(data.readString(), Uri.CREATOR.createFromParcel(data), ContentValues.CREATOR.createFromParcel(data));
            reply.writeNoException();
            Uri.writeToParcel(reply, out);
            return true;
        } else if (code == 4) {
            data.enforceInterface(IContentProvider.descriptor);
            int count = delete(data.readString(), Uri.CREATOR.createFromParcel(data), data.readString(), data.readStringArray());
            reply.writeNoException();
            reply.writeInt(count);
            return true;
        } else if (code != 10) {
            switch (code) {
                case 13:
                    data.enforceInterface(IContentProvider.descriptor);
                    int count2 = bulkInsert(data.readString(), Uri.CREATOR.createFromParcel(data), (ContentValues[]) data.createTypedArray(ContentValues.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(count2);
                    return true;
                case 14:
                    data.enforceInterface(IContentProvider.descriptor);
                    ParcelFileDescriptor fd = openFile(data.readString(), Uri.CREATOR.createFromParcel(data), data.readString(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                    reply.writeNoException();
                    if (fd != null) {
                        reply.writeInt(1);
                        fd.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 15:
                    data.enforceInterface(IContentProvider.descriptor);
                    AssetFileDescriptor fd2 = openAssetFile(data.readString(), Uri.CREATOR.createFromParcel(data), data.readString(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (fd2 != null) {
                        reply.writeInt(1);
                        fd2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                default:
                    switch (code) {
                        case 20:
                            data.enforceInterface(IContentProvider.descriptor);
                            String callingPkg2 = data.readString();
                            String authority = data.readString();
                            int numOperations = data.readInt();
                            ArrayList<ContentProviderOperation> operations = new ArrayList<>(numOperations);
                            for (int i3 = 0; i3 < numOperations; i3++) {
                                operations.add(i3, ContentProviderOperation.CREATOR.createFromParcel(data));
                            }
                            ContentProviderResult[] results = applyBatch(callingPkg2, authority, operations);
                            reply.writeNoException();
                            reply.writeTypedArray(results, 0);
                            return true;
                        case 21:
                            data.enforceInterface(IContentProvider.descriptor);
                            Bundle responseBundle = call(data.readString(), data.readString(), data.readString(), data.readString(), data.readBundle());
                            reply.writeNoException();
                            reply.writeBundle(responseBundle);
                            return true;
                        case 22:
                            data.enforceInterface(IContentProvider.descriptor);
                            String[] types = getStreamTypes(Uri.CREATOR.createFromParcel(data), data.readString());
                            reply.writeNoException();
                            reply.writeStringArray(types);
                            return true;
                        case 23:
                            data.enforceInterface(IContentProvider.descriptor);
                            AssetFileDescriptor fd3 = openTypedAssetFile(data.readString(), Uri.CREATOR.createFromParcel(data), data.readString(), data.readBundle(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            if (fd3 != null) {
                                reply.writeInt(1);
                                fd3.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        case 24:
                            data.enforceInterface(IContentProvider.descriptor);
                            ICancellationSignal cancellationSignal = createCancellationSignal();
                            reply.writeNoException();
                            reply.writeStrongBinder(cancellationSignal.asBinder());
                            return true;
                        case 25:
                            data.enforceInterface(IContentProvider.descriptor);
                            Uri out2 = canonicalize(data.readString(), Uri.CREATOR.createFromParcel(data));
                            reply.writeNoException();
                            Uri.writeToParcel(reply, out2);
                            return true;
                        case 26:
                            data.enforceInterface(IContentProvider.descriptor);
                            Uri out3 = uncanonicalize(data.readString(), Uri.CREATOR.createFromParcel(data));
                            reply.writeNoException();
                            Uri.writeToParcel(reply, out3);
                            return true;
                        case 27:
                            try {
                                data.enforceInterface(IContentProvider.descriptor);
                                boolean out4 = refresh(data.readString(), Uri.CREATOR.createFromParcel(data), data.readBundle(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                                reply.writeNoException();
                                if (!out4) {
                                    i = -1;
                                }
                                reply.writeInt(i);
                                return true;
                            } catch (Exception e) {
                                DatabaseUtils.writeExceptionToParcel(reply, e);
                                return true;
                            }
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        } else {
            data.enforceInterface(IContentProvider.descriptor);
            int count3 = update(data.readString(), Uri.CREATOR.createFromParcel(data), ContentValues.CREATOR.createFromParcel(data), data.readString(), data.readStringArray());
            reply.writeNoException();
            reply.writeInt(count3);
            return true;
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}
