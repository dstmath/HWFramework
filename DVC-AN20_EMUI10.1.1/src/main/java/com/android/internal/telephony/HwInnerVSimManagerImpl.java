package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SubscriptionManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimUtilsImpl;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.data.ApnSettingEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneNotifierEx;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HwInnerVSimManagerImpl extends DefaultHwInnerVSimManager {
    private static boolean HWFLOW = true;
    private static final int IS_RADIO_AVAILABLE = 7;
    private static final int IS_VSIM_CAUSE_CARD_RELOAD = 8;
    private static final int IS_VSIM_ENABLED = 9;
    private static final int IS_VSIM_IN_PROCESS = 2;
    private static final int IS_VSIM_ON = 1;
    private static final String LOG_TAG = "HwInnerVSimMngrImpl";
    private static final int NEED_BLOCK_PIN = 10;
    private static final int NEED_BLOCK_UNRESERVED_SUBID = 11;
    private static final int PHONE_COUNT = 2;
    private static HwInnerVSimManager mInstance;
    private HwVSimService mHwVSimService;
    private boolean[] mLegacyMarkTable = new boolean[2];

    public static HwInnerVSimManager getDefault() {
        if (mInstance == null) {
            mInstance = new HwInnerVSimManagerImpl();
        }
        return mInstance;
    }

    public void createHwVSimService(Context context) {
        this.mHwVSimService = HwVSimService.getDefault(context);
        if (this.mHwVSimService == null) {
            RlogEx.e(LOG_TAG, "create vsim service not success");
        }
        for (int i = 0; i < 2; i++) {
            this.mLegacyMarkTable[i] = false;
        }
    }

    public void makeVSimPhoneFactory(Context context, PhoneNotifierEx notifier, PhoneExt[] pps, CommandsInterfaceEx[] cis) {
        HwVSimPhoneFactory.make(context, notifier, pps, cis);
    }

    public void dumpVSimPhoneFactory(FileDescriptor fd, PrintWriter pw, String[] args) {
        HwVSimPhoneFactory.dump(fd, pw, args);
    }

    public ServiceStateTrackerEx makeVSimServiceStateTracker(PhoneExt phone, CommandsInterfaceEx ci) {
        return HwVSimPhoneFactory.makeVSimServiceStateTracker(phone, ci);
    }

    public PhoneExt getVSimPhone() {
        return HwVSimPhoneFactory.getVSimPhone();
    }

    public boolean isVSimPhone(PhoneExt phone) {
        return HwVSimPhoneFactory.isVSimPhone(phone);
    }

    public boolean isVSimInStatus(int type, int subId) {
        boolean result = false;
        if (type == 7) {
            result = true;
        }
        if (!HwVSimController.isInstantiated()) {
            return result;
        }
        switch (type) {
            case 1:
                result = HwVSimController.getInstance().isVSimOn();
                break;
            case 2:
                result = HwVSimController.getInstance().isVSimInProcess();
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            default:
                if (HWFLOW) {
                    RlogEx.i(LOG_TAG, "isVSimInStatus type " + type + "not support");
                    break;
                }
                break;
            case 7:
                if (HwVSimUtilsImpl.getInstance().isPlatformTwoModems()) {
                    result = HwVSimUtilsImpl.getInstance().isRadioAvailable(subId);
                    break;
                }
                break;
            case 8:
                result = HwVSimController.getInstance().isVSimCauseCardReload();
                break;
            case 9:
                result = HwVSimController.getInstance().isVSimEnabled();
                break;
            case 10:
                result = HwVSimController.getInstance().needBlockPin(subId);
                break;
            case 11:
                result = HwVSimController.getInstance().needBlockUnReservedForVsim(subId);
                break;
        }
        return result;
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        HwVSimUiccController.getInstance().registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        HwVSimUiccController.getInstance().unregisterForIccChanged(h);
    }

    public IccRecordsEx fetchVSimIccRecords(int family) {
        return HwVSimUiccController.getInstance().getIccRecords(family);
    }

    public UiccCardApplicationEx getVSimUiccCardApplication(int appFamily) {
        return HwVSimUiccController.getInstance().getUiccCardApplication(appFamily);
    }

    public void setMarkForCardReload(int subId, boolean value) {
        if (HwVSimController.getInstance().isVSimCauseCardReload()) {
            setVSimLegacyReloadMark(subId, true);
        }
        HwVSimController.getInstance().setMarkForCardReload(subId, value);
    }

    public String getPendingDeviceInfoFromSP(String prefKey) {
        if (HwVSimController.isInstantiated() && HwVSimUtilsImpl.getInstance().isPlatformTwoModems()) {
            return HwVSimController.getInstance().getPendingDeviceInfoFromSP(prefKey);
        }
        return null;
    }

    public int getTopPrioritySubscriptionId() {
        if (!HwVSimController.isInstantiated()) {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
        return HwVSimPhoneFactory.getTopPrioritySubscriptionId();
    }

    public ArrayList<ApnSetting> createVSimApnList() {
        ArrayList<ApnSetting> result = new ArrayList<>();
        result.add(makeVSimApnSetting());
        logi("createVSimApnList: X result=" + result);
        return result;
    }

    private ApnSetting makeVSimApnSetting() {
        return ApnSettingEx.makeApnSetting(0, "00000", "vsim", "apn", BuildConfig.FLAVOR, -1, (Uri) null, BuildConfig.FLAVOR, -1, BuildConfig.FLAVOR, BuildConfig.FLAVOR, -1, ApnSettingEx.getApnTypesBitmaskFromString("default,supl"), ApnSettingEx.getProtocolIntFromString("IP"), ApnSettingEx.getProtocolIntFromString("IP"), true, 0, 0, false, 0, 0, 0, 0, ApnSettingEx.getMvnoTypeIntFromString(BuildConfig.FLAVOR), BuildConfig.FLAVOR, 0, -1, -1);
    }

    private boolean isIccStateChangedByVSimReload(String value, int subId) {
        if (!"ABSENT".equals(value) && !"READY".equals(value) && !"IMSI".equals(value) && !"LOADED".equals(value)) {
            return false;
        }
        if (HwVSimController.getInstance().isVSimEnabled() || HwVSimController.getInstance().isVSimCauseCardReload()) {
            if ("LOADED".equals(value)) {
                setVSimLegacyReloadMark(subId, false);
            }
            return true;
        } else if (!isVSimLegacyReloadMark(subId)) {
            return false;
        } else {
            setVSimLegacyReloadMark(subId, false);
            return true;
        }
    }

    private boolean isVSimLegacyReloadMark(int subId) {
        if (subId < 0 || subId >= 2) {
            return false;
        }
        return this.mLegacyMarkTable[subId];
    }

    private void setVSimLegacyReloadMark(int subId, boolean value) {
        if (subId >= 0 && subId < 2) {
            logi("set sLegacyMarkTable[" + subId + "] = " + value);
            this.mLegacyMarkTable[subId] = value;
        }
    }

    public void putVSimExtraForIccStateChanged(Intent intent, int subId, String value) {
        if (intent != null && value != null && isIccStateChangedByVSimReload(value, subId)) {
            logi("vsim add extra param for ACTION_SIM_STATE_CHANGED as vsim reload");
            intent.putExtra("vsim", "VSIM_RELOAD");
        }
    }

    public void disposeSSTForVSim() {
        logi("disposeSSTForVSim");
        PhoneExt vsimPhone = getVSimPhone();
        if (vsimPhone == null) {
            logi("disposeSSTForVSim, phone is null, return.");
            return;
        }
        ServiceStateTrackerEx serviceStateTrackerEx = vsimPhone.getServiceStateTracker();
        if (serviceStateTrackerEx instanceof HwVSimServiceStateTracker) {
            logi("disposeSSTForVSim try to dispose");
            ((HwVSimServiceStateTracker) serviceStateTrackerEx).dispose();
        }
    }

    public String changeSpnForVSim(String spn) {
        String spnFromApk = HwVSimController.getInstance().getSpn();
        logi("changeSpnForVSim, spnFromApk " + spnFromApk + " instead of " + spn);
        return !TextUtils.isEmpty(spnFromApk) ? spnFromApk : spn;
    }

    public int changeRuleForVSim(int rule) {
        int ruleFromApk = HwVSimController.getInstance().getRule();
        logi("changeRuleForVSim, ruleFromApk " + ruleFromApk + " instead of " + rule);
        return ruleFromApk != -1 ? ruleFromApk : rule;
    }

    public IccCardStatusExt.CardStateEx getVSimCardState() {
        UiccCardExt uiccCard = HwVSimUiccController.getInstance().getUiccCard();
        if (uiccCard == null) {
            return IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
        }
        return uiccCard.getCardState();
    }

    private void logi(String msg) {
        if (HWFLOW) {
            RlogEx.i(LOG_TAG, msg);
        }
    }
}
