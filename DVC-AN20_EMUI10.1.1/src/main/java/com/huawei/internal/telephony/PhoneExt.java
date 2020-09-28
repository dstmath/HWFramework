package com.huawei.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.ims.ImsException;
import com.android.ims.ImsUt;
import com.android.ims.ImsUtInterface;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstantConversions;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.android.ims.HwImsUtManagerEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;

public class PhoneExt {
    public static final int EVENT_GET_DEVICE_IDENTITY_DONE = 21;
    public static final int EVENT_UNSOL_HW_CALL_ALT_SRV_DONE = 113;
    private static final String LOG_TAG = "PhoneExt";
    public static final String REASON_USER_DATA_ENABLED = "userDataEnabled";
    private static final String ZERO_STRING = "0";
    private CommandsInterfaceEx mCommandsInterfaceEx;
    private DcTrackerEx mDcTrackerEx;
    protected Phone mPhone;
    protected int mPhoneId;
    private ServiceStateTrackerEx mServiceStateTrackerEx;
    private String subTag;

    public void setPhone(Phone phone) {
        if (phone != null) {
            this.mPhone = phone;
            this.mDcTrackerEx = new DcTrackerEx();
            this.mDcTrackerEx.setDcTracker(phone.getDcTracker(1));
            this.mServiceStateTrackerEx = new ServiceStateTrackerEx();
            this.mServiceStateTrackerEx.setServiceStateTracker(phone.getServiceStateTracker());
            this.mCommandsInterfaceEx = new CommandsInterfaceEx();
            this.mCommandsInterfaceEx.setCommandsInterface(this.mPhone.mCi);
            this.subTag = "PhoneExt[" + this.mPhone.getPhoneId() + "]";
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public static PhoneExt getPhoneExt(Object phone) {
        PhoneExt phoneExt = new PhoneExt();
        if (phone instanceof Phone) {
            phoneExt.setPhone((Phone) phone);
        }
        return phoneExt;
    }

    public static PhoneExt[] getPhoneExts(Phone[] phones) {
        int phoneArraysLength = phones != null ? phones.length : 0;
        PhoneExt[] phoneExts = new PhoneExt[phoneArraysLength];
        for (int i = 0; i < phoneArraysLength; i++) {
            phoneExts[i] = new PhoneExt();
            phoneExts[i].setPhone(phones[i]);
        }
        return phoneExts;
    }

    public Message obtainMessage(int what) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.obtainMessage(what);
        }
        return null;
    }

    public Message obtainMessage(int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.obtainMessage(what, obj);
        }
        return null;
    }

    public final Message obtainMessage(int what, int arg1, int arg2) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.obtainMessage(what, arg1, arg2);
        }
        return null;
    }

    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.obtainMessage(what, arg1, arg2, obj);
        }
        return null;
    }

    public void sendMessage(Message message) {
        Phone phone = this.mPhone;
        if (phone != null && message != null) {
            phone.sendMessage(message);
        }
    }

    public DcTrackerEx getDcTracker() {
        return this.mDcTrackerEx;
    }

    public ServiceStateTrackerEx getServiceStateTracker() {
        ServiceStateTrackerEx serviceStateTrackerEx = this.mServiceStateTrackerEx;
        if ((serviceStateTrackerEx == null || serviceStateTrackerEx.getServiceStateTracker() == null) && this.mPhone != null) {
            this.mServiceStateTrackerEx = new ServiceStateTrackerEx();
            this.mServiceStateTrackerEx.setServiceStateTracker(this.mPhone.getServiceStateTracker());
        }
        return this.mServiceStateTrackerEx;
    }

    public Context getContext() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getContext();
        }
        return null;
    }

    public int getPhoneId() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getPhoneId();
        }
        return 0;
    }

    public int getSubId() {
        Phone phone = this.mPhone;
        return phone != null ? phone.getSubId() : KeepaliveStatus.INVALID_HANDLE;
    }

    public boolean hasIccCard() {
        Phone phone = this.mPhone;
        if (phone == null || phone.getIccCard() == null) {
            return false;
        }
        return this.mPhone.getIccCard().hasIccCard();
    }

    public ServiceState getServiceState() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getServiceState();
        }
        return null;
    }

    public String getSubscriberId() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getSubscriberId();
        }
        return null;
    }

    public void setUserDataEnabled(boolean isUserDataEnabled) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getDataEnabledSettings() != null) {
            this.mPhone.getDataEnabledSettings().setUserDataEnabled(isUserDataEnabled);
        }
    }

    public void setInternalDataEnabled(boolean isInternalDataEnabled) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getDataEnabledSettings() != null) {
            this.mPhone.getDataEnabledSettings().setInternalDataEnabled(isInternalDataEnabled);
        }
    }

    public boolean isDataEnabled() {
        Phone phone = this.mPhone;
        if (phone == null || phone.getDataEnabledSettings() == null) {
            return false;
        }
        return this.mPhone.getDataEnabledSettings().isDataEnabled();
    }

    public void notifyDataConnection() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyDataConnection();
        }
    }

    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.registerForServiceStateChanged(h, what, obj);
        }
    }

    public static int getEventIccChangedHw() {
        return Phone.getEventIccChangedHw();
    }

    public Handler getHandler() {
        return this.mPhone;
    }

    public void updateCurrentCarrierInProvider() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.updateCurrentCarrierInProvider();
        }
    }

    public void notifyServiceStateChanged(ServiceState ss) {
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) phone).notifyServiceStateChanged(ss);
        }
    }

    public void notifyServiceStateChanged() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyServiceStateChangedPHw(phone.getServiceState());
        }
    }

    public boolean isPhoneTypeGsm() {
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            return ((GsmCdmaPhone) phone).isPhoneTypeGsm();
        }
        return false;
    }

    public String getUtIMPUFromNetwork() {
        ImsPhone imsPhone;
        Phone phone = this.mPhone;
        if (phone == null || (imsPhone = (ImsPhone) phone.getImsPhone()) == null || !(imsPhone.getCallTracker() instanceof ImsPhoneCallTracker)) {
            return null;
        }
        try {
            ImsUtInterface ut = ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterface();
            if (ut instanceof ImsUt) {
                return HwImsUtManagerEx.getUtIMPUFromNetwork(getPhoneId(), (ImsUt) ut);
            }
            return null;
        } catch (ImsException e) {
            logi("get UtInterface occures exception");
            return null;
        }
    }

    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.setLine1Number(alphaTag, number, onComplete);
        }
        return false;
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.invokeOemRilRequestRaw(data, response);
        }
    }

    public String getImei() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getImei();
        }
        return null;
    }

    public String getDeviceSvn() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getDeviceSvn();
        }
        return null;
    }

    public String getDeviceId() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getDeviceId();
        }
        return null;
    }

    public String getMeid() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getMeid();
        }
        return null;
    }

    public void setMeid(String value) {
        Phone phone = this.mPhone;
        if (phone != null && (phone instanceof GsmCdmaPhone)) {
            ((GsmCdmaPhone) phone).setMeidHw(value);
        }
    }

    public String getEsn() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getEsn();
        }
        return null;
    }

    public IccRecordsEx getIccRecords() {
        if (this.mPhone == null) {
            return null;
        }
        IccRecordsEx iccRecordsEx = new IccRecordsEx();
        iccRecordsEx.setIccRecords(this.mPhone.getIccRecords());
        return iccRecordsEx;
    }

    public CommandsInterfaceEx getCi() {
        CommandsInterfaceEx commandsInterfaceEx = this.mCommandsInterfaceEx;
        if (commandsInterfaceEx != null) {
            return commandsInterfaceEx;
        }
        if (this.mPhone == null) {
            return null;
        }
        CommandsInterfaceEx commandsInterfaceEx2 = new CommandsInterfaceEx();
        commandsInterfaceEx2.setCommandsInterface(this.mPhone.mCi);
        return commandsInterfaceEx2;
    }

    public void notifyMccChanged(String mcc) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyMccChanged(mcc);
        }
    }

    public void setNetworkSelectionModeAutomatic(Message message) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setNetworkSelectionModeAutomatic(message);
        }
    }

    public void globalEccCustom(String operatorNumeric) {
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) phone).globalEccCustom(operatorNumeric);
        }
    }

    public boolean getOOSFlag() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getOOSFlag();
        }
        return false;
    }

    public CellLocation getCellLocation() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getCellLocation();
        }
        return null;
    }

    public void notifySignalStrength() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifySignalStrength();
        }
    }

    public RadioCapabilityEx getRadioCapability() {
        if (this.mPhone == null) {
            return null;
        }
        RadioCapabilityEx radioCapabilityEx = new RadioCapabilityEx();
        radioCapabilityEx.setRadioCapability(this.mPhone.getRadioCapability());
        return radioCapabilityEx;
    }

    public void setOOSFlagOnSelectNetworkManually(boolean isOosFlag) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setOOSFlagOnSelectNetworkManually(isOosFlag);
        }
    }

    public void restoreSavedNetworkSelectionHw(Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.restoreSavedNetworkSelectionHw(response);
        }
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setPreferredNetworkType(networkType, response);
        }
    }

    public void registerForMccChanged(Handler h, int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.registerForMccChanged(h, what, obj);
        }
    }

    public void unregisterForMccChanged(Handler h) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.unregisterForMccChanged(h);
        }
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getCallTracker() != null) {
            this.mPhone.getCallTracker().registerForVoiceCallStarted(h, what, obj);
        }
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getCallTracker() != null) {
            this.mPhone.getCallTracker().registerForVoiceCallEnded(h, what, obj);
        }
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getCallTracker() != null) {
            this.mPhone.getCallTracker().unregisterForVoiceCallStarted(h);
        }
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getCallTracker() != null) {
            this.mPhone.getCallTracker().unregisterForVoiceCallEnded(h);
        }
    }

    public static int getPreferredNetworkMode() {
        return RILConstants.PREFERRED_NETWORK_MODE;
    }

    public int getPhoneType() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getPhoneType();
        }
        return 0;
    }

    public void reRegisterNetwork(Message onComplete) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getServiceStateTracker() != null) {
            this.mPhone.getServiceStateTracker().reRegisterNetwork(onComplete);
        }
    }

    public void sendIntentWhenReenableNr() {
        if (this.mPhone != null) {
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenReenableNr(this.mPhone);
        }
    }

    public void sendIntentWhenDisableNr(int failCause, long delay) {
        if (this.mPhone != null) {
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDisableNr(this.mPhone, failCause, delay);
        }
    }

    public void sendIntentDsUseStatistics(int duration) {
        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDsUseStatistics(this.mPhone, duration);
    }

    public IccFileHandlerEx getIccFileHandlerEx() {
        Phone phone = this.mPhone;
        if (phone == null || phone.getIccFileHandler() == null) {
            return null;
        }
        IccFileHandlerEx iccFileHandlerEx = new IccFileHandlerEx();
        iccFileHandlerEx.setIccFileHandlerEx(this.mPhone.getIccFileHandler());
        return iccFileHandlerEx;
    }

    public String getLine1Number() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getLine1Number();
        }
        return null;
    }

    public String getName() {
        return this.mPhone.getPhoneName();
    }

    public void getCallbarringOption(String facility, int serviceClass, Message response) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            this.mCommandsInterfaceEx.queryFacilityLock(facility, PhoneConfigurationManager.SSSS, serviceClass, response);
        } else {
            imsPhone.getCallBarring(facility, response);
        }
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
            this.mCommandsInterfaceEx.setFacilityLock(facility, isActivate, password, serviceClass, response);
        } else {
            imsPhone.setCallBarring(facility, isActivate, password, response);
        }
    }

    public boolean isSupportCFT() {
        boolean isSupportCFT = false;
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone != null) {
            logi("imsPhone is exist, isSupportCFT");
            isSupportCFT = imsPhone.mHwImsPhoneEx.isSupportCFT();
        }
        logi("isSupportCFT=" + isSupportCFT);
        return isSupportCFT;
    }

    public boolean isCspPlmnEnabled() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isCspPlmnEnabled();
        }
        return false;
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.getCallForwardingOption(commandInterfaceCFReason, onComplete);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, onComplete);
        }
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        if (this.mPhone.getImsPhone() instanceof ImsPhone) {
            ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
            if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isUtEnable()) {
                logi("setCallForwardingUncondTimerOption can not go Ut interface, imsPhone=" + imsPhone);
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                    onComplete.sendToTarget();
                    return;
                }
                return;
            }
            logi("setCallForwardingUncondTimerOption via Ut");
            imsPhone.mHwImsPhoneEx.setCallForwardingUncondTimerOption(startHour, startMinute, endHour, endMinute, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, onComplete);
            return;
        }
        logi("The Phone is not ImsPhone.");
    }

    public String getIccSerialNumber() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getIccSerialNumber();
        }
        return null;
    }

    public void setSystemProperty(String property, String value) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setSystemProperty(property, value);
        }
    }

    public void restoreSavedRadioTech() {
        if (this.mPhone.mCi instanceof RIL) {
            RIL ci = (RIL) this.mPhone.mCi;
            boolean isAirplaneModeOn = false;
            if (Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) == 1) {
                isAirplaneModeOn = true;
            }
            if (ci.getLastRadioTech() >= 0 && isAirplaneModeOn) {
                Rlog.e(LOG_TAG, "change to LastRadioTech" + ci.getLastRadioTech());
                int newVoiceTech = ci.getLastRadioTech();
                Phone phone = this.mPhone;
                if (phone instanceof GsmCdmaPhone) {
                    ((GsmCdmaPhone) phone).phoneObjectUpdaterHw(newVoiceTech);
                }
            }
        }
    }

    public void registerForPreciseCallStateChanged(Handler handler, int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.registerForPreciseCallStateChanged(handler, what, obj);
        }
    }

    public void unregisterForPreciseCallStateChanged(Handler handler) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.unregisterForPreciseCallStateChanged(handler);
        }
    }

    public void registerForDisconnect(Handler handler, int what, Object obj) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.registerForDisconnect(handler, what, obj);
        }
    }

    public void unregisterForDisconnect(Handler handler) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.unregisterForDisconnect(handler);
        }
    }

    public void saveClirSetting(int commandInterfaceCLIRMode) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.saveClirSetting(commandInterfaceCLIRMode);
        }
    }

    public void setRadioPower(boolean isOn, Message msg) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setRadioPower(isOn, msg);
        }
    }

    public void getPreferredNetworkType(Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.getPreferredNetworkType(response);
        }
    }

    public int getCdmaEriIconIndex() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getCdmaEriIconIndex();
        }
        return -1;
    }

    public String getCdmaEriText() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getCdmaEriText();
        }
        return null;
    }

    public void setImsRegistrationState(boolean isRegistered) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setImsRegistrationState(isRegistered);
        }
    }

    public void dispose() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.dispose();
        }
    }

    public boolean isImsRegistered() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isImsRegistered();
        }
        return false;
    }

    public boolean isWifiCallingEnabled() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isWifiCallingEnabled();
        }
        return false;
    }

    public boolean isRadioAvailable() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isRadioAvailable();
        }
        return false;
    }

    public boolean isRadioOn() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isRadioOn();
        }
        return false;
    }

    public boolean isVideoEnabled() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isVideoEnabled();
        }
        return false;
    }

    public String getCdmaGsmImsi() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getCdmaGsmImsi();
        }
        return null;
    }

    public String getCdmaMlplVersion() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getCdmaMlplVersion();
        }
        return null;
    }

    public String getCdmaMsplVersion() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getCdmaMsplVersion();
        }
        return null;
    }

    public boolean isPhoneTypeCdma() {
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            return !((GsmCdmaPhone) phone).isPhoneTypeGsm();
        }
        return false;
    }

    public void setDataRoamingEnabled(boolean isEnabled) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setDataRoamingEnabled(isEnabled);
        }
    }

    public void notifyEmergencyCallRegistrants(boolean isStarted) {
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) phone).notifyEmergencyCallRegistrants(isStarted);
        }
    }

    public String getOperatorNumericHw() {
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            return ((GsmCdmaPhone) phone).getOperatorNumericHw();
        }
        return null;
    }

    public boolean isVolteEnabled() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.isVolteEnabled();
        }
        return false;
    }

    public boolean getIccLockEnabled() {
        Phone phone = this.mPhone;
        if (phone == null || phone.getIccCard() == null) {
            return false;
        }
        return this.mPhone.getIccCard().getIccLockEnabled();
    }

    public IccCardConstantsEx.StateEx getIccCardState() {
        Phone phone = this.mPhone;
        if (phone == null || phone.getIccCard() == null) {
            return IccCardConstantsEx.StateEx.UNKNOWN;
        }
        return IccCardConstantsEx.StateEx.getStateExByState(this.mPhone.getIccCard().getState());
    }

    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getIccCard() != null) {
            this.mPhone.getIccCard().changeIccLockPassword(oldPassword, newPassword, onComplete);
        }
    }

    public void setIccLockEnabled(boolean isEnabled, String password, Message onComplete) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getIccCard() != null) {
            this.mPhone.getIccCard().setIccLockEnabled(isEnabled, password, onComplete);
        }
    }

    public void notifyCellularCommParaReady(int paraType, int pathType, Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyCellularCommParaReady(paraType, pathType, response);
        }
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.cmdForECInfo(event, action, buf);
        }
        return false;
    }

    public void requestForECInfo(Message msg, int event, byte[] buf) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.sendHWSolicited(msg, event, buf);
        }
    }

    public void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.handleUiccAuth(authType, rand, auth, response);
        }
    }

    public void getImsDomain(Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.getImsDomain(response);
        }
    }

    public void setImsDomainConfig(int domainType) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setImsDomainConfig(domainType);
        }
    }

    public void setImsSwitch(boolean isOn) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.setImsSwitch(isOn);
        }
    }

    public boolean getImsSwitch() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getImsSwitch();
        }
        return false;
    }

    public void setNrSwitch(boolean isOn, Message response) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.mCi.setNrSwitch(isOn, response);
        }
    }

    public IccCardApplicationStatusEx.AppTypeEx getCurrentUiccAppType() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return IccCardApplicationStatusEx.AppTypeEx.getAppTypeEx(phone.getCurrentUiccAppType());
        }
        return IccCardApplicationStatusEx.AppTypeEx.APPTYPE_UNKNOWN;
    }

    public int getUiccCardType() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getUiccCardType();
        }
        return 0;
    }

    public void handleMapconImsaReq(byte[] msg) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.handleMapconImsaReq(msg);
        }
    }

    public boolean setISMCOEX(String setISMCoex) {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.setISMCOEX(setISMCoex);
        }
        return false;
    }

    public void setDataRoamingEnabledWithoutPromp(boolean isEnabled) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getDcTracker(1) != null) {
            this.mPhone.getDcTracker(1).setDataRoamingEnabledByUser(isEnabled);
        }
    }

    public void setDataEnabledWithoutPromp(boolean isEnabled) {
        Phone phone = this.mPhone;
        if (phone != null && phone.getDataEnabledSettings() != null) {
            this.mPhone.getDataEnabledSettings().setUserDataEnabled(isEnabled);
        }
    }

    public int getConvertDataConnectionState() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return PhoneConstantConversions.convertDataState(phone.getDataConnectionState());
        }
        return 0;
    }

    public void closeRrc() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.closeRrc();
        }
    }

    public String getNVESN() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getNVESN();
        }
        return null;
    }

    public String getPesn() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getPesn();
        }
        return "0";
    }

    public static PhoneExt getVsimPhone() {
        Phone phone = VSimUtilsInner.getVSimPhone();
        if (phone != null) {
            return getPhoneExt(phone);
        }
        return null;
    }

    private void logi(String msg) {
        Rlog.i(this.subTag, msg);
    }
}
