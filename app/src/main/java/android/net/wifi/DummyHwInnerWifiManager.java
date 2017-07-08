package android.net.wifi;

import android.content.Context;
import android.net.ProxyInfo;
import java.util.HashMap;
import java.util.List;

public class DummyHwInnerWifiManager implements HwInnerWifiManager {
    private static HwInnerWifiManager mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.DummyHwInnerWifiManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.DummyHwInnerWifiManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.DummyHwInnerWifiManager.<clinit>():void");
    }

    public HwInnerWifiManager getDefault() {
        return mInstance;
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public int calculateSignalLevelHW(int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    public String getWpaSuppConfig() {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public boolean setWifiEnterpriseConfigEapMethod(int eapMethod, HashMap<String, String> hashMap) {
        return false;
    }

    public boolean getHwMeteredHint(Context context) {
        return false;
    }

    public PPPOEInfo getPPPOEInfo() {
        return null;
    }

    public void startPPPOE(PPPOEConfig config) {
    }

    public void stopPPPOE() {
    }

    public List<String> getApLinkedStaList() {
        return null;
    }

    public void setSoftapMacFilter(String macFilter) {
    }

    public void setSoftapDisassociateSta(String mac) {
    }

    public void userHandoverWifi() {
    }

    public int[] getChannelListFor5G() {
        return null;
    }

    public void setWifiApEvaluateEnabled(boolean enabled) {
    }

    public byte[] fetchWifiSignalInfoForVoWiFi() {
        return null;
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        return false;
    }

    public WifiDetectConfInfo getVoWifiDetectMode() {
        return null;
    }

    public boolean setVoWifiDetectPeriod(int period) {
        return false;
    }

    public int getVoWifiDetectPeriod() {
        return -1;
    }

    public boolean isSupportVoWifiDetect() {
        return false;
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
    }
}
