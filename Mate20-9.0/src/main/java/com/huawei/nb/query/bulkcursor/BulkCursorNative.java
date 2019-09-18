package com.huawei.nb.query.bulkcursor;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class BulkCursorNative extends Binder implements IBulkCursor {
    public BulkCursorNative() {
        attachInterface(this, "android.content.IBulkCursor");
    }

    public static IBulkCursor asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IInterface iInterface = obj.queryLocalInterface("android.content.IBulkCursor");
        if (iInterface == null || !(iInterface instanceof IBulkCursor)) {
            return new BulkCursorProxy(obj);
        }
        return (IBulkCursor) iInterface;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1:
                try {
                    data.enforceInterface("android.content.IBulkCursor");
                    CursorWindow window = getWindow(data.readInt());
                    reply.writeNoException();
                    if (window == null) {
                        reply.writeInt(0);
                        return true;
                    }
                    reply.writeInt(1);
                    window.writeToParcel(reply, 1);
                    return true;
                } catch (Exception e) {
                    DatabaseUtils.writeExceptionToParcel(reply, e);
                    return true;
                }
            case 2:
                data.enforceInterface("android.content.IBulkCursor");
                deactivate();
                reply.writeNoException();
                return true;
            case 4:
                data.enforceInterface("android.content.IBulkCursor");
                onMove(data.readInt());
                reply.writeNoException();
                return true;
            case 5:
                data.enforceInterface("android.content.IBulkCursor");
                Bundle extras = getExtras();
                reply.writeNoException();
                reply.writeBundle(extras);
                return true;
            case 6:
                data.enforceInterface("android.content.IBulkCursor");
                Bundle returnExtras = respond(data.readBundle());
                reply.writeNoException();
                reply.writeBundle(returnExtras);
                return true;
            case 7:
                data.enforceInterface("android.content.IBulkCursor");
                close();
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public IBinder asBinder() {
        return this;
    }
}
