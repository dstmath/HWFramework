package com.tencent.tmsecurelite.base;

import android.os.IInterface;
import android.os.Message;
import android.os.RemoteException;

public interface ITmsCallbackEx extends IInterface {
    void onCallback(Message message) throws RemoteException;
}
