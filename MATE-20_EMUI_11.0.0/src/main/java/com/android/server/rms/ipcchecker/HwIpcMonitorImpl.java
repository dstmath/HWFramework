package com.android.server.rms.ipcchecker;

import android.os.Bundle;
import android.os.RemoteException;
import android.rms.IHwSysResManager;
import android.rms.utils.Utils;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import android.util.Log;
import android.util.ZRHung;
import com.android.internal.os.SomeArgs;
import com.android.server.rms.HwSysResManagerService;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.rms.record.ResourceRecordStore;
import com.android.server.rms.record.ResourceUtils;
import com.huawei.dfr.HwDFRFrameworkFactory;
import com.huawei.dfr.zrhung.DefaultAppEyeBinderBlock;
import com.huawei.dfr.zrhung.DefaultZrHungImpl;

public class HwIpcMonitorImpl implements IHwIpcMonitor {
    private static final long IPCOBJECT_KILL_ERECOVERYID = 401012005;
    private static final long IPCOBJECT_KILL_FAULTID = 901004000;
    private static final long IPCOBJECT_REBOOTVM_ERECOVERYID = 401012001;
    private static final long IPCOBJECT_REBOOTVM_FAULTID = 901004000;
    private static final String TAG = "RMS.HwIpcMonitorImpl";
    private static final short ZRHUNG_WP_IPC_OBJECT = 19;
    DefaultZrHungImpl mAppEyeFwkBlock;
    protected int mKillTarGetPid;
    protected long mLastRefreshTime;
    protected Object mLock;
    protected String mName;
    protected int mRecoverCount;

    protected HwIpcMonitorImpl(Object object, String name) {
        this.mKillTarGetPid = -1;
        this.mRecoverCount = 1;
        this.mName = name;
        this.mLock = object;
        this.mLastRefreshTime = 0;
    }

    protected HwIpcMonitorImpl() {
        this.mKillTarGetPid = -1;
        this.mRecoverCount = 1;
        this.mLastRefreshTime = 0;
    }

    public static IHwIpcMonitor getHwIpcMonitor(Object object, String type, String name) {
        type.hashCode();
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
        if (this.mName == null) {
            this.mName = "general";
        }
        this.mAppEyeFwkBlock = HwDFRFrameworkFactory.getZrHungFrameworkFactory().getAppEyeFwkBlock();
        uploadIPCMonitorFaultToZerohung(this.mName, this.mRecoverCount);
        if (recoverBlockingProcess(true)) {
            return true;
        }
        return false;
    }

    public boolean action(Object lock) {
        return false;
    }

    public String getMonitorName() {
        return this.mName;
    }

    public boolean recoverBlockingProcess(boolean needRecheck) {
        Log.i(TAG, "recoverBlockingProcess monitor:" + this.mName + " needRecheck:" + needRecheck);
        if (this.mLock == null) {
            Log.i(TAG, "recoverBlockingProcess failed due to null lock:" + this.mName);
            return false;
        } else if (recoverBlockingProcessInner()) {
            this.mKillTarGetPid = this.mAppEyeFwkBlock.getLockOwnerPid(this.mLock);
            ipcMonitorRecoveryBegin();
            HwSysResManagerService.self().ipcProcessEndRecovery(this);
            return true;
        } else {
            if (needRecheck) {
                if (HwSysResManagerService.self() == null) {
                    Log.i(TAG, "recoverBlockingProcess failed due to null HwSysResManagerService");
                    return false;
                }
                HwSysResManagerService.self().recheckBlockIpcProcess(this);
            }
            return false;
        }
    }

    private boolean recoverBlockingProcessInner() {
        int pid = -1;
        DefaultZrHungImpl defaultZrHungImpl = this.mAppEyeFwkBlock;
        if (defaultZrHungImpl != null) {
            pid = defaultZrHungImpl.getLockOwnerPid(this.mLock);
        }
        if (DefaultAppEyeBinderBlock.isNativeProcess(pid) != 0 || !doRecoveryForApplication(pid)) {
            return false;
        }
        return true;
    }

    public void ipcMonitorRecoveryBegin() {
        ERecoveryEvent beginEvent = new ERecoveryEvent();
        beginEvent.setERecoveryID((long) IPCOBJECT_KILL_ERECOVERYID);
        beginEvent.setFaultID(901004000);
        beginEvent.setPid((long) this.mKillTarGetPid);
        beginEvent.setState(0);
        ERecovery.eRecoveryReport(beginEvent);
    }

    public void ipcMonitorRecoveryEnd(boolean success) {
        ERecoveryEvent endEvent = new ERecoveryEvent();
        endEvent.setERecoveryID((long) IPCOBJECT_KILL_ERECOVERYID);
        endEvent.setFaultID(901004000);
        endEvent.setPid((long) this.mKillTarGetPid);
        endEvent.setState(1);
        endEvent.setResult(0);
        this.mKillTarGetPid = -1;
        ERecovery.eRecoveryReport(endEvent);
    }

    private boolean doRecoveryForNativeDaemon(int pid) {
        if (!Utils.DEBUG) {
            return false;
        }
        Log.d(TAG, " doRecoveryForNativeDaemon:" + pid);
        return false;
    }

    private boolean doRecoveryForApplication(int pid) {
        if (Utils.DEBUG) {
            Log.d(TAG, " doRecoveryForApplication:" + pid);
        }
        if (!ResourceUtils.killApplicationProcess(pid)) {
            return false;
        }
        checkUploadIPCFaultsLogs();
        return true;
    }

    private void checkUploadIPCFaultsLogs() {
        long currentTime = System.currentTimeMillis();
        if (Utils.IS_DEBUG_VERSION || currentTime - this.mLastRefreshTime >= 86400000) {
            uploadIPCMonitorFaults(this.mName);
            this.mLastRefreshTime = currentTime;
            this.mRecoverCount = 1;
            return;
        }
        this.mRecoverCount++;
    }

    /* access modifiers changed from: protected */
    public void uploadIPCMonitorFaults(String name) {
        IHwSysResManager mgr;
        if (name == null) {
            name = "general";
        }
        if (HwSysResManagerService.self() != null) {
            mgr = HwSysResManagerService.self().getHwSysResManagerService();
        } else {
            mgr = null;
        }
        if (mgr != null) {
            try {
                mgr.recordResourceOverloadStatus(-1, name, 33, this.mRecoverCount, -1, this.mRecoverCount, (Bundle) null);
            } catch (RemoteException e) {
                Log.e(TAG, "upload IPCTIMEOUT error failed:" + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void uploadIPCMonitorFaultToZerohung(String name, int recoverCount) {
        int pid;
        StringBuilder cmd = new StringBuilder("B,n=system_server");
        DefaultZrHungImpl defaultZrHungImpl = this.mAppEyeFwkBlock;
        if (defaultZrHungImpl != null && (pid = defaultZrHungImpl.getLockOwnerPid(this.mLock)) > 0) {
            cmd.append(",p=");
            cmd.append(pid);
        }
        if (!ZRHung.sendHungEvent((short) ZRHUNG_WP_IPC_OBJECT, cmd.toString(), "MonitorName:" + name + " RecoveryCount:" + recoverCount)) {
            Log.e(TAG, " ZRHung.sendHungEvent failed!");
        }
    }

    /* access modifiers changed from: protected */
    public void uploadIPCMonitorFaultToIMonitor(String pkgName, int overloadCount, int timeoutDuration) {
        if (Utils.DEBUG) {
            Log.d(TAG, "uploadIPCMonitorFaultToIMonitor" + this.mLock);
        }
        ResourceUtils.uploadBigDataLogToIMonitor(33, pkgName, overloadCount, timeoutDuration);
    }

    /* access modifiers changed from: protected */
    public void uploadIPCMonitorFaultToBigDatainfo(String pkgName, int overloadCount, int recoverCount) {
        ResourceRecordStore resourceRecordStore = ResourceRecordStore.getInstance();
        if (resourceRecordStore != null) {
            if (Utils.DEBUG) {
                Log.d(TAG, "uploadIPCMonitorFaultToBigDatainfo" + this.mLock);
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = pkgName;
            args.argi1 = overloadCount;
            args.argi2 = -1;
            args.argi3 = recoverCount;
            resourceRecordStore.createAndCheckUploadBigDataInfos(-1, 33, args, null);
        }
    }
}
