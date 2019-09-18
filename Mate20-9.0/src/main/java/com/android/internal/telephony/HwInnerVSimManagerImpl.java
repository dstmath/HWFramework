package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HwInnerVSimManagerImpl implements HwInnerVSimManager {
    private static boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(LOG_TAG, 4)));
    private static final boolean HWLOGW_E = true;
    private static final int IS_RADIO_AVAILABLE = 7;
    private static final int IS_VSIM_CAUSE_CARD_RELOAD = 8;
    private static final int IS_VSIM_ENABLED = 9;
    private static final int IS_VSIM_IN_PROCESS = 2;
    private static final int IS_VSIM_ON = 1;
    private static final String LOG_TAG = "HwInnerVSimMngrImpl";
    private static final int NEED_BLOCK_PIN = 10;
    private static final int NEED_BLOCK_UNRESERVED_SUBID = 11;
    private static final int PHONE_COUNT = 2;
    private static HwInnerVSimManager mInstance = new HwInnerVSimManagerImpl();
    private HwVSimService mHwVSimService;
    private boolean[] mLegacyMarkTable = new boolean[2];

    public static HwInnerVSimManager getDefault() {
        return mInstance;
    }

    public void createHwVSimService(Context context) {
        this.mHwVSimService = HwVSimService.getDefault(context);
        if (this.mHwVSimService == null) {
            Rlog.e(LOG_TAG, "create vsim service not success");
        }
        for (int i = 0; i < 2; i++) {
            this.mLegacyMarkTable[i] = false;
        }
    }

    public void makeVSimPhoneFactory(Context context, PhoneNotifier notifier, Phone[] pps, CommandsInterface[] cis) {
        HwVSimPhoneFactory.make(context, notifier, pps, cis);
    }

    public void dumpVSimPhoneFactory(FileDescriptor fd, PrintWriter pw, String[] args) {
        HwVSimPhoneFactory.dump(fd, pw, args);
    }

    public ServiceStateTracker makeVSimServiceStateTracker(Phone phone, CommandsInterface ci) {
        return HwVSimPhoneFactory.makeVSimServiceStateTracker(phone, ci);
    }

    public Phone getVSimPhone() {
        return HwVSimPhoneFactory.getVSimPhone();
    }

    public boolean isVSimPhone(Phone phone) {
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
            default:
                switch (type) {
                    case 7:
                        if (HwVSimUtils.isPlatformTwoModems()) {
                            result = HwVSimUtils.isRadioAvailable(subId);
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
                    default:
                        if (HWFLOW) {
                            Rlog.i(LOG_TAG, "isVSimInStatus type " + type + "not support");
                            break;
                        }
                        break;
                }
        }
        return result;
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        HwVSimUiccController.getInstance().registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        HwVSimUiccController.getInstance().unregisterForIccChanged(h);
    }

    public IccRecords fetchVSimIccRecords(int family) {
        return HwVSimUiccController.getInstance().getIccRecords(family);
    }

    public UiccCardApplication getVSimUiccCardApplication(int appFamily) {
        return HwVSimUiccController.getInstance().getUiccCardApplication(appFamily);
    }

    public void setMarkForCardReload(int subId, boolean value) {
        if (HwVSimController.getInstance().isVSimCauseCardReload()) {
            setVSimLegacyReloadMark(subId, true);
        }
        HwVSimController.getInstance().setMarkForCardReload(subId, value);
    }

    public String getPendingDeviceInfoFromSP(String prefKey) {
        if (HwVSimController.isInstantiated() && HwVSimUtils.isPlatformTwoModems()) {
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
        ApnSetting apnSetting = new ApnSetting(0, "00000", "vsim", "apn", "", "", "", "", "", "", "", -1, parseTypes("default,supl"), "IP", "IP", true, 0, 0, false, 0, 0, 0, 0, "", "");
        return apnSetting;
    }

    private static String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        return new String[]{"*"};
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
        if (isIccStateChangedByVSimReload(value, subId)) {
            logi("vsim add extra param for ACTION_SIM_STATE_CHANGED as vsim reload");
            intent.putExtra("vsim", "VSIM_RELOAD");
        }
    }

    private void logi(String msg) {
        if (HWFLOW) {
            Rlog.i(LOG_TAG, msg);
        }
    }
}
