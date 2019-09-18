package com.huawei.systemmanager.power;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.pgmng.plug.DetailBatterySipper;
import com.huawei.pgmng.plug.PGSdk;
import java.util.ArrayList;
import java.util.List;

public class HwPGSdkImpl implements IHwPGSdk {
    private static IHwPGSdk sInstance = null;
    private PGSdk mPGSdkService = PGSdk.getInstance();

    private HwPGSdkImpl() {
    }

    public static synchronized IHwPGSdk getInstance() {
        synchronized (HwPGSdkImpl.class) {
            HwPGSdkImpl tmp = new HwPGSdkImpl();
            if (tmp.mPGSdkService == null) {
                return null;
            }
            return tmp;
        }
    }

    public List<HwDetailBatterySipper> getBatteryStats(Context context, List<UserHandle> userList) throws RemoteException {
        if (this.mPGSdkService == null) {
            return null;
        }
        List<DetailBatterySipper> detailist = this.mPGSdkService.getBatteryStats(context, userList);
        if (detailist == null) {
            return null;
        }
        List<HwDetailBatterySipper> hwdetailist = new ArrayList<>();
        for (DetailBatterySipper item : detailist) {
            hwdetailist.add(new HwDetailBatterySipper(item));
        }
        return hwdetailist;
    }

    public boolean checkStateIsAudio(Context context, String pkg) throws RemoteException {
        boolean z = false;
        if (this.mPGSdkService == null) {
            return false;
        }
        if (this.mPGSdkService.checkStateByPkg(context, pkg, 1) || this.mPGSdkService.checkStateByPkg(context, pkg, 2)) {
            z = true;
        }
        return z;
    }
}
