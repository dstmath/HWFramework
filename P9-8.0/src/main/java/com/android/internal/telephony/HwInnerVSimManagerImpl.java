package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwInnerVSimManagerImpl implements HwInnerVSimManager {
    private static boolean HWFLOW = false;
    private static final boolean HWLOGW_E = true;
    private static final int IS_M2_CS_ONLY = 4;
    private static final int IS_MMS_ON_M2 = 5;
    private static final int IS_RADIO_AVAILABLE = 7;
    private static final int IS_SUB_ON_M2 = 6;
    private static final int IS_VSIM_CAUSE_CARD_RELOAD = 8;
    private static final int IS_VSIM_ENABLED = 9;
    private static final int IS_VSIM_IN_PROCESS = 2;
    private static final int IS_VSIM_ON = 1;
    private static final int IS_VSIM_RECONNECTING = 3;
    private static final String LOG_TAG = "HwInnerVSimMngrImpl";
    private static final int MMS_START = 1;
    private static final int MMS_STOP = 2;
    private static final int NEED_BLOCK_PIN = 10;
    private static final int NEED_BLOCK_UNRESERVED_SUBID = 11;
    private static HwInnerVSimManager mInstance = new HwInnerVSimManagerImpl();
    private HwVSimService mHwVSimService;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(LOG_TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static HwInnerVSimManager getDefault() {
        return mInstance;
    }

    public void createHwVSimService(Context context) {
        this.mHwVSimService = HwVSimService.getDefault(context);
        if (this.mHwVSimService == null) {
            Rlog.e(LOG_TAG, "create vsim service not success");
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
            case 3:
                result = HwVSimController.getInstance().isVSimReconnecting();
                break;
            case 4:
                result = HwVSimController.getInstance().isM2CSOnly();
                break;
            case 5:
                result = HwVSimController.getInstance().isMmsOnM2();
                break;
            case 6:
                result = HwVSimController.getInstance().isSubOnM2(subId);
                break;
            case 7:
                if (HwVSimUtils.isPlatformTwoModems()) {
                    CommandsInterface ci = HwVSimController.getInstance().getCiBySub(subId);
                    if (ci != null) {
                        result = ci.isRadioAvailable();
                        break;
                    }
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

    public void setLastRilFailCause(int cause) {
        HwVSimController.getInstance().setLastRilFailCause(cause);
    }

    public void setMarkForCardReload(int subId, boolean value) {
        HwVSimController.getInstance().setMarkForCardReload(subId, value);
    }

    public void checkMmsForVSim(int type, int subId) {
        if (HwVSimController.isInstantiated()) {
            switch (type) {
                case 1:
                    HwVSimController.getInstance().checkMmsStart(subId);
                    break;
                case 2:
                    HwVSimController.getInstance().checkMmsStop(subId);
                    break;
                default:
                    if (HWFLOW) {
                        Rlog.i(LOG_TAG, "checkMmsForVSim invalid type " + type);
                        break;
                    }
                    break;
            }
        }
    }

    public String getPendingDeviceInfoFromSP(String prefKey) {
        if (HwVSimController.isInstantiated() && HwVSimUtils.isPlatformTwoModems()) {
            return HwVSimController.getInstance().getPendingDeviceInfoFromSP(prefKey);
        }
        return null;
    }

    public int getTopPrioritySubscriptionId() {
        if (HwVSimController.isInstantiated()) {
            return HwVSimPhoneFactory.getTopPrioritySubscriptionId();
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }
}
