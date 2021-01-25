package com.android.server.rms.iaware.cpu;

import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.rms.iaware.AwareLog;
import android.util.SparseArray;
import com.android.server.rms.iaware.AwareCallback;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.os.ProcessExt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class CpuProcessInherit {
    private static final int GROUP_FOREGROUP = 1;
    private static final int INIT_PROC_PID = 1;
    private static final int MAX_TRY_FORK_TIMES = 5;
    private static final int MSG_APP_BACKGROUND = 2;
    private static final int MSG_APP_DIED = 3;
    private static final int MSG_APP_FOREGROUND = 1;
    private static final String PATH_GROUPPROCS = "cgroup.procs";
    private static final String PATH_UID_INFO = "/acct/uid_";
    private static final String TAG = "CpuProcessInherit";
    private SparseArray<InheritInfo> mForkList = new SparseArray<>();
    private ProcessHandler mProcessHandler = new ProcessHandler();
    private ProcessObserverStub mProcessObserver = new ProcessObserverStub();

    public void registerPorcessObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mProcessObserver);
    }

    public void unregisterPorcessObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mProcessObserver);
    }

    /* access modifiers changed from: private */
    public class ProcessObserverStub extends IProcessObserverEx {
        private ProcessObserverStub() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (CpuFeature.isCpusetEnable()) {
                Message observerMsg = CpuProcessInherit.this.mProcessHandler.obtainMessage();
                observerMsg.arg1 = pid;
                observerMsg.arg2 = uid;
                if (!foregroundActivities) {
                    observerMsg.what = 2;
                    NetManager.getInstance().sendMsgToNetMng(pid, uid, 1);
                } else {
                    observerMsg.what = 1;
                    NetManager.getInstance().sendMsgToNetMng(pid, uid, 0);
                }
                CpuProcessInherit.this.mProcessHandler.sendMessage(observerMsg);
            }
        }

        public void onProcessDied(int pid, int uid) {
            NetManager.getInstance().sendMsgToNetMng(pid, uid, 2, 1000);
            Message observerMsg = CpuProcessInherit.this.mProcessHandler.obtainMessage();
            observerMsg.arg1 = pid;
            observerMsg.arg2 = uid;
            observerMsg.what = 3;
            CpuProcessInherit.this.mProcessHandler.sendMessage(observerMsg);
        }
    }

    private int getProcessGrp(int pid) {
        long oldId = Binder.clearCallingIdentity();
        int group = Integer.MIN_VALUE;
        try {
            group = ProcessExt.getProcessGroup(pid);
        } catch (IllegalArgumentException | SecurityException e) {
            AwareLog.e(TAG, "getProcessGroup pid " + pid + " exception");
        } catch (RuntimeException e2) {
            AwareLog.e(TAG, "getProcessGroup pid" + pid + " is not existed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId);
            throw th;
        }
        Binder.restoreCallingIdentity(oldId);
        return group;
    }

    private int setHeriPidGroup(int pid, int heriPid) {
        int curSchedGroup = getProcessGrp(pid);
        int heriPidcurSchedGroup = getProcessGrp(heriPid);
        if (curSchedGroup == Integer.MIN_VALUE || heriPidcurSchedGroup == Integer.MIN_VALUE) {
            return -1;
        }
        boolean isForkOk = false;
        boolean isGroupSet = false;
        if (pid == 1) {
            if (heriPidcurSchedGroup != 0) {
                isForkOk = setProcessGroup(heriPid, 0);
                isGroupSet = true;
            } else if (CpuKeyBackground.getInstance().checkIsTargetGroup(heriPid, CpuKeyBackground.GRP_KEY_BACKGROUND)) {
                CpuKeyBackground.getInstance().sendSwitchGroupMessage(heriPid, 105);
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
        long callingId = Binder.clearCallingIdentity();
        int prio = Integer.MIN_VALUE;
        try {
            prio = Process.getThreadPriority(pid);
        } catch (IllegalArgumentException | SecurityException e) {
            AwareLog.e(TAG, "getThreadPriority pid " + pid + " exception");
        } catch (RuntimeException e2) {
            AwareLog.e(TAG, "getThreadPriority pid " + pid + " is not existed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Binder.restoreCallingIdentity(callingId);
        return prio;
    }

    private boolean setProcessGroup(int pid, int schedGroup) {
        long callingId = Binder.clearCallingIdentity();
        boolean isSuccess = false;
        if (schedGroup == 10) {
            schedGroup = 5;
        }
        try {
            ProcessExt.setProcessGroup(pid, schedGroup);
            isSuccess = true;
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, "setProcessGroup pid" + pid + " has illegal argument");
        } catch (SecurityException e2) {
            AwareLog.e(TAG, "setProcessGroup pid" + pid + " has no permission");
        } catch (RuntimeException e3) {
            AwareLog.e(TAG, "setProcessGroup pid" + pid + " is not existed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Binder.restoreCallingIdentity(callingId);
        return isSuccess;
    }

    private boolean isValidHeriPid(int pid, int ppid, int recordParentPid, int uid) {
        int pidUid = ProcessExt.getUidForPid(pid);
        if (pidUid <= 0 || ppid <= 0) {
            return false;
        }
        if (ppid != 1) {
            int ppidUid = ProcessExt.getUidForPid(ppid);
            if (ppidUid <= 0 || pidUid != uid || ppidUid != uid || ppid != recordParentPid) {
                return false;
            }
        } else if (pidUid != uid) {
            return false;
        }
        return true;
    }

    private void changeProcessGroupFromList(int pid, int uid, InheritInfo info) {
        if (info != null) {
            int i = 0;
            while (i < info.getListSize()) {
                int heriPid = info.getPidFromList(i);
                int recordParentPid = info.getParentPidFromList(i);
                int parentPid = ProcessExt.getParentPid(heriPid);
                if (!isValidHeriPid(heriPid, parentPid, recordParentPid, uid)) {
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
    /* access modifiers changed from: public */
    private void handleProcessFork(int pid, int uid, boolean foregroundActivities) {
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
            if (dir.isDirectory() && dir.getName().indexOf("pid_") != -1) {
                String[] pidList = dir.getName().split("_");
                if (pidList.length > 1) {
                    getForkPidList(pidList[1], PATH_UID_INFO + uid + '/' + dir.getName() + '/' + PATH_GROUPPROCS, info);
                }
            }
        }
        this.mForkList.put(pid, info);
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
                String line = br.readLine();
                if (line == null) {
                    break;
                } else if (line.indexOf(pid) == -1) {
                    int heriPid = Integer.parseInt(line.trim());
                    int parentPid = ProcessExt.getParentPid(heriPid);
                    setHeriPidGroup(parentPid, heriPid);
                    info.addToPidList(heriPid, parentPid);
                }
            }
        } catch (UnsupportedEncodingException | NumberFormatException e) {
            AwareLog.e(TAG, "Invalid NumberFormat or UnsupportedEncoding");
        } catch (FileNotFoundException e2) {
            AwareLog.e(TAG, "Invalid file");
        } catch (IOException e3) {
            AwareLog.e(TAG, "IOException");
        } catch (Throwable th) {
            FileContent.closeBufferedReader(null);
            FileContent.closeInputStreamReader(null);
            FileContent.closeFileInputStream(null);
            throw th;
        }
        FileContent.closeBufferedReader(br);
        FileContent.closeInputStreamReader(isr);
        FileContent.closeFileInputStream(fis);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProcessDie(int pid, int uid) {
        if (this.mForkList.get(pid) != null) {
            this.mForkList.remove(pid);
        }
    }

    /* access modifiers changed from: private */
    public class ProcessHandler extends Handler {
        private ProcessHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int pid = msg.arg1;
            int uid = msg.arg2;
            int i = msg.what;
            if (i == 1) {
                CpuProcessInherit.this.handleProcessFork(pid, uid, true);
            } else if (i == 2) {
                CpuMultiDisplay.getInstance().multiDisplayResetVip(pid);
                CpuProcessInherit.this.handleProcessFork(pid, uid, false);
            } else if (i == 3) {
                CpuProcessInherit.this.handleProcessDie(pid, uid);
                AwareRmsRtgSchedPlugin.getInstance().processDied(pid);
                CpuMultiDisplay.getInstance().removePidDisplayInfo(pid);
                CpuMultiDisplay.getInstance().multiDisplayProcessDie(pid);
            }
        }
    }
}
