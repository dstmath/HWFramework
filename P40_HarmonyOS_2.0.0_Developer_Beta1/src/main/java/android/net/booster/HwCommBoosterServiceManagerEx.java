package android.net.booster;

import android.common.HwFrameworkFactory;
import android.os.Bundle;

public class HwCommBoosterServiceManagerEx {
    public static int registerCallBack(String pkgName, IHwCommBoosterCallback cb) {
        return HwFrameworkFactory.getHwCommBoosterServiceManager().registerCallBack(pkgName, cb);
    }

    public static int unRegisterCallBack(String pkgName, IHwCommBoosterCallback cb) {
        return HwFrameworkFactory.getHwCommBoosterServiceManager().unRegisterCallBack(pkgName, cb);
    }

    public static int reportBoosterPara(String pkgName, int dataType, Bundle data) {
        return HwFrameworkFactory.getHwCommBoosterServiceManager().reportBoosterPara(pkgName, dataType, data);
    }
}
