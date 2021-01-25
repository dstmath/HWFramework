package android.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pManager;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class WifiP2pManagerUtils extends EasyInvokeUtils {
    MethodObject<Integer> putListener;

    @InvokeMethod(methodObject = "putListener")
    public int putListener(WifiP2pManager.Channel channel, Object listener) {
        return ((Integer) invokeMethod(this.putListener, channel, new Object[]{listener})).intValue();
    }
}
