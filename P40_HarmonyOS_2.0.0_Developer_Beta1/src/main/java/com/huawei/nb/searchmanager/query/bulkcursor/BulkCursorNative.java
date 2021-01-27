package com.huawei.nb.searchmanager.query.bulkcursor;

import android.database.CursorWindow;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class BulkCursorNative extends Binder implements IBulkCursor {
    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }

    public BulkCursorNative() {
        attachInterface(this, IBulkCursor.DESCRIPTOR);
    }

    public static IBulkCursor asInterface(IBinder iBinder) {
        if (iBinder == null) {
            return null;
        }
        IInterface queryLocalInterface = iBinder.queryLocalInterface(IBulkCursor.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IBulkCursor)) {
            return new BulkCursorProxy(iBinder);
        }
        return (IBulkCursor) queryLocalInterface;
    }

    @Override // android.os.Binder
    public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        parcel.enforceInterface(IBulkCursor.DESCRIPTOR);
        if (i == 1) {
            CursorWindow window = getWindow(parcel.readInt());
            parcel2.writeNoException();
            if (window == null) {
                parcel2.writeInt(0);
            } else {
                parcel2.writeInt(1);
                window.writeToParcel(parcel2, 1);
            }
            return true;
        } else if (i == 2) {
            deactivate();
            parcel2.writeNoException();
            return true;
        } else if (i == 4) {
            onMove(parcel.readInt());
            parcel2.writeNoException();
            return true;
        } else if (i == 5) {
            Bundle extras = getExtras();
            parcel2.writeNoException();
            parcel2.writeBundle(extras);
            return true;
        } else if (i == 6) {
            Bundle respond = respond(parcel.readBundle());
            parcel2.writeNoException();
            parcel2.writeBundle(respond);
            return true;
        } else if (i != 7) {
            return super.onTransact(i, parcel, parcel2, i2);
        } else {
            close();
            parcel2.writeNoException();
            return true;
        }
    }
}
