package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Telephony.Carriers;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.intelligentdataswitch.IDSConstants;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class HwDataServiceChrManagerImpl implements HwDataServiceChrManager {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int CLEANUP = 1;
    private static final boolean DBG = true;
    private static final int DNS_BIG_LATENCY = 2000;
    private static final int DNS_ERROR_IPV6_TIMEOUT = 15;
    private static final int DNS_REPORT_COUNTING_INTERVAL = 100;
    private static final int DNS_TIME_MIN = 10;
    private static final int EVENT_APN_CHANGED = 4;
    private static final int EVENT_APN_CONNECTION_INFO_INTENT = 5;
    private static final int EVENT_GET_CDMA_CHR_INFO = 2;
    private static final int EVENT_TIMER_EXPIRE = 1;
    private static final int EVENT_WIFI_DISCONNECT_TIMER_EXPIRE = 3;
    private static final int GET_DATA_CALL_LIST = 0;
    private static final String LOG_TAG = "HwChrManagerImpl";
    private static final int MAX_SLOT = 3;
    private static final int RADIO_RESTART = 3;
    private static final int RADIO_RESTART_WITH_PROP = 4;
    private static final int REREGISTER = 2;
    private static final int TIMER_INTERVAL_CDMA_PDP_SAME_STATUS = 60000;
    private static final int TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED = 180000;
    private static final long TIMER_INTERVAL_SEND_APN_INFO_INTENT = 3000;
    private static Context mContext;
    private static HwDataServiceChrManager mInstance;
    private int WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL;
    private Phone mActivePhone;
    private ApnChangeObserver mApnObserver;
    private Timer mCdmaPdpSameStatusTimer;
    private GsmCdmaPhone mCdmaPhone;
    private boolean mCheckApnContextState;
    private int mChrCdmaPdpRilFailCause;
    private String mDataNotAllowedReason;
    private int mDataSubId;
    private boolean[] mDefaultAPNReported;
    private ApnContext mDefaultApnContext;
    private int mDnsBigLatency;
    private int mDnsCount;
    private int mDnsIpv6Timeout;
    private boolean[] mDunAPNReported;
    private String mGetAnyDataEnabledFalseReason;
    private Handler mHandler;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsBringUp;
    private boolean mIsDataConnectionAttached;
    private boolean mIsFirstReport;
    private boolean mIsReceivedSimloadedMsg;
    private boolean mIsRecordsLoadedRegistered;
    private int mLastDataCallFailStatus;
    private boolean[] mMmsAPNReported;
    private String mPdpActiveIpType;
    private int mSameStatusTimes;
    private Timer mSimloadedTimer;
    private boolean mUserDataEnabled;

    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(HwDataServiceChrManagerImpl.this.mHandler);
        }

        public void onChange(boolean selfChange) {
            HwDataServiceChrManagerImpl.this.mHandler.sendMessage(HwDataServiceChrManagerImpl.this.mHandler.obtainMessage(HwDataServiceChrManagerImpl.RADIO_RESTART_WITH_PROP));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwDataServiceChrManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwDataServiceChrManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwDataServiceChrManagerImpl.<clinit>():void");
    }

    public HwDataServiceChrManagerImpl() {
        this.mPdpActiveIpType = null;
        this.mDataNotAllowedReason = null;
        this.mGetAnyDataEnabledFalseReason = null;
        this.mIsBringUp = false;
        this.mIsReceivedSimloadedMsg = false;
        this.mIsRecordsLoadedRegistered = false;
        this.mIsDataConnectionAttached = false;
        this.mUserDataEnabled = false;
        this.mCheckApnContextState = false;
        this.mLastDataCallFailStatus = -1;
        this.mSameStatusTimes = GET_DATA_CALL_LIST;
        this.mDefaultAPNReported = new boolean[]{false, false, false};
        this.mMmsAPNReported = new boolean[]{false, false, false};
        this.mDunAPNReported = new boolean[]{false, false, false};
        this.mIsFirstReport = DBG;
        this.mDnsCount = GET_DATA_CALL_LIST;
        this.mDnsBigLatency = GET_DATA_CALL_LIST;
        this.mDnsIpv6Timeout = GET_DATA_CALL_LIST;
        this.WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL = IDSConstants.SIGNAL_MAX_WEAK_TIMER;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                try {
                    String action = intent.getAction();
                    Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "onReceive: action=" + action);
                    if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                        int subId = intent.getIntExtra("subscription", HwDataServiceChrManagerImpl.RADIO_RESTART);
                        String simState = intent.getStringExtra("ss");
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "ACTION_SIM_STATE_CHANGED: simState=" + simState + "  SUBSCRIPTION_KEY = " + subId);
                        if ("READY".equals(simState) && subId == SubscriptionManager.getDefaultSubscriptionId()) {
                            HwDataServiceChrManagerImpl.this.mDataSubId = subId;
                            HwDataServiceChrManagerImpl.this.startTimer(HwDataServiceChrManagerImpl.this.mSimloadedTimer, HwDataServiceChrManagerImpl.TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED);
                        }
                    } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null && networkInfo.getType() == HwDataServiceChrManagerImpl.EVENT_TIMER_EXPIRE) {
                            HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                            if (networkInfo.getState() == State.DISCONNECTED) {
                                HwDataServiceChrManagerImpl.this.sendMonitorWifiSwitchToMobileMessage(HwDataServiceChrManagerImpl.this.WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(HwDataServiceChrManagerImpl.LOG_TAG, "Exception: onReceive fail! " + e);
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case HwDataServiceChrManagerImpl.EVENT_TIMER_EXPIRE /*1*/:
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "received EVENT_TIMER_EXPIRE message!isReceivedEventRecordsLoaded = " + HwDataServiceChrManagerImpl.this.getReceivedSimloadedMsg());
                            HwDataServiceChrManagerImpl.this.stopTimer(HwDataServiceChrManagerImpl.this.mSimloadedTimer);
                            HwDataServiceChrManagerImpl.this.stopTimer(HwDataServiceChrManagerImpl.this.mCdmaPdpSameStatusTimer);
                            HwDataServiceChrManagerImpl.this.cleanLastStatus();
                            if (HwDataServiceChrManagerImpl.this.getSimCardState(HwDataServiceChrManagerImpl.this.mDataSubId) == HwDataServiceChrManagerImpl.EVENT_TIMER_EXPIRE || HwDataServiceChrManagerImpl.this.getSimCardState(HwDataServiceChrManagerImpl.this.mDataSubId) == 0) {
                                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "Timer expire,sim card is absent!");
                                return;
                            } else if (SubscriptionController.getInstance().getSubState(HwDataServiceChrManagerImpl.this.mDataSubId) == 0) {
                                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "startTimer again,getSubState = INACTIVE");
                                HwDataServiceChrManagerImpl.this.startTimer(HwDataServiceChrManagerImpl.this.mSimloadedTimer, HwDataServiceChrManagerImpl.TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED);
                                return;
                            } else if (HwDataServiceChrManagerImpl.this.mDataSubId == SubscriptionManager.getDefaultSubscriptionId()) {
                                if (HwDataServiceChrManagerImpl.this.getRecordsLoadedRegistered() && !HwDataServiceChrManagerImpl.this.getReceivedSimloadedMsg()) {
                                    Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "Timer expire,simloaded msg is not received,sim card is present!trigger chr!SubId:" + HwDataServiceChrManagerImpl.this.mDataSubId + " getSimCardState = " + HwDataServiceChrManagerImpl.this.getSimCardState(HwDataServiceChrManagerImpl.this.mDataSubId));
                                    HwDataServiceChrManagerImpl.this.sendIntentWhenSimloadedMsgIsNotReceived(HwDataServiceChrManagerImpl.this.mDataSubId);
                                } else if (!HwDataServiceChrManagerImpl.this.getBringUp() && HwDataServiceChrManagerImpl.this.mDefaultApnContext != null && HwDataServiceChrManagerImpl.this.mDefaultApnContext.isEnabled() && HwDataServiceChrManagerImpl.this.mUserDataEnabled && HwDataServiceChrManagerImpl.this.mActivePhone.getServiceState().getDataRegState() == 0) {
                                    Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "Timer expire,pdp activing process return error,when data switch is on and ps is attached!");
                                    HwDataServiceChrManagerImpl.this.sendIntentWhenPdpActFailBlockAtFw(HwDataServiceChrManagerImpl.this.mActivePhone.getSubId());
                                    HwDataServiceChrManagerImpl.this.setDataNotAllowedReasonToNull();
                                    HwDataServiceChrManagerImpl.this.setAnyDataEnabledFalseReasonToNull();
                                }
                                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "isBringUp=" + HwDataServiceChrManagerImpl.this.getBringUp() + " defaultApnContext.isEnabled()" + HwDataServiceChrManagerImpl.this.mDefaultApnContext.isEnabled() + " mUserDataEnabled=" + HwDataServiceChrManagerImpl.this.mUserDataEnabled + " isDataConnectionAttached=" + HwDataServiceChrManagerImpl.this.mIsDataConnectionAttached);
                                return;
                            } else {
                                return;
                            }
                        case HwDataServiceChrManagerImpl.REREGISTER /*2*/:
                            HwDataServiceChrManagerImpl.this.sendIntentWhenCdmaPdpActFail(msg.obj);
                            HwDataServiceChrManagerImpl.this.mChrCdmaPdpRilFailCause = -1;
                            return;
                        case HwDataServiceChrManagerImpl.RADIO_RESTART /*3*/:
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "handleMessage:msg=" + msg);
                            if (HwDataServiceChrManagerImpl.this.mDefaultApnContext == null || HwDataServiceChrManagerImpl.this.mDefaultApnContext.isEnabled() || HwDataServiceChrManagerImpl.this.isWifiConnected()) {
                                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "wifi to mobile process is ok, DefaultTypeAPN is enabled");
                                return;
                            }
                            Intent apkIntent = new Intent("com.android.intent.action.wifi_switchto_mobile_fail");
                            apkIntent.putExtra("subscription", HwDataServiceChrManagerImpl.this.mActivePhone.getSubId());
                            HwDataServiceChrManagerImpl.mContext.sendBroadcast(apkIntent, HwDataServiceChrManagerImpl.CHR_BROADCAST_PERMISSION);
                            return;
                        case HwDataServiceChrManagerImpl.RADIO_RESTART_WITH_PROP /*4*/:
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "CHR set all type APNReported as false because of apn changed");
                            for (int subId = HwDataServiceChrManagerImpl.GET_DATA_CALL_LIST; subId < HwDataServiceChrManagerImpl.RADIO_RESTART; subId += HwDataServiceChrManagerImpl.EVENT_TIMER_EXPIRE) {
                                HwDataServiceChrManagerImpl.this.mDefaultAPNReported[subId] = false;
                                HwDataServiceChrManagerImpl.this.mMmsAPNReported[subId] = false;
                                HwDataServiceChrManagerImpl.this.mDunAPNReported[subId] = false;
                            }
                            return;
                        case HwDataServiceChrManagerImpl.EVENT_APN_CONNECTION_INFO_INTENT /*5*/:
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "CHR received the event EVENT_APN_CONNECTION_INFO_INTENT, then send the intent");
                            HwDataServiceChrManagerImpl.mContext.sendBroadcast(msg.obj, HwDataServiceChrManagerImpl.CHR_BROADCAST_PERMISSION);
                            return;
                        default:
                            return;
                    }
                } catch (Exception e) {
                    Log.e(HwDataServiceChrManagerImpl.LOG_TAG, "Exception: handleMessage fail! " + e);
                }
                Log.e(HwDataServiceChrManagerImpl.LOG_TAG, "Exception: handleMessage fail! " + e);
            }
        };
    }

    public static HwDataServiceChrManager getDefault() {
        return mInstance;
    }

    public void init(Context context) {
        if (context != null) {
            try {
                mContext = context;
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SIM_STATE_CHANGED");
                filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                mContext.registerReceiver(this.mIntentReceiver, filter);
                this.mApnObserver = new ApnChangeObserver();
                mContext.getContentResolver().registerContentObserver(Carriers.CONTENT_URI, DBG, this.mApnObserver);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: init fail! " + e);
            }
        }
    }

    public void setPdpActiveIpType(String pdpActiveIpType, int subId) {
        this.mPdpActiveIpType = pdpActiveIpType;
        sendIntentWithPdpIpType(subId);
    }

    public String getPdpActiveIpType() {
        return this.mPdpActiveIpType;
    }

    public void setDataNotAllowedReason(Phone phone, boolean attachedState, boolean autoAttachOnCreation, boolean recordsLoaded, boolean internalDataEnabled, boolean userDataEnabled, boolean isPsRestricted) {
        String reason = "";
        if (phone != null) {
            try {
                reason = attachedState + "," + autoAttachOnCreation + "," + recordsLoaded + "," + phone.getState() + "," + phone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() + "," + internalDataEnabled + "," + phone.getServiceState().getRoaming() + "," + phone.mDcTracker.getDataOnRoamingEnabled() + "," + userDataEnabled + "," + isPsRestricted + "," + phone.getServiceStateTracker().getDesiredPowerState();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: setDataNotAllowedReason fail! " + e);
                return;
            }
        }
        Log.d(LOG_TAG, "DcTrackerBse setDataNotAllowedReason,reason=" + reason);
        this.mDataNotAllowedReason = reason;
    }

    public void setDataNotAllowedReasonToNull() {
        this.mDataNotAllowedReason = "";
    }

    public String getDataNotAllowedReason() {
        return this.mDataNotAllowedReason;
    }

    public void setAnyDataEnabledFalseReason(boolean internalDataEnabled, boolean userDataEnabled, boolean sPolicyDataEnabled, boolean checkUserDataEnabled) {
        String reason = "";
        try {
            reason = internalDataEnabled + "," + userDataEnabled + "," + sPolicyDataEnabled + "," + checkUserDataEnabled;
            Log.d(LOG_TAG, "DcTrackerBse setAnyDataEnabledFalseReason,flag=" + reason);
            this.mGetAnyDataEnabledFalseReason = reason;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: setAnyDataEnabledFalseReason fail! " + e);
        }
    }

    public void setAnyDataEnabledFalseReasonToNull() {
        this.mGetAnyDataEnabledFalseReason = "";
    }

    public String getAnyDataEnabledFalseReason() {
        return this.mGetAnyDataEnabledFalseReason;
    }

    public void setBringUp(boolean isBringUp) {
        this.mIsBringUp = isBringUp;
    }

    public boolean getBringUp() {
        return this.mIsBringUp;
    }

    public void setReceivedSimloadedMsg(Phone phone, boolean isReceivedSimloadedMsg, ConcurrentHashMap<String, ApnContext> apnContexts, boolean userDataEnabled) {
        this.mActivePhone = phone;
        this.mIsReceivedSimloadedMsg = isReceivedSimloadedMsg;
        this.mDefaultApnContext = (ApnContext) apnContexts.get("default");
        this.mUserDataEnabled = userDataEnabled;
    }

    public boolean getReceivedSimloadedMsg() {
        return this.mIsReceivedSimloadedMsg;
    }

    public void setRecordsLoadedRegistered(boolean isRecordsLoadedRegistered, int subId) {
        if (subId == this.mDataSubId) {
            this.mIsRecordsLoadedRegistered = isRecordsLoadedRegistered;
        }
    }

    public boolean getRecordsLoadedRegistered() {
        return this.mIsRecordsLoadedRegistered;
    }

    public void setCheckApnContextState(boolean checkApnContextState) {
        this.mCheckApnContextState = checkApnContextState;
    }

    public void getModemParamsWhenCdmaPdpActFail(Phone phone, int rilFailCause) {
        try {
            if (phone.getPhoneType() != EVENT_TIMER_EXPIRE) {
                this.mCdmaPhone = (GsmCdmaPhone) phone;
                String isSupportCdmaChr = SystemProperties.get("ro.sys.support_cdma_chr", "false");
                Log.d(LOG_TAG, "EVENT_GET_LAST_FAIL_DONE isSupportCdmaChr = " + isSupportCdmaChr);
                if ("true".equals(isSupportCdmaChr)) {
                    Log.d(LOG_TAG, "CDMAPhone pdp active fail");
                    phone.mCi.getCdmaChrInfo(this.mHandler.obtainMessage(REREGISTER, null));
                    this.mChrCdmaPdpRilFailCause = rilFailCause - 131072;
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: getModemParamsWhenCdmaPdpActFail fail! " + e);
        }
    }

    public void sendIntentWhenDorecovery(Phone phone, int recoveryAction) {
        try {
            if (phone.getServiceState().getDataRegState() == 0 && recoveryAction > 0) {
                Log.d(LOG_TAG, "sendIntentWhenDorecovery recoveryAction = " + recoveryAction);
                Context context = phone.getContext();
                int subId = phone.getSubId();
                Intent intent = new Intent("com.android.intent.action.do_recovery");
                intent.putExtra("subscription", subId);
                intent.putExtra("recoveryAction", recoveryAction);
                context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: sendIntentWhenDorecovery fail! " + e);
        }
    }

    public void sendIntentWhenSetDataSubFail(int subId) {
        Intent intent = new Intent("com.android.intent.action.set_data_sub_fail");
        intent.putExtra("subscription", subId);
        mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    public void sendIntentApnContextDisabledWhenWifiDisconnected(Phone phone, boolean isWifiConnected, boolean userDataEnabled, ApnContext apnContext) {
        if (this.mCheckApnContextState && apnContext != null) {
            try {
                if (apnContext.getApnType().equals("default") && !isWifiConnected && userDataEnabled && getReceivedSimloadedMsg() && !apnContext.isEnabled() && !ignoreReport(phone)) {
                    Log.d(LOG_TAG, "ready to trigger chr: apnContext = " + apnContext.toString());
                    Intent intent = new Intent("com.intent.action.apn_disable_while_wifi_disconnect");
                    intent.putExtra("subscription", phone.getSubId());
                    mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: sendIntentApnContextDisabledWhenWifiDisconnected fail! " + e);
            }
        }
    }

    private boolean isLabCard() {
        try {
            String hplmn = TelephonyManager.getDefault().getSimOperator();
            String[] blackListCards = new String[REREGISTER];
            blackListCards[GET_DATA_CALL_LIST] = "46060";
            blackListCards[EVENT_TIMER_EXPIRE] = "00101";
            int length = blackListCards.length;
            for (int i = GET_DATA_CALL_LIST; i < length; i += EVENT_TIMER_EXPIRE) {
                if (blackListCards[i].equals(hplmn)) {
                    Log.d(LOG_TAG, "Lab card, Ignore report CHR event!");
                    return DBG;
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: isLabCard fail! " + e);
        }
        return false;
    }

    private boolean isCardReady(Phone phone) {
        boolean z = false;
        int simstate = EVENT_APN_CONNECTION_INFO_INTENT;
        try {
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            if (phone.getSubId() != SubscriptionManager.getDefaultSubscriptionId() || subscriptionController.getSubState(phone.getSubId()) == 0) {
                Log.d(LOG_TAG, "isCardReady return false,subid != getDataSubscription || INACTIVE == getSubState");
                return false;
            }
            simstate = getSimCardState(phone.getSubId());
            if (EVENT_APN_CONNECTION_INFO_INTENT == simstate) {
                z = DBG;
            }
            return z;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: isCardReady fail! " + e);
        }
    }

    private boolean ignoreReport(Phone phone) {
        try {
            if (!isCardReady(phone)) {
                Log.d(LOG_TAG, "Card not Ready! Ignore report CHR event!");
                return DBG;
            } else if (isLabCard()) {
                Log.d(LOG_TAG, "isLabCard! Ignore report CHR event!");
                return DBG;
            } else {
                if (phone.getServiceState().getRilDataRadioTechnology() == 0 || phone.getServiceState().getDataRegState() != 0) {
                    Log.d(LOG_TAG, "ignoreReport: ps domain is not attached, skipped");
                    return DBG;
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: ignoreReport fail! " + e);
        }
    }

    private void cleanLastStatus() {
        this.mLastDataCallFailStatus = GET_DATA_CALL_LIST;
        this.mSameStatusTimes = GET_DATA_CALL_LIST;
    }

    private boolean isNeedReportChrForSameFailStatus(int[] params) {
        if (params != null) {
            try {
                if (RADIO_RESTART == params[GET_DATA_CALL_LIST] || 8 == params[GET_DATA_CALL_LIST] || 9 == params[GET_DATA_CALL_LIST] || 34 == params[GET_DATA_CALL_LIST] || 35 == params[GET_DATA_CALL_LIST] || 36 == params[GET_DATA_CALL_LIST] || 37 == params[GET_DATA_CALL_LIST]) {
                    if (this.mLastDataCallFailStatus == params[GET_DATA_CALL_LIST]) {
                        this.mSameStatusTimes += EVENT_TIMER_EXPIRE;
                        if (this.mSameStatusTimes > RADIO_RESTART) {
                            cleanLastStatus();
                            stopTimer(this.mCdmaPdpSameStatusTimer);
                            Log.d(LOG_TAG, "mSameStatusTimes>3.return true,report chr.");
                            return DBG;
                        }
                        Log.d(LOG_TAG, "mSameStatusTimes<=3.return false,dont report chr.");
                        return false;
                    }
                    cleanLastStatus();
                    stopTimer(this.mCdmaPdpSameStatusTimer);
                    startTimer(this.mCdmaPdpSameStatusTimer, TIMER_INTERVAL_CDMA_PDP_SAME_STATUS);
                    this.mSameStatusTimes += EVENT_TIMER_EXPIRE;
                    this.mLastDataCallFailStatus = params[GET_DATA_CALL_LIST];
                    Log.d(LOG_TAG, "new fail status!return false,dont report chr.");
                    return false;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: isNeedReportChrForSameFailStatus !!" + e);
            }
        }
        return DBG;
    }

    private void sendIntentWithPdpIpType(int subId) {
        Intent intent = new Intent("com.android.intent.action.pdp_act_ip_type");
        intent.putExtra("subscription", subId);
        intent.putExtra("pdpActIpType", getPdpActiveIpType());
        mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    public void SendIntentDNSfailure(String[] dnses) {
        if (dnses == null || dnses.length == 0) {
            Log.d(LOG_TAG, " send DNSfailureIntent,check dnses is null");
            mContext.sendBroadcast(new Intent("com.intent.action.dns_fail"), CHR_BROADCAST_PERMISSION);
        }
    }

    private boolean isUserModifyApnSetting() {
        String[] selections = new String[REREGISTER];
        selections[GET_DATA_CALL_LIST] = "visible<>1";
        selections[EVENT_TIMER_EXPIRE] = "visible is null";
        Uri uri = Carriers.CONTENT_URI;
        int length = selections.length;
        int i = GET_DATA_CALL_LIST;
        while (i < length) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, selections[i], null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                i += EVENT_TIMER_EXPIRE;
            } else {
                Log.d(LOG_TAG, "User ever add new APN!");
                cursor.close();
                return DBG;
            }
        }
        Log.d(LOG_TAG, "User never add APN setting!");
        return false;
    }

    public void sendIntentApnListEmpty(int subId) {
        Log.d(LOG_TAG, " send ApnListEmpty");
        Intent intent = new Intent("com.intent.action.apn_list_empty");
        intent.putExtra("subscription", subId);
        intent.putExtra("userModifyApnList", isUserModifyApnSetting() ? EVENT_TIMER_EXPIRE : GET_DATA_CALL_LIST);
        mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    public void onDnsEvent(Context context, int returnCode, int latencyMs) {
        if (latencyMs > DNS_TIME_MIN) {
            this.mDnsCount += EVENT_TIMER_EXPIRE;
            if (DNS_ERROR_IPV6_TIMEOUT == returnCode) {
                this.mDnsIpv6Timeout += EVENT_TIMER_EXPIRE;
            }
            if (latencyMs > DNS_BIG_LATENCY) {
                this.mDnsBigLatency += EVENT_TIMER_EXPIRE;
            }
            if (DNS_REPORT_COUNTING_INTERVAL == this.mDnsCount) {
                sendIntentDnsEvent(context);
            }
        }
    }

    private void sendIntentDnsEvent(Context context) {
        Log.d(LOG_TAG, "sendIntentDnsEvent mDnsCount:" + this.mDnsCount + " mDnsIpv6Timeout:" + this.mDnsIpv6Timeout + " mDnsBigLatency:" + this.mDnsBigLatency);
        Intent intent = new Intent("com.intent.action.dns_statistics");
        intent.putExtra("dnsCount", this.mDnsCount);
        intent.putExtra("dnsBigLatency", this.mDnsBigLatency);
        intent.putExtra("dnsIpv6Timeout", this.mDnsIpv6Timeout);
        this.mDnsCount = GET_DATA_CALL_LIST;
        this.mDnsBigLatency = GET_DATA_CALL_LIST;
        this.mDnsIpv6Timeout = GET_DATA_CALL_LIST;
        context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    private void sendIntentWhenCdmaPdpActFail(AsyncResult cdmaChrAR) {
        try {
            int[] params = cdmaChrAR.result;
            if (isNeedReportChrForSameFailStatus(params)) {
                if (params != null) {
                    Intent intent;
                    Log.d(LOG_TAG, "EVENT_GET_CDMA_CHR_INFO,params.length:" + params.length);
                    String pdpActIPType = getPdpActiveIpType();
                    if (this.mCdmaPhone.getServiceState().getDataRegState() == 0) {
                        intent = new Intent("com.android.intent.action.cdma_pdp_act_fail");
                        Log.d(LOG_TAG, "cdma pdp actived fail.");
                    } else {
                        intent = new Intent("com.android.intent.action.cdma_pdp_act_fail_fall_2g");
                        Log.d(LOG_TAG, "cdma pdp actived fail,because of fall to 2G.data register state is STATE_OUT_OF_SERVICE.");
                    }
                    intent.putExtra("subscription", this.mCdmaPhone.getSubId());
                    intent.putExtra("cdmaRilFailCause", this.mChrCdmaPdpRilFailCause);
                    intent.putExtra("pdpActIpType", pdpActIPType);
                    intent.putExtra("cdmaParams", params);
                    mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
                } else {
                    Log.d(LOG_TAG, "EVENT_GET_CDMA_CHR_INFO,params is null");
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: sendIntentWhenCdmaPdpActFail fail! " + e);
        }
    }

    private void sendIntentWhenSimloadedMsgIsNotReceived(int subId) {
        Intent intent = new Intent("com.android.intent.action.simloaded_msg_not_received");
        intent.putExtra("subscription", subId);
        mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    private void sendIntentWhenPdpActFailBlockAtFw(int subId) {
        Intent intent = new Intent("com.android.intent.action.pdp_fail_block_at_fw");
        intent.putExtra("subscription", subId);
        intent.putExtra("AnyDataEnabledFlag", getAnyDataEnabledFalseReason());
        intent.putExtra("DataNotAllowedReason", getDataNotAllowedReason());
        mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    private void startTimer(Timer timer, int timerInterval) {
        Exception e;
        Log.d(LOG_TAG, "startTimer!getSubId =" + this.mDataSubId + ",SubscriptionManager.getDefaultSubscriptionId() =" + SubscriptionManager.getDefaultSubscriptionId());
        try {
            Timer timer2 = new Timer();
            try {
                timer2.schedule(new TimerTask() {
                    public void run() {
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "TimerTask run enter");
                        HwDataServiceChrManagerImpl.this.mHandler.sendMessage(HwDataServiceChrManagerImpl.this.mHandler.obtainMessage(HwDataServiceChrManagerImpl.EVENT_TIMER_EXPIRE));
                    }
                }, (long) timerInterval);
                timer = timer2;
            } catch (Exception e2) {
                e = e2;
                timer = timer2;
                Log.e(LOG_TAG, "Exception: startTimer fail! " + e);
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(LOG_TAG, "Exception: startTimer fail! " + e);
        }
    }

    private void stopTimer(Timer timer) {
        if (timer != null) {
            try {
                Log.d(LOG_TAG, "mTimer!=null");
                timer.cancel();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: stopTimer fail! " + e);
            }
        }
    }

    private int getSimCardState(int subId) {
        return TelephonyManager.getDefault().getSimState(subId);
    }

    public void sendIntentWhenDataConnected(Phone phone, ApnSetting apn, LinkProperties linkProperties) {
        int subId = phone.getSubId();
        if (subId < 0 || subId >= RADIO_RESTART) {
            Log.e(LOG_TAG, "invalid subId: " + subId);
            return;
        }
        Log.d(LOG_TAG, "CHR APN setttings = " + apn);
        Log.d(LOG_TAG, "CHR Report flag: subId = " + subId + ", default flag = " + this.mDefaultAPNReported[subId] + ", mms flag = " + this.mMmsAPNReported[subId] + ", dun flag = " + this.mDunAPNReported[subId]);
        if (apn != null) {
            boolean needToReport = false;
            String[] strArr = apn.types;
            int length = strArr.length;
            for (int i = GET_DATA_CALL_LIST; i < length; i += EVENT_TIMER_EXPIRE) {
                String t = strArr[i];
                if (t.equalsIgnoreCase("default")) {
                    if (!this.mDefaultAPNReported[subId]) {
                        needToReport = DBG;
                        this.mDefaultAPNReported[subId] = DBG;
                    }
                } else if (t.equalsIgnoreCase("mms")) {
                    if (!this.mMmsAPNReported[subId]) {
                        needToReport = DBG;
                        this.mMmsAPNReported[subId] = DBG;
                    }
                } else if (t.equalsIgnoreCase("dun")) {
                    if (!this.mDunAPNReported[subId]) {
                        needToReport = DBG;
                        this.mDunAPNReported[subId] = DBG;
                    }
                } else if (t.equalsIgnoreCase("*")) {
                    boolean z = (this.mDefaultAPNReported[subId] && this.mMmsAPNReported[subId]) ? this.mDunAPNReported[subId] : false;
                    if (!z) {
                        needToReport = DBG;
                        this.mDefaultAPNReported[subId] = DBG;
                        this.mMmsAPNReported[subId] = DBG;
                        this.mDunAPNReported[subId] = DBG;
                    }
                }
            }
            if (needToReport) {
                Log.d(LOG_TAG, "CHR Report flag after processing the types : default flag = " + this.mDefaultAPNReported[subId] + " , mms flag = " + this.mMmsAPNReported[subId] + ", dun flag = " + this.mDunAPNReported[subId]);
                int hasUserPassword = GET_DATA_CALL_LIST;
                if (!(apn.user == null || apn.user.length() == 0)) {
                    hasUserPassword = EVENT_TIMER_EXPIRE;
                }
                Collection<InetAddress> dnses = linkProperties.getDnsServers();
                int hasDns = GET_DATA_CALL_LIST;
                if (dnses.size() != 0) {
                    hasDns = EVENT_TIMER_EXPIRE;
                }
                int chrRilRat = phone.getServiceState().getRilDataRadioTechnology();
                Log.d(LOG_TAG, "CHR chrRilRat = " + chrRilRat);
                Log.d(LOG_TAG, "CHR send apn info intent mIsFirstReport = " + this.mIsFirstReport);
                Intent intent = new Intent("com.intent.action.APN_CONNECTION_INFO");
                intent.putExtra("subscription", phone.getSubId());
                intent.putExtra("rilRat", chrRilRat);
                intent.putExtra("apnUserPassword", hasUserPassword);
                intent.putExtra("linkDns", hasDns);
                intent.putExtra("apnSetting", apn.toString());
                if (this.mIsFirstReport) {
                    sendBroadcastDelayed(intent, TIMER_INTERVAL_SEND_APN_INFO_INTENT);
                    this.mIsFirstReport = false;
                } else {
                    mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
                }
            } else {
                Log.d(LOG_TAG, "CHR do not need to report , ignore it ");
            }
        } else {
            Log.e(LOG_TAG, "CHR apn is null ");
        }
    }

    private void sendBroadcastDelayed(Intent intent, long delayedTimer) {
        Log.d(LOG_TAG, "CHR sendBroadcastDelayed delayedTimer = " + delayedTimer);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_APN_CONNECTION_INFO_INTENT, intent), delayedTimer);
    }

    public void sendMonitorWifiSwitchToMobileMessage(int delayInterval) {
        Log.d(LOG_TAG, "wifi disconnect, sendMonitorWifiSwitchToMobileMessage!");
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(RADIO_RESTART), (long) delayInterval);
    }

    public void removeMonitorWifiSwitchToMobileMessage() {
        this.mHandler.removeMessages(RADIO_RESTART);
    }

    private boolean isWifiConnected() {
        if (mContext != null) {
            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService("connectivity");
            if (connManager != null) {
                NetworkInfo wifiInfo = connManager.getNetworkInfo(EVENT_TIMER_EXPIRE);
                if (wifiInfo != null) {
                    Log.d(LOG_TAG, "mWifiConnected = " + wifiInfo.isConnected());
                    return wifiInfo.isConnected();
                }
            }
        }
        Log.d(LOG_TAG, "Get WifiConnected Info failed!");
        return false;
    }
}
