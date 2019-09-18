package android.net.wifi.wifipro;

import android.net.NetworkAgent;
import android.net.wifi.wifipro.WifiProInvokeUtils;

public class NetworkAgentUtils extends WifiProInvokeUtils {
    WifiProInvokeUtils.MethodObject<Void> queueOrSendMessage;

    @WifiProInvokeUtils.InvokeMethod(methodObject = "queueOrSendMessage")
    public void queueOrSendMessage(NetworkAgent networkAgent, int what, Object obj) {
        invokeMethod(this.queueOrSendMessage, networkAgent, Integer.valueOf(what), obj);
    }
}
