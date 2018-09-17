package com.android.server.rms.iaware.cpu;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver.Stub;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.util.SparseArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class CPUProcessInherit {
    private static final int GROUP_FOREGROUP = 1;
    private static final int INIT_PROC_PID = 1;
    private static final int MAX_TRY_FORK_TIMES = 5;
    private static final int MSG_APP_BACKGROUND = 2;
    private static final int MSG_APP_DIED = 3;
    private static final int MSG_APP_FOREGROUND = 1;
    private static final String PATH_GROUPPROCS = "cgroup.procs";
    private static final String PATH_UID_INFO = "/acct/uid_";
    private static final String TAG = "CPUProcessInherit";
    private SparseArray<InheritInfo> mForkList = new SparseArray();
    private ProcessHandler mProcessHandler = new ProcessHandler(this, null);
    private MyIProcessObserver mProcessObserver = new MyIProcessObserver();

    class MyIProcessObserver extends Stub {
        MyIProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (CPUFeature.isCpusetEnable()) {
                Message observerMsg = CPUProcessInherit.this.mProcessHandler.obtainMessage();
                observerMsg.arg1 = pid;
                observerMsg.arg2 = uid;
                if (foregroundActivities) {
                    observerMsg.what = 1;
                    NetManager.getInstance().sendMsgToNetMng(pid, uid, 0);
                } else {
                    observerMsg.what = 2;
                    NetManager.getInstance().sendMsgToNetMng(pid, uid, 1);
                }
                CPUProcessInherit.this.mProcessHandler.sendMessage(observerMsg);
            }
        }

        public void onProcessDied(int pid, int uid) {
            NetManager.getInstance().sendMsgToNetMng(pid, uid, 2, 1000);
            Message observerMsg = CPUProcessInherit.this.mProcessHandler.obtainMessage();
            observerMsg.arg1 = pid;
            observerMsg.arg2 = uid;
            observerMsg.what = 3;
            CPUProcessInherit.this.mProcessHandler.sendMessage(observerMsg);
        }
    }

    private class ProcessHandler extends Handler {
        /* synthetic */ ProcessHandler(CPUProcessInherit this$0, ProcessHandler -this1) {
            this();
        }

        private ProcessHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int pid = msg.arg1;
            int uid = msg.arg2;
            switch (msg.what) {
                case 1:
                    CPUProcessInherit.this.handleProcessFork(pid, uid, true);
                    SchedLevelBoost.getInstance().onFgActivitiesChanged(pid, uid, true);
                    return;
                case 2:
                    CPUProcessInherit.this.handleProcessFork(pid, uid, false);
                    SchedLevelBoost.getInstance().onFgActivitiesChanged(pid, uid, false);
                    return;
                case 3:
                    CPUProcessInherit.this.handleProcessDie(pid, uid);
                    CPUFullscreenMgr.getInstance().onProcessDied(pid);
                    SchedLevelBoost.getInstance().onProcessDied(pid, uid);
                    return;
                default:
                    return;
            }
        }
    }

    public void registerPorcessObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "CPUProcessInherit register process observer failed");
        }
    }

    public void unregisterPorcessObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "CPUProcessInherit unregister process observer failed");
        }
    }

    private int setHeriPidGroup(int pid, int heriPid) {
        long time = System.currentTimeMillis();
        long oldId = Binder.clearCallingIdentity();
        int curSchedGroup = Integer.MIN_VALUE;
        int heriPidcurSchedGroup = Integer.MIN_VALUE;
        try {
            curSchedGroup = Process.getProcessGroup(pid);
            heriPidcurSchedGroup = Process.getProcessGroup(heriPid);
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, "getProcessGroup pid " + heriPid + " or ppid " + pid + " has illegal argument");
        } catch (SecurityException e2) {
            AwareLog.e(TAG, "getProcessGroup pid " + heriPid + " or ppid " + pid + " has no permission");
        } catch (RuntimeException e3) {
            AwareLog.e(TAG, "getProcessGroup pid" + heriPid + " is not existed");
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
        if (curSchedGroup == Integer.MIN_VALUE || heriPidcurSchedGroup == Integer.MIN_VALUE) {
            return -1;
        }
        String operation = null;
        String reason = null;
        String subType = null;
        boolean isForkOk = false;
        boolean isGroupSet = false;
        if (1 == pid) {
            if (heriPidcurSchedGroup != 0) {
                isForkOk = setProcessGroup(heriPid, 0);
                isGroupSet = true;
                reason = "process parent is init";
                subType = CpuDumpRadar.STATISTICS_FORK_INIT_POLICY;
                operation = buildForkMsg(subType, pid, heriPid, 0);
            } else if (CPUKeyBackground.getInstance().checkIsTargetGroup(heriPid, CPUKeyBackground.GRP_KEY_BACKGROUND)) {
                CPUKeyBackground.getInstance().sendSwitchGroupMessage(heriPid, 105);
            }
        } else if (curSchedGroup != heriPidcurSchedGroup) {
            if (curSchedGroup == 1) {
                curSchedGroup = -1;
            }
            isForkOk = setProcessGroup(heriPid, curSchedGroup);
            isGroupSet = true;
            reason = " parent process group  is not equal to the child";
            subType = CpuDumpRadar.STATISTICS_FORK_APP_POLICY;
            operation = buildForkMsg(subType, pid, heriPid, curSchedGroup);
        } else if (curSchedGroup == 0 && getThreadPriority(heriPid) >= 10) {
            isForkOk = setProcessGroup(heriPid, curSchedGroup);
            isGroupSet = true;
            reason = " process priority lower than THREAD_PRIORITY_BACKGROUND";
            subType = CpuDumpRadar.STATISTICS_FORK_APP_POLICY;
            operation = buildForkMsg(subType, pid, heriPid, curSchedGroup);
        }
        if (isForkOk) {
            CpuDumpRadar.getInstance().insertDumpInfo(time, operation, reason, subType);
        } else if (isGroupSet) {
            return -1;
        }
        return 0;
    }

    private String buildForkMsg(String subType, int pid, int heriPid, int group) {
        StringBuilder strMsg = new StringBuilder();
        strMsg.append(subType).append(" setProcessGroup parent pid is ").append(pid);
        strMsg.append(" child pid is ").append(heriPid).append(" to group ").append(group);
        return strMsg.toString();
    }

    private int getThreadPriority(int pid) {
        long oldId1 = Binder.clearCallingIdentity();
        int prio = Integer.MIN_VALUE;
        try {
            prio = Process.getThreadPriority(pid);
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, "getThreadPriority pid " + pid + " has illegal argument");
        } catch (SecurityException e2) {
            AwareLog.e(TAG, "getThreadPriority pid" + pid + " has no permission");
        } catch (RuntimeException e3) {
            AwareLog.e(TAG, "getThreadPriority pid " + pid + " is not existed");
        } finally {
            Binder.restoreCallingIdentity(oldId1);
        }
        return prio;
    }

    private boolean setProcessGroup(int pid, int schedGroup) {
        long oldId1 = Binder.clearCallingIdentity();
        boolean isSuccess = false;
        if (schedGroup == 8) {
            schedGroup = 2;
        }
        try {
            Process.setProcessGroup(pid, schedGroup);
            isSuccess = true;
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, "setProcessGroup pid" + pid + " has illegal argument");
        } catch (SecurityException e2) {
            AwareLog.e(TAG, "setProcessGroup pid" + pid + " has no permission");
        } catch (RuntimeException e3) {
            AwareLog.e(TAG, "setProcessGroup pid" + pid + " is not existed");
        } finally {
            Binder.restoreCallingIdentity(oldId1);
        }
        return isSuccess;
    }

    private boolean isValidHeriPid(int pid, int ppid, int recordPPid, int uid) {
        int pid_uid = Process.getUidForPid(pid);
        if (pid_uid <= 0 || ppid <= 0) {
            return false;
        }
        if (1 != ppid) {
            int ppid_uid = Process.getUidForPid(ppid);
            return ppid_uid > 0 && pid_uid == uid && ppid_uid == uid && ppid == recordPPid;
        } else if (pid_uid != uid) {
            return false;
        }
    }

    private void changeProcessGroupFromList(int pid, int uid, InheritInfo info) {
        if (info != null) {
            int i = 0;
            while (i < info.getListSize()) {
                int heriPid = info.getPidFromList(i);
                int recordPPid = info.getPPidFromList(i);
                int parentPid = Process.getParentPid(heriPid);
                if (!isValidHeriPid(heriPid, parentPid, recordPPid, uid)) {
                    info.removeFromPidList(heriPid);
                } else if (setHeriPidGroup(parentPid, heriPid) < 0) {
                    info.removeFromPidList(heriPid);
                } else {
                    i++;
                }
            }
        }
    }

    private void handleProcessFork(int pid, int uid, boolean foregroundActivities) {
        InheritInfo info = (InheritInfo) this.mForkList.get(pid);
        if (foregroundActivities) {
            changeProcessGroupFromList(pid, uid, info);
            return;
        }
        if (info == null) {
            info = new InheritInfo();
        } else if (info.getComputeCount() >= 5) {
            changeProcessGroupFromList(pid, uid, info);
            return;
        } else {
            info.clearPidList();
        }
        info.addComputeCount();
        StringBuilder str = new StringBuilder();
        str.append(PATH_UID_INFO);
        str.append(uid);
        File[] files = new File(str.toString()).listFiles();
        if (files == null) {
            AwareLog.e(TAG, "files null ");
            return;
        }
        for (File dir : files) {
            if (dir.isDirectory() && -1 != dir.getName().indexOf("pid_")) {
                String[] a = dir.getName().split("_");
                if (a.length > 1) {
                    StringBuilder targetPath = new StringBuilder();
                    targetPath.append(PATH_UID_INFO);
                    targetPath.append(uid).append('/');
                    targetPath.append(dir.getName()).append('/');
                    targetPath.append(PATH_GROUPPROCS);
                    getForkPidList(a[1], targetPath.toString(), info);
                }
            }
        }
        this.mForkList.put(pid, info);
    }

    private void closeBufferedReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeBufferedReader exception " + e.getMessage());
            }
        }
    }

    private void closeInputStreamReader(InputStreamReader isr) {
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeInputStreamReader exception " + e.getMessage());
            }
        }
    }

    private void closeFileInputStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeFileInputStream exception " + e.getMessage());
            }
        }
    }

    private void getForkPidList(String pid, String path, InheritInfo info) {
        NumberFormatException e;
        Throwable th;
        FileNotFoundException e2;
        UnsupportedEncodingException e3;
        IOException e4;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            FileInputStream fis2 = new FileInputStream(path);
            try {
                InputStreamReader isr2 = new InputStreamReader(fis2, "UTF-8");
                try {
                    BufferedReader br2 = new BufferedReader(isr2);
                    try {
                        String str = "";
                        while (true) {
                            str = br2.readLine();
                            if (str == null) {
                                closeBufferedReader(br2);
                                closeInputStreamReader(isr2);
                                closeFileInputStream(fis2);
                                return;
                            } else if (str.indexOf(pid) == -1) {
                                int heriPid = Integer.parseInt(str.trim());
                                int parentPid = Process.getParentPid(heriPid);
                                setHeriPidGroup(parentPid, heriPid);
                                info.addToPidList(heriPid, parentPid);
                            }
                        }
                    } catch (NumberFormatException e5) {
                        e = e5;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        try {
                            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                            closeBufferedReader(br);
                            closeInputStreamReader(isr);
                            closeFileInputStream(fis);
                        } catch (Throwable th2) {
                            th = th2;
                            closeBufferedReader(br);
                            closeInputStreamReader(isr);
                            closeFileInputStream(fis);
                            throw th;
                        }
                    } catch (FileNotFoundException e6) {
                        e2 = e6;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (UnsupportedEncodingException e7) {
                        e3 = e7;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (IOException e8) {
                        e4 = e8;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "IOException " + e4.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (Throwable th3) {
                        th = th3;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        throw th;
                    }
                } catch (NumberFormatException e9) {
                    e = e9;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (FileNotFoundException e10) {
                    e2 = e10;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (UnsupportedEncodingException e11) {
                    e3 = e11;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (IOException e12) {
                    e4 = e12;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (Throwable th4) {
                    th = th4;
                    isr = isr2;
                    fis = fis2;
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    throw th;
                }
            } catch (NumberFormatException e13) {
                e = e13;
                fis = fis2;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (FileNotFoundException e14) {
                e2 = e14;
                fis = fis2;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (UnsupportedEncodingException e15) {
                e3 = e15;
                fis = fis2;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (IOException e16) {
                e4 = e16;
                fis = fis2;
                AwareLog.e(TAG, "IOException " + e4.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (Throwable th5) {
                th = th5;
                fis = fis2;
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
                throw th;
            }
        } catch (NumberFormatException e17) {
            e = e17;
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
        } catch (FileNotFoundException e18) {
            e2 = e18;
            AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
        } catch (UnsupportedEncodingException e19) {
            e3 = e19;
            AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
        } catch (IOException e20) {
            e4 = e20;
            AwareLog.e(TAG, "IOException " + e4.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
        }
    }

    private void handleProcessDie(int pid, int uid) {
        if (this.mForkList.get(pid) != null) {
            this.mForkList.remove(pid);
        }
    }
}
