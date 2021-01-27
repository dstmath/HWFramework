package com.android.server.wifi.hwUtil;

import android.net.wifi.ScanResult;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.util.InformationElementUtil;
import java.util.BitSet;

public class GenerateScanResult {
    private static final int CAPABILITY_SIZE = 16;

    public static String generateScanResultCapabilities(ScanResult.InformationElement[] ies, short capabilityInt) {
        BitSet hidlCapability = new BitSet(16);
        for (int i = 0; i < 16; i++) {
            if (((1 << i) & capabilityInt) != 0) {
                hidlCapability.set(i);
            }
        }
        boolean isEnhancedOpenSupported = false;
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector != null) {
            isEnhancedOpenSupported = wifiInjector.mWificondControl.isEnhancedOpenSupported();
        }
        InformationElementUtil.Capabilities capabilities = new InformationElementUtil.Capabilities();
        capabilities.from(ies, hidlCapability, isEnhancedOpenSupported);
        return capabilities.generateCapabilitiesString();
    }
}
