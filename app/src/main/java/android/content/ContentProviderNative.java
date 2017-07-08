package android.content;

import android.content.res.AssetFileDescriptor;
import android.database.BulkCursorDescriptor;
import android.database.Cursor;
import android.database.CursorToBulkCursorAdaptor;
import android.database.DatabaseUtils;
import android.database.IContentObserver;
import android.database.IContentObserver.Stub;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.rms.HwSysResource;
import android.service.notification.NotificationRankerService;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
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

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Throwable th;
        String callingPkg;
        Uri url;
        int i;
        Uri out;
        int count;
        AssetFileDescriptor fd;
        switch (code) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                try {
                    data.enforceInterface(IContentProvider.descriptor);
                    callingPkg = data.readString();
                    url = (Uri) Uri.CREATOR.createFromParcel(data);
                    int num = data.readInt();
                    String[] strArr = null;
                    if (num > 0) {
                        strArr = new String[num];
                        for (i = 0; i < num; i++) {
                            strArr[i] = data.readString();
                        }
                    }
                    String selection = data.readString();
                    num = data.readInt();
                    String[] strArr2 = null;
                    if (num > 0) {
                        strArr2 = new String[num];
                        for (i = 0; i < num; i++) {
                            strArr2[i] = data.readString();
                        }
                    }
                    String sortOrder = data.readString();
                    IContentObserver observer = Stub.asInterface(data.readStrongBinder());
                    Cursor cursor = query(callingPkg, url, strArr, selection, strArr2, sortOrder, ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                    if (cursor != null) {
                        CursorToBulkCursorAdaptor cursorToBulkCursorAdaptor = null;
                        try {
                            CursorToBulkCursorAdaptor cursorToBulkCursorAdaptor2 = new CursorToBulkCursorAdaptor(cursor, observer, getProviderName());
                            cursor = null;
                            try {
                                BulkCursorDescriptor d = cursorToBulkCursorAdaptor2.getBulkCursorDescriptor();
                                cursorToBulkCursorAdaptor = null;
                                reply.writeNoException();
                                reply.writeInt(1);
                                d.writeToParcel(reply, 1);
                            } catch (Throwable th2) {
                                th = th2;
                                cursorToBulkCursorAdaptor = cursorToBulkCursorAdaptor2;
                                if (cursorToBulkCursorAdaptor != null) {
                                    cursorToBulkCursorAdaptor.close();
                                }
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursorToBulkCursorAdaptor != null) {
                                cursorToBulkCursorAdaptor.close();
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    reply.writeNoException();
                    reply.writeInt(0);
                    return true;
                } catch (Exception e) {
                    DatabaseUtils.writeExceptionToParcel(reply, e);
                    return true;
                }
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                data.enforceInterface(IContentProvider.descriptor);
                String type = getType((Uri) Uri.CREATOR.createFromParcel(data));
                reply.writeNoException();
                reply.writeString(type);
                return true;
            case Engine.DEFAULT_STREAM /*3*/:
                data.enforceInterface(IContentProvider.descriptor);
                out = insert(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data), (ContentValues) ContentValues.CREATOR.createFromParcel(data));
                reply.writeNoException();
                Uri.writeToParcel(reply, out);
                return true;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                data.enforceInterface(IContentProvider.descriptor);
                count = delete(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data), data.readString(), data.readStringArray());
                reply.writeNoException();
                reply.writeInt(count);
                return true;
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                data.enforceInterface(IContentProvider.descriptor);
                count = update(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data), (ContentValues) ContentValues.CREATOR.createFromParcel(data), data.readString(), data.readStringArray());
                reply.writeNoException();
                reply.writeInt(count);
                return true;
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                data.enforceInterface(IContentProvider.descriptor);
                count = bulkInsert(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data), (ContentValues[]) data.createTypedArray(ContentValues.CREATOR));
                reply.writeNoException();
                reply.writeInt(count);
                return true;
            case NotificationRankerService.REASON_PACKAGE_SUSPENDED /*14*/:
                data.enforceInterface(IContentProvider.descriptor);
                String readString = data.readString();
                Uri uri = (Uri) Uri.CREATOR.createFromParcel(data);
                ParcelFileDescriptor fd2 = openFile(callingPkg, url, data.readString(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                reply.writeNoException();
                if (fd2 != null) {
                    reply.writeInt(1);
                    fd2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case NotificationRankerService.REASON_PROFILE_TURNED_OFF /*15*/:
                data.enforceInterface(IContentProvider.descriptor);
                fd = openAssetFile(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data), data.readString(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                if (fd != null) {
                    reply.writeInt(1);
                    fd.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case HwSysResource.MEMORY /*20*/:
                data.enforceInterface(IContentProvider.descriptor);
                callingPkg = data.readString();
                int numOperations = data.readInt();
                ArrayList<ContentProviderOperation> arrayList = new ArrayList(numOperations);
                for (i = 0; i < numOperations; i++) {
                    arrayList.add(i, (ContentProviderOperation) ContentProviderOperation.CREATOR.createFromParcel(data));
                }
                ContentProviderResult[] results = applyBatch(callingPkg, arrayList);
                reply.writeNoException();
                reply.writeTypedArray(results, 0);
                return true;
            case HwSysResource.CPU /*21*/:
                data.enforceInterface(IContentProvider.descriptor);
                Bundle responseBundle = call(data.readString(), data.readString(), data.readString(), data.readBundle());
                reply.writeNoException();
                reply.writeBundle(responseBundle);
                return true;
            case HwSysResource.IO /*22*/:
                data.enforceInterface(IContentProvider.descriptor);
                String[] types = getStreamTypes((Uri) Uri.CREATOR.createFromParcel(data), data.readString());
                reply.writeNoException();
                reply.writeStringArray(types);
                return true;
            case HwSysResource.SCHEDGROUP /*23*/:
                data.enforceInterface(IContentProvider.descriptor);
                String readString2 = data.readString();
                Uri uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                fd = openTypedAssetFile(callingPkg, url, data.readString(), data.readBundle(), ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                if (fd != null) {
                    reply.writeInt(1);
                    fd.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case HwSysResource.ANR /*24*/:
                data.enforceInterface(IContentProvider.descriptor);
                ICancellationSignal cancellationSignal = createCancellationSignal();
                reply.writeNoException();
                reply.writeStrongBinder(cancellationSignal.asBinder());
                return true;
            case HwSysResource.DELAY /*25*/:
                data.enforceInterface(IContentProvider.descriptor);
                out = canonicalize(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data));
                reply.writeNoException();
                Uri.writeToParcel(reply, out);
                return true;
            case HwSysResource.FRAMELOST /*26*/:
                data.enforceInterface(IContentProvider.descriptor);
                out = uncanonicalize(data.readString(), (Uri) Uri.CREATOR.createFromParcel(data));
                reply.writeNoException();
                Uri.writeToParcel(reply, out);
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public IBinder asBinder() {
        return this;
    }
}
