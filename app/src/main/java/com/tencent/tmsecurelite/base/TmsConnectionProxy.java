package com.tencent.tmsecurelite.base;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.tencent.tmsecurelite.commom.ITmsCallback;

public final class TmsConnectionProxy implements ITmsConnection {
    private IBinder mRemote;

    public TmsConnectionProxy(IBinder iBinder) {
        this.mRemote = iBinder;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public boolean checkVersion(int i) throws RemoteException {
        boolean z = false;
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            obtain.writeInt(i);
            this.mRemote.transact(2, obtain, obtain2, 0);
            obtain2.readException();
            if (obtain2.readInt() == 1) {
                z = true;
            }
            obtain.recycle();
            obtain2.recycle();
            return z;
        } catch (Throwable th) {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    public void updateTmsConfigAsync(ITmsCallback iTmsCallback) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            obtain.writeStrongBinder((IBinder) iTmsCallback);
            this.mRemote.transact(3, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    public boolean checkPermission(String str, int i) throws RemoteException {
        boolean z = false;
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            obtain.writeString(str);
            obtain.writeInt(i);
            this.mRemote.transact(1, obtain, obtain2, 0);
            obtain2.readException();
            if (obtain2.readInt() == 1) {
                z = true;
            }
            obtain.recycle();
            obtain2.recycle();
            return z;
        } catch (Throwable th) {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    public int sendTmsRequest(int i, Bundle bundle, Bundle bundle2) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            obtain.writeInt(i);
            bundle.writeToParcel(obtain, 0);
            bundle2.writeToParcel(obtain, 0);
            this.mRemote.transact(4, obtain, obtain2, 0);
            obtain2.readException();
            int readInt = obtain2.readInt();
            bundle2.readFromParcel(obtain2);
            return readInt;
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    public int sendTmsCallback(int i, Bundle bundle, ITmsCallbackEx iTmsCallbackEx) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            obtain.writeInt(i);
            bundle.writeToParcel(obtain, 0);
            obtain.writeStrongBinder((IBinder) iTmsCallbackEx);
            this.mRemote.transact(5, obtain, obtain2, 0);
            obtain2.readException();
            int readInt = obtain2.readInt();
            return readInt;
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    public int setProvider(ITmsProvider iTmsProvider) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            obtain.writeStrongBinder((IBinder) iTmsProvider);
            this.mRemote.transact(6, obtain, obtain2, 0);
            obtain2.readException();
            int readInt = obtain2.readInt();
            return readInt;
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }
}
