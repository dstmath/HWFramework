package com.huawei.systemmanager.power;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.android.pgmng.plug.DetailBatterySipper;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.ArrayList;
import java.util.List;

public class HwPGSdkImpl implements IHwPGSdk {
    private static IHwPGSdk sInstance = null;
    private PowerKit mPGSdkService = PowerKit.getInstance();

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

    @Override // com.huawei.systemmanager.power.IHwPGSdk
    public List<HwDetailBatterySipper> getBatteryStats(Context context, List<UserHandle> userList) throws RemoteException {
        List<DetailBatterySipper> detailist;
        PowerKit powerKit = this.mPGSdkService;
        if (powerKit == null || (detailist = powerKit.getBatteryStats(context, userList)) == null) {
            return null;
        }
        List<HwDetailBatterySipper> hwdetailist = new ArrayList<>();
        for (DetailBatterySipper item : detailist) {
            hwdetailist.add(new HwDetailBatterySipper(item));
        }
        return hwdetailist;
    }

    @Override // com.huawei.systemmanager.power.IHwPGSdk
    public boolean checkStateIsAudio(Context context, String pkg) throws RemoteException {
        PowerKit powerKit = this.mPGSdkService;
        if (powerKit == null) {
            return false;
        }
        if (powerKit.checkStateByPkg(context, pkg, 1) || this.mPGSdkService.checkStateByPkg(context, pkg, 2)) {
            return true;
        }
        return false;
    }
}
