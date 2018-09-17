package android.database;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: BulkCursorNative */
final class BulkCursorProxy implements IBulkCursor {
    private Bundle mExtras = null;
    private IBinder mRemote;

    public BulkCursorProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public CursorWindow getWindow(int position) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.descriptor);
            data.writeInt(position);
            this.mRemote.transact(1, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            CursorWindow window = null;
            if (reply.readInt() == 1) {
                window = CursorWindow.newFromParcel(reply);
            }
            data.recycle();
            reply.recycle();
            return window;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public void onMove(int position) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.descriptor);
            data.writeInt(position);
            this.mRemote.transact(4, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void deactivate() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.descriptor);
            this.mRemote.transact(2, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void close() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.descriptor);
            this.mRemote.transact(7, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int requery(IContentObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            int count;
            data.writeInterfaceToken(IBulkCursor.descriptor);
            data.writeStrongInterface(observer);
            boolean result = this.mRemote.transact(3, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            if (result) {
                count = reply.readInt();
                this.mExtras = reply.readBundle();
            } else {
                count = -1;
            }
            data.recycle();
            reply.recycle();
            return count;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public Bundle getExtras() throws RemoteException {
        if (this.mExtras == null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IBulkCursor.descriptor);
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

    public Bundle respond(Bundle extras) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IBulkCursor.descriptor);
            data.writeBundle(extras);
            this.mRemote.transact(6, data, reply, 0);
            DatabaseUtils.readExceptionFromParcel(reply);
            Bundle returnExtras = reply.readBundle();
            return returnExtras;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
