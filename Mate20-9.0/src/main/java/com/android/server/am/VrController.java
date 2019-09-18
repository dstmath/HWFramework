package com.android.server.am;

import android.content.ComponentName;
import android.os.Process;
import android.service.vr.IPersistentVrStateCallbacks;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.util.proto.ProtoUtils;
import com.android.server.LocalServices;
import com.android.server.vr.VrManagerInternal;

final class VrController {
    private static final int FLAG_NON_VR_MODE = 0;
    private static final int FLAG_PERSISTENT_VR_MODE = 2;
    private static final int FLAG_VR_MODE = 1;
    private static int[] ORIG_ENUMS = {0, 1, 2};
    private static int[] PROTO_ENUMS = {0, 1, 2};
    private static final String TAG = "VrController";
    /* access modifiers changed from: private */
    public final Object mGlobalAmLock;
    private final IPersistentVrStateCallbacks mPersistentVrModeListener = new IPersistentVrStateCallbacks.Stub() {
        public void onPersistentVrStateChanged(boolean enabled) {
            synchronized (VrController.this.mGlobalAmLock) {
                if (enabled) {
                    try {
                        int unused = VrController.this.setVrRenderThreadLocked(0, 3, true);
                        VrController.access$276(VrController.this, 2);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    int unused2 = VrController.this.setPersistentVrRenderThreadLocked(0, true);
                    VrController.access$272(VrController.this, -3);
                }
            }
        }
    };
    private int mVrRenderThreadTid = 0;
    private int mVrState = 0;

    static /* synthetic */ int access$272(VrController x0, int x1) {
        int i = x0.mVrState & x1;
        x0.mVrState = i;
        return i;
    }

    static /* synthetic */ int access$276(VrController x0, int x1) {
        int i = x0.mVrState | x1;
        x0.mVrState = i;
        return i;
    }

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
        if (proc.curSchedGroup == 3) {
            setVrRenderThreadLocked(proc.vrThreadTid, proc.curSchedGroup, true);
        } else if (proc.vrThreadTid == this.mVrRenderThreadTid) {
            clearVrRenderThreadLocked(true);
        }
    }

    public boolean onVrModeChanged(ActivityRecord record) {
        int processId;
        VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        boolean z = false;
        if (vrService == null) {
            return false;
        }
        synchronized (this.mGlobalAmLock) {
            try {
                if (record.requestedVrComponent != null) {
                    z = true;
                }
                boolean vrMode = z;
                ComponentName requestedPackage = record.requestedVrComponent;
                int userId = record.userId;
                ComponentName callingPackage = record.info.getComponentName();
                boolean changed = changeVrModeLocked(vrMode, record.app);
                try {
                    if (record.app != null) {
                        processId = record.app.pid;
                    } else {
                        processId = -1;
                    }
                } catch (Throwable th) {
                    th = th;
                    boolean z2 = changed;
                    throw th;
                }
                try {
                    vrService.setVrMode(vrMode, requestedPackage, userId, processId, callingPackage);
                    return changed;
                } catch (Throwable th2) {
                    th = th2;
                    boolean z3 = changed;
                    int i = processId;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
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
            int i = 0;
            if (!inVrMode()) {
                Slog.w(TAG, "VR thread cannot be set when not in VR mode!");
            } else {
                setVrRenderThreadLocked(tid, proc.curSchedGroup, false);
            }
            if (tid > 0) {
                i = tid;
            }
            proc.vrThreadTid = i;
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
        boolean changed = true;
        if (vrMode) {
            this.mVrState |= 1;
        } else {
            this.mVrState &= -2;
        }
        if (oldVrState == this.mVrState) {
            changed = false;
        }
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

    /* access modifiers changed from: private */
    public int setPersistentVrRenderThreadLocked(int newTid, boolean suppressLogs) {
        if (hasPersistentVrFlagSet()) {
            return updateVrRenderThreadLocked(newTid, suppressLogs);
        }
        if (!suppressLogs) {
            Slog.w(TAG, "Failed to set persistent VR thread, system not in persistent VR mode.");
        }
        return this.mVrRenderThreadTid;
    }

    /* access modifiers changed from: private */
    public int setVrRenderThreadLocked(int newTid, int schedGroup, boolean suppressLogs) {
        boolean inVr = inVrMode();
        boolean inPersistentVr = hasPersistentVrFlagSet();
        if (inVr && !inPersistentVr && schedGroup == 3) {
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

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        ProtoUtils.writeBitWiseFlagsToProtoEnum(proto, 2259152797697L, this.mVrState, ORIG_ENUMS, PROTO_ENUMS);
        proto.write(1120986464258L, this.mVrRenderThreadTid);
        proto.end(token);
    }
}
