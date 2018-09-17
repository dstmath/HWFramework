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
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
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
    private static final Object mLock = new Object();
    private static boolean mUnfinishedBusiness = false;
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
                DeviceStorageManagerPlugin.mUnfinishedBusiness = false;
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
        struct.addStruct("disable-sdwriting", PolicyType.STATE, new String[]{"value"});
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

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        if (removedPolicies == null) {
            HwLog.i(TAG, "removedPolicies is null");
            return;
        }
        int size = removedPolicies.size();
        for (int i = 0; i < size; i++) {
            PolicyItem policyItem = (PolicyItem) removedPolicies.get(i);
            doRemovePolicy(policyItem.getPolicyName(), policyItem.isGlobalPolicyChanged());
        }
    }

    /* JADX WARNING: Missing block: B:27:0x0032, code:
            if (r7.getBoolean("value") == false) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:28:0x0034, code:
            r5.mHandler.sendEmptyMessage(0);
     */
    /* JADX WARNING: Missing block: B:33:0x003d, code:
            r5.mHandler.sendEmptyMessage(1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doExecutePolicy(String policyName, Bundle policyData, boolean changed) {
        if (changed && policyName.equals("disable-sdwriting")) {
            synchronized (mLock) {
                if (mUnfinishedBusiness) {
                    return false;
                } else if (isExternalSdcardDecryptingOrEncrypting()) {
                    return false;
                } else if (isExternalSdcardNotStable()) {
                    return false;
                } else {
                    mUnfinishedBusiness = true;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:26:0x002b, code:
            r4.mHandler.sendEmptyMessage(1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doRemovePolicy(String policyName, boolean changed) {
        if (changed && policyName.equals("disable-sdwriting")) {
            synchronized (mLock) {
                if (mUnfinishedBusiness) {
                    return false;
                } else if (isExternalSdcardDecryptingOrEncrypting()) {
                    return false;
                } else if (isExternalSdcardNotStable()) {
                    return false;
                } else {
                    mUnfinishedBusiness = true;
                }
            }
        }
        return true;
    }

    private boolean shouldDoEnableWriting() {
        if (StorageUtils.isSDCardWritingDisabled()) {
            return true;
        }
        return false;
    }

    private boolean shouldDoDisableWriting() {
        if (!StorageUtils.isExternalSdcardMounted(this.mContext) || (StorageUtils.isSDCardWritingDisabled() ^ 1) == 0) {
            return false;
        }
        return true;
    }

    private void doMountandUnMountSdcard() {
        StorageUtils.doUnMount(this.mContext);
        StorageUtils.doMount(this.mContext);
    }

    private boolean isExternalSdcardNotStable() {
        long token = Binder.clearCallingIdentity();
        try {
            boolean isExternalSdcardNotStable = StorageUtils.isExternalSdcardNotStable(this.mContext);
            return isExternalSdcardNotStable;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isExternalSdcardDecryptingOrEncrypting() {
        long token = Binder.clearCallingIdentity();
        try {
            String state = SystemProperties.get("vold.cryptsd.state", "none");
            boolean equals = !state.equals(SD_CRYPT_STATE_DECRYPTING) ? state.equals(SD_CRYPT_STATE_ENCRYPTING) : true;
            Binder.restoreCallingIdentity(token);
            return equals;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }
}
