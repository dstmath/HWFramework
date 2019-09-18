package com.android.server.rms.iaware.cpu;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
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
    private SparseArray<InheritInfo> mForkList = new SparseArray<>();
    /* access modifiers changed from: private */
    public ProcessHandler mProcessHandler = new ProcessHandler();
    private MyIProcessObserver mProcessObserver = new MyIProcessObserver();

    class MyIProcessObserver extends IProcessObserver.Stub {
        MyIProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (CPUFeature.isCpusetEnable()) {
                Message observerMsg = CPUProcessInherit.this.mProcessHandler.obtainMessage();
                observerMsg.arg1 = pid;
                observerMsg.arg2 = uid;
                if (!foregroundActivities) {
                    observerMsg.what = 2;
                    NetManager.getInstance().sendMsgToNetMng(pid, uid, 1);
                } else {
                    observerMsg.what = 1;
                    NetManager.getInstance().sendMsgToNetMng(pid, uid, 0);
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
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId);
            throw th;
        }
        Binder.restoreCallingIdentity(oldId);
        if (curSchedGroup == Integer.MIN_VALUE || heriPidcurSchedGroup == Integer.MIN_VALUE) {
            return -1;
        }
        boolean isForkOk = false;
        boolean isGroupSet = false;
        if (1 == pid) {
            if (heriPidcurSchedGroup != 0) {
                isForkOk = setProcessGroup(heriPid, 0);
                isGroupSet = true;
            } else if (CPUKeyBackground.getInstance().checkIsTargetGroup(heriPid, CPUKeyBackground.GRP_KEY_BACKGROUND)) {
                CPUKeyBackground.getInstance().sendSwitchGroupMessage(heriPid, 105);
            }
        } else if (curSchedGroup != heriPidcurSchedGroup) {
            if (curSchedGroup == 1) {
                curSchedGroup = -1;
            }
            isForkOk = setProcessGroup(heriPid, curSchedGroup);
            isGroupSet = true;
        } else if (curSchedGroup == 0 && getThreadPriority(heriPid) >= 10) {
            isForkOk = setProcessGroup(heriPid, curSchedGroup);
            isGroupSet = true;
        }
        if (isForkOk || !isGroupSet) {
            return 0;
        }
        return -1;
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
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId1);
            throw th;
        }
        Binder.restoreCallingIdentity(oldId1);
        return prio;
    }

    private boolean setProcessGroup(int pid, int schedGroup) {
        long oldId1 = Binder.clearCallingIdentity();
        boolean isSuccess = false;
        if (schedGroup == 10) {
            schedGroup = 5;
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
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId1);
            throw th;
        }
        Binder.restoreCallingIdentity(oldId1);
        return isSuccess;
    }

    private boolean isValidHeriPid(int pid, int ppid, int recordPPid, int uid) {
        int pid_uid = Process.getUidForPid(pid);
        if (pid_uid <= 0 || ppid <= 0) {
            return false;
        }
        if (1 != ppid) {
            int ppid_uid = Process.getUidForPid(ppid);
            if (ppid_uid > 0 && pid_uid == uid && ppid_uid == uid && ppid == recordPPid) {
                return true;
            }
            return false;
        } else if (pid_uid != uid) {
            return false;
        }
        return true;
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

    /* access modifiers changed from: private */
    public void handleProcessFork(int pid, int uid, boolean foregroundActivities) {
        InheritInfo info = this.mForkList.get(pid);
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
        File[] files = new File(PATH_UID_INFO + uid).listFiles();
        if (files == null) {
            AwareLog.e(TAG, "files null ");
            return;
        }
        for (File dir : files) {
            if (dir.isDirectory() && -1 != dir.getName().indexOf("pid_")) {
                String[] a = dir.getName().split("_");
                if (a.length > 1) {
                    getForkPidList(a[1], PATH_UID_INFO + uid + '/' + dir.getName() + '/' + PATH_GROUPPROCS, info);
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
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            while (true) {
                String readLine = br.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                } else if (line.indexOf(pid) == -1) {
                    int heriPid = Integer.parseInt(line.trim());
                    int parentPid = Process.getParentPid(heriPid);
                    setHeriPidGroup(parentPid, heriPid);
                    info.addToPidList(heriPid, parentPid);
                }
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
        } catch (FileNotFoundException e2) {
            AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
        } catch (UnsupportedEncodingException e3) {
            AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
        } catch (IOException e4) {
            AwareLog.e(TAG, "IOException " + e4.getMessage());
        } catch (Throwable th) {
            closeBufferedReader(null);
            closeInputStreamReader(null);
            closeFileInputStream(null);
            throw th;
        }
        closeBufferedReader(br);
        closeInputStreamReader(isr);
        closeFileInputStream(fis);
    }

    /* access modifiers changed from: private */
    public void handleProcessDie(int pid, int uid) {
        if (this.mForkList.get(pid) != null) {
            this.mForkList.remove(pid);
        }
    }
}
