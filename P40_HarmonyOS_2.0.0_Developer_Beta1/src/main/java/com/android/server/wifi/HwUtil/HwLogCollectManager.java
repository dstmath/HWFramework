package com.android.server.wifi.HwUtil;

import android.content.Context;
import android.os.RemoteException;
import com.android.server.wifi.hwUtil.IHwLogCollectManagerEx;
import com.huawei.lcagent.client.LogCollectManager;

public class HwLogCollectManager implements IHwLogCollectManagerEx {
    private static LogCollectManager mClient = null;

    public static synchronized HwLogCollectManager createHwLogCollectManager(Context context) {
        HwLogCollectManager hwLogCollectManager;
        synchronized (HwLogCollectManager.class) {
            hwLogCollectManager = new HwLogCollectManager(context);
        }
        return hwLogCollectManager;
    }

    public int getUserType() throws RemoteException {
        return mClient.getUserType();
    }

    private HwLogCollectManager(Context context) {
        mClient = new LogCollectManager(context);
    }
}
