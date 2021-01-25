package com.huawei.server.security.securitydiagnose;

import android.util.HiLog;
import android.util.HiLogLabel;
import com.huawei.hwstp.HwStpHidlAdapter;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class KernelDetection {
    private static final int CMD_START = 1;
    private static final int CMD_STOP = 0;
    private static final int DOMAIN = 218115849;
    private static final int ERR_ARG = -1008;
    private static final int ERR_CNT = -1004;
    private static final int ERR_NULL = -1001;
    private static final int ERR_REG = -1002;
    private static final int ERR_START = -1005;
    private static final int ERR_STOP = -1006;
    private static final int ERR_UNREG = -1003;
    private static final int ERR_UPDATE = -1007;
    private static final HiLogLabel HILOG_LABEL = new HiLogLabel(3, (int) DOMAIN, TAG);
    private static final int RET_SUCC = 0;
    private static final String STP_HIDL_SERVICE_NAME = "hwstp";
    private static final String TAG = "Module Kernel Detection";
    private HwStpHidlAdapter mHwStpHidlAdapter;
    private final Object mRegisterMutex;
    private final Object mStpMutex;
    private int mUidCount;

    private KernelDetection() {
        this.mRegisterMutex = new Object();
        this.mStpMutex = new Object();
        this.mHwStpHidlAdapter = null;
        this.mUidCount = 0;
    }

    /* access modifiers changed from: private */
    public static class SingleInstanceHolder {
        private static final KernelDetection INSTANCE = new KernelDetection();

        private SingleInstanceHolder() {
        }
    }

    static KernelDetection getInstance() {
        return SingleInstanceHolder.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public int startInspection(int uid) {
        int ret;
        if (uid < 0) {
            return ERR_ARG;
        }
        initHwStp();
        if (this.mHwStpHidlAdapter == null) {
            HiLog.error(HILOG_LABEL, "Start: mHwStpHidlAdapter is null", new Object[0]);
            return ERR_NULL;
        }
        synchronized (this.mRegisterMutex) {
            if (this.mUidCount >= 1 || (ret = registerKernelDetectionCallback()) == 0) {
                int ret2 = this.mHwStpHidlAdapter.stpTriggerKernelDetection(uid, 1);
                if (ret2 == ERR_REG) {
                    HiLog.error(HILOG_LABEL, "Start: RemoteException", new Object[0]);
                    return ERR_START;
                } else if (ret2 != 0) {
                    HiLog.error(HILOG_LABEL, "Invoke hidl start failed ret = %{public}d", new Object[]{Integer.valueOf(ret2)});
                    return ERR_START;
                } else {
                    this.mUidCount++;
                    return 0;
                }
            } else {
                HiLog.error(HILOG_LABEL, "Register callback failed ret = %{public}d", new Object[]{Integer.valueOf(ret)});
                return ERR_REG;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int stopInspection(int uid) {
        if (uid < 0) {
            return ERR_ARG;
        }
        initHwStp();
        HwStpHidlAdapter hwStpHidlAdapter = this.mHwStpHidlAdapter;
        if (hwStpHidlAdapter == null) {
            return ERR_NULL;
        }
        int ret = hwStpHidlAdapter.stpTriggerKernelDetection(uid, 0);
        if (ret == ERR_REG) {
            HiLog.error(HILOG_LABEL, "Stop: RemoteException ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_STOP;
        } else if (ret != 0) {
            return ERR_STOP;
        } else {
            synchronized (this.mRegisterMutex) {
                if (this.mUidCount > 1) {
                    this.mUidCount--;
                    return 0;
                } else if (this.mUidCount != 1) {
                    return ERR_CNT;
                } else {
                    int ret2 = unregisterKernelDetectionCallback();
                    if (ret2 == 0) {
                        this.mUidCount--;
                        return 0;
                    }
                    HiLog.error(HILOG_LABEL, "Unregister callback failed ret = %{public}d", new Object[]{Integer.valueOf(ret2)});
                    return ERR_UNREG;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int updateKernelDetectionConfig(int[] conf) {
        if (conf == null || conf.length == 0) {
            return ERR_ARG;
        }
        initHwStp();
        if (this.mHwStpHidlAdapter == null) {
            return ERR_NULL;
        }
        ArrayList<Integer> confList = new ArrayList<>(conf.length);
        for (int value : conf) {
            confList.add(Integer.valueOf(value));
        }
        int ret = this.mHwStpHidlAdapter.stpUpdateKernelDetectionConfig(confList);
        if (ret == ERR_REG) {
            HiLog.error(HILOG_LABEL, "Invoke hidl update failed ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_UPDATE;
        } else if (ret != 0) {
            HiLog.error(HILOG_LABEL, "Update: ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_UPDATE;
        } else {
            HiLog.info(HILOG_LABEL, "Update configuration for kernel detection module succeed", new Object[0]);
            return 0;
        }
    }

    private void initHwStp() {
        synchronized (this.mStpMutex) {
            if (this.mHwStpHidlAdapter == null) {
                this.mHwStpHidlAdapter = new HwStpHidlAdapter();
                if (!this.mHwStpHidlAdapter.isServiceConnected()) {
                    HiLog.error(HILOG_LABEL, "Exception when initial mHwStpHidlAdapter", new Object[0]);
                }
            }
        }
    }

    private int registerKernelDetectionCallback() {
        KernelDetectionCallback callback = new KernelDetectionCallback();
        initHwStp();
        int ret = ERR_REG;
        HwStpHidlAdapter hwStpHidlAdapter = this.mHwStpHidlAdapter;
        if (hwStpHidlAdapter != null && (ret = hwStpHidlAdapter.stpRegisterKernelDetectionCallback(callback)) == ERR_REG) {
            HiLog.error(HILOG_LABEL, "Invoke hidl register failed ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_REG;
        } else if (ret == 0) {
            return 0;
        } else {
            HiLog.error(HILOG_LABEL, "Register: ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_REG;
        }
    }

    private int unregisterKernelDetectionCallback() {
        initHwStp();
        int ret = ERR_UNREG;
        HwStpHidlAdapter hwStpHidlAdapter = this.mHwStpHidlAdapter;
        if (hwStpHidlAdapter != null && (ret = hwStpHidlAdapter.stpUnregisterKernelDetectionCallback()) == ERR_REG) {
            HiLog.error(HILOG_LABEL, "Invoke hidl unregister failed ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_UNREG;
        } else if (ret == 0) {
            return 0;
        } else {
            HiLog.error(HILOG_LABEL, "Unregister: ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return ERR_UNREG;
        }
    }
}
