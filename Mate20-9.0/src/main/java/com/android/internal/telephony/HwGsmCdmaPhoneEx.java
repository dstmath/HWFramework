package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.imsphone.ImsPhone;

public class HwGsmCdmaPhoneEx implements IHwGsmCdmaPhoneEx {
    private static final int EVENT_SET_CALL_FORWARD_DONE = 12;
    private static final String LOG_TAG = "HwGsmCdmaPhoneEx";
    private static final String SC_WAIT = "43";
    private IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner;

    public HwGsmCdmaPhoneEx(IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner2) {
        this.hwGsmCdmaPhoneInner = hwGsmCdmaPhoneInner2;
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        Message rsp;
        int i = commandInterfaceCFAction;
        int i2 = commandInterfaceCFReason;
        String str = dialingNumber;
        GsmCdmaPhone gsmCdmaPhone = this.hwGsmCdmaPhoneInner.getGsmCdmaPhone();
        boolean isPhoneTypeGsm = gsmCdmaPhone.isPhoneTypeGsm();
        ImsPhone imsPhoneHw = this.hwGsmCdmaPhoneInner.getImsPhoneHw();
        if (isPhoneTypeGsm || (imsPhoneHw != null && gsmCdmaPhone.isPhoneTypeCdmaLte())) {
            ImsPhone imsPhone = imsPhoneHw;
            if (imsPhone != null && imsPhone.mHwImsPhoneEx.isUtEnable()) {
                imsPhone.setCallForwardingOption(i, i2, str, serviceClass, timerSeconds, onComplete);
                return;
            }
        }
        if (!isPhoneTypeGsm) {
            Message message = onComplete;
            Rlog.e(LOG_TAG, "setCallForwardingOption: not possible in CDMA");
        } else if (!this.hwGsmCdmaPhoneInner.isValidCommandInterfaceCFActionHw(i) || !this.hwGsmCdmaPhoneInner.isValidCommandInterfaceCFReasonHw(i2)) {
            Message message2 = onComplete;
        } else {
            if (i2 == 0) {
                rsp = gsmCdmaPhone.obtainMessage(12, this.hwGsmCdmaPhoneInner.isCfEnableHw(i) ? 1 : 0, 0, new GsmCdmaPhone.Cfu(str, onComplete));
            } else {
                rsp = onComplete;
            }
            gsmCdmaPhone.mCi.setCallForward(i, i2, serviceClass, gsmCdmaPhone.processPlusSymbol(str, gsmCdmaPhone.getSubscriberId()), timerSeconds, rsp);
        }
    }

    public boolean dialInternalForCdmaLte(String newDialString) {
        if (newDialString == null || newDialString.length() == 0) {
            return false;
        }
        Phone imsPhoneHw = this.hwGsmCdmaPhoneInner.getImsPhoneHw();
        GsmCdmaPhone gsmCdmaPhone = this.hwGsmCdmaPhoneInner.getGsmCdmaPhone();
        if (imsPhoneHw != null && gsmCdmaPhone.isPhoneTypeCdmaLte()) {
            GsmMmiCode mmiCode = GsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), gsmCdmaPhone, this.hwGsmCdmaPhoneInner.getUiccApplicationHw());
            Phone imsPhone = imsPhoneHw;
            if (mmiCode != null) {
                HwChrServiceManager hwChrServiceManager = HwTelephonyFactory.getHwChrServiceManager();
                if (hwChrServiceManager != null) {
                    hwChrServiceManager.reportCallException("Telephony", gsmCdmaPhone.getSubId(), 0, "AP_FLOW_SUC");
                }
            }
            if ((mmiCode == null || mmiCode.getmSC() == null || (!mmiCode.getmSC().equals(SC_WAIT) && !GsmMmiCode.isServiceCodeCallForwarding(mmiCode.getmSC()))) ? false : true) {
                this.hwGsmCdmaPhoneInner.addPendingMMIsHw(mmiCode);
                this.hwGsmCdmaPhoneInner.notifyRegistrantsHw(new AsyncResult(null, mmiCode, null));
                if (((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                    mmiCode.setImsPhone(imsPhone);
                    try {
                        mmiCode.processCode();
                    } catch (CallStateException e) {
                        Rlog.e(LOG_TAG, "processCode error");
                    }
                } else {
                    Rlog.e(LOG_TAG, "isUtEnable() state is false");
                }
                return true;
            }
        }
        return false;
    }

    public void autoExitEmergencyCallbackMode() {
        boolean isPhoneInEcmState = this.hwGsmCdmaPhoneInner.isPhoneInEcmState();
        Rlog.d(LOG_TAG, "autoExitEmergencyCallbackMode, mIsPhoneInEcmState " + isPhoneInEcmState);
        if (isPhoneInEcmState) {
            GsmCdmaPhone phone = this.hwGsmCdmaPhoneInner.getGsmCdmaPhone();
            this.hwGsmCdmaPhoneInner.removeCallbacksHw();
            this.hwGsmCdmaPhoneInner.handleEcmExitRespRegistrant();
            this.hwGsmCdmaPhoneInner.handleWakeLock();
            this.hwGsmCdmaPhoneInner.setPhoneInEcmState(false);
            phone.setSystemProperty("ril.cdma.inecmmode", "false");
            this.hwGsmCdmaPhoneInner.sendEmergencyCallbackModeChangeHw();
            phone.mDcTracker.setInternalDataEnabled(true);
            phone.notifyEmergencyCallRegistrants(false);
        }
    }

    public void restoreSavedRadioTech() {
        GsmCdmaPhone phone = this.hwGsmCdmaPhoneInner.getGsmCdmaPhone();
        if (phone.mCi instanceof RIL) {
            RIL ci = phone.mCi;
            boolean z = true;
            if (Settings.Global.getInt(phone.getContext().getContentResolver(), "airplane_mode_on", 0) != 1) {
                z = false;
            }
            boolean airplaneModeOn = z;
            if (ci.getLastRadioTech() >= 0 && airplaneModeOn) {
                Rlog.e(LOG_TAG, "change to LastRadioTech" + ci.getLastRadioTech());
                this.hwGsmCdmaPhoneInner.phoneObjectUpdaterHw(ci.getLastRadioTech());
            }
        }
    }
}
