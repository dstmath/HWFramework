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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class HwDataServiceChrManagerImpl implements HwDataServiceChrManager {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int CLEANUP = 1;
    private static final boolean DBG = true;
    private static final int EVENT_APN_CHANGED = 4;
    private static final int EVENT_APN_CONNECTION_INFO_INTENT = 5;
    private static final int EVENT_GET_CDMA_CHR_INFO = 2;
    private static final int EVENT_TIMER_EXPIRE = 1;
    private static final int EVENT_WIFI_DISCONNECT_TIMER_EXPIRE = 3;
    private static final int GET_DATA_CALL_LIST = 0;
    private static final String LOG_TAG = "HwChrManagerImpl";
    private static final int MAX_PHONENUM = 3;
    private static final int MAX_SLOT = 3;
    private static final int RADIO_RESTART = 3;
    private static final int RADIO_RESTART_WITH_PROP = 4;
    private static final int REREGISTER = 2;
    private static final int TIMER_INTERVAL_CDMA_PDP_SAME_STATUS = 60000;
    private static final int TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED = 180000;
    private static final long TIMER_INTERVAL_SEND_APN_INFO_INTENT = 3000;
    private static Context mContext;
    private static HwDataServiceChrManager mInstance = new HwDataServiceChrManagerImpl();
    private int WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL = 20000;
    private Phone[] mActivePhone = new Phone[3];
    private ApnChangeObserver mApnObserver;
    private Timer mCdmaPdpSameStatusTimer;
    private GsmCdmaPhone mCdmaPhone;
    private boolean mCheckApnContextState = false;
    private int mChrCdmaPdpRilFailCause;
    private String mDataNotAllowedReason = null;
    private int mDataSubId;
    private boolean[] mDefaultAPNReported = new boolean[]{false, false, false};
    private ApnContext[] mDefaultApnContext = new ApnContext[3];
    private boolean[] mDunAPNReported = new boolean[]{false, false, false};
    private String mGetAnyDataEnabledFalseReason = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 1:
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "received EVENT_TIMER_EXPIRE message!isReceivedEventRecordsLoaded = " + HwDataServiceChrManagerImpl.this.getReceivedSimloadedMsg());
                        HwDataServiceChrManagerImpl.this.stopTimer(HwDataServiceChrManagerImpl.this.mSimloadedTimer);
                        HwDataServiceChrManagerImpl.this.mSimloadedTimer = null;
                        HwDataServiceChrManagerImpl.this.stopTimer(HwDataServiceChrManagerImpl.this.mCdmaPdpSameStatusTimer);
                        HwDataServiceChrManagerImpl.this.mCdmaPdpSameStatusTimer = null;
                        HwDataServiceChrManagerImpl.this.cleanLastStatus();
                        HwDataServiceChrManagerImpl.this.mUserDataEnabled = TelephonyManager.getDefault().getDataEnabled();
                        if (HwDataServiceChrManagerImpl.this.getSimCardState(HwDataServiceChrManagerImpl.this.mDataSubId) == 1 || HwDataServiceChrManagerImpl.this.getSimCardState(HwDataServiceChrManagerImpl.this.mDataSubId) == 0) {
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "Timer expire,sim card is absent!");
                            return;
                        } else if (SubscriptionController.getInstance().getSubState(HwDataServiceChrManagerImpl.this.mDataSubId) == 0) {
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "startTimer again,getSubState = INACTIVE");
                            HwDataServiceChrManagerImpl.this.mSimloadedTimer = HwDataServiceChrManagerImpl.this.startTimer(HwDataServiceChrManagerImpl.this.mSimloadedTimer, HwDataServiceChrManagerImpl.TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED);
                            return;
                        } else if (HwDataServiceChrManagerImpl.this.mDataSubId == SubscriptionController.getInstance().getDefaultDataSubId()) {
                            if (HwDataServiceChrManagerImpl.this.getRecordsLoadedRegistered() && !HwDataServiceChrManagerImpl.this.getReceivedSimloadedMsg()) {
                                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "Timer expire,simloaded msg is not received,sim card is present!trigger chr!SubId:" + HwDataServiceChrManagerImpl.this.mDataSubId + " getSimCardState = " + HwDataServiceChrManagerImpl.this.getSimCardState(HwDataServiceChrManagerImpl.this.mDataSubId));
                                HwDataServiceChrManagerImpl.this.sendIntentWhenSimloadedMsgIsNotReceived(HwDataServiceChrManagerImpl.this.mDataSubId);
                            } else if (!HwDataServiceChrManagerImpl.this.getBringUp() && HwDataServiceChrManagerImpl.this.mDataSubId >= 0 && 3 > HwDataServiceChrManagerImpl.this.mDataSubId && HwDataServiceChrManagerImpl.this.mDefaultApnContext[HwDataServiceChrManagerImpl.this.mDataSubId] != null && HwDataServiceChrManagerImpl.this.mActivePhone[HwDataServiceChrManagerImpl.this.mDataSubId] != null && HwDataServiceChrManagerImpl.this.mDefaultApnContext[HwDataServiceChrManagerImpl.this.mDataSubId].isEnabled() && HwDataServiceChrManagerImpl.this.mUserDataEnabled && HwDataServiceChrManagerImpl.this.mActivePhone[HwDataServiceChrManagerImpl.this.mDataSubId].getServiceState().getDataRegState() == 0) {
                                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "Timer expire,pdp activing process return error,when data switch is on and ps is attached!");
                                HwDataServiceChrManagerImpl.this.sendIntentWhenPdpActFailBlockAtFw(HwDataServiceChrManagerImpl.this.mDataSubId);
                                HwDataServiceChrManagerImpl.this.setDataNotAllowedReasonToNull();
                                HwDataServiceChrManagerImpl.this.setAnyDataEnabledFalseReasonToNull();
                            }
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "isBringUp=" + HwDataServiceChrManagerImpl.this.getBringUp() + " mDataSubId =" + HwDataServiceChrManagerImpl.this.mDataSubId + " defaultApnContext.isEnabled()" + HwDataServiceChrManagerImpl.this.mDefaultApnContext[HwDataServiceChrManagerImpl.this.mDataSubId].isEnabled() + " mUserDataEnabled=" + HwDataServiceChrManagerImpl.this.mUserDataEnabled + " isDataConnectionAttached=" + HwDataServiceChrManagerImpl.this.mIsDataConnectionAttached);
                            return;
                        } else {
                            return;
                        }
                    case 2:
                        HwDataServiceChrManagerImpl.this.sendIntentWhenCdmaPdpActFail(msg.obj);
                        HwDataServiceChrManagerImpl.this.mChrCdmaPdpRilFailCause = -1;
                        return;
                    case 3:
                        int DataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
                        if (DataSubId >= 3 || DataSubId < 0) {
                            Log.e(HwDataServiceChrManagerImpl.LOG_TAG, "EVENT_WIFI_DISCONNECT_TIMER_EXPIRE DataSubId invaild");
                            return;
                        }
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "handleMessage: DataSubId = " + DataSubId + " msg=" + msg);
                        if (HwDataServiceChrManagerImpl.this.mDefaultApnContext[DataSubId] == null || (HwDataServiceChrManagerImpl.this.mDefaultApnContext[DataSubId].isEnabled() ^ 1) == 0 || (HwDataServiceChrManagerImpl.this.isWifiConnected() ^ 1) == 0) {
                            Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "wifi to mobile process is ok, DefaultTypeAPN is enabled");
                            return;
                        }
                        Intent apkIntent = new Intent("com.android.intent.action.wifi_switchto_mobile_fail");
                        apkIntent.putExtra("subscription", DataSubId);
                        HwDataServiceChrManagerImpl.mContext.sendBroadcast(apkIntent, "com.huawei.android.permission.GET_CHR_DATA");
                        return;
                    case 4:
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "CHR set all type APNReported as false because of apn changed");
                        for (int subId = 0; subId < 3; subId++) {
                            HwDataServiceChrManagerImpl.this.mDefaultAPNReported[subId] = false;
                            HwDataServiceChrManagerImpl.this.mMmsAPNReported[subId] = false;
                            HwDataServiceChrManagerImpl.this.mDunAPNReported[subId] = false;
                        }
                        return;
                    case 5:
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "CHR received the event EVENT_APN_CONNECTION_INFO_INTENT, then send the intent");
                        HwDataServiceChrManagerImpl.mContext.sendBroadcast(msg.obj, "com.huawei.android.permission.GET_CHR_DATA");
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
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "onReceive: action=" + action);
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    int subId = intent.getIntExtra("subscription", 3);
                    String simState = intent.getStringExtra("ss");
                    Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "ACTION_SIM_STATE_CHANGED: simState=" + simState + "  SUBSCRIPTION_KEY = " + subId);
                    if ("READY".equals(simState) && subId == SubscriptionController.getInstance().getDefaultDataSubId()) {
                        HwDataServiceChrManagerImpl.this.mDataSubId = subId;
                        if (HwDataServiceChrManagerImpl.this.mSimloadedTimer == null) {
                            HwDataServiceChrManagerImpl.this.mSimloadedTimer = HwDataServiceChrManagerImpl.this.startTimer(HwDataServiceChrManagerImpl.this.mSimloadedTimer, HwDataServiceChrManagerImpl.TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED);
                        }
                    }
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    if (HwDataServiceChrManagerImpl.this.mIsWifiConnected && intent.getIntExtra("wifi_state", 4) == 1) {
                        HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                        HwDataServiceChrManagerImpl.this.sendMonitorWifiSwitchToMobileMessage(HwDataServiceChrManagerImpl.this.WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL);
                        HwDataServiceChrManagerImpl.this.mIsWifiConnected = false;
                    }
                    Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "mIsWifiConnected = " + HwDataServiceChrManagerImpl.this.mIsWifiConnected);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        if (HwDataServiceChrManagerImpl.this.mIsWifiConnected && networkInfo.getState() == State.DISCONNECTED) {
                            HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                            HwDataServiceChrManagerImpl.this.sendMonitorWifiSwitchToMobileMessage(HwDataServiceChrManagerImpl.this.WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL);
                        } else if (!HwDataServiceChrManagerImpl.this.mIsWifiConnected && networkInfo.getState() == State.CONNECTED) {
                            HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                        }
                        HwDataServiceChrManagerImpl.this.mIsWifiConnected = networkInfo.isConnected();
                    }
                    Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "mIsWifiConnected = " + HwDataServiceChrManagerImpl.this.mIsWifiConnected);
                }
            } catch (RuntimeException ex) {
                Log.e(HwDataServiceChrManagerImpl.LOG_TAG, "Exception: onReceive fail! " + ex);
            } catch (Exception e) {
                Log.e(HwDataServiceChrManagerImpl.LOG_TAG, "Exception: onReceive fail! " + e);
            }
        }
    };
    private boolean mIsBringUp = false;
    private boolean mIsDataConnectionAttached = false;
    private boolean mIsFirstReport = true;
    private boolean mIsReceivedSimloadedMsg = false;
    private boolean mIsRecordsLoadedRegistered = false;
    private boolean mIsWifiConnected = false;
    private int mLastDataCallFailStatus = -1;
    private boolean[] mMmsAPNReported = new boolean[]{false, false, false};
    private String mPdpActiveIpType = null;
    private int mSameStatusTimes = 0;
    private Timer mSimloadedTimer;
    private boolean mUserDataEnabled = false;

    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(HwDataServiceChrManagerImpl.this.mHandler);
        }

        public void onChange(boolean selfChange) {
            HwDataServiceChrManagerImpl.this.mHandler.sendMessage(HwDataServiceChrManagerImpl.this.mHandler.obtainMessage(4));
        }
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
                filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                filter.addAction("android.net.wifi.STATE_CHANGE");
                mContext.registerReceiver(this.mIntentReceiver, filter);
                this.mApnObserver = new ApnChangeObserver();
                mContext.getContentResolver().registerContentObserver(Carriers.CONTENT_URI, true, this.mApnObserver);
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
                reason = attachedState + "," + autoAttachOnCreation + "," + recordsLoaded + "," + phone.getState() + "," + phone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() + "," + internalDataEnabled + "," + phone.getServiceState().getRoaming() + "," + phone.mDcTracker.getDataRoamingEnabled() + "," + userDataEnabled + "," + isPsRestricted + "," + phone.getServiceStateTracker().getDesiredPowerState();
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
        if (phone == null) {
            Log.e(LOG_TAG, "setReceivedSimloadedMsg Phone is null ");
            return;
        }
        int sub = phone.getSubId();
        if (sub >= 3 || sub < 0) {
            Log.e(LOG_TAG, "setReceivedSimloadedMsg sub is invaild ");
            return;
        }
        this.mActivePhone[sub] = phone;
        this.mIsReceivedSimloadedMsg = isReceivedSimloadedMsg;
        this.mDefaultApnContext[sub] = (ApnContext) apnContexts.get("default");
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
            if (phone.getPhoneType() != 1) {
                this.mCdmaPhone = (GsmCdmaPhone) phone;
                String isSupportCdmaChr = SystemProperties.get("ro.sys.support_cdma_chr", "false");
                Log.d(LOG_TAG, "EVENT_GET_LAST_FAIL_DONE isSupportCdmaChr = " + isSupportCdmaChr);
                if ("true".equals(isSupportCdmaChr)) {
                    Log.d(LOG_TAG, "CDMAPhone pdp active fail");
                    phone.mCi.getCdmaChrInfo(this.mHandler.obtainMessage(2, null));
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
                context.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: sendIntentWhenDorecovery fail! " + e);
        }
    }

    public void sendIntentWhenSetDataSubFail(int subId) {
        Intent intent = new Intent("com.android.intent.action.set_data_sub_fail");
        intent.putExtra("subscription", subId);
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    public void sendIntentApnContextDisabledWhenWifiDisconnected(Phone phone, boolean isWifiConnected, boolean userDataEnabled, ApnContext apnContext) {
        if (this.mCheckApnContextState && apnContext != null) {
            try {
                if (apnContext.getApnType().equals("default") && (isWifiConnected ^ 1) != 0 && userDataEnabled && getReceivedSimloadedMsg() && (apnContext.isEnabled() ^ 1) != 0 && (ignoreReport(phone) ^ 1) != 0 && phone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                    Log.d(LOG_TAG, "ready to trigger chr: apnContext = " + apnContext.toString());
                    Intent intent = new Intent("com.intent.action.apn_disable_while_wifi_disconnect");
                    intent.putExtra("subscription", phone.getSubId());
                    mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: sendIntentApnContextDisabledWhenWifiDisconnected fail! " + e);
            }
        }
    }

    private boolean isLabCard() {
        try {
            String hplmn = TelephonyManager.getDefault().getSimOperator();
            for (String tmp : new String[]{"46060", "00101"}) {
                if (tmp.equals(hplmn)) {
                    Log.d(LOG_TAG, "Lab card, Ignore report CHR event!");
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: isLabCard fail! " + e);
        }
        return false;
    }

    private boolean isCardReady(Phone phone) {
        boolean z = false;
        int simstate = 5;
        try {
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            if (phone.getSubId() != SubscriptionManager.getDefaultSubscriptionId() || subscriptionController.getSubState(phone.getSubId()) == 0) {
                Log.d(LOG_TAG, "isCardReady return false,subid != getDataSubscription || INACTIVE == getSubState");
                return false;
            }
            simstate = getSimCardState(phone.getSubId());
            if (5 == simstate) {
                z = true;
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
                return true;
            } else if (isLabCard()) {
                Log.d(LOG_TAG, "isLabCard! Ignore report CHR event!");
                return true;
            } else {
                if (phone.getServiceState().getRilDataRadioTechnology() == 0 || phone.getServiceState().getDataRegState() != 0) {
                    Log.d(LOG_TAG, "ignoreReport: ps domain is not attached, skipped");
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: ignoreReport fail! " + e);
        }
    }

    private void cleanLastStatus() {
        this.mLastDataCallFailStatus = 0;
        this.mSameStatusTimes = 0;
    }

    private boolean isNeedReportChrForSameFailStatus(int[] params) {
        if (params != null) {
            try {
                if (3 == params[0] || 8 == params[0] || 9 == params[0] || 34 == params[0] || 35 == params[0] || 36 == params[0] || 37 == params[0]) {
                    if (this.mLastDataCallFailStatus == params[0]) {
                        this.mSameStatusTimes++;
                        Log.d(LOG_TAG, "mSameStatusTimes : " + this.mSameStatusTimes + " if mSameStatusTimes>3.return true,report chr. else .return false,dont report chr.");
                        if (this.mSameStatusTimes <= 3) {
                            return false;
                        }
                        cleanLastStatus();
                        stopTimer(this.mCdmaPdpSameStatusTimer);
                        this.mCdmaPdpSameStatusTimer = null;
                        return true;
                    }
                    cleanLastStatus();
                    stopTimer(this.mCdmaPdpSameStatusTimer);
                    this.mCdmaPdpSameStatusTimer = startTimer(this.mCdmaPdpSameStatusTimer, TIMER_INTERVAL_CDMA_PDP_SAME_STATUS);
                    this.mSameStatusTimes++;
                    this.mLastDataCallFailStatus = params[0];
                    Log.d(LOG_TAG, "new fail status!return false,dont report chr.");
                    return false;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: isNeedReportChrForSameFailStatus !!" + e);
            }
        }
        return true;
    }

    private void sendIntentWithPdpIpType(int subId) {
        Intent intent = new Intent("com.android.intent.action.pdp_act_ip_type");
        intent.putExtra("subscription", subId);
        intent.putExtra("pdpActIpType", getPdpActiveIpType());
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    public void SendIntentDNSfailure(String[] dnses) {
        if (dnses == null || dnses.length == 0) {
            Log.d(LOG_TAG, " send DNSfailureIntent,check dnses is null");
            mContext.sendBroadcast(new Intent("com.intent.action.dns_fail"), "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    private boolean isUserModifyApnSetting() {
        String[] selections = new String[]{"visible<>1", "visible is null"};
        Uri uri = Carriers.CONTENT_URI;
        int length = selections.length;
        int i = 0;
        while (i < length) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, selections[i], null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                i++;
            } else {
                Log.d(LOG_TAG, "User ever add new APN!");
                cursor.close();
                return true;
            }
        }
        Log.d(LOG_TAG, "User never add APN setting!");
        return false;
    }

    public void sendIntentApnListEmpty(int subId) {
        Log.d(LOG_TAG, " send ApnListEmpty");
        Intent intent = new Intent("com.intent.action.apn_list_empty");
        intent.putExtra("subscription", subId);
        intent.putExtra("userModifyApnList", isUserModifyApnSetting() ? 1 : 0);
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    public void sendIntentDataConnectionSetupResult(int subId, String state, String reason, String apn, String apnType, LinkProperties linkProperties) {
        Log.d(LOG_TAG, " send sendIntentDataConnectionSetupResult");
        Intent intent = new Intent("com.intent.action.data_connection_setup_result");
        intent.putExtra("subscription", subId);
        intent.putExtra("state", state);
        if (reason != null) {
            intent.putExtra("reason", reason);
        }
        if (apn != null) {
            intent.putExtra("apn", apn);
        }
        if (apnType != null) {
            intent.putExtra("apnType", apnType);
        }
        if (linkProperties != null) {
            intent.putExtra("linkProperties", linkProperties);
            String iface = linkProperties.getInterfaceName();
            if (iface != null) {
                intent.putExtra("iface", iface);
            }
        }
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
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
                    mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
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
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    private void sendIntentWhenPdpActFailBlockAtFw(int subId) {
        Intent intent = new Intent("com.android.intent.action.pdp_fail_block_at_fw");
        intent.putExtra("subscription", subId);
        intent.putExtra("AnyDataEnabledFlag", getAnyDataEnabledFalseReason());
        intent.putExtra("DataNotAllowedReason", getDataNotAllowedReason());
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    private Timer startTimer(Timer timer, int timerInterval) {
        Exception e;
        Log.d(LOG_TAG, "startTimer!getSubId =" + this.mDataSubId + ",SubscriptionManager.getDefaultSubscriptionId() =" + SubscriptionManager.getDefaultSubscriptionId());
        try {
            Timer timer2 = new Timer();
            try {
                timer2.schedule(new TimerTask() {
                    public void run() {
                        Log.d(HwDataServiceChrManagerImpl.LOG_TAG, "TimerTask run enter");
                        HwDataServiceChrManagerImpl.this.mHandler.sendMessage(HwDataServiceChrManagerImpl.this.mHandler.obtainMessage(1));
                    }
                }, (long) timerInterval);
                return timer2;
            } catch (Exception e2) {
                e = e2;
                timer = timer2;
                Log.e(LOG_TAG, "Exception: startTimer fail! " + e);
                return null;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(LOG_TAG, "Exception: startTimer fail! " + e);
            return null;
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
        if (subId < 0 || subId >= 3) {
            Log.e(LOG_TAG, "invalid subId: " + subId);
            return;
        }
        Log.d(LOG_TAG, "CHR APN setttings = " + apn);
        Log.d(LOG_TAG, "CHR Report flag: subId = " + subId + ", default flag = " + this.mDefaultAPNReported[subId] + ", mms flag = " + this.mMmsAPNReported[subId] + ", dun flag = " + this.mDunAPNReported[subId]);
        if (apn == null) {
            Log.e(LOG_TAG, "CHR apn is null ");
        } else if (isNeedToReport(apn, subId)) {
            int hasUserPassword = 0;
            if (!(apn.user == null || apn.user.length() == 0)) {
                hasUserPassword = 1;
            }
            int hasDns = 0;
            if (linkProperties.getDnsServers().size() != 0) {
                hasDns = 1;
            }
            int chrRilRat = phone.getServiceState().getRilDataRadioTechnology();
            Log.d(LOG_TAG, "CHR Report flag after processing the types : default flag = " + this.mDefaultAPNReported[subId] + " , mms flag = " + this.mMmsAPNReported[subId] + ", dun flag = " + this.mDunAPNReported[subId]);
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
                mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
            }
        } else {
            Log.d(LOG_TAG, "CHR do not need to report , ignore it ");
        }
    }

    private boolean isNeedToReport(ApnSetting apn, int subId) {
        boolean needToReport = false;
        for (String t : apn.types) {
            if (t.equalsIgnoreCase("default")) {
                if (!this.mDefaultAPNReported[subId]) {
                    needToReport = true;
                    this.mDefaultAPNReported[subId] = true;
                }
            } else if (t.equalsIgnoreCase("mms")) {
                if (!this.mMmsAPNReported[subId]) {
                    needToReport = true;
                    this.mMmsAPNReported[subId] = true;
                }
            } else if (t.equalsIgnoreCase("dun")) {
                if (!this.mDunAPNReported[subId]) {
                    needToReport = true;
                    this.mDunAPNReported[subId] = true;
                }
            } else if (t.equalsIgnoreCase("*")) {
                boolean z;
                if (this.mDefaultAPNReported[subId] && this.mMmsAPNReported[subId]) {
                    z = this.mDunAPNReported[subId];
                } else {
                    z = false;
                }
                if (!z) {
                    needToReport = true;
                    this.mDefaultAPNReported[subId] = true;
                    this.mMmsAPNReported[subId] = true;
                    this.mDunAPNReported[subId] = true;
                }
            }
        }
        return needToReport;
    }

    private void sendBroadcastDelayed(Intent intent, long delayedTimer) {
        Log.d(LOG_TAG, "CHR sendBroadcastDelayed delayedTimer = " + delayedTimer);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, intent), delayedTimer);
    }

    public void sendMonitorWifiSwitchToMobileMessage(int delayInterval) {
        Log.d(LOG_TAG, "wifi disconnect, sendMonitorWifiSwitchToMobileMessage!");
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), (long) delayInterval);
    }

    public void removeMonitorWifiSwitchToMobileMessage() {
        this.mHandler.removeMessages(3);
    }

    private boolean isWifiConnected() {
        if (mContext != null) {
            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService("connectivity");
            if (connManager != null) {
                NetworkInfo wifiInfo = connManager.getNetworkInfo(1);
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
