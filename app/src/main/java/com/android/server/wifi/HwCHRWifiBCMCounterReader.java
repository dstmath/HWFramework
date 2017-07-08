package com.android.server.wifi;

import java.util.ArrayList;
import java.util.List;

public class HwCHRWifiBCMCounterReader {
    private static final String TAG = "HwCHRWifiBCMCounterReader";
    List<HwCHRWifiBCMCounter> mBcmCounters;

    public HwCHRWifiBCMCounterReader() {
        this.mBcmCounters = new ArrayList();
    }

    public static boolean isNumeric(String str) {
        int i = str.length();
        while (true) {
            i--;
            if (i < 0) {
                return true;
            }
            if ((i != 0 || str.charAt(i) != '-') && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
    }

    private long parseLong(String num) {
        long result = 0;
        try {
            result = Long.parseLong(num);
        } catch (NumberFormatException e) {
        }
        return result;
    }

    public List<HwCHRWifiBCMCounter> parseValue(List<String> lines) {
        this.mBcmCounters.clear();
        if (lines == null) {
            return this.mBcmCounters;
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = ((String) lines.get(i)).trim();
            if (!line.equals("")) {
                String[] item = line.split(HwCHRWifiCPUUsage.COL_SEP);
                for (String split : item) {
                    String[] t = split.split(":");
                    if (t.length == 2) {
                        this.mBcmCounters.add(new HwCHRWifiBCMCounter(t[0].trim(), parseLong(t[1].trim())));
                    }
                }
            }
        }
        return this.mBcmCounters;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i = 0; i < this.mBcmCounters.size(); i++) {
            HwCHRWifiBCMCounter item = (HwCHRWifiBCMCounter) this.mBcmCounters.get(i);
            buffer.append(item.getName() + "=" + item.getValue() + HwCHRWifiCPUUsage.COL_SEP);
        }
        buffer.append("]");
        return buffer.toString();
    }

    public HwCHRWifiBCMCounter getBcmCounter(String counterName) {
        if (counterName == null) {
            return null;
        }
        HwCHRWifiBCMCounter tag = new HwCHRWifiBCMCounter(counterName, 0);
        for (int i = 0; i < this.mBcmCounters.size(); i++) {
            if (((HwCHRWifiBCMCounter) this.mBcmCounters.get(i)).equals(tag)) {
                return (HwCHRWifiBCMCounter) this.mBcmCounters.get(i);
            }
        }
        return null;
    }
}
