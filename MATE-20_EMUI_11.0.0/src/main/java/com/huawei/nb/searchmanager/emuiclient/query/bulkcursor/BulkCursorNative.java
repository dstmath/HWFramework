package com.huawei.nb.searchmanager.emuiclient.query.bulkcursor;

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
        attachInterface(this, IBulkCursor.DESCRIPTOR);
    }

    public static IBulkCursor asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IInterface iInterface = obj.queryLocalInterface(IBulkCursor.DESCRIPTOR);
        if (iInterface == null || !(iInterface instanceof IBulkCursor)) {
            return new BulkCursorProxy(obj);
        }
        return (IBulkCursor) iInterface;
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1) {
            data.enforceInterface(IBulkCursor.DESCRIPTOR);
            CursorWindow window = getWindow(data.readInt());
            reply.writeNoException();
            if (window == null) {
                reply.writeInt(0);
            } else {
                reply.writeInt(1);
                window.writeToParcel(reply, 1);
            }
            return true;
        } else if (code == 2) {
            data.enforceInterface(IBulkCursor.DESCRIPTOR);
            deactivate();
            reply.writeNoException();
            return true;
        } else if (code == 4) {
            data.enforceInterface(IBulkCursor.DESCRIPTOR);
            onMove(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == 5) {
            data.enforceInterface(IBulkCursor.DESCRIPTOR);
            Bundle extras = getExtras();
            reply.writeNoException();
            reply.writeBundle(extras);
            return true;
        } else if (code == 6) {
            data.enforceInterface(IBulkCursor.DESCRIPTOR);
            Bundle returnExtras = respond(data.readBundle());
            reply.writeNoException();
            reply.writeBundle(returnExtras);
            return true;
        } else if (code != 7) {
            return super.onTransact(code, data, reply, flags);
        } else {
            try {
                data.enforceInterface(IBulkCursor.DESCRIPTOR);
                close();
                reply.writeNoException();
                return true;
            } catch (Exception e) {
                DatabaseUtils.writeExceptionToParcel(reply, e);
                return true;
            }
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}
