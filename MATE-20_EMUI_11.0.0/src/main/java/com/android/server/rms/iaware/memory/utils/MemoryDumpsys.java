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
    private static final int ACTIVITY_MEMINFO_ARGS_LENGTH = 5;
    private static final int ACTIVITY_NAME_PARA_INDEX = 4;
    private static final int ARGS_LENGH_FOUR = 4;
    private static final int ARGS_LENGH_THREE = 3;
    private static final int ARGS_LENGH_TWO = 2;
    private static final int ARGS_SECOND = 2;
    private static final int ARGS_THIRD = 3;
    private static final String BACKGROUND = "BACKGROUND";
    private static final String COMPLETE = "COMPLETE";
    private static final int DEFAULT_PRINT_STRING_BUILDER_SIZE = 800;
    private static final String ENABLE_LOG = "enable_log";
    private static final String GET_SAMPLE = "getSample";
    private static final String HIDDEN = "HIDDEN";
    private static final String INSERT_ACTIVITY_MEMINFO = "insert_big_activity";
    private static final int MEM_SIZE_PARA_INDEX = 3;
    private static final String MODERATE = "MODERATE";
    private static final int OPERATE_PARA_INDEX = 1;
    private static final String PKG_TRACKER = "PkgTracker";
    private static final String SEMICOLON = ";";
    private static final String SHOW_ACTIVITY_MEMINFO = "get_big_activity";
    private static final String TAG = "MemoryDumpsys";
    private static final String TEST_MEMORY = "--test-Memory";
    private static final String TRIM = "trim";
    private static final int UID_PARA_INDEX = 2;

    private MemoryDumpsys() {
    }

    public static boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length <= 0 || feature == null || !(feature instanceof MemoryFeature) || !TEST_MEMORY.equals(args[0])) {
            return false;
        }
        if (dumpMemRepair(pw, args) || dumpPkgTracker(args)) {
            return true;
        }
        if (args.length == 4) {
            return dealArgFour(args);
        }
        if (showOrInsertActivityMemInfo(args)) {
            return true;
        }
        return false;
    }

    private static boolean showOrInsertActivityMemInfo(String[] paras) {
        if (paras.length != 2 || !SHOW_ACTIVITY_MEMINFO.equals(paras[1])) {
            if (paras.length == 5) {
                String operate = paras[1];
                String inputUid = paras[2];
                String inputMemSize = paras[3];
                String activityName = paras[4];
                if (INSERT_ACTIVITY_MEMINFO.equals(operate)) {
                    try {
                        BigMemoryInfo.getInstance().insertDataForDumpSys(Integer.parseInt(inputUid), Integer.parseInt(inputMemSize), activityName);
                        return true;
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "parse String to integer error");
                        return false;
                    }
                }
            }
            return false;
        }
        BigMemoryInfo.getInstance().showActivityMemInfo();
        return true;
    }

    private static boolean dumpMemRepair(PrintWriter pw, String[] args) {
        if (args.length != 2 || !GET_SAMPLE.equals(args[1])) {
            return false;
        }
        printMemListMap(ProcStateStatisData.getInstance().getPssListMap(), pw);
        printMemListMap(ProcStateStatisData.getInstance().getVssListMap(), pw);
        return true;
    }

    private static boolean dealArgFour(String[] args) {
        if (TRIM.equals(args[1])) {
            return trimMemory(args[2], args[3]);
        }
        return false;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static boolean trimMemory(String proc, String levelArg) {
        char c;
        int level;
        switch (levelArg.hashCode()) {
            case -847101650:
                if (levelArg.equals(BACKGROUND)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 163769603:
                if (levelArg.equals(MODERATE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 183181625:
                if (levelArg.equals(COMPLETE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2130809258:
                if (levelArg.equals(HIDDEN)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            level = 20;
        } else if (c == 1) {
            level = 40;
        } else if (c == 2) {
            level = 60;
        } else if (c != 3) {
            AwareLog.e(TAG, "Error: Unknown level option: " + levelArg);
            return false;
        } else {
            level = 80;
        }
        return MemoryUtils.trimMemory(HwActivityManagerService.self(), proc, level);
    }

    private static void printMemListMap(Map<String, List<ProcStateData>> pssListMap, PrintWriter pw) {
        AwareLog.d(TAG, "enter printMemListMap...");
        StringBuilder sb = new StringBuilder((int) DEFAULT_PRINT_STRING_BUILDER_SIZE);
        for (Map.Entry<String, List<ProcStateData>> entry : pssListMap.entrySet()) {
            for (ProcStateData procStateData : entry.getValue()) {
                if (procStateData.getStateMemList() != null) {
                    sb.delete(0, sb.length());
                    sb.append("procName=");
                    sb.append(procStateData.getProcName());
                    sb.append(";");
                    sb.append("isPss=");
                    sb.append(procStateData.isPss());
                    sb.append(";");
                    sb.append("key=");
                    sb.append(entry.getKey());
                    sb.append(";");
                    sb.append("memList=");
                    sb.append(procStateData.getStateMemList());
                    sb.append(";");
                    sb.append("procState=");
                    sb.append(procStateData.getState());
                    sb.append(";");
                    sb.append("mergeCount=");
                    sb.append(procStateData.getMergeCount());
                    sb.append(";");
                    sb.append("minMem=");
                    sb.append(procStateData.getMinMem());
                    sb.append(";");
                    sb.append("maxMem=");
                    sb.append(procStateData.getMaxMem());
                    pw.println(sb.toString());
                }
            }
        }
    }

    private static boolean dumpPkgTracker(String[] args) {
        if (args.length != 3) {
            return false;
        }
        if (PKG_TRACKER.equals(args[1]) && ENABLE_LOG.equals(args[2])) {
            PackageTracker.getInstance().enableDebug();
            return true;
        } else if (!PKG_TRACKER.equals(args[1]) || !ENABLE_LOG.equals(args[2])) {
            return false;
        } else {
            PackageTracker.getInstance().disableDebug();
            return true;
        }
    }
}
