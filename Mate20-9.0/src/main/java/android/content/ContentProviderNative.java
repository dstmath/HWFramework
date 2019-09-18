package android.content;

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

    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0295, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0296, code lost:
        if (r4 != null) goto L_0x0298;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0298, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x029b, code lost:
        if (r1 != null) goto L_0x029d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x029d, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x02a0, code lost:
        throw r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x001f, B:62:0x0271] */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int i = code;
        Parcel parcel = data;
        Parcel parcel2 = reply;
        if (i != 10) {
            int i2 = 0;
            switch (i) {
                case 1:
                    parcel.enforceInterface(IContentProvider.descriptor);
                    String callingPkg = data.readString();
                    Uri url = Uri.CREATOR.createFromParcel(parcel);
                    int num = data.readInt();
                    String[] projection = null;
                    if (num > 0) {
                        projection = new String[num];
                        for (int i3 = 0; i3 < num; i3++) {
                            projection[i3] = data.readString();
                        }
                    }
                    Bundle queryArgs = data.readBundle();
                    IContentObserver observer = IContentObserver.Stub.asInterface(data.readStrongBinder());
                    Cursor cursor = query(callingPkg, url, projection, queryArgs, ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                    if (cursor != null) {
                        CursorToBulkCursorAdaptor adaptor = null;
                        cursor = null;
                        BulkCursorDescriptor d = new CursorToBulkCursorAdaptor(cursor, observer, getProviderName()).getBulkCursorDescriptor();
                        adaptor = null;
                        reply.writeNoException();
                        parcel2.writeInt(1);
                        d.writeToParcel(parcel2, 1);
                        if (adaptor != null) {
                            adaptor.close();
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } else {
                        reply.writeNoException();
                        parcel2.writeInt(0);
                    }
                    return true;
                case 2:
                    parcel.enforceInterface(IContentProvider.descriptor);
                    String type = getType(Uri.CREATOR.createFromParcel(parcel));
                    reply.writeNoException();
                    parcel2.writeString(type);
                    return true;
                case 3:
                    parcel.enforceInterface(IContentProvider.descriptor);
                    Uri out = insert(data.readString(), Uri.CREATOR.createFromParcel(parcel), ContentValues.CREATOR.createFromParcel(parcel));
                    reply.writeNoException();
                    Uri.writeToParcel(parcel2, out);
                    return true;
                case 4:
                    parcel.enforceInterface(IContentProvider.descriptor);
                    int count = delete(data.readString(), Uri.CREATOR.createFromParcel(parcel), data.readString(), data.readStringArray());
                    reply.writeNoException();
                    parcel2.writeInt(count);
                    return true;
                default:
                    switch (i) {
                        case 13:
                            parcel.enforceInterface(IContentProvider.descriptor);
                            int count2 = bulkInsert(data.readString(), Uri.CREATOR.createFromParcel(parcel), (ContentValues[]) parcel.createTypedArray(ContentValues.CREATOR));
                            reply.writeNoException();
                            parcel2.writeInt(count2);
                            return true;
                        case 14:
                            parcel.enforceInterface(IContentProvider.descriptor);
                            ParcelFileDescriptor fd = openFile(data.readString(), Uri.CREATOR.createFromParcel(parcel), data.readString(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                            reply.writeNoException();
                            if (fd != null) {
                                parcel2.writeInt(1);
                                fd.writeToParcel(parcel2, 1);
                            } else {
                                parcel2.writeInt(0);
                            }
                            return true;
                        case 15:
                            parcel.enforceInterface(IContentProvider.descriptor);
                            AssetFileDescriptor fd2 = openAssetFile(data.readString(), Uri.CREATOR.createFromParcel(parcel), data.readString(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            if (fd2 != null) {
                                parcel2.writeInt(1);
                                fd2.writeToParcel(parcel2, 1);
                            } else {
                                parcel2.writeInt(0);
                            }
                            return true;
                        default:
                            switch (i) {
                                case 20:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    String callingPkg2 = data.readString();
                                    int numOperations = data.readInt();
                                    ArrayList<ContentProviderOperation> operations = new ArrayList<>(numOperations);
                                    for (int i4 = 0; i4 < numOperations; i4++) {
                                        operations.add(i4, ContentProviderOperation.CREATOR.createFromParcel(parcel));
                                    }
                                    ContentProviderResult[] results = applyBatch(callingPkg2, operations);
                                    reply.writeNoException();
                                    parcel2.writeTypedArray(results, 0);
                                    return true;
                                case 21:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    Bundle responseBundle = call(data.readString(), data.readString(), data.readString(), data.readBundle());
                                    reply.writeNoException();
                                    parcel2.writeBundle(responseBundle);
                                    return true;
                                case 22:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    String[] types = getStreamTypes(Uri.CREATOR.createFromParcel(parcel), data.readString());
                                    reply.writeNoException();
                                    parcel2.writeStringArray(types);
                                    return true;
                                case 23:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    AssetFileDescriptor fd3 = openTypedAssetFile(data.readString(), Uri.CREATOR.createFromParcel(parcel), data.readString(), data.readBundle(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                                    reply.writeNoException();
                                    if (fd3 != null) {
                                        parcel2.writeInt(1);
                                        fd3.writeToParcel(parcel2, 1);
                                    } else {
                                        parcel2.writeInt(0);
                                    }
                                    return true;
                                case 24:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    ICancellationSignal cancellationSignal = createCancellationSignal();
                                    reply.writeNoException();
                                    parcel2.writeStrongBinder(cancellationSignal.asBinder());
                                    return true;
                                case 25:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    Uri out2 = canonicalize(data.readString(), Uri.CREATOR.createFromParcel(parcel));
                                    reply.writeNoException();
                                    Uri.writeToParcel(parcel2, out2);
                                    return true;
                                case 26:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    Uri out3 = uncanonicalize(data.readString(), Uri.CREATOR.createFromParcel(parcel));
                                    reply.writeNoException();
                                    Uri.writeToParcel(parcel2, out3);
                                    return true;
                                case 27:
                                    parcel.enforceInterface(IContentProvider.descriptor);
                                    boolean out4 = refresh(data.readString(), Uri.CREATOR.createFromParcel(parcel), data.readBundle(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                                    reply.writeNoException();
                                    if (!out4) {
                                        i2 = -1;
                                    }
                                    parcel2.writeInt(i2);
                                    return true;
                                default:
                                    return super.onTransact(code, data, reply, flags);
                            }
                    }
            }
            DatabaseUtils.writeExceptionToParcel(parcel2, e);
            return true;
        }
        parcel.enforceInterface(IContentProvider.descriptor);
        int count3 = update(data.readString(), Uri.CREATOR.createFromParcel(parcel), ContentValues.CREATOR.createFromParcel(parcel), data.readString(), data.readStringArray());
        reply.writeNoException();
        parcel2.writeInt(count3);
        return true;
    }

    public IBinder asBinder() {
        return this;
    }
}
