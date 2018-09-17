package android.net.wifi.wifipro;

import android.content.Context;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.os.Looper;

public class HwNetworkAgent extends NetworkAgent {
    private static final int BASE = 528384;
    public static final int EVENT_SET_EXPLICITLY_UNSELECTED = 528585;
    public static final int EVENT_TRIGGER_ROAMING_NETWORK_MONITOR = 528587;
    public static final int EVENT_UPDATE_NETWORK_CONCURRENTLY = 528586;
    public static final int PORTAL_NETWORK = 3;
    public static final int WIFI_BACKGROUND_READY = 4;
    private static NetworkAgentUtils networkAgentUtils = ((NetworkAgentUtils) WifiProInvokeUtils.getInvokeUtils(NetworkAgentUtils.class));

    public HwNetworkAgent(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score) {
        super(looper, context, logTag, ni, nc, lp, score, null);
    }

    public HwNetworkAgent(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
        super(looper, context, logTag, ni, nc, lp, score, misc);
    }

    public void explicitlyUnselected() {
        if (networkAgentUtils != null) {
            networkAgentUtils.queueOrSendMessage(this, EVENT_SET_EXPLICITLY_UNSELECTED, Integer.valueOf(0));
        }
    }

    public void updateNetworkConcurrently(NetworkInfo networkInfo) {
        if (networkAgentUtils != null) {
            networkAgentUtils.queueOrSendMessage(this, EVENT_UPDATE_NETWORK_CONCURRENTLY, new NetworkInfo(networkInfo));
        }
    }

    public void triggerRoamingNetworkMonitor(NetworkInfo networkInfo) {
        if (networkAgentUtils != null) {
            networkAgentUtils.queueOrSendMessage(this, EVENT_TRIGGER_ROAMING_NETWORK_MONITOR, new NetworkInfo(networkInfo));
        }
    }

    protected void unwanted() {
    }
}
