package com.android.server.wifi;

import com.huawei.device.connectivitychrlog.CSubMemInfo;
import java.util.List;

public class HwCHRWifiMemUsage extends HwCHRWifiSpeedBaseChecker {
    public static final String COL_MEMAVAILBLE = "MemAvailable";
    public static final String COL_MEMFREE = "MemFree";
    public static final String COL_MEMTOTAL = "MemTotal";

    private static class MemCounterInfo extends HwCHRWifiCounterInfo {
        public MemCounterInfo(String info) {
            super(info, ":");
        }

        public void parserValue(String Line, String cols) {
            try {
                this.mDelta = Long.parseLong(Line.replace(this.mTag + this.mOperate, "").replace("kB", "").trim());
            } catch (NumberFormatException e) {
            }
        }
    }

    public /* bridge */ /* synthetic */ long getCounterDetaByTab(String tag) {
        return super.getCounterDetaByTab(tag);
    }

    public /* bridge */ /* synthetic */ String getCountersInfo() {
        return super.getCountersInfo();
    }

    public /* bridge */ /* synthetic */ int getFailReason() {
        return super.getFailReason();
    }

    public /* bridge */ /* synthetic */ int getOld() {
        return super.getOld();
    }

    public /* bridge */ /* synthetic */ String getSpeedInfo() {
        return super.getSpeedInfo();
    }

    public /* bridge */ /* synthetic */ int getSuckTimes() {
        return super.getSuckTimes();
    }

    public /* bridge */ /* synthetic */ int old() {
        return super.old();
    }

    public HwCHRWifiMemUsage() {
        this.counters.add(new MemCounterInfo(COL_MEMAVAILBLE));
        this.counters.add(new MemCounterInfo(COL_MEMTOTAL));
        this.counters.add(new MemCounterInfo(COL_MEMFREE));
    }

    public void parse_file(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String l = (String) lines.get(i);
            for (int j = 0; j < this.counters.size(); j++) {
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
