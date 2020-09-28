package com.android.internal.telephony.sip;

import android.content.Context;
import android.media.AudioManager;
import android.net.LinkProperties;
import android.net.rtp.AudioGroup;
import android.net.sip.SipAudioCall;
import android.net.sip.SipErrorCode;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class SipPhone extends SipPhoneBase {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SipPhone";
    private static final int TIMEOUT_ANSWER_CALL = 8;
    private static final int TIMEOUT_HOLD_CALL = 15;
    private static final long TIMEOUT_HOLD_PROCESSING = 1000;
    private static final int TIMEOUT_MAKE_CALL = 15;
    private static final boolean VDBG = false;
    private SipCall mBackgroundCall = new SipCall();
    private SipCall mForegroundCall = new SipCall();
    private SipProfile mProfile;
    private SipCall mRingingCall = new SipCall();
    private SipManager mSipManager;
    private long mTimeOfLastValidHoldRequest = System.currentTimeMillis();

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void activateCellBroadcastSms(int i, Message message) {
        super.activateCellBroadcastSms(i, message);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean canDial() {
        return super.canDial();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean disableDataConnectivity() {
        return super.disableDataConnectivity();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void disableLocationUpdates() {
        super.disableLocationUpdates();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean enableDataConnectivity() {
        return super.enableDataConnectivity();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void enableLocationUpdates() {
        super.enableLocationUpdates();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void getAvailableNetworks(Message message) {
        super.getAvailableNetworks(message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void getCallBarring(String str, String str2, Message message, int i) {
        super.getCallBarring(str, str2, message, i);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ boolean getCallForwardingIndicator() {
        return super.getCallForwardingIndicator();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void getCallForwardingOption(int i, Message message) {
        super.getCallForwardingOption(i, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void getCellBroadcastSmsConfig(Message message) {
        super.getCellBroadcastSmsConfig(message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ PhoneInternalInterface.DataActivityState getDataActivityState() {
        return super.getDataActivityState();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ PhoneConstants.DataState getDataConnectionState() {
        return super.getDataConnectionState();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean getDataRoamingEnabled() {
        return super.getDataRoamingEnabled();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getDeviceId() {
        return super.getDeviceId();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getDeviceSvn() {
        return super.getDeviceSvn();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getEsn() {
        return super.getEsn();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getGroupIdLevel1() {
        return super.getGroupIdLevel1();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getGroupIdLevel2() {
        return super.getGroupIdLevel2();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ IccCard getIccCard() {
        return super.getIccCard();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ IccFileHandler getIccFileHandler() {
        return super.getIccFileHandler();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return super.getIccPhoneBookInterfaceManager();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ boolean getIccRecordsLoaded() {
        return super.getIccRecordsLoaded();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ String getIccSerialNumber() {
        return super.getIccSerialNumber();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getImei() {
        return super.getImei();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getLine1AlphaTag() {
        return super.getLine1AlphaTag();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getLine1Number() {
        return super.getLine1Number();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ LinkProperties getLinkProperties(String str) {
        return super.getLinkProperties(str);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public /* bridge */ /* synthetic */ String getMeid() {
        return super.getMeid();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ boolean getMessageWaitingIndicator() {
        return super.getMessageWaitingIndicator();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ List getPendingMmiCodes() {
        return super.getPendingMmiCodes();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ int getPhoneType() {
        return super.getPhoneType();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ SignalStrength getSignalStrength() {
        return super.getSignalStrength();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ PhoneConstants.State getState() {
        return super.getState();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getSubscriberId() {
        return super.getSubscriberId();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getVoiceMailAlphaTag() {
        return super.getVoiceMailAlphaTag();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ String getVoiceMailNumber() {
        return super.getVoiceMailNumber();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean handleInCallMmiCommands(String str) {
        return super.handleInCallMmiCommands(str);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean handlePinMmi(String str) {
        return super.handlePinMmi(str);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean handleUssdRequest(String str, ResultReceiver resultReceiver) {
        return super.handleUssdRequest(str, resultReceiver);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ boolean isDataAllowed(int i) {
        return super.isDataAllowed(i);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean isUserDataEnabled() {
        return super.isUserDataEnabled();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ boolean isVideoEnabled() {
        return super.isVideoEnabled();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ boolean needsOtaServiceProvisioning() {
        return super.needsOtaServiceProvisioning();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void notifyCallForwardingIndicator() {
        super.notifyCallForwardingIndicator();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void registerForRingbackTone(Handler handler, int i, Object obj) {
        super.registerForRingbackTone(handler, i, obj);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void registerForSuppServiceNotification(Handler handler, int i, Object obj) {
        super.registerForSuppServiceNotification(handler, i, obj);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void saveClirSetting(int i) {
        super.saveClirSetting(i);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void selectNetworkManually(OperatorInfo operatorInfo, boolean z, Message message) {
        super.selectNetworkManually(operatorInfo, z, message);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void sendEmergencyCallStateChange(boolean z) {
        super.sendEmergencyCallStateChange(z);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void sendUssdResponse(String str) {
        super.sendUssdResponse(str);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void setBroadcastEmergencyCallStateChanges(boolean z) {
        super.setBroadcastEmergencyCallStateChanges(z);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void setCallBarring(String str, boolean z, String str2, Message message, int i) {
        super.setCallBarring(str, z, str2, message, i);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void setCallForwardingOption(int i, int i2, String str, int i3, Message message) {
        super.setCallForwardingOption(i, i2, str, i3, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void setCellBroadcastSmsConfig(int[] iArr, Message message) {
        super.setCellBroadcastSmsConfig(iArr, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void setDataRoamingEnabled(boolean z) {
        super.setDataRoamingEnabled(z);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ boolean setLine1Number(String str, String str2, Message message) {
        return super.setLine1Number(str, str2, message);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void setNetworkSelectionModeAutomatic(Message message) {
        super.setNetworkSelectionModeAutomatic(message);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void setOnPostDialCharacter(Handler handler, int i, Object obj) {
        super.setOnPostDialCharacter(handler, i, obj);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void setRadioPower(boolean z) {
        super.setRadioPower(z);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void setVoiceMailNumber(String str, String str2, Message message) {
        super.setVoiceMailNumber(str, str2, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void startNetworkScan(NetworkScanRequest networkScanRequest, Message message) {
        super.startNetworkScan(networkScanRequest, message);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void startRingbackTone() {
        super.startRingbackTone();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void stopNetworkScan(Message message) {
        super.stopNetworkScan(message);
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void stopRingbackTone() {
        super.stopRingbackTone();
    }

    @Override // com.android.internal.telephony.sip.SipPhoneBase, com.android.internal.telephony.Phone
    public /* bridge */ /* synthetic */ void unregisterForRingbackTone(Handler handler) {
        super.unregisterForRingbackTone(handler);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void unregisterForSuppServiceNotification(Handler handler) {
        super.unregisterForSuppServiceNotification(handler);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public /* bridge */ /* synthetic */ void updateServiceLocation() {
        super.updateServiceLocation();
    }

    SipPhone(Context context, PhoneNotifier notifier, SipProfile profile) {
        super("SIP:" + profile.getUriString(), context, notifier);
        log("new SipPhone: " + hidePii(profile.getUriString()));
        this.mRingingCall = new SipCall();
        this.mForegroundCall = new SipCall();
        this.mBackgroundCall = new SipCall();
        this.mProfile = profile;
        this.mSipManager = SipManager.newInstance(context);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SipPhone)) {
            return false;
        }
        return this.mProfile.getUriString().equals(((SipPhone) o).mProfile.getUriString());
    }

    public String getSipUri() {
        return this.mProfile.getUriString();
    }

    public boolean equals(SipPhone phone) {
        return getSipUri().equals(phone.getSipUri());
    }

    public Connection takeIncomingCall(Object incomingCall) {
        synchronized (SipPhone.class) {
            if (!(incomingCall instanceof SipAudioCall)) {
                log("takeIncomingCall: ret=null, not a SipAudioCall");
                return null;
            } else if (this.mRingingCall.getState().isAlive()) {
                log("takeIncomingCall: ret=null, ringingCall not alive");
                return null;
            } else if (!this.mForegroundCall.getState().isAlive() || !this.mBackgroundCall.getState().isAlive()) {
                try {
                    SipAudioCall sipAudioCall = (SipAudioCall) incomingCall;
                    log("takeIncomingCall: taking call from: " + hidePii(sipAudioCall.getPeerProfile().getUriString()));
                    if (sipAudioCall.getLocalProfile().getUriString().equals(this.mProfile.getUriString())) {
                        SipConnection connection = this.mRingingCall.initIncomingCall(sipAudioCall, this.mForegroundCall.getState().isAlive());
                        if (sipAudioCall.getState() != 3) {
                            log("    takeIncomingCall: call cancelled !!");
                            this.mRingingCall.reset();
                            connection = null;
                        }
                        return connection;
                    }
                } catch (Exception e) {
                    log("    takeIncomingCall: exception e=" + e);
                    this.mRingingCall.reset();
                }
                log("takeIncomingCall: NOT taking !!");
                return null;
            } else {
                log("takeIncomingCall: ret=null, foreground and background both alive");
                return null;
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void acceptCall(int videoState) throws CallStateException {
        synchronized (SipPhone.class) {
            if (this.mRingingCall.getState() != Call.State.INCOMING) {
                if (this.mRingingCall.getState() != Call.State.WAITING) {
                    log("acceptCall: throw CallStateException(\"phone not ringing\")");
                    throw new CallStateException("phone not ringing");
                }
            }
            log("acceptCall: accepting");
            this.mRingingCall.setMute(false);
            this.mRingingCall.acceptCall();
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void rejectCall() throws CallStateException {
        synchronized (SipPhone.class) {
            if (this.mRingingCall.getState().isRinging()) {
                log("rejectCall: rejecting");
                this.mRingingCall.rejectCall();
            } else {
                log("rejectCall: throw CallStateException(\"phone not ringing\")");
                throw new CallStateException("phone not ringing");
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public Connection dial(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        Connection dialInternal;
        synchronized (SipPhone.class) {
            dialInternal = dialInternal(dialString, dialArgs.videoState);
        }
        return dialInternal;
    }

    private Connection dialInternal(String dialString, int videoState) throws CallStateException {
        log("dialInternal: dialString=" + hidePii(dialString));
        clearDisconnected();
        if (canDial()) {
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                switchHoldingAndActive();
            }
            if (this.mForegroundCall.getState() == Call.State.IDLE) {
                this.mForegroundCall.setMute(false);
                try {
                    return this.mForegroundCall.dial(dialString);
                } catch (SipException e) {
                    loge("dialInternal: ", e);
                    throw new CallStateException("dial error: " + e);
                }
            } else {
                throw new CallStateException("cannot dial in current state");
            }
        } else {
            throw new CallStateException("dialInternal: cannot dial in current state");
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void switchHoldingAndActive() throws CallStateException {
        if (!isHoldTimeoutExpired()) {
            log("switchHoldingAndActive: Disregarded! Under 1000 ms...");
            return;
        }
        log("switchHoldingAndActive: switch fg and bg");
        synchronized (SipPhone.class) {
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            if (this.mBackgroundCall.getState().isAlive()) {
                this.mBackgroundCall.hold();
            }
            if (this.mForegroundCall.getState().isAlive()) {
                this.mForegroundCall.unhold();
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean canConference() {
        log("canConference: ret=true");
        return true;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void conference() throws CallStateException {
        synchronized (SipPhone.class) {
            if (this.mForegroundCall.getState() == Call.State.ACTIVE && this.mForegroundCall.getState() == Call.State.ACTIVE) {
                log("conference: merge fg & bg");
                this.mForegroundCall.merge(this.mBackgroundCall);
            } else {
                throw new CallStateException("wrong state to merge calls: fg=" + this.mForegroundCall.getState() + ", bg=" + this.mBackgroundCall.getState());
            }
        }
    }

    public void conference(Call that) throws CallStateException {
        synchronized (SipPhone.class) {
            if (that instanceof SipCall) {
                this.mForegroundCall.merge((SipCall) that);
            } else {
                throw new CallStateException("expect " + SipCall.class + ", cannot merge with " + that.getClass());
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean canTransfer() {
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void explicitCallTransfer() {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void clearDisconnected() {
        synchronized (SipPhone.class) {
            this.mRingingCall.clearDisconnected();
            this.mForegroundCall.clearDisconnected();
            this.mBackgroundCall.clearDisconnected();
            updatePhoneState();
            notifyPreciseCallStateChanged();
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mForegroundCall.getState().isAlive()) {
            synchronized (SipPhone.class) {
                this.mForegroundCall.sendDtmf(c);
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("startDtmf called with invalid character '" + c + "'");
            return;
        }
        sendDtmf(c);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopDtmf() {
    }

    public void sendBurstDtmf(String dtmfString) {
        loge("sendBurstDtmf() is a CDMA method");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public void getOutgoingCallerIdDisplay(Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public void getCallWaiting(Message onComplete) {
        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
        onComplete.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public void setCallWaiting(boolean enable, Message onComplete) {
        loge("call waiting not supported");
    }

    @Override // com.android.internal.telephony.Phone
    public void setEchoSuppressionEnabled() {
        synchronized (SipPhone.class) {
            if (((AudioManager) this.mContext.getSystemService("audio")).getParameters("ec_supported").contains("off")) {
                this.mForegroundCall.setAudioGroupMode();
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setMute(boolean muted) {
        synchronized (SipPhone.class) {
            this.mForegroundCall.setMute(muted);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean getMute() {
        if (this.mForegroundCall.getState().isAlive()) {
            return this.mForegroundCall.getMute();
        }
        return this.mBackgroundCall.getMute();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public Call getForegroundCall() {
        return this.mForegroundCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public Call getBackgroundCall() {
        return this.mBackgroundCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public Call getRingingCall() {
        return this.mRingingCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.sip.SipPhoneBase
    public ServiceState getServiceState() {
        return super.getServiceState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getUriString(SipProfile p) {
        return p.getUserName() + "@" + getSipDomain(p);
    }

    private String getSipDomain(SipProfile p) {
        String domain = p.getSipDomain();
        if (domain.endsWith(":5060")) {
            return domain.substring(0, domain.length() - 5);
        }
        return domain;
    }

    /* access modifiers changed from: private */
    public static Call.State getCallStateFrom(SipAudioCall sipAudioCall) {
        if (sipAudioCall.isOnHold()) {
            return Call.State.HOLDING;
        }
        int sessionState = sipAudioCall.getState();
        if (sessionState == 0) {
            return Call.State.IDLE;
        }
        switch (sessionState) {
            case 3:
            case 4:
                return Call.State.INCOMING;
            case 5:
                return Call.State.DIALING;
            case 6:
                return Call.State.ALERTING;
            case 7:
                return Call.State.DISCONNECTING;
            case 8:
                return Call.State.ACTIVE;
            default:
                slog("illegal connection state: " + sessionState);
                return Call.State.DISCONNECTED;
        }
    }

    private synchronized boolean isHoldTimeoutExpired() {
        long currTime = System.currentTimeMillis();
        if (currTime - this.mTimeOfLastValidHoldRequest <= TIMEOUT_HOLD_PROCESSING) {
            return false;
        }
        this.mTimeOfLastValidHoldRequest = currTime;
        return true;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private static void slog(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    private void loge(String s, Exception e) {
        Rlog.e(LOG_TAG, s, e);
    }

    /* access modifiers changed from: private */
    public class SipCall extends SipCallBase {
        private static final boolean SC_DBG = true;
        private static final String SC_TAG = "SipCall";
        private static final boolean SC_VDBG = false;

        private SipCall() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            log("reset");
            this.mConnections.clear();
            setState(Call.State.IDLE);
        }

        /* access modifiers changed from: package-private */
        public void switchWith(SipCall that) {
            log("switchWith");
            synchronized (SipPhone.class) {
                SipCall tmp = new SipCall();
                tmp.takeOver(this);
                takeOver(that);
                that.takeOver(tmp);
            }
        }

        private void takeOver(SipCall that) {
            log("takeOver");
            this.mConnections = that.mConnections;
            this.mState = that.mState;
            Iterator it = this.mConnections.iterator();
            while (it.hasNext()) {
                ((SipConnection) ((Connection) it.next())).changeOwner(this);
            }
        }

        @Override // com.android.internal.telephony.Call
        public Phone getPhone() {
            return SipPhone.this;
        }

        @Override // com.android.internal.telephony.sip.SipCallBase, com.android.internal.telephony.Call
        public List<Connection> getConnections() {
            ArrayList arrayList;
            synchronized (SipPhone.class) {
                arrayList = this.mConnections;
            }
            return arrayList;
        }

        /* access modifiers changed from: package-private */
        public Connection dial(String originalNumber) throws SipException {
            log("dial: num=" + "xxx");
            String calleeSipUri = originalNumber;
            if (!calleeSipUri.contains("@")) {
                String replaceStr = Pattern.quote(SipPhone.this.mProfile.getUserName() + "@");
                String uriString = SipPhone.this.mProfile.getUriString();
                calleeSipUri = uriString.replaceFirst(replaceStr, calleeSipUri + "@");
            }
            try {
                SipConnection c = new SipConnection(this, new SipProfile.Builder(calleeSipUri).build(), originalNumber);
                c.dial();
                this.mConnections.add(c);
                setState(Call.State.DIALING);
                return c;
            } catch (ParseException e) {
                throw new SipException("dial", e);
            }
        }

        @Override // com.android.internal.telephony.Call
        public void hangup() throws CallStateException {
            synchronized (SipPhone.class) {
                if (this.mState.isAlive()) {
                    log("hangup: call " + getState() + ": " + this + " on phone " + getPhone());
                    setState(Call.State.DISCONNECTING);
                    CallStateException excp = null;
                    Iterator it = this.mConnections.iterator();
                    while (it.hasNext()) {
                        try {
                            ((Connection) it.next()).hangup();
                        } catch (CallStateException e) {
                            excp = e;
                        }
                    }
                    if (excp != null) {
                        throw excp;
                    }
                } else {
                    log("hangup: dead call " + getState() + ": " + this + " on phone " + getPhone());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public SipConnection initIncomingCall(SipAudioCall sipAudioCall, boolean makeCallWait) {
            SipConnection c = new SipConnection(SipPhone.this, this, sipAudioCall.getPeerProfile());
            this.mConnections.add(c);
            Call.State newState = makeCallWait ? Call.State.WAITING : Call.State.INCOMING;
            c.initIncomingCall(sipAudioCall, newState);
            setState(newState);
            SipPhone.this.notifyNewRingingConnectionP(c);
            return c;
        }

        /* access modifiers changed from: package-private */
        public void rejectCall() throws CallStateException {
            log("rejectCall:");
            hangup();
        }

        /* access modifiers changed from: package-private */
        public void acceptCall() throws CallStateException {
            log("acceptCall: accepting");
            if (this != SipPhone.this.mRingingCall) {
                throw new CallStateException("acceptCall() in a non-ringing call");
            } else if (this.mConnections.size() == 1) {
                ((SipConnection) this.mConnections.get(0)).acceptCall();
            } else {
                throw new CallStateException("acceptCall() in a conf call");
            }
        }

        private boolean isSpeakerOn() {
            return Boolean.valueOf(((AudioManager) SipPhone.this.mContext.getSystemService("audio")).isSpeakerphoneOn()).booleanValue();
        }

        /* access modifiers changed from: package-private */
        public void setAudioGroupMode() {
            AudioGroup audioGroup = getAudioGroup();
            if (audioGroup == null) {
                log("setAudioGroupMode: audioGroup == null ignore");
                return;
            }
            int mode = audioGroup.getMode();
            if (this.mState == Call.State.HOLDING) {
                audioGroup.setMode(0);
            } else if (getMute()) {
                audioGroup.setMode(1);
            } else if (isSpeakerOn()) {
                audioGroup.setMode(3);
            } else {
                audioGroup.setMode(2);
            }
            log(String.format("setAudioGroupMode change: %d --> %d", Integer.valueOf(mode), Integer.valueOf(audioGroup.getMode())));
        }

        /* access modifiers changed from: package-private */
        public void hold() throws CallStateException {
            log("hold:");
            setState(Call.State.HOLDING);
            Iterator it = this.mConnections.iterator();
            while (it.hasNext()) {
                ((SipConnection) ((Connection) it.next())).hold();
            }
            setAudioGroupMode();
        }

        /* access modifiers changed from: package-private */
        public void unhold() throws CallStateException {
            log("unhold:");
            setState(Call.State.ACTIVE);
            AudioGroup audioGroup = new AudioGroup();
            Iterator it = this.mConnections.iterator();
            while (it.hasNext()) {
                ((SipConnection) ((Connection) it.next())).unhold(audioGroup);
            }
            setAudioGroupMode();
        }

        /* access modifiers changed from: package-private */
        public void setMute(boolean muted) {
            log("setMute: muted=" + muted);
            Iterator it = this.mConnections.iterator();
            while (it.hasNext()) {
                ((SipConnection) ((Connection) it.next())).setMute(muted);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean getMute() {
            boolean ret = false;
            if (!this.mConnections.isEmpty()) {
                ret = ((SipConnection) this.mConnections.get(0)).getMute();
            }
            log("getMute: ret=" + ret);
            return ret;
        }

        /* access modifiers changed from: package-private */
        public void merge(SipCall that) throws CallStateException {
            log("merge:");
            AudioGroup audioGroup = getAudioGroup();
            for (Connection c : (Connection[]) that.mConnections.toArray(new Connection[that.mConnections.size()])) {
                SipConnection conn = (SipConnection) c;
                add(conn);
                if (conn.getState() == Call.State.HOLDING) {
                    conn.unhold(audioGroup);
                }
            }
            that.setState(Call.State.IDLE);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void add(SipConnection conn) {
            log("add:");
            SipCall call = conn.getCall();
            if (call != this) {
                if (call != null) {
                    call.mConnections.remove(conn);
                }
                this.mConnections.add(conn);
                conn.changeOwner(this);
            }
        }

        /* access modifiers changed from: package-private */
        public void sendDtmf(char c) {
            log("sendDtmf: c=" + c);
            AudioGroup audioGroup = getAudioGroup();
            if (audioGroup == null) {
                log("sendDtmf: audioGroup == null, ignore c=" + c);
                return;
            }
            audioGroup.sendDtmf(convertDtmf(c));
        }

        private int convertDtmf(char c) {
            int code = c - '0';
            if (code >= 0 && code <= 9) {
                return code;
            }
            if (c == '#') {
                return 11;
            }
            if (c == '*') {
                return 10;
            }
            switch (c) {
                case 'A':
                    return 12;
                case 'B':
                    return 13;
                case TelephonyProto.RilErrno.RIL_E_INVALID_RESPONSE:
                    return 14;
                case CallFailCause.ACM_LIMIT_EXCEEDED:
                    return 15;
                default:
                    throw new IllegalArgumentException("invalid DTMF char: " + ((int) c));
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.Call
        public void setState(Call.State newState) {
            if (this.mState != newState) {
                log("setState: cur state" + this.mState + " --> " + newState + ": " + this + ": on phone " + getPhone() + " " + this.mConnections.size());
                if (newState == Call.State.ALERTING) {
                    this.mState = newState;
                    SipPhone.this.startRingbackTone();
                } else if (this.mState == Call.State.ALERTING) {
                    SipPhone.this.stopRingbackTone();
                }
                this.mState = newState;
                SipPhone.this.updatePhoneState();
                SipPhone.this.notifyPreciseCallStateChanged();
            }
        }

        /* access modifiers changed from: package-private */
        public void onConnectionStateChanged(SipConnection conn) {
            log("onConnectionStateChanged: conn=" + conn);
            if (this.mState != Call.State.ACTIVE) {
                setState(conn.getState());
            }
        }

        /* access modifiers changed from: package-private */
        public void onConnectionEnded(SipConnection conn) {
            log("onConnectionEnded: conn=" + conn);
            if (this.mState != Call.State.DISCONNECTED) {
                boolean allConnectionsDisconnected = true;
                log("---check connections: " + this.mConnections.size());
                Iterator it = this.mConnections.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Connection c = (Connection) it.next();
                    log("   state=" + c.getState() + ": " + c);
                    if (c.getState() != Call.State.DISCONNECTED) {
                        allConnectionsDisconnected = false;
                        break;
                    }
                }
                if (allConnectionsDisconnected) {
                    setState(Call.State.DISCONNECTED);
                }
            }
            SipPhone.this.notifyDisconnectP(conn);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private AudioGroup getAudioGroup() {
            if (this.mConnections.isEmpty()) {
                return null;
            }
            return ((SipConnection) this.mConnections.get(0)).getAudioGroup();
        }

        private void log(String s) {
            Rlog.d(SC_TAG, s);
        }
    }

    /* access modifiers changed from: private */
    public class SipConnection extends SipConnectionBase {
        private static final boolean SCN_DBG = true;
        private static final String SCN_TAG = "SipConnection";
        private SipAudioCallAdapter mAdapter;
        private boolean mIncoming;
        private String mOriginalNumber;
        private SipCall mOwner;
        private SipProfile mPeer;
        private SipAudioCall mSipAudioCall;
        private Call.State mState;

        public SipConnection(SipCall owner, SipProfile callee, String originalNumber) {
            super(originalNumber);
            this.mState = Call.State.IDLE;
            this.mIncoming = false;
            this.mAdapter = new SipAudioCallAdapter() {
                /* class com.android.internal.telephony.sip.SipPhone.SipConnection.AnonymousClass1 */

                {
                    SipPhone sipPhone = SipPhone.this;
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.sip.SipPhone.SipAudioCallAdapter
                public void onCallEnded(int cause) {
                    String sessionState;
                    if (SipConnection.this.getDisconnectCause() != 3) {
                        SipConnection.this.setDisconnectCause(cause);
                    }
                    synchronized (SipPhone.class) {
                        SipConnection.this.setState(Call.State.DISCONNECTED);
                        SipAudioCall sipAudioCall = SipConnection.this.mSipAudioCall;
                        SipConnection.this.mSipAudioCall = null;
                        if (sipAudioCall == null) {
                            sessionState = PhoneConfigurationManager.SSSS;
                        } else {
                            sessionState = sipAudioCall.getState() + ", ";
                        }
                        SipConnection.this.log("[SipAudioCallAdapter] onCallEnded: " + SipPhone.hidePii(SipConnection.this.mPeer.getUriString()) + ": " + sessionState + "cause: " + SipConnection.this.getDisconnectCause() + ", on phone " + SipConnection.this.getPhone());
                        if (sipAudioCall != null) {
                            sipAudioCall.setListener(null);
                            sipAudioCall.close();
                        }
                        SipConnection.this.mOwner.onConnectionEnded(SipConnection.this);
                    }
                }

                public void onCallEstablished(SipAudioCall call) {
                    onChanged(call);
                    if (SipConnection.this.mState == Call.State.ACTIVE) {
                        call.startAudio();
                    }
                }

                public void onCallHeld(SipAudioCall call) {
                    onChanged(call);
                    if (SipConnection.this.mState == Call.State.HOLDING) {
                        call.startAudio();
                    }
                }

                public void onChanged(SipAudioCall call) {
                    synchronized (SipPhone.class) {
                        Call.State newState = SipPhone.getCallStateFrom(call);
                        if (SipConnection.this.mState != newState) {
                            if (newState == Call.State.INCOMING) {
                                SipConnection.this.setState(SipConnection.this.mOwner.getState());
                            } else {
                                if (SipConnection.this.mOwner == SipPhone.this.mRingingCall) {
                                    if (SipPhone.this.mRingingCall.getState() == Call.State.WAITING) {
                                        try {
                                            SipPhone.this.switchHoldingAndActive();
                                        } catch (CallStateException e) {
                                            onCallEnded(3);
                                            return;
                                        }
                                    }
                                    SipPhone.this.mForegroundCall.switchWith(SipPhone.this.mRingingCall);
                                }
                                SipConnection.this.setState(newState);
                            }
                            SipConnection.this.mOwner.onConnectionStateChanged(SipConnection.this);
                            SipConnection sipConnection = SipConnection.this;
                            sipConnection.log("onChanged: " + SipPhone.hidePii(SipConnection.this.mPeer.getUriString()) + ": " + SipConnection.this.mState + " on phone " + SipConnection.this.getPhone());
                        }
                    }
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.sip.SipPhone.SipAudioCallAdapter
                public void onError(int cause) {
                    SipConnection sipConnection = SipConnection.this;
                    sipConnection.log("onError: " + cause);
                    onCallEnded(cause);
                }
            };
            this.mOwner = owner;
            this.mPeer = callee;
            this.mOriginalNumber = originalNumber;
        }

        public SipConnection(SipPhone sipPhone, SipCall owner, SipProfile callee) {
            this(owner, callee, sipPhone.getUriString(callee));
        }

        @Override // com.android.internal.telephony.Connection
        public String getCnapName() {
            String displayName = this.mPeer.getDisplayName();
            if (TextUtils.isEmpty(displayName)) {
                return null;
            }
            return displayName;
        }

        @Override // com.android.internal.telephony.sip.SipConnectionBase, com.android.internal.telephony.Connection
        public int getNumberPresentation() {
            return 1;
        }

        /* access modifiers changed from: package-private */
        public void initIncomingCall(SipAudioCall sipAudioCall, Call.State newState) {
            setState(newState);
            this.mSipAudioCall = sipAudioCall;
            sipAudioCall.setListener(this.mAdapter);
            this.mIncoming = true;
        }

        /* access modifiers changed from: package-private */
        public void acceptCall() throws CallStateException {
            try {
                this.mSipAudioCall.answerCall(8);
            } catch (SipException e) {
                throw new CallStateException("acceptCall(): " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public void changeOwner(SipCall owner) {
            this.mOwner = owner;
        }

        /* access modifiers changed from: package-private */
        public AudioGroup getAudioGroup() {
            SipAudioCall sipAudioCall = this.mSipAudioCall;
            if (sipAudioCall == null) {
                return null;
            }
            return sipAudioCall.getAudioGroup();
        }

        /* access modifiers changed from: package-private */
        public void dial() throws SipException {
            setState(Call.State.DIALING);
            this.mSipAudioCall = SipPhone.this.mSipManager.makeAudioCall(SipPhone.this.mProfile, this.mPeer, (SipAudioCall.Listener) null, 15);
            this.mSipAudioCall.setListener(this.mAdapter);
        }

        /* access modifiers changed from: package-private */
        public void hold() throws CallStateException {
            setState(Call.State.HOLDING);
            try {
                this.mSipAudioCall.holdCall(15);
            } catch (SipException e) {
                throw new CallStateException("hold(): " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public void unhold(AudioGroup audioGroup) throws CallStateException {
            this.mSipAudioCall.setAudioGroup(audioGroup);
            setState(Call.State.ACTIVE);
            try {
                this.mSipAudioCall.continueCall(15);
            } catch (SipException e) {
                throw new CallStateException("unhold(): " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public void setMute(boolean muted) {
            SipAudioCall sipAudioCall = this.mSipAudioCall;
            if (sipAudioCall != null && muted != sipAudioCall.isMuted()) {
                StringBuilder sb = new StringBuilder();
                sb.append("setState: prev muted=");
                sb.append(!muted);
                sb.append(" new muted=");
                sb.append(muted);
                log(sb.toString());
                this.mSipAudioCall.toggleMute();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean getMute() {
            SipAudioCall sipAudioCall = this.mSipAudioCall;
            if (sipAudioCall == null) {
                return false;
            }
            return sipAudioCall.isMuted();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.sip.SipConnectionBase
        public void setState(Call.State state) {
            if (state != this.mState) {
                super.setState(state);
                this.mState = state;
            }
        }

        @Override // com.android.internal.telephony.Connection
        public Call.State getState() {
            return this.mState;
        }

        @Override // com.android.internal.telephony.Connection
        public boolean isIncoming() {
            return this.mIncoming;
        }

        @Override // com.android.internal.telephony.Connection
        public String getAddress() {
            return this.mOriginalNumber;
        }

        @Override // com.android.internal.telephony.Connection
        public SipCall getCall() {
            return this.mOwner;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.sip.SipConnectionBase
        public Phone getPhone() {
            return this.mOwner.getPhone();
        }

        @Override // com.android.internal.telephony.Connection
        public void hangup() throws CallStateException {
            synchronized (SipPhone.class) {
                log("hangup: conn=" + SipPhone.hidePii(this.mPeer.getUriString()) + ": " + this.mState + ": on phone " + getPhone().getPhoneName());
                if (this.mState.isAlive()) {
                    int i = 3;
                    try {
                        SipAudioCall sipAudioCall = this.mSipAudioCall;
                        if (sipAudioCall != null) {
                            sipAudioCall.setListener(null);
                            sipAudioCall.endCall();
                        }
                        SipAudioCallAdapter sipAudioCallAdapter = this.mAdapter;
                        if (this.mState == Call.State.INCOMING || this.mState == Call.State.WAITING) {
                            i = 16;
                        }
                        sipAudioCallAdapter.onCallEnded(i);
                    } catch (SipException e) {
                        throw new CallStateException("hangup(): " + e);
                    } catch (Throwable th) {
                        SipAudioCallAdapter sipAudioCallAdapter2 = this.mAdapter;
                        if (this.mState == Call.State.INCOMING || this.mState == Call.State.WAITING) {
                            i = 16;
                        }
                        sipAudioCallAdapter2.onCallEnded(i);
                        throw th;
                    }
                }
            }
        }

        @Override // com.android.internal.telephony.Connection
        public void separate() throws CallStateException {
            SipCall call;
            synchronized (SipPhone.class) {
                if (getPhone() == SipPhone.this) {
                    call = (SipCall) SipPhone.this.getBackgroundCall();
                } else {
                    call = (SipCall) SipPhone.this.getForegroundCall();
                }
                if (call.getState() == Call.State.IDLE) {
                    log("separate: conn=" + this.mPeer.getUriString() + " from " + this.mOwner + " back to " + call);
                    Phone originalPhone = getPhone();
                    AudioGroup audioGroup = call.getAudioGroup();
                    call.add(this);
                    this.mSipAudioCall.setAudioGroup(audioGroup);
                    originalPhone.switchHoldingAndActive();
                    SipCall call2 = (SipCall) SipPhone.this.getForegroundCall();
                    this.mSipAudioCall.startAudio();
                    call2.onConnectionStateChanged(this);
                } else {
                    throw new CallStateException("cannot put conn back to a call in non-idle state: " + call.getState());
                }
            }
        }

        @Override // com.android.internal.telephony.Connection
        public void deflect(String number) throws CallStateException {
            throw new CallStateException("deflect is not supported for SipPhone");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void log(String s) {
            Rlog.d(SCN_TAG, s);
        }
    }

    /* access modifiers changed from: private */
    public abstract class SipAudioCallAdapter extends SipAudioCall.Listener {
        private static final boolean SACA_DBG = true;
        private static final String SACA_TAG = "SipAudioCallAdapter";

        /* access modifiers changed from: protected */
        public abstract void onCallEnded(int i);

        /* access modifiers changed from: protected */
        public abstract void onError(int i);

        private SipAudioCallAdapter() {
        }

        public void onCallEnded(SipAudioCall call) {
            int i;
            log("onCallEnded: call=" + call);
            if (call.isInCall()) {
                i = 2;
            } else {
                i = 1;
            }
            onCallEnded(i);
        }

        public void onCallBusy(SipAudioCall call) {
            log("onCallBusy: call=" + call);
            onCallEnded(4);
        }

        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            log("onError: call=" + call + " code=" + SipErrorCode.toString(errorCode) + ": " + errorMessage);
            switch (errorCode) {
                case -12:
                    onError(9);
                    return;
                case -11:
                    onError(11);
                    return;
                case -10:
                    onError(14);
                    return;
                case -9:
                case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_PREF_RADIO_TECH_CHANGED:
                default:
                    onError(36);
                    return;
                case -8:
                    onError(10);
                    return;
                case -7:
                    onError(8);
                    return;
                case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_TETHERED_CALL_ACTIVE:
                    onError(7);
                    return;
                case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_RADIO_POWER_OFF:
                case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_SIGNAL_LOST:
                    onError(13);
                    return;
                case -2:
                    onError(12);
                    return;
            }
        }

        private void log(String s) {
            Rlog.d(SACA_TAG, s);
        }
    }

    public static String hidePii(String s) {
        return "xxxxx";
    }
}
