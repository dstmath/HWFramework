package com.huawei.internal.telephony.dataconnection;

import android.common.HwFrameworkFactory;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.DataFailCause;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataProfile;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DataServiceManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DcTrackerEx {
    public static final int AID_DNS = 1051;
    public static final int CALL_STACK_COMPARE_OFF_SET = 4;
    public static final int FAILED = -1;
    public static final String HWDCTRACKEREX_CALSS_NAME = "com.android.internal.telephony.dataconnection.HwDcTrackerEx";
    public static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    public static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = Uri.parse("content://telephony/carriers/preferapn_no_update/subId/");
    public static final boolean RADIO_TESTS = false;
    public static final int RECOVERY_ACTION_CLEANUP = 1;
    public static final int RECOVERY_ACTION_GET_DATA_CALL_LIST = 0;
    public static final int RECOVERY_ACTION_OEM_EXT = 4;
    public static final int RECOVERY_ACTION_RADIO_RESTART = 3;
    public static final int RECOVERY_ACTION_REREGISTER = 2;
    public static final int RELEASE_TYPE_NORMAL = 1;
    public static final int REQUEST_TYPE_NORMAL = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "DcTrackerEx";
    public static final int TYPE_RX_PACKETS = 1;
    public static final int TYPE_TX_PACKETS = 3;
    private static IHwCommBoosterServiceManager sIhwCommBoosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
    private static volatile INetworkStatsService sStatsService;
    private DcTracker mDcTracker;

    public static void sendIntentDataSelfCure(int oldFailCause, int uploadReason) {
        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDataSelfCure(oldFailCause, uploadReason);
    }

    public static int reportBoosterPara(String pkg, int type, Bundle bundle) {
        IHwCommBoosterServiceManager boosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (boosterServiceManager != null) {
            return boosterServiceManager.reportBoosterPara(pkg, type, bundle);
        }
        return -1;
    }

    public static int registerBoosterCallBack(String pkgName, BoosterCallback callback) {
        IHwCommBoosterServiceManager boosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (boosterServiceManager != null) {
            return boosterServiceManager.registerCallBack(pkgName, callback.getCallback());
        }
        return -1;
    }

    public DcTracker getDcTracker() {
        return this.mDcTracker;
    }

    public void setDcTracker(DcTracker dcTracker) {
        this.mDcTracker = dcTracker;
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

    public int getRecoveryAction() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return dcTracker.getRecoveryActionHw();
        }
        return 0;
    }

    public void putRecoveryAction(int action) {
        if (checkCallerLegal(HWDCTRACKEREX_CALSS_NAME, "actionProcess")) {
            DcTracker dcTracker = this.mDcTracker;
            if (dcTracker != null) {
                dcTracker.putRecoveryActionHw(action);
                return;
            }
            return;
        }
        throw new SecurityException("disallowed call putRecoveryAction");
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

    public long getUidStats(int uid, int type) {
        INetworkStatsService statsService = getStatsService();
        if (statsService == null) {
            return 0;
        }
        try {
            return statsService.getUidStats(uid, type);
        } catch (RemoteException e) {
            Rlog.e(getClass().getName(), "getUidStats catch remote exception.");
            return 0;
        }
    }

    public boolean isFailCauseValid(int failCause) {
        return DataFailCause.isFailCauseValid(failCause);
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

    public State getState(String apnType) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            return getStateFromState(dcTracker.getState(apnType));
        }
        return State.IDLE;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.internal.telephony.dataconnection.DcTrackerEx$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.FAILED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.RETRYING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private State getStateFromState(DctConstants.State state) {
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$DctConstants$State[state.ordinal()]) {
            case 1:
                return State.IDLE;
            case 2:
                return State.FAILED;
            case 3:
                return State.RETRYING;
            case 4:
                return State.CONNECTED;
            case 5:
                return State.CONNECTING;
            case 6:
                return State.DISCONNECTING;
            default:
                return null;
        }
    }

    public void clearDefaultLink() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.clearDefaultLink();
        }
    }

    public final ConcurrentHashMap<String, ApnContextEx> getApnContextHw() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.getMApnContextsHw() == null) {
            return null;
        }
        Map<String, ApnContext> apnContexts = this.mDcTracker.getMApnContextsHw();
        ConcurrentHashMap<String, ApnContextEx> apnContextExes = new ConcurrentHashMap<>();
        for (String key : apnContexts.keySet()) {
            ApnContextEx apnContextEx = new ApnContextEx();
            apnContextEx.setApnContext(apnContexts.get(key));
            apnContextExes.put(key, apnContextEx);
        }
        return apnContextExes;
    }

    public void resumeDefaultLink() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null) {
            dcTracker.resumeDefaultLink();
        }
    }

    public void setSinglePdpAllow(boolean isSinglePdpAllowed) {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker != null && dcTracker.getHwCustDcTracker() != null) {
            this.mDcTracker.getHwCustDcTracker().setSinglePdpAllow(isSinglePdpAllowed);
        }
    }

    private boolean checkCallerLegal(String className, String methodName) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements == null || elements.length <= 4 || !elements[4].getClassName().equals(className) || !elements[4].getMethodName().equals(methodName)) {
            return false;
        }
        return true;
    }

    public enum State {
        IDLE(DctConstants.State.IDLE),
        CONNECTING(DctConstants.State.CONNECTING),
        RETRYING(DctConstants.State.RETRYING),
        CONNECTED(DctConstants.State.CONNECTED),
        DISCONNECTING(DctConstants.State.DISCONNECTING),
        FAILED(DctConstants.State.FAILED);
        
        private final DctConstants.State value;

        private State(DctConstants.State value2) {
            this.value = value2;
        }
    }

    public static class TxRxSumEx {
        public long rxPkts;
        public long txPkts;
        private DcTracker.TxRxSum txRxSum;

        public DcTracker.TxRxSum getTxRxSum() {
            return this.txRxSum;
        }

        public void setTxRxSum(DcTracker.TxRxSum txRxSum2) {
            this.txRxSum = txRxSum2;
            this.txPkts = txRxSum2.txPkts;
            this.rxPkts = txRxSum2.rxPkts;
        }

        public String toString() {
            DcTracker.TxRxSum txRxSum2 = this.txRxSum;
            if (txRxSum2 != null) {
                return txRxSum2.toString();
            }
            return PhoneConfigurationManager.SSSS;
        }
    }

    public static abstract class BoosterCallback {
        private IHwCommBoosterCallback mHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
            /* class com.huawei.internal.telephony.dataconnection.DcTrackerEx.BoosterCallback.AnonymousClass1 */

            public void callBack(int type, Bundle b) throws RemoteException {
                BoosterCallback.this.callback(type, b);
            }
        };

        public abstract void callback(int i, Bundle bundle);

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private IHwCommBoosterCallback getCallback() {
            return this.mHwCommBoosterCallback;
        }
    }

    public static class LteAttachInfoEx {
        private String apn;
        private int protocol;

        public String getApn() {
            return this.apn;
        }

        public void setApn(String apn2) {
            this.apn = apn2;
        }

        public int getProtocol() {
            return this.protocol;
        }

        public void setProtocol(int protocol2) {
            this.protocol = protocol2;
        }
    }

    public void syncAttachedApnToModem(ApnSetting apn, PhoneExt phoneExt) {
        if (apn != null && phoneExt != null) {
            Rlog.i(TAG, "syncAttachedApnToModem.");
            ArrayList<DataProfile> dataProfileList = new ArrayList<>();
            dataProfileList.add(DcTracker.createDataProfile(apn, apn.getProfileId(), false, null));
            Phone phone = phoneExt.getPhone();
            DataServiceManager dataServiceManager = this.mDcTracker.getDataServiceManager();
            if (phone != null && dataServiceManager != null && !dataProfileList.isEmpty()) {
                dataServiceManager.setDataProfile(dataProfileList, phone.getServiceState().getRoaming(), null);
            }
        }
    }

    private static INetworkStatsService getStatsService() {
        if (sStatsService == null) {
            synchronized (DcTrackerEx.class) {
                if (sStatsService == null) {
                    sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
                }
            }
        }
        return sStatsService;
    }
}
