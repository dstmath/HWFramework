package com.android.server.rms.iaware.appmng.game;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;

public class AwareGameStatusCallback extends Binder implements IInterface {
    private static final String SDK_CALLBACK_DESCRIPTOR = "com.huawei.iaware.sdk.ISDKCallbak";
    private static final String TAG = "AwareGameStatusCallback";
    private static final int TRANSACTION_UPDATE_GAME_INFO = 1;

    public AwareGameStatusCallback() {
        attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code < 1 || code > 16777215) {
            return super.onTransact(code, data, reply, flags);
        }
        if (code != 1 || data == null) {
            return false;
        }
        try {
            data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
            resolveCallBackData(data.readString());
            return true;
        } catch (SecurityException e) {
            AwareLog.w(TAG, "onTransact, SecurityException");
            return false;
        }
    }

    private void resolveCallBackData(String info) {
        if (info == null) {
            AwareLog.d(TAG, "resolve callback data, invalid info");
        } else {
            AwareGameStatus.getInstance().processGameMsg(info);
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}
