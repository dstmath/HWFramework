package android.net.wifi.p2p;

import android.net.wifi.WifiConfiguration;

public class DummyHwInnerWifiP2pManager implements HwInnerWifiP2pManager {
    private static HwInnerWifiP2pManager mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.DummyHwInnerWifiP2pManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.DummyHwInnerWifiP2pManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.DummyHwInnerWifiP2pManager.<clinit>():void");
    }

    public HwInnerWifiP2pManager getDefault() {
        return mInstance;
    }

    public WifiConfiguration getWifiRepeaterConfiguration() {
        return null;
    }

    public boolean setWifiRepeaterConfiguration(WifiConfiguration wifiConfig) {
        return false;
    }

    public boolean releaseIPAddr(String ifName) {
        return false;
    }

    public boolean configIPAddr(String ifName, String ipAddr, String server) {
        return false;
    }
}
