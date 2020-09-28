package android.net.booster;

import android.os.Handler;

public interface HwDataServiceQoe {
    public static final int ERROR_INVALID_CALLER = -4;
    public static final int ERROR_INVALID_PARAM = -3;
    public static final int ERROR_NO_BOOSERT_PLUGIN = -5;
    public static final int ERROR_NO_SERVICE = -1;
    public static final int ERROR_REMOTE_EXCEPTION = -2;
    public static final int SUCCESS = 0;

    int registerNetworkQoe(String str, String str2, Handler handler);

    int unRegisterNetworkQoe(String str);
}
