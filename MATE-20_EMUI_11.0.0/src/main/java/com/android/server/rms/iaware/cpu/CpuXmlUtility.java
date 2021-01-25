package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: CpuXmlConfiguration */
public class CpuXmlUtility {
    private static final int BYTE_SIZE_OF_INT = 4;
    private static final String TAG = "CpuXmlUtility";

    CpuXmlUtility() {
    }

    public static ByteBuffer getConfigInfo(Map<String, CpuPropInfoItem> infoMap, String[] itemIndex, int msg) {
        int totalLen;
        if (itemIndex == null) {
            return null;
        }
        byte[] cpusetInfoBytes = getFormatedStrBytes(infoMap, itemIndex);
        if (cpusetInfoBytes.length == 0 || (totalLen = cpusetInfoBytes.length + 12) > 512) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(totalLen);
        buffer.putInt(CpuFeature.MSG_SET_CPUCONFIG);
        buffer.putInt(msg);
        buffer.putInt(cpusetInfoBytes.length);
        buffer.put(cpusetInfoBytes);
        return buffer;
    }

    private static byte[] getFormatedStrBytes(Map<String, CpuPropInfoItem> infoMap, String[] itemIndex) {
        StringBuffer sb = new StringBuffer();
        for (String itemName : itemIndex) {
            CpuPropInfoItem itemValue = infoMap.get(itemName);
            if (itemValue == null) {
                sb.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
                sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            } else {
                sb.append(itemValue.mValue);
                sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            }
        }
        try {
            return sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            AwareLog.e(TAG, "UnsupportedEncodingException " + e.toString());
            return new byte[0];
        }
    }
}
