package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.NetworkRegistrationInfoEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneStateListenerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import java.util.ArrayList;
import java.util.List;

public class HwPhoneProxyReference {
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final int CUST_IMSI_LENGTH = 7;
    protected static final String LOG_TAG = "HwPhoneProxy";
    private static final int MAX_IMSI_LENGTH = 15;
    private BroadcastHelper broadcastHelper;
    private boolean firstQueryDone = false;
    private Context mContext;
    private GlobalParamsAdaptor mGlobalParamsAdaptor;
    private int mNetworkRegState = 2;
    private int mPhoneId = 0;
    private PhoneExt mPhoneProxy;
    private ServiceStateListener mPhoneStateListener;
    private int mSubId = 0;
    private TelephonyManager mTelephonyManager;

    public HwPhoneProxyReference(PhoneExt phoneExt, Context context) {
        this.mContext = context;
        this.mPhoneProxy = phoneExt;
        this.mPhoneId = this.mPhoneProxy.getPhoneId();
        this.broadcastHelper = new BroadcastHelper();
        this.broadcastHelper.init();
        this.mGlobalParamsAdaptor = new GlobalParamsAdaptor(this.mPhoneId);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener() {
            /* class com.android.internal.telephony.HwPhoneProxyReference.AnonymousClass1 */

            @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
            public void onSubscriptionsChanged() {
                HwPhoneProxyReference.this.logd("onSubscriptionsChanged, registerPhoneStateListener.");
                HwPhoneProxyReference.this.registerPhoneStateListener();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerPhoneStateListener() {
        int newSubId = SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
        if (!SubscriptionManager.isValidSubscriptionId(newSubId)) {
            logd("sub id is invalid, cancel listen.");
            ServiceStateListener serviceStateListener = this.mPhoneStateListener;
            if (serviceStateListener != null) {
                serviceStateListener.cancelListen();
                this.mPhoneStateListener = null;
            }
            this.mSubId = -1;
            return;
        }
        if (this.mPhoneStateListener == null || newSubId != this.mSubId) {
            logd("sub id is different, cancel first and create a new one, newSubId=" + newSubId);
            ServiceStateListener serviceStateListener2 = this.mPhoneStateListener;
            if (serviceStateListener2 != null) {
                serviceStateListener2.cancelListen();
            }
            this.mPhoneStateListener = new ServiceStateListener(newSubId);
            this.mPhoneStateListener.listen();
        } else {
            logd("sub id is not change, do nothing." + this.mSubId);
        }
        this.mSubId = newSubId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkGlobalEccNumIfNeed() {
        String hplmn = null;
        String imsi = null;
        IccRecordsEx iccRecords = this.mPhoneProxy.getIccRecords();
        if (iccRecords != null) {
            hplmn = iccRecords.getOperatorNumeric();
            imsi = iccRecords.getIMSI();
        }
        boolean isRegRoamingState = getRoamingRegState(this.mPhoneId);
        if ((TextUtils.isEmpty(hplmn) || imsi == null || imsi.length() < 7 || imsi.length() > 15) || isRegRoamingState) {
            loge("checkGlobalEccNumIfNeed: invalid IMSI, or Roaming network, clearEccFakeProp");
            clearEccFakeProp();
            return;
        }
        String hplmn2 = getMccmncIfRoamingBroaker(hplmn);
        if (imsi.substring(0, 7).equals("2400768")) {
            this.mGlobalParamsAdaptor.checkGlobalEccNum("24205", getContext());
        } else {
            this.mGlobalParamsAdaptor.checkGlobalEccNum(hplmn2, getContext());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearEccFakeProp() {
        try {
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                SystemPropertiesEx.set("gsm.hw.cust.ecclist" + this.mPhoneId, (String) null);
            } else {
                SystemPropertiesEx.set("gsm.hw.cust.ecclist", (String) null);
            }
            if (this.mPhoneProxy != null) {
                this.mPhoneProxy.updateFakeEccEmergencyNumberListAndNotify(new ArrayList());
            }
        } catch (IllegalArgumentException e) {
            loge("clearEccFakeProp:Failed to save fake_ecc to system property");
        }
    }

    private String getMccmncIfRoamingBroaker(String mccmnc) {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            if (HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated(Integer.valueOf(this.mPhoneId))) {
                return HwPhoneManagerImpl.getDefault().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mPhoneId));
            }
            return mccmnc;
        } else if (HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated()) {
            return HwPhoneManagerImpl.getDefault().getRoamingBrokerOperatorNumeric();
        } else {
            return mccmnc;
        }
    }

    private int getCombinedNetworkRegState(int phoneId) {
        return getCombinedNetworkRegStateFromServicestate(TelephonyManagerEx.getServiceStateForSubscriber(SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(phoneId)));
    }

    private boolean getRoamingRegState(int phoneId) {
        if (SubscriptionControllerEx.getInstance() == null || TelephonyManagerEx.getDefault() == null) {
            loge("getRoamingRegState: getInstance or telephonyManager is null");
            return false;
        }
        ServiceState ss = TelephonyManagerEx.getServiceStateForSubscriber(SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(phoneId));
        if (ss != null) {
            return ss.getRoaming();
        }
        loge("getRoamingRegState: ServiceState is null");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCombinedNetworkRegStateFromServicestate(ServiceState ss) {
        if (ss == null) {
            loge("getCombinedNetworkRegStateFromServicestate: ServiceState is null");
            return 2;
        }
        List<NetworkRegistrationInfoEx> netRegStateList = ServiceStateEx.getNetworkRegistrationInfoExListForTransportType(1, ss);
        if (netRegStateList == null || netRegStateList.size() == 0) {
            loge("getCombinedNetworkRegStateFromServicestate: netRegStateList is null or empty.");
            return 0;
        }
        int regState = 2;
        for (NetworkRegistrationInfoEx tmpNetRegState : netRegStateList) {
            if (1 == tmpNetRegState.getRegistrationState()) {
                logd("getCombinedNetworkRegStateFromServicestate: transportType=" + tmpNetRegState.getTransportType() + ", regState=" + tmpNetRegState.getRegistrationState());
                return 1;
            }
            regState = tmpNetRegState.getRegistrationState();
        }
        return regState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String string) {
        RlogEx.i("HwPhoneProxy[SUB" + this.mPhoneProxy.getPhoneId() + "]", string);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String string) {
        RlogEx.e("HwPhoneProxy[SUB" + this.mPhoneProxy.getPhoneId() + "]", string);
    }

    private class BroadcastHelper {
        private static final int MIN_MATCH = 7;
        private BroadcastReceiver mPhoneProxyReceiver;

        public BroadcastHelper() {
            this.mPhoneProxyReceiver = new BroadcastReceiver(HwPhoneProxyReference.this) {
                /* class com.android.internal.telephony.HwPhoneProxyReference.BroadcastHelper.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    int phoneId;
                    if (intent == null) {
                        HwPhoneProxyReference.this.loge("intent is null.");
                    } else if (!TelephonyManagerEx.isMultiSimEnabled() || (phoneId = intent.getIntExtra("phone", 0)) == HwPhoneProxyReference.this.mPhoneProxy.getPhoneId()) {
                        HwPhoneProxyReference hwPhoneProxyReference = HwPhoneProxyReference.this;
                        hwPhoneProxyReference.logd("HwPhoneProxy BroadcastHelper onReceive action = " + intent.getAction());
                        if ("com.huawei.intent.action.ACTION_SIM_RECORDS_READY".equals(intent.getAction())) {
                            BroadcastHelper.this.handleSimRecordsReady(intent);
                        } else if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                            BroadcastHelper.this.handleServiceStateChanged();
                        } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                            BroadcastHelper.this.handleSimStateChanged(intent);
                        }
                    } else {
                        HwPhoneProxyReference hwPhoneProxyReference2 = HwPhoneProxyReference.this;
                        hwPhoneProxyReference2.logd("ignore HwPhoneProxy BroadcastHelper onReceive action = " + intent.getAction() + "with phoneId = " + phoneId);
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleSimRecordsReady(Intent intent) {
            String mccmnc = intent.getStringExtra("mccMnc");
            int simId = HwPhoneProxyReference.this.mPhoneProxy.getPhoneId();
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                HwPhoneProxyReference hwPhoneProxyReference = HwPhoneProxyReference.this;
                hwPhoneProxyReference.logd("RoamingBroker.getRBOperatorNumeric begin:" + mccmnc);
                if (HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated(Integer.valueOf(simId))) {
                    mccmnc = HwPhoneManagerImpl.getDefault().getRoamingBrokerOperatorNumeric(Integer.valueOf(simId));
                }
                HwPhoneProxyReference hwPhoneProxyReference2 = HwPhoneProxyReference.this;
                hwPhoneProxyReference2.logd("RoamingBroker.getRBOperatorNumeric end:" + mccmnc);
            } else {
                HwPhoneProxyReference hwPhoneProxyReference3 = HwPhoneProxyReference.this;
                hwPhoneProxyReference3.logd("RoamingBroker.getRBOperatorNumeric begin:" + mccmnc);
                if (HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated()) {
                    mccmnc = HwPhoneManagerImpl.getDefault().getRoamingBrokerOperatorNumeric();
                }
                HwPhoneProxyReference hwPhoneProxyReference4 = HwPhoneProxyReference.this;
                hwPhoneProxyReference4.logd("RoamingBroker.getRBOperatorNumeric end:" + mccmnc);
            }
            HwPhoneProxyReference.this.mGlobalParamsAdaptor.checkPrePostPay(mccmnc, intent.getStringExtra("imsi"), HwPhoneProxyReference.this.getContext());
            HwPhoneProxyReference.this.mGlobalParamsAdaptor.checkGlobalAutoMatchParam(mccmnc, HwPhoneProxyReference.this.getContext());
            HwPhoneProxyReference.this.mGlobalParamsAdaptor.checkAgpsServers(mccmnc);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleServiceStateChanged() {
            String networkCountryIso = null;
            String networkOperator = null;
            int phoneCount = TelephonyManagerEx.getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                networkCountryIso = TelephonyManagerEx.getNetworkCountryIsoForPhone(i);
                networkOperator = TelephonyManagerEx.getNetworkOperatorForPhone(i);
                if (!TextUtils.isEmpty(networkCountryIso)) {
                    break;
                }
            }
            boolean rplmnChanged = false;
            String lastNetworkOperator = SystemPropertiesEx.get("gsm.hw.operator.numeric.old", BuildConfig.FLAVOR);
            if (!TextUtils.isEmpty(lastNetworkOperator) && !TextUtils.isEmpty(networkOperator) && !networkOperator.equals(lastNetworkOperator)) {
                rplmnChanged = true;
                HwPhoneProxyReference hwPhoneProxyReference = HwPhoneProxyReference.this;
                hwPhoneProxyReference.logd("ACTION_SERVICE_STATE_CHANGED, network operator changed from " + lastNetworkOperator + " to " + networkOperator);
            }
            if (!TextUtils.isEmpty(networkOperator)) {
                SystemPropertiesEx.set("gsm.hw.operator.numeric.old", networkOperator);
            }
            SystemPropertiesEx.set("gsm.hw.operator.iso-country", networkCountryIso);
            SystemPropertiesEx.set("gsm.hw.operator.numeric", networkOperator);
            boolean isNetworkRoaming = false;
            for (int i2 = 0; i2 < phoneCount; i2++) {
                isNetworkRoaming = TelephonyManagerEx.isNetworkRoaming(SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(i2));
                if (isNetworkRoaming) {
                    break;
                }
            }
            SystemPropertiesEx.set("gsm.hw.operator.isroaming", isNetworkRoaming ? "true" : "false");
            if (SystemPropertiesEx.getBoolean("gsm.hw.operator.isroaming", false) && !TextUtils.isEmpty(networkOperator)) {
                if (!HwPhoneProxyReference.this.firstQueryDone || rplmnChanged) {
                    HwPhoneProxyReference.this.firstQueryDone = true;
                    HwPhoneProxyReference.this.mGlobalParamsAdaptor.queryRoamingNumberMatchRuleByNetwork(networkOperator, HwPhoneProxyReference.this.getContext());
                    HwPhoneProxyReference.this.mGlobalParamsAdaptor.checkValidityOfRoamingNumberMatchRule();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleSimStateChanged(Intent intent) {
            String simState = intent.getStringExtra("ss");
            if ("LOADED".equals(simState)) {
                HwPhoneProxyReference hwPhoneProxyReference = HwPhoneProxyReference.this;
                hwPhoneProxyReference.logd("ACTION_SIM_STATE_CHANGED:SIM LOADED, checkFakeEcc for phoneId=" + HwPhoneProxyReference.this.mPhoneId);
                HwPhoneProxyReference.this.checkGlobalEccNumIfNeed();
            } else if ("ABSENT".equals(simState)) {
                HwPhoneProxyReference hwPhoneProxyReference2 = HwPhoneProxyReference.this;
                hwPhoneProxyReference2.logd("ACTION_SIM_STATE_CHANGED:SIM ABSENT, clearFakeEcc prop for phoneId=" + HwPhoneProxyReference.this.mPhoneId);
                HwPhoneProxyReference.this.clearEccFakeProp();
            } else {
                HwPhoneProxyReference.this.logd("ACTION_SIM_STATE_CHANGED: ignore other SIM state.");
            }
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.huawei.intent.action.ACTION_SIM_RECORDS_READY");
            intentFilter.addAction("android.intent.action.SERVICE_STATE");
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
            HwPhoneProxyReference.this.getContext().registerReceiver(this.mPhoneProxyReceiver, intentFilter);
            HwPhoneProxyReference.this.logd("HwPhoneProxy BroadcastHelper register complelte");
        }
    }

    /* access modifiers changed from: private */
    public class ServiceStateListener extends PhoneStateListenerEx {
        ServiceStateListener(int subId) {
            super(subId);
            HwPhoneProxyReference.this.logd("ServiceStateListener create subId:" + subId);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.HwPhoneProxyReference$ServiceStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void listen() {
            HwPhoneProxyReference.this.mTelephonyManager.listen(this, 1);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.HwPhoneProxyReference$ServiceStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void cancelListen() {
            HwPhoneProxyReference.this.mTelephonyManager.listen(this, 0);
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null) {
                int currentRegState = HwPhoneProxyReference.this.getCombinedNetworkRegStateFromServicestate(state);
                HwPhoneProxyReference hwPhoneProxyReference = HwPhoneProxyReference.this;
                hwPhoneProxyReference.logd("ServiceStateListener:onServiceStateChanged currentRegState=" + currentRegState + ", mNetworkRegState=" + HwPhoneProxyReference.this.mNetworkRegState);
                if (HwPhoneProxyReference.this.mNetworkRegState != currentRegState) {
                    HwPhoneProxyReference.this.mNetworkRegState = currentRegState;
                    HwPhoneProxyReference.this.checkGlobalEccNumIfNeed();
                }
            }
        }
    }
}
