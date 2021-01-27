package ohos.net;

import android.net.NetworkUtils;
import ohos.hiviewdfx.HiLogLabel;

public class NetworkUtilsAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NetworkUtilsAdapter");

    public static boolean protectFromVpn(int i) {
        return NetworkUtils.protectFromVpn(i);
    }
}
