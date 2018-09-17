package com.tencent.tmsecurelite.base;

import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;

public class TmsCallbackExProxy implements ITmsCallbackEx {
    private IBinder mRemote;

    public TmsCallbackExProxy(IBinder iBinder) {
        this.mRemote = iBinder;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public void onCallback(Message message) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsCallbackEx");
            obtain.writeParcelable(message, 0);
            this.mRemote.transact(1, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }
}
