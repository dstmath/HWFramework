package com.huawei.nb.query.bulkcursor;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/* access modifiers changed from: package-private */
/* compiled from: BulkCursorNative */
public final class BulkCursorProxy implements IBulkCursor {
    private Bundle mExtras = null;
    private IBinder mRemote;

    BulkCursorProxy(IBinder iBinder) {
        this.mRemote = iBinder;
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this.mRemote;
    }

    @Override // com.huawei.nb.query.bulkcursor.IBulkCursor
    public CursorWindow getWindow(int i) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            obtain.writeInt(i);
            this.mRemote.transact(1, obtain, obtain2, 0);
            DatabaseUtils.readExceptionFromParcel(obtain2);
            CursorWindow cursorWindow = null;
            if (obtain2.readInt() == 1) {
                cursorWindow = CursorWindow.newFromParcel(obtain2);
            }
            return cursorWindow;
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    @Override // com.huawei.nb.query.bulkcursor.IBulkCursor
    public void onMove(int i) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            obtain.writeInt(i);
            this.mRemote.transact(4, obtain, obtain2, 0);
            DatabaseUtils.readExceptionFromParcel(obtain2);
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    @Override // com.huawei.nb.query.bulkcursor.IBulkCursor
    public void deactivate() throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            this.mRemote.transact(2, obtain, obtain2, 0);
            DatabaseUtils.readExceptionFromParcel(obtain2);
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    @Override // com.huawei.nb.query.bulkcursor.IBulkCursor
    public void close() throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            this.mRemote.transact(7, obtain, obtain2, 0);
            DatabaseUtils.readExceptionFromParcel(obtain2);
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    @Override // com.huawei.nb.query.bulkcursor.IBulkCursor
    public Bundle getExtras() throws RemoteException {
        if (this.mExtras == null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
                this.mRemote.transact(5, obtain, obtain2, 0);
                DatabaseUtils.readExceptionFromParcel(obtain2);
                this.mExtras = obtain2.readBundle();
            } finally {
                obtain.recycle();
                obtain2.recycle();
            }
        }
        return this.mExtras;
    }

    @Override // com.huawei.nb.query.bulkcursor.IBulkCursor
    public Bundle respond(Bundle bundle) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            obtain.writeBundle(bundle);
            this.mRemote.transact(6, obtain, obtain2, 0);
            DatabaseUtils.readExceptionFromParcel(obtain2);
            return obtain2.readBundle();
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }
}
