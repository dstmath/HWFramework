package com.android.server.wifi;

import android.os.SystemProperties;
import com.huawei.device.connectivitychrlog.CSubDNS;

public class HwCHRWifiDNS {
    private int mFailesCount = 0;
    private int m_oldFailesTime = 0;

    public void monitorDNS() {
        int dnsFailCnt = 0;
        try {
            dnsFailCnt = Integer.parseInt(SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0"));
        } catch (NumberFormatException e) {
        }
        this.mFailesCount = dnsFailCnt - this.m_oldFailesTime;
        this.m_oldFailesTime = dnsFailCnt;
    }

    public CSubDNS getDNSCHR() {
        CSubDNS result = new CSubDNS();
        result.iFailedCnt.setValue(this.mFailesCount);
        return result;
    }
}
