package com.android.server.wifi;

import com.huawei.device.connectivitychrlog.CSubCPUInfo;
import java.util.List;

public class HwCHRWifiCPUUsage extends HwCHRWifiSpeedBaseChecker {
    public static final String COL_ARRAY = "CPU USER NICE SYSTEM IDLE IOWAIT IRQ SOFTIRQ";
    public static final String COL_CPU = "CPU";
    public static final String COL_IDLE = "IDLE";
    public static final String COL_IOWAIT = "IOWAIT";
    public static final String COL_IRQ = "IRQ";
    public static final String COL_NICE = "NICE";
    public static final String COL_SEP = " ";
    public static final String COL_SOFTIRQ = "SOFTIRQ";
    public static final String COL_SYSTEM = "SYSTEM";
    public static final String COL_USER = "USER";

    private static class HwCHRWifiIncrHorizontalCounter extends HwCHRWifiHorizontalCounter {
        private long mOrg_value;

        public HwCHRWifiIncrHorizontalCounter(String counterName) {
            super(counterName, HwCHRWifiCPUUsage.COL_SEP);
            this.mOrg_value = 0;
        }

        public void parserValue(String valuesline, String cols) {
            super.parserValue(valuesline, cols);
            long tmp = this.mDelta;
            this.mDelta -= this.mOrg_value;
            this.mOrg_value = tmp;
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

    public HwCHRWifiCPUUsage() {
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_CPU));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_USER));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_NICE));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_SYSTEM));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_IDLE));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_IOWAIT));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_IRQ));
        this.counters.add(new HwCHRWifiIncrHorizontalCounter(COL_SOFTIRQ));
    }

    public void parserValue(String line) {
        line = line.replaceAll("  ", COL_SEP);
        for (int i = 0; i < this.counters.size(); i++) {
            ((HwCHRWifiCounterInfo) this.counters.get(i)).parserValue(line, COL_ARRAY);
        }
    }

    public long getTotal() {
        return (((((getCounterDetaByTab(COL_USER) + getCounterDetaByTab(COL_NICE)) + getCounterDetaByTab(COL_SYSTEM)) + getCounterDetaByTab(COL_IDLE)) + getCounterDetaByTab(COL_IOWAIT)) + getCounterDetaByTab(COL_SOFTIRQ)) + getCounterDetaByTab(COL_IRQ);
    }

    public long getIdle() {
        return getCounterDetaByTab(COL_IDLE);
    }

    public String toString() {
        return "HwCHRWifiCPUUsage [" + getCountersInfo() + "]";
    }

    public long getMaxFreq() {
        long value = 0;
        for (int i = 0; i < 16; i++) {
            long f = readFreqFrmFile(HwCHRWifiFile.getFileResult("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq"));
            if (f >= value) {
                value = f;
            }
        }
        return value;
    }

    public long readFreqFrmFile(List<String> lines) {
        long freq = 0;
        if (lines.size() < 1 || ((String) lines.get(0)).equals("")) {
            return freq;
        }
        try {
            freq = Long.parseLong(((String) lines.get(0)).trim());
        } catch (NumberFormatException e) {
        }
        return freq;
    }

    public CSubCPUInfo getCPUInfoCHR() {
        CSubCPUInfo cpu = new CSubCPUInfo();
        if (getTotal() > 0) {
            cpu.ipercent.setValue((int) (100 - ((getIdle() * 100) / getTotal())));
        }
        cpu.lmaxFreq.setValue(getMaxFreq());
        return cpu;
    }
}
