package com.android.internal.telephony.imsphone;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.hardware.radio.V1_0.RadioError;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.telecom.Connection.VideoProvider;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
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
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.google.android.mms.pdu.CharacterSets;
import com.huawei.internal.telephony.HwRadarUtils;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import vendor.huawei.hardware.radio.V1_1.RilConstS32;

public class ImsPhoneCallTracker extends AbstractImsPhoneCallTracker implements ImsPullCall {
    private static final int CEILING_SERVICE_RETRY_COUNT = 6;
    private static final boolean DBG = true;
    private static final int EVENT_CHECK_FOR_WIFI_HANDOVER = 25;
    private static final int EVENT_DATA_ENABLED_CHANGED = 23;
    private static final int EVENT_DIAL_PENDINGMO = 20;
    private static final int EVENT_EXIT_ECBM_BEFORE_PENDINGMO = 21;
    private static final int EVENT_GET_IMS_SERVICE = 24;
    private static final int EVENT_HANGUP_PENDINGMO = 18;
    private static final int EVENT_ON_FEATURE_CAPABILITY_CHANGED = 26;
    private static final int EVENT_RESUME_BACKGROUND = 19;
    private static final int EVENT_VT_DATA_USAGE_UPDATE = 22;
    private static final boolean FORCE_VERBOSE_STATE_LOGGING = false;
    private static final int HANDOVER_TO_WIFI_TIMEOUT_MS = 60000;
    private static final int IMS_RETRY_STARTING_TIMEOUT_MS = 500;
    static final String LOG_TAG = "ImsPhoneCallTracker";
    static final int MAX_CONNECTIONS = 7;
    static final int MAX_CONNECTIONS_PER_CALL = 5;
    private static final int ONE_CALL_LEFT_IN_IMS_CONF = 1;
    private static final SparseIntArray PRECISE_CAUSE_MAP = new SparseIntArray();
    private static final int TIMEOUT_HANGUP_PENDINGMO = 500;
    private static final boolean VERBOSE_STATE_LOGGING = Rlog.isLoggable(VERBOSE_STATE_TAG, 2);
    static final String VERBOSE_STATE_TAG = "IPCTState";
    private boolean isHwVolte = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    private boolean mAllowAddCallDuringVideoCall = true;
    private boolean mAllowEmergencyVideoCalls = false;
    public ImsPhoneCall mBackgroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_BACKGROUND);
    private ArrayList<ImsCall> mCallExpectedToResume = new ArrayList();
    private int mClirMode = 0;
    private ArrayList<ImsPhoneConnection> mConnections = new ArrayList();
    private HwCustImsPhoneCallTracker mCust;
    private boolean mDesiredMute = false;
    private boolean mDropVideoCallWhenAnsweringAudioCall = false;
    public ImsPhoneCall mForegroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_FOREGROUND);
    private boolean mGetImsService = false;
    public ImsPhoneCall mHandoverCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_HANDOVER);
    private boolean mIgnoreDataEnabledChangedForVideoCalls = false;
    private Listener mImsCallListener = new Listener() {
        public void onCallProgressing(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallProgressing");
            ImsPhoneCallTracker.this.mPendingMO = null;
            State imsPhoneCallState = State.ALERTING;
            if (!(imsCall == null || 2 == imsCall.getState())) {
                ImsPhoneCallTracker.this.log("DIALING");
                imsPhoneCallState = State.DIALING;
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, imsPhoneCallState, 0);
            if (imsCall != null) {
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallProgressing(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            } else {
                ImsPhoneCallTracker.this.log("imscall is null can't write on progressing");
            }
        }

        public void onCallStarted(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallStarted");
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume.remove(imsCall);
            }
            ImsPhoneCallTracker.this.mPendingMO = null;
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.ACTIVE, 0);
            if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail && (imsCall.isWifiCall() ^ 1) != 0 && imsCall.isVideoCall() && ImsPhoneCallTracker.this.isWifiConnected()) {
                ImsPhoneCallTracker.this.sendMessageDelayed(ImsPhoneCallTracker.this.obtainMessage(25, imsCall), 60000);
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStarted(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallUpdated(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallUpdated");
            if (imsCall != null) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, conn.getCall().mState, 0, true);
                    ImsPhoneCallTracker.this.mMetrics.writeImsCallState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), conn.getCall().mState);
                }
            }
        }

        public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallStartFailed reasonCode=" + reasonInfo.getCode());
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume.remove(imsCall);
            }
            if (ImsPhoneCallTracker.this.mPendingMO != null) {
                if (reasonInfo.getCode() == 146 && ImsPhoneCallTracker.this.mBackgroundCall.getState() == State.IDLE && ImsPhoneCallTracker.this.mRingingCall.getState() == State.IDLE) {
                    ImsPhoneCallTracker.this.mForegroundCall.detach(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker.this.removeConnection(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker.this.mPendingMO.finalize();
                    ImsPhoneCallTracker.this.mPendingMO = null;
                    ImsPhoneCallTracker.this.mPhone.initiateSilentRedial();
                    return;
                }
                ImsPhoneCallTracker.this.mPendingMO = null;
                int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo);
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.setPreciseDisconnectCause(ImsPhoneCallTracker.this.getPreciseDisconnectCauseFromReasonInfo(reasonInfo));
                }
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.DISCONNECTED, cause);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStartFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }
        }

        public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallTerminated reasonCode=" + reasonInfo.getCode());
            int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo);
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            ImsPhoneCallTracker.this.log("cause = " + cause + " conn = " + conn);
            if (conn != null) {
                VideoProvider videoProvider = conn.getVideoProvider();
                if (videoProvider instanceof ImsVideoCallProviderWrapper) {
                    ((ImsVideoCallProviderWrapper) videoProvider).removeImsVideoProviderCallback(conn);
                }
            }
            if (ImsPhoneCallTracker.this.mOnHoldToneId == System.identityHashCode(conn)) {
                if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                }
                ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                ImsPhoneCallTracker.this.mOnHoldToneId = -1;
            }
            if (conn != null) {
                if (conn.equals(ImsPhoneCallTracker.this.mPendingMO)) {
                    ImsPhoneCallTracker.this.log("mPendingMO == conn");
                    ImsPhoneCallTracker.this.mPendingMO = null;
                }
                if (conn.isPulledCall() && ((reasonInfo.getCode() == CharacterSets.UTF_16 || reasonInfo.getCode() == 336 || reasonInfo.getCode() == 332) && ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getExternalCallTracker() != null)) {
                    ImsPhoneCallTracker.this.log("Call pull failed.");
                    conn.onCallPullFailed(ImsPhoneCallTracker.this.mPhone.getExternalCallTracker().getConnectionById(conn.getPulledDialogId()));
                    cause = 0;
                } else if (conn.isIncoming() && conn.getConnectTime() == 0 && cause != 52) {
                    if (cause == 2) {
                        cause = 1;
                    } else if (cause == 3) {
                        cause = 16;
                    }
                    ImsPhoneCallTracker.this.log("Incoming connection of 0 connect time detected - translated cause = " + cause);
                }
            }
            if (cause == 2 && conn != null && conn.getImsCall() != null && conn.getImsCall().isMerged()) {
                cause = 45;
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallTerminated(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            if (conn != null) {
                conn.setPreciseDisconnectCause(ImsPhoneCallTracker.this.getPreciseDisconnectCauseFromReasonInfo(reasonInfo));
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.DISCONNECTED, cause);
            if (!(!SystemProperties.getBoolean("ro.config.hw_add_sip_error_pop", false) || ImsPhoneCallTracker.this.mCust == null || ImsPhoneCallTracker.this.mPhone == null)) {
                ImsPhoneCallTracker.this.mCust.addSipErrorPopup(reasonInfo, ImsPhoneCallTracker.this.mPhone.getContext());
            }
            if (ImsPhoneCallTracker.this.mForegroundCall.getState() != State.ACTIVE) {
                if (ImsPhoneCallTracker.this.mRingingCall.getState().isRinging()) {
                    ImsPhoneCallTracker.this.mPendingMO = null;
                } else if (ImsPhoneCallTracker.this.mPendingMO != null) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(20);
                }
            }
            if (ImsPhoneCallTracker.this.mCust != null) {
                ImsPhoneCallTracker.this.mCust.handleCallDropErrors(reasonInfo);
            }
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                String str;
                ImsPhoneCallTracker.this.log("onCallTerminated: Call terminated in the midst of Switching Fg and Bg calls.");
                if (ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                    ImsPhoneCallTracker.this.log("onCallTerminated: switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    if (!((ImsPhoneCallTracker.this.mForegroundCall.getState() == State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == State.HOLDING) || (ImsPhoneCallTracker.this.mForegroundCall.getState() == State.ACTIVE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == State.IDLE))) {
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    }
                }
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                StringBuilder append = new StringBuilder().append("onCallTerminated: foreground call in state ").append(ImsPhoneCallTracker.this.mForegroundCall.getState()).append(" and ringing call in state ");
                if (ImsPhoneCallTracker.this.mRingingCall == null) {
                    str = "null";
                } else {
                    str = ImsPhoneCallTracker.this.mRingingCall.getState().toString();
                }
                imsPhoneCallTracker.log(append.append(str).toString());
                if (ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == State.WAITING) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                }
            }
            if (ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect) {
                HwFrameworkFactory.updateImsServiceConfig(ImsPhoneCallTracker.this.mPhone.getContext(), ImsPhoneCallTracker.this.mPhone.getPhoneId(), true);
                ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect = false;
            }
        }

        public void onCallHeld(ImsCall imsCall) {
            if (ImsPhoneCallTracker.this.mForegroundCall.getImsCall() == imsCall) {
                ImsPhoneCallTracker.this.log("onCallHeld (fg) " + imsCall);
            } else if (ImsPhoneCallTracker.this.mBackgroundCall.getImsCall() == imsCall) {
                ImsPhoneCallTracker.this.log("onCallHeld (bg) " + imsCall);
            }
            synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                State oldState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.HOLDING, 0);
                if (oldState == State.ACTIVE) {
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == State.WAITING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    } else {
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.dialPendingMO();
                        }
                        ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    }
                } else if (oldState == State.IDLE && ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                }
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHeld(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallHoldFailed reasonCode=" + reasonInfo.getCode());
            synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                State bgState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                if (reasonInfo.getCode() == 148) {
                    if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.dialPendingMO();
                    }
                } else if (bgState == State.ACTIVE) {
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.mPendingMO.setDisconnectCause(36);
                        ImsPhoneCallTracker.this.sendEmptyMessageDelayed(18, 500);
                    }
                }
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.HOLD);
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
        }

        public void onCallResumed(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallResumed");
            if (ImsPhoneCallTracker.this.isHwVolte) {
                hwOnCallResumed(imsCall);
            } else if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && (ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall) ^ 1) != 0) {
                ImsPhoneCallTracker.this.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.ACTIVE, 0);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
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
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && (ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall) ^ 1) != 0) {
                ImsPhoneCallTracker.this.log("onCallResumeFailed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
            }
            ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.RESUME);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
        }

        public void onCallResumeReceived(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallResumeReceived");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                }
                conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_UNHELD", null);
            }
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = 1;
            supp.code = 3;
            if (imsCall.isOnHold()) {
                supp.type = 1;
            } else {
                supp.type = 0;
            }
            ImsPhoneCallTracker.this.log("onCallResumeReceived supp.type:" + supp.type);
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallHoldReceived(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallHoldReceived");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (!ImsPhoneCallTracker.this.mOnHoldToneStarted && ImsPhoneCall.isLocalTone(imsCall) && conn.getState() == State.ACTIVE) {
                    ImsPhoneCallTracker.this.mPhone.startOnHoldTone(conn);
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = true;
                    ImsPhoneCallTracker.this.mOnHoldToneId = System.identityHashCode(conn);
                }
                conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_HELD", null);
            }
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = 1;
            supp.code = 2;
            if (imsCall.isOnHold()) {
                supp.type = 1;
            } else {
                supp.type = 0;
            }
            ImsPhoneCallTracker.this.log("onCallHoldReceived supp.type:" + supp.type);
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
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
            ImsPhoneCall peerImsPhoneCall;
            ImsPhoneCallTracker.this.log("onCallMerged");
            ImsPhoneCall foregroundImsPhoneCall = ImsPhoneCallTracker.this.findConnection(call).getCall();
            ImsPhoneConnection peerConnection = ImsPhoneCallTracker.this.findConnection(peerCall);
            if (peerConnection == null) {
                peerImsPhoneCall = null;
            } else {
                peerImsPhoneCall = peerConnection.getCall();
            }
            if (swapCalls) {
                ImsPhoneCallTracker.this.switchAfterConferenceSuccess();
            }
            foregroundImsPhoneCall.merge(peerImsPhoneCall, State.ACTIVE);
            try {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                ImsPhoneCallTracker.this.log("onCallMerged: ImsPhoneConnection=" + conn);
                ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                ImsPhoneCallTracker.this.setVideoCallProvider(conn, call);
                ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
            } catch (Exception e) {
                ImsPhoneCallTracker.this.loge("onCallMerged: exception " + e);
            }
            ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mForegroundCall.getImsCall(), State.ACTIVE, 0);
            if (peerConnection != null) {
                ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mBackgroundCall.getImsCall(), State.HOLDING, 0);
            }
            if (call.isMergeRequestedByConf()) {
                ImsPhoneCallTracker.this.log("onCallMerged :: Merge requested by existing conference.");
                call.resetIsMergeRequestedByConf(false);
            } else {
                ImsPhoneCallTracker.this.log("onCallMerged :: calling onMultipartyStateChanged()");
                onMultipartyStateChanged(call, true);
            }
            ImsPhoneCallTracker.this.logState();
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallMergeFailed reasonInfo=" + reasonInfo);
            ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.CONFERENCE);
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
            if (conn != null) {
                conn.onConferenceMergeFailed();
                conn.onConnectionEvent("android.telecom.event.MERGE_COMPLETE", null);
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
            ImsPhoneConnection conn;
            ImsPhoneCallTracker.this.log("onCallHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            boolean isHandoverToWifi = srcAccessTech != 18 ? targetAccessTech == 18 : false;
            if (isHandoverToWifi) {
                ImsPhoneCallTracker.this.removeMessages(25);
                if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromLteToWifi && imsCall.isVideoCall()) {
                    conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                    if (conn != null) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI", null);
                    } else {
                        ImsPhoneCallTracker.this.loge("onCallHandover :: failed to notify of handover; connection is null.");
                    }
                }
            }
            boolean isHandoverFromWifi = srcAccessTech == 18 ? targetAccessTech != 18 : false;
            if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromWifiToLTE && isHandoverFromWifi && imsCall.isVideoCall()) {
                ImsPhoneCallTracker.this.log("onCallHandover :: notifying of WIFI to LTE handover.");
                conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE", null);
                } else {
                    ImsPhoneCallTracker.this.loge("onCallHandover :: failed to notify of handover; connection is null.");
                }
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 18, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallHandoverFailed :: srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 19, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            boolean isHandoverToWifi = srcAccessTech != 18 ? targetAccessTech == 18 : false;
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null && isHandoverToWifi) {
                ImsPhoneCallTracker.this.log("onCallHandoverFailed - handover to WIFI Failed");
                ImsPhoneCallTracker.this.removeMessages(25);
                if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail) {
                    conn.onHandoverToWifiFailed();
                }
            }
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
            ImsPhoneCallTracker.this.log("onMultipartyStateChanged to " + (isMultiParty ? "Y" : "N"));
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.updateMultipartyState(isMultiParty);
            }
        }
    };
    private Stub mImsConfigListener = new Stub() {
        public void onGetFeatureResponse(int feature, int network, int value, int status) {
        }

        public void onSetFeatureResponse(int feature, int network, int value, int status) {
            ImsPhoneCallTracker.this.mMetrics.writeImsSetFeatureValue(ImsPhoneCallTracker.this.mPhone.getPhoneId(), feature, network, value, status);
        }

        public void onGetVideoQuality(int status, int quality) {
        }

        public void onSetVideoQuality(int status) {
        }
    };
    private ImsConnectionStateListener mImsConnectionStateListener = new ImsConnectionStateListener() {
        public void onImsConnected(int imsRadioTech) {
            ImsPhoneCallTracker.this.log("onImsConnected imsRadioTech=" + imsRadioTech);
            ImsPhoneCallTracker.this.mPhone.setServiceState(0);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(true);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 1, null);
        }

        public void onImsDisconnected(ImsReasonInfo imsReasonInfo) {
            ImsPhoneCallTracker.this.log("onImsDisconnected imsReasonInfo=" + imsReasonInfo);
            ImsPhoneCallTracker.this.resetImsCapabilities();
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
            ImsPhoneCallTracker.this.mPhone.processDisconnectReason(imsReasonInfo);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 3, imsReasonInfo);
        }

        public void onImsProgressing(int imsRadioTech) {
            ImsPhoneCallTracker.this.log("onImsProgressing imsRadioTech=" + imsRadioTech);
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 2, null);
        }

        public void onImsResumed() {
            ImsPhoneCallTracker.this.log("onImsResumed");
            ImsPhoneCallTracker.this.mPhone.setServiceState(0);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 4, null);
        }

        public void onImsSuspended() {
            ImsPhoneCallTracker.this.log("onImsSuspended");
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 5, null);
        }

        public void onFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
            ImsPhoneCallTracker.this.log("onFeatureCapabilityChanged");
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = serviceClass;
            args.arg1 = enabledFeatures;
            args.arg2 = disabledFeatures;
            ImsPhoneCallTracker.this.removeMessages(26);
            ImsPhoneCallTracker.this.obtainMessage(26, args).sendToTarget();
        }

        public void onVoiceMessageCountChanged(int count) {
            ImsPhoneCallTracker.this.log("onVoiceMessageCountChanged :: count=" + count);
            ImsPhoneCallTracker.this.mPhone.mDefaultPhone.setVoiceMessageCount(count);
        }

        public void registrationAssociatedUriChanged(Uri[] uris) {
            ImsPhoneCallTracker.this.log("registrationAssociatedUriChanged");
            ImsPhoneCallTracker.this.mPhone.setCurrentSubscriberUris(uris);
        }
    };
    private boolean[] mImsFeatureEnabled = new boolean[]{false, false, false, false, false, false};
    private final String[] mImsFeatureStrings = new String[]{"VoLTE", "ViLTE", "VoWiFi", "ViWiFi", "UTLTE", "UTWiFi"};
    private ImsManager mImsManager;
    private Map<Pair<Integer, String>, Integer> mImsReasonCodeMap = new ArrayMap();
    private int mImsServiceRetryCount;
    private Listener mImsUssdListener = new Listener() {
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
            ImsPhoneCallTracker.this.removeMessages(25);
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
                case 0:
                    ussdMode = 0;
                    break;
                case 1:
                    ussdMode = 1;
                    break;
            }
            ImsPhoneCallTracker.this.mPhone.onIncomingUSSD(ussdMode, ussdMessage);
        }
    };
    private boolean mIsDataEnabled = false;
    private boolean mIsInEmergencyCall = false;
    private TelephonyMetrics mMetrics;
    private boolean mNotifyHandoverVideoFromLteToWifi = false;
    private boolean mNotifyHandoverVideoFromWifiToLTE = false;
    private INotifyStatusChanged mNotifyStatusChangedCallback = new -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w(this);
    private boolean mNotifyVtHandoverToWifiFail = false;
    private int mOnHoldToneId = -1;
    private boolean mOnHoldToneStarted = false;
    private int mPendingCallVideoState;
    private Bundle mPendingIntentExtras;
    private ImsPhoneConnection mPendingMO;
    private Message mPendingUssd = null;
    ImsPhone mPhone;
    private List<PhoneStateListener> mPhoneStateListeners = new ArrayList();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
                        if (HwDeviceManager.disallowOp(1)) {
                            Log.i(ImsPhoneCallTracker.LOG_TAG, "MDM APK disallow open call.");
                            return;
                        }
                        ImsPhoneCall imsPhoneCall;
                        ImsCall imsCall = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsCallListener);
                        Phone phone = ImsPhoneCallTracker.this.mPhone;
                        ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                        if (isUnknown) {
                            imsPhoneCall = ImsPhoneCallTracker.this.mForegroundCall;
                        } else {
                            imsPhoneCall = ImsPhoneCallTracker.this.mRingingCall;
                        }
                        ImsPhoneConnection conn = new ImsPhoneConnection(phone, imsCall, imsPhoneCallTracker, imsPhoneCall, isUnknown);
                        if (ImsPhoneCallTracker.this.mForegroundCall.hasConnections()) {
                            ImsCall activeCall = ImsPhoneCallTracker.this.mForegroundCall.getFirstConnection().getImsCall();
                            if (!(activeCall == null || imsCall == null)) {
                                conn.setActiveCallDisconnectedOnAnswer(ImsPhoneCallTracker.this.shouldDisconnectActiveCallOnAnswer(activeCall, imsCall));
                            }
                        }
                        conn.setAllowAddCallDuringVideoCall(ImsPhoneCallTracker.this.mAllowAddCallDuringVideoCall);
                        ImsPhoneCallTracker.this.addConnection(conn);
                        ImsPhoneCallTracker.this.setVideoCallProvider(conn, imsCall);
                        TelephonyMetrics.getInstance().writeOnImsCallReceive(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getSession());
                        if (isUnknown) {
                            ImsPhoneCallTracker.this.mPhone.notifyUnknownConnection(conn);
                        } else {
                            if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == State.IDLE)) {
                                conn.update(imsCall, State.WAITING);
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
                    ImsPhoneCallTracker.this.cacheCarrierConfiguration(subId);
                    ImsPhoneCallTracker.this.log("onReceive : Updating mAllowEmergencyVideoCalls = " + ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls);
                }
            }
        }
    };
    public IRetryTimeout mRetryTimeout = new com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w.AnonymousClass1(this);
    public ImsPhoneCall mRingingCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_RINGING);
    private int mServiceId = -1;
    private boolean mShouldUpdateImsConfigOnDisconnect = false;
    private SrvccState mSrvccState = SrvccState.NONE;
    private PhoneConstants.State mState = PhoneConstants.State.IDLE;
    private boolean mSupportDowngradeVtToAudio = false;
    private boolean mSupportPauseVideo = false;
    private boolean mSwitchingFgAndBgCalls = false;
    private Object mSyncHold = new Object();
    private Object mSyncResume = new Object();
    private volatile long mTotalVtDataUsage = 0;
    private boolean mTreatDowngradedVideoCallsAsVideoCalls = false;
    private ImsCall mUssdSession = null;
    private RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    private RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();
    private final HashMap<Integer, Long> mVtDataUsageMap = new HashMap();
    private int pendingCallClirMode;
    private boolean pendingCallInEcm = false;

    public interface IRetryTimeout {
        int get();
    }

    public interface PhoneStateListener {
        void onPhoneStateChanged(PhoneConstants.State state, PhoneConstants.State state2);
    }

    static {
        PRECISE_CAUSE_MAP.append(101, 1200);
        PRECISE_CAUSE_MAP.append(102, 1201);
        PRECISE_CAUSE_MAP.append(103, 1202);
        PRECISE_CAUSE_MAP.append(106, 1203);
        PRECISE_CAUSE_MAP.append(107, 1204);
        PRECISE_CAUSE_MAP.append(AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE, 16);
        PRECISE_CAUSE_MAP.append(111, 1205);
        PRECISE_CAUSE_MAP.append(112, 1206);
        PRECISE_CAUSE_MAP.append(121, 1207);
        PRECISE_CAUSE_MAP.append(122, 1208);
        PRECISE_CAUSE_MAP.append(123, 1209);
        PRECISE_CAUSE_MAP.append(124, 1210);
        PRECISE_CAUSE_MAP.append(131, 1211);
        PRECISE_CAUSE_MAP.append(132, 1212);
        PRECISE_CAUSE_MAP.append(141, 1213);
        PRECISE_CAUSE_MAP.append(143, 1214);
        PRECISE_CAUSE_MAP.append(144, 1215);
        PRECISE_CAUSE_MAP.append(145, 1216);
        PRECISE_CAUSE_MAP.append(146, 1217);
        PRECISE_CAUSE_MAP.append(147, 1218);
        PRECISE_CAUSE_MAP.append(148, 1219);
        PRECISE_CAUSE_MAP.append(149, 1220);
        PRECISE_CAUSE_MAP.append(201, 1221);
        PRECISE_CAUSE_MAP.append(202, 1222);
        PRECISE_CAUSE_MAP.append(203, 1223);
        PRECISE_CAUSE_MAP.append(241, 241);
        PRECISE_CAUSE_MAP.append(321, HwRadarUtils.ERROR_BASE_MMS);
        PRECISE_CAUSE_MAP.append(331, 1310);
        PRECISE_CAUSE_MAP.append(332, HwRadarUtils.ERR_SMS_SEND);
        PRECISE_CAUSE_MAP.append(333, HwRadarUtils.ERR_SMS_RECEIVE);
        PRECISE_CAUSE_MAP.append(334, 1313);
        PRECISE_CAUSE_MAP.append(335, 1314);
        PRECISE_CAUSE_MAP.append(336, 1315);
        PRECISE_CAUSE_MAP.append(337, 1316);
        PRECISE_CAUSE_MAP.append(338, HwRadarUtils.ERR_SMS_SEND_BACKGROUND);
        PRECISE_CAUSE_MAP.append(339, 1318);
        PRECISE_CAUSE_MAP.append(340, 1319);
        PRECISE_CAUSE_MAP.append(341, 1320);
        PRECISE_CAUSE_MAP.append(342, 1321);
        PRECISE_CAUSE_MAP.append(RilConstS32.RIL_REQUEST_CANCEL_IMS_VIDEO_CALL, 1330);
        PRECISE_CAUSE_MAP.append(352, 1331);
        PRECISE_CAUSE_MAP.append(353, 1332);
        PRECISE_CAUSE_MAP.append(354, 1333);
        PRECISE_CAUSE_MAP.append(361, 1340);
        PRECISE_CAUSE_MAP.append(362, 1341);
        PRECISE_CAUSE_MAP.append(363, 1342);
        PRECISE_CAUSE_MAP.append(364, 1343);
        PRECISE_CAUSE_MAP.append(401, 1400);
        PRECISE_CAUSE_MAP.append(402, 1401);
        PRECISE_CAUSE_MAP.append(403, 1402);
        PRECISE_CAUSE_MAP.append(404, 1403);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_1, 1500);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_2, 1501);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_3, 1502);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_4, 1503);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_5, 1504);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_6, 1505);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_10, 1510);
        PRECISE_CAUSE_MAP.append(801, 1800);
        PRECISE_CAUSE_MAP.append(802, 1801);
        PRECISE_CAUSE_MAP.append(803, 1802);
        PRECISE_CAUSE_MAP.append(804, 1803);
        PRECISE_CAUSE_MAP.append(821, 1804);
        PRECISE_CAUSE_MAP.append(901, 1900);
        PRECISE_CAUSE_MAP.append(902, 1901);
        PRECISE_CAUSE_MAP.append(1100, ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT);
        PRECISE_CAUSE_MAP.append(1014, 2100);
        PRECISE_CAUSE_MAP.append(CharacterSets.UTF_16, 2101);
        PRECISE_CAUSE_MAP.append(1016, 2102);
        PRECISE_CAUSE_MAP.append(1201, 2300);
        PRECISE_CAUSE_MAP.append(1202, 2301);
        PRECISE_CAUSE_MAP.append(1203, 2302);
        PRECISE_CAUSE_MAP.append(HwRadarUtils.ERROR_BASE_MMS, 2400);
        PRECISE_CAUSE_MAP.append(1400, 2500);
        PRECISE_CAUSE_MAP.append(1401, 2501);
        PRECISE_CAUSE_MAP.append(1402, 2502);
        PRECISE_CAUSE_MAP.append(1403, 2503);
        PRECISE_CAUSE_MAP.append(1404, 2504);
        PRECISE_CAUSE_MAP.append(1405, 2505);
        PRECISE_CAUSE_MAP.append(1406, 2506);
        PRECISE_CAUSE_MAP.append(1407, 2507);
        PRECISE_CAUSE_MAP.append(1500, 247);
        PRECISE_CAUSE_MAP.append(1501, 249);
        PRECISE_CAUSE_MAP.append(1502, 250);
        PRECISE_CAUSE_MAP.append(1503, 251);
        PRECISE_CAUSE_MAP.append(1504, 252);
        PRECISE_CAUSE_MAP.append(1505, 253);
        PRECISE_CAUSE_MAP.append(1506, 254);
        PRECISE_CAUSE_MAP.append(1507, 255);
        PRECISE_CAUSE_MAP.append(1508, 256);
        PRECISE_CAUSE_MAP.append(1509, 257);
        PRECISE_CAUSE_MAP.append(1510, 258);
        PRECISE_CAUSE_MAP.append(1511, 259);
        PRECISE_CAUSE_MAP.append(1512, 260);
        PRECISE_CAUSE_MAP.append(1513, 261);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_1, LastCallFailCause.OEM_CAUSE_1);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_2, LastCallFailCause.OEM_CAUSE_2);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_3, LastCallFailCause.OEM_CAUSE_3);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_4, LastCallFailCause.OEM_CAUSE_4);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_5, LastCallFailCause.OEM_CAUSE_5);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_6, LastCallFailCause.OEM_CAUSE_6);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_7, LastCallFailCause.OEM_CAUSE_7);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_8, LastCallFailCause.OEM_CAUSE_8);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_9, LastCallFailCause.OEM_CAUSE_9);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_10, LastCallFailCause.OEM_CAUSE_10);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_11, LastCallFailCause.OEM_CAUSE_11);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_12, LastCallFailCause.OEM_CAUSE_12);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_13, LastCallFailCause.OEM_CAUSE_13);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_14, LastCallFailCause.OEM_CAUSE_14);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_15, LastCallFailCause.OEM_CAUSE_15);
    }

    /* synthetic */ void lambda$-com_android_internal_telephony_imsphone_ImsPhoneCallTracker_32584() {
        try {
            int status = this.mImsManager.getImsServiceStatus();
            log("Status Changed: " + status);
            switch (status) {
                case 0:
                case 1:
                    stopListeningForCalls();
                    return;
                case 2:
                    startListeningForCalls();
                    return;
                default:
                    Log.w(LOG_TAG, "Unexpected State!");
                    return;
            }
        } catch (ImsException e) {
            retryGetImsService();
        }
        retryGetImsService();
    }

    /* synthetic */ int lambda$-com_android_internal_telephony_imsphone_ImsPhoneCallTracker_33656() {
        int timeout = (1 << this.mImsServiceRetryCount) * 500;
        if (this.mImsServiceRetryCount <= 6) {
            this.mImsServiceRetryCount++;
        }
        return timeout;
    }

    public ImsPhoneCallTracker(ImsPhone phone) {
        this.mPhone = phone;
        this.mMetrics = TelephonyMetrics.getInstance();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.ims.IMS_INCOMING_CALL");
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mReceiver, intentfilter);
        cacheCarrierConfiguration(this.mPhone.getSubId());
        this.mPhone.getDefaultPhone().registerForDataEnabledChanged(this, 23, null);
        this.mImsServiceRetryCount = 0;
        this.mCust = (HwCustImsPhoneCallTracker) HwCustUtils.createObj(HwCustImsPhoneCallTracker.class, new Object[]{this.mPhone.getContext()});
        sendEmptyMessage(24);
    }

    private PendingIntent createIncomingCallPendingIntent() {
        Intent intent = new Intent("com.android.ims.IMS_INCOMING_CALL");
        intent.addFlags(268435456);
        return PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
    }

    private void getImsService() throws ImsException {
        log("getImsService");
        this.mImsManager = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId());
        this.mImsManager.addNotifyStatusChangedCallbackIfAvailable(this.mNotifyStatusChangedCallback);
        this.mNotifyStatusChangedCallback.notifyStatusChanged();
        this.mGetImsService = true;
    }

    private void startListeningForCalls() throws ImsException {
        this.mImsServiceRetryCount = 0;
        this.mServiceId = this.mImsManager.open(1, createIncomingCallPendingIntent(), this.mImsConnectionStateListener);
        this.mImsManager.setImsConfigListener(this.mImsConfigListener);
        getEcbmInterface().setEcbmStateListener(this.mPhone.getImsEcbmStateListener());
        if (this.mPhone.isInEcm()) {
            this.mPhone.exitEmergencyCallbackMode();
        }
        this.mImsManager.setUiTTYMode(this.mPhone.getContext(), Secure.getInt(this.mPhone.getContext().getContentResolver(), "preferred_tty_mode", 0), null);
        ImsMultiEndpoint multiEndpoint = getMultiEndpointInterface();
        if (multiEndpoint != null) {
            multiEndpoint.setExternalCallStateListener(this.mPhone.getExternalCallTracker().getExternalCallStateListener());
        }
    }

    private void stopListeningForCalls() {
        try {
            if (this.mImsManager != null && this.mServiceId > 0) {
                this.mImsManager.close(this.mServiceId);
                this.mServiceId = -1;
            }
        } catch (ImsException e) {
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
        this.mPhone.getDefaultPhone().unregisterForDataEnabledChanged(this);
        removeMessages(24);
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
            if (isEmergencyNumber && VideoProfile.isVideo(videoState) && (this.mAllowEmergencyVideoCalls ^ 1) != 0) {
                loge("dial: carrier does not support video emergency calls; downgrade to audio-only");
                videoState = 0;
            }
            boolean holdBeforeDial = false;
            if (this.mForegroundCall.getState() == State.ACTIVE) {
                if (this.mBackgroundCall.getState() != State.IDLE) {
                    throw new CallStateException("cannot dial in current state");
                }
                holdBeforeDial = true;
                this.mPendingCallVideoState = videoState;
                this.mPendingIntentExtras = intentExtras;
                switchWaitingOrHoldingAndActive();
            }
            State fgState = State.IDLE;
            State bgState = State.IDLE;
            this.mClirMode = clirMode;
            synchronized (this.mSyncHold) {
                if (holdBeforeDial) {
                    fgState = this.mForegroundCall.getState();
                    bgState = this.mBackgroundCall.getState();
                    if (fgState == State.ACTIVE) {
                        throw new CallStateException("cannot dial in current state");
                    } else if (bgState == State.HOLDING) {
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
                        this.pendingCallInEcm = true;
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

    private void cacheCarrierConfiguration(int subId) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null) {
            loge("cacheCarrierConfiguration: No carrier config service found.");
            return;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(subId);
        if (carrierConfig == null) {
            loge("cacheCarrierConfiguration: Empty carrier config.");
            return;
        }
        this.mAllowEmergencyVideoCalls = carrierConfig.getBoolean("allow_emergency_video_calls_bool");
        this.mTreatDowngradedVideoCallsAsVideoCalls = carrierConfig.getBoolean("treat_downgraded_video_calls_as_video_calls_bool");
        this.mDropVideoCallWhenAnsweringAudioCall = carrierConfig.getBoolean("drop_video_call_when_answering_audio_call_bool");
        this.mAllowAddCallDuringVideoCall = carrierConfig.getBoolean("allow_add_call_during_video_call");
        this.mNotifyVtHandoverToWifiFail = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mSupportDowngradeVtToAudio = carrierConfig.getBoolean("support_downgrade_vt_to_audio_bool");
        this.mNotifyHandoverVideoFromWifiToLTE = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mNotifyHandoverVideoFromLteToWifi = carrierConfig.getBoolean("notify_handover_video_from_lte_to_wifi_bool");
        this.mIgnoreDataEnabledChangedForVideoCalls = carrierConfig.getBoolean("ignore_data_enabled_changed_for_video_calls");
        this.mSupportPauseVideo = carrierConfig.getBoolean("support_pause_ims_video_calls_bool");
        String[] mappings = carrierConfig.getStringArray("ims_reasoninfo_mapping_string_array");
        if (mappings == null || mappings.length <= 0) {
            log("No carrier ImsReasonInfo mappings defined.");
        } else {
            for (String mapping : mappings) {
                String[] values = mapping.split(Pattern.quote("|"));
                if (values.length == 3) {
                    try {
                        Integer fromCode;
                        if (values[0].equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                            fromCode = null;
                        } else {
                            fromCode = Integer.valueOf(Integer.parseInt(values[0]));
                        }
                        String message = values[1];
                        int toCode = Integer.parseInt(values[2]);
                        addReasonCodeRemapping(fromCode, message, Integer.valueOf(toCode));
                        log(new StringBuilder().append("Loaded ImsReasonInfo mapping : fromCode = ").append(fromCode).toString() == null ? "any" : fromCode + " ; message = " + message + " ; toCode = " + toCode);
                    } catch (NumberFormatException e) {
                        loge("Invalid ImsReasonInfo mapping found: " + mapping);
                    }
                }
            }
        }
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
            case 1:
                return;
            default:
                log("handleEcmTimer, unsupported action " + action);
                return;
        }
    }

    private void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        if (conn != null) {
            if (conn.getAddress() == null || conn.getAddress().length() == 0 || conn.getAddress().indexOf(78) >= 0) {
                conn.setDisconnectCause(7);
                sendEmptyMessageDelayed(18, 500);
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
                    if (intentExtras.containsKey("CallPull")) {
                        profile.mCallExtras.putBoolean("CallPull", intentExtras.getBoolean("CallPull"));
                        int dialogId = intentExtras.getInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID);
                        conn.setIsPulledCall(true);
                        conn.setPulledDialogId(dialogId);
                    }
                    profile.mCallExtras.putBundle("OemCallExtras", intentExtras);
                }
                ImsCall imsCall = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsCallListener);
                conn.setImsCall(imsCall);
                setMute(false);
                this.mMetrics.writeOnImsCallStart(this.mPhone.getPhoneId(), imsCall.getSession());
                setVideoCallProvider(conn, imsCall);
                conn.setAllowAddCallDuringVideoCall(this.mAllowAddCallDuringVideoCall);
            } catch (ImsException e) {
                loge("dialInternal : " + e);
                conn.setDisconnectCause(36);
                sendEmptyMessageDelayed(18, 500);
                retryGetImsService();
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
        if (this.mRingingCall.getState() == State.WAITING && this.mForegroundCall.getState().isAlive()) {
            setMute(false);
            boolean answeringWillDisconnect = false;
            ImsCall activeCall = this.mForegroundCall.getImsCall();
            ImsCall ringingCall = this.mRingingCall.getImsCall();
            if (this.mForegroundCall.hasConnections() && this.mRingingCall.hasConnections()) {
                answeringWillDisconnect = shouldDisconnectActiveCallOnAnswer(activeCall, ringingCall);
            }
            this.mPendingCallVideoState = videoState;
            if (answeringWillDisconnect) {
                this.mForegroundCall.hangup();
                try {
                    ringingCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                    return;
                } catch (ImsException e) {
                    throw new CallStateException("cannot accept call");
                }
            }
            switchWaitingOrHoldingAndActive();
        } else if (this.mRingingCall.getState().isRinging()) {
            log("acceptCall: incoming...");
            setMute(false);
            try {
                ImsCall imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                    return;
                }
                throw new CallStateException("no valid ims call");
            } catch (ImsException e2) {
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
        if (this.mForegroundCall.getState() == State.IDLE && this.mBackgroundCall.getState() == State.HOLDING) {
            log("switchAfterConferenceSuccess");
            this.mForegroundCall.switchWith(this.mBackgroundCall);
        }
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        log("switchWaitingOrHoldingAndActive");
        if (this.mRingingCall.getState() == State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (this.mForegroundCall.getState() == State.ACTIVE) {
            ImsCall imsCall = this.mForegroundCall.getImsCall();
            if (imsCall == null) {
                throw new CallStateException("no ims call");
            }
            boolean switchingWithWaitingCall = (this.mBackgroundCall.getImsCall() != null || this.mRingingCall == null) ? false : this.mRingingCall.getState() == State.WAITING;
            this.mSwitchingFgAndBgCalls = true;
            if (switchingWithWaitingCall) {
                for (Connection c : this.mRingingCall.getConnections()) {
                    if (c != null) {
                        this.mCallExpectedToResume.add(((ImsPhoneConnection) c).getImsCall());
                    }
                }
            } else {
                for (Connection c2 : this.mBackgroundCall.getConnections()) {
                    if (c2 != null) {
                        this.mCallExpectedToResume.add(((ImsPhoneConnection) c2).getImsCall());
                    }
                }
            }
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            try {
                imsCall.hold(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 5);
                if (this.mCallExpectedToResume.isEmpty()) {
                    log("mCallExpectedToResume is empty");
                    this.mSwitchingFgAndBgCalls = false;
                }
            } catch (ImsException e) {
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                throw new CallStateException(e.getMessage());
            }
        } else if (this.mBackgroundCall.getState() == State.HOLDING) {
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
            foregroundConnection.onConnectionEvent("android.telecom.event.MERGE_START", null);
        }
        ImsPhoneConnection backgroundConnection = findConnection(bgImsCall);
        if (backgroundConnection != null) {
            backgroundConnection.onConnectionEvent("android.telecom.event.MERGE_START", null);
        }
        try {
            fgImsCall.merge(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState), bgImsCall);
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
        if (this.mForegroundCall.getState() == State.ACTIVE && this.mBackgroundCall.getState() == State.HOLDING && (this.mBackgroundCall.isFull() ^ 1) != 0) {
            return this.mForegroundCall.isFull() ^ 1;
        }
        return false;
    }

    public boolean canDial() {
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        State fgCallState = this.mForegroundCall.getState();
        State bgCallState = this.mBackgroundCall.getState();
        if (this.mPendingMO != null || (this.mRingingCall.isRinging() ^ 1) == 0 || (disableCall.equals("true") ^ 1) == 0) {
            return false;
        }
        if (fgCallState != State.IDLE && fgCallState != State.DISCONNECTED && fgCallState != State.ACTIVE) {
            return false;
        }
        if (bgCallState == State.IDLE || bgCallState == State.DISCONNECTED) {
            return true;
        }
        return bgCallState == State.HOLDING;
    }

    public boolean canTransfer() {
        if (this.mForegroundCall.getState() == State.ACTIVE && this.mBackgroundCall.getState() == State.HOLDING) {
            return true;
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
        Object obj;
        PhoneConstants.State oldState = this.mState;
        boolean isPendingMOIdle = this.mPendingMO != null ? this.mPendingMO.getState().isAlive() ^ 1 : true;
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (isPendingMOIdle && (this.mForegroundCall.isIdle() ^ 1) == 0 && (this.mBackgroundCall.isIdle() ^ 1) == 0) {
            this.mState = PhoneConstants.State.IDLE;
        } else {
            this.mState = PhoneConstants.State.OFFHOOK;
        }
        if (this.mState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (oldState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        StringBuilder append = new StringBuilder().append("updatePhoneState pendingMo = ");
        if (this.mPendingMO == null) {
            obj = "null";
        } else {
            obj = this.mPendingMO.getState();
        }
        log(append.append(obj).append(", fg= ").append(this.mForegroundCall.getState()).append("(").append(this.mForegroundCall.getConnections().size()).append("), bg= ").append(this.mBackgroundCall.getState()).append("(").append(this.mBackgroundCall.getConnections().size()).append(")").toString());
        log("updatePhoneState oldState=" + oldState + ", newState=" + this.mState);
        if (this.mState != oldState) {
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            notifyPhoneStateChanged(oldState, this.mState);
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
        if (this.mImsManager == null) {
            this.mPhone.sendErrorResponse(onComplete, getImsManagerIsNullException());
            return;
        }
        try {
            this.mImsManager.setUiTTYMode(this.mPhone.getContext(), uiTtyMode, onComplete);
        } catch (ImsException e) {
            loge("setTTYMode : " + e);
            this.mPhone.sendErrorResponse(onComplete, e);
            retryGetImsService();
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
            ImsPhoneCall call = conn.getCall();
            if (call != null && call.getConnections() != null) {
                int size = call.getConnections().size();
                Rlog.d(LOG_TAG, "ImsPhoneCallTracker:hangup:size =" + size);
                if (!conn.isMultiparty() || size != 1) {
                    hangupImsConnection(conn);
                } else if (call == this.mForegroundCall) {
                    hangupForegroundResumeBackground(conn.getCall());
                } else if (call == this.mBackgroundCall) {
                    hangupWaitingOrBackground(conn.getCall());
                } else {
                    hangupImsConnection(conn);
                }
            }
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
            rejectCall = true;
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
                if (!call.isMultiparty()) {
                    hangupImsCall(call);
                } else if (call == this.mForegroundCall) {
                    hangupForegroundResumeBackground(call);
                } else if (call == this.mBackgroundCall) {
                    hangupWaitingOrBackground(call);
                } else {
                    hangupImsCall(call);
                }
            } else if (imsCall != null) {
                if (rejectCall) {
                    imsCall.reject(RadioError.OEM_ERROR_4);
                } else {
                    imsCall.terminate(RadioError.OEM_ERROR_1);
                }
            } else if (this.mPendingMO != null && call == this.mForegroundCall) {
                this.mPendingMO.update(null, State.DISCONNECTED);
                this.mPendingMO.onDisconnect();
                removeConnection(this.mPendingMO);
                this.mPendingMO = null;
                updatePhoneState();
                removeMessages(20);
            }
            this.mPhone.notifyPreciseCallStateChanged();
        } catch (ImsException e) {
            throw new CallStateException(e.getMessage());
        }
    }

    private void hangupForegroundResumeBackground(ImsPhoneCall call) throws CallStateException {
        log("hangupForegroundResumeBackground");
        ImsCall imsCall = call.getImsCall();
        if (imsCall == null) {
            try {
                log("imsCall is null,faild");
                return;
            } catch (ImsException e) {
                throw new CallStateException(e.getMessage());
            }
        }
        imsCall.hangupForegroundResumeBackground(RadioError.OEM_ERROR_1);
        this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
    }

    private void hangupWaitingOrBackground(ImsPhoneCall call) throws CallStateException {
        log("hangupWaitingOrBackground");
        ImsCall imsCall = call.getImsCall();
        if (imsCall == null) {
            try {
                log("imsCall is null,faild");
                return;
            } catch (ImsException e) {
                throw new CallStateException(e.getMessage());
            }
        }
        imsCall.hangupWaitingOrBackground(RadioError.OEM_ERROR_1);
        this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
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
            rejectCall = true;
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
        if (call.getConnections().size() == 1) {
            conn.getCall().setState(State.DISCONNECTING);
        }
        log("hangupImsConnection imsCall  :: " + imsCall);
        if (imsCall != null) {
            if (rejectCall) {
                try {
                    imsCall.reject(RadioError.OEM_ERROR_4);
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 3);
                } catch (ImsException e) {
                    throw new CallStateException(e.getMessage());
                }
            }
            imsCall.terminate(RadioError.OEM_ERROR_1);
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
        } else if (this.mPendingMO != null && call == this.mForegroundCall) {
            this.mPendingMO.update(null, State.DISCONNECTED);
            this.mPendingMO.onDisconnect();
            removeConnection(this.mPendingMO);
            this.mPendingMO = null;
            updatePhoneState();
            removeMessages(20);
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
            this.mConnections.clear();
            this.mState = PhoneConstants.State.IDLE;
        }
    }

    void resumeWaitingOrHolding() throws CallStateException {
        log("resumeWaitingOrHolding");
        try {
            ImsCall imsCall;
            if (this.mForegroundCall.getState().isAlive()) {
                imsCall = this.mForegroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            } else if (this.mRingingCall.getState() == State.WAITING) {
                imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                }
            } else {
                imsCall = this.mBackgroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
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
            } else if (this.mImsManager == null) {
                this.mPhone.sendErrorResponse(response, getImsManagerIsNullException());
            } else {
                String[] callees = new String[]{ussdString};
                ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, 1, 2);
                profile.setCallExtraInt("dialstring", 2);
                this.mUssdSession = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsUssdListener);
            }
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
            retryGetImsService();
        }
    }

    public void cancelUSSD() {
        if (this.mUssdSession != null) {
            try {
                this.mUssdSession.terminate(RadioError.OEM_ERROR_1);
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
                    isEmergencyCallInList = true;
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
            this.mIsInEmergencyCall = true;
            this.mPhone.sendEmergencyCallStateChange(true);
        }
    }

    private void processCallStateChange(ImsCall imsCall, State state, int cause) {
        log("processCallStateChange " + imsCall + " state=" + state + " cause=" + cause);
        processCallStateChange(imsCall, state, cause, false);
    }

    private void processCallStateChange(ImsCall imsCall, State state, int cause, boolean ignoreState) {
        log("processCallStateChange state=" + state + " cause=" + cause + " ignoreState=" + ignoreState);
        if (imsCall != null) {
            ImsPhoneConnection conn = findConnection(imsCall);
            if (conn != null) {
                conn.updateMediaCapabilities(imsCall);
                boolean changed = conn.update(imsCall, state);
                conn.updateMediaCapabilities(imsCall);
                if (state == State.DISCONNECTED) {
                    if (conn.onDisconnect(cause)) {
                        changed = true;
                    }
                    conn.getCall().detach(conn);
                    removeConnection(conn);
                }
                if ((changed || ImsPhoneFactory.isimsAsNormalCon()) && conn.getCall() != this.mHandoverCall) {
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

    public void addReasonCodeRemapping(Integer fromCode, String message, Integer toCode) {
        this.mImsReasonCodeMap.put(new Pair(fromCode, message), toCode);
    }

    public int maybeRemapReasonCode(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        Pair<Integer, String> toCheck = new Pair(Integer.valueOf(code), reasonInfo.getExtraMessage());
        Pair<Integer, String> wildcardToCheck = new Pair(null, reasonInfo.getExtraMessage());
        int toCode;
        if (this.mImsReasonCodeMap.containsKey(toCheck)) {
            toCode = ((Integer) this.mImsReasonCodeMap.get(toCheck)).intValue();
            log("maybeRemapReasonCode : fromCode = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode);
            return toCode;
        } else if (!this.mImsReasonCodeMap.containsKey(wildcardToCheck)) {
            return code;
        } else {
            toCode = ((Integer) this.mImsReasonCodeMap.get(wildcardToCheck)).intValue();
            log("maybeRemapReasonCode : fromCode(wildcard) = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode);
            return toCode;
        }
    }

    private int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        if (this.mCust != null && this.mCust.isForkedCallLoggingEnabled()) {
            int custCause = this.mCust.getDisconnectCauseFromReasonInfo(reasonInfo);
            if (custCause != 36) {
                return custCause;
            }
        }
        switch (maybeRemapReasonCode(reasonInfo)) {
            case 21:
                return 21;
            case 106:
            case 121:
            case 122:
            case 123:
            case 124:
            case 131:
            case 132:
            case 144:
                return 18;
            case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE /*108*/:
                return 45;
            case 111:
            case 112:
                return 17;
            case 143:
            case 1404:
                return 16;
            case 201:
            case 202:
            case 203:
            case 335:
                return 13;
            case 241:
                return 21;
            case 321:
            case 331:
            case 340:
            case 361:
            case 362:
                return 12;
            case 332:
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
                return 100;
            case 364:
                return 101;
            case RadioError.OEM_ERROR_1 /*501*/:
                return 3;
            case RadioError.OEM_ERROR_10 /*510*/:
                return 2;
            case 1014:
                return 52;
            case 1016:
                return 51;
            case 1100:
                return 106;
            case 1403:
                return 53;
            case 1405:
                return 55;
            case 1406:
                return 54;
            case 1407:
                return 59;
            default:
                return 36;
        }
    }

    private int getPreciseDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        return PRECISE_CAUSE_MAP.get(maybeRemapReasonCode(reasonInfo), 65535);
    }

    private boolean isPhoneInEcbMode() {
        return this.mPhone.isInEcm();
    }

    private void dialPendingMO() {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPendingMO.isEmergency();
        if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
            sendEmptyMessage(20);
        } else {
            sendEmptyMessage(21);
        }
    }

    public ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getSupplementaryServiceConfiguration();
        }
        throw getImsManagerIsNullException();
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
        call.mState = State.IDLE;
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
        AsyncResult ar;
        switch (msg.what) {
            case 14:
                if (this.pendingCallInEcm) {
                    dialInternal(this.mPendingMO, this.pendingCallClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    this.pendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                return;
            case 18:
                if (this.mPendingMO != null) {
                    this.mPendingMO.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                }
                this.mPendingIntentExtras = null;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                return;
            case 19:
                try {
                    resumeWaitingOrHolding();
                    return;
                } catch (CallStateException e) {
                    loge("handleMessage EVENT_RESUME_BACKGROUND exception=" + e);
                    return;
                }
            case 20:
                dialInternal(this.mPendingMO, this.mClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                this.mPendingIntentExtras = null;
                return;
            case 21:
                if (this.mPendingMO != null) {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = this.mClirMode;
                        this.pendingCallInEcm = true;
                        return;
                    } catch (ImsException e2) {
                        e2.printStackTrace();
                        this.mPendingMO.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        return;
                    }
                }
                return;
            case 22:
                ar = msg.obj;
                ImsCall call = ar.userObj;
                Long usage = Long.valueOf(((Long) ar.result).longValue());
                log("VT data usage update. usage = " + usage + ", imsCall = " + call);
                Long oldUsage = Long.valueOf(0);
                if (this.mVtDataUsageMap.containsKey(Integer.valueOf(call.uniqueId))) {
                    oldUsage = (Long) this.mVtDataUsageMap.get(Integer.valueOf(call.uniqueId));
                }
                this.mTotalVtDataUsage += usage.longValue() - oldUsage.longValue();
                this.mVtDataUsageMap.put(Integer.valueOf(call.uniqueId), usage);
                return;
            case 23:
                ar = (AsyncResult) msg.obj;
                if (ar.result instanceof Pair) {
                    Pair<Boolean, Integer> p = ar.result;
                    onDataEnabledChanged(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                    return;
                }
                return;
            case 24:
                try {
                    getImsService();
                    return;
                } catch (ImsException e22) {
                    loge("getImsService: " + e22);
                    this.mGetImsService = false;
                    retryGetImsService();
                    return;
                }
            case 25:
                if (msg.obj instanceof ImsCall) {
                    ImsCall imsCall = msg.obj;
                    if (!imsCall.isWifiCall()) {
                        ImsPhoneConnection conn = findConnection(imsCall);
                        if (conn != null) {
                            conn.onHandoverToWifiFailed();
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            case 26:
                SomeArgs args = msg.obj;
                try {
                    handleFeatureCapabilityChanged(args.argi1, args.arg1, args.arg2);
                    return;
                } finally {
                    args.recycle();
                }
            default:
                return;
        }
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    void logState() {
        if (VERBOSE_STATE_LOGGING) {
            StringBuilder sb = new StringBuilder();
            sb.append("Current IMS PhoneCall State:\n");
            sb.append(" Foreground: ");
            sb.append(this.mForegroundCall);
            sb.append("\n");
            sb.append(" Background: ");
            sb.append(this.mBackgroundCall);
            sb.append("\n");
            sb.append(" Ringing: ");
            sb.append(this.mRingingCall);
            sb.append("\n");
            sb.append(" Handover: ");
            sb.append(this.mHandoverCall);
            sb.append("\n");
            Rlog.v(LOG_TAG, sb.toString());
        }
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
        pw.println(" mTotalVtDataUsage=" + this.mTotalVtDataUsage);
        for (Entry<Integer, Long> entry : this.mVtDataUsageMap.entrySet()) {
            pw.println("    id=" + entry.getKey() + " ,usage=" + entry.getValue());
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
        throw getImsManagerIsNullException();
    }

    ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        if (this.mImsManager == null) {
            throw getImsManagerIsNullException();
        }
        try {
            return this.mImsManager.getMultiEndpointInterface(this.mServiceId);
        } catch (ImsException e) {
            if (e.getCode() == 902) {
                return null;
            }
            throw e;
        }
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
            return true;
        }
        return this.mImsFeatureEnabled[3];
    }

    public PhoneConstants.State getState() {
        return this.mState;
    }

    private void retryGetImsService() {
        if (!this.mImsManager.isServiceAvailable() || !this.mGetImsService) {
            this.mImsManager = null;
            loge("getImsService: Retrying getting ImsService...");
            removeMessages(24);
            sendEmptyMessageDelayed(24, (long) this.mRetryTimeout.get());
        }
    }

    private void setVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) throws RemoteException {
        IImsVideoCallProvider imsVideoCallProvider = imsCall.getCallSession().getVideoCallProvider();
        if (imsVideoCallProvider != null) {
            boolean useVideoPauseWorkaround = this.mPhone.getContext().getResources().getBoolean(17957041);
            ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = new ImsVideoCallProviderWrapper(imsVideoCallProvider);
            if (useVideoPauseWorkaround) {
                imsVideoCallProviderWrapper.setUseVideoPauseWorkaround(useVideoPauseWorkaround);
            }
            conn.setVideoProvider(imsVideoCallProviderWrapper);
            imsVideoCallProviderWrapper.registerForDataUsageUpdate(this, 22, imsCall);
            imsVideoCallProviderWrapper.addImsVideoProviderCallback(conn);
        }
    }

    public boolean isUtEnabled() {
        if (this.mImsFeatureEnabled[4]) {
            return true;
        }
        return this.mImsFeatureEnabled[5];
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
        if (this.mImsFeatureEnabled[4] || this.mImsFeatureEnabled[5]) {
            return this.mCust != null ? this.mCust.checkImsRegistered() : true;
        } else {
            return false;
        }
    }

    public void pullExternalCall(String number, int videoState, int dialogId) {
        Bundle extras = new Bundle();
        extras.putBoolean("CallPull", true);
        extras.putInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID, dialogId);
        try {
            this.mPhone.notifyUnknownConnection(dial(number, videoState, extras));
        } catch (CallStateException e) {
            loge("pullExternalCall failed - " + e);
        }
    }

    private ImsException getImsManagerIsNullException() {
        return new ImsException("no ims manager", 102);
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean shouldDisconnectActiveCallOnAnswer(ImsCall activeCall, ImsCall incomingCall) {
        boolean z = false;
        if (activeCall == null || incomingCall == null || !this.mDropVideoCallWhenAnsweringAudioCall) {
            return false;
        }
        boolean isVoWifiEnabled;
        boolean isActiveCallVideo = !activeCall.isVideoCall() ? this.mTreatDowngradedVideoCallsAsVideoCalls ? activeCall.wasVideoCall() : false : true;
        boolean isActiveCallOnWifi = activeCall.isWifiCall();
        ImsManager imsManager = this.mImsManager;
        if (ImsManager.isWfcEnabledByPlatform(this.mPhone.getContext())) {
            imsManager = this.mImsManager;
            isVoWifiEnabled = ImsManager.isWfcEnabledByUser(this.mPhone.getContext());
        } else {
            isVoWifiEnabled = false;
        }
        boolean isIncomingCallAudio = incomingCall.isVideoCall() ^ 1;
        log("shouldDisconnectActiveCallOnAnswer : isActiveCallVideo=" + isActiveCallVideo + " isActiveCallOnWifi=" + isActiveCallOnWifi + " isIncomingCallAudio=" + isIncomingCallAudio + " isVowifiEnabled=" + isVoWifiEnabled);
        if (isActiveCallVideo && isActiveCallOnWifi && isIncomingCallAudio) {
            z = isVoWifiEnabled ^ 1;
        }
        return z;
    }

    public long getVtDataUsage() {
        if (this.mState != PhoneConstants.State.IDLE) {
            for (ImsPhoneConnection conn : this.mConnections) {
                VideoProvider videoProvider = conn.getVideoProvider();
                if (videoProvider != null) {
                    videoProvider.onRequestConnectionDataUsage();
                }
            }
        }
        return this.mTotalVtDataUsage;
    }

    public void registerPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.add(listener);
    }

    public void unregisterPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.remove(listener);
    }

    private void notifyPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State newState) {
        for (PhoneStateListener listener : this.mPhoneStateListeners) {
            listener.onPhoneStateChanged(oldState, newState);
        }
    }

    private void modifyVideoCall(ImsCall imsCall, int newVideoState) {
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            int oldVideoState = conn.getVideoState();
            if (conn.getVideoProvider() != null) {
                conn.getVideoProvider().onSendSessionModifyRequest(new VideoProfile(oldVideoState), new VideoProfile(newVideoState));
            }
        }
    }

    private void onDataEnabledChanged(boolean enabled, int reason) {
        log("onDataEnabledChanged: enabled=" + enabled + ", reason=" + reason);
        ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).setDataEnabled(enabled);
        this.mIsDataEnabled = enabled;
        if (this.mIgnoreDataEnabledChangedForVideoCalls) {
            log("Ignore data " + (enabled ? "enabled" : "disabled") + " due to carrier policy.");
        } else if (this.mIgnoreDataEnabledChangedForVideoCalls) {
            log("Ignore data " + (enabled ? "enabled" : "disabled") + " due to carrier policy.");
        } else {
            if (!enabled) {
                int reasonCode;
                if (reason == 3) {
                    reasonCode = 1405;
                } else if (reason == 2) {
                    reasonCode = 1406;
                } else {
                    reasonCode = 1406;
                }
                for (ImsPhoneConnection conn : this.mConnections) {
                    ImsCall imsCall = conn.getImsCall();
                    if (!(imsCall == null || !imsCall.isVideoCall() || (imsCall.isWifiCall() ^ 1) == 0)) {
                        if (conn.hasCapabilities(3)) {
                            if (reasonCode == 1406) {
                                conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED", null);
                            } else if (reasonCode == 1405) {
                                conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED", null);
                            }
                            modifyVideoCall(imsCall, 0);
                        } else if (this.mSupportPauseVideo) {
                            this.mShouldUpdateImsConfigOnDisconnect = true;
                            conn.pauseVideo(2);
                        } else {
                            try {
                                imsCall.terminate(RadioError.OEM_ERROR_1, reasonCode);
                            } catch (ImsException e) {
                                loge("Couldn't terminate call " + imsCall);
                            }
                        }
                    }
                }
            } else if (this.mSupportPauseVideo) {
                for (ImsPhoneConnection conn2 : this.mConnections) {
                    log("onDataEnabledChanged - resuming " + conn2);
                    if (VideoProfile.isPaused(conn2.getVideoState()) && conn2.wasVideoPausedFromSource(2)) {
                        conn2.resumeVideo(2);
                    }
                }
                this.mShouldUpdateImsConfigOnDisconnect = false;
            }
            if (!this.mShouldUpdateImsConfigOnDisconnect) {
                HwFrameworkFactory.updateImsServiceConfig(this.mPhone.getContext(), this.mPhone.getPhoneId(), true);
            }
        }
    }

    private void resetImsCapabilities() {
        log("Resetting Capabilities...");
        for (int i = 0; i < this.mImsFeatureEnabled.length; i++) {
            if (i != 4) {
                this.mImsFeatureEnabled[i] = false;
            }
        }
    }

    private boolean isWifiConnected() {
        boolean z = true;
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                if (ni.getType() != 1) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    public boolean isCarrierDowngradeOfVtCallSupported() {
        return this.mSupportDowngradeVtToAudio;
    }

    private void handleFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
        if (serviceClass == 1) {
            StringBuilder sb = new StringBuilder(120);
            sb.append("handleFeatureCapabilityChanged: ");
            int i = 0;
            while (i <= 5 && i < enabledFeatures.length) {
                if (enabledFeatures[i] == i) {
                    sb.append(this.mImsFeatureStrings[i]);
                    sb.append(":true ");
                    this.mImsFeatureEnabled[i] = true;
                } else if (enabledFeatures[i] == -1) {
                    sb.append(this.mImsFeatureStrings[i]);
                    sb.append(":false ");
                    this.mImsFeatureEnabled[i] = false;
                } else {
                    loge("handleFeatureCapabilityChanged(" + i + ", " + this.mImsFeatureStrings[i] + "): unexpectedValue=" + enabledFeatures[i]);
                }
                i++;
            }
            this.mPhone.notifyForVideoCapabilityChanged(isVideoCallEnabled());
            log(sb.toString());
            log("handleFeatureCapabilityChanged: isVolteEnabled=" + isVolteEnabled() + ", isVideoCallEnabled=" + isVideoCallEnabled() + ", isVowifiEnabled=" + isVowifiEnabled() + ", isUtEnabled=" + isUtEnabled());
            for (ImsPhoneConnection connection : this.mConnections) {
                connection.updateWifiState();
            }
            this.mPhone.onFeatureCapabilityChanged();
            this.mMetrics.writeOnImsCapabilities(this.mPhone.getPhoneId(), this.mImsFeatureEnabled);
        }
    }

    public void disableUTForQcom() {
        log("disableUTForQcom");
        this.mImsFeatureEnabled[4] = false;
        this.mImsFeatureEnabled[5] = false;
    }

    public void enableUTForQcom() {
        log("enableUTForQcom");
        this.mImsFeatureEnabled[4] = true;
        this.mImsFeatureEnabled[5] = true;
    }
}
