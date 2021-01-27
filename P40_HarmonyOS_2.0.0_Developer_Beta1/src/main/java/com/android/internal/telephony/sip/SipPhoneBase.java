package com.android.internal.telephony.sip;

import android.content.Context;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telephony.NetworkScanRequest;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public abstract class SipPhoneBase extends Phone {
    private static final String LOG_TAG = "SipPhoneBase";
    private RegistrantList mRingbackRegistrants = new RegistrantList();
    private PhoneConstants.State mState = PhoneConstants.State.IDLE;

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public abstract Call getBackgroundCall();

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public abstract Call getForegroundCall();

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public abstract Call getRingingCall();

    public SipPhoneBase(String name, Context context, PhoneNotifier notifier) {
        super(name, notifier, context, new SipCommandInterface(context), false);
    }

    /* access modifiers changed from: package-private */
    public void migrateFrom(SipPhoneBase from) {
        super.migrateFrom((Phone) from);
        migrate(this.mRingbackRegistrants, from.mRingbackRegistrants);
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mRingbackRegistrants.addUnique(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForRingbackTone(Handler h) {
        this.mRingbackRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.Phone
    public void startRingbackTone() {
        this.mRingbackRegistrants.notifyRegistrants(new AsyncResult((Object) null, Boolean.TRUE, (Throwable) null));
    }

    @Override // com.android.internal.telephony.Phone
    public void stopRingbackTone() {
        this.mRingbackRegistrants.notifyRegistrants(new AsyncResult((Object) null, Boolean.FALSE, (Throwable) null));
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public ServiceState getServiceState() {
        ServiceState s = new ServiceState();
        s.setVoiceRegState(0);
        return s;
    }

    @Override // com.android.internal.telephony.Phone
    public PhoneConstants.State getState() {
        return this.mState;
    }

    @Override // com.android.internal.telephony.Phone
    public int getPhoneType() {
        return 3;
    }

    @Override // com.android.internal.telephony.Phone
    public SignalStrength getSignalStrength() {
        return new SignalStrength();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean getMessageWaitingIndicator() {
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean getCallForwardingIndicator() {
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public List<? extends MmiCode> getPendingMmiCodes() {
        return new ArrayList(0);
    }

    @Override // com.android.internal.telephony.Phone
    public PhoneConstants.DataState getDataConnectionState() {
        return PhoneConstants.DataState.DISCONNECTED;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public PhoneInternalInterface.DataActivityState getDataActivityState() {
        return PhoneInternalInterface.DataActivityState.NONE;
    }

    /* access modifiers changed from: package-private */
    public void notifyPhoneStateChanged() {
    }

    /* access modifiers changed from: package-private */
    public void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }

    /* access modifiers changed from: package-private */
    public void notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
    }

    /* access modifiers changed from: package-private */
    public void notifyDisconnect(Connection cn) {
        this.mDisconnectRegistrants.notifyResult(cn);
    }

    /* access modifiers changed from: package-private */
    public void notifyUnknownConnection() {
        this.mUnknownConnectionRegistrants.notifyResult(this);
    }

    /* access modifiers changed from: package-private */
    public void notifySuppServiceFailed(PhoneInternalInterface.SuppService code) {
        this.mSuppServiceFailedRegistrants.notifyResult(code);
    }

    /* access modifiers changed from: package-private */
    public void notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }

    @Override // com.android.internal.telephony.Phone
    public void notifyCallForwardingIndicator() {
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    public boolean canDial() {
        int serviceState = getServiceState().getState();
        Rlog.v(LOG_TAG, "canDial(): serviceState = " + serviceState);
        if (serviceState == 3) {
            return false;
        }
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Rlog.v(LOG_TAG, "canDial(): disableCall = " + disableCall);
        if (disableCall.equals("true")) {
            return false;
        }
        Rlog.v(LOG_TAG, "canDial(): ringingCall: " + getRingingCall().getState());
        Rlog.v(LOG_TAG, "canDial(): foregndCall: " + getForegroundCall().getState());
        Rlog.v(LOG_TAG, "canDial(): backgndCall: " + getBackgroundCall().getState());
        if (getRingingCall().isRinging()) {
            return false;
        }
        if (!getForegroundCall().getState().isAlive() || !getBackgroundCall().getState().isAlive()) {
            return true;
        }
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean handleInCallMmiCommands(String dialString) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isInCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getRingingCall().getState().isAlive();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean handlePinMmi(String dialString) {
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean handleUssdRequest(String dialString, ResultReceiver wrappedCallback) {
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void sendUssdResponse(String ussdMessge) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void unregisterForSuppServiceNotification(Handler h) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setRadioPower(boolean power) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getVoiceMailNumber() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getVoiceMailAlphaTag() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getDeviceId() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getDeviceSvn() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getImei() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getEsn() {
        Rlog.e(LOG_TAG, "[SipPhone] getEsn() is a CDMA method");
        return ProxyController.MODEM_0;
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getMeid() {
        Rlog.e(LOG_TAG, "[SipPhone] getMeid() is a CDMA method");
        return ProxyController.MODEM_0;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getSubscriberId() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getGroupIdLevel1() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getGroupIdLevel2() {
        return null;
    }

    @Override // com.android.internal.telephony.Phone
    public String getIccSerialNumber() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getLine1Number() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getLine1AlphaTag() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getOutgoingCallerIdDisplay(Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCallWaiting(Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCallWaiting(boolean enable, Message onComplete) {
        Rlog.e(LOG_TAG, "call waiting not supported");
    }

    @Override // com.android.internal.telephony.Phone
    public boolean getIccRecordsLoaded() {
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    public IccCard getIccCard() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getAvailableNetworks(Message response) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startNetworkScan(NetworkScanRequest nsr, Message response) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopNetworkScan(Message response) {
    }

    @Override // com.android.internal.telephony.Phone
    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    @Override // com.android.internal.telephony.Phone
    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
    }

    @Override // com.android.internal.telephony.Phone
    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void updateServiceLocation() {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void enableLocationUpdates() {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void disableLocationUpdates() {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean getDataRoamingEnabled() {
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setDataRoamingEnabled(boolean enable) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean isUserDataEnabled() {
        return false;
    }

    public boolean enableDataConnectivity() {
        return false;
    }

    public boolean disableDataConnectivity() {
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isDataAllowed(int apnType) {
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    public void saveClirSetting(int commandInterfaceCLIRMode) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return null;
    }

    @Override // com.android.internal.telephony.Phone
    public IccFileHandler getIccFileHandler() {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void activateCellBroadcastSms(int activate, Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for SIP.");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCellBroadcastSmsConfig(Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for SIP.");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for SIP.");
    }

    @Override // com.android.internal.telephony.Phone
    public boolean needsOtaServiceProvisioning() {
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    public LinkProperties getLinkProperties(String apnType) {
        return null;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isVideoEnabled() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updatePhoneState() {
        PhoneConstants.State oldState = this.mState;
        if (getRingingCall().isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (!getForegroundCall().isIdle() || !getBackgroundCall().isIdle()) {
            this.mState = PhoneConstants.State.OFFHOOK;
        } else {
            this.mState = PhoneConstants.State.IDLE;
        }
        if (this.mState != oldState) {
            Rlog.d(LOG_TAG, " ^^^ new phone state: " + this.mState);
            notifyPhoneStateChanged();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.Phone
    public void onUpdateIccAvailability() {
    }

    @Override // com.android.internal.telephony.Phone
    public void sendEmergencyCallStateChange(boolean callActive) {
    }

    @Override // com.android.internal.telephony.Phone
    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startUploadAvailableNetworks(Object obj) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopUploadAvailableNetworks() {
    }
}
