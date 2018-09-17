package com.tencent.tmsecurelite.commom;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TmsCallbackProxy implements ITmsCallback {
    private IBinder mRemote;

    public TmsCallbackProxy(IBinder iBinder) {
        this.mRemote = iBinder;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public void onResultGot(int i, DataEntity dataEntity) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInt(i);
            dataEntity.writeToParcel(obtain, 0);
            this.mRemote.transact(1, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }

    public void onArrayResultGot(int i, ArrayList<DataEntity> arrayList) throws RemoteException {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInt(i);
            DataEntity.writeToParcel((List) arrayList, obtain);
            this.mRemote.transact(2, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain.recycle();
            obtain2.recycle();
        }
    }
}
