package com.android.server.am;

import android.content.ComponentName;
import android.os.Process;
import android.service.vr.IPersistentVrStateCallbacks;
import android.service.vr.IPersistentVrStateCallbacks.Stub;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.vr.VrManagerInternal;

final class VrController {
    private static final int FLAG_NON_VR_MODE = 0;
    private static final int FLAG_PERSISTENT_VR_MODE = 2;
    private static final int FLAG_VR_MODE = 1;
    private static final String TAG = "VrController";
    private final Object mGlobalAmLock;
    private final IPersistentVrStateCallbacks mPersistentVrModeListener = new Stub() {
        public void onPersistentVrStateChanged(boolean enabled) {
            synchronized (VrController.this.mGlobalAmLock) {
                VrController vrController;
                if (enabled) {
                    VrController.this.setVrRenderThreadLocked(0, 2, true);
                    vrController = VrController.this;
                    vrController.mVrState = vrController.mVrState | 2;
                } else {
                    VrController.this.setPersistentVrRenderThreadLocked(0, true);
                    vrController = VrController.this;
                    vrController.mVrState = vrController.mVrState & -3;
                }
            }
        }
    };
    private int mVrRenderThreadTid = 0;
    private int mVrState = 0;

    public VrController(Object globalAmLock) {
        this.mGlobalAmLock = globalAmLock;
    }

    public void onSystemReady() {
        VrManagerInternal vrManagerInternal = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        if (vrManagerInternal != null) {
            vrManagerInternal.addPersistentVrModeStateListener(this.mPersistentVrModeListener);
        }
    }

    public void onTopProcChangedLocked(ProcessRecord proc) {
        if (proc.curSchedGroup == 2) {
            setVrRenderThreadLocked(proc.vrThreadTid, proc.curSchedGroup, true);
        } else if (proc.vrThreadTid == this.mVrRenderThreadTid) {
            clearVrRenderThreadLocked(true);
        }
    }

    public boolean onVrModeChanged(ActivityRecord record) {
        VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        if (vrService == null) {
            return false;
        }
        boolean vrMode;
        ComponentName requestedPackage;
        int userId;
        ComponentName callingPackage;
        boolean changed;
        synchronized (this.mGlobalAmLock) {
            vrMode = record.requestedVrComponent != null;
            requestedPackage = record.requestedVrComponent;
            userId = record.userId;
            callingPackage = record.info.getComponentName();
            changed = changeVrModeLocked(vrMode, record.app);
        }
        vrService.setVrMode(vrMode, requestedPackage, userId, callingPackage);
        return changed;
    }

    public void setVrThreadLocked(int tid, int pid, ProcessRecord proc) {
        if (hasPersistentVrFlagSet()) {
            Slog.w(TAG, "VR thread cannot be set in persistent VR mode!");
        } else if (proc == null) {
            Slog.w(TAG, "Persistent VR thread not set, calling process doesn't exist!");
        } else {
            if (tid != 0) {
                enforceThreadInProcess(tid, pid);
            }
            if (inVrMode()) {
                setVrRenderThreadLocked(tid, proc.curSchedGroup, false);
            } else {
                Slog.w(TAG, "VR thread cannot be set when not in VR mode!");
            }
            if (tid <= 0) {
                tid = 0;
            }
            proc.vrThreadTid = tid;
        }
    }

    public void setPersistentVrThreadLocked(int tid, int pid, ProcessRecord proc) {
        if (!hasPersistentVrFlagSet()) {
            Slog.w(TAG, "Persistent VR thread may only be set in persistent VR mode!");
        } else if (proc == null) {
            Slog.w(TAG, "Persistent VR thread not set, calling process doesn't exist!");
        } else {
            if (tid != 0) {
                enforceThreadInProcess(tid, pid);
            }
            setPersistentVrRenderThreadLocked(tid, false);
        }
    }

    public boolean shouldDisableNonVrUiLocked() {
        return this.mVrState != 0;
    }

    private boolean changeVrModeLocked(boolean vrMode, ProcessRecord proc) {
        int oldVrState = this.mVrState;
        if (vrMode) {
            this.mVrState |= 1;
        } else {
            this.mVrState &= -2;
        }
        boolean changed = oldVrState != this.mVrState;
        if (changed) {
            if (proc == null) {
                clearVrRenderThreadLocked(false);
            } else if (proc.vrThreadTid > 0) {
                setVrRenderThreadLocked(proc.vrThreadTid, proc.curSchedGroup, false);
            }
        }
        return changed;
    }

    private int updateVrRenderThreadLocked(int newTid, boolean suppressLogs) {
        if (this.mVrRenderThreadTid == newTid) {
            return this.mVrRenderThreadTid;
        }
        if (this.mVrRenderThreadTid > 0) {
            ActivityManagerService.scheduleAsRegularPriority(this.mVrRenderThreadTid, suppressLogs);
            this.mVrRenderThreadTid = 0;
        }
        if (newTid > 0) {
            this.mVrRenderThreadTid = newTid;
            ActivityManagerService.scheduleAsFifoPriority(this.mVrRenderThreadTid, suppressLogs);
        }
        return this.mVrRenderThreadTid;
    }

    private int setPersistentVrRenderThreadLocked(int newTid, boolean suppressLogs) {
        if (hasPersistentVrFlagSet()) {
            return updateVrRenderThreadLocked(newTid, suppressLogs);
        }
        if (!suppressLogs) {
            Slog.w(TAG, "Failed to set persistent VR thread, system not in persistent VR mode.");
        }
        return this.mVrRenderThreadTid;
    }

    private int setVrRenderThreadLocked(int newTid, int schedGroup, boolean suppressLogs) {
        boolean inVr = inVrMode();
        boolean inPersistentVr = hasPersistentVrFlagSet();
        if (inVr && !inPersistentVr && schedGroup == 2) {
            return updateVrRenderThreadLocked(newTid, suppressLogs);
        }
        if (!suppressLogs) {
            String reason = "caller is not the current top application.";
            if (!inVr) {
                reason = "system not in VR mode.";
            } else if (inPersistentVr) {
                reason = "system in persistent VR mode.";
            }
            Slog.w(TAG, "Failed to set VR thread, " + reason);
        }
        return this.mVrRenderThreadTid;
    }

    private void clearVrRenderThreadLocked(boolean suppressLogs) {
        updateVrRenderThreadLocked(0, suppressLogs);
    }

    private void enforceThreadInProcess(int tid, int pid) {
        if (!Process.isThreadInProcess(pid, tid)) {
            throw new IllegalArgumentException("VR thread does not belong to process");
        }
    }

    private boolean inVrMode() {
        return (this.mVrState & 1) != 0;
    }

    private boolean hasPersistentVrFlagSet() {
        return (this.mVrState & 2) != 0;
    }

    public String toString() {
        return String.format("[VrState=0x%x,VrRenderThreadTid=%d]", new Object[]{Integer.valueOf(this.mVrState), Integer.valueOf(this.mVrRenderThreadTid)});
    }
}
