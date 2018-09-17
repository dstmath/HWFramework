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
    private static NetworkAgentUtils networkAgentUtils;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.wifipro.HwNetworkAgent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.wifipro.HwNetworkAgent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.wifipro.HwNetworkAgent.<clinit>():void");
    }

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
