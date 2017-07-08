package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.content.Context;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.ProxyInfo;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TimeUtils;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DataConnection extends StateMachine {
    static final int BASE = 262144;
    private static final int CMD_TO_STRING_COUNT = 17;
    private static final boolean CUST_RETRY_CONFIG = false;
    private static final boolean DBG = true;
    private static final int EHRPD_MAX_RETRY = 2;
    static final int EVENT_BW_REFRESH_RESPONSE = 262158;
    static final int EVENT_CONNECT = 262144;
    static final int EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED = 262155;
    static final int EVENT_DATA_CONNECTION_ROAM_OFF = 262157;
    static final int EVENT_DATA_CONNECTION_ROAM_ON = 262156;
    static final int EVENT_DATA_CONNECTION_VOICE_CALL_ENDED = 262160;
    static final int EVENT_DATA_CONNECTION_VOICE_CALL_STARTED = 262159;
    static final int EVENT_DATA_STATE_CHANGED = 262151;
    static final int EVENT_DEACTIVATE_DONE = 262147;
    static final int EVENT_DISCONNECT = 262148;
    static final int EVENT_DISCONNECT_ALL = 262150;
    static final int EVENT_GET_LAST_FAIL_DONE = 262146;
    static final int EVENT_LOST_CONNECTION = 262153;
    static final int EVENT_RIL_CONNECTED = 262149;
    static final int EVENT_SETUP_DATA_CONNECTION_DONE = 262145;
    static final int EVENT_TEAR_DOWN_NOW = 262152;
    private static boolean HW_SET_EHRPD_DATA = false;
    private static final String NETWORK_TYPE = "MOBILE";
    private static final String NULL_IP = "0.0.0.0";
    private static final String TCP_BUFFER_SIZES_1XRTT = "16384,32768,131072,4096,16384,102400";
    private static final String TCP_BUFFER_SIZES_EDGE = "4093,26280,70800,4096,16384,70800";
    private static final String TCP_BUFFER_SIZES_EHRPD = "131072,262144,1048576,4096,16384,524288";
    private static final String TCP_BUFFER_SIZES_EVDO = "4094,87380,262144,4096,16384,262144";
    private static final String TCP_BUFFER_SIZES_GPRS = "4092,8760,48000,4096,8760,48000";
    private static final String TCP_BUFFER_SIZES_HSDPA = "61167,367002,1101005,8738,52429,262114";
    private static final String TCP_BUFFER_SIZES_HSPA = "40778,244668,734003,16777,100663,301990";
    private static final String TCP_BUFFER_SIZES_HSPAP = "122334,734003,2202010,32040,192239,576717";
    private static final String TCP_BUFFER_SIZES_LTE = "524288,4194304,8388608,262144,524288,1048576";
    private static final String TCP_BUFFER_SIZES_UMTS = "58254,349525,1048576,58254,349525,1048576";
    private static final boolean VDBG = true;
    private static AtomicInteger mInstanceNumber;
    private static String[] sCmdToString;
    private AsyncChannel mAc;
    private DcActivatingState mActivatingState;
    private DcActiveState mActiveState;
    public HashMap<ApnContext, ConnectionParams> mApnContexts;
    private ApnSetting mApnSetting;
    public int mCid;
    private ConnectionParams mConnectionParams;
    private long mCreateTime;
    private int mDataRegState;
    private DcController mDcController;
    private DcFailCause mDcFailCause;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcTracker mDct;
    private DcDefaultState mDefaultState;
    private DisconnectParams mDisconnectParams;
    private DcDisconnectionErrorCreatingConnection mDisconnectingErrorCreatingConnection;
    private DcDisconnectingState mDisconnectingState;
    private int mEhrpdFailCount;
    private HwCustDataConnection mHwCustDataConnection;
    private int mId;
    private DcInactiveState mInactiveState;
    private DcFailCause mLastFailCause;
    private long mLastFailTime;
    private LinkProperties mLinkProperties;
    private NetworkAgent mNetworkAgent;
    private NetworkInfo mNetworkInfo;
    protected String[] mPcscfAddr;
    private Phone mPhone;
    PendingIntent mReconnectIntent;
    private int mRilRat;
    int mTag;
    private Object mUserData;
    private boolean misLastFailed;

    /* renamed from: com.android.internal.telephony.dataconnection.DataConnection.1 */
    class AnonymousClass1 extends PrintWriter {
        AnonymousClass1(Writer $anonymous0) {
            super($anonymous0);
        }

        public void println(String s) {
            DataConnection.this.logd(s);
        }

        public void flush() {
        }
    }

    public static class ConnectionParams {
        ApnContext mApnContext;
        final int mConnectionGeneration;
        Message mOnCompletedMsg;
        int mProfileId;
        int mRilRat;
        int mTag;
        boolean mdefered;

        ConnectionParams(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration) {
            this.mApnContext = apnContext;
            this.mProfileId = profileId;
            this.mRilRat = rilRadioTechnology;
            this.mOnCompletedMsg = onCompletedMsg;
            this.mConnectionGeneration = connectionGeneration;
            this.mdefered = DataConnection.CUST_RETRY_CONFIG;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mProfileId=" + this.mProfileId + " mRat=" + this.mRilRat + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    private class DcActivatingState extends State {
        private static final /* synthetic */ int[] -com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$dataconnection$DataCallResponse$SetupResult;

        private static /* synthetic */ int[] -getcom-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues() {
            if (-com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues != null) {
                return -com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues;
            }
            int[] iArr = new int[SetupResult.values().length];
            try {
                iArr[SetupResult.ERR_BadCommand.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SetupResult.ERR_GetLastErrorFromRil.ordinal()] = DataConnection.EHRPD_MAX_RETRY;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SetupResult.ERR_RilError.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SetupResult.ERR_Stale.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SetupResult.ERR_UnacceptableParameter.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SetupResult.SUCCESS.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            -com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues = iArr;
            return iArr;
        }

        private DcActivatingState() {
        }

        public void enter() {
            for (ApnContext apnContext : DataConnection.this.mApnContexts.keySet()) {
                if (apnContext.getState() == DctConstants.State.RETRYING) {
                    DataConnection.this.log("DcActivatingState: Set Retrying To Connecting!");
                    apnContext.setState(DctConstants.State.CONNECTING);
                }
            }
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcActivatingState: msg=" + DataConnection.msgToString(msg));
            AsyncResult ar;
            ConnectionParams cp;
            switch (msg.what) {
                case DataConnection.EVENT_CONNECT /*262144*/:
                    msg.obj.mdefered = DataConnection.VDBG;
                    DataConnection.this.deferMessage(msg);
                    return DataConnection.VDBG;
                case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /*262145*/:
                    ar = msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    SetupResult result = DataConnection.this.onSetupConnectionCompleted(ar);
                    if (!(result == SetupResult.ERR_Stale || DataConnection.this.mConnectionParams == cp)) {
                        DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                    }
                    DataConnection.this.log("DcActivatingState onSetupConnectionCompleted result=" + result);
                    if (cp.mApnContext != null) {
                        cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                    }
                    if (result != SetupResult.SUCCESS) {
                        VSimUtilsInner.checkMmsStop(DataConnection.this.mPhone.getPhoneId());
                    }
                    if (!(result == SetupResult.ERR_RilError && DataConnection.this.mRilRat == 13)) {
                        DataConnection.this.mEhrpdFailCount = 0;
                    }
                    switch (-getcom-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues()[result.ordinal()]) {
                        case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                            DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                            break;
                        case DataConnection.EHRPD_MAX_RETRY /*2*/:
                            DataConnection.this.mPhone.mCi.getLastDataCallFailCause(DataConnection.this.obtainMessage(DataConnection.EVENT_GET_LAST_FAIL_DONE, cp));
                            break;
                        case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                            long delay = DataConnection.this.getSuggestedRetryDelay(ar);
                            cp.mApnContext.setModemSuggestedDelay(delay);
                            String str = "DcActivatingState: ERR_RilError  delay=" + delay + " result=" + result + " result.isRestartRadioFail=" + result.mFailCause.isRestartRadioFail() + " result.isPermanentFail=" + DataConnection.this.mDct.isPermanentFail(result.mFailCause);
                            DataConnection.this.log(str);
                            if (cp.mApnContext != null) {
                                cp.mApnContext.requestLog(str);
                            }
                            DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                            if (DataConnection.HW_SET_EHRPD_DATA && DataConnection.this.mDct.isCTSimCard(DataConnection.this.mPhone.getPhoneId()) && DataConnection.this.mRilRat == 13 && !result.mFailCause.isRestartRadioFail()) {
                                String apnContextType = cp.mApnContext.getApnType();
                                if ("default".equals(apnContextType) || "mms".equals(apnContextType)) {
                                    if (DataConnection.this.mEhrpdFailCount < DataConnection.EHRPD_MAX_RETRY && !result.mFailCause.isPermanentFail()) {
                                        DataConnection dataConnection = DataConnection.this;
                                        dataConnection.mEhrpdFailCount = dataConnection.mEhrpdFailCount + 1;
                                        break;
                                    }
                                    DataConnection.this.mPhone.mCi.setEhrpdByQMI(DataConnection.CUST_RETRY_CONFIG);
                                    DataConnection.this.mEhrpdFailCount = 0;
                                    DataConnection.this.logd("ehrpd fail times reaches EHRPD_MAX_RETRY or permanent fail ,disable eHRPD.");
                                    break;
                                }
                            }
                            break;
                        case CharacterSets.ISO_8859_1 /*4*/:
                            DataConnection.this.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                            break;
                        case CharacterSets.ISO_8859_2 /*5*/:
                            DataConnection.this.tearDownData(cp);
                            DataConnection.this.transitionTo(DataConnection.this.mDisconnectingErrorCreatingConnection);
                            break;
                        case CharacterSets.ISO_8859_3 /*6*/:
                            DataConnection.this.mDcFailCause = DcFailCause.NONE;
                            DataConnection.this.transitionTo(DataConnection.this.mActiveState);
                            break;
                        default:
                            throw new RuntimeException("Unknown SetupResult, should not happen");
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_GET_LAST_FAIL_DONE /*262146*/:
                    ar = (AsyncResult) msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    if (cp.mTag == DataConnection.this.mTag) {
                        if (DataConnection.this.mConnectionParams != cp) {
                            DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                        }
                        DcFailCause cause = DcFailCause.UNKNOWN;
                        if (ar.exception == null) {
                            int rilFailCause = ((int[]) ar.result)[0];
                            if (DataConnection.this.mPhone.getPhoneType() == DataConnection.EHRPD_MAX_RETRY) {
                                HwTelephonyFactory.getHwDataServiceChrManager().getModemParamsWhenCdmaPdpActFail(DataConnection.this.mPhone, rilFailCause);
                            }
                            VSimUtilsInner.setLastRilFailCause(rilFailCause);
                            cause = DcFailCause.fromInt(rilFailCause);
                            if (cause == DcFailCause.NONE) {
                                DataConnection.this.log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE BAD: error was NONE, change to UNKNOWN");
                                cause = DcFailCause.UNKNOWN;
                            }
                        }
                        DataConnection.this.mDcFailCause = cause;
                        DataConnection.this.log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE cause=" + cause + " dc=" + DataConnection.this);
                        DataConnection.this.mInactiveState.setEnterNotificationParams(cp, cause);
                        DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                        if (DataConnection.this.mPhone.getPhoneType() == DataConnection.EHRPD_MAX_RETRY && SystemProperties.getBoolean("hw.dct.psrecovery", DataConnection.CUST_RETRY_CONFIG)) {
                            DataConnection.this.log("pdp active fail may need to restart rild");
                            DataConnection.this.mDct.sendDataSetupCompleteFailed();
                        }
                    } else {
                        DataConnection.this.loge("DcActivatingState: stale EVENT_GET_LAST_FAIL_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    DataConnection.this.deferMessage(msg);
                    return DataConnection.VDBG;
                default:
                    DataConnection.this.log("DcActivatingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                    return DataConnection.CUST_RETRY_CONFIG;
            }
        }
    }

    private class DcActiveState extends State {
        private DcActiveState() {
        }

        public void enter() {
            DataConnection.this.log("DcActiveState: enter dc=*");
            boolean createNetworkAgent = DataConnection.VDBG;
            if (DataConnection.this.hasMessages(DataConnection.EVENT_DISCONNECT) || DataConnection.this.hasMessages(DataConnection.EVENT_DISCONNECT_ALL) || DataConnection.this.hasDeferredMessages(DataConnection.EVENT_DISCONNECT) || DataConnection.this.hasDeferredMessages(DataConnection.EVENT_DISCONNECT_ALL)) {
                DataConnection.this.log("DcActiveState: skipping notifyAllOfConnected()");
                createNetworkAgent = DataConnection.CUST_RETRY_CONFIG;
            } else {
                DataConnection.this.notifyAllOfConnected(PhoneInternalInterface.REASON_CONNECTED);
            }
            if (DataConnection.this.mPhone.getCallTracker() != null) {
                DataConnection.this.mPhone.getCallTracker().registerForVoiceCallStarted(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED, null);
                DataConnection.this.mPhone.getCallTracker().registerForVoiceCallEnded(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED, null);
            }
            DataConnection.this.mDcController.addActiveDcByCid(DataConnection.this);
            DataConnection.this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), null);
            DataConnection.this.mNetworkInfo.setExtraInfo(DataConnection.this.mApnSetting.apn);
            DataConnection.this.updateTcpBufferSizes(DataConnection.this.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
            if (createNetworkAgent) {
                DataConnection.this.mNetworkAgent = new DcNetworkAgent(DataConnection.this.getHandler().getLooper(), DataConnection.this.mPhone.getContext(), "DcNetworkAgent", DataConnection.this.mNetworkInfo, DataConnection.this.makeNetworkCapabilities(), DataConnection.this.mLinkProperties, 50, misc);
            }
        }

        public void exit() {
            DataConnection.this.log("DcActiveState: exit dc=" + this);
            String reason = DataConnection.this.mNetworkInfo.getReason();
            if (DataConnection.this.mDcController.isExecutingCarrierChange()) {
                reason = PhoneInternalInterface.REASON_CARRIER_CHANGE;
            } else if (DataConnection.this.mDisconnectParams != null && DataConnection.this.mDisconnectParams.mReason != null) {
                reason = DataConnection.this.mDisconnectParams.mReason;
            } else if (DataConnection.this.mDcFailCause != null) {
                reason = DataConnection.this.mDcFailCause.toString();
            }
            if (DataConnection.this.mPhone.getCallTracker() != null) {
                DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallStarted(DataConnection.this.getHandler());
                DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallEnded(DataConnection.this.getHandler());
            }
            DataConnection.this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, reason, DataConnection.this.mNetworkInfo.getExtraInfo());
            if (DataConnection.this.mNetworkAgent != null) {
                DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                DataConnection.this.mNetworkAgent = null;
            }
        }

        public boolean processMessage(Message msg) {
            DisconnectParams dp;
            switch (msg.what) {
                case DataConnection.EVENT_CONNECT /*262144*/:
                    ConnectionParams cp = msg.obj;
                    DataConnection.this.mApnContexts.put(cp.mApnContext, cp);
                    DataConnection.this.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + DataConnection.this);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendRematchNetworkAndRequests(DataConnection.this.mNetworkInfo);
                    }
                    DataConnection.this.notifyConnectCompleted(cp, DcFailCause.NONE, DataConnection.CUST_RETRY_CONFIG);
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    dp = msg.obj;
                    DataConnection.this.log("DcActiveState: EVENT_DISCONNECT dp=*");
                    if (DataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                        DataConnection.this.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                        if (DataConnection.this.mApnContexts.size() == 1) {
                            DataConnection.this.mApnContexts.clear();
                            DataConnection.this.mDisconnectParams = dp;
                            DataConnection.this.mConnectionParams = null;
                            dp.mTag = DataConnection.this.mTag;
                            DataConnection.this.tearDownData(dp);
                            DataConnection.this.transitionTo(DataConnection.this.mDisconnectingState);
                        } else {
                            DataConnection.this.mApnContexts.remove(dp.mApnContext);
                            DataConnection.this.notifyDisconnectCompleted(dp, DataConnection.CUST_RETRY_CONFIG);
                        }
                    } else {
                        DataConnection.this.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + DataConnection.this);
                        DataConnection.this.notifyDisconnectCompleted(dp, DataConnection.CUST_RETRY_CONFIG);
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection.this.log("DcActiveState EVENT_DISCONNECT clearing apn contexts, dc=" + DataConnection.this);
                    dp = (DisconnectParams) msg.obj;
                    DataConnection.this.mDisconnectParams = dp;
                    DataConnection.this.mConnectionParams = null;
                    dp.mTag = DataConnection.this.mTag;
                    DataConnection.this.tearDownData(dp);
                    DataConnection.this.transitionTo(DataConnection.this.mDisconnectingState);
                    return DataConnection.VDBG;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    DataConnection.this.log("DcActiveState EVENT_LOST_CONNECTION dc=" + DataConnection.this);
                    DataConnection.this.mInactiveState.setEnterNotificationParams(DcFailCause.LOST_CONNECTION);
                    DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    DataConnection.this.mNetworkInfo.setRoaming(DataConnection.VDBG);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    DataConnection.this.mNetworkInfo.setRoaming(DataConnection.CUST_RETRY_CONFIG);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_BW_REFRESH_RESPONSE /*262158*/:
                    AsyncResult ar = msg.obj;
                    if (ar.exception != null) {
                        DataConnection.this.log("EVENT_BW_REFRESH_RESPONSE: error ignoring, e=" + ar.exception);
                    } else {
                        int lceBwDownKbps = ((Integer) ar.result.get(0)).intValue();
                        NetworkCapabilities nc = DataConnection.this.makeNetworkCapabilities();
                        if (DataConnection.this.mPhone.getLceStatus() == 1) {
                            nc.setLinkDownstreamBandwidthKbps(lceBwDownKbps);
                            if (DataConnection.this.mNetworkAgent != null) {
                                DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc);
                            }
                        }
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED /*262159*/:
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED /*262160*/:
                    if (DataConnection.this.updateNetworkInfoSuspendState() && DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    return DataConnection.VDBG;
                default:
                    DataConnection.this.log("DcActiveState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return DataConnection.CUST_RETRY_CONFIG;
            }
        }
    }

    private class DcDefaultState extends State {
        private DcDefaultState() {
        }

        public void enter() {
            DataConnection.this.log("DcDefaultState: enter");
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOn(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_ON, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOff(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF, null);
            DataConnection.this.mDcController.addDc(DataConnection.this);
        }

        public void exit() {
            DataConnection.this.log("DcDefaultState: exit");
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(DataConnection.this.getHandler());
            DataConnection.this.mDcController.removeDc(DataConnection.this);
            if (DataConnection.this.mAc != null) {
                DataConnection.this.mAc.disconnected();
                DataConnection.this.mAc = null;
            }
            DataConnection.this.mApnContexts = null;
            DataConnection.this.mReconnectIntent = null;
            DataConnection.this.mDct = null;
            DataConnection.this.mApnSetting = null;
            DataConnection.this.mPhone = null;
            DataConnection.this.mLinkProperties = null;
            DataConnection.this.mLastFailCause = null;
            DataConnection.this.mUserData = null;
            DataConnection.this.mDcController = null;
            DataConnection.this.mDcTesterFailBringUpAll = null;
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcDefault msg=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
            boolean val;
            switch (msg.what) {
                case 69633:
                    if (DataConnection.this.mAc == null) {
                        DataConnection.this.mAc = new AsyncChannel();
                        DataConnection.this.mAc.connected(null, DataConnection.this.getHandler(), msg.replyTo);
                        DataConnection.this.log("DcDefaultState: FULL_CONNECTION reply connected");
                        DataConnection.this.mAc.replyToMessage(msg, 69634, 0, DataConnection.this.mId, "hi");
                        break;
                    }
                    DataConnection.this.log("Disconnecting to previous connection mAc=" + DataConnection.this.mAc);
                    DataConnection.this.mAc.replyToMessage(msg, 69634, 3);
                    break;
                case 69636:
                    DataConnection.this.log("DcDefault: CMD_CHANNEL_DISCONNECTED before quiting call dump");
                    DataConnection.this.dumpToLog();
                    DataConnection.this.quit();
                    break;
                case DataConnection.EVENT_CONNECT /*262144*/:
                    DataConnection.this.log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    DataConnection.this.notifyConnectCompleted(msg.obj, DcFailCause.UNKNOWN, DataConnection.CUST_RETRY_CONFIG);
                    break;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    DataConnection.this.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection.this.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT_ALL RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_TEAR_DOWN_NOW /*262152*/:
                    DataConnection.this.log("DcDefaultState EVENT_TEAR_DOWN_NOW");
                    DataConnection.this.mPhone.mCi.deactivateDataCall(DataConnection.this.mCid, 0, null);
                    break;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    DataConnection.this.logAndAddLogRec("DcDefaultState ignore EVENT_LOST_CONNECTION tag=" + msg.arg1 + ":mTag=" + DataConnection.this.mTag);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    Pair<Integer, Integer> drsRatPair = msg.obj.result;
                    DataConnection.this.mDataRegState = ((Integer) drsRatPair.first).intValue();
                    if (DataConnection.this.mRilRat != ((Integer) drsRatPair.second).intValue()) {
                        DataConnection.this.updateTcpBufferSizes(((Integer) drsRatPair.second).intValue());
                    }
                    DataConnection.this.mRilRat = ((Integer) drsRatPair.second).intValue();
                    DataConnection.this.log("DcDefaultState: EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED drs=" + DataConnection.this.mDataRegState + " mRilRat=" + DataConnection.this.mRilRat);
                    int networkType = DataConnection.this.mPhone.getServiceState().getDataNetworkType();
                    DataConnection.this.mNetworkInfo.setSubtype(networkType, TelephonyManager.getNetworkTypeName(networkType));
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.updateNetworkInfoSuspendState();
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.makeNetworkCapabilities());
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties);
                        break;
                    }
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    DataConnection.this.mNetworkInfo.setRoaming(DataConnection.VDBG);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    DataConnection.this.mNetworkInfo.setRoaming(DataConnection.CUST_RETRY_CONFIG);
                    break;
                case DcAsyncChannel.REQ_IS_INACTIVE /*266240*/:
                    val = DataConnection.this.getIsInactive();
                    DataConnection.this.log("REQ_IS_INACTIVE  isInactive=" + val);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_IS_INACTIVE, val ? 1 : 0);
                    break;
                case DcAsyncChannel.REQ_GET_CID /*266242*/:
                    int cid = DataConnection.this.getCid();
                    DataConnection.this.log("REQ_GET_CID  cid=" + cid);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_CID, cid);
                    break;
                case DcAsyncChannel.REQ_GET_APNSETTING /*266244*/:
                    ApnSetting apnSetting = DataConnection.this.getApnSetting();
                    DataConnection.this.log("REQ_GET_APNSETTING  mApnSetting=" + apnSetting);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_APNSETTING, apnSetting);
                    break;
                case DcAsyncChannel.REQ_GET_LINK_PROPERTIES /*266246*/:
                    LinkProperties lp = DataConnection.this.getCopyLinkProperties();
                    DataConnection.this.log("REQ_GET_LINK_PROPERTIES linkProperties");
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_LINK_PROPERTIES, lp);
                    break;
                case DcAsyncChannel.REQ_SET_LINK_PROPERTIES_HTTP_PROXY /*266248*/:
                    ProxyInfo proxy = msg.obj;
                    DataConnection.this.log("REQ_SET_LINK_PROPERTIES_HTTP_PROXY proxy=" + proxy);
                    DataConnection.this.setLinkPropertiesHttpProxy(proxy);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_SET_LINK_PROPERTIES_HTTP_PROXY);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties);
                        break;
                    }
                    break;
                case DcAsyncChannel.REQ_GET_NETWORK_CAPABILITIES /*266250*/:
                    NetworkCapabilities nc = DataConnection.this.getCopyNetworkCapabilities();
                    DataConnection.this.log("REQ_GET_NETWORK_CAPABILITIES networkCapabilities" + nc);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_NETWORK_CAPABILITIES, nc);
                    break;
                case DcAsyncChannel.REQ_RESET /*266252*/:
                    DataConnection.this.log("DcDefaultState: msg.what=REQ_RESET");
                    DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    break;
                case DcAsyncChannel.REQ_CHECK_APNCONTEXT /*266254*/:
                    val = DataConnection.this.checkApnContext((ApnContext) msg.obj);
                    DataConnection.this.log("REQ_CHECK_APNCONTEXT  checkApnContext=" + val);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_CHECK_APNCONTEXT, val ? 1 : 0);
                    break;
                default:
                    DataConnection.this.log("DcDefaultState: shouldn't happen but ignore msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    break;
            }
            return DataConnection.VDBG;
        }
    }

    private class DcDisconnectingState extends State {
        private DcDisconnectingState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case DataConnection.EVENT_CONNECT /*262144*/:
                    DataConnection.this.log("DcDisconnectingState msg.what=EVENT_CONNECT. Defer. RefCount = " + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DEACTIVATE_DONE /*262147*/:
                    AsyncResult ar = msg.obj;
                    DisconnectParams dp = ar.userObj;
                    String str = "DcDisconnectingState msg.what=EVENT_DEACTIVATE_DONE RefCount=" + DataConnection.this.mApnContexts.size();
                    DataConnection.this.log(str);
                    if (dp.mApnContext != null) {
                        dp.mApnContext.requestLog(str);
                    }
                    if (dp.mTag == DataConnection.this.mTag) {
                        DataConnection.this.mInactiveState.setEnterNotificationParams((DisconnectParams) ar.userObj);
                        DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    } else {
                        DataConnection.this.log("DcDisconnectState stale EVENT_DEACTIVATE_DONE dp.tag=" + dp.mTag + " mTag=" + DataConnection.this.mTag);
                    }
                    return DataConnection.VDBG;
                default:
                    DataConnection.this.log("DcDisconnectingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return DataConnection.CUST_RETRY_CONFIG;
            }
        }
    }

    private class DcDisconnectionErrorCreatingConnection extends State {
        private DcDisconnectionErrorCreatingConnection() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case DataConnection.EVENT_DEACTIVATE_DONE /*262147*/:
                    ConnectionParams cp = msg.obj.userObj;
                    if (cp.mTag == DataConnection.this.mTag) {
                        String str = "DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE";
                        DataConnection.this.log(str);
                        if (cp.mApnContext != null) {
                            cp.mApnContext.requestLog(str);
                        }
                        DataConnection.this.mInactiveState.setEnterNotificationParams(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER);
                        DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    } else {
                        DataConnection.this.log("DcDisconnectionErrorCreatingConnection stale EVENT_DEACTIVATE_DONE dp.tag=" + cp.mTag + ", mTag=" + DataConnection.this.mTag);
                    }
                    return DataConnection.VDBG;
                default:
                    DataConnection.this.log("DcDisconnectionErrorCreatingConnection not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return DataConnection.CUST_RETRY_CONFIG;
            }
        }
    }

    private class DcInactiveState extends State {
        private DcInactiveState() {
        }

        public void setEnterNotificationParams(ConnectionParams cp, DcFailCause cause) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams cp,cause");
            DataConnection.this.mConnectionParams = cp;
            DataConnection.this.mDisconnectParams = null;
            DataConnection.this.mDcFailCause = cause;
        }

        public void setEnterNotificationParams(DisconnectParams dp) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams dp");
            DataConnection.this.mConnectionParams = null;
            DataConnection.this.mDisconnectParams = dp;
            DataConnection.this.mDcFailCause = DcFailCause.NONE;
        }

        public void setEnterNotificationParams(DcFailCause cause) {
            DataConnection.this.mConnectionParams = null;
            DataConnection.this.mDisconnectParams = null;
            DataConnection.this.mDcFailCause = cause;
        }

        public void enter() {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mTag++;
            DataConnection.this.log("DcInactiveState: enter() mTag=" + DataConnection.this.mTag);
            if (DataConnection.this.mConnectionParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyConnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                if (DataConnection.this.mDcFailCause != DcFailCause.NONE) {
                    DataConnection.this.misLastFailed = DataConnection.VDBG;
                }
                DataConnection.this.notifyConnectCompleted(DataConnection.this.mConnectionParams, DataConnection.this.mDcFailCause, DataConnection.VDBG);
            }
            if (DataConnection.this.mDisconnectParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyDisconnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyDisconnectCompleted(DataConnection.this.mDisconnectParams, DataConnection.VDBG);
            }
            if (DataConnection.this.mDisconnectParams == null && DataConnection.this.mConnectionParams == null && DataConnection.this.mDcFailCause != null) {
                DataConnection.this.log("DcInactiveState: enter notifyAllDisconnectCompleted failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyAllDisconnectCompleted(DataConnection.this.mDcFailCause);
            }
            DataConnection.this.mDcController.removeActiveDcByCid(DataConnection.this);
            DataConnection.this.clearSettings();
        }

        public void exit() {
            DataConnection.this.misLastFailed = DataConnection.CUST_RETRY_CONFIG;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case DataConnection.EVENT_CONNECT /*262144*/:
                    DataConnection.this.log("DcInactiveState: mag.what=EVENT_CONNECT");
                    ConnectionParams cp = msg.obj;
                    if (DataConnection.this.misLastFailed && cp.mdefered) {
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT apnContext with defefed msg, not process ");
                        DataConnection.this.notifyConnectCompleted(cp, DcFailCause.UNKNOWN, DataConnection.CUST_RETRY_CONFIG);
                    } else if (DataConnection.this.initConnection(cp)) {
                        DataConnection.this.onConnect(DataConnection.this.mConnectionParams);
                        DataConnection.this.transitionTo(DataConnection.this.mActivatingState);
                    } else {
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT initConnection failed");
                        DataConnection.this.notifyConnectCompleted(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER, DataConnection.CUST_RETRY_CONFIG);
                    }
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, DataConnection.CUST_RETRY_CONFIG);
                    return DataConnection.VDBG;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT_ALL");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, DataConnection.CUST_RETRY_CONFIG);
                    return DataConnection.VDBG;
                case DcAsyncChannel.REQ_RESET /*266252*/:
                    DataConnection.this.log("DcInactiveState: msg.what=RSP_RESET, ignore we're already reset");
                    return DataConnection.VDBG;
                default:
                    DataConnection.this.log("DcInactiveState nothandled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return DataConnection.CUST_RETRY_CONFIG;
            }
        }
    }

    private class DcNetworkAgent extends NetworkAgent {
        public DcNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (DataConnection.this.mNetworkAgent != this) {
                log("DcNetworkAgent: unwanted found mNetworkAgent=" + DataConnection.this.mNetworkAgent + ", which isn't me.  Aborting unwanted");
            } else if (DataConnection.this.mApnContexts != null) {
                for (ConnectionParams cp : DataConnection.this.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    Pair<ApnContext, Integer> pair = new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration));
                    log("DcNetworkAgent: [unwanted]: disconnect apnContext=" + apnContext + ". And no retry it after disconnected");
                    apnContext.setReason(AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT);
                    DataConnection.this.sendMessage(DataConnection.this.obtainMessage(DataConnection.EVENT_DISCONNECT, new DisconnectParams(apnContext, apnContext.getReason(), DataConnection.this.mDct.obtainMessage(270351, pair))));
                }
            }
        }

        protected void pollLceData() {
            if (DataConnection.this.mPhone.getLceStatus() == 1) {
                DataConnection.this.mPhone.mCi.pullLceData(DataConnection.this.obtainMessage(DataConnection.EVENT_BW_REFRESH_RESPONSE));
            }
        }

        protected void networkStatus(int status, String redirectUrl) {
            if (!TextUtils.isEmpty(redirectUrl)) {
                log("validation status: " + status + " with redirection URL: " + redirectUrl);
                Message msg = DataConnection.this.mDct.obtainMessage(270380, redirectUrl);
                AsyncResult.forMessage(msg, DataConnection.this.mApnContexts, null);
                msg.sendToTarget();
            }
        }
    }

    public static class DisconnectParams {
        public ApnContext mApnContext;
        Message mOnCompletedMsg;
        String mReason;
        int mTag;

        DisconnectParams(ApnContext apnContext, String reason, Message onCompletedMsg) {
            this.mApnContext = apnContext;
            this.mReason = reason;
            this.mOnCompletedMsg = onCompletedMsg;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mReason=" + this.mReason + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    public static class UpdateLinkPropertyResult {
        public LinkProperties newLp;
        public LinkProperties oldLp;
        public SetupResult setupResult;

        public UpdateLinkPropertyResult(LinkProperties curLp) {
            this.setupResult = SetupResult.SUCCESS;
            this.oldLp = curLp;
            this.newLp = curLp;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DataConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DataConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.<clinit>():void");
    }

    static String cmdToString(int cmd) {
        String value;
        cmd -= EVENT_CONNECT;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            value = DcAsyncChannel.cmdToString(cmd + EVENT_CONNECT);
        } else {
            value = sCmdToString[cmd];
        }
        if (value == null) {
            return "0x" + Integer.toHexString(cmd + EVENT_CONNECT);
        }
        return value;
    }

    public static DataConnection makeDataConnection(Phone phone, int id, DcTracker dct, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        DataConnection dc = new DataConnection(phone, "DC-" + mInstanceNumber.incrementAndGet(), id, dct, failBringUpAll, dcc);
        dc.start();
        dc.log("Made " + dc.getName());
        return dc;
    }

    void dispose() {
        log("dispose: call quiteNow()");
        quitNow();
    }

    NetworkCapabilities getCopyNetworkCapabilities() {
        return makeNetworkCapabilities();
    }

    LinkProperties getCopyLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    boolean getIsInactive() {
        return getCurrentState() == this.mInactiveState ? VDBG : CUST_RETRY_CONFIG;
    }

    int getCid() {
        return this.mCid;
    }

    ApnSetting getApnSetting() {
        return this.mApnSetting;
    }

    boolean checkApnContext(ApnContext apnContext) {
        if (apnContext == null) {
            return CUST_RETRY_CONFIG;
        }
        return this.mApnContexts.containsKey(apnContext);
    }

    void setLinkPropertiesHttpProxy(ProxyInfo proxy) {
        this.mLinkProperties.setHttpProxy(proxy);
    }

    public boolean isIpv4Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet4Address) {
                Inet4Address i4addr = (Inet4Address) addr;
                if (!(i4addr.isAnyLocalAddress() || i4addr.isLinkLocalAddress() || i4addr.isLoopbackAddress() || i4addr.isMulticastAddress())) {
                    return VDBG;
                }
            }
        }
        return CUST_RETRY_CONFIG;
    }

    public boolean isIpv6Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet6Address) {
                Inet6Address i6addr = (Inet6Address) addr;
                if (!(i6addr.isAnyLocalAddress() || i6addr.isLinkLocalAddress() || i6addr.isLoopbackAddress() || i6addr.isMulticastAddress())) {
                    return VDBG;
                }
            }
        }
        return CUST_RETRY_CONFIG;
    }

    public UpdateLinkPropertyResult updateLinkProperty(DataCallResponse newState) {
        UpdateLinkPropertyResult result = new UpdateLinkPropertyResult(this.mLinkProperties);
        if (newState == null) {
            return result;
        }
        result.newLp = new LinkProperties();
        result.setupResult = setLinkProperties(newState, result.newLp);
        if (result.setupResult != SetupResult.SUCCESS) {
            log("updateLinkProperty failed : " + result.setupResult);
            return result;
        }
        result.newLp.setHttpProxy(this.mLinkProperties.getHttpProxy());
        checkSetMtu(this.mApnSetting, result.newLp);
        this.mLinkProperties = result.newLp;
        updateTcpBufferSizes(this.mRilRat);
        if (!result.oldLp.equals(result.newLp)) {
            log("updateLinkProperty old LP=*");
        }
        if (!(result.newLp.equals(result.oldLp) || this.mNetworkAgent == null)) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        return result;
    }

    private void checkSetMtu(ApnSetting apn, LinkProperties lp) {
        if (lp != null && apn != null && lp != null) {
            if (lp.getMtu() != 0) {
                log("MTU set by call response to: " + lp.getMtu());
            } else if (this.mHwCustDataConnection != null && this.mHwCustDataConnection.setMtuIfNeeded(lp, this.mPhone)) {
            } else {
                if (apn != null && apn.mtu != 0) {
                    lp.setMtu(apn.mtu);
                    log("MTU set by APN to: " + apn.mtu);
                } else if (this.mDct.isCTSimCard(this.mPhone.getPhoneId())) {
                    log("MTU not set in CT Card");
                } else {
                    int mtu = this.mPhone.getContext().getResources().getInteger(17694864);
                    if (mtu != 0) {
                        lp.setMtu(mtu);
                        log("MTU set by config resource to: " + mtu);
                    }
                }
            }
        }
    }

    private DataConnection(Phone phone, String name, int id, DcTracker dct, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super(name, dcc.getHandler());
        this.mEhrpdFailCount = 0;
        this.mDct = null;
        this.mLinkProperties = new LinkProperties();
        this.mRilRat = Integer.MAX_VALUE;
        this.mDataRegState = Integer.MAX_VALUE;
        this.misLastFailed = CUST_RETRY_CONFIG;
        this.mApnContexts = null;
        this.mReconnectIntent = null;
        this.mDefaultState = new DcDefaultState();
        this.mInactiveState = new DcInactiveState();
        this.mActivatingState = new DcActivatingState();
        this.mActiveState = new DcActiveState();
        this.mDisconnectingState = new DcDisconnectingState();
        this.mDisconnectingErrorCreatingConnection = new DcDisconnectionErrorCreatingConnection();
        setLogRecSize(300);
        setLogOnlyTransitions(VDBG);
        log("DataConnection created");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDcTesterFailBringUpAll = failBringUpAll;
        this.mDcController = dcc;
        this.mId = id;
        this.mCid = -1;
        ServiceState ss = this.mPhone.getServiceState();
        this.mRilRat = ss.getRilDataRadioTechnology();
        this.mDataRegState = this.mPhone.getServiceState().getDataRegState();
        int networkType = ss.getDataNetworkType();
        this.mNetworkInfo = new NetworkInfo(0, networkType, NETWORK_TYPE, TelephonyManager.getNetworkTypeName(networkType));
        this.mNetworkInfo.setRoaming(ss.getDataRoaming());
        this.mNetworkInfo.setIsAvailable(VDBG);
        addState(this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        addState(this.mDisconnectingState, this.mDefaultState);
        addState(this.mDisconnectingErrorCreatingConnection, this.mDefaultState);
        setInitialState(this.mInactiveState);
        this.mApnContexts = new HashMap();
        this.mHwCustDataConnection = (HwCustDataConnection) HwCustUtils.createObj(HwCustDataConnection.class, new Object[0]);
    }

    private void onConnect(ConnectionParams cp) {
        log("onConnect: carrier='" + this.mApnSetting.carrier + "' APN='" + this.mApnSetting.apn + "' proxy='" + this.mApnSetting.proxy + "' port='" + this.mApnSetting.port + "'");
        if (cp.mApnContext != null) {
            cp.mApnContext.requestLog("DataConnection.onConnect");
        }
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            DataCallResponse response = new DataCallResponse();
            response.version = this.mPhone.mCi.getRilVersion();
            response.status = this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause.getErrorCode();
            response.cid = 0;
            response.active = 0;
            response.type = "";
            response.ifname = "";
            response.addresses = new String[0];
            response.dnses = new String[0];
            response.gateways = new String[0];
            response.suggestedRetryTime = this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime;
            response.pcscf = new String[0];
            response.mtu = 0;
            Message msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
            AsyncResult.forMessage(msg, response, null);
            sendMessage(msg);
            log("onConnect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + response);
            DcFailBringUp dcFailBringUp = this.mDcTesterFailBringUpAll.getDcFailBringUp();
            dcFailBringUp.mCounter--;
            return;
        }
        String protocol;
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg.obj = cp;
        int authType = this.mApnSetting.authType;
        if (authType == -1) {
            if (TextUtils.isEmpty(this.mApnSetting.user)) {
                authType = 0;
            } else {
                authType = 3;
            }
        }
        if (this.mPhone.getServiceState().getDataRoamingFromRegistration()) {
            protocol = this.mApnSetting.roamingProtocol;
        } else {
            protocol = this.mApnSetting.protocol;
        }
        HwTelephonyFactory.getHwDataServiceChrManager().setPdpActiveIpType(protocol, this.mPhone.getSubId());
        String mApn = this.mApnSetting.apn;
        if (!(this.mHwCustDataConnection == null || !this.mHwCustDataConnection.whetherSetApnByCust(this.mPhone) || HwModemCapability.isCapabilitySupport(9))) {
            mApn = "";
        }
        this.mPhone.mCi.setupDataCall(cp.mRilRat, cp.mProfileId, mApn, this.mApnSetting.user, this.mApnSetting.password, authType, protocol, msg);
    }

    private void tearDownData(Object o) {
        int discReason = 0;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DisconnectParams)) {
            DisconnectParams dp = (DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RADIO_TURNED_OFF)) {
                discReason = 1;
            } else if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_PDP_RESET)) {
                discReason = EHRPD_MAX_RETRY;
            }
        }
        String str;
        if (this.mPhone.mCi.getRadioState().isOn() || this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            str = "tearDownData radio is on, call deactivateDataCall";
            log(str);
            if (apnContext != null) {
                apnContext.requestLog(str);
            }
            this.mPhone.mCi.deactivateDataCall(this.mCid, discReason, obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, o));
        } else {
            str = "tearDownData radio is off sendMessage EVENT_DEACTIVATE_DONE immediately";
            log(str);
            if (apnContext != null) {
                apnContext.requestLog(str);
            }
            sendMessage(obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, new AsyncResult(o, null, null)));
        }
        VSimUtilsInner.checkMmsStop(this.mPhone.getPhoneId());
    }

    private void notifyAllWithEvent(ApnContext alreadySent, int event, String reason) {
        this.mNetworkInfo.setDetailedState(this.mNetworkInfo.getDetailedState(), reason, this.mNetworkInfo.getExtraInfo());
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext != alreadySent) {
                if (reason != null) {
                    apnContext.setReason(reason);
                }
                Message msg = this.mDct.obtainMessage(event, new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration)));
                AsyncResult.forMessage(msg);
                msg.sendToTarget();
            }
        }
    }

    private void notifyAllOfConnected(String reason) {
        notifyAllWithEvent(null, 270336, reason);
    }

    private void notifyAllOfDisconnectDcRetrying(String reason) {
        notifyAllWithEvent(null, 270370, reason);
    }

    private void notifyAllDisconnectCompleted(DcFailCause cause) {
        notifyAllWithEvent(null, 270351, cause.toString());
    }

    private void notifyConnectCompleted(ConnectionParams cp, DcFailCause cause, boolean sendAll) {
        ApnContext apnContext = null;
        if (!(cp == null || cp.mOnCompletedMsg == null)) {
            Message connectionCompletedMsg = cp.mOnCompletedMsg;
            cp.mOnCompletedMsg = null;
            apnContext = cp.mApnContext;
            long timeStamp = System.currentTimeMillis();
            connectionCompletedMsg.arg1 = this.mCid;
            if (cause == DcFailCause.NONE) {
                this.mCreateTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg);
            } else {
                this.mLastFailCause = cause;
                this.mLastFailTime = timeStamp;
                if (cause == null) {
                    cause = DcFailCause.UNKNOWN;
                }
                cp.mApnContext.setPdpFailCause(cause);
                AsyncResult.forMessage(connectionCompletedMsg, cause, new Throwable(cause.toString()));
            }
            log("notifyConnectCompleted at " + timeStamp + " cause=" + cause + " connectionCompletedMsg=" + msgToString(connectionCompletedMsg));
            connectionCompletedMsg.sendToTarget();
        }
        if (sendAll) {
            log("Send to all. " + apnContext + " " + cause.toString());
            notifyAllWithEvent(apnContext, 270371, cause.toString());
        }
    }

    private void notifyDisconnectCompleted(DisconnectParams dp, boolean sendAll) {
        log("NotifyDisconnectCompleted");
        ApnContext apnContext = null;
        String str = null;
        if (!(dp == null || dp.mOnCompletedMsg == null)) {
            Message msg = dp.mOnCompletedMsg;
            dp.mOnCompletedMsg = null;
            if (msg.obj instanceof ApnContext) {
                apnContext = msg.obj;
            }
            str = dp.mReason;
            String str2 = "msg=%s msg.obj=%s";
            Object[] objArr = new Object[EHRPD_MAX_RETRY];
            objArr[0] = msg.toString();
            objArr[1] = msg.obj instanceof String ? (String) msg.obj : "<no-reason>";
            log(String.format(str2, objArr));
            AsyncResult.forMessage(msg);
            msg.sendToTarget();
        }
        if (sendAll) {
            if (str == null) {
                str = DcFailCause.UNKNOWN.toString();
            }
            notifyAllWithEvent(apnContext, 270351, str);
        }
        log("NotifyDisconnectCompleted DisconnectParams=*");
    }

    public int getDataConnectionId() {
        return this.mId;
    }

    private void clearSettings() {
        log("clearSettings");
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        this.mCid = -1;
        this.mPcscfAddr = new String[5];
        this.mLinkProperties = new LinkProperties();
        this.mApnContexts.clear();
        this.mApnSetting = null;
        this.mDcFailCause = null;
        this.mEhrpdFailCount = 0;
    }

    private SetupResult onSetupConnectionCompleted(AsyncResult ar) {
        DataCallResponse response = ar.result;
        ConnectionParams cp = ar.userObj;
        if (cp.mTag != this.mTag) {
            log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            return SetupResult.ERR_Stale;
        } else if (ar.exception != null) {
            log("onSetupConnectionCompleted failed, ar.exception=" + ar.exception + " response=" + response);
            if ((ar.exception instanceof CommandException) && ((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
                result = SetupResult.ERR_BadCommand;
                result.mFailCause = DcFailCause.RADIO_NOT_AVAILABLE;
                return result;
            } else if (response == null || response.version < 4) {
                return SetupResult.ERR_GetLastErrorFromRil;
            } else {
                result = SetupResult.ERR_RilError;
                result.mFailCause = DcFailCause.fromInt(response.status);
                return result;
            }
        } else if (response.status != 0) {
            result = SetupResult.ERR_RilError;
            result.mFailCause = DcFailCause.fromInt(response.status);
            return result;
        } else {
            log("onSetupConnectionCompleted received successful DataCallResponse");
            this.mCid = response.cid;
            this.mPcscfAddr = response.pcscf;
            result = updateLinkProperty(response).setupResult;
            if (cp.mApnContext.getApnType().equals("mms")) {
                return result;
            }
            HwTelephonyFactory.getHwDataServiceChrManager().SendIntentDNSfailure(response.dnses);
            return result;
        }
    }

    private void updateTcpBufferSizes(int rilRat) {
        String[] configOverride;
        String[] split;
        String tcpBufferSizePropName;
        String sizes = null;
        String ratName = ServiceState.rilRadioTechnologyToString(rilRat).toLowerCase(Locale.ROOT);
        if (!(rilRat == 7 || rilRat == 8)) {
            String custTcpBuffer;
            if (rilRat == 12) {
            }
            configOverride = this.mPhone.getContext().getResources().getStringArray(17236019);
            for (String split2 : configOverride) {
                split = split2.split(":");
                if (!ratName.equals(split[0]) && split.length == EHRPD_MAX_RETRY) {
                    sizes = split[1];
                    break;
                }
            }
            tcpBufferSizePropName = "hw.net.tcp.buffersize.";
            if (sizes == null) {
                switch (rilRat) {
                    case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                        sizes = TCP_BUFFER_SIZES_GPRS;
                        tcpBufferSizePropName = tcpBufferSizePropName + "gprs";
                        break;
                    case EHRPD_MAX_RETRY /*2*/:
                        sizes = TCP_BUFFER_SIZES_EDGE;
                        tcpBufferSizePropName = tcpBufferSizePropName + "edge";
                        break;
                    case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                    case CMD_TO_STRING_COUNT /*17*/:
                        sizes = TCP_BUFFER_SIZES_UMTS;
                        tcpBufferSizePropName = tcpBufferSizePropName + "umts";
                        break;
                    case CharacterSets.ISO_8859_3 /*6*/:
                        sizes = TCP_BUFFER_SIZES_1XRTT;
                        tcpBufferSizePropName = tcpBufferSizePropName + "1xrtt";
                        break;
                    case CharacterSets.ISO_8859_4 /*7*/:
                    case CharacterSets.ISO_8859_5 /*8*/:
                    case CharacterSets.ISO_8859_9 /*12*/:
                        sizes = TCP_BUFFER_SIZES_EVDO;
                        tcpBufferSizePropName = tcpBufferSizePropName + "evdo";
                        break;
                    case CharacterSets.ISO_8859_6 /*9*/:
                        sizes = TCP_BUFFER_SIZES_HSDPA;
                        tcpBufferSizePropName = tcpBufferSizePropName + "hsdpa";
                        break;
                    case CharacterSets.ISO_8859_7 /*10*/:
                    case CharacterSets.ISO_8859_8 /*11*/:
                        sizes = TCP_BUFFER_SIZES_HSPA;
                        tcpBufferSizePropName = tcpBufferSizePropName + "hspa";
                        break;
                    case UserData.ASCII_CR_INDEX /*13*/:
                        sizes = TCP_BUFFER_SIZES_EHRPD;
                        tcpBufferSizePropName = tcpBufferSizePropName + "ehrpd";
                        break;
                    case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                        sizes = TCP_BUFFER_SIZES_LTE;
                        tcpBufferSizePropName = tcpBufferSizePropName + "lte";
                        break;
                    case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                        sizes = TCP_BUFFER_SIZES_HSPAP;
                        tcpBufferSizePropName = tcpBufferSizePropName + "hspap";
                        break;
                }
            }
            custTcpBuffer = SystemProperties.get(tcpBufferSizePropName);
            log("custTcpBuffer = " + custTcpBuffer);
            if (custTcpBuffer != null && custTcpBuffer.length() > 0) {
                sizes = custTcpBuffer;
            }
            this.mLinkProperties.setTcpBufferSizes(sizes);
        }
        ratName = "evdo";
        configOverride = this.mPhone.getContext().getResources().getStringArray(17236019);
        while (i < configOverride.length) {
            split = split2.split(":");
            if (!ratName.equals(split[0])) {
            }
        }
        tcpBufferSizePropName = "hw.net.tcp.buffersize.";
        if (sizes == null) {
            switch (rilRat) {
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    sizes = TCP_BUFFER_SIZES_GPRS;
                    tcpBufferSizePropName = tcpBufferSizePropName + "gprs";
                    break;
                case EHRPD_MAX_RETRY /*2*/:
                    sizes = TCP_BUFFER_SIZES_EDGE;
                    tcpBufferSizePropName = tcpBufferSizePropName + "edge";
                    break;
                case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                case CMD_TO_STRING_COUNT /*17*/:
                    sizes = TCP_BUFFER_SIZES_UMTS;
                    tcpBufferSizePropName = tcpBufferSizePropName + "umts";
                    break;
                case CharacterSets.ISO_8859_3 /*6*/:
                    sizes = TCP_BUFFER_SIZES_1XRTT;
                    tcpBufferSizePropName = tcpBufferSizePropName + "1xrtt";
                    break;
                case CharacterSets.ISO_8859_4 /*7*/:
                case CharacterSets.ISO_8859_5 /*8*/:
                case CharacterSets.ISO_8859_9 /*12*/:
                    sizes = TCP_BUFFER_SIZES_EVDO;
                    tcpBufferSizePropName = tcpBufferSizePropName + "evdo";
                    break;
                case CharacterSets.ISO_8859_6 /*9*/:
                    sizes = TCP_BUFFER_SIZES_HSDPA;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hsdpa";
                    break;
                case CharacterSets.ISO_8859_7 /*10*/:
                case CharacterSets.ISO_8859_8 /*11*/:
                    sizes = TCP_BUFFER_SIZES_HSPA;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hspa";
                    break;
                case UserData.ASCII_CR_INDEX /*13*/:
                    sizes = TCP_BUFFER_SIZES_EHRPD;
                    tcpBufferSizePropName = tcpBufferSizePropName + "ehrpd";
                    break;
                case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                    sizes = TCP_BUFFER_SIZES_LTE;
                    tcpBufferSizePropName = tcpBufferSizePropName + "lte";
                    break;
                case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                    sizes = TCP_BUFFER_SIZES_HSPAP;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hspap";
                    break;
            }
        }
        try {
            custTcpBuffer = SystemProperties.get(tcpBufferSizePropName);
            log("custTcpBuffer = " + custTcpBuffer);
            sizes = custTcpBuffer;
        } catch (Exception e) {
            log("Exception: read custTcpBuffer error " + e.toString());
        }
        this.mLinkProperties.setTcpBufferSizes(sizes);
    }

    private NetworkCapabilities makeNetworkCapabilities() {
        NetworkCapabilities result = new NetworkCapabilities();
        result.addTransportType(0);
        ArrayList<String> apnTypes = new ArrayList();
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            apnTypes.add(apnContext.getApnType());
        }
        if (this.mApnSetting != null) {
            String[] types = this.mApnSetting.types;
            if (enableCompatibleSimilarApnSettings()) {
                types = getCompatibleSimilarApnSettingsTypes(this.mApnSetting, this.mDct.getAllApnList());
            }
            for (String type : types) {
                if (type.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                    result.addCapability(12);
                    result.addCapability(0);
                    if (DcTracker.CT_SUPL_FEATURE_ENABLE && !apnTypes.contains("supl") && this.mDct.isCTSimCard(this.mPhone.getSubId())) {
                        log("ct supl feature enabled and apncontex didn't contain supl, didn't add supl capability");
                    } else {
                        result.addCapability(1);
                    }
                    result.addCapability(3);
                    result.addCapability(4);
                    result.addCapability(5);
                    result.addCapability(7);
                    result.addCapability(18);
                    result.addCapability(19);
                    result.addCapability(20);
                    result.addCapability(21);
                    result.addCapability(22);
                    result.addCapability(23);
                    result.addCapability(24);
                    result.addCapability(9);
                } else if (type.equals("default")) {
                    result.addCapability(12);
                } else if (type.equals("mms")) {
                    result.addCapability(0);
                } else if (type.equals("supl")) {
                    if (DcTracker.CT_SUPL_FEATURE_ENABLE && !apnTypes.contains("supl") && this.mDct.isCTSimCard(this.mPhone.getSubId())) {
                        log("ct supl feature enabled and apncontex didn't contain supl, didn't add supl capability");
                    } else {
                        result.addCapability(1);
                    }
                } else if (type.equals("dun")) {
                    ApnSetting securedDunApn = this.mDct.fetchDunApn();
                    if (securedDunApn == null || securedDunApn.equals(this.mApnSetting)) {
                        result.addCapability(EHRPD_MAX_RETRY);
                    }
                } else if (type.equals("fota")) {
                    result.addCapability(3);
                } else if (type.equals("ims")) {
                    result.addCapability(4);
                } else if (type.equals("cbs")) {
                    result.addCapability(5);
                } else if (type.equals("ia")) {
                    result.addCapability(7);
                } else if (type.equals("emergency")) {
                    result.addCapability(10);
                } else if (type.equals("bip0")) {
                    result.addCapability(18);
                } else if (type.equals("bip1")) {
                    result.addCapability(19);
                } else if (type.equals("bip2")) {
                    result.addCapability(20);
                } else if (type.equals("bip3")) {
                    result.addCapability(21);
                } else if (type.equals("bip4")) {
                    result.addCapability(22);
                } else if (type.equals("bip5")) {
                    result.addCapability(23);
                } else if (type.equals("bip6")) {
                    result.addCapability(24);
                } else if (type.equals("xcap")) {
                    result.addCapability(9);
                }
            }
            if (!this.mApnSetting.isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming())) {
                result.addCapability(11);
            }
            result.maybeMarkCapabilitiesRestricted();
        }
        int up = 14;
        int down = 14;
        switch (this.mRilRat) {
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                up = 80;
                down = 80;
                break;
            case EHRPD_MAX_RETRY /*2*/:
                up = 59;
                down = 236;
                break;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                up = 384;
                down = 384;
                break;
            case CharacterSets.ISO_8859_1 /*4*/:
            case CharacterSets.ISO_8859_2 /*5*/:
                up = 14;
                down = 14;
                break;
            case CharacterSets.ISO_8859_3 /*6*/:
                up = 100;
                down = 100;
                break;
            case CharacterSets.ISO_8859_4 /*7*/:
                up = PduPart.P_START;
                down = 2457;
                break;
            case CharacterSets.ISO_8859_5 /*8*/:
                up = 1843;
                down = 3174;
                break;
            case CharacterSets.ISO_8859_6 /*9*/:
                up = 2048;
                down = 14336;
                break;
            case CharacterSets.ISO_8859_7 /*10*/:
                up = 5898;
                down = 14336;
                break;
            case CharacterSets.ISO_8859_8 /*11*/:
                up = 5898;
                down = 14336;
                break;
            case CharacterSets.ISO_8859_9 /*12*/:
                up = 1843;
                down = 5017;
                break;
            case UserData.ASCII_CR_INDEX /*13*/:
                up = PduPart.P_START;
                down = 2516;
                break;
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                up = 51200;
                down = 102400;
                break;
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                up = 11264;
                down = 43008;
                break;
        }
        result.setLinkUpstreamBandwidthKbps(up);
        result.setLinkDownstreamBandwidthKbps(down);
        result.setNetworkSpecifier(Integer.toString(this.mPhone.getSubId()));
        return result;
    }

    public boolean enableCompatibleSimilarApnSettings() {
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            return CUST_RETRY_CONFIG;
        }
        String plmnsConfig = System.getString(this.mPhone.getContext().getContentResolver(), "compatible_apn_plmn");
        if (TextUtils.isEmpty(plmnsConfig)) {
            return CUST_RETRY_CONFIG;
        }
        String operator = this.mDct.getCTOperator(this.mDct.getOperatorNumeric());
        String[] plmns = plmnsConfig.split(",");
        int length = plmns.length;
        int i = 0;
        while (i < length) {
            String plmn = plmns[i];
            if (TextUtils.isEmpty(plmn) || !plmn.equals(operator)) {
                i++;
            } else {
                log("enableCompatibleSimilarApnSettings: " + operator);
                return VDBG;
            }
        }
        return CUST_RETRY_CONFIG;
    }

    public String[] getCompatibleSimilarApnSettingsTypes(ApnSetting currentApnSetting, ArrayList<ApnSetting> allApnSettings) {
        ArrayList<String> resultTypes = new ArrayList();
        if (currentApnSetting == null) {
            return (String[]) resultTypes.toArray(new String[0]);
        }
        resultTypes.addAll(Arrays.asList(currentApnSetting.types));
        if (allApnSettings == null) {
            return (String[]) resultTypes.toArray(new String[0]);
        }
        for (ApnSetting apn : allApnSettings) {
            if (!currentApnSetting.equals(apn) && apnSettingsSimilar(currentApnSetting, apn)) {
                for (String type : apn.types) {
                    if (!resultTypes.contains(type)) {
                        resultTypes.add(type);
                    }
                }
            }
        }
        log("getCompatibleSimilarApnSettingsTypes: " + resultTypes);
        return (String[]) resultTypes.toArray(new String[0]);
    }

    private boolean apnSettingsSimilar(ApnSetting first, ApnSetting second) {
        return (Objects.equals(first.apn, second.apn) && ((first.authType == second.authType || -1 == first.authType || -1 == second.authType) && Objects.equals(first.user, second.user) && Objects.equals(first.password, second.password) && Objects.equals(first.proxy, second.proxy) && Objects.equals(first.port, second.port) && xorEqualsProtocol(first.protocol, second.protocol) && xorEqualsProtocol(first.roamingProtocol, second.roamingProtocol) && first.carrierEnabled == second.carrierEnabled && first.bearerBitmask == second.bearerBitmask && first.mtu == second.mtu && xorEquals(first.mmsc, second.mmsc) && xorEquals(first.mmsProxy, second.mmsProxy))) ? xorEquals(first.mmsPort, second.mmsPort) : CUST_RETRY_CONFIG;
    }

    private boolean xorEquals(String first, String second) {
        if (Objects.equals(first, second) || TextUtils.isEmpty(first)) {
            return VDBG;
        }
        return TextUtils.isEmpty(second);
    }

    private boolean xorEqualsProtocol(String first, String second) {
        if (Objects.equals(first, second) || (("IPV4V6".equals(first) && ("IP".equals(second) || "IPV6".equals(second))) || ("IP".equals(first) && "IPV4V6".equals(second)))) {
            return VDBG;
        }
        return "IPV6".equals(first) ? "IPV4V6".equals(second) : CUST_RETRY_CONFIG;
    }

    private SetupResult setLinkProperties(DataCallResponse response, LinkProperties lp) {
        boolean okToUseSystemPropertyDns = CUST_RETRY_CONFIG;
        int subId = this.mPhone.getSubId();
        if (subId == SubscriptionController.getInstance().getDefaultDataSubId()) {
            log("setLinkProperties: ok to use system property dns");
            okToUseSystemPropertyDns = VDBG;
        }
        return response.setLinkProperties(lp, okToUseSystemPropertyDns, subId);
    }

    private boolean initConnection(ConnectionParams cp) {
        ApnContext apnContext = cp.mApnContext;
        boolean isBipUsingDefaultAPN = CUST_RETRY_CONFIG;
        if (this.mApnSetting == null) {
            this.mApnSetting = apnContext.getApnSetting();
        }
        if (this.mApnSetting != null) {
            if (this.mDct.isBipApnType(apnContext.getApnType()) && "default".equals(SystemProperties.get("gsm.bip.apn"))) {
                isBipUsingDefaultAPN = this.mApnSetting.canHandleType("default");
            } else {
                isBipUsingDefaultAPN = CUST_RETRY_CONFIG;
            }
        }
        if (this.mApnSetting == null || !(this.mApnSetting.canHandleType(apnContext.getApnType()) || r1)) {
            log("initConnection: incompatible apnSetting in ConnectionParams cp=" + cp + " dc=" + this);
            return CUST_RETRY_CONFIG;
        }
        this.mTag++;
        this.mConnectionParams = cp;
        this.mConnectionParams.mTag = this.mTag;
        this.mApnContexts.put(apnContext, cp);
        log("initConnection:  RefCount=" + this.mApnContexts.size());
        return VDBG;
    }

    private boolean updateNetworkInfoSuspendState() {
        boolean z = VDBG;
        DetailedState oldState = this.mNetworkInfo.getDetailedState();
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (sst.getCurrentDataConnectionState() != 0) {
            this.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        } else {
            if (!sst.isConcurrentVoiceAndDataAllowed()) {
                CallTracker ct = this.mPhone.getCallTracker();
                if (!(ct == null || ct.getState() == PhoneConstants.State.IDLE)) {
                    this.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
                    if (oldState == DetailedState.SUSPENDED) {
                        z = CUST_RETRY_CONFIG;
                    }
                    return z;
                }
            }
            this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        }
        if (oldState == this.mNetworkInfo.getDetailedState()) {
            z = CUST_RETRY_CONFIG;
        }
        return z;
    }

    void tearDownNow() {
        log("tearDownNow()");
        sendMessage(obtainMessage(EVENT_TEAR_DOWN_NOW));
    }

    private long getSuggestedRetryDelay(AsyncResult ar) {
        DataCallResponse response = ar.result;
        if (response.suggestedRetryTime < 0) {
            log("No suggested retry delay.");
            return -2;
        } else if (response.suggestedRetryTime != Integer.MAX_VALUE) {
            return (long) response.suggestedRetryTime;
        } else {
            log("Modem suggested not retrying.");
            return -1;
        }
    }

    protected String getWhatToString(int what) {
        return cmdToString(what);
    }

    private static String msgToString(Message msg) {
        if (msg == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder();
        b.append("{what=");
        b.append(cmdToString(msg.what));
        b.append(" when=");
        TimeUtils.formatDuration(msg.getWhen() - SystemClock.uptimeMillis(), b);
        if (msg.arg1 != 0) {
            b.append(" arg1=");
            b.append(msg.arg1);
        }
        if (msg.arg2 != 0) {
            b.append(" arg2=");
            b.append(msg.arg2);
        }
        if (msg.obj != null) {
            b.append(" obj=");
            b.append(msg.obj);
        }
        b.append(" target=");
        b.append(msg.getTarget());
        b.append(" replyTo=");
        b.append(msg.replyTo);
        b.append("}");
        return b.toString();
    }

    static void slog(String s) {
        Rlog.d("DC", s);
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void logd(String s) {
        Rlog.d(getName(), s);
    }

    protected void logv(String s) {
        Rlog.v(getName(), s);
    }

    protected void logi(String s) {
        Rlog.i(getName(), s);
    }

    protected void logw(String s) {
        Rlog.w(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    public String toStringSimple() {
        return getName() + ": State=" + getCurrentState().getName() + " mApnSetting=" + this.mApnSetting + " RefCount=" + this.mApnContexts.size() + " mCid=" + this.mCid + " mCreateTime=" + this.mCreateTime + " mLastastFailTime=" + this.mLastFailTime + " mLastFailCause=" + this.mLastFailCause + " mTag=" + this.mTag + " mLinkProperties=" + this.mLinkProperties + " linkCapabilities=" + makeNetworkCapabilities();
    }

    public String toString() {
        return "{" + toStringSimple() + "}";
    }

    private void dumpToLog() {
        dump(null, new AnonymousClass1(new StringWriter(0)), null);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("DataConnection ");
        super.dump(fd, pw, args);
        pw.println(" mApnContexts.size=" + this.mApnContexts.size());
        pw.println(" mApnContexts=" + this.mApnContexts);
        pw.flush();
        pw.println(" mDataConnectionTracker=" + this.mDct);
        pw.println(" mApnSetting=" + this.mApnSetting);
        pw.println(" mTag=" + this.mTag);
        pw.println(" mCid=" + this.mCid);
        pw.println(" mConnectionParams=" + this.mConnectionParams);
        pw.println(" mDisconnectParams=" + this.mDisconnectParams);
        pw.println(" mDcFailCause=" + this.mDcFailCause);
        pw.flush();
        pw.println(" mPhone=" + this.mPhone);
        pw.flush();
        pw.println(" mLinkProperties=" + this.mLinkProperties);
        pw.flush();
        pw.println(" mDataRegState=" + this.mDataRegState);
        pw.println(" mRilRat=" + this.mRilRat);
        pw.println(" mNetworkCapabilities=" + makeNetworkCapabilities());
        pw.println(" mCreateTime=" + TimeUtils.logTimeOfDay(this.mCreateTime));
        pw.println(" mLastFailTime=" + TimeUtils.logTimeOfDay(this.mLastFailTime));
        pw.println(" mLastFailCause=" + this.mLastFailCause);
        pw.flush();
        pw.println(" mUserData=" + this.mUserData);
        pw.println(" mInstanceNumber=" + mInstanceNumber);
        pw.println(" mAc=" + this.mAc);
        pw.flush();
    }
}
