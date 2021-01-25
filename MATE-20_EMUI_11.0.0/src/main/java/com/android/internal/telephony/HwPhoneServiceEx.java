package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.WorkSource;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManagerUtils;
import com.android.internal.telephony.vsim.HwPhoneServiceVsimEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.IHwCommonPhoneCallback;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class HwPhoneServiceEx {
    private static final String CALLBACK_EXCEPTION = "EXCEPTION";
    private static final String CALLBACK_RESULT = "RESULT";
    private static final int DATA_SERVICES = 0;
    private static final int EVENT_RADIO_STATE_CHANED = 1001;
    private static final String HW_CUST_SW_SIMLOCK = "hw.cust.sw.simlock";
    private static final int HW_PHONE_SERVICE_EXTEND_EVENT_BASE = 1000;
    private static final int INVALID = -1;
    private static final String INVALID_MCCMNC = "00000";
    private static final int NETWORK_MODE_NSA = 1;
    private static final int NETWORK_MODE_SA = 2;
    private static final int NETWORK_MODE_UNKNOWN = 0;
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE5 = 5;
    private static final int SET_SIM_POWER_STATE = 1000;
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String TAG = "HwPhoneServiceEx";
    private Context mContext;
    private HwPhoneServiceVsimEx mHwPhoneServiceVsimEx = null;
    private Handler mMainHandler;
    private PhoneExt[] mPhones;
    private final Set<IHwCommonPhoneCallback> mRadioPowerStateChangedCallbacks;
    private int mRadioPowerStateForPhone0;
    private int mRadioPowerStateForPhone1;

    public HwPhoneServiceEx(Context context, PhoneExt[] phones, Handler mainHandler) {
        int numPhones = 0;
        this.mRadioPowerStateForPhone0 = 0;
        this.mRadioPowerStateForPhone1 = 0;
        this.mRadioPowerStateChangedCallbacks = new CopyOnWriteArraySet();
        this.mContext = context;
        numPhones = phones != null ? phones.length : numPhones;
        this.mPhones = new PhoneExt[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mPhones[i] = phones[i];
        }
        this.mMainHandler = mainHandler;
        initAllComponents();
    }

    private void initAllComponents() {
        initVsimComponent();
        initListeners();
    }

    private void initVsimComponent() {
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
            this.mHwPhoneServiceVsimEx = new HwPhoneServiceVsimEx(this.mContext, this.mPhones);
        }
    }

    private void initListeners() {
        PhoneExt[] phoneExtArr = this.mPhones;
        if (phoneExtArr.length > 0) {
            phoneExtArr[0].getCi().registerForRadioStateChanged(this.mMainHandler, (int) EVENT_RADIO_STATE_CHANED, (Object) null);
        }
        PhoneExt[] phoneExtArr2 = this.mPhones;
        if (phoneExtArr2.length > 1) {
            phoneExtArr2[1].getCi().registerForRadioStateChanged(this.mMainHandler, (int) EVENT_RADIO_STATE_CHANED, (Object) null);
        }
    }

    public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) {
        return primaryLevel;
    }

    public int getRrcConnectionState(int slotId) {
        enforceReadPermission();
        HwServiceStateTrackerEx serviceStateTrackerEx = HwServiceStateTrackerEx.getInstance(slotId);
        if (serviceStateTrackerEx != null) {
            return serviceStateTrackerEx.getRrcConnectionState();
        }
        return -1;
    }

    public int getPlatformSupportVsimVer(int what) {
        HwPhoneServiceVsimEx hwPhoneServiceVsimEx = this.mHwPhoneServiceVsimEx;
        if (hwPhoneServiceVsimEx != null) {
            return hwPhoneServiceVsimEx.getPlatformSupportVsimVer(what);
        }
        return -1;
    }

    public String getRegPlmn(int slotId) {
        HwPhoneServiceVsimEx hwPhoneServiceVsimEx = this.mHwPhoneServiceVsimEx;
        if (hwPhoneServiceVsimEx != null) {
            return hwPhoneServiceVsimEx.getRegPlmn(slotId);
        }
        return BuildConfig.FLAVOR;
    }

    public boolean setVsimUserReservedSubId(int slotId) {
        HwPhoneServiceVsimEx hwPhoneServiceVsimEx = this.mHwPhoneServiceVsimEx;
        if (hwPhoneServiceVsimEx != null) {
            return hwPhoneServiceVsimEx.setVsimUserReservedSubId(slotId);
        }
        return false;
    }

    public int getVsimUserReservedSubId() {
        HwPhoneServiceVsimEx hwPhoneServiceVsimEx = this.mHwPhoneServiceVsimEx;
        if (hwPhoneServiceVsimEx != null) {
            return hwPhoneServiceVsimEx.getVsimUserReservedSubId();
        }
        return -1;
    }

    public void blockingGetVsimService(IGetVsimServiceCallback callback) throws RemoteException {
        HwPhoneServiceVsimEx hwPhoneServiceVsimEx = this.mHwPhoneServiceVsimEx;
        if (hwPhoneServiceVsimEx != null) {
            hwPhoneServiceVsimEx.blockingGetVsimService(callback);
        } else if (callback != null) {
            callback.onComplete((IHwVSim) null);
        }
    }

    public boolean isVsimEnabledByDatabase() {
        HwPhoneServiceVsimEx hwPhoneServiceVsimEx = this.mHwPhoneServiceVsimEx;
        if (hwPhoneServiceVsimEx != null) {
            return hwPhoneServiceVsimEx.isVsimEnabledByDatabase();
        }
        return false;
    }

    private void enforceReadPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
    }

    public void setSimPowerStateForSlot(int slotIndex, int state, Message msg) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(slotIndex)) {
            RlogEx.e(TAG, "not ValidSlotId: slotIndex = " + slotIndex);
        } else if (this.mPhones[slotIndex] == null) {
            RlogEx.e(TAG, "setSimPowerStateForSlot: phone is null for slotId " + slotIndex);
        } else {
            WorkSource workSource = new WorkSource();
            this.mPhones[slotIndex].getCi().setSimCardPower(state, this.mMainHandler.obtainMessage(1000, msg), workSource);
        }
    }

    private boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }

    private String getIccidBySlot(int slotId) {
        UiccControllerExt uiccController = UiccControllerExt.getInstance();
        if (uiccController.getUiccCard(slotId) != null) {
            return uiccController.getUiccCard(slotId).getIccId();
        }
        return null;
    }

    public boolean isSmartCard(int slotId) {
        String inn = BuildConfig.FLAVOR;
        String iccId = getIccidBySlot(slotId);
        if (!TextUtils.isEmpty(iccId) && iccId.length() >= 7) {
            inn = iccId.substring(0, 7);
        }
        String mccMnc = TelephonyManagerEx.getSimOperatorNumericForPhone(slotId);
        if (!TextUtils.isEmpty(inn) && HwIccIdUtil.isSmart(inn)) {
            return true;
        }
        if (TextUtils.isEmpty(mccMnc) || !HwIccIdUtil.isSmartByMccMnc(mccMnc)) {
            return false;
        }
        return true;
    }

    public boolean isCustomSmart() {
        return HwFullNetworkManagerUtils.getInstance().isIsSmart4gDsdxEnable();
    }

    private boolean isCustomVersion(String custom) {
        if ("ais".equalsIgnoreCase(custom) || "smart".equalsIgnoreCase(custom) || "mtn".equalsIgnoreCase(custom)) {
            return true;
        }
        return false;
    }

    private boolean isCustomSimlockDisable() {
        return INVALID_MCCMNC.equals(SystemPropertiesEx.get(HW_CUST_SW_SIMLOCK, BuildConfig.FLAVOR));
    }

    private boolean isCustomCard(int slotId, String custom) {
        String inn = BuildConfig.FLAVOR;
        String iccId = getIccidBySlot(slotId);
        if (!TextUtils.isEmpty(iccId) && iccId.length() >= 7) {
            inn = iccId.substring(0, 7);
        }
        String mccMnc = TelephonyManagerEx.getSimOperatorNumericForPhone(slotId);
        if (!TextUtils.isEmpty(inn) && HwIccIdUtil.isCustomByIccid(inn, custom)) {
            return true;
        }
        if (TextUtils.isEmpty(mccMnc) || !HwIccIdUtil.isCustomByMccMnc(mccMnc, custom)) {
            return false;
        }
        return true;
    }

    public boolean isBlockNonCustomSlot(int slotId, int customBlockType) {
        if (!HuaweiTelephonyConfigs.isMTKPlatform()) {
            return false;
        }
        String custom = SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", BuildConfig.FLAVOR);
        if (!isCustomVersion(custom) || isCustomSimlockDisable() || isCustomCard(slotId, custom)) {
            return false;
        }
        char c = 65535;
        if (custom.hashCode() == 108455 && custom.equals("mtn")) {
            c = 0;
        }
        if (c == 0 && customBlockType == 0) {
            return true;
        }
        return false;
    }

    public boolean registerForRadioStateChanged(IHwCommonPhoneCallback callback) {
        if (callback == null) {
            return false;
        }
        return this.mRadioPowerStateChangedCallbacks.add(callback);
    }

    public boolean unregisterForRadioStateChanged(IHwCommonPhoneCallback callback) {
        if (callback == null) {
            return false;
        }
        return this.mRadioPowerStateChangedCallbacks.remove(callback);
    }

    private void notifyRadioPower() {
        RlogEx.i(TAG, "notifyRadioPower, phone0: " + this.mRadioPowerStateForPhone0 + ", phone1: " + this.mRadioPowerStateForPhone1 + " has " + this.mRadioPowerStateChangedCallbacks.size() + " receivers.");
        for (IHwCommonPhoneCallback callback : this.mRadioPowerStateChangedCallbacks) {
            try {
                callback.onCallback1(this.mRadioPowerStateForPhone0);
                callback.onCallback2(1, this.mRadioPowerStateForPhone1);
            } catch (RemoteException e) {
                RlogEx.e(TAG, "notifyRadioProxyDeadEvent occur an exception.");
            }
        }
    }

    public void handleMessageForServiceEx(Message msg) {
        int i = msg.what;
        if (i == 1000) {
            RlogEx.d(TAG, "EVENT_SET_SUBSCRIPTION_DONE");
            handleSetFunctionDone(msg);
        } else if (i != EVENT_RADIO_STATE_CHANED) {
            RlogEx.i(TAG, "handleMessageForServiceEx unhandled message: " + msg.what);
        } else {
            RlogEx.i(TAG, "EVENT_RADIO_STATE_CHANED");
            handleRadioStateChanged();
        }
    }

    private void handleSetFunctionDone(Message msg) {
        Message cbMsg;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (ar.getUserObj() instanceof Message) && (cbMsg = (Message) ar.getUserObj()) != null && cbMsg.replyTo != null) {
            Bundle data = new Bundle();
            if (ar.getException() != null) {
                data.putBoolean(CALLBACK_RESULT, false);
                data.putString(CALLBACK_EXCEPTION, ar.getException().toString());
            } else {
                data.putBoolean(CALLBACK_RESULT, true);
            }
            cbMsg.setData(data);
            try {
                cbMsg.replyTo.send(cbMsg);
            } catch (RemoteException e) {
                RlogEx.e(TAG, "EVENT_SET_FUNCTION_DONE RemoteException");
            }
        }
    }

    private void handleRadioStateChanged() {
        PhoneExt[] phoneExtArr = this.mPhones;
        int i = 0;
        this.mRadioPowerStateForPhone0 = phoneExtArr.length > 0 ? phoneExtArr[0].getCi().getRadioState() : 0;
        PhoneExt[] phoneExtArr2 = this.mPhones;
        if (phoneExtArr2.length > 1) {
            i = phoneExtArr2[1].getCi().getRadioState();
        }
        this.mRadioPowerStateForPhone1 = i;
        notifyRadioPower();
    }

    public int getNetworkMode(int phoneId) {
        if (!isValidSlotId(phoneId)) {
            RlogEx.e(TAG, "phoneId is invalid.");
            return 0;
        }
        ServiceStateTrackerEx sst = this.mPhones[phoneId].getServiceStateTracker();
        if (sst == null) {
            RlogEx.e(TAG, "sst is null.");
            return 0;
        }
        int nsaState = sst.getNewNsaState();
        if (nsaState >= 2 && nsaState <= 5) {
            return 1;
        }
        ServiceState serviceState = this.mPhones[phoneId].getServiceState();
        if (serviceState == null) {
            RlogEx.e(TAG, "serviceState is null.");
            return 0;
        } else if (ServiceStateEx.getRilDataRadioTechnology(serviceState) == 20) {
            return 2;
        } else {
            return 0;
        }
    }
}
