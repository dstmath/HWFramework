package com.huawei.nb.searchmanager.emuiclient.query.bulkcursor;

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

    public BulkCursorProxy(IBinder remote) {
        this.mRemote = remote;
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this.mRemote;
    }

    @Override // com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.IBulkCursor
    public CursorWindow getWindow(int position) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            data.writeInt(position);
            this.mRemote.transact(1, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            CursorWindow window = null;
            if (reply.readInt() == 1) {
                window = CursorWindow.newFromParcel(reply);
            }
            return window;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.IBulkCursor
    public void onMove(int position) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            data.writeInt(position);
            this.mRemote.transact(4, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.IBulkCursor
    public void deactivate() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            this.mRemote.transact(2, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.IBulkCursor
    public void close() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            this.mRemote.transact(7, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.IBulkCursor
    public Bundle getExtras() throws RemoteException {
        if (this.mExtras == null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
                this.mRemote.transact(5, data, reply, 0);
                DatabaseUtils.readExceptionFromParcel(reply);
                this.mExtras = reply.readBundle();
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
        return this.mExtras;
    }

    @Override // com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.IBulkCursor
    public Bundle respond(Bundle extras) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.DESCRIPTOR);
            data.writeBundle(extras);
            this.mRemote.transact(6, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            return reply.readBundle();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
