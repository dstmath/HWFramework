package com.android.internal.telephony.sip;

import android.content.Context;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.CellLocation;
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

abstract class SipPhoneBase extends Phone {
    private static final String LOG_TAG = "SipPhoneBase";
    private RegistrantList mRingbackRegistrants = new RegistrantList();
    private PhoneConstants.State mState = PhoneConstants.State.IDLE;

    public abstract Call getBackgroundCall();

    public abstract Call getForegroundCall();

    public abstract Call getRingingCall();

    public SipPhoneBase(String name, Context context, PhoneNotifier notifier) {
        super(name, notifier, context, new SipCommandInterface(context), false);
    }

    /* access modifiers changed from: package-private */
    public void migrateFrom(SipPhoneBase from) {
        super.migrateFrom(from);
        migrate(this.mRingbackRegistrants, from.mRingbackRegistrants);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mRingbackRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForRingbackTone(Handler h) {
        this.mRingbackRegistrants.remove(h);
    }

    public void startRingbackTone() {
        this.mRingbackRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.TRUE, null));
    }

    public void stopRingbackTone() {
        this.mRingbackRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.FALSE, null));
    }

    public ServiceState getServiceState() {
        ServiceState s = new ServiceState();
        s.setVoiceRegState(0);
        return s;
    }

    public CellLocation getCellLocation(WorkSource workSource) {
        return null;
    }

    public PhoneConstants.State getState() {
        return this.mState;
    }

    public int getPhoneType() {
        return 3;
    }

    public SignalStrength getSignalStrength() {
        return new SignalStrength();
    }

    public boolean getMessageWaitingIndicator() {
        return false;
    }

    public boolean getCallForwardingIndicator() {
        return false;
    }

    public List<? extends MmiCode> getPendingMmiCodes() {
        return new ArrayList(0);
    }

    public PhoneConstants.DataState getDataConnectionState() {
        return PhoneConstants.DataState.DISCONNECTED;
    }

    public PhoneConstants.DataState getDataConnectionState(String apnType) {
        return PhoneConstants.DataState.DISCONNECTED;
    }

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

    public void notifyCallForwardingIndicator() {
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    public boolean canDial() {
        int serviceState = getServiceState().getState();
        Rlog.v(LOG_TAG, "canDial(): serviceState = " + serviceState);
        boolean z = false;
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
        if (!getRingingCall().isRinging() && (!getForegroundCall().getState().isAlive() || !getBackgroundCall().getState().isAlive())) {
            z = true;
        }
        return z;
    }

    public boolean handleInCallMmiCommands(String dialString) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isInCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getRingingCall().getState().isAlive();
    }

    public boolean handlePinMmi(String dialString) {
        return false;
    }

    public boolean handleUssdRequest(String dialString, ResultReceiver wrappedCallback) {
        return false;
    }

    public void sendUssdResponse(String ussdMessge) {
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
    }

    public void unregisterForSuppServiceNotification(Handler h) {
    }

    public void setRadioPower(boolean power) {
    }

    public String getVoiceMailNumber() {
        return null;
    }

    public String getVoiceMailAlphaTag() {
        return null;
    }

    public String getDeviceId() {
        return null;
    }

    public String getDeviceSvn() {
        return null;
    }

    public String getImei() {
        return null;
    }

    public String getEsn() {
        Rlog.e(LOG_TAG, "[SipPhone] getEsn() is a CDMA method");
        return ProxyController.MODEM_0;
    }

    public String getMeid() {
        Rlog.e(LOG_TAG, "[SipPhone] getMeid() is a CDMA method");
        return ProxyController.MODEM_0;
    }

    public String getSubscriberId() {
        return null;
    }

    public String getGroupIdLevel1() {
        return null;
    }

    public String getGroupIdLevel2() {
        return null;
    }

    public String getIccSerialNumber() {
        return null;
    }

    public String getLine1Number() {
        return null;
    }

    public String getLine1AlphaTag() {
        return null;
    }

    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        return false;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void getCallWaiting(Message onComplete) {
        AsyncResult.forMessage(onComplete, null, null);
        onComplete.sendToTarget();
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        Rlog.e(LOG_TAG, "call waiting not supported");
    }

    public boolean getIccRecordsLoaded() {
        return false;
    }

    public IccCard getIccCard() {
        return null;
    }

    public void getAvailableNetworks(Message response) {
    }

    public void startNetworkScan(NetworkScanRequest nsr, Message response) {
    }

    public void stopNetworkScan(Message response) {
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
    }

    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
    }

    public void updateServiceLocation() {
    }

    public void enableLocationUpdates() {
    }

    public void disableLocationUpdates() {
    }

    public boolean getDataRoamingEnabled() {
        return false;
    }

    public void setDataRoamingEnabled(boolean enable) {
    }

    public boolean isUserDataEnabled() {
        return false;
    }

    public boolean isDataEnabled() {
        return false;
    }

    public void setUserDataEnabled(boolean enable) {
    }

    public boolean enableDataConnectivity() {
        return false;
    }

    public boolean disableDataConnectivity() {
        return false;
    }

    public boolean isDataAllowed() {
        return false;
    }

    public void saveClirSetting(int commandInterfaceCLIRMode) {
    }

    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return null;
    }

    public IccFileHandler getIccFileHandler() {
        return null;
    }

    public void activateCellBroadcastSms(int activate, Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for SIP.");
    }

    public void getCellBroadcastSmsConfig(Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for SIP.");
    }

    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for SIP.");
    }

    public boolean needsOtaServiceProvisioning() {
        return false;
    }

    public LinkProperties getLinkProperties(String apnType) {
        return null;
    }

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
    public void onUpdateIccAvailability() {
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
    }

    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
    }
}
