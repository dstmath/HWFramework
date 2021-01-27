package com.android.server.am;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import com.huawei.android.app.IHwDAMonitorCallback;

/* access modifiers changed from: package-private */
public class HwDAMonitorProxy {
    private static final String DEFAULT_EMPTY_RECENT_TASK = "";
    private static final int DEFAULT_GROUP_BG_VALUE = 1;
    private static final int DEFAULT_INVALID_INT_VALUE = -1;
    private static final int DEFAULT_MINI_COUNT = 0;
    private static final String TAG = "HwDAMonitorProxy";
    IHwDAMonitorCallback mDACallback = null;
    private boolean mIsSetCallback = false;

    HwDAMonitorProxy() {
    }

    public void registerDAMonitorCallback(IHwDAMonitorCallback callback) {
        if (callback != null && !this.mIsSetCallback) {
            this.mDACallback = callback;
            this.mIsSetCallback = true;
        }
    }

    public int getActivityImportCount() {
        if (!this.mIsSetCallback) {
            return 0;
        }
        try {
            return this.mDACallback.getActivityImportCount();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return 0;
        }
    }

    public String getRecentTask() {
        if (!this.mIsSetCallback) {
            return "";
        }
        try {
            return this.mDACallback.getRecentTask();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return "";
        }
    }

    public int isCPUConfigWhiteList(String processName) {
        if (!this.mIsSetCallback) {
            return -1;
        }
        try {
            return this.mDACallback.isCpuConfigWhiteList(processName);
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return -1;
        }
    }

    public void reportScreenRecord(int uid, int pid, int status) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.reportScreenRecord(uid, pid, status);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown : ", e);
            }
        }
    }

    public void reportCamera(int uid, int status) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.reportCamera(uid, status);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown : ", e);
            }
        }
    }

    public int getCPUConfigGroupBG() {
        if (!this.mIsSetCallback) {
            return 1;
        }
        try {
            return this.mDACallback.getCpuConfigGroupBg();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return 1;
        }
    }

    public int getFirstDevSchedEventId() {
        if (!this.mIsSetCallback) {
            return -1;
        }
        try {
            return this.mDACallback.getFirstDevSchedEventId();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown : ", e);
            return -1;
        }
    }

    public void notifyActivityState(String activityInfo) {
        if (this.mIsSetCallback) {
            try {
                if (this.mDACallback != null) {
                    this.mDACallback.notifyActivityState(activityInfo);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "RemoteException thrown : ", e);
            }
        }
    }

    public void notifyProcessGroupChangeCpu(int pid, int uid, int renderThreadTid, int grp) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyProcessGroupChangeCpu(pid, uid, renderThreadTid, grp);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyProcessGroupChangeCpu thrown RemoteException!");
            }
        }
    }

    public void setVipThread(int uid, int pid, int renderThreadTid, boolean isSet, boolean isSetGroup) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.setVipThread(uid, pid, renderThreadTid, isSet, isSetGroup);
            } catch (RemoteException e) {
                Slog.w(TAG, "setVipThread thrown RemoteException!");
            }
        }
    }

    public void onPointerEvent(int action) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.onPointerEvent(action);
            } catch (RemoteException e) {
                Slog.w(TAG, "onPointerEvent thrown RemoteException!");
            }
        }
    }

    public void addPssToMap(String packageName, String procName, int uid, int pid, int procState, long pss, long uss, long swapPss, boolean test) {
        if (this.mIsSetCallback) {
            try {
                try {
                    this.mDACallback.addPssToMap(new String[]{packageName, procName}, new int[]{uid, pid, procState}, new long[]{pss, uss, swapPss}, test);
                } catch (RemoteException e) {
                }
            } catch (RemoteException e2) {
                Slog.w(TAG, "addPssToMap thrown RemoteException!");
            }
        }
    }

    public void reportAppDiedMsg(int userId, String processName, String reason) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.reportAppDiedMsg(userId, processName, reason);
            } catch (RemoteException e) {
                Slog.w(TAG, "reportAppDiedMsg thrown RemoteException!");
            }
        }
    }

    public int killProcessGroupForQuickKill(int uid, int pid) {
        if (!this.mIsSetCallback) {
            return -1;
        }
        try {
            return this.mDACallback.killProcessGroupForQuickKill(uid, pid);
        } catch (RemoteException e) {
            Slog.w(TAG, "killProcessGroupForQuickKill thrown RemoteException!");
            return -1;
        }
    }

    public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.noteProcessStart(new String[]{packageName, processName, launcherMode, reason}, pid, uid, started);
            } catch (RemoteException e) {
                Slog.w(TAG, "noteProcessStart thrown RemoteException!");
            }
        }
    }

    public void onWakefulnessChanged(int wakefulness) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.onWakefulnessChanged(wakefulness);
            } catch (RemoteException e) {
                Slog.w(TAG, "onWakefulnessChanged thrown RemoteException!");
            }
        }
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyProcessGroupChange(pid, uid);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyProcessGroupChange thrown RemoteException!");
            }
        }
    }

    public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyProcessStatusChange(pkg, process, hostingType, pid, uid);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyProcessStatusChange thrown RemoteException!");
            }
        }
    }

    public void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyProcessWillDie(new boolean[]{byForceStop, crashed, byAnr}, packageName, pid, uid);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyProcessWillDie thrown RemoteException!");
            }
        }
    }

    public void notifyProcessDied(int pid, int uid) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.notifyProcessDied(pid, uid);
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyProcessDied thrown RemoteException!");
            }
        }
    }

    public int resetAppMngOomAdj(int maxAdj, String packageName) {
        if (!this.mIsSetCallback) {
            return maxAdj;
        }
        try {
            return this.mDACallback.resetAppMngOomAdj(maxAdj, packageName);
        } catch (RemoteException e) {
            Slog.w(TAG, "resetAppMngOomAdj thrown RemoteException!");
            return maxAdj;
        }
    }

    public boolean isResourceNeeded(String resourceid) {
        if (!this.mIsSetCallback) {
            return false;
        }
        try {
            return this.mDACallback.isResourceNeeded(resourceid);
        } catch (RemoteException e) {
            Slog.w(TAG, "isResourceNeeded thrown RemoteException!");
            return false;
        }
    }

    public void reportData(String resourceid, long timestamp, Bundle args) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.reportData(resourceid, timestamp, args);
            } catch (RemoteException e) {
                Slog.w(TAG, "reportData thrown RemoteException!");
            }
        }
    }

    public boolean isExcludedInBGCheck(String pkg, String action) {
        if (!this.mIsSetCallback) {
            return false;
        }
        try {
            return this.mDACallback.isExcludedInBgCheck(pkg, action);
        } catch (RemoteException e) {
            Slog.w(TAG, "isExcludedInBGCheck thrown RemoteException!");
            return false;
        }
    }

    public void noteActivityDisplayedStart(String componentName, int uid, int pid) {
        if (this.mIsSetCallback) {
            try {
                this.mDACallback.noteActivityDisplayedStart(componentName, uid, pid);
            } catch (RemoteException e) {
                Slog.w(TAG, "noteActivityDisplayedStart thrown RemoteException!");
            }
        }
    }

    public boolean isFastKillSwitch(String processName, int uid) {
        if (!this.mIsSetCallback) {
            return false;
        }
        try {
            return this.mDACallback.isFastKillSwitch(processName, uid);
        } catch (RemoteException e) {
            Slog.w(TAG, "isFastKillSwitch thrown RemoteException!");
            return false;
        }
    }
}
