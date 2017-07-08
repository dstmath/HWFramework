package com.android.internal.telephony.imsphone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telecom.ConferenceParticipant;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsCall.Listener;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsConfigListener.Stub;
import com.android.ims.ImsConnectionStateListener;
import com.android.ims.ImsEcbm;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsMultiEndpoint;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSuppServiceNotification;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.TelephonyEventLog;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ImsPhoneCallTracker extends AbstractImsPhoneCallTracker implements ImsPullCall {
    private static final boolean DBG = true;
    private static final int EVENT_DIAL_PENDINGMO = 20;
    private static final int EVENT_EXIT_ECBM_BEFORE_PENDINGMO = 21;
    private static final int EVENT_HANGUP_PENDINGMO = 18;
    private static final int EVENT_RESUME_BACKGROUND = 19;
    static final String LOG_TAG = "ImsPhoneCallTracker";
    static final int MAX_CONNECTIONS = 7;
    static final int MAX_CONNECTIONS_PER_CALL = 5;
    private static final int TIMEOUT_HANGUP_PENDINGMO = 500;
    private static final boolean VERBOSE_STATE_LOGGING = false;
    private boolean isHwVolte;
    private boolean mAllowEmergencyVideoCalls;
    public ImsPhoneCall mBackgroundCall;
    private ArrayList<ImsCall> mCallExpectedToResume;
    private int mClirMode;
    private ArrayList<ImsPhoneConnection> mConnections;
    private HwCustImsPhoneCallTracker mCust;
    private boolean mDesiredMute;
    private TelephonyEventLog mEventLog;
    public ImsPhoneCall mForegroundCall;
    public ImsPhoneCall mHandoverCall;
    private Listener mImsCallListener;
    private Stub mImsConfigListener;
    private ImsConnectionStateListener mImsConnectionStateListener;
    private boolean[] mImsFeatureEnabled;
    private final String[] mImsFeatureStrings;
    private ImsManager mImsManager;
    private Listener mImsUssdListener;
    private boolean mIsInEmergencyCall;
    private int mOnHoldToneId;
    private boolean mOnHoldToneStarted;
    private int mPendingCallVideoState;
    private Bundle mPendingIntentExtras;
    private ImsPhoneConnection mPendingMO;
    private Message mPendingUssd;
    ImsPhone mPhone;
    private BroadcastReceiver mReceiver;
    public ImsPhoneCall mRingingCall;
    private int mServiceId;
    private SrvccState mSrvccState;
    private State mState;
    private boolean mSwitchingFgAndBgCalls;
    private Object mSyncHold;
    private Object mSyncResume;
    private ImsCall mUssdSession;
    private RegistrantList mVoiceCallEndedRegistrants;
    private RegistrantList mVoiceCallStartedRegistrants;
    private int pendingCallClirMode;
    private boolean pendingCallInEcm;

    public ImsPhoneCallTracker(ImsPhone phone) {
        this.mImsFeatureEnabled = new boolean[]{false, false, false, false, false, false};
        this.mImsFeatureStrings = new String[]{TelephonyEventLog.DATA_KEY_VOLTE, TelephonyEventLog.DATA_KEY_VILTE, TelephonyEventLog.DATA_KEY_VOWIFI, TelephonyEventLog.DATA_KEY_VIWIFI, TelephonyEventLog.DATA_KEY_UTLTE, TelephonyEventLog.DATA_KEY_UTWIFI};
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.android.ims.IMS_INCOMING_CALL")) {
                    ImsPhoneCallTracker.this.log("onReceive : incoming call intent");
                    if (ImsPhoneCallTracker.this.mImsManager != null && ImsPhoneCallTracker.this.mServiceId >= 0) {
                        try {
                            if (intent.getBooleanExtra("android:ussd", false)) {
                                ImsPhoneCallTracker.this.log("onReceive : USSD");
                                ImsPhoneCallTracker.this.mUssdSession = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsUssdListener);
                                if (ImsPhoneCallTracker.this.mUssdSession != null) {
                                    ImsPhoneCallTracker.this.mUssdSession.accept(2);
                                }
                                return;
                            }
                            boolean isUnknown = intent.getBooleanExtra("android:isUnknown", false);
                            ImsPhoneCallTracker.this.log("onReceive : isUnknown = " + isUnknown + " fg = " + ImsPhoneCallTracker.this.mForegroundCall.getState() + " bg = " + ImsPhoneCallTracker.this.mBackgroundCall.getState());
                            ImsCall imsCall = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsCallListener);
                            ImsPhoneConnection conn = new ImsPhoneConnection(ImsPhoneCallTracker.this.mPhone, imsCall, ImsPhoneCallTracker.this, isUnknown ? ImsPhoneCallTracker.this.mForegroundCall : ImsPhoneCallTracker.this.mRingingCall, isUnknown);
                            ImsPhoneCallTracker.this.addConnection(conn);
                            ImsPhoneCallTracker.this.setVideoCallProvider(conn, imsCall);
                            ImsPhoneCallTracker.this.mEventLog.writeOnImsCallReceive(imsCall.getSession());
                            if (isUnknown) {
                                ImsPhoneCallTracker.this.mPhone.notifyUnknownConnection(conn);
                            } else {
                                if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE)) {
                                    conn.update(imsCall, Call.State.WAITING);
                                }
                                ImsPhoneCallTracker.this.mPhone.notifyNewRingingConnection(conn);
                                ImsPhoneCallTracker.this.mPhone.notifyIncomingRing();
                            }
                            ImsPhoneCallTracker.this.updatePhoneState();
                            ImsPhoneCallTracker.this.mPhone.notifyPreciseCallStateChanged();
                        } catch (ImsException e) {
                            ImsPhoneCallTracker.this.loge("onReceive : exception " + e);
                        } catch (RemoteException e2) {
                        }
                    }
                } else if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    if (subId == ImsPhoneCallTracker.this.mPhone.getSubId()) {
                        ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls = ImsPhoneCallTracker.this.isEmergencyVtCallAllowed(subId);
                        ImsPhoneCallTracker.this.log("onReceive : Updating mAllowEmergencyVideoCalls = " + ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls);
                    }
                }
            }
        };
        this.mConnections = new ArrayList();
        this.mVoiceCallEndedRegistrants = new RegistrantList();
        this.mVoiceCallStartedRegistrants = new RegistrantList();
        this.mRingingCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_RINGING);
        this.mForegroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_FOREGROUND);
        this.mBackgroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_BACKGROUND);
        this.mHandoverCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_HANDOVER);
        this.mClirMode = 0;
        this.mSyncHold = new Object();
        this.mSyncResume = new Object();
        this.isHwVolte = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
        this.mUssdSession = null;
        this.mPendingUssd = null;
        this.mDesiredMute = false;
        this.mOnHoldToneStarted = false;
        this.mOnHoldToneId = -1;
        this.mState = State.IDLE;
        this.mServiceId = -1;
        this.mSrvccState = SrvccState.NONE;
        this.mIsInEmergencyCall = false;
        this.pendingCallInEcm = false;
        this.mSwitchingFgAndBgCalls = false;
        this.mCallExpectedToResume = new ArrayList();
        this.mAllowEmergencyVideoCalls = false;
        this.mImsCallListener = new Listener() {
            public void onCallProgressing(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallProgressing");
                ImsPhoneCallTracker.this.mPendingMO = null;
                Call.State imsPhoneCallState = Call.State.ALERTING;
                if (!(imsCall == null || 2 == imsCall.getState())) {
                    ImsPhoneCallTracker.this.log("DIALING");
                    imsPhoneCallState = Call.State.DIALING;
                }
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, imsPhoneCallState, 0);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallProgressing(imsCall.getCallSession());
            }

            public void onCallStarted(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallStarted");
                ImsPhoneCallTracker.this.mPendingMO = null;
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallStarted(imsCall.getCallSession());
            }

            public void onCallUpdated(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallUpdated");
                if (imsCall != null) {
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                    if (conn != null) {
                        ImsPhoneCallTracker.this.processCallStateChange(imsCall, conn.getCall().mState, 0, ImsPhoneCallTracker.DBG);
                        ImsPhoneCallTracker.this.mEventLog.writeImsCallState(imsCall.getCallSession(), conn.getCall().mState);
                    }
                }
            }

            public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallStartFailed reasonCode=" + reasonInfo.getCode());
                if (ImsPhoneCallTracker.this.mPendingMO != null) {
                    if (reasonInfo.getCode() == PduPart.P_MAC && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.IDLE) {
                        ImsPhoneCallTracker.this.mForegroundCall.detach(ImsPhoneCallTracker.this.mPendingMO);
                        ImsPhoneCallTracker.this.removeConnection(ImsPhoneCallTracker.this.mPendingMO);
                        ImsPhoneCallTracker.this.mPendingMO.finalize();
                        ImsPhoneCallTracker.this.mPendingMO = null;
                        ImsPhoneCallTracker.this.mPhone.initiateSilentRedial();
                        return;
                    }
                    ImsPhoneCallTracker.this.mPendingMO = null;
                    int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo);
                    ImsPhoneCallTracker.this.mPendingMO = null;
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
                    ImsPhoneCallTracker.this.mEventLog.writeOnImsCallStartFailed(imsCall.getCallSession(), reasonInfo);
                }
            }

            public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallTerminated reasonCode=" + reasonInfo.getCode());
                Call.State oldState = ImsPhoneCallTracker.this.mForegroundCall.getState();
                int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo);
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                ImsPhoneCallTracker.this.log("cause = " + cause + " conn = " + conn);
                if (ImsPhoneCallTracker.this.mOnHoldToneId == System.identityHashCode(conn)) {
                    if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                        ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    }
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                    ImsPhoneCallTracker.this.mOnHoldToneId = -1;
                }
                if (conn != null && conn.equals(ImsPhoneCallTracker.this.mPendingMO)) {
                    ImsPhoneCallTracker.this.log("mPendingMO == conn");
                    ImsPhoneCallTracker.this.mPendingMO = null;
                }
                if (conn != null && conn.isIncoming() && conn.getConnectTime() == 0) {
                    if (cause == 2) {
                        cause = 1;
                    } else if (cause == 3) {
                        cause = 16;
                    }
                    ImsPhoneCallTracker.this.log("Incoming connection of 0 connect time detected - translated cause = " + cause);
                }
                if (cause == 2 && conn != null && conn.getImsCall() != null && conn.getImsCall().isMerged()) {
                    cause = 45;
                }
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallTerminated(imsCall.getCallSession(), reasonInfo);
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
                if (ImsPhoneCallTracker.this.mForegroundCall.getState() != Call.State.ACTIVE) {
                    if (ImsPhoneCallTracker.this.mRingingCall.getState().isRinging()) {
                        ImsPhoneCallTracker.this.mPendingMO = null;
                    } else if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(ImsPhoneCallTracker.EVENT_DIAL_PENDINGMO);
                    }
                }
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                    ImsPhoneCallTracker.this.log("onCallTerminated: Call terminated in the midst of Switching Fg and Bg calls.");
                    if (ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                        ImsPhoneCallTracker.this.log("onCallTerminated: switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.IDLE || ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.HOLDING)) {
                            ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        }
                    }
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(ImsPhoneCallTracker.EVENT_RESUME_BACKGROUND);
                        ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                    }
                }
            }

            public void onCallHeld(ImsCall imsCall) {
                if (ImsPhoneCallTracker.this.mForegroundCall.getImsCall() == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallHeld (fg) " + imsCall);
                } else if (ImsPhoneCallTracker.this.mBackgroundCall.getImsCall() == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallHeld (bg) " + imsCall);
                }
                synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                    Call.State oldState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.HOLDING, 0);
                    if (oldState == Call.State.ACTIVE) {
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(ImsPhoneCallTracker.EVENT_RESUME_BACKGROUND);
                        } else {
                            if (ImsPhoneCallTracker.this.mPendingMO != null) {
                                ImsPhoneCallTracker.this.dialPendingMO();
                            }
                            ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        }
                    } else if (oldState == Call.State.IDLE && ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(ImsPhoneCallTracker.EVENT_RESUME_BACKGROUND);
                        ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                    }
                }
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallHeld(imsCall.getCallSession());
            }

            public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallHoldFailed reasonCode=" + reasonInfo.getCode());
                synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                    Call.State bgState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                    if (reasonInfo.getCode() == PduPart.P_MODIFICATION_DATE) {
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.dialPendingMO();
                        }
                    } else if (bgState == Call.State.ACTIVE) {
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.mPendingMO.setDisconnectCause(36);
                            ImsPhoneCallTracker.this.sendEmptyMessageDelayed(ImsPhoneCallTracker.EVENT_HANGUP_PENDINGMO, 500);
                        }
                    }
                    ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.HOLD);
                }
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallHoldFailed(imsCall.getCallSession(), reasonInfo);
            }

            public void onCallResumed(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallResumed");
                if (ImsPhoneCallTracker.this.isHwVolte) {
                    hwOnCallResumed(imsCall);
                } else if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && !ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                    ImsPhoneCallTracker.this.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                }
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallResumed(imsCall.getCallSession());
            }

            private void hwOnCallResumed(ImsCall imsCall) {
                if (imsCall == null) {
                    ImsPhoneCallTracker.this.loge("imsCall is null, resume failed");
                    return;
                }
                synchronized (ImsPhoneCallTracker.this.mSyncResume) {
                    if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                        if (ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                            ImsPhoneCallTracker.this.mCallExpectedToResume.remove(imsCall);
                        } else {
                            ImsPhoneCallTracker.this.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                            ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                            ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                            ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                        }
                        if (ImsPhoneCallTracker.this.mCallExpectedToResume.isEmpty()) {
                            ImsPhoneCallTracker.this.log("mCallExpectedToResume cleared, reset mSwitchingFgAndBgCalls to false");
                            ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        }
                    }
                }
            }

            public void onCallResumeFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && !ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                    ImsPhoneCallTracker.this.log("onCallResumeFailed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                }
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.RESUME);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallResumeFailed(imsCall.getCallSession(), reasonInfo);
            }

            public void onCallResumeReceived(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallResumeReceived");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                }
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = 1;
                supp.code = 3;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallResumeReceived(imsCall.getCallSession());
            }

            public void onCallHoldReceived(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallHoldReceived");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null && conn.getState() == Call.State.ACTIVE && !ImsPhoneCallTracker.this.mOnHoldToneStarted && ImsPhoneCall.isLocalTone(imsCall)) {
                    ImsPhoneCallTracker.this.mPhone.startOnHoldTone(conn);
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = ImsPhoneCallTracker.DBG;
                    ImsPhoneCallTracker.this.mOnHoldToneId = System.identityHashCode(conn);
                }
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = 1;
                supp.code = 2;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallHoldReceived(imsCall.getCallSession());
            }

            public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
                ImsPhoneCallTracker.this.log("onCallSuppServiceReceived: suppServiceInfo=" + suppServiceInfo);
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = suppServiceInfo.notificationType;
                supp.code = suppServiceInfo.code;
                supp.index = suppServiceInfo.index;
                supp.number = suppServiceInfo.number;
                supp.history = suppServiceInfo.history;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            }

            public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
                ImsPhoneCall imsPhoneCall;
                ImsPhoneCallTracker.this.log("onCallMerged");
                ImsPhoneCall foregroundImsPhoneCall = ImsPhoneCallTracker.this.findConnection(call).getCall();
                ImsPhoneConnection peerConnection = ImsPhoneCallTracker.this.findConnection(peerCall);
                if (peerConnection == null) {
                    imsPhoneCall = null;
                } else {
                    imsPhoneCall = peerConnection.getCall();
                }
                if (swapCalls) {
                    ImsPhoneCallTracker.this.switchAfterConferenceSuccess();
                }
                foregroundImsPhoneCall.merge(imsPhoneCall, Call.State.ACTIVE);
                try {
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                    ImsPhoneCallTracker.this.log("onCallMerged: ImsPhoneConnection=" + conn);
                    ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                    ImsPhoneCallTracker.this.setVideoCallProvider(conn, call);
                    ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                } catch (Exception e) {
                    ImsPhoneCallTracker.this.loge("onCallMerged: exception " + e);
                }
                ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mForegroundCall.getImsCall(), Call.State.ACTIVE, 0);
                if (peerConnection != null) {
                    ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mBackgroundCall.getImsCall(), Call.State.HOLDING, 0);
                }
                if (call.isMergeRequestedByConf()) {
                    ImsPhoneCallTracker.this.log("onCallMerged :: Merge requested by existing conference.");
                    call.resetIsMergeRequestedByConf(false);
                } else {
                    ImsPhoneCallTracker.this.log("onCallMerged :: calling onMultipartyStateChanged()");
                    onMultipartyStateChanged(call, ImsPhoneCallTracker.DBG);
                }
                ImsPhoneCallTracker.this.logState();
            }

            public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallMergeFailed reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.CONFERENCE);
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.onConferenceMergeFailed();
                }
            }

            public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> participants) {
                ImsPhoneCallTracker.this.log("onConferenceParticipantsStateChanged");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.updateConferenceParticipants(participants);
                }
            }

            public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
                ImsPhoneCallTracker.this.mPhone.onTtyModeReceived(mode);
            }

            public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallHandover(imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            }

            public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallHandoverFailed :: srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsCallHandoverFailed(imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            }

            public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
                ImsPhoneCallTracker.this.log("onMultipartyStateChanged to " + (isMultiParty ? "Y" : "N"));
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.updateMultipartyState(isMultiParty);
                }
            }
        };
        this.mImsUssdListener = new Listener() {
            public void onCallStarted(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallStarted");
                if (imsCall == ImsPhoneCallTracker.this.mUssdSession && ImsPhoneCallTracker.this.mPendingUssd != null) {
                    AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd);
                    ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                    ImsPhoneCallTracker.this.mPendingUssd = null;
                }
            }

            public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallStartFailed reasonCode=" + reasonInfo.getCode());
                onCallTerminated(imsCall, reasonInfo);
            }

            public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallTerminated reasonCode=" + reasonInfo.getCode());
                if (imsCall == ImsPhoneCallTracker.this.mUssdSession) {
                    ImsPhoneCallTracker.this.mUssdSession = null;
                    if (ImsPhoneCallTracker.this.mPendingUssd != null) {
                        AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd, null, new CommandException(Error.GENERIC_FAILURE));
                        ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                        ImsPhoneCallTracker.this.mPendingUssd = null;
                    }
                }
                imsCall.close();
            }

            public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallUssdMessageReceived mode=" + mode);
                int ussdMode = -1;
                switch (mode) {
                    case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                        ussdMode = 0;
                        break;
                    case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                        ussdMode = 1;
                        break;
                }
                ImsPhoneCallTracker.this.mPhone.onIncomingUSSD(ussdMode, ussdMessage);
            }
        };
        this.mImsConnectionStateListener = new ImsConnectionStateListener() {
            public void onImsConnected() {
                ImsPhoneCallTracker.this.log("onImsConnected");
                ImsPhoneCallTracker.this.mPhone.setServiceState(0);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(ImsPhoneCallTracker.DBG);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsConnectionState(1, null);
            }

            public void onImsDisconnected(ImsReasonInfo imsReasonInfo) {
                ImsPhoneCallTracker.this.log("onImsDisconnected imsReasonInfo=" + imsReasonInfo);
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
                ImsPhoneCallTracker.this.mPhone.processDisconnectReason(imsReasonInfo);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsConnectionState(3, imsReasonInfo);
            }

            public void onImsProgressing() {
                ImsPhoneCallTracker.this.log("onImsProgressing");
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsConnectionState(2, null);
            }

            public void onImsResumed() {
                ImsPhoneCallTracker.this.log("onImsResumed");
                ImsPhoneCallTracker.this.mPhone.setServiceState(0);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsConnectionState(4, null);
            }

            public void onImsSuspended() {
                ImsPhoneCallTracker.this.log("onImsSuspended");
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mEventLog.writeOnImsConnectionState(ImsPhoneCallTracker.MAX_CONNECTIONS_PER_CALL, null);
            }

            public void onFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
                if (serviceClass == 1) {
                    boolean tmpIsVideoCallEnabled = ImsPhoneCallTracker.this.isVideoCallEnabled();
                    StringBuilder sb = new StringBuilder(AbstractPhoneBase.BUFFER_SIZE);
                    sb.append("onFeatureCapabilityChanged: ");
                    int i = 0;
                    while (i <= ImsPhoneCallTracker.MAX_CONNECTIONS_PER_CALL && i < enabledFeatures.length) {
                        if (enabledFeatures[i] == i) {
                            sb.append(ImsPhoneCallTracker.this.mImsFeatureStrings[i]);
                            sb.append(":true ");
                            ImsPhoneCallTracker.this.mImsFeatureEnabled[i] = ImsPhoneCallTracker.DBG;
                        } else if (enabledFeatures[i] == -1) {
                            sb.append(ImsPhoneCallTracker.this.mImsFeatureStrings[i]);
                            sb.append(":false ");
                            ImsPhoneCallTracker.this.mImsFeatureEnabled[i] = false;
                        } else {
                            ImsPhoneCallTracker.this.loge("onFeatureCapabilityChanged(" + i + ", " + ImsPhoneCallTracker.this.mImsFeatureStrings[i] + "): unexpectedValue=" + enabledFeatures[i]);
                        }
                        i++;
                    }
                    ImsPhoneCallTracker.this.log(sb.toString());
                    if (tmpIsVideoCallEnabled != ImsPhoneCallTracker.this.isVideoCallEnabled()) {
                        ImsPhoneCallTracker.this.mPhone.notifyForVideoCapabilityChanged(ImsPhoneCallTracker.this.isVideoCallEnabled());
                    }
                    ImsPhoneCallTracker.this.log("onFeatureCapabilityChanged: isVolteEnabled=" + ImsPhoneCallTracker.this.isVolteEnabled() + ", isVideoCallEnabled=" + ImsPhoneCallTracker.this.isVideoCallEnabled() + ", isVowifiEnabled=" + ImsPhoneCallTracker.this.isVowifiEnabled() + ", isUtEnabled=" + ImsPhoneCallTracker.this.isUtEnabled());
                    for (ImsPhoneConnection connection : ImsPhoneCallTracker.this.mConnections) {
                        connection.updateWifiState();
                    }
                    ImsPhoneCallTracker.this.mPhone.onFeatureCapabilityChanged();
                    ImsPhoneCallTracker.this.mEventLog.writeOnImsCapabilities(ImsPhoneCallTracker.this.mImsFeatureEnabled);
                }
            }

            public void onVoiceMessageCountChanged(int count) {
                ImsPhoneCallTracker.this.log("onVoiceMessageCountChanged :: count=" + count);
                ImsPhoneCallTracker.this.mPhone.mDefaultPhone.setVoiceMessageCount(count);
            }
        };
        this.mImsConfigListener = new Stub() {
            public void onGetFeatureResponse(int feature, int network, int value, int status) {
            }

            public void onSetFeatureResponse(int feature, int network, int value, int status) {
                ImsPhoneCallTracker.this.mEventLog.writeImsSetFeatureValue(feature, network, value, status);
            }

            public void onGetVideoQuality(int status, int quality) {
            }

            public void onSetVideoQuality(int status) {
            }
        };
        this.mPhone = phone;
        this.mEventLog = new TelephonyEventLog(this.mPhone.getPhoneId());
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.ims.IMS_INCOMING_CALL");
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mReceiver, intentfilter);
        this.mAllowEmergencyVideoCalls = isEmergencyVtCallAllowed(this.mPhone.getSubId());
        this.mCust = (HwCustImsPhoneCallTracker) HwCustUtils.createObj(HwCustImsPhoneCallTracker.class, new Object[]{this.mPhone.getContext()});
        new Thread() {
            public void run() {
                ImsPhoneCallTracker.this.getImsService();
            }
        }.start();
    }

    private PendingIntent createIncomingCallPendingIntent() {
        Intent intent = new Intent("com.android.ims.IMS_INCOMING_CALL");
        intent.addFlags(268435456);
        return PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
    }

    private void getImsService() {
        log("getImsService");
        this.mImsManager = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId());
        try {
            this.mServiceId = this.mImsManager.open(1, createIncomingCallPendingIntent(), this.mImsConnectionStateListener);
            this.mImsManager.setImsConfigListener(this.mImsConfigListener);
            getEcbmInterface().setEcbmStateListener(this.mPhone.getImsEcbmStateListener());
            if (this.mPhone.isInEcm()) {
                this.mPhone.exitEmergencyCallbackMode();
            }
            this.mImsManager.setUiTTYMode(this.mPhone.getContext(), this.mServiceId, Secure.getInt(this.mPhone.getContext().getContentResolver(), "preferred_tty_mode", 0), null);
        } catch (ImsException e) {
            loge("getImsService: " + e);
            this.mImsManager = null;
        }
    }

    public void dispose() {
        log("dispose");
        this.mRingingCall.dispose();
        this.mBackgroundCall.dispose();
        this.mForegroundCall.dispose();
        this.mHandoverCall.dispose();
        clearDisconnected();
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
        try {
            if (this.mImsManager != null) {
                this.mImsManager.close(this.mServiceId);
            }
        } catch (ImsException e) {
            loge("ImsManager.close : exception " + e);
        }
    }

    protected void finalize() {
        log("ImsPhoneCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        this.mVoiceCallStartedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public Connection dial(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        return dial(dialString, PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getInt(Phone.CLIR_KEY, 0), videoState, intentExtras);
    }

    synchronized Connection dial(String dialString, int clirMode, int videoState, Bundle intentExtras) throws CallStateException {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(dialString);
        log("dial clirMode=" + clirMode);
        clearDisconnected();
        if (this.mImsManager == null) {
            throw new CallStateException("service not available");
        } else if (canDial()) {
            if (isPhoneInEcmMode && isEmergencyNumber) {
                handleEcmTimer(1);
            }
            if (isEmergencyNumber && VideoProfile.isVideo(videoState) && !this.mAllowEmergencyVideoCalls) {
                loge("dial: carrier does not support video emergency calls; downgrade to audio-only");
                videoState = 0;
            }
            boolean holdBeforeDial = false;
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                if (this.mBackgroundCall.getState() != Call.State.IDLE) {
                    throw new CallStateException("cannot dial in current state");
                }
                holdBeforeDial = DBG;
                this.mPendingCallVideoState = videoState;
                this.mPendingIntentExtras = intentExtras;
                switchWaitingOrHoldingAndActive();
            }
            Call.State fgState = Call.State.IDLE;
            Call.State bgState = Call.State.IDLE;
            this.mClirMode = clirMode;
            synchronized (this.mSyncHold) {
                if (holdBeforeDial) {
                    fgState = this.mForegroundCall.getState();
                    bgState = this.mBackgroundCall.getState();
                    if (fgState == Call.State.ACTIVE) {
                        throw new CallStateException("cannot dial in current state");
                    } else if (bgState == Call.State.HOLDING) {
                        holdBeforeDial = false;
                    }
                }
                this.mPendingMO = new ImsPhoneConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyNumber);
                this.mPendingMO.setVideoState(videoState);
            }
            addConnection(this.mPendingMO);
            if (!holdBeforeDial) {
                if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
                    dialInternal(this.mPendingMO, clirMode, videoState, intentExtras);
                } else {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = clirMode;
                        this.mPendingCallVideoState = videoState;
                        this.pendingCallInEcm = DBG;
                    } catch (ImsException e) {
                        e.printStackTrace();
                        throw new CallStateException("service not available");
                    }
                }
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    private boolean isEmergencyVtCallAllowed(int subId) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null) {
            loge("isEmergencyVideoCallsSupported: No carrier config service found.");
            return false;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(subId);
        if (carrierConfig != null) {
            return carrierConfig.getBoolean("allow_emergency_video_calls_bool");
        }
        loge("isEmergencyVideoCallsSupported: Empty carrier config.");
        return false;
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
            default:
                log("handleEcmTimer, unsupported action " + action);
        }
    }

    private void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        if (conn != null) {
            if (conn.getAddress() == null || conn.getAddress().length() == 0 || conn.getAddress().indexOf(78) >= 0) {
                conn.setDisconnectCause(MAX_CONNECTIONS);
                sendEmptyMessageDelayed(EVENT_HANGUP_PENDINGMO, 500);
                return;
            }
            int serviceType = PhoneNumberUtils.isEmergencyNumber(conn.getAddress()) ? 2 : 1;
            int callType = ImsCallProfile.getCallTypeFromVideoState(videoState);
            conn.setVideoState(videoState);
            try {
                String[] callees = new String[]{conn.getAddress()};
                ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, serviceType, callType);
                profile.setCallExtraInt("oir", clirMode);
                if (intentExtras != null) {
                    if (intentExtras.containsKey("android.telecom.extra.CALL_SUBJECT")) {
                        intentExtras.putString("DisplayText", cleanseInstantLetteringMessage(intentExtras.getString("android.telecom.extra.CALL_SUBJECT")));
                    }
                    profile.mCallExtras.putBundle("OemCallExtras", intentExtras);
                }
                ImsCall imsCall = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsCallListener);
                conn.setImsCall(imsCall);
                setMute(false);
                this.mEventLog.writeOnImsCallStart(imsCall.getSession(), callees[0]);
                setVideoCallProvider(conn, imsCall);
            } catch (ImsException e) {
                loge("dialInternal : " + e);
                conn.setDisconnectCause(36);
                sendEmptyMessageDelayed(EVENT_HANGUP_PENDINGMO, 500);
            } catch (RemoteException e2) {
            }
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
        log("acceptCall");
        if (this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException("cannot accept call");
        }
        sendAnswerResultCheckMessage();
        if (this.mRingingCall.getState() == Call.State.WAITING && this.mForegroundCall.getState().isAlive()) {
            setMute(false);
            this.mPendingCallVideoState = videoState;
            switchWaitingOrHoldingAndActive();
        } else if (this.mRingingCall.getState().isRinging()) {
            log("acceptCall: incoming...");
            setMute(false);
            try {
                ImsCall imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                    this.mEventLog.writeOnImsCallAccept(imsCall.getSession());
                    return;
                }
                throw new CallStateException("no valid ims call");
            } catch (ImsException e) {
                throw new CallStateException("cannot accept call");
            }
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    public void rejectCall() throws CallStateException {
        log("rejectCall");
        if (this.mRingingCall.getState().isRinging()) {
            hangup(this.mRingingCall);
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void switchAfterConferenceSuccess() {
        log("switchAfterConferenceSuccess fg =" + this.mForegroundCall.getState() + ", bg = " + this.mBackgroundCall.getState());
        if (this.mForegroundCall.getState() == Call.State.IDLE && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            log("switchAfterConferenceSuccess");
            this.mForegroundCall.switchWith(this.mBackgroundCall);
        }
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        log("switchWaitingOrHoldingAndActive");
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
            ImsCall imsCall = this.mForegroundCall.getImsCall();
            if (imsCall == null) {
                throw new CallStateException("no ims call");
            }
            this.mSwitchingFgAndBgCalls = DBG;
            for (Connection c : this.mBackgroundCall.getConnections()) {
                if (c != null) {
                    this.mCallExpectedToResume.add(((ImsPhoneConnection) c).getImsCall());
                }
            }
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            try {
                imsCall.hold();
                this.mEventLog.writeOnImsCallHold(imsCall.getSession());
                if (this.mCallExpectedToResume.isEmpty()) {
                    this.mSwitchingFgAndBgCalls = false;
                }
            } catch (ImsException e) {
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                throw new CallStateException(e.getMessage());
            }
        } else if (this.mBackgroundCall.getState() == Call.State.HOLDING) {
            resumeWaitingOrHolding();
        }
    }

    public void conference() {
        log("conference");
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("conference no foreground ims call");
            return;
        }
        ImsCall bgImsCall = this.mBackgroundCall.getImsCall();
        if (bgImsCall == null) {
            log("conference no background ims call");
            return;
        }
        long conferenceConnectTime;
        long foregroundConnectTime = this.mForegroundCall.getEarliestConnectTime();
        long backgroundConnectTime = this.mBackgroundCall.getEarliestConnectTime();
        if (foregroundConnectTime > 0 && backgroundConnectTime > 0) {
            conferenceConnectTime = Math.min(this.mForegroundCall.getEarliestConnectTime(), this.mBackgroundCall.getEarliestConnectTime());
            log("conference - using connect time = " + conferenceConnectTime);
        } else if (foregroundConnectTime > 0) {
            log("conference - bg call connect time is 0; using fg = " + foregroundConnectTime);
            conferenceConnectTime = foregroundConnectTime;
        } else {
            log("conference - fg call connect time is 0; using bg = " + backgroundConnectTime);
            conferenceConnectTime = backgroundConnectTime;
        }
        ImsPhoneConnection foregroundConnection = this.mForegroundCall.getFirstConnection();
        if (foregroundConnection != null) {
            foregroundConnection.setConferenceConnectTime(conferenceConnectTime);
        }
        try {
            fgImsCall.merge(bgImsCall);
        } catch (ImsException e) {
            log("conference " + e.getMessage());
        }
    }

    public void explicitCallTransfer() {
    }

    public void clearDisconnected() {
        log("clearDisconnected");
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        if (this.mForegroundCall.getState() != Call.State.ACTIVE || this.mBackgroundCall.getState() != Call.State.HOLDING || this.mBackgroundCall.isFull() || this.mForegroundCall.isFull()) {
            return false;
        }
        return DBG;
    }

    public boolean canDial() {
        boolean z = DBG;
        boolean z2 = false;
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Call.State fgCallState = this.mForegroundCall.getState();
        Call.State bgCallState = this.mBackgroundCall.getState();
        if (this.mPendingMO != null || this.mRingingCall.isRinging() || disableCall.equals("true")) {
            return false;
        }
        if (!(fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED)) {
            if (fgCallState == Call.State.ACTIVE) {
            }
            return z2;
        }
        if (!(bgCallState == Call.State.IDLE || bgCallState == Call.State.DISCONNECTED || bgCallState == Call.State.HOLDING)) {
            z = false;
        }
        z2 = z;
        return z2;
    }

    public boolean canTransfer() {
        if (this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            return DBG;
        }
        return false;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
        this.mHandoverCall.clearDisconnected();
    }

    private void updatePhoneState() {
        State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = State.RINGING;
        } else if (this.mPendingMO == null && this.mForegroundCall.isIdle() && this.mBackgroundCall.isIdle()) {
            this.mState = State.IDLE;
        } else {
            this.mState = State.OFFHOOK;
        }
        if (this.mState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (oldState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        log("updatePhoneState oldState=" + oldState + ", newState=" + this.mState);
        if (this.mState != oldState) {
            this.mPhone.notifyPhoneStateChanged();
            this.mEventLog.writePhoneState(this.mState);
        }
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        int i;
        log("Phone State:" + this.mState);
        log("Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        try {
            this.mImsManager.setUiTTYMode(this.mPhone.getContext(), this.mServiceId, uiTtyMode, onComplete);
        } catch (ImsException e) {
            loge("setTTYMode : " + e);
            this.mPhone.sendErrorResponse(onComplete, e);
        }
    }

    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mForegroundCall.setMute(mute);
        this.mRingingCall.setMute(mute);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void sendDtmf(char c, Message result) {
        log("sendDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.sendDtmf(c, result);
        }
    }

    public void startDtmf(char c) {
        log("startDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.startDtmf(c);
        } else {
            loge("startDtmf : no foreground call");
        }
    }

    public void stopDtmf() {
        log("stopDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.stopDtmf();
        } else {
            loge("stopDtmf : no foreground call");
        }
    }

    public void hangup(ImsPhoneConnection conn) throws CallStateException {
        log("hangup connection");
        if (conn.getOwner() != this) {
            throw new CallStateException("ImsPhoneConnection " + conn + "does not belong to ImsPhoneCallTracker " + this);
        } else if (ImsPhoneFactory.isimsAsNormalCon()) {
            hangupImsConnection(conn);
        } else {
            hangup(conn.getCall());
        }
    }

    public void hangup(ImsPhoneCall call) throws CallStateException {
        log("hangup call");
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections");
        }
        ImsCall imsCall = call.getImsCall();
        boolean rejectCall = false;
        if (call == this.mRingingCall) {
            log("(ringing) hangup incoming");
            rejectCall = DBG;
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
            } else {
                log("(foregnd) hangup foreground");
            }
        } else if (call == this.mBackgroundCall) {
            log("(backgnd) hangup waiting or background");
        } else {
            throw new CallStateException("ImsPhoneCall " + call + "does not belong to ImsPhoneCallTracker " + this);
        }
        call.onHangupLocal();
        try {
            if (ImsPhoneFactory.isimsAsNormalCon()) {
                hangupImsCall(call);
            } else if (imsCall != null) {
                if (rejectCall) {
                    imsCall.reject(504);
                } else {
                    imsCall.terminate(501);
                }
            } else if (this.mPendingMO != null && call == this.mForegroundCall) {
                this.mPendingMO.update(null, Call.State.DISCONNECTED);
                this.mPendingMO.onDisconnect();
                removeConnection(this.mPendingMO);
                this.mPendingMO = null;
                updatePhoneState();
                removeMessages(EVENT_DIAL_PENDINGMO);
            }
            this.mPhone.notifyPreciseCallStateChanged();
        } catch (ImsException e) {
            throw new CallStateException(e.getMessage());
        }
    }

    private void hangupImsCall(ImsPhoneCall call) throws CallStateException {
        log("hangupImsCall ImsPhoneCall");
        for (int i = 0; i < call.getConnections().size(); i++) {
            hangupImsConnection((ImsPhoneConnection) call.getConnections().get(i));
        }
    }

    private void hangupImsConnection(ImsPhoneConnection conn) throws CallStateException {
        log("hangupImsConnection ImsPhoneConnection");
        ImsPhoneCall call = conn.getCall();
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections");
        }
        ImsCall imsCall = conn.getImsCall();
        boolean rejectCall = false;
        if (call == this.mRingingCall) {
            log("(ringing) hangup incoming");
            rejectCall = DBG;
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
            } else {
                log("(foregnd) hangup foreground");
            }
        } else if (call == this.mBackgroundCall) {
            log("(backgnd) hangup waiting or background");
        } else {
            throw new CallStateException("ImsPhoneCall " + call + "does not belong to ImsPhoneCallTracker " + this);
        }
        conn.onHangupLocal();
        log("hangupImsConnection imsCall  :: " + imsCall);
        if (imsCall != null) {
            if (rejectCall) {
                try {
                    imsCall.reject(504);
                    this.mEventLog.writeOnImsCallReject(imsCall.getSession());
                } catch (ImsException e) {
                    throw new CallStateException(e.getMessage());
                }
            }
            imsCall.terminate(501);
            this.mEventLog.writeOnImsCallTerminate(imsCall.getSession());
        } else if (this.mPendingMO != null && call == this.mForegroundCall) {
            this.mPendingMO.update(null, Call.State.DISCONNECTED);
            this.mPendingMO.onDisconnect();
            removeConnection(this.mPendingMO);
            this.mPendingMO = null;
            updatePhoneState();
            removeMessages(EVENT_DIAL_PENDINGMO);
        }
        this.mPhone.notifyPreciseCallStateChanged();
    }

    void hangupConnectionByIndex(ImsPhoneCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            ImsPhoneConnection cn = (ImsPhoneConnection) call.mConnections.get(i);
            if (cn.getImsIndex() == index) {
                hangupImsConnection(cn);
                return;
            }
        }
        throw new CallStateException("no gsm index found");
    }

    void callEndCleanupHandOverCallIfAny() {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("callEndCleanupHandOverCallIfAny, mHandoverCall.mConnections=" + this.mHandoverCall.mConnections);
            this.mHandoverCall.mConnections.clear();
            this.mState = State.IDLE;
        }
    }

    void resumeWaitingOrHolding() throws CallStateException {
        log("resumeWaitingOrHolding");
        try {
            ImsCall imsCall;
            if (this.mForegroundCall.getState().isAlive()) {
                imsCall = this.mForegroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    this.mEventLog.writeOnImsCallResume(imsCall.getSession());
                }
            } else if (this.mRingingCall.getState() == Call.State.WAITING) {
                imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mEventLog.writeOnImsCallAccept(imsCall.getSession());
                }
            } else {
                imsCall = this.mBackgroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    this.mEventLog.writeOnImsCallResume(imsCall.getSession());
                }
            }
        } catch (ImsException e) {
            log("ImsException e = " + e);
        }
    }

    public void sendUSSD(String ussdString, Message response) {
        log("sendUSSD");
        try {
            if (this.mUssdSession != null) {
                this.mUssdSession.sendUssd(ussdString);
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
                return;
            }
            String[] callees = new String[]{ussdString};
            ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, 1, 2);
            profile.setCallExtraInt("dialstring", 2);
            this.mUssdSession = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsUssdListener);
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
        }
    }

    public void cancelUSSD() {
        if (this.mUssdSession != null) {
            try {
                this.mUssdSession.terminate(501);
            } catch (ImsException e) {
            }
        }
    }

    private synchronized ImsPhoneConnection findConnection(ImsCall imsCall) {
        for (ImsPhoneConnection conn : this.mConnections) {
            if (conn.getImsCall() == imsCall) {
                return conn;
            }
        }
        return null;
    }

    private synchronized void removeConnection(ImsPhoneConnection conn) {
        this.mConnections.remove(conn);
        if (this.mIsInEmergencyCall) {
            boolean isEmergencyCallInList = false;
            for (ImsPhoneConnection imsPhoneConnection : this.mConnections) {
                if (imsPhoneConnection != null && imsPhoneConnection.isEmergency()) {
                    isEmergencyCallInList = DBG;
                    break;
                }
            }
            if (!isEmergencyCallInList) {
                this.mIsInEmergencyCall = false;
                this.mPhone.sendEmergencyCallStateChange(false);
            }
        }
    }

    private synchronized void addConnection(ImsPhoneConnection conn) {
        this.mConnections.add(conn);
        if (conn.isEmergency()) {
            this.mIsInEmergencyCall = DBG;
            this.mPhone.sendEmergencyCallStateChange(DBG);
        }
    }

    private void processCallStateChange(ImsCall imsCall, Call.State state, int cause) {
        log("processCallStateChange " + imsCall + " state=" + state + " cause=" + cause);
        processCallStateChange(imsCall, state, cause, false);
    }

    private void processCallStateChange(ImsCall imsCall, Call.State state, int cause, boolean ignoreState) {
        log("processCallStateChange state=" + state + " cause=" + cause + " ignoreState=" + ignoreState);
        if (imsCall != null) {
            ImsPhoneConnection conn = findConnection(imsCall);
            if (conn != null) {
                conn.updateMediaCapabilities(imsCall);
                boolean update = conn.update(imsCall, state);
                conn.updateMediaCapabilities(imsCall);
                if (state == Call.State.DISCONNECTED) {
                    if (conn.onDisconnect(cause)) {
                        update = DBG;
                    }
                    conn.getCall().detach(conn);
                    removeConnection(conn);
                }
                if ((update || ImsPhoneFactory.isimsAsNormalCon()) && conn.getCall() != this.mHandoverCall) {
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                }
            }
        }
    }

    private void maybeSetVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) {
        if (conn.getVideoProvider() == null && imsCall.getCallSession().getVideoCallProvider() != null) {
            try {
                setVideoCallProvider(conn, imsCall);
            } catch (RemoteException e) {
                loge("maybeSetVideoCallProvider: exception " + e);
            }
        }
    }

    private int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        switch (reasonInfo.getCode()) {
            case EVENT_EXIT_ECBM_BEFORE_PENDINGMO /*21*/:
                return EVENT_EXIT_ECBM_BEFORE_PENDINGMO;
            case CharacterSets.UTF_8 /*106*/:
            case 121:
            case 122:
            case 123:
            case 124:
            case PduPart.P_TYPE /*131*/:
            case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
            case PduPart.P_SECURE /*144*/:
                return EVENT_HANGUP_PENDINGMO;
            case CallFailCause.PROTOCOL_ERROR_UNSPECIFIED /*111*/:
            case 112:
                return 17;
            case PduPart.P_DEP_PATH /*143*/:
                return 16;
            case 201:
            case 202:
            case 203:
            case 335:
                return 13;
            case CallFailCause.FDN_BLOCKED /*241*/:
                return EVENT_EXIT_ECBM_BEFORE_PENDINGMO;
            case 321:
            case 331:
            case 332:
            case 340:
            case 361:
            case 362:
                return 12;
            case 333:
            case 352:
            case 354:
                return 9;
            case 337:
            case 341:
                return 8;
            case 338:
                return 4;
            case 363:
                return 92;
            case 364:
                return 93;
            case 501:
                return 3;
            case 510:
                return 2;
            default:
                return 36;
        }
    }

    private boolean isPhoneInEcbMode() {
        return SystemProperties.getBoolean("ril.cdma.inecmmode", false);
    }

    private void dialPendingMO() {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPendingMO.isEmergency();
        if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
            sendEmptyMessage(EVENT_DIAL_PENDINGMO);
        } else {
            sendEmptyMessage(EVENT_EXIT_ECBM_BEFORE_PENDINGMO);
        }
    }

    public ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getSupplementaryServiceConfiguration(this.mServiceId);
        }
        throw new ImsException("no ims manager", 0);
    }

    private void transferHandoverConnections(ImsPhoneCall call) {
        if (call.mConnections != null) {
            for (Connection c : call.mConnections) {
                c.mPreHandoverState = call.mState;
                log("Connection state before handover is " + c.getStateBeforeHandover());
            }
        }
        if (this.mHandoverCall.mConnections == null) {
            this.mHandoverCall.mConnections = call.mConnections;
        } else {
            this.mHandoverCall.mConnections.addAll(call.mConnections);
        }
        if (this.mHandoverCall.mConnections != null) {
            if (call.getImsCall() != null) {
                call.getImsCall().close();
            }
            for (Connection c2 : this.mHandoverCall.mConnections) {
                ((ImsPhoneConnection) c2).changeParent(this.mHandoverCall);
                ((ImsPhoneConnection) c2).releaseWakeLock();
                if (c2.equals(this.mPendingMO)) {
                    log("srvcc mPendingMO == conn");
                    this.mPendingMO = null;
                }
            }
        }
        if (call.getState().isAlive()) {
            log("Call is alive and state is " + call.mState);
            this.mHandoverCall.mState = call.mState;
        }
        call.mConnections.clear();
        call.mState = Call.State.IDLE;
    }

    void notifySrvccState(SrvccState state) {
        log("notifySrvccState state=" + state);
        this.mSrvccState = state;
        if (this.mSrvccState == SrvccState.COMPLETED) {
            transferHandoverConnections(this.mForegroundCall);
            transferHandoverConnections(this.mBackgroundCall);
            transferHandoverConnections(this.mRingingCall);
        }
    }

    public void handleMessage(Message msg) {
        log("handleMessage what=" + msg.what);
        switch (msg.what) {
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                if (this.pendingCallInEcm) {
                    dialInternal(this.mPendingMO, this.pendingCallClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    this.pendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
            case EVENT_HANGUP_PENDINGMO /*18*/:
                if (this.mPendingMO != null) {
                    this.mPendingMO.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                }
                this.mPendingIntentExtras = null;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
            case EVENT_RESUME_BACKGROUND /*19*/:
                try {
                    resumeWaitingOrHolding();
                } catch (CallStateException e) {
                    loge("handleMessage EVENT_RESUME_BACKGROUND exception=" + e);
                }
            case EVENT_DIAL_PENDINGMO /*20*/:
                dialInternal(this.mPendingMO, this.mClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                this.mPendingIntentExtras = null;
            case EVENT_EXIT_ECBM_BEFORE_PENDINGMO /*21*/:
                if (this.mPendingMO != null) {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = this.mClirMode;
                        this.pendingCallInEcm = DBG;
                    } catch (ImsException e2) {
                        e2.printStackTrace();
                        this.mPendingMO.setDisconnectCause(36);
                        sendEmptyMessageDelayed(EVENT_HANGUP_PENDINGMO, 500);
                    }
                }
            default:
        }
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    void logState() {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("ImsPhoneCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mHandoverCall=" + this.mHandoverCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        for (i = 0; i < this.mImsFeatureEnabled.length; i++) {
            pw.println(" " + this.mImsFeatureStrings[i] + ": " + (this.mImsFeatureEnabled[i] ? "enabled" : "disabled"));
        }
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            if (this.mImsManager != null) {
                this.mImsManager.dump(fd, pw, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mConnections != null && this.mConnections.size() > 0) {
            pw.println("mConnections:");
            for (i = 0; i < this.mConnections.size(); i++) {
                pw.println("  [" + i + "]: " + this.mConnections.get(i));
            }
        }
    }

    protected void handlePollCalls(AsyncResult ar) {
    }

    ImsEcbm getEcbmInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getEcbmInterface(this.mServiceId);
        }
        throw new ImsException("no ims manager", 0);
    }

    ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getMultiEndpointInterface(this.mServiceId);
        }
        throw new ImsException("no ims manager", 0);
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public boolean isVolteEnabled() {
        return this.mImsFeatureEnabled[0];
    }

    public boolean isVowifiEnabled() {
        return this.mImsFeatureEnabled[2];
    }

    public boolean isVideoCallEnabled() {
        if (this.mImsFeatureEnabled[1]) {
            return DBG;
        }
        return this.mImsFeatureEnabled[3];
    }

    public State getState() {
        return this.mState;
    }

    private void setVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) throws RemoteException {
        IImsVideoCallProvider imsVideoCallProvider = imsCall.getCallSession().getVideoCallProvider();
        if (imsVideoCallProvider != null) {
            conn.setVideoProvider(new ImsVideoCallProviderWrapper(imsVideoCallProvider));
        }
    }

    public boolean isUtEnabled() {
        if (this.mImsFeatureEnabled[4]) {
            return DBG;
        }
        return this.mImsFeatureEnabled[MAX_CONNECTIONS_PER_CALL];
    }

    private String cleanseInstantLetteringMessage(String callSubject) {
        if (TextUtils.isEmpty(callSubject)) {
            return callSubject;
        }
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configMgr == null) {
            return callSubject;
        }
        PersistableBundle carrierConfig = configMgr.getConfigForSubId(this.mPhone.getSubId());
        if (carrierConfig == null) {
            return callSubject;
        }
        String invalidCharacters = carrierConfig.getString("carrier_instant_lettering_invalid_chars_string");
        if (!TextUtils.isEmpty(invalidCharacters)) {
            callSubject = callSubject.replaceAll(invalidCharacters, "");
        }
        String escapedCharacters = carrierConfig.getString("carrier_instant_lettering_escaped_chars_string");
        if (!TextUtils.isEmpty(escapedCharacters)) {
            callSubject = escapeChars(escapedCharacters, callSubject);
        }
        return callSubject;
    }

    private String escapeChars(String toEscape, String source) {
        StringBuilder escaped = new StringBuilder();
        for (char c : source.toCharArray()) {
            if (toEscape.contains(Character.toString(c))) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public ImsUtInterface getUtInterfaceEx() throws ImsException {
        return getUtInterface();
    }

    public boolean isUtEnabledForQcom() {
        if (this.mImsFeatureEnabled[4] || this.mImsFeatureEnabled[MAX_CONNECTIONS_PER_CALL]) {
            return this.mCust != null ? this.mCust.checkImsRegistered() : DBG;
        } else {
            return false;
        }
    }

    public void pullExternalCall(String number, int videoState) {
        Bundle extras = new Bundle();
        extras.putBoolean("CallPull", DBG);
        try {
            this.mPhone.notifyUnknownConnection(dial(number, videoState, extras));
        } catch (CallStateException e) {
            loge("pullExternalCall failed - " + e);
        }
    }

    public void disableUTForQcom() {
        log("disableUTForQcom");
        this.mImsFeatureEnabled[4] = false;
        this.mImsFeatureEnabled[MAX_CONNECTIONS_PER_CALL] = false;
    }

    public void enableUTForQcom() {
        log("enableUTForQcom");
        this.mImsFeatureEnabled[4] = DBG;
        this.mImsFeatureEnabled[MAX_CONNECTIONS_PER_CALL] = DBG;
    }
}
