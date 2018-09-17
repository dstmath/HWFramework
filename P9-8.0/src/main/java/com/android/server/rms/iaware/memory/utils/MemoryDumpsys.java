package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.rms.iaware.feature.MemoryFeature;
import com.android.server.rms.iaware.feature.RFeature;
import com.android.server.rms.memrepair.ProcStateData;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class MemoryDumpsys {
    private static final String TAG = "MemoryDumpsys";

    /* JADX WARNING: Missing block: B:22:0x003f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length <= 0 || feature == null || !(feature instanceof MemoryFeature) || !"--test-Memory".equals(args[0])) {
            return false;
        }
        if (args.length == 2) {
            if (!"getSample".equals(args[1])) {
                return false;
            }
            printPssListMap(ProcStateStatisData.getInstance().getPssListMap(), pw);
            return true;
        } else if (args.length == 4) {
            return dealArgFour(args);
        } else {
            return false;
        }
    }

    private static boolean dealArgFour(String[] args) {
        if ("trim".equals(args[1])) {
            return trimMemory(args[2], args[3]);
        }
        return false;
    }

    private static boolean trimMemory(String proc, String levelArg) {
        int level;
        if (levelArg.equals("HIDDEN")) {
            level = 20;
        } else if (levelArg.equals("BACKGROUND")) {
            level = 40;
        } else if (levelArg.equals("MODERATE")) {
            level = 60;
        } else if (levelArg.equals("COMPLETE")) {
            level = 80;
        } else {
            AwareLog.e(TAG, "Error: Unknown level option: " + levelArg);
            return false;
        }
        return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
    }

    private static void printPssListMap(Map<String, List<ProcStateData>> pssListMap, PrintWriter pw) {
        AwareLog.d(TAG, "enter printPssListMap...");
        StringBuilder sb = new StringBuilder();
        for (Entry<String, List<ProcStateData>> entry : pssListMap.entrySet()) {
            for (ProcStateData procStateData : (List) entry.getValue()) {
                if (procStateData.getStatePssList() != null) {
                    sb.delete(0, sb.length());
                    sb.append("procName=").append(procStateData.getProcName());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("key=").append((String) entry.getKey());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("pssList=").append(procStateData.getStatePssList());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("procState=").append(procStateData.getState());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("mergeCount=").append(procStateData.getMergeCount());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("minPss=").append(procStateData.getMinPss());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("maxPss=").append(procStateData.getMaxPss());
                    pw.println(sb.toString());
                }
            }
        }
    }
}
