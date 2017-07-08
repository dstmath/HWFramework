package com.tencent.tmsecurelite.base;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.tencent.tmsecurelite.base.ITmsProvider.Stub;
import com.tencent.tmsecurelite.commom.TmsCallbackStub;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

public abstract class TmsConnectionStub extends Binder implements ITmsConnection {
    public static ITmsConnection asInterface(IBinder iBinder) {
        if (iBinder == null) {
            return null;
        }
        IInterface queryLocalInterface = iBinder.queryLocalInterface("com.tencent.tmsecurelite.base.ITmsConnection");
        if (queryLocalInterface != null && (queryLocalInterface instanceof ITmsConnection)) {
            return (ITmsConnection) queryLocalInterface;
        }
        return new TmsConnectionProxy(iBinder);
    }

    protected boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        int i3 = 0;
        boolean checkPermission;
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                checkPermission = checkPermission(parcel.readString(), parcel.readInt());
                parcel2.writeNoException();
                if (checkPermission) {
                    i3 = 1;
                }
                parcel2.writeInt(i3);
                break;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                checkPermission = checkVersion(parcel.readInt());
                parcel2.writeNoException();
                if (checkPermission) {
                    i3 = 1;
                }
                parcel2.writeInt(i3);
                break;
            case FileInfo.TYPE_BIGFILE /*3*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                updateTmsConfigAsync(TmsCallbackStub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                return true;
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                Bundle bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                i3 = sendTmsRequest(parcel.readInt(), (Bundle) Bundle.CREATOR.createFromParcel(parcel), bundle);
                parcel2.writeNoException();
                parcel2.writeInt(i3);
                bundle.writeToParcel(parcel2, 1);
                break;
            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                i3 = sendTmsCallback(parcel.readInt(), (Bundle) Bundle.CREATOR.createFromParcel(parcel), TmsCallbackExStub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                parcel2.writeInt(i3);
                break;
            case UrlCheckType.TIPS_CHEAT /*6*/:
                parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                i3 = setProvider(Stub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                parcel2.writeInt(i3);
                break;
        }
        return true;
    }

    public boolean checkVersion(int i) {
        return 3 >= i;
    }
}
