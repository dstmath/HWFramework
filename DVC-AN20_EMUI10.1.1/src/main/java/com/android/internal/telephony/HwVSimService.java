package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.telephony.ServiceState;
import com.android.internal.telephony.IHwVSim;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.internal.telephony.PhoneExt;

public class HwVSimService extends IHwVSim.Stub {
    private static final String LOG_TAG = "HwVSimService";
    private static final int VSIM_ENABLE_RESULT_FAIL = 3;
    private static final Object mLock = new Object();
    private static HwVSimService sInstance = null;
    private static final boolean sIsPlatformSupportVSim = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private Context mContext;
    private HwVSimController mVSimController = null;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.HwVSimService */
    /* JADX WARN: Multi-variable type inference failed */
    private HwVSimService(Context context) {
        this.mContext = context;
        if (ServiceManagerEx.getService("ihwvsim") == null) {
            ServiceManagerEx.addService("ihwvsim", this);
        }
        if (sIsPlatformSupportVSim && HwVSimController.isInstantiated()) {
            RlogEx.d(LOG_TAG, "get vsim controller");
            this.mVSimController = HwVSimController.getInstance();
            this.mVSimController.broadcastVSimServiceReady();
        }
    }

    public static HwVSimService getDefault(Context context) {
        HwVSimService hwVSimService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new HwVSimService(context);
            }
            hwVSimService = sInstance;
        }
        return hwVSimService;
    }

    public boolean hasVSimIccCard() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return (getVSimPhone() != null && getVSimPhone().hasIccCard()) || (this.mVSimController != null && this.mVSimController.getVSimOnSuccess());
    }

    private PhoneExt getVSimPhone() {
        return HwVSimPhoneFactory.getVSimPhone();
    }

    public int getVSimSubId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getVSimSubId();
    }

    public boolean isVSimEnabled() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isVSimEnabled();
    }

    public boolean isVSimInProcess() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isVSimInProcess();
    }

    public boolean isVSimOn() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isVSimOn();
    }

    public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String challenge) {
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "enableVSim , checkCallingPermission is fasle");
            return 3;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return 3;
        }
        return this.mVSimController.enableVSim(operation, imsi, cardType, apnType, acqorder, null, -1, challenge);
    }

    public int enableVSimV2(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) {
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "enableVSimV2 , checkCallingPermission is fasle");
            return 3;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return 3;
        }
        return this.mVSimController.enableVSim(operation, imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge);
    }

    public int enableVSimV3(int operation, Bundle bundle) {
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "enableVSimV3 , checkCallingPermission is fasle");
            return 3;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return 3;
        }
        return this.mVSimController.enableVSim(operation, bundle);
    }

    public boolean isSupportVSimByOperation(int operation) {
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "isSupportVSimByOperation , checkCallingPermission is fasle");
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isSupportVSimByOperation(operation);
    }

    public boolean disableVSim() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.disableVSim();
    }

    public boolean hasHardIccCardForVSim(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.hasHardIccCardForVSim(subId);
    }

    public int getSimMode(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getSimMode(subId);
    }

    public void recoverSimMode() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (sIsPlatformSupportVSim && this.mVSimController != null) {
            this.mVSimController.recoverSimMode();
        }
    }

    public String getRegPlmn(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getRegPlmn(subId);
    }

    public String getTrafficData() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getTrafficData();
    }

    public boolean clearTrafficData() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.clearTrafficData();
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.dsFlowCfg(repFlag, threshold, totalThreshold, oper);
    }

    public int getSimStateViaSysinfoEx(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getSimStateViaSysinfoEx(subId);
    }

    public int getCpserr(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return 0;
        }
        return this.mVSimController.getLastRilFailCause();
    }

    public int scanVsimAvailableNetworks(int subId, int type) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.networksScan(subId, type);
    }

    public boolean setUserReservedSubId(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.setUserReservedSubId(subId);
    }

    public int getUserReservedSubId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getUserReservedSubId();
    }

    public String getDevSubMode(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getDevSubMode(subId);
    }

    public String getPreferredNetworkTypeForVSim(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getPreferredNetworkTypeForVSim(subId);
    }

    public int getVSimCurCardType() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getVSimCurCardType();
    }

    public int getVSimNetworkType() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        int result = -1;
        if (sIsPlatformSupportVSim) {
            PhoneExt vsimPhone = getVSimPhone();
            if (vsimPhone == null) {
                return -1;
            }
            ServiceState serviceState = vsimPhone.getServiceState();
            if (serviceState == null) {
                return -1;
            }
            int nsaState = ServiceStateEx.getNsaState(serviceState);
            if (nsaState < 2 || nsaState > 5) {
                result = ServiceStateEx.getDataNetworkType(serviceState);
            } else {
                result = ServiceStateEx.getConfigRadioTechnology(serviceState);
            }
        }
        return result;
    }

    public String getVSimSubscriberId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (sIsPlatformSupportVSim) {
            return getVSimPhone().getSubscriberId();
        }
        return null;
    }

    public boolean setVSimULOnlyMode(boolean isULOnly) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.setVSimULOnlyMode(isULOnly);
    }

    public boolean getVSimULOnlyMode() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.getVSimULOnlyMode();
    }

    public int getVSimOccupiedSubId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getVSimOccupiedSubId();
    }

    public int getPlatformSupportVSimVer(int key) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return -1;
        }
        return this.mVSimController.getPlatformSupportVSimVer(key);
    }

    public boolean switchVSimWorkMode(int workMode) {
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "switchVSimWorkMode, checkCallingPermission is fasle");
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.switchVSimWorkMode(workMode);
    }

    public int dialupForVSim() {
        int result = -1;
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "dialupForVSim, checkCallingPermission is fasle");
            return -1;
        }
        if (sIsPlatformSupportVSim && this.mVSimController != null) {
            result = this.mVSimController.dialupForVSim();
        }
        return result;
    }
}
