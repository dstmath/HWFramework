package com.android.server.wifi;

import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.huawei.device.connectivitychrlog.CSubMemInfo;
import java.util.List;

public class HwCHRWifiMemUsage extends HwCHRWifiSpeedBaseChecker {
    public static final String COL_MEMAVAILBLE = "MemAvailable";
    public static final String COL_MEMFREE = "MemFree";
    public static final String COL_MEMTOTAL = "MemTotal";

    private static class MemCounterInfo extends HwCHRWifiCounterInfo {
        public MemCounterInfo(String info) {
            super(info, HwQoEUtils.SEPARATOR);
        }

        public void parserValue(String Line, String cols) {
            try {
                this.mDelta = Long.parseLong(Line.replace(this.mTag + this.mOperate, "").replace("kB", "").trim());
            } catch (NumberFormatException e) {
            }
        }
    }

    public HwCHRWifiMemUsage() {
        this.counters.add(new MemCounterInfo(COL_MEMAVAILBLE));
        this.counters.add(new MemCounterInfo(COL_MEMTOTAL));
        this.counters.add(new MemCounterInfo(COL_MEMFREE));
    }

    public void parse_file(List<String> lines) {
        int listSize = lines.size();
        int counterSize = this.counters.size();
        for (int i = 0; i < listSize; i++) {
            String l = (String) lines.get(i);
            for (int j = 0; j < counterSize; j++) {
                if (((HwCHRWifiCounterInfo) this.counters.get(j)).match(l)) {
                    ((HwCHRWifiCounterInfo) this.counters.get(j)).parserValue(l, "");
                }
            }
        }
    }

    public String toString() {
        return "HwCHRWifiMemUsage[" + getCountersInfo() + "]";
    }

    public CSubMemInfo getMemInfoCHR() {
        CSubMemInfo mem = new CSubMemInfo();
        int load = 0;
        if (getCounterDetaByTab(COL_MEMTOTAL) > 0) {
            load = (int) (((getCounterDetaByTab(COL_MEMFREE) + getCounterDetaByTab(COL_MEMAVAILBLE)) * 100) / getCounterDetaByTab(COL_MEMTOTAL));
        }
        mem.iMemLoad.setValue(100 - load);
        return mem;
    }
}
