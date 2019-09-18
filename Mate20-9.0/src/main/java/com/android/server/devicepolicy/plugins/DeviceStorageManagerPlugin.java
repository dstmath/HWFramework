package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.StorageUtils;
import java.util.ArrayList;

public class DeviceStorageManagerPlugin extends DevicePolicyPlugin {
    private static final String DISABLE_SDWRITING = "disable-sdwriting";
    private static final int MSG_DO_DISABLE = 0;
    private static final int MSG_DO_ENABLE = 1;
    private static final String PERMISSION_MDM_SDCARD_WRITING = "com.huawei.permission.sec.MDM_SDCARD";
    private static final String SD_CRYPT_STATE_DECRYPTING = "decrypting";
    private static final String SD_CRYPT_STATE_ENCRYPTING = "encrypting";
    public static final String TAG = "DeviceStorageManagerPlugin";
    /* access modifiers changed from: private */
    public static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static boolean mUnfinishedBusiness = false;
    private Context mContext;
    private HwPluginHandler mHandler;
    private HandlerThread mThread = new HandlerThread(TAG);

    private final class HwPluginHandler extends Handler {
        public HwPluginHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (DeviceStorageManagerPlugin.this.shouldDoDisableWriting()) {
                        DeviceStorageManagerPlugin.this.doMountandUnMountSdcard();
                        break;
                    }
                    break;
                case 1:
                    if (DeviceStorageManagerPlugin.this.shouldDoEnableWriting()) {
                        DeviceStorageManagerPlugin.this.doMountandUnMountSdcard();
                        break;
                    }
                    break;
            }
            synchronized (DeviceStorageManagerPlugin.mLock) {
                boolean unused = DeviceStorageManagerPlugin.mUnfinishedBusiness = false;
            }
        }
    }

    public DeviceStorageManagerPlugin(Context context) {
        super(context);
        this.mThread.start();
        this.mHandler = new HwPluginHandler(this.mThread.getLooper());
        this.mContext = context;
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct("disable-sdwriting", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(PERMISSION_MDM_SDCARD_WRITING, "does not have com.huawei.permission.sec.MDM_SDCARD permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.i(TAG, "onSetPolicy + policyName :" + policyName + ", changed :" + changed);
        if (policyName != null && policyData != null) {
            return doExecutePolicy(policyName, policyData, changed);
        }
        HwLog.i(TAG, "policyData is null");
        return false;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.i(TAG, "onRemovePolicy+ policyName :" + policyName + ", changed :" + changed);
        if (policyName != null && policyData != null) {
            return doRemovePolicy(policyName, changed);
        }
        HwLog.i(TAG, "policyData is null");
        return false;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        if (removedPolicies == null) {
            HwLog.i(TAG, "removedPolicies is null");
            return;
        }
        int size = removedPolicies.size();
        for (int i = 0; i < size; i++) {
            PolicyStruct.PolicyItem policyItem = removedPolicies.get(i);
            doRemovePolicy(policyItem.getPolicyName(), policyItem.isGlobalPolicyChanged());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003a, code lost:
        if (r7.getBoolean("value") == false) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003c, code lost:
        r5.mHandler.sendEmptyMessage(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0042, code lost:
        r5.mHandler.sendEmptyMessage(1);
     */
    private boolean doExecutePolicy(String policyName, Bundle policyData, boolean changed) {
        if (!changed) {
            return true;
        }
        char c = 65535;
        if (policyName.hashCode() == -1002053434 && policyName.equals("disable-sdwriting")) {
            c = 0;
        }
        if (c == 0) {
            synchronized (mLock) {
                if (!mUnfinishedBusiness && !isExternalSdcardDecryptingOrEncrypting()) {
                    if (!isExternalSdcardNotStable()) {
                        mUnfinishedBusiness = true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0033, code lost:
        r5.mHandler.sendEmptyMessage(1);
     */
    private boolean doRemovePolicy(String policyName, boolean changed) {
        if (!changed) {
            return true;
        }
        char c = 65535;
        if (policyName.hashCode() == -1002053434 && policyName.equals("disable-sdwriting")) {
            c = 0;
        }
        if (c == 0) {
            synchronized (mLock) {
                if (!mUnfinishedBusiness && !isExternalSdcardDecryptingOrEncrypting()) {
                    if (!isExternalSdcardNotStable()) {
                        mUnfinishedBusiness = true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean shouldDoEnableWriting() {
        if (StorageUtils.isExternalSdcardMountedRO(this.mContext)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean shouldDoDisableWriting() {
        if (StorageUtils.isExternalSdcardMountedRW(this.mContext)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void doMountandUnMountSdcard() {
        StorageUtils.doUnMount(this.mContext);
        StorageUtils.doMount(this.mContext);
    }

    private boolean isExternalSdcardNotStable() {
        long token = Binder.clearCallingIdentity();
        try {
            return StorageUtils.isExternalSdcardNotStable(this.mContext);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isExternalSdcardDecryptingOrEncrypting() {
        long token = Binder.clearCallingIdentity();
        try {
            String state = SystemProperties.get("vold.cryptsd.state", "none");
            return state.equals(SD_CRYPT_STATE_DECRYPTING) || state.equals(SD_CRYPT_STATE_ENCRYPTING);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
