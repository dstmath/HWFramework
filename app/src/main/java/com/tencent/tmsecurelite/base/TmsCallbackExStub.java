package com.tencent.tmsecurelite.base;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

public abstract class TmsCallbackExStub extends Binder implements ITmsCallbackEx {
    public TmsCallbackExStub() {
        attachInterface(this, "com.tencent.tmsecurelite.base.ITmsCallbackEx");
    }

    public static ITmsCallbackEx asInterface(IBinder iBinder) {
        if (iBinder == null) {
            return null;
        }
        IInterface queryLocalInterface = iBinder.queryLocalInterface("com.tencent.tmsecurelite.base.ITmsCallbackEx");
        if (queryLocalInterface != null && (queryLocalInterface instanceof ITmsCallbackEx)) {
            return (ITmsCallbackEx) queryLocalInterface;
        }
        return new TmsCallbackExProxy(iBinder);
    }

    protected boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsCallbackEx");
                onCallback((Message) parcel.readParcelable(TmsCallbackExStub.class.getClassLoader()));
                parcel2.writeNoException();
                break;
        }
        return true;
    }
}
