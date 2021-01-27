package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.telephony.ServiceState;
import com.android.internal.telephony.IHwVSim;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.huawei.android.hwdfu.ServiceManagerUtil;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;

public class HwVSimService extends IHwVSim.Stub {
    private static final String LOG_TAG = "HwVSimService";
    private static final int VSIM_ENABLE_RESULT_FAIL = 3;
    private static final Object mLock = new Object();
    private static HwVSimService sInstance = null;
    private static final boolean sIsPlatformSupportVSim = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private Context mContext;
    private HwVSimBaseController mVsimBaseController = null;
    private HwVSimController mVsimController = null;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.HwVSimService */
    /* JADX WARN: Multi-variable type inference failed */
    private HwVSimService(Context context) {
        this.mContext = context;
        if (ServiceManagerEx.getService("ihwvsim") == null) {
            ServiceManagerEx.addService("ihwvsim", this);
        }
        if (!sIsPlatformSupportVSim) {
            logi("not support vsim, return");
        } else if (HwVSimBaseController.isInstantiated()) {
            logi("get vsim controller");
            if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                this.mVsimController = HwVSimController.getInstance();
                this.mVsimBaseController = this.mVsimController;
            }
            if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                this.mVsimBaseController = HwVSimMtkController.getInstance();
            }
            HwVSimBaseController hwVSimBaseController = this.mVsimBaseController;
            if (hwVSimBaseController != null) {
                hwVSimBaseController.broadcastVsimServiceReady();
            }
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

    public static HwVSimService getInstance() {
        HwVSimService hwVSimService;
        synchronized (mLock) {
            hwVSimService = sInstance;
        }
        return hwVSimService;
    }

    public static void dispose() {
        RlogEx.i(LOG_TAG, "dispose");
        synchronized (mLock) {
            if (sInstance != null) {
                ServiceManagerUtil.removeService("ihwvsim", false);
                sInstance = null;
                RlogEx.i(LOG_TAG, "dispose end");
            }
        }
    }

    public boolean hasVSimIccCard() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.hasVSimIccCard();
    }

    private PhoneExt getVSimPhone() {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            return HwVSimPhoneFactory.getVSimPhone();
        }
        int vsimSlotId = this.mVsimBaseController.getVSimOccupiedSlotId();
        if (SubscriptionManagerEx.isValidSlotIndex(vsimSlotId)) {
            return PhoneFactoryExt.getPhone(vsimSlotId);
        }
        return null;
    }

    public int getVSimSubId() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return -1;
        }
        return hwVSimBaseController.getVsimSlotId();
    }

    public boolean isVSimEnabled() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.isVSimEnabled();
    }

    public boolean isVSimInProcess() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.isVSimInProcess();
    }

    public boolean isVSimOn() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.isVSimOn();
    }

    public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String challenge) {
        HwVSimController hwVSimController;
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "enableVSim , checkCallingPermission is fasle");
            return 3;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimController = this.mVsimController) == null) {
            return 3;
        }
        return hwVSimController.enableVSim(operation, imsi, cardType, apnType, acqorder, null, -1, challenge);
    }

    public int enableVSimV2(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) {
        HwVSimController hwVSimController;
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "enableVSimV2 , checkCallingPermission is fasle");
            return 3;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimController = this.mVsimController) == null) {
            return 3;
        }
        return hwVSimController.enableVSim(operation, imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge);
    }

    public int enableVSimV3(int operation, Bundle bundle) {
        HwVSimBaseController hwVSimBaseController;
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "enableVSimV3 , checkCallingPermission is false");
            return 3;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return 3;
        }
        return hwVSimBaseController.enableVSim(operation, bundle);
    }

    public boolean disableVSim() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.disableVSim();
    }

    public String getTrafficData() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return null;
        }
        return hwVSimBaseController.getTrafficData();
    }

    public boolean clearTrafficData() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.clearTrafficData();
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.dsFlowCfg(repFlag, threshold, totalThreshold, oper);
    }

    public int getSimStateViaSysinfoEx(int subId) {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return -1;
        }
        return hwVSimBaseController.getSimStateViaSysinfoEx(subId);
    }

    public int scanVsimAvailableNetworks(int subId, int type) {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return -1;
        }
        return hwVSimBaseController.scanVsimAvailableNetworks(subId, type);
    }

    public String getDevSubMode(int subId) {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return null;
        }
        return hwVSimBaseController.getDevSubMode(subId);
    }

    public String getPreferredNetworkTypeForVSim(int subId) {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return null;
        }
        return hwVSimBaseController.getPreferredNetworkTypeForVsim(subId);
    }

    public int getVSimCurCardType() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return -1;
        }
        return hwVSimBaseController.getVSimCurCardType();
    }

    public int getVSimNetworkType() {
        PhoneExt vsimPhone;
        ServiceState serviceState;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (vsimPhone = getVSimPhone()) == null || (serviceState = vsimPhone.getServiceState()) == null) {
            return -1;
        }
        int nsaState = ServiceStateEx.getNsaState(serviceState);
        if (nsaState < 2 || nsaState > 5) {
            return ServiceStateEx.getDataNetworkType(serviceState);
        }
        return ServiceStateEx.getConfigRadioTechnology(serviceState);
    }

    public String getVSimSubscriberId() {
        PhoneExt vsimPhone;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (vsimPhone = getVSimPhone()) == null) {
            return null;
        }
        return vsimPhone.getSubscriberId();
    }

    public int getVSimOccupiedSubId() {
        HwVSimBaseController hwVSimBaseController;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return -1;
        }
        return hwVSimBaseController.getVSimOccupiedSlotId();
    }

    public boolean switchVSimWorkMode(int workMode) {
        HwVSimBaseController hwVSimBaseController;
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "switchVSimWorkMode, checkCallingPermission is fasle");
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return false;
        }
        return hwVSimBaseController.switchVsimWorkMode(workMode);
    }

    public int dialupForVSim() {
        HwVSimBaseController hwVSimBaseController;
        if (-1 == this.mContext.checkCallingPermission(HwVSimConstants.VSIM_BUSSINESS_PERMISSION)) {
            RlogEx.e(LOG_TAG, "dialupForVSim, checkCallingPermission is fasle");
            return -1;
        } else if (!sIsPlatformSupportVSim || (hwVSimBaseController = this.mVsimBaseController) == null) {
            return -1;
        } else {
            return hwVSimBaseController.dialupForVSim();
        }
    }

    private void logi(String s) {
        HwVSimLog.VSimLogI(LOG_TAG, s);
    }
}
