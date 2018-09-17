package com.android.server.rms.ipcchecker;

import android.os.RemoteException;
import android.rms.IHwSysResManager;
import android.rms.utils.Utils;
import android.util.Log;
import android.util.ZRHung;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.HwSysResManagerService;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.rms.record.ResourceRecordStore;
import com.android.server.rms.record.ResourceUtils;

public class HwIpcMonitorImpl implements IHwIpcMonitor {
    private static final String TAG = "RMS.HwIpcMonitorImpl";
    private static final short ZRHUNG_WP_IPC_OBJECT = (short) 19;
    protected long mLastRefreshTime;
    protected Object mLock;
    protected String mName;
    protected int mRecoverCount;

    protected HwIpcMonitorImpl(Object object, String name) {
        this.mRecoverCount = 1;
        this.mName = name;
        this.mLock = object;
        this.mLastRefreshTime = 0;
    }

    protected HwIpcMonitorImpl() {
        this.mRecoverCount = 1;
        this.mLastRefreshTime = 0;
    }

    public static IHwIpcMonitor getHwIpcMonitor(Object object, String type, String name) {
        if (type.equals("location")) {
            return LocationIpcMonitor.getInstance(object, name);
        }
        if (type.equals("backup")) {
            return BackupIpcMonitor.getInstance(object, name);
        }
        return null;
    }

    public void doMonitor() {
        if (Utils.DEBUG) {
            Log.d(TAG, " Begin checkt this monitor! " + this.mName);
        }
        synchronized (this.mLock) {
            if (Utils.DEBUG) {
                Log.d(TAG, " check this monitor! " + this.mName);
            }
        }
    }

    public boolean action() {
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.d(TAG, PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY + this.mName);
        }
        long currentTime = System.currentTimeMillis();
        if (Utils.IS_DEBUG_VERSION || currentTime - this.mLastRefreshTime >= HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS) {
            uploadIPCMonitorFaults(this.mName);
            this.mLastRefreshTime = currentTime;
            this.mRecoverCount = 1;
        } else {
            this.mRecoverCount++;
        }
        return false;
    }

    public boolean action(Object lock) {
        return false;
    }

    public String getMonitorName() {
        return this.mName;
    }

    protected void uploadIPCMonitorFaults(String name) {
        if (name == null) {
            name = "general";
        }
        uploadIPCMonitorFaultToZerohung(name, this.mRecoverCount);
        IHwSysResManager mgr = HwSysResManagerService.self().getHwSysResManagerService();
        if (mgr != null) {
            try {
                mgr.recordResourceOverloadStatus(-1, name, 33, this.mRecoverCount, -1, this.mRecoverCount, null);
            } catch (RemoteException e) {
                if (Utils.DEBUG) {
                    Log.e(TAG, "upload IPCTIMEOUT error failed:" + e.getMessage());
                }
            }
        }
    }

    protected void uploadIPCMonitorFaultToZerohung(String name, int recoverCount) {
        StringBuilder message = new StringBuilder();
        message.append("MonitorName:").append(name).append(" RecoveryCount:").append(recoverCount);
        if (!ZRHung.sendHungEvent((short) 19, "n=system_server", message.toString())) {
            Log.e(TAG, " ZRHung.sendHungEvent failed!");
        }
    }

    protected void uploadIPCMonitorFaultToIMonitor(String pkgName, int overloadCount, int timeoutDuration) {
        if (Utils.DEBUG) {
            Log.d(TAG, "uploadIPCMonitorFaultToIMonitor" + this.mLock);
        }
        ResourceUtils.uploadBigDataLogToIMonitor(33, pkgName, overloadCount, timeoutDuration);
    }

    protected void uploadIPCMonitorFaultToBigDatainfo(String pkgName, int overloadCount, int recoverCount) {
        ResourceRecordStore resourceRecordStore = ResourceRecordStore.getInstance();
        if (resourceRecordStore != null) {
            if (Utils.DEBUG) {
                Log.d(TAG, "uploadIPCMonitorFaultToBigDatainfo" + this.mLock);
            }
            resourceRecordStore.createAndCheckUploadBigDataInfos(-1, 33, pkgName, overloadCount, -1, recoverCount, null);
        }
    }
}
