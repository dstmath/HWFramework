package com.android.server.wifi;

import android.util.Log;
import com.huawei.device.connectivitychrlog.CSubCPUInfo;
import java.io.File;
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
        private long mOrg_value = 0;

        public HwCHRWifiIncrHorizontalCounter(String counterName) {
            super(counterName, HwCHRWifiCPUUsage.COL_SEP);
        }

        public void parserValue(String valuesline, String cols) {
            super.parserValue(valuesline, cols);
            long tmp = this.mDelta;
            this.mDelta -= this.mOrg_value;
            this.mOrg_value = tmp;
        }
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
        int listSize = this.counters.size();
        for (int i = 0; i < listSize; i++) {
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
        String[] fileName = new File("/sys/devices/system/cpu").list();
        if (fileName != null) {
            int i = 0;
            while (i < fileName.length) {
                if (fileName[i].length() == 4 && fileName[i].startsWith("cpu")) {
                    char num = fileName[i].charAt(3);
                    if (Character.isDigit(num)) {
                        long f = readFreqFrmFile(HwCHRWifiFile.getFileResult("/sys/devices/system/cpu/cpu" + num + "/cpufreq/scaling_cur_freq"));
                        if (f >= value) {
                            value = f;
                        }
                    }
                }
                i++;
            }
        } else {
            Log.e("HwCHRWifiCPUUsage", "CPU file not found");
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
