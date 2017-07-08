package com.tencent.tmsecurelite.commom;

import android.os.IInterface;
import android.os.RemoteException;
import java.util.ArrayList;

public interface ITmsCallback extends IInterface {
    void onArrayResultGot(int i, ArrayList<DataEntity> arrayList) throws RemoteException;

    void onResultGot(int i, DataEntity dataEntity) throws RemoteException;
}
