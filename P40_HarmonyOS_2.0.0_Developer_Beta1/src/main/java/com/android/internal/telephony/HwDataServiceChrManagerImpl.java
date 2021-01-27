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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.util.Log;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.huawei.internal.telephony.SubscriptionControllerEx;
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
    private static final int EXCEPTION_REASON_PS_BASE = 131072;
    private static final int GET_DATA_CALL_LIST = 0;
    private static final String INTENT_DS_NO_DEFAULT_APN = "com.huawei.intent.action.no_default_apn";
    private static final String INTENT_DS_PDN_CURE_REPORT = "com.huawei.intent.action.pdn_cure_chr_report";
    private static final String LOG_TAG = "HwDataServiceChrManagerImpl";
    private static final int MAX_PHONENUM = 3;
    private static final int MAX_SLOT = 3;
    private static final String NETWORK_APN = "network_apn";
    private static final int RADIO_RESTART = 3;
    private static final int RADIO_RESTART_WITH_PROP = 4;
    private static final int REREGISTER = 2;
    private static final int TIMER_INTERVAL_CDMA_PDP_SAME_STATUS = 60000;
    private static final int TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED = 180000;
    private static final long TIMER_INTERVAL_SEND_APN_INFO_INTENT = 3000;
    private static final int WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL = 20000;
    private static Context mContext;
    private static HwDataServiceChrManager mInstance = new HwDataServiceChrManagerImpl();
    private Phone[] mActivePhone = new Phone[3];
    private ApnChangeObserver mApnObserver;
    private Timer mCdmaPdpSameStatusTimer;
    private GsmCdmaPhone mCdmaPhone;
    private boolean mCheckApnContextState = false;
    private int mChrCdmaPdpRilFailCause;
    private String mDataNotAllowedReason = null;
    private int mDataSubId;
    private ApnContext[] mDefaultApnContext = new ApnContext[3];
    private boolean[] mDefaultApnReported = {false, false, false};
    private boolean[] mDisableNrAllowApnReport = {false, false, false};
    private boolean[] mDunApnReported = {false, false, false};
    private String mGetAnyDataEnabledFalseReason = null;
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.HwDataServiceChrManagerImpl.AnonymousClass1 */

        private boolean isDataSlotIdVaild(int dataSlotId) {
            return dataSlotId >= 0 && dataSlotId < 3;
        }

        private boolean isDataSubIdVaild(int dataSubId) {
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            if (subscriptionController != null && dataSubId == subscriptionController.getDefaultDataSubId()) {
                return isDataSlotIdVaild(subscriptionController.getSlotIndex(dataSubId));
            }
            return false;
        }

        private boolean isPdpActFailBlockAtFramework(int dataSlotId) {
            if (isDataSlotIdVaild(dataSlotId) && !HwDataServiceChrManagerImpl.this.getBringUp() && HwDataServiceChrManagerImpl.this.mDefaultApnContext[dataSlotId] != null && HwDataServiceChrManagerImpl.this.mActivePhone[dataSlotId] != null && HwDataServiceChrManagerImpl.this.mDefaultApnContext[dataSlotId].isEnabled() && HwDataServiceChrManagerImpl.this.mUserDataEnabled && HwDataServiceChrManagerImpl.this.mActivePhone[dataSlotId].getServiceState().getDataRegState() == 0) {
                return true;
            }
            return false;
        }

        private void onTimerExpire() {
            HwDataServiceChrManagerImpl.logd("received EVENT_TIMER_EXPIRE message!isReceivedEventRecordsLoaded = " + HwDataServiceChrManagerImpl.this.getReceivedSimloadedMsg());
            HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl = HwDataServiceChrManagerImpl.this;
            hwDataServiceChrManagerImpl.stopTimer(hwDataServiceChrManagerImpl.mSimloadedTimer);
            HwDataServiceChrManagerImpl.this.mSimloadedTimer = null;
            HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl2 = HwDataServiceChrManagerImpl.this;
            hwDataServiceChrManagerImpl2.stopTimer(hwDataServiceChrManagerImpl2.mCdmaPdpSameStatusTimer);
            HwDataServiceChrManagerImpl.this.mCdmaPdpSameStatusTimer = null;
            HwDataServiceChrManagerImpl.this.cleanLastStatus();
            if (TelephonyManager.getDefault() == null) {
                HwDataServiceChrManagerImpl.loge("Timer expire, TelephonyManager is null!");
                return;
            }
            HwDataServiceChrManagerImpl.this.mUserDataEnabled = TelephonyManager.getDefault().getDataEnabled();
            HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl3 = HwDataServiceChrManagerImpl.this;
            if (hwDataServiceChrManagerImpl3.getSimCardState(hwDataServiceChrManagerImpl3.mDataSubId) != 1) {
                HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl4 = HwDataServiceChrManagerImpl.this;
                if (hwDataServiceChrManagerImpl4.getSimCardState(hwDataServiceChrManagerImpl4.mDataSubId) != 0) {
                    SubscriptionController subscriptionController = SubscriptionController.getInstance();
                    SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
                    if (subscriptionController == null) {
                        HwDataServiceChrManagerImpl.loge("onTimerExpire SubscriptionController is null ");
                        return;
                    }
                    int dataSlotId = subscriptionController.getSlotIndex(HwDataServiceChrManagerImpl.this.mDataSubId);
                    if (subscriptionControllerEx.getSubState(dataSlotId) == 0) {
                        HwDataServiceChrManagerImpl.logd("startTimer again,getSubState = INACTIVE");
                        HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl5 = HwDataServiceChrManagerImpl.this;
                        hwDataServiceChrManagerImpl5.mSimloadedTimer = hwDataServiceChrManagerImpl5.startTimer(HwDataServiceChrManagerImpl.TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED);
                        return;
                    } else if (isDataSubIdVaild(HwDataServiceChrManagerImpl.this.mDataSubId)) {
                        HwDataServiceChrManagerImpl.logd("isBringUp=" + HwDataServiceChrManagerImpl.this.getBringUp() + " dataSlotId =" + dataSlotId + " mDataSubId=" + HwDataServiceChrManagerImpl.this.mDataSubId + " mUserDataEnabled=" + HwDataServiceChrManagerImpl.this.mUserDataEnabled + " isDataConnectionAttached=" + HwDataServiceChrManagerImpl.this.mIsDataConnectionAttached);
                        if (HwDataServiceChrManagerImpl.this.getRecordsLoadedRegistered() && !HwDataServiceChrManagerImpl.this.getReceivedSimloadedMsg()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Timer expire,simloaded msg is not received,sim card is present!trigger chr! SlotId:");
                            sb.append(dataSlotId);
                            sb.append(" mDataSubId:");
                            sb.append(HwDataServiceChrManagerImpl.this.mDataSubId);
                            sb.append(" getSimCardState = ");
                            HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl6 = HwDataServiceChrManagerImpl.this;
                            sb.append(hwDataServiceChrManagerImpl6.getSimCardState(hwDataServiceChrManagerImpl6.mDataSubId));
                            HwDataServiceChrManagerImpl.logd(sb.toString());
                            HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl7 = HwDataServiceChrManagerImpl.this;
                            hwDataServiceChrManagerImpl7.sendIntentWhenSimloadedMsgIsNotReceived(hwDataServiceChrManagerImpl7.mDataSubId);
                            return;
                        } else if (!isPdpActFailBlockAtFramework(dataSlotId)) {
                            HwDataServiceChrManagerImpl.logd("Timer expire,pdp activing process return error,when data switch is on and ps is attached!");
                            return;
                        } else {
                            HwDataServiceChrManagerImpl.logd("defaultApnContext.isEnabled()=" + HwDataServiceChrManagerImpl.this.mDefaultApnContext[dataSlotId].isEnabled());
                            HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl8 = HwDataServiceChrManagerImpl.this;
                            hwDataServiceChrManagerImpl8.sendIntentWhenPdpActFailBlockAtFw(hwDataServiceChrManagerImpl8.mDataSubId);
                            HwDataServiceChrManagerImpl.this.setDataNotAllowedReasonToNull();
                            HwDataServiceChrManagerImpl.this.setAnyDataEnabledFalseReasonToNull();
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            HwDataServiceChrManagerImpl.logd("Timer expire,sim card is absent!");
        }

        private void onWifiDisconnectTimeExpire(Message msg) {
            if (SubscriptionController.getInstance() == null) {
                HwDataServiceChrManagerImpl.loge("onWifiDisconnectTimeExpire SubscriptionController is null ");
                return;
            }
            int dataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            int dataSlotId = SubscriptionController.getInstance().getSlotIndex(dataSubId);
            if (dataSlotId >= 3 || dataSlotId < 0) {
                HwDataServiceChrManagerImpl.loge("EVENT_WIFI_DISCONNECT_TIMER_EXPIRE dataSubId invaild");
                return;
            }
            HwDataServiceChrManagerImpl.logd("handleMessage: dataSubId=" + dataSubId + " dataSlotId=" + dataSlotId + " msg=" + msg);
            if (HwDataServiceChrManagerImpl.mContext == null) {
                HwDataServiceChrManagerImpl.loge("onWifiDisconnectTimeExpire mContext is null");
            } else if (HwDataServiceChrManagerImpl.this.mDefaultApnContext[dataSlotId] == null || HwDataServiceChrManagerImpl.this.mDefaultApnContext[dataSlotId].isEnabled() || HwDataServiceChrManagerImpl.this.isWifiConnected()) {
                HwDataServiceChrManagerImpl.logd("wifi to mobile process is ok, DefaultTypeAPN is enabled");
            } else {
                Intent apkIntent = new Intent("com.huawei.intent.action.wifi_switchto_mobile_fail");
                apkIntent.putExtra("subscription", dataSlotId);
                HwDataServiceChrManagerImpl.mContext.sendBroadcast(apkIntent, "com.huawei.android.permission.GET_CHR_DATA");
            }
        }

        private void onApnChanged() {
            HwDataServiceChrManagerImpl.logd("CHR set all type APNReported as false because of apn changed");
            for (int slotId = 0; slotId < 3; slotId++) {
                HwDataServiceChrManagerImpl.this.mDefaultApnReported[slotId] = false;
                HwDataServiceChrManagerImpl.this.mMmsApnReported[slotId] = false;
                HwDataServiceChrManagerImpl.this.mDunApnReported[slotId] = false;
                HwDataServiceChrManagerImpl.this.mNetworkApnReported[slotId] = false;
                HwDataServiceChrManagerImpl.this.mNetworkApnUsedReported[slotId] = false;
            }
        }

        private void onReceivedApnConnectionInfo(Message msg) {
            HwDataServiceChrManagerImpl.logd("CHR received the event EVENT_APN_CONNECTION_INFO_INTENT, then send the intent");
            if (HwDataServiceChrManagerImpl.mContext == null) {
                HwDataServiceChrManagerImpl.loge("onReceivedApnConnectionInfo mContext is null");
            } else if (msg != null && (msg.obj instanceof Intent)) {
                HwDataServiceChrManagerImpl.mContext.sendBroadcast((Intent) msg.obj, "com.huawei.android.permission.GET_CHR_DATA");
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                onTimerExpire();
            } else if (i == 2) {
                HwDataServiceChrManagerImpl.logd("Ignore EVENT_GET_CDMA_CHR_INFO");
            } else if (i == 3) {
                onWifiDisconnectTimeExpire(msg);
            } else if (i == 4) {
                onApnChanged();
            } else if (i == 5) {
                onReceivedApnConnectionInfo(msg);
            }
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwDataServiceChrManagerImpl.AnonymousClass2 */

        private void onSimStateChanged(Intent intent) {
            if (SubscriptionController.getInstance() == null) {
                HwDataServiceChrManagerImpl.loge("onSimStateChanged SubscriptionController is null ");
                return;
            }
            int subId = intent.getIntExtra("subscription", -1);
            String simState = intent.getStringExtra("ss");
            if (simState != null && simState.equals("LOADED") && subId == SubscriptionController.getInstance().getDefaultDataSubId()) {
                HwDataServiceChrManagerImpl.logd("ACTION_SIM_STATE_CHANGED: simState=" + simState + " SUBSCRIPTION_KEY = " + subId);
                HwDataServiceChrManagerImpl.this.mDataSubId = subId;
                if (HwDataServiceChrManagerImpl.this.mSimloadedTimer == null) {
                    HwDataServiceChrManagerImpl hwDataServiceChrManagerImpl = HwDataServiceChrManagerImpl.this;
                    hwDataServiceChrManagerImpl.mSimloadedTimer = hwDataServiceChrManagerImpl.startTimer(HwDataServiceChrManagerImpl.TIMER_INTERVAL_CHECK_SIMLOADED_ISRECEIVED);
                }
            }
        }

        private void onWifiStateChanged(Intent intent) {
            if (HwDataServiceChrManagerImpl.this.mIsWifiConnected && intent.getIntExtra("wifi_state", 4) == 1) {
                HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                HwDataServiceChrManagerImpl.this.sendMonitorWifiSwitchToMobileMessage(HwDataServiceChrManagerImpl.WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL);
                HwDataServiceChrManagerImpl.this.mIsWifiConnected = false;
            }
            HwDataServiceChrManagerImpl.logd("mIsWifiConnected = " + HwDataServiceChrManagerImpl.this.mIsWifiConnected);
        }

        private void onNetworkStateChanged(Intent intent) {
            if (intent.getParcelableExtra("networkInfo") instanceof NetworkInfo) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo != null) {
                    if (HwDataServiceChrManagerImpl.this.mIsWifiConnected && networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                        HwDataServiceChrManagerImpl.this.sendMonitorWifiSwitchToMobileMessage(HwDataServiceChrManagerImpl.WIFI_TO_MOBILE_MONITOR_TIMER_INTERVAL);
                    } else if (!HwDataServiceChrManagerImpl.this.mIsWifiConnected && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        HwDataServiceChrManagerImpl.this.removeMonitorWifiSwitchToMobileMessage();
                    }
                    HwDataServiceChrManagerImpl.this.mIsWifiConnected = networkInfo.isConnected();
                }
                HwDataServiceChrManagerImpl.logd("mIsWifiConnected = " + HwDataServiceChrManagerImpl.this.mIsWifiConnected);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwDataServiceChrManagerImpl.loge("onReceive: intent is invalid.");
                return;
            }
            String action = intent.getAction();
            HwDataServiceChrManagerImpl.logd("onReceive: action=" + action);
            if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                onSimStateChanged(intent);
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                onWifiStateChanged(intent);
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                onNetworkStateChanged(intent);
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
    private boolean[] mMmsApnReported = {false, false, false};
    private boolean[] mNetworkApnReported = {false, false, false};
    private boolean[] mNetworkApnUsedReported = {false, false, false};
    private int mSameStatusTimes = 0;
    private Timer mSimloadedTimer;
    private boolean mUserDataEnabled = false;

    public static HwDataServiceChrManager getDefault() {
        return mInstance;
    }

    /* access modifiers changed from: private */
    public static void logd(String str) {
        Log.i(LOG_TAG, str);
    }

    /* access modifiers changed from: private */
    public static void loge(String str) {
        Log.e(LOG_TAG, str);
    }

    public void init(Context context) {
        if (context == null) {
            loge("context is empty, init fail!");
        } else if (context.getContentResolver() == null) {
            loge("getContentResolver is empty, init fail!");
        } else {
            mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SIM_STATE_CHANGED");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            mContext.registerReceiver(this.mIntentReceiver, filter);
            this.mApnObserver = new ApnChangeObserver();
            mContext.getContentResolver().registerContentObserver(Telephony.Carriers.CONTENT_URI, true, this.mApnObserver);
        }
    }

    public void setDataNotAllowedReasonToNull() {
        this.mDataNotAllowedReason = "";
    }

    public String getDataNotAllowedReason() {
        return this.mDataNotAllowedReason;
    }

    public void setAnyDataEnabledFalseReason(boolean internalDataEnabled, boolean userDataEnabled, boolean sPolicyDataEnabled, boolean checkUserDataEnabled) {
        String reason = internalDataEnabled + "," + userDataEnabled + "," + sPolicyDataEnabled + "," + checkUserDataEnabled;
        logd("DcTrackerBse setAnyDataEnabledFalseReason,flag=" + reason);
        this.mGetAnyDataEnabledFalseReason = reason;
    }

    public void setAnyDataEnabledFalseReasonToNull() {
        this.mGetAnyDataEnabledFalseReason = "";
    }

    public String getAnyDataEnabledFalseReason() {
        return this.mGetAnyDataEnabledFalseReason;
    }

    public boolean getBringUp() {
        return this.mIsBringUp;
    }

    public void setBringUp(boolean isBringUp) {
        this.mIsBringUp = isBringUp;
    }

    public void setReceivedSimloadedMsg(Phone phone, boolean isReceivedSimloadedMsg, ConcurrentHashMap<String, ApnContext> apnContexts, boolean userDataEnabled) {
        if (phone == null || apnContexts == null) {
            loge("setReceivedSimloadedMsg Phone is null ");
            return;
        }
        int slotId = phone.getPhoneId();
        if (slotId >= 3 || slotId < 0) {
            loge("setReceivedSimloadedMsg sub is invaild ");
            return;
        }
        this.mActivePhone[slotId] = phone;
        this.mIsReceivedSimloadedMsg = isReceivedSimloadedMsg;
        this.mDefaultApnContext[slotId] = apnContexts.get("default");
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
        if (phone == null) {
            loge("getModemParamsWhenCdmaPdpActFail Phone is null ");
            return;
        }
        try {
            if (phone.getPhoneType() != 1 && (phone instanceof GsmCdmaPhone)) {
                this.mCdmaPhone = (GsmCdmaPhone) phone;
                String isSupportCdmaChr = SystemProperties.get("ro.sys.support_cdma_chr", "false");
                logd("EVENT_GET_LAST_FAIL_DONE isSupportCdmaChr = " + isSupportCdmaChr);
                if ("true".equals(isSupportCdmaChr)) {
                    logd("CDMAPhone pdp active fail");
                    this.mChrCdmaPdpRilFailCause = rilFailCause - EXCEPTION_REASON_PS_BASE;
                }
            }
        } catch (ClassCastException e) {
            loge("ClassCastException: getModemParamsWhenCdmaPdpActFail fail!");
        }
    }

    public void sendIntentWhenDisableNr(Phone phone, int cause, long backoffTime) {
        if (phone == null) {
            loge("sendIntentWhenDisableNr Phone is null");
            return;
        }
        Context context = phone.getContext();
        if (context == null) {
            loge("sendIntentWhenDisableNr context is null");
            return;
        }
        int slotId = phone.getPhoneId();
        Intent intent = new Intent("com.android.intent.action.disable_nr");
        this.mDisableNrAllowApnReport[slotId] = true;
        Long disableNrTimeLen = Long.valueOf(backoffTime);
        intent.putExtra("subscription", slotId);
        intent.putExtra("DisableNrCause", cause);
        intent.putExtra("DisableNrTimeLength", disableNrTimeLen.intValue());
        context.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    public void sendIntentWhenReenableNr(Phone phone) {
        if (phone == null) {
            loge("sendIntentWhenReenableNr Phone is null");
            return;
        }
        Context context = phone.getContext();
        if (context == null) {
            loge("sendIntentWhenReenableNr context is null");
            return;
        }
        int slotId = phone.getPhoneId();
        Intent intent = new Intent("com.android.intent.action.reenable_nr");
        this.mDisableNrAllowApnReport[slotId] = true;
        intent.putExtra("subscription", slotId);
        context.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    public void sendIntentWhenDorecovery(Phone phone, int recoveryAction, String recoveryReason) {
        if (phone == null) {
            loge("sendIntentWhenDorecovery Phone is null ");
        } else if (phone.getServiceState() == null) {
            loge("sendIntentWhenDorecovery Phone ServiceState is null ");
        } else if (phone.getServiceState().getDataRegState() == 0 && recoveryAction > 0) {
            logd("sendIntentWhenDorecovery recoveryAction = " + recoveryAction + ", recoveryReason = " + recoveryReason);
            Context context = phone.getContext();
            if (context == null) {
                loge("sendIntentWhenDorecovery context is null");
                return;
            }
            int slotId = phone.getPhoneId();
            Intent intent = new Intent("com.huawei.intent.action.do_recovery");
            intent.putExtra("subscription", slotId);
            intent.putExtra("recoveryAction", recoveryAction);
            intent.putExtra("recoveryReason", recoveryReason);
            context.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    public void sendIntentDsUseStatistics(Phone phone, int duration) {
        logd("sendIntentDsUseStatistics duration = " + duration);
        if (phone == null) {
            loge("sendIntentDsUseStatistics phone is empty!");
            return;
        }
        Context context = phone.getContext();
        if (context == null) {
            loge("sendIntentDsUseStatistics context is empty!");
            return;
        }
        int slotId = phone.getPhoneId();
        Intent intent = new Intent("com.huawei.intent.action.use_statistics");
        intent.putExtra("subscription", slotId);
        intent.putExtra("duration", duration);
        context.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    public void sendIntentWhenSetDataSubFail(int subId) {
        if (mContext != null) {
            Intent intent = new Intent("com.huawei.intent.action.set_data_sub_fail");
            intent.putExtra("subscription", subId);
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    public void sendIntentApnContextDisabledWhenWifiDisconnected(Phone phone, boolean isWifiConnected, boolean userDataEnabled, ApnContext apnContext) {
        if (phone == null || apnContext == null) {
            loge("sendIntentApnContextDisabledWhenWifiDisconnected Phone is null ");
        } else if (this.mCheckApnContextState && apnContext.getApnType().equals("default") && !isWifiConnected && getReceivedSimloadedMsg() && userDataEnabled && !apnContext.isEnabled() && !ignoreReport(phone) && SubscriptionController.getInstance() != null && phone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
            logd("ready to trigger chr: apnContext = " + apnContext.toString());
            Intent intent = new Intent("com.intent.action.apn_disable_while_wifi_disconnect");
            intent.putExtra("subscription", phone.getPhoneId());
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    public void sendIntentHasNoDefaultApn(int subId) {
        if (mContext != null) {
            logd("sendIntentHasNoDefaultApn.");
            Intent intent = new Intent(INTENT_DS_NO_DEFAULT_APN);
            intent.putExtra("subscription", subId);
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    private boolean isLabCard() {
        if (TelephonyManager.getDefault() == null) {
            loge("isLabCard: TelephonyManager is null!");
            return false;
        }
        String hplmn = TelephonyManager.getDefault().getSimOperator();
        if (hplmn == null || hplmn.equals("")) {
            loge("Exception: isLabCard fail!");
            return false;
        }
        for (String tmp : new String[]{"46060", "00101"}) {
            if (tmp.equals(hplmn)) {
                logd("Lab card, Ignore report CHR event!");
                return true;
            }
        }
        return false;
    }

    private boolean isCardReady(Phone phone) {
        if (phone == null) {
            loge("isCardReady Phone is null ");
            return false;
        }
        SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
        if (subscriptionControllerEx == null) {
            loge("isCardReady SubscriptionController is null ");
            return false;
        } else if (phone.getSubId() != SubscriptionManager.getDefaultSubscriptionId() || subscriptionControllerEx.getSubState(phone.getPhoneId()) == 0) {
            logd("isCardReady return false,subid != getDataSubscription or INACTIVE == getSubState");
            return false;
        } else if (getSimCardState(phone.getSubId()) == 5) {
            return true;
        } else {
            return false;
        }
    }

    private boolean ignoreReport(Phone phone) {
        if (phone == null) {
            loge("ignoreReport, Phone is empty!");
            return true;
        } else if (!isCardReady(phone)) {
            logd("Card not Ready! Ignore report CHR event!");
            return true;
        } else if (isLabCard()) {
            logd("isLabCard! Ignore report CHR event!");
            return true;
        } else if (phone.getServiceState().getRilDataRadioTechnology() != 0 && phone.getServiceState().getDataRegState() == 0) {
            return false;
        } else {
            logd("ignoreReport: ps domain is not attached, skipped");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanLastStatus() {
        this.mLastDataCallFailStatus = 0;
        this.mSameStatusTimes = 0;
    }

    public void sendIntentDnsFailure(String[] dnses) {
        if ((dnses == null || dnses.length == 0) && mContext != null) {
            logd(" send DNSfailureIntent,check dnses is null");
            mContext.sendBroadcast(new Intent("com.intent.action.dns_fail"), "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    private boolean isUserModifyApnSetting() {
        Uri uri = Telephony.Carriers.CONTENT_URI;
        for (String sel : new String[]{"visible<>1", "visible is null"}) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, sel, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                logd("User ever add new APN!");
                cursor.close();
                return true;
            }
        }
        logd("User never add APN setting!");
        return false;
    }

    public void sendIntentApnListEmpty(int subId) {
        if (mContext != null) {
            logd(" send ApnListEmpty");
            Intent intent = new Intent("com.intent.action.apn_list_empty");
            intent.putExtra("subscription", subId);
            intent.putExtra("userModifyApnList", isUserModifyApnSetting() ? 1 : 0);
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendIntentWhenSimloadedMsgIsNotReceived(int subId) {
        if (mContext != null) {
            Intent intent = new Intent("com.huawei.intent.action.simloaded_msg_not_received");
            intent.putExtra("subscription", subId);
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendIntentWhenPdpActFailBlockAtFw(int subId) {
        if (mContext != null) {
            Intent intent = new Intent("com.huawei.intent.action.pdp_fail_block_at_fw");
            intent.putExtra("subscription", subId);
            intent.putExtra("AnyDataEnabledFlag", getAnyDataEnabledFalseReason());
            intent.putExtra("DataNotAllowedReason", getDataNotAllowedReason());
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Timer startTimer(int timerInterval) {
        logd("startTimer!getSubId =" + this.mDataSubId + ",SubscriptionManager.getDefaultSubscriptionId() =" + SubscriptionManager.getDefaultSubscriptionId());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            /* class com.android.internal.telephony.HwDataServiceChrManagerImpl.AnonymousClass3 */

            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                HwDataServiceChrManagerImpl.logd("TimerTask run enter");
                if (HwDataServiceChrManagerImpl.this.mHandler != null) {
                    HwDataServiceChrManagerImpl.this.mHandler.sendMessage(HwDataServiceChrManagerImpl.this.mHandler.obtainMessage(1));
                }
            }
        }, (long) timerInterval);
        return timer;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopTimer(Timer timer) {
        if (timer != null) {
            logd("mTimer!=null");
            timer.cancel();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getSimCardState(int subId) {
        if (TelephonyManager.getDefault() == null) {
            loge("getSimCardState TelephonyManager is null");
            return 0;
        } else if (SubscriptionController.getInstance() == null) {
            loge("getSimCardState SubscriptionController is null");
            return 0;
        } else {
            return TelephonyManager.getDefault().getSimState(SubscriptionController.getInstance().getSlotIndex(subId));
        }
    }

    public void sendIntentWhenApnNeedReport(Phone phone, ApnSetting apn, int apnTypes, LinkProperties linkProperties) {
        ApnSetting chrApnSettings = convertToChrApn(apn, apnTypes);
        if (chrApnSettings != null) {
            logd("report Attach APN");
            sendIntentWhenDataConnected(phone, chrApnSettings, linkProperties);
        }
    }

    private ApnSetting convertToChrApn(ApnSetting attachedApnSettings, int apnTypes) {
        ApnSetting apn = new ApnSetting.Builder().setOperatorNumeric(attachedApnSettings.getOperatorNumeric()).setApnName(attachedApnSettings.getApnName()).setEntryName(NETWORK_APN).setApnTypeBitmask(apnTypes).setProtocol(attachedApnSettings.getProtocol()).buildWithoutCheck();
        logd("convertToChrApn, Chr APN is:" + apn);
        return apn;
    }

    public void sendIntentWhenDataConnected(Phone phone, ApnSetting apn, LinkProperties linkProperties) {
        if (phone == null || apn == null || linkProperties == null) {
            loge("sendIntentWhenDataConnected paras is null ");
            return;
        }
        int slotId = phone.getPhoneId();
        if (slotId < 0 || slotId >= 3) {
            loge("invalid slotId: " + slotId);
            return;
        }
        logd("CHR APN setttings = " + apn);
        logd("CHR Report flag: slotId = " + slotId + ", default flag = " + this.mDefaultApnReported[slotId] + ", mms flag = " + this.mMmsApnReported[slotId] + ", dun flag = " + this.mDunApnReported[slotId] + ", network flag = " + this.mNetworkApnReported[slotId]);
        if (isNeedToReport(apn, slotId)) {
            int hasUserPassword = 0;
            if (!(apn.getUser() == null || apn.getUser().length() == 0)) {
                hasUserPassword = 1;
            }
            int hasDns = 0;
            if (linkProperties.getDnsServers().size() != 0) {
                hasDns = 1;
            }
            int chrRilRat = phone.getServiceState().getRilDataRadioTechnology();
            logd("CHR Report flag after processing the types : default flag = " + this.mDefaultApnReported[slotId] + " , mms flag = " + this.mMmsApnReported[slotId] + ", dun flag = " + this.mDunApnReported[slotId] + ", network flag = " + this.mNetworkApnReported[slotId]);
            StringBuilder sb = new StringBuilder();
            sb.append("CHR chrRilRat = ");
            sb.append(chrRilRat);
            logd(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("CHR send apn info intent mIsFirstReport = ");
            sb2.append(this.mIsFirstReport);
            logd(sb2.toString());
            Intent intent = new Intent("com.intent.action.APN_CONNECTION_INFO");
            intent.putExtra("subscription", phone.getPhoneId());
            intent.putExtra("rilRat", chrRilRat);
            intent.putExtra("apnUserPassword", hasUserPassword);
            intent.putExtra("linkDns", hasDns);
            intent.putExtras(parseBundleFromApn(apn));
            if (this.mIsFirstReport) {
                sendBroadcastDelayed(intent, TIMER_INTERVAL_SEND_APN_INFO_INTENT);
                this.mIsFirstReport = false;
                return;
            }
            mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
            return;
        }
        logd("CHR do not need to report , ignore it ");
    }

    private Bundle parseBundleFromApn(ApnSetting apn) {
        Bundle bundle = new Bundle();
        bundle.putString("Carrier", apn.getEntryName());
        bundle.putString("Apn", apn.getApnName());
        bundle.putString("Proxy", apn.getProxyAddressAsString());
        bundle.putString("Port", String.valueOf(apn.getProxyPort()));
        Uri mmsc = apn.getMmsc();
        if (mmsc != null) {
            bundle.putString("Mmsc", mmsc.toString());
        }
        bundle.putString("MmsPorxy", apn.getMmsProxyAddressAsString());
        bundle.putString("MmsPort", String.valueOf(apn.getMmsProxyPort()));
        bundle.putString("Numeric", apn.getOperatorNumeric());
        bundle.putInt("AuthType", apn.getAuthType());
        bundle.putString("ApnTypes", ApnSetting.getApnTypesStringFromBitmask(apn.getApnTypeBitmask()));
        bundle.putInt("Protocol", apn.getProtocol());
        bundle.putInt("RoamingProtocol", apn.getRoamingProtocol());
        bundle.putBoolean("CarrierEnabled", apn.isEnabled());
        bundle.putInt("Bearer", apn.getId());
        bundle.putInt("BearerBitmask", apn.getApnTypeBitmask());
        bundle.putString("MvnoType", String.valueOf(apn.getMvnoType()));
        bundle.putString("MvnoMatchData", apn.getMvnoMatchData());
        return bundle;
    }

    private boolean isNeedToReport(ApnSetting apn, int slotId) {
        String[] split = ApnSetting.getApnTypesStringFromBitmask(apn.getApnTypeBitmask()).split(",");
        boolean needToReport = false;
        for (String t : split) {
            if (t.equalsIgnoreCase("default")) {
                if (!this.mDefaultApnReported[slotId] || this.mDisableNrAllowApnReport[slotId]) {
                    needToReport = true;
                    this.mDefaultApnReported[slotId] = true;
                    this.mDisableNrAllowApnReport[slotId] = false;
                }
            } else if (t.equalsIgnoreCase("mms")) {
                boolean[] zArr = this.mMmsApnReported;
                if (!zArr[slotId]) {
                    needToReport = true;
                    zArr[slotId] = true;
                }
            } else if (t.equalsIgnoreCase("dun")) {
                boolean[] zArr2 = this.mDunApnReported;
                if (!zArr2[slotId]) {
                    needToReport = true;
                    zArr2[slotId] = true;
                }
            } else if (t.equalsIgnoreCase("emergency")) {
                boolean[] zArr3 = this.mNetworkApnUsedReported;
                if (!zArr3[slotId]) {
                    needToReport = true;
                    zArr3[slotId] = true;
                }
            } else if (t.equalsIgnoreCase("mcx")) {
                boolean[] zArr4 = this.mNetworkApnReported;
                if (!zArr4[slotId]) {
                    needToReport = true;
                    zArr4[slotId] = true;
                }
            } else if (t.equalsIgnoreCase("*") && (!this.mDefaultApnReported[slotId] || !this.mMmsApnReported[slotId] || !this.mDunApnReported[slotId])) {
                needToReport = true;
                this.mDefaultApnReported[slotId] = true;
                this.mMmsApnReported[slotId] = true;
                this.mDunApnReported[slotId] = true;
            }
        }
        return needToReport;
    }

    private void sendBroadcastDelayed(Intent intent, long delayedTimer) {
        logd("CHR sendBroadcastDelayed delayedTimer = " + delayedTimer);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(5, intent), delayedTimer);
    }

    public void sendMonitorWifiSwitchToMobileMessage(int delayInterval) {
        logd("wifi disconnect, sendMonitorWifiSwitchToMobileMessage!");
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), (long) delayInterval);
    }

    public void removeMonitorWifiSwitchToMobileMessage() {
        this.mHandler.removeMessages(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifiConnected() {
        ConnectivityManager connManager;
        NetworkInfo wifiInfo;
        Context context = mContext;
        if (context == null || (connManager = (ConnectivityManager) context.getSystemService("connectivity")) == null || (wifiInfo = connManager.getNetworkInfo(1)) == null) {
            logd("Get WifiConnected Info failed!");
            return false;
        }
        logd("mWifiConnected = " + wifiInfo.isConnected());
        return wifiInfo.isConnected();
    }

    public void sendIntentDataSelfCure(int oldFailCause, int uploadReason) {
        Intent intent = new Intent(INTENT_DS_PDN_CURE_REPORT);
        intent.putExtra("old_fail_cause", oldFailCause);
        intent.putExtra("upload_reason", uploadReason);
        mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
    }

    private class ApnChangeObserver extends ContentObserver {
        ApnChangeObserver() {
            super(HwDataServiceChrManagerImpl.this.mHandler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            HwDataServiceChrManagerImpl.this.mHandler.sendMessage(HwDataServiceChrManagerImpl.this.mHandler.obtainMessage(4));
        }
    }
}
