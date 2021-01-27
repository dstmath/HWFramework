package com.android.internal.telephony;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import com.android.internal.telephony.cdma.HwCDMAPhoneReference;
import com.android.internal.telephony.gsm.HwGSMPhoneReference;
import com.android.internal.telephony.gsm.HwGsmMmiCode;
import com.android.internal.telephony.timezone.HwTimeZoneManager;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.PhoneNumberUtilsEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.GsmCdmaPhoneEx;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public class HwGsmCdmaPhoneEx extends DefaultHwGsmCdmaPhoneEx {
    private static final int AIRPLANE_MODE_ON_MODE = 1;
    private static final String CALL_SATET_EXCEPTION_BACKGROUND = "This call is not allowed to dial for the background call state.";
    private static final String CALL_SATET_EXCEPTION_FOREGROUND = "This call is not allowed to dial for the foreground call state.";
    private static final String DEFAULT_VM_NUMBER_CUST = SystemPropertiesEx.get("ro.voicemail.number", (String) null);
    private static final int EVENT_SET_CALL_FORWARD_DONE = 12;
    private static final String LOG_TAG = "HwGsmCdmaPhoneEx";
    private static final String MDN_NUMBER_CDMA = "mdn_number_key_cdma";
    private static final String SC_WAIT = "43";
    private static final boolean SET_MDN_AS_VM_NUMBER = SystemPropertiesEx.getBoolean("ro.config.hw_setMDNasVMnum", false);
    private static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
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

    /* JADX INFO: Multiple debug info for r1v14 android.os.Message: [D('isCfEnable' boolean), D('rsp' android.os.Message)] */
    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        Message rsp;
        boolean isPhoneTypeGsm = this.mPhoneExt.isPhoneTypeGsm();
        PhoneExt imsPhoneHw = this.mHwGsmCdmaPhoneInner.getImsPhoneHw();
        if ((isPhoneTypeGsm || (imsPhoneHw != null && this.mHwGsmCdmaPhoneInner.isPhoneTypeCdmaLteHw())) && imsPhoneHw != null && imsPhoneHw.isUtEnable()) {
            imsPhoneHw.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, serviceClass, timerSeconds, onComplete);
        } else if (isPhoneTypeGsm) {
            if (this.mHwGsmCdmaPhoneInner.isValidCommandInterfaceCFActionHw(commandInterfaceCFAction)) {
                if (this.mHwGsmCdmaPhoneInner.isValidCommandInterfaceCFReasonHw(commandInterfaceCFReason)) {
                    if (commandInterfaceCFReason == 0) {
                        rsp = this.mPhoneExt.getRspMessage(dialingNumber, onComplete, this.mHwGsmCdmaPhoneInner.isCfEnableHw(commandInterfaceCFAction));
                    } else {
                        rsp = onComplete;
                    }
                    String dialingNumber2 = processPlusSymbol(dialingNumber, this.mPhoneExt.getSubscriberId());
                    CommandsInterfaceEx ciEx = this.mPhoneExt.getCi();
                    if (ciEx != null) {
                        ciEx.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber2, timerSeconds, rsp);
                        return;
                    }
                    return;
                }
            }
            GsmCdmaPhoneEx.sendInvalidMessageBack(onComplete);
        } else {
            RlogEx.e(LOG_TAG, "setCallForwardingOption: not possible in CDMA");
            GsmCdmaPhoneEx.sendInvalidMessageBack(onComplete);
        }
    }

    public boolean dialInternalForCdmaLte(String newDialString) {
        PhoneExt imsPhoneHw;
        if (!(newDialString == null || newDialString.length() == 0 || (imsPhoneHw = this.mHwGsmCdmaPhoneInner.getImsPhoneHw()) == null || !this.mHwGsmCdmaPhoneInner.isPhoneTypeCdmaLteHw())) {
            String networkPortionStr = PhoneNumberUtilsEx.extractNetworkPortionAlt(newDialString);
            MmiCodeExt mmiCodeExt = new MmiCodeExt();
            IHwGsmCdmaPhoneInner iHwGsmCdmaPhoneInner = this.mHwGsmCdmaPhoneInner;
            mmiCodeExt.setGsmMmiCode(networkPortionStr, iHwGsmCdmaPhoneInner, iHwGsmCdmaPhoneInner.getUiccApplicationHw());
            HwChrServiceManagerEx.reportCallException("Telephony", imsPhoneHw.getPhoneId(), 0, "AP_FLOW_SUC");
            String serviceCode = mmiCodeExt.getSc();
            if (serviceCode != null && (SC_WAIT.equals(serviceCode) || mmiCodeExt.isServiceCodeCallForwarding(serviceCode))) {
                this.mHwGsmCdmaPhoneInner.addPendingMMIsHw(mmiCodeExt);
                this.mHwGsmCdmaPhoneInner.notifyRegistrantsHw((Object) null, mmiCodeExt, (Throwable) null);
                if (imsPhoneHw.isUtEnable()) {
                    mmiCodeExt.setImsPhone(imsPhoneHw);
                    mmiCodeExt.processCode();
                } else {
                    RlogEx.e(LOG_TAG, "isUtEnable() state is false");
                }
                return true;
            }
        }
        return false;
    }

    public void autoExitEmergencyCallbackMode() {
        boolean isPhoneInEcmState = this.mHwGsmCdmaPhoneInner.isPhoneInEcmState();
        RlogEx.i(LOG_TAG, "autoExitEmergencyCallbackMode, mIsPhoneInEcmState " + isPhoneInEcmState);
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
            boolean airplaneModeOn = false;
            if (Settings.Global.getInt(phoneExt.getContext().getContentResolver(), "airplane_mode_on", 0) == 1) {
                airplaneModeOn = true;
            }
            int lastRadioTech = this.mPhoneExt.getCi().getLastRadioTech();
            if (lastRadioTech >= 0 && airplaneModeOn) {
                RlogEx.e(LOG_TAG, "change to LastRadioTech" + lastRadioTech);
                this.mPhoneExt.phoneObjectUpdaterHw(lastRadioTech);
            }
        }
    }

    public boolean isDialCsFallback(Exception exception) {
        if (exception == null) {
            return true;
        }
        String message = exception.getMessage();
        if (!CALL_SATET_EXCEPTION_FOREGROUND.equals(message) && !CALL_SATET_EXCEPTION_BACKGROUND.equals(message)) {
            return true;
        }
        RlogEx.e(LOG_TAG, "isCanDialCsFallback: foreground or backgroud call state is invalid");
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

    public boolean getImsSwitch() {
        return this.mGsmReference.getImsSwitch();
    }

    public void setImsSwitch(boolean on) {
        this.mGsmReference.setImsSwitch(on);
    }

    public void cleanDeviceId() {
        RlogEx.d(LOG_TAG, "clean device id");
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
        } else if (response != null) {
            AsyncResultEx.forMessage(response).setResult((Object) null);
            response.sendToTarget();
        }
    }

    public void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message response) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.handleUiccAuth(authType, rand, auth, response);
        } else if (response != null) {
            AsyncResultEx.forMessage(response).setResult((Object) null);
            response.sendToTarget();
        }
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

    public String getCdmaVoiceMailNumberHwCust(Context context, String line1Number, int phoneId) {
        if (context == null) {
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String vmNumberKey = VM_NUMBER_CDMA + phoneId;
        if (!DEFAULT_VM_NUMBER_CUST.equals(BuildConfig.FLAVOR)) {
            return sp.getString(vmNumberKey, DEFAULT_VM_NUMBER_CUST);
        }
        if (!SET_MDN_AS_VM_NUMBER) {
            return sp.getString(vmNumberKey, "*86");
        }
        String oldMdnNumber = sp.getString(MDN_NUMBER_CDMA, BuildConfig.FLAVOR);
        SharedPreferences.Editor editor = sp.edit();
        if (line1Number == null) {
            return sp.getString(vmNumberKey, BuildConfig.FLAVOR);
        }
        if (oldMdnNumber.equals(BuildConfig.FLAVOR)) {
            editor.putString(MDN_NUMBER_CDMA, line1Number);
            editor.commit();
        }
        if (!line1Number.equals(oldMdnNumber)) {
            editor.putString(MDN_NUMBER_CDMA, line1Number);
            editor.putString(vmNumberKey, line1Number);
            editor.commit();
        }
        return sp.getString(vmNumberKey, line1Number);
    }

    public void initHwTimeZoneUpdater(Context context) {
        HwTimeZoneManager.getInstance().initHwTimeZoneUpdater(context);
    }

    public int removeUssdCust(PhoneExt phone) {
        return HwGsmMmiCode.removeUssdCust(phone);
    }

    public boolean isShortCodeHw(String dialString, PhoneExt phone) {
        return HwGsmMmiCode.isShortCodeHw(dialString, phone);
    }

    public String custTimeZoneForMcc(int mcc) {
        return HwMccTable.custTimeZoneForMcc(mcc);
    }

    public void startUploadAvailableNetworks(Object obj) {
        this.mGsmReference.startUploadAvailableNetworks(obj);
    }

    public void stopUploadAvailableNetworks() {
        this.mGsmReference.stopUploadAvailableNetworks();
    }
}
