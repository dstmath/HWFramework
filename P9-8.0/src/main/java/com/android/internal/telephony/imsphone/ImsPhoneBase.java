package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Pair;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.ArrayList;
import java.util.List;

abstract class ImsPhoneBase extends Phone {
    private static final String LOG_TAG = "ImsPhoneBase";
    private RegistrantList mOnHoldRegistrants = new RegistrantList();
    private RegistrantList mRingbackRegistrants = new RegistrantList();
    private State mState = State.IDLE;
    private RegistrantList mTtyModeReceivedRegistrants = new RegistrantList();

    public ImsPhoneBase(String name, Context context, PhoneNotifier notifier, boolean unitTestMode) {
        super(name, notifier, context, new ImsPhoneCommandInterface(context), unitTestMode);
    }

    public void migrateFrom(Phone from) {
        super.migrateFrom(from);
        migrate(this.mRingbackRegistrants, ((ImsPhoneBase) from).mRingbackRegistrants);
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

    public void registerForOnHoldTone(Handler h, int what, Object obj) {
        this.mOnHoldRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForOnHoldTone(Handler h) {
        this.mOnHoldRegistrants.remove(h);
    }

    protected void startOnHoldTone(Connection cn) {
        this.mOnHoldRegistrants.notifyRegistrants(new AsyncResult(null, new Pair(cn, Boolean.TRUE), null));
    }

    protected void stopOnHoldTone(Connection cn) {
        this.mOnHoldRegistrants.notifyRegistrants(new AsyncResult(null, new Pair(cn, Boolean.FALSE), null));
    }

    public void registerForTtyModeReceived(Handler h, int what, Object obj) {
        this.mTtyModeReceivedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForTtyModeReceived(Handler h) {
        this.mTtyModeReceivedRegistrants.remove(h);
    }

    public void onTtyModeReceived(int mode) {
        this.mTtyModeReceivedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(mode), null));
    }

    public ServiceState getServiceState() {
        ServiceState s = new ServiceState();
        s.setVoiceRegState(0);
        return s;
    }

    public List<CellInfo> getAllCellInfo(WorkSource workSource) {
        return getServiceStateTracker().getAllCellInfo(workSource);
    }

    public CellLocation getCellLocation(WorkSource workSource) {
        return null;
    }

    public State getState() {
        return this.mState;
    }

    public int getPhoneType() {
        return 5;
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

    public DataState getDataConnectionState() {
        return DataState.DISCONNECTED;
    }

    public DataState getDataConnectionState(String apnType) {
        return DataState.DISCONNECTED;
    }

    public DataActivityState getDataActivityState() {
        return DataActivityState.NONE;
    }

    public void notifyPhoneStateChanged() {
        this.mNotifier.notifyPhoneState(this);
    }

    public void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }

    public void notifyDisconnect(Connection cn) {
        this.mDisconnectRegistrants.notifyResult(cn);
        this.mNotifier.notifyDisconnectCause(cn.getDisconnectCause(), cn.getPreciseDisconnectCause());
    }

    void notifyUnknownConnection() {
        this.mUnknownConnectionRegistrants.notifyResult(this);
    }

    void notifySuppServiceFailed(SuppService code) {
        this.mSuppServiceFailedRegistrants.notifyResult(code);
    }

    void notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }

    public void notifyCallForwardingIndicator() {
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    public boolean canDial() {
        boolean z = false;
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
        if (!getRingingCall().isRinging()) {
            if (getForegroundCall().getState().isAlive()) {
                z = getBackgroundCall().getState().isAlive() ^ 1;
            } else {
                z = true;
            }
        }
        return z;
    }

    public boolean handleInCallMmiCommands(String dialString) {
        return false;
    }

    boolean isInCall() {
        Call.State foregroundCallState = getForegroundCall().getState();
        Call.State backgroundCallState = getBackgroundCall().getState();
        Call.State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    public boolean handlePinMmi(String dialString) {
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
        Rlog.e(LOG_TAG, "[VoltePhone] getEsn() is a CDMA method");
        return ProxyController.MODEM_0;
    }

    public String getMeid() {
        Rlog.e(LOG_TAG, "[VoltePhone] getMeid() is a CDMA method");
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

    public void setNetworkSelectionModeAutomatic(Message response) {
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
    }

    public void getDataCallList(Message response) {
    }

    public List<DataConnection> getCurrentDataConnectionList() {
        return null;
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

    public boolean getDataEnabled() {
        return false;
    }

    public void setDataEnabled(boolean enable) {
    }

    public boolean enableDataConnectivity() {
        return false;
    }

    public boolean disableDataConnectivity() {
        return false;
    }

    public boolean isDataConnectivityPossible() {
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
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for Volte.");
    }

    public void getCellBroadcastSmsConfig(Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for Volte.");
    }

    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        Rlog.e(LOG_TAG, "Error! This functionality is not implemented for Volte.");
    }

    public boolean needsOtaServiceProvisioning() {
        return false;
    }

    public LinkProperties getLinkProperties(String apnType) {
        return null;
    }

    protected void onUpdateIccAvailability() {
    }

    void updatePhoneState() {
        State oldState = this.mState;
        if (getRingingCall().isRinging()) {
            this.mState = State.RINGING;
        } else if (getForegroundCall().isIdle() && getBackgroundCall().isIdle()) {
            this.mState = State.IDLE;
        } else {
            this.mState = State.OFFHOOK;
        }
        if (this.mState != oldState) {
            Rlog.d(LOG_TAG, " ^^^ new phone state: " + this.mState);
            notifyPhoneStateChanged();
        }
    }
}
