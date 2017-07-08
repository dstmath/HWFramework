package com.tencent.tmsecurelite.commom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import org.json.JSONException;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

public abstract class TmsCallbackStub extends Binder implements ITmsCallback {
    public static ITmsCallback asInterface(IBinder iBinder) {
        if (iBinder == null) {
            return null;
        }
        IInterface queryLocalInterface = iBinder.queryLocalInterface("com.tencent.tmsecurelite.ITmsCallback");
        if (queryLocalInterface != null && (queryLocalInterface instanceof ITmsCallback)) {
            return (ITmsCallback) queryLocalInterface;
        }
        return new TmsCallbackProxy(iBinder);
    }

    protected boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                DataEntity dataEntity;
                int readInt = parcel.readInt();
                try {
                    dataEntity = new DataEntity(parcel);
                } catch (JSONException e) {
                    e.printStackTrace();
                    dataEntity = null;
                }
                onResultGot(readInt, dataEntity);
                parcel2.writeNoException();
                break;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                onArrayResultGot(parcel.readInt(), DataEntity.readFromParcel(parcel));
                parcel2.writeNoException();
                break;
        }
        return true;
    }
}
