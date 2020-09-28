package com.huawei.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.IHwDcTrackerEx;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.huawei.android.util.LocalLogEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DcTrackerEx {
    public static final int AID_DNS = 1051;
    public static final Uri MSIM_TELEPHONY_CARRIERS_URI = DcTracker.MSIM_TELEPHONY_CARRIERS_URI;
    public static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = DcTracker.PREFERAPN_NO_UPDATE_URI_USING_SUBID;
    public static final boolean RADIO_TESTS = false;
    public static final int RELEASE_TYPE_NORMAL = 1;
    public static final int REQUEST_TYPE_NORMAL = 1;
    private static final String TAG = "DcTrackerEx";
    public static final int TYPE_RX_PACKETS = 1;
    public static final int TYPE_TX_PACKETS = 3;
    private DcTracker mDcTracker;

    public static class TxRxSumEx {
        public long rxPkts;
        public long txPkts;
        private DcTracker.TxRxSum txRxSum;

        public void setTxRxSum(DcTracker.TxRxSum txRxSum2) {
            this.txRxSum = txRxSum2;
            this.txPkts = txRxSum2.txPkts;
            this.rxPkts = txRxSum2.rxPkts;
        }

        public DcTracker.TxRxSum getTxRxSum() {
            return this.txRxSum;
        }

        public String toString() {
            DcTracker.TxRxSum txRxSum2 = this.txRxSum;
            if (txRxSum2 != null) {
                return txRxSum2.toString();
            }
            return PhoneConfigurationManager.SSSS;
        }
    }

    public void setDcTracker(DcTracker dcTracker) {
        this.mDcTracker = dcTracker;
    }

    public DcTracker getDcTracker() {
        return this.mDcTracker;
    }

    public void setEnabledPublic(int id, boolean enabled) {
    }

    public void setDataAllowed(boolean allowed, Message message) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.setDataAllowed(allowed, message);
        }
    }

    public void sendRestartRadio() {
        if (this.mDcTracker != null) {
            Rlog.i(TAG, "send restartRadio message to DcTracker");
            this.mDcTracker.sendMessage(this.mDcTracker.obtainMessage(270362));
        }
    }

    public void cleanUpAllConnections(String reason) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.cleanUpAllConnections(reason);
        }
    }

    public Message obtainMessage(int what, Object object) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.obtainMessage(what, object);
        }
        return null;
    }

    public void sendMessage(Message message) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && message != null) {
            dcTracker.sendMessage(message);
        }
    }

    public void sendMessageDelayed(Message message, long delay) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && message != null) {
            dcTracker.sendMessageDelayed(message, delay);
        }
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            for (ApnContext apnContext : dcTracker.getMPrioritySortedApnContexts()) {
                if (TextUtils.isEmpty(excludedApnType) || !excludedApnType.equals(apnContext.getApnType())) {
                    if (apnContext.getState() == DctConstants.State.FAILED) {
                        apnContext.setState(DctConstants.State.IDLE);
                    }
                    if (apnContext.isConnectable()) {
                        apnContext.setReason(reason);
                        this.mDcTracker.onTrySetupDataHw(apnContext);
                    }
                }
            }
        }
    }

    public void updateForVSim() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.updateForVSim();
        }
    }

    public void requestNetwork(NetworkRequest networkRequest, int type, Message onCompleteMsg, LocalLogEx log) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.requestNetwork(networkRequest, type, onCompleteMsg);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, int type, LocalLogEx log) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.releaseNetwork(networkRequest, type);
        }
    }

    public void checkPLMN(String plmn) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.checkPLMN(plmn);
        }
    }

    public boolean isDisconnected() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.isDisconnected();
        }
        return true;
    }

    public boolean isConnected() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.getOverallState() != DctConstants.State.CONNECTED) {
            return false;
        }
        return true;
    }

    public void clearCureApnSettings() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.mCureApnSettings.clear();
        }
    }

    public void addCureApnSettings(String requestedApnType, List<ApnSetting> apnList) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && dcTracker.mCureApnSettings != null) {
            this.mDcTracker.mCureApnSettings.put(requestedApnType, apnList);
        }
    }

    public boolean isCureApnContainsType(String type) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.mCureApnSettings == null) {
            return false;
        }
        return this.mDcTracker.mCureApnSettings.containsKey(type);
    }

    public int getTransportType() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getTransportType();
        }
        return -1;
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        DataEnabledSettings dbSettings;
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && (dbSettings = dcTracker.getDataEnabledSettingsHw()) != null) {
            dbSettings.registerForDataEnabledChanged(h, what, obj);
        }
    }

    public void setupDataOnAllConnectableApns(String reason) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.setupDataOnAllConnectableApnsHw(reason);
        }
    }

    public boolean isLimitPDPAct() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.getHwCustDcTracker() == null) {
            return false;
        }
        return this.mDcTracker.getHwCustDcTracker().isLimitPDPAct();
    }

    public boolean getDataRoamingEnabled() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getDataRoamingEnabled();
        }
        return false;
    }

    public boolean isPhoneStateIdle() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.isPhoneStateIdleHw();
        }
        return true;
    }

    public List<ApnSetting> getAllApnList() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getAllApnList();
        }
        return Collections.emptyList();
    }

    public ApnSetting getPreferredApn() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getPreferredApnHw();
        }
        return null;
    }

    public void setPreferredApn(int position) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.setPreferredApnHw(position);
        }
    }

    public void setVpStatus(int vpStatus) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.mVpStatus = vpStatus;
        }
    }

    public void startNetStatPoll() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.startNetStatPollHw();
        }
    }

    public void stopNetStatPoll() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.stopNetStatPollHw();
        }
    }

    public void startDataStallAlarm(boolean suspectedStall) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.startDataStallAlarmHw(suspectedStall);
        }
    }

    public void stopDataStallAlarm() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.stopDataStallAlarmHw();
        }
    }

    public void notifyDataConnection() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.notifyDataConnectionHw();
        }
    }

    public boolean isDataEnabled() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.getDataEnabledSettingsHw() == null) {
            return false;
        }
        return this.mDcTracker.getDataEnabledSettingsHw().isDataEnabled();
    }

    public void cancelReconnectAlarm(ApnContextEx apnContextEx) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && apnContextEx != null) {
            dcTracker.cancelReconnectAlarmHw(apnContextEx.getApnContext());
        }
    }

    public void updateApnFromRetryToIdle() {
        Map<String, ApnContext> apnContexts;
        DcTracker dcTracker = this.mDcTracker;
        if (!(dcTracker == null || (apnContexts = dcTracker.getMApnContextsHw()) == null)) {
            for (ApnContext apnContext : apnContexts.values()) {
                if (apnContext.getState() == DctConstants.State.RETRYING) {
                    apnContext.setState(DctConstants.State.IDLE);
                    apnContext.setDataConnection(null);
                    this.mDcTracker.cancelReconnectAlarmHw(apnContext);
                }
            }
        }
    }

    public void registerForAllEvents() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.registerForAllEventsHw();
        }
    }

    public void unRegisterForAllEvents() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.unregisterForAllEventsHw();
        }
    }

    public void setUserDataEnabled(boolean enabled) {
        DataEnabledSettings dbSettings;
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && (dbSettings = dcTracker.getDataEnabledSettingsHw()) != null) {
            dbSettings.setUserDataEnabled(enabled);
        }
    }

    public void update() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.update();
        }
    }

    public boolean isOnlySingleDcAllowed(int rilRadioTech) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.isOnlySingleDcAllowedHw(rilRadioTech);
        }
        return false;
    }

    public void createAllApnList() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.createAllApnListHw();
        }
    }

    public boolean isCustCorrectApnAuthOn() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.getHwCustDcTracker() == null) {
            return false;
        }
        return this.mDcTracker.getHwCustDcTracker().isCustCorrectApnAuthOn();
    }

    public void custCorrectApnAuth(List<ApnSetting> allApnSettings) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && dcTracker.getHwCustDcTracker() != null) {
            this.mDcTracker.getHwCustDcTracker().custCorrectApnAuth(allApnSettings);
        }
    }

    public void startAlarmForReconnect(long delay, ApnContextEx apnContextEx) {
        if (this.mDcTracker != null && apnContextEx != null && apnContextEx.getApnContext() != null) {
            this.mDcTracker.startAlarmForReconnectHw(delay, apnContextEx.getApnContext());
        }
    }

    public int getDataRat() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getDataRatHw();
        }
        return 0;
    }

    public void setSentSinceLastRecv(long sentSinceLastRecv, boolean isAdd) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.setSentSinceLastRecvHw(sentSinceLastRecv, isAdd);
        }
    }

    public void resetRecoveryInfo() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.resetRecoveryInfoHw();
        }
    }

    public void setRecoveryReason(String reason, boolean isAdd) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.setRecoveryReasonHw(reason, isAdd);
        }
    }

    public long getSentSinceLastRecv() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getSentSinceLastRecvHw();
        }
        return 0;
    }

    public TxRxSumEx getPreDataStallTcpTxRxSum() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getPreDataStallTcpTxRxSumHw();
        }
        return null;
    }

    public TxRxSumEx getDataStallTcpTxRxSum() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getDataStallTcpTxRxSumHw();
        }
        return null;
    }

    public TxRxSumEx getPreDataStallDnsTxRxSum() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getPreDataStallDnsTxRxSumHw();
        }
        return null;
    }

    public TxRxSumEx getDataStallDnsTxRxSum() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getDataStallDnsTxRxSumHw();
        }
        return null;
    }

    public void updateHwTcpTxRxSum(long tx, long rx) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.getDataStallTcpTxRxSumHw().getTxRxSum().updateHwTcpTxRxSum(tx, rx);
        }
    }

    public void setDnsTxPktsSum(long tx) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.getDataStallDnsTxRxSumHw().getTxRxSum().txPkts = tx;
        }
    }

    public void setDnsRxPktsSum(long rx) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.getDataStallDnsTxRxSumHw().getTxRxSum().rxPkts = rx;
        }
    }

    public ApnContextEx getApnContextByType(String type) {
        Map<String, ApnContext> apnContexts;
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || type == null || (apnContexts = dcTracker.getMApnContextsHw()) == null) {
            return null;
        }
        ApnContextEx apnContextEx = new ApnContextEx();
        apnContextEx.setApnContext(apnContexts.get(type));
        return apnContextEx;
    }

    public void resetDefaultApnRetryCount() {
        List<ApnContext> prioritySortedApn;
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && (prioritySortedApn = dcTracker.getMPrioritySortedApnContexts()) != null) {
            prioritySortedApn.forEach($$Lambda$DcTrackerEx$oYBRqVUcAH9G7X58w04FCRO7FZQ.INSTANCE);
        }
    }

    static /* synthetic */ void lambda$resetDefaultApnRetryCount$0(ApnContext apnContext) {
        if (apnContext.getApnType().equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
            apnContext.resetRetryCount();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0018  */
    public boolean isDisconnectedOrConnecting() {
        Map<String, ApnContext> apnContexts = this.mDcTracker.getMApnContextsHw();
        if (apnContexts == null) {
            return false;
        }
        for (ApnContext apnContext : apnContexts.values()) {
            if (apnContext.getState() == DctConstants.State.CONNECTED || apnContext.getState() == DctConstants.State.DISCONNECTING) {
                return false;
            }
            while (r2.hasNext()) {
            }
        }
        return true;
    }

    public List<ApnContextEx> getApnContextsList() {
        Map<String, ApnContext> apnContexts;
        List<ApnContextEx> list = new ArrayList<>();
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || (apnContexts = dcTracker.getMApnContextsHw()) == null) {
            return list;
        }
        apnContexts.values().forEach(new Consumer(list) {
            /* class com.huawei.internal.telephony.dataconnection.$$Lambda$DcTrackerEx$GiI38uTQJJl7RL3WFTfq7GkT_c */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DcTrackerEx.lambda$getApnContextsList$1(this.f$0, (ApnContext) obj);
            }
        });
        return list;
    }

    static /* synthetic */ void lambda$getApnContextsList$1(List list, ApnContext apnContext) {
        ApnContextEx apnContextEx = new ApnContextEx();
        apnContextEx.setApnContext(apnContext);
        list.add(apnContextEx);
    }

    public void setInitialAttachApn() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.setInitialAttachApnHw();
        }
    }

    public int getApnStateCount(ApnContextEx.StateEx stateEx) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getApnStatedCount(ApnContextEx.getDctStateFromStateEx(stateEx));
        }
        return 0;
    }

    public Handler getHandler() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker;
        }
        return null;
    }

    public void sendIntentWhenApnNeedReport(PhoneExt phone, ApnSetting apn, int apnTypes, LinkProperties linkProperties) {
        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenApnNeedReport(phone.getPhone(), apn, apnTypes, linkProperties);
    }

    public IHwDcTrackerEx getHwDcTrackerEx() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getHwDcTrackerEx();
        }
        return null;
    }

    public void setSinglePdpAllow(boolean isSinglePdpAllowed) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && dcTracker.getHwCustDcTracker() != null) {
            this.mDcTracker.getHwCustDcTracker().setSinglePdpAllow(isSinglePdpAllowed);
        }
    }
}
