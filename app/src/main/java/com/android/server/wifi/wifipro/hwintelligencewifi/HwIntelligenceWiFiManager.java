package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.wifipro.WifiProUIDisplayManager;
import java.util.List;

public class HwIntelligenceWiFiManager {
    private static HwIntelligenceWiFiManager mHwIntelligenceWiFiManager;
    private static List<ScanResult> mScanList;
    private HwIntelligenceStateMachine mStateMachine;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager.<clinit>():void");
    }

    private HwIntelligenceWiFiManager(Context context, WifiProUIDisplayManager wifiProUIDisplayManager) {
        this.mStateMachine = HwIntelligenceStateMachine.createIntelligenceStateMachine(context, wifiProUIDisplayManager);
    }

    public static HwIntelligenceWiFiManager createInstance(Context context, WifiProUIDisplayManager wifiProUIDisplayManager) {
        if (mHwIntelligenceWiFiManager == null) {
            mHwIntelligenceWiFiManager = new HwIntelligenceWiFiManager(context, wifiProUIDisplayManager);
        }
        return mHwIntelligenceWiFiManager;
    }

    public void start() {
        this.mStateMachine.onStart();
    }

    public void stop() {
        this.mStateMachine.onStop();
    }

    public static void setWiFiProScanResultList(List<ScanResult> list) {
        Log.e(MessageUtil.TAG, "setWiFiProScanResultList");
        mScanList = list;
    }

    public static List<ScanResult> getWiFiProScanResultList() {
        return mScanList;
    }
}
