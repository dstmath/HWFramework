package com.huawei.android.os;

import android.os.Process;

public class ProcessExt {
    public static final int BLUETOOTH_UID = 1002;
    public static final int CAMERASERVER_UID = 1047;
    public static final int CLOUDSERVICE_UID = 5513;
    public static final int FIRST_ISOLATED_UID = 99000;
    public static final int LAST_ISOLATED_UID = 99999;
    public static final int MEDIA_UID = 1013;
    public static final int PROC_COMBINE = 256;
    public static final int PROC_OUT_LONG = 8192;
    public static final int PROC_OUT_STRING = 4096;
    public static final int PROC_PARENS = 512;
    public static final int PROC_SPACE_TERM = 32;
    public static final int PROC_TAB_TERM = 9;
    public static final int ROOT_UID = 0;
    public static final int SCHED_FIFO = 1;
    public static final int SCHED_OTHER = 0;
    public static final int SCHED_RESET_ON_FORK = 1073741824;
    public static final int SHELL_UID = 2000;
    public static final int THREAD_GROUP_BG_NONINTERACTIVE = 0;
    public static final int THREAD_GROUP_DEFAULT = -1;
    public static final int THREAD_GROUP_KEY_BACKGROUND = 8;
    public static final int THREAD_GROUP_SYSTEM = 2;
    public static final int THREAD_GROUP_TOP_APP = 5;
    public static final int THREAD_GROUP_VIP = 10;

    public static void setThreadScheduler(int tid, int policy, int priority) throws IllegalArgumentException {
        Process.setThreadScheduler(tid, policy, priority);
    }

    public static void setThreadGroupAndCpuset(int tid, int group) throws IllegalArgumentException, SecurityException {
        Process.setThreadGroupAndCpuset(tid, group);
    }

    public static void setProcessGroup(int pid, int group) {
        Process.setProcessGroup(pid, group);
    }

    public static long getTotalMemory() {
        return Process.getTotalMemory();
    }

    public static long getPss(int pid) {
        return Process.getPss(pid);
    }

    public static int[] getPidsForCommands(String[] procName) {
        return Process.getPidsForCommands(procName);
    }

    public static int getUidForPid(int tgid) {
        return Process.getUidForPid(tgid);
    }

    public static int getProcessGroup(int pid) {
        return Process.getProcessGroup(pid);
    }

    public static int getParentPid(int heriPid) {
        return Process.getParentPid(heriPid);
    }

    public static void setThreadBoostInfo(String[] threadBoostInfo) {
        Process.setThreadBoostInfo(threadBoostInfo);
    }

    public static int[] getPids(String path, int[] lastArray) {
        return Process.getPids(path, lastArray);
    }

    public static void setQosSched(boolean enable) {
        Process.setQosSched(enable);
    }

    public static void setThreadQosPolicy(int tid, int group) {
        Process.setThreadQosPolicy(tid, group);
    }

    public static void readProcLines(String path, String[] reqFields, long[] outSizes) {
        Process.readProcLines(path, reqFields, outSizes);
    }

    public static String getCmdlineForPid(int pid) {
        return Process.getCmdlineForPid(pid);
    }
}
