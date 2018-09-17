package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.UiccCardApplication;

public abstract class AbstractGsmCdmaPhone extends Phone {
    private static final String LOG_TAG = "AbstractGsmCdmaPhone";
    private CDMAPhoneReference mCdmaReference;
    private GSMPhoneReference mGsmReference;
    private final String mName;

    public interface CDMAPhoneReference {
        void afterHandleMessage(Message message);

        boolean beforeHandleMessage(Message message);

        void closeRrc();

        boolean cmdForECInfo(int i, int i2, byte[] bArr);

        int getLteReleaseVersion();

        String getMeid();

        String getPesn();

        boolean isCTSimCard(int i);

        boolean isChinaTelecom(int i);

        void registerForHWBuffer(Handler handler, int i, Object obj);

        void registerForLineControlInfo(Handler handler, int i, Object obj);

        void riseCdmaCutoffFreq(boolean z);

        void selectNetworkManually(OperatorInfo operatorInfo, Message message);

        void sendHWSolicited(Message message, int i, byte[] bArr);

        void setLTEReleaseVersion(boolean z, Message message);

        void setNetworkSelectionModeAutomatic(Message message);

        void switchVoiceCallBackgroundState(int i);

        void unregisterForHWBuffer(Handler handler);

        void unregisterForLineControlInfo(Handler handler);
    }

    public interface GSMPhoneReference {
        void afterHandleMessage(Message message);

        boolean beforeHandleMessage(Message message);

        void changeBarringPassword(String str, String str2, Message message);

        void closeRrc();

        void dispose();

        void getCallbarringOption(String str, String str2, Message message);

        Message getCustAvailableNetworksMessage(Message message);

        String getDefaultVoiceMailAlphaTagText(Context context, String str);

        String getHwCdmaEsn();

        String getHwCdmaPrlVersion();

        void getImsDomain(Message message);

        boolean getImsSwitch();

        int getLteReleaseVersion();

        String getMeid();

        void getPOLCapabilty(Message message);

        String getPesn();

        void getPreferedOperatorList(Message message);

        String getVMNumberWhenIMSIChange();

        void globalEccCustom(String str);

        void handleMapconImsaReq(byte[] bArr);

        void handleUiccAuth(int i, byte[] bArr, byte[] bArr2, Message message);

        boolean isCTSimCard(int i);

        boolean isMmiCode(String str, UiccCardApplication uiccCardApplication);

        boolean isSupportCFT();

        void judgeToLaunchCsgPeriodicSearchTimer();

        void processEccNunmber(ServiceStateTracker serviceStateTracker);

        String processPlusSymbol(String str, String str2);

        void registerForCsgRecordsLoadedEvent();

        void selectCsgNetworkManually(Message message);

        void setCallForwardingUncondTimerOption(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);

        void setCallbarringOption(String str, String str2, boolean z, String str3, Message message);

        boolean setISMCOEX(String str);

        void setImsDomainConfig(int i);

        void setImsSwitch(boolean z);

        void setLTEReleaseVersion(boolean z, Message message);

        void setMeid(String str);

        void setPOLEntry(int i, String str, int i2, Message message);

        void switchVoiceCallBackgroundState(int i);

        void unregisterForCsgRecordsLoadedEvent();

        void updateReduceSARState();
    }

    protected AbstractGsmCdmaPhone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode) {
        super(name, notifier, context, ci, unitTestMode);
        this.mGsmReference = HwTelephonyFactory.getHwPhoneManager().createHwGSMPhoneReference(this);
        this.mCdmaReference = HwTelephonyFactory.getHwPhoneManager().createHwCDMAPhoneReference(this);
        this.mName = name;
    }

    protected AbstractGsmCdmaPhone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode, int phoneId, TelephonyComponentFactory telephonyComponentFactory) {
        super(name, notifier, context, ci, unitTestMode, phoneId, telephonyComponentFactory);
        this.mGsmReference = HwTelephonyFactory.getHwPhoneManager().createHwGSMPhoneReference(this);
        this.mCdmaReference = HwTelephonyFactory.getHwPhoneManager().createHwCDMAPhoneReference(this);
        this.mName = name;
    }

    protected boolean isPhoneTypeGsm() {
        return "GSM".equalsIgnoreCase(this.mName);
    }

    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        this.mGsmReference.getCallbarringOption(facility, serviceClass, response);
    }

    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        this.mGsmReference.setCallbarringOption(facility, password, isActivate, serviceClass, response);
    }

    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        this.mGsmReference.changeBarringPassword(oldPassword, newPassword, response);
    }

    protected Message getCustAvailableNetworksMessage(Message response) {
        return this.mGsmReference.getCustAvailableNetworksMessage(response);
    }

    protected String getDefaultVoiceMailAlphaTagText(Context mContext, String ret) {
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

    public void dispose() {
        synchronized (Phone.lockForRadioTechnologyChange) {
            super.dispose();
            this.mGsmReference.dispose();
        }
    }

    public boolean isMmiCode(String dialString, UiccCardApplication app) {
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

    public void setLTEReleaseVersion(boolean state, Message response) {
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

    public void processEccNunmber(ServiceStateTracker gSST) {
        this.mGsmReference.processEccNunmber(gSST);
    }

    public void globalEccCustom(String operatorNumeric) {
        this.mGsmReference.globalEccCustom(operatorNumeric);
    }

    public String getCdmaPrlVersion() {
        return this.mGsmReference.getHwCdmaPrlVersion();
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

    public void setNetworkSelectionModeAutomatic(Message response) {
        if (isPhoneTypeGsm()) {
            super.setNetworkSelectionModeAutomatic(response);
        } else if (isChinaTelecom(getPhoneId())) {
            Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomatic: It can run in ChinaTelecom");
            super.setNetworkSelectionModeAutomatic(response);
        } else {
            this.mCdmaReference.setNetworkSelectionModeAutomatic(response);
        }
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        if (isPhoneTypeGsm()) {
            super.selectNetworkManually(network, persistSelection, response);
        } else if (isChinaTelecom(getPhoneId())) {
            Rlog.d(LOG_TAG, "selectNetworkManually: It can run in ChinaTelecom");
            super.selectNetworkManually(network, persistSelection, response);
        } else {
            this.mCdmaReference.selectNetworkManually(network, response);
        }
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

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message response) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.handleUiccAuth(auth_type, rand, auth, response);
            return;
        }
        AsyncResult.forMessage(response).result = null;
        response.sendToTarget();
    }

    public void handleMapconImsaReq(byte[] Msg) {
        if (isPhoneTypeGsm()) {
            this.mGsmReference.handleMapconImsaReq(Msg);
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
}
