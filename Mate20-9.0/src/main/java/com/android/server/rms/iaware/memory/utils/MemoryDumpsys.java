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

public final class MemoryDumpsys {
    private static final String TAG = "MemoryDumpsys";

    public static final boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length <= 0 || feature == null || !(feature instanceof MemoryFeature) || !"--test-Memory".equals(args[0])) {
            return false;
        }
        if (args.length == 2) {
            if (!"getSample".equals(args[1])) {
                return false;
            }
            printMemListMap(ProcStateStatisData.getInstance().getPssListMap(), pw);
            printMemListMap(ProcStateStatisData.getInstance().getVssListMap(), pw);
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

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0066  */
    private static boolean trimMemory(String proc, String levelArg) {
        char c;
        int level;
        int hashCode = levelArg.hashCode();
        if (hashCode != -847101650) {
            if (hashCode != 163769603) {
                if (hashCode != 183181625) {
                    if (hashCode == 2130809258 && levelArg.equals("HIDDEN")) {
                        c = 0;
                        switch (c) {
                            case 0:
                                level = 20;
                                break;
                            case 1:
                                level = 40;
                                break;
                            case 2:
                                level = 60;
                                break;
                            case 3:
                                level = 80;
                                break;
                            default:
                                AwareLog.e(TAG, "Error: Unknown level option: " + levelArg);
                                return false;
                        }
                        return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
                    }
                } else if (levelArg.equals("COMPLETE")) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
                }
            } else if (levelArg.equals("MODERATE")) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
            }
        } else if (levelArg.equals("BACKGROUND")) {
            c = 1;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
        return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
    }

    private static void printMemListMap(Map<String, List<ProcStateData>> pssListMap, PrintWriter pw) {
        AwareLog.d(TAG, "enter printMemListMap...");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<ProcStateData>> entry : pssListMap.entrySet()) {
            for (ProcStateData procStateData : entry.getValue()) {
                if (procStateData.getStateMemList() != null) {
                    sb.delete(0, sb.length());
                    sb.append("procName=");
                    sb.append(procStateData.getProcName());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("isPss=");
                    sb.append(procStateData.isPss());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("key=");
                    sb.append(entry.getKey());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("memList=");
                    sb.append(procStateData.getStateMemList());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("procState=");
                    sb.append(procStateData.getState());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("mergeCount=");
                    sb.append(procStateData.getMergeCount());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("minMem=");
                    sb.append(procStateData.getMinMem());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("maxMem=");
                    sb.append(procStateData.getMaxMem());
                    pw.println(sb.toString());
                }
            }
        }
    }
}
