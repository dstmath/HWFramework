package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.cdma.HwCDMAPhoneReference;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.HwGSMPhoneReference;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgrImpl;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public class HwGsmCdmaPhoneEx extends DefaultHwGsmCdmaPhoneEx {
    private static final int EVENT_SET_CALL_FORWARD_DONE = 12;
    private static final String LOG_TAG = "HwGsmCdmaPhoneEx";
    private static final String SC_WAIT = "43";
    private HwCDMAPhoneReference mCdmaReference;
    private HwGSMPhoneReference mGsmReference;
    private IHwGsmCdmaPhoneInner mHwGsmCdmaPhoneInner;
    private PhoneExt mPhoneExt;

    public HwGsmCdmaPhoneEx(IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner, PhoneExt phoneExt) {
        super(phoneExt.getContext());
        this.mHwGsmCdmaPhoneInner = hwGsmCdmaPhoneInner;
        this.mPhoneExt = phoneExt;
        this.mGsmReference = new HwGSMPhoneReference(hwGsmCdmaPhoneInner, phoneExt);
        this.mCdmaReference = new HwCDMAPhoneReference(hwGsmCdmaPhoneInner, phoneExt);
    }

    /* JADX INFO: Multiple debug info for r1v13 android.os.Message: [D('cfu' com.android.internal.telephony.GsmCdmaPhone$Cfu), D('rsp' android.os.Message)] */
    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        Message rsp;
        boolean isPhoneTypeGsm = this.mPhoneExt.isPhoneTypeGsm();
        Phone imsPhoneHw = this.mHwGsmCdmaPhoneInner.getImsPhoneHw();
        if ((isPhoneTypeGsm || (imsPhoneHw != null && this.mHwGsmCdmaPhoneInner.isPhoneTypeCdmaLteHw())) && imsPhoneHw != null && ((ImsPhone) imsPhoneHw).mHwImsPhoneEx.isUtEnable()) {
            ((ImsPhone) imsPhoneHw).setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, serviceClass, timerSeconds, onComplete);
        } else if (!isPhoneTypeGsm) {
            Rlog.e(LOG_TAG, "setCallForwardingOption: not possible in CDMA");
            sendInvalidMessageBack(onComplete);
        } else if (!this.mHwGsmCdmaPhoneInner.isValidCommandInterfaceCFActionHw(commandInterfaceCFAction) || !this.mHwGsmCdmaPhoneInner.isValidCommandInterfaceCFReasonHw(commandInterfaceCFReason)) {
            sendInvalidMessageBack(onComplete);
        } else {
            if (commandInterfaceCFReason == 0) {
                rsp = this.mPhoneExt.obtainMessage(12, this.mHwGsmCdmaPhoneInner.isCfEnableHw(commandInterfaceCFAction) ? 1 : 0, 0, new GsmCdmaPhone.Cfu(dialingNumber, onComplete));
            } else {
                rsp = onComplete;
            }
            this.mPhoneExt.getCi().setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, processPlusSymbol(dialingNumber, this.mPhoneExt.getSubscriberId()), timerSeconds, rsp);
        }
    }

    private void sendInvalidMessageBack(Message onComplete) {
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    public boolean dialInternalForCdmaLte(String newDialString) {
        Phone imsPhoneHw;
        HwChrServiceManager hwChrServiceManager;
        if (!(newDialString == null || newDialString.length() == 0 || (imsPhoneHw = this.mHwGsmCdmaPhoneInner.getImsPhoneHw()) == null || !this.mHwGsmCdmaPhoneInner.isPhoneTypeCdmaLteHw())) {
            String networkPortionStr = PhoneNumberUtils.extractNetworkPortionAlt(newDialString);
            IHwGsmCdmaPhoneInner iHwGsmCdmaPhoneInner = this.mHwGsmCdmaPhoneInner;
            GsmMmiCode mmiCode = GsmMmiCode.newFromDialString(networkPortionStr, iHwGsmCdmaPhoneInner, iHwGsmCdmaPhoneInner.getUiccApplicationHw());
            if (!(mmiCode == null || (hwChrServiceManager = HwTelephonyFactory.getHwChrServiceManager()) == null)) {
                hwChrServiceManager.reportCallException("Telephony", this.mPhoneExt.getPhoneId(), 0, "AP_FLOW_SUC");
            }
            if ((mmiCode == null || mmiCode.getmSC() == null || (!mmiCode.getmSC().equals(SC_WAIT) && !GsmMmiCode.isServiceCodeCallForwarding(mmiCode.getmSC()))) ? false : true) {
                this.mHwGsmCdmaPhoneInner.addPendingMMIsHw(mmiCode);
                this.mHwGsmCdmaPhoneInner.notifyRegistrantsHw(new AsyncResult((Object) null, mmiCode, (Throwable) null));
                if (((ImsPhone) imsPhoneHw).mHwImsPhoneEx.isUtEnable()) {
                    mmiCode.setImsPhone(imsPhoneHw);
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
        boolean isPhoneInEcmState = this.mHwGsmCdmaPhoneInner.isPhoneInEcmState();
        Rlog.i(LOG_TAG, "autoExitEmergencyCallbackMode, mIsPhoneInEcmState " + isPhoneInEcmState);
        if (isPhoneInEcmState) {
            this.mHwGsmCdmaPhoneInner.removeCallbacksHw();
            this.mHwGsmCdmaPhoneInner.handleEcmExitRespRegistrant();
            this.mHwGsmCdmaPhoneInner.handleWakeLock();
            this.mHwGsmCdmaPhoneInner.setPhoneInEcmState(false);
            this.mPhoneExt.setSystemProperty("ril.cdma.inecmmode", "false");
            this.mHwGsmCdmaPhoneInner.sendEmergencyCallbackModeChangeHw();
            this.mPhoneExt.setInternalDataEnabled(true);
            this.mHwGsmCdmaPhoneInner.notifyEmergencyCallRegistrantsHw(false);
        }
    }

    public void restoreSavedRadioTech() {
        PhoneExt phoneExt = this.mPhoneExt;
        if (phoneExt != null) {
            phoneExt.restoreSavedRadioTech();
        }
    }

    public boolean isDialCsFallback(Exception exception) {
        if (exception == null) {
            return true;
        }
        String message = exception.getMessage();
        if (!HwImsPhoneCallTrackerMgrImpl.CALL_SATET_EXCEPTION_FOREGROUND.equals(message) && !HwImsPhoneCallTrackerMgrImpl.CALL_SATET_EXCEPTION_BACKGROUND.equals(message)) {
            return true;
        }
        Rlog.e(LOG_TAG, "isCanDialCsFallback: foreground or backgroud call state is invalid");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isPhoneTypeGsm() {
        return this.mPhoneExt.isPhoneTypeGsm();
    }

    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        this.mGsmReference.getCallbarringOption(facility, serviceClass, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        this.mGsmReference.setCallbarringOption(facility, password, isActivate, serviceClass, response);
    }

    public void getCallbarringOption(String facility, int serviceClass, Message response) {
        this.mGsmReference.getCallbarringOption(facility, serviceClass, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
        this.mGsmReference.setCallbarringOption(facility, password, isActivate, serviceClass, response);
    }

    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        this.mGsmReference.changeBarringPassword(oldPassword, newPassword, response);
    }

    public Message getCustAvailableNetworksMessage(Message response) {
        return this.mGsmReference.getCustAvailableNetworksMessage(response);
    }

    public String getDefaultVoiceMailAlphaTagText(Context mContext, String ret) {
        return this.mGsmReference.getDefaultVoiceMailAlphaTagText(mContext, ret);
    }

    public boolean isSupportCFT() {
        return this.mGsmReference.isSupportCFT();
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        this.mGsmReference.setCallForwardingUncondTimerOption(startHour, startMinute, endHour, endMinute, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, onComplete);
    }

    public void setImsSwitch(boolean on) {
        this.mGsmReference.setImsSwitch(on);
    }

    public boolean getImsSwitch() {
        return this.mGsmReference.getImsSwitch();
    }

    public void cleanDeviceId() {
        Rlog.d(LOG_TAG, "clean device id");
        this.mGsmReference.setMeid(null);
    }

    public void updateReduceSARState() {
        this.mGsmReference.updateReduceSARState();
    }

    public void resetReduceSARPowerGrade() {
        this.mGsmReference.resetReduceSARPowerGrade();
    }

    public boolean isMmiCode(String dialString, UiccCardApplicationEx app) {
        return this.mGsmReference.isMmiCode(dialString, app);
    }

    public void getPOLCapabilty(Message response) {
        this.mGsmReference.getPOLCapabilty(response);
    }

    public void getPreferedOperatorList(Message response) {
        this.mGsmReference.getPreferedOperatorList(response);
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        this.mGsmReference.setPOLEntry(index, numeric, nAct, response);
    }

    public void setLTEReleaseVersion(int state, Message response) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.setLTEReleaseVersion(state, response);
        } else {
            this.mCdmaReference.setLTEReleaseVersion(state, response);
        }
    }

    public int getLteReleaseVersion() {
        if (isPhoneTypeGsm()) {
            return this.mGsmReference.getLteReleaseVersion();
        }
        return this.mCdmaReference.getLteReleaseVersion();
    }

    public String processPlusSymbol(String dialNumber, String imsi) {
        return this.mGsmReference.processPlusSymbol(dialNumber, imsi);
    }

    public void processEccNumber(IServiceStateTrackerInner sST) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.processEccNumber(sST);
        } else {
            this.mCdmaReference.processEccNumber(sST);
        }
    }

    public void globalEccCustom(String operatorNumeric) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.globalEccCustom(operatorNumeric);
        } else {
            this.mCdmaReference.globalEccCustom(operatorNumeric);
        }
    }

    public String getHwCdmaEsn() {
        return this.mGsmReference.getHwCdmaEsn();
    }

    public String getVMNumberWhenIMSIChange() {
        return this.mGsmReference.getVMNumberWhenIMSIChange();
    }

    public boolean setISMCOEX(String setISMCoex) {
        return this.mGsmReference.setISMCOEX(setISMCoex);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mCdmaReference.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mCdmaReference.unregisterForLineControlInfo(h);
    }

    public void riseCdmaCutoffFreq(boolean on) {
        this.mCdmaReference.riseCdmaCutoffFreq(on);
    }

    public boolean isChinaTelecom(int slotId) {
        return this.mCdmaReference.isChinaTelecom(slotId);
    }

    public String getMeid() {
        if (isPhoneTypeGsm()) {
            return this.mGsmReference.getMeid();
        }
        return this.mCdmaReference.getMeid();
    }

    public String getPesn() {
        if (isPhoneTypeGsm()) {
            return this.mGsmReference.getPesn();
        }
        return this.mCdmaReference.getPesn();
    }

    public boolean beforeHandleMessage(Message msg) {
        if (isPhoneTypeGsm()) {
            return this.mGsmReference.beforeHandleMessage(msg);
        }
        return this.mCdmaReference.beforeHandleMessage(msg);
    }

    public void afterHandleMessage(Message msg) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.afterHandleMessage(msg);
        } else {
            this.mCdmaReference.afterHandleMessage(msg);
        }
    }

    public void closeRrc() {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.closeRrc();
        } else {
            this.mCdmaReference.closeRrc();
        }
    }

    public void switchVoiceCallBackgroundState(int state) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.switchVoiceCallBackgroundState(state);
        } else {
            this.mCdmaReference.switchVoiceCallBackgroundState(state);
        }
    }

    public boolean isCTSimCard(int slotId) {
        if (isPhoneTypeGsm()) {
            return this.mGsmReference.isCTSimCard(slotId);
        }
        return this.mCdmaReference.isCTSimCard(slotId);
    }

    public void setImsDomainConfig(int domainType) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.setImsDomainConfig(domainType);
        }
    }

    public void getImsDomain(Message response) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.getImsDomain(response);
            return;
        }
        AsyncResult.forMessage(response).result = null;
        response.sendToTarget();
    }

    public void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message response) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.handleUiccAuth(authType, rand, auth, response);
            return;
        }
        AsyncResult.forMessage(response).result = null;
        response.sendToTarget();
    }

    public void handleMapconImsaReq(byte[] msg) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.handleMapconImsaReq(msg);
        }
    }

    public void selectCsgNetworkManually(Message response) {
        this.mGsmReference.selectCsgNetworkManually(response);
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        this.mGsmReference.judgeToLaunchCsgPeriodicSearchTimer();
    }

    public void registerForCsgRecordsLoadedEvent() {
        this.mGsmReference.registerForCsgRecordsLoadedEvent();
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        this.mGsmReference.unregisterForCsgRecordsLoadedEvent();
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mCdmaReference.registerForHWBuffer(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        this.mCdmaReference.unregisterForHWBuffer(h);
    }

    public void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
        this.mCdmaReference.sendHWSolicited(reqMsg, event, reqData);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return this.mCdmaReference.cmdForECInfo(event, action, buf);
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.notifyCellularCommParaReady(paratype, pathtype, response);
        } else {
            this.mCdmaReference.notifyCellularCommParaReady(paratype, pathtype, response);
        }
    }

    public void updateWfcMode(Context context, boolean roaming, int subId) throws ImsExceptionExt {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.updateWfcMode(context, roaming, subId);
        } else {
            this.mCdmaReference.updateWfcMode(context, roaming, subId);
        }
    }

    public void registerForCallAltSrv(Handler h, int what, Object obj) {
        if (this.mPhoneExt.getCi() != null) {
            this.mPhoneExt.getCi().registerForCallAltSrv(h, what, obj);
        }
    }

    public void unregisterForCallAltSrv(Handler h) {
        if (this.mPhoneExt.getCi() != null) {
            this.mPhoneExt.getCi().unregisterForCallAltSrv(h);
        }
    }

    public boolean isDualImsAvailable() {
        if (isPhoneTypeGsm()) {
            return this.mGsmReference.isDualImsAvailable();
        }
        return this.mCdmaReference.isDualImsAvailable();
    }

    public boolean isUssdOkForRelease() {
        return this.mGsmReference.isUssdOkForRelease();
    }

    public void logForImei(String phoneType, String imei) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.logForImei(phoneType, imei);
        } else {
            this.mCdmaReference.logForImei(phoneType, imei);
        }
    }

    public String getCdmaMlplVersion(String mlplVersion) {
        return this.mCdmaReference.getCdmaMlplVersion(mlplVersion);
    }

    public String getCdmaMsplVersion(String msplVersion) {
        return this.mCdmaReference.getCdmaMsplVersion(msplVersion);
    }

    public void dispose() {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.dispose();
        }
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        this.mCdmaReference.setNetworkSelectionModeAutomatic(response);
    }

    public void selectNetworkManually(Message response) {
        this.mCdmaReference.selectNetworkManually(response);
    }
}
