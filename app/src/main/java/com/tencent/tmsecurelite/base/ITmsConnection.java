package com.tencent.tmsecurelite.base;

import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import com.tencent.tmsecurelite.commom.ITmsCallback;

public interface ITmsConnection extends IInterface {
    boolean checkPermission(String str, int i) throws RemoteException;

    boolean checkVersion(int i) throws RemoteException;

    int sendTmsCallback(int i, Bundle bundle, ITmsCallbackEx iTmsCallbackEx) throws RemoteException;

    int sendTmsRequest(int i, Bundle bundle, Bundle bundle2) throws RemoteException;

    int setProvider(ITmsProvider iTmsProvider) throws RemoteException;

    void updateTmsConfigAsync(ITmsCallback iTmsCallback) throws RemoteException;
}
