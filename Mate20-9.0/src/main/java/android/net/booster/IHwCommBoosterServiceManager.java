package android.net.booster;

import android.os.Bundle;

public interface IHwCommBoosterServiceManager {
    public static final int ERROR_INVALID_CALLER = -4;
    public static final int ERROR_INVALID_PARAM = -3;
    public static final int ERROR_NO_BOOSERT_PLUGIN = -5;
    public static final int ERROR_NO_SERVICE = -1;
    public static final int ERROR_REMOTE_EXCEPTION = -2;
    public static final String SERVICE_NAME = "HwCommBoosterService";
    public static final int SUCCESS = 0;

    int registerCallBack(String str, IHwCommBoosterCallback iHwCommBoosterCallback);

    int reportBoosterPara(String str, int i, Bundle bundle);

    int unRegisterCallBack(String str, IHwCommBoosterCallback iHwCommBoosterCallback);
}
