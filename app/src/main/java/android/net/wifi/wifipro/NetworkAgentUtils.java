package android.net.wifi.wifipro;

import android.net.NetworkAgent;
import android.net.wifi.wifipro.WifiProInvokeUtils.InvokeMethod;
import android.net.wifi.wifipro.WifiProInvokeUtils.MethodObject;

public class NetworkAgentUtils extends WifiProInvokeUtils {
    MethodObject<Void> queueOrSendMessage;

    @InvokeMethod(methodObject = "queueOrSendMessage")
    public void queueOrSendMessage(NetworkAgent networkAgent, int what, Object obj) {
        invokeMethod(this.queueOrSendMessage, networkAgent, Integer.valueOf(what), obj);
    }
}
