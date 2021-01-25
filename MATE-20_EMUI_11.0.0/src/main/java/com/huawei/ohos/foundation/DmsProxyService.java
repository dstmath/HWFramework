package com.huawei.ohos.foundation;

import android.content.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.sysability.samgr.SysAbilityManager;

public class DmsProxyService {
    private static final HiLogLabel DMS_LABEL = new HiLogLabel(3, 218109952, "DmsProxyService");
    private Context mContext = null;
    private DmsProxyManagerWrap mDmsProxy;

    static {
        try {
            HiLog.info(DMS_LABEL, "Load libfoundation_jni.z.so", new Object[0]);
            System.loadLibrary("foundation_jni.z");
            HiLog.info(DMS_LABEL, "Load libces_jni.z.so", new Object[0]);
            System.loadLibrary("ces_jni.z");
            HiLog.info(DMS_LABEL, "Load libipc_core.z.so", new Object[0]);
            System.loadLibrary("ipc_core.z");
            HiLog.info(DMS_LABEL, "Load libbundlemgr_jni.z.so", new Object[0]);
            System.loadLibrary("bundlemgr_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(DMS_LABEL, "ERROR: Could not load so ", new Object[0]);
        }
    }

    public DmsProxyService(Context context) {
        this.mContext = context;
        this.mDmsProxy = new DmsProxyManagerWrap(context);
    }

    public void start() {
        if (this.mContext == null) {
            HiLog.info(DMS_LABEL, "context is null", new Object[0]);
            return;
        }
        HiLog.info(DMS_LABEL, "add dmsproxy result is %{public}d", new Object[]{Integer.valueOf(SysAbilityManager.addSysAbility(1402, this.mDmsProxy.asObject()))});
    }
}
