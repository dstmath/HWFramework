package com.android.server.rms.iaware.cpu;

import com.android.server.wifipro.WifiProCHRManager;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class CPUXMLUtility {
    CPUXMLUtility() {
    }

    public static ByteBuffer getConfigInfo(Map<String, CPUPropInfoItem> infoMap, String[] itemIndex, int msg) {
        byte[] cpusetInfoBytes = getFormatedStrBytes(infoMap, itemIndex);
        if (cpusetInfoBytes.length == 0) {
            return null;
        }
        int totalLen = cpusetInfoBytes.length + 12;
        if (totalLen > HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(totalLen);
        buffer.putInt(WifiProCHRManager.WIFI_PORTAL_AUTH_MSG_COLLECTE);
        buffer.putInt(msg);
        buffer.putInt(cpusetInfoBytes.length);
        buffer.put(cpusetInfoBytes);
        return buffer;
    }

    private static byte[] getFormatedStrBytes(Map<String, CPUPropInfoItem> infoMap, String[] itemIndex) {
        StringBuffer sb = new StringBuffer();
        for (String itemName : itemIndex) {
            CPUPropInfoItem itemValue = (CPUPropInfoItem) infoMap.get(itemName);
            if (itemValue == null) {
                sb.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
                sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            } else {
                sb.append(itemValue.mValue);
                sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            }
        }
        try {
            return sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }
}
