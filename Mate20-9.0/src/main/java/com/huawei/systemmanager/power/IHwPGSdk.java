package com.huawei.systemmanager.power;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.List;

public interface IHwPGSdk {
    boolean checkStateIsAudio(Context context, String str) throws RemoteException;

    List<HwDetailBatterySipper> getBatteryStats(Context context, List<UserHandle> list) throws RemoteException;
}
