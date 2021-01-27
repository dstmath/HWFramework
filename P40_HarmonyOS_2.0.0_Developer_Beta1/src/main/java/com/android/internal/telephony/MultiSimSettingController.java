package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MultiSimSettingController extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_ALL_SUBSCRIPTIONS_LOADED = 3;
    private static final int EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED = 6;
    private static final int EVENT_ROAMING_DATA_ENABLED = 2;
    private static final int EVENT_SUBSCRIPTION_GROUP_CHANGED = 5;
    private static final int EVENT_SUBSCRIPTION_INFO_CHANGED = 4;
    private static final int EVENT_USER_DATA_ENABLED = 1;
    private static final String LOG_TAG = "MultiSimSettingController";
    private static final int PRIMARY_SUB_ADDED = 1;
    private static final int PRIMARY_SUB_INITIALIZED = 6;
    private static final int PRIMARY_SUB_MARKED_OPPT = 5;
    private static final int PRIMARY_SUB_NO_CHANGE = 0;
    private static final int PRIMARY_SUB_REMOVED = 2;
    private static final int PRIMARY_SUB_SWAPPED = 3;
    private static final int PRIMARY_SUB_SWAPPED_IN_GROUP = 4;
    private static MultiSimSettingController sInstance = null;
    private final Context mContext;
    private List<Integer> mPrimarySubList = new ArrayList();
    private final SubscriptionController mSubController;
    private boolean mSubInfoInitialized = false;

    @Retention(RetentionPolicy.SOURCE)
    private @interface PrimarySubChangeType {
    }

    /* access modifiers changed from: private */
    public interface UpdateDefaultAction {
        void update(int i);
    }

    public static MultiSimSettingController getInstance() {
        MultiSimSettingController multiSimSettingController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                Log.wtf(LOG_TAG, "getInstance null");
            }
            multiSimSettingController = sInstance;
        }
        return multiSimSettingController;
    }

    public static MultiSimSettingController init(Context context, SubscriptionController sc) {
        MultiSimSettingController multiSimSettingController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new MultiSimSettingController(context, sc);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            multiSimSettingController = sInstance;
        }
        return multiSimSettingController;
    }

    @VisibleForTesting
    public MultiSimSettingController(Context context, SubscriptionController sc) {
        this.mContext = context;
        this.mSubController = sc;
    }

    public void notifyUserDataEnabled(int subId, boolean enable) {
        obtainMessage(1, subId, enable ? 1 : 0).sendToTarget();
    }

    public void notifyRoamingDataEnabled(int subId, boolean enable) {
        obtainMessage(2, subId, enable ? 1 : 0).sendToTarget();
    }

    public void notifyAllSubscriptionLoaded() {
        obtainMessage(3).sendToTarget();
    }

    public void notifySubscriptionInfoChanged() {
        obtainMessage(4).sendToTarget();
    }

    public void notifySubscriptionGroupChanged(ParcelUuid groupUuid) {
        obtainMessage(5, groupUuid).sendToTarget();
    }

    public void notifyDefaultDataSubChanged() {
        obtainMessage(6).sendToTarget();
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        boolean enable = true;
        switch (msg.what) {
            case 1:
                int subId = msg.arg1;
                if (msg.arg2 == 0) {
                    enable = false;
                }
                onUserDataEnabled(subId, enable);
                return;
            case 2:
                int subId2 = msg.arg1;
                if (msg.arg2 == 0) {
                    enable = false;
                }
                onRoamingDataEnabled(subId2, enable);
                return;
            case 3:
                onAllSubscriptionsLoaded();
                return;
            case 4:
                onSubscriptionsChanged();
                return;
            case 5:
                onSubscriptionGroupChanged((ParcelUuid) msg.obj);
                return;
            case 6:
                onDefaultDataSettingChanged();
                return;
            default:
                return;
        }
    }

    private void onUserDataEnabled(int subId, boolean enable) {
        log("onUserDataEnabled");
        setUserDataEnabledForGroup(subId, enable);
        if (this.mSubController.getDefaultDataSubId() != subId && !this.mSubController.isOpportunistic(subId) && enable) {
            this.mSubController.setDefaultDataSubId(subId);
        }
    }

    private void onRoamingDataEnabled(int subId, boolean enable) {
        log("onRoamingDataEnabled");
        if (SubscriptionManager.isValidSubscriptionId(subId) && subId != Integer.MAX_VALUE) {
            setRoamingDataEnabledForGroup(subId, enable);
            this.mSubController.setDataRoaming(enable ? 1 : 0, subId);
        }
    }

    private void onAllSubscriptionsLoaded() {
        log("onAllSubscriptionsLoaded");
        this.mSubInfoInitialized = true;
        updateDefaults(true);
        disableDataForNonDefaultNonOpportunisticSubscriptions();
    }

    private void onSubscriptionsChanged() {
        log("onSubscriptionsChanged");
        if (this.mSubInfoInitialized) {
            updateDefaults(false);
            disableDataForNonDefaultNonOpportunisticSubscriptions();
        }
    }

    private void onDefaultDataSettingChanged() {
        log("onDefaultDataSettingChanged");
        disableDataForNonDefaultNonOpportunisticSubscriptions();
    }

    private void onSubscriptionGroupChanged(ParcelUuid groupUuid) {
        log("onSubscriptionGroupChanged");
        List<SubscriptionInfo> infoList = this.mSubController.getSubscriptionsInGroup(groupUuid, this.mContext.getOpPackageName());
        if (infoList != null && !infoList.isEmpty()) {
            int refSubId = infoList.get(0).getSubscriptionId();
            Iterator<SubscriptionInfo> it = infoList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                int subId = it.next().getSubscriptionId();
                if (this.mSubController.isActiveSubId(subId) && !this.mSubController.isOpportunistic(subId)) {
                    refSubId = subId;
                    break;
                }
            }
            log("refSubId is " + refSubId);
            boolean enable = false;
            try {
                enable = GlobalSettingsHelper.getBoolean(this.mContext, "mobile_data", refSubId);
                onUserDataEnabled(refSubId, enable);
            } catch (Settings.SettingNotFoundException e) {
                onUserDataEnabled(refSubId, GlobalSettingsHelper.getBoolean(this.mContext, "mobile_data", -1, enable));
            }
            boolean enable2 = false;
            try {
                enable2 = GlobalSettingsHelper.getBoolean(this.mContext, "data_roaming", refSubId);
                onRoamingDataEnabled(refSubId, enable2);
            } catch (Settings.SettingNotFoundException e2) {
                onRoamingDataEnabled(refSubId, GlobalSettingsHelper.getBoolean(this.mContext, "data_roaming", -1, enable2));
            }
            this.mSubController.syncGroupedSetting(refSubId);
        }
    }

    private void updateDefaults(boolean init) {
        log("updateDefaults");
        if (this.mSubInfoInitialized) {
            List<SubscriptionInfo> activeSubInfos = this.mSubController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (ArrayUtils.isEmpty(activeSubInfos)) {
                this.mPrimarySubList.clear();
                log("[updateDefaultValues] No active sub. Setting default to INVALID sub.");
                this.mSubController.setDefaultDataSubId(-1);
                this.mSubController.setDefaultVoiceSubId(-1);
                this.mSubController.setDefaultSmsSubId(-1);
                return;
            }
            int change = updatePrimarySubListAndGetChangeType(activeSubInfos, init);
            log("[updateDefaultValues] change: " + change);
            if (change != 0) {
                if (this.mPrimarySubList.size() != 1 || change == 2) {
                    log("[updateDefaultValues] records: " + this.mPrimarySubList);
                    log("[updateDefaultValues] Update default data subscription");
                    boolean dataSelected = updateDefaultValue(this.mPrimarySubList, this.mSubController.getDefaultDataSubId(), new UpdateDefaultAction() {
                        /* class com.android.internal.telephony.$$Lambda$MultiSimSettingController$55347QtGjuukXpx3jYZkJd_z3U */

                        @Override // com.android.internal.telephony.MultiSimSettingController.UpdateDefaultAction
                        public final void update(int i) {
                            MultiSimSettingController.this.lambda$updateDefaults$0$MultiSimSettingController(i);
                        }
                    });
                    log("[updateDefaultValues] Update default voice subscription");
                    boolean voiceSelected = updateDefaultValue(this.mPrimarySubList, this.mSubController.getDefaultVoiceSubId(), new UpdateDefaultAction() {
                        /* class com.android.internal.telephony.$$Lambda$MultiSimSettingController$WtGtOenjqxSBoW5BUjTVlNoSTM */

                        @Override // com.android.internal.telephony.MultiSimSettingController.UpdateDefaultAction
                        public final void update(int i) {
                            MultiSimSettingController.this.lambda$updateDefaults$1$MultiSimSettingController(i);
                        }
                    });
                    log("[updateDefaultValues] Update default sms subscription");
                    sendSubChangeNotificationIfNeeded(change, dataSelected, voiceSelected, updateDefaultValue(this.mPrimarySubList, this.mSubController.getDefaultSmsSubId(), new UpdateDefaultAction() {
                        /* class com.android.internal.telephony.$$Lambda$MultiSimSettingController$DcLtrTEtdlCd4WOev4Zk79vrSko */

                        @Override // com.android.internal.telephony.MultiSimSettingController.UpdateDefaultAction
                        public final void update(int i) {
                            MultiSimSettingController.this.lambda$updateDefaults$2$MultiSimSettingController(i);
                        }
                    }));
                    return;
                }
                int subId = this.mPrimarySubList.get(0).intValue();
                log("[updateDefaultValues] to only primary sub " + subId);
                this.mSubController.setDefaultDataSubId(subId);
                this.mSubController.setDefaultVoiceSubId(subId);
                this.mSubController.setDefaultSmsSubId(subId);
            }
        }
    }

    public /* synthetic */ void lambda$updateDefaults$0$MultiSimSettingController(int newValue) {
        this.mSubController.setDefaultDataSubId(newValue);
    }

    public /* synthetic */ void lambda$updateDefaults$1$MultiSimSettingController(int newValue) {
        this.mSubController.setDefaultVoiceSubId(newValue);
    }

    public /* synthetic */ void lambda$updateDefaults$2$MultiSimSettingController(int newValue) {
        this.mSubController.setDefaultSmsSubId(newValue);
    }

    private int updatePrimarySubListAndGetChangeType(List<SubscriptionInfo> activeSubList, boolean init) {
        List<Integer> prevPrimarySubList = this.mPrimarySubList;
        this.mPrimarySubList = (List) activeSubList.stream().filter($$Lambda$MultiSimSettingController$7eK1c9cJ2YdsAwoYGhX7w7nMM.INSTANCE).map($$Lambda$MultiSimSettingController$OwaLr1D2oeslrR0hgRvph4WwUo8.INSTANCE).collect(Collectors.toList());
        if (init) {
            return 6;
        }
        if (this.mPrimarySubList.equals(prevPrimarySubList)) {
            return 0;
        }
        if (this.mPrimarySubList.size() > prevPrimarySubList.size()) {
            return 1;
        }
        if (this.mPrimarySubList.size() == prevPrimarySubList.size()) {
            for (Integer num : this.mPrimarySubList) {
                int subId = num.intValue();
                boolean swappedInSameGroup = false;
                Iterator<Integer> it = prevPrimarySubList.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (areSubscriptionsInSameGroup(subId, it.next().intValue())) {
                            swappedInSameGroup = true;
                            continue;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!swappedInSameGroup) {
                    return 3;
                }
            }
            return 4;
        }
        for (Integer num2 : prevPrimarySubList) {
            int subId2 = num2.intValue();
            if (!this.mPrimarySubList.contains(Integer.valueOf(subId2))) {
                if (!this.mSubController.isActiveSubId(subId2)) {
                    return 2;
                }
                if (!this.mSubController.isOpportunistic(subId2)) {
                    loge("[updatePrimarySubListAndGetChangeType]: missing active primary subId " + subId2);
                }
            }
        }
        return 5;
    }

    static /* synthetic */ boolean lambda$updatePrimarySubListAndGetChangeType$3(SubscriptionInfo info) {
        return !info.isOpportunistic();
    }

    private void sendSubChangeNotificationIfNeeded(int change, boolean dataSelected, boolean voiceSelected, boolean smsSelected) {
        int simSelectDialogType = getSimSelectDialogType(change, dataSelected, voiceSelected, smsSelected);
        SimCombinationWarningParams simCombinationParams = getSimCombinationWarningParams(change);
        if (simSelectDialogType != 0 || simCombinationParams.mWarningType != 0) {
            log("[sendSubChangeNotificationIfNeeded] showing dialog type " + simSelectDialogType);
            log("[sendSubChangeNotificationIfNeeded] showing sim warning " + simCombinationParams.mWarningType);
            Intent intent = new Intent();
            intent.setAction("android.telephony.action.PRIMARY_SUBSCRIPTION_LIST_CHANGED");
            intent.setClassName("com.android.settings", "com.android.settings.sim.SimSelectNotification");
            intent.addFlags(268435456);
            intent.putExtra("android.telephony.extra.DEFAULT_SUBSCRIPTION_SELECT_TYPE", simSelectDialogType);
            if (simSelectDialogType == 4) {
                intent.putExtra("android.telephony.extra.SUBSCRIPTION_ID", this.mPrimarySubList.get(0));
            }
            intent.putExtra("android.telephony.extra.SIM_COMBINATION_WARNING_TYPE", simCombinationParams.mWarningType);
            if (simCombinationParams.mWarningType == 1) {
                intent.putExtra("android.telephony.extra.SIM_COMBINATION_NAMES", simCombinationParams.mSimNames);
            }
            this.mContext.sendBroadcast(intent);
        }
    }

    private int getSimSelectDialogType(int change, boolean dataSelected, boolean voiceSelected, boolean smsSelected) {
        if (this.mPrimarySubList.size() == 1 && change == 2 && (!dataSelected || !smsSelected || !voiceSelected)) {
            return 4;
        }
        if (this.mPrimarySubList.size() <= 1 || !isUserVisibleChange(change)) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    public class SimCombinationWarningParams {
        String mSimNames;
        int mWarningType;

        private SimCombinationWarningParams() {
            this.mWarningType = 0;
        }
    }

    private SimCombinationWarningParams getSimCombinationWarningParams(int change) {
        SimCombinationWarningParams params = new SimCombinationWarningParams();
        if (this.mPrimarySubList.size() <= 1 || !isUserVisibleChange(change)) {
            return params;
        }
        List<String> simNames = new ArrayList<>();
        int cdmaPhoneCount = 0;
        for (Integer num : this.mPrimarySubList) {
            int subId = num.intValue();
            Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
            if (phone != null && phone.isCdmaSubscriptionAppPresent()) {
                cdmaPhoneCount++;
                String simName = this.mSubController.getActiveSubscriptionInfo(subId, this.mContext.getOpPackageName()).getDisplayName().toString();
                if (TextUtils.isEmpty(simName)) {
                    simName = phone.getCarrierName();
                }
                simNames.add(simName);
            }
        }
        if (cdmaPhoneCount > 1) {
            params.mWarningType = 1;
            params.mSimNames = String.join(" & ", simNames);
        }
        return params;
    }

    private boolean isUserVisibleChange(int change) {
        return change == 1 || change == 2 || change == 3;
    }

    private void disableDataForNonDefaultNonOpportunisticSubscriptions() {
        if (this.mSubInfoInitialized) {
            int defaultDataSub = this.mSubController.getDefaultDataSubId();
            Phone[] phones = PhoneFactory.getPhones();
            for (Phone phone : phones) {
                if (phone.getSubId() != defaultDataSub && SubscriptionManager.isValidSubscriptionId(phone.getSubId()) && !this.mSubController.isOpportunistic(phone.getSubId()) && phone.isUserDataEnabled() && !areSubscriptionsInSameGroup(defaultDataSub, phone.getSubId())) {
                    log("setting data to false on " + phone.getSubId());
                    phone.getDataEnabledSettings().setUserDataEnabled(false);
                }
            }
        }
    }

    private boolean areSubscriptionsInSameGroup(int subId1, int subId2) {
        if (!SubscriptionManager.isUsableSubscriptionId(subId1) || !SubscriptionManager.isUsableSubscriptionId(subId2)) {
            return false;
        }
        if (subId1 == subId2) {
            return true;
        }
        ParcelUuid groupUuid1 = this.mSubController.getGroupUuid(subId1);
        ParcelUuid groupUuid2 = this.mSubController.getGroupUuid(subId2);
        if (groupUuid1 == null || !groupUuid1.equals(groupUuid2)) {
            return false;
        }
        return true;
    }

    private void setUserDataEnabledForGroup(int subId, boolean enable) {
        log("setUserDataEnabledForGroup subId " + subId + " enable " + enable);
        SubscriptionController subscriptionController = this.mSubController;
        List<SubscriptionInfo> infoList = subscriptionController.getSubscriptionsInGroup(subscriptionController.getGroupUuid(subId), this.mContext.getOpPackageName());
        if (infoList != null) {
            for (SubscriptionInfo info : infoList) {
                int currentSubId = info.getSubscriptionId();
                if (this.mSubController.isActiveSubId(currentSubId)) {
                    Phone phone = PhoneFactory.getPhone(this.mSubController.getPhoneId(currentSubId));
                    if (phone != null) {
                        phone.getDataEnabledSettings().setUserDataEnabled(enable);
                    }
                } else {
                    GlobalSettingsHelper.setBoolean(this.mContext, "mobile_data", currentSubId, enable);
                }
            }
        }
    }

    private void setRoamingDataEnabledForGroup(int subId, boolean enable) {
        List<SubscriptionInfo> infoList = SubscriptionController.getInstance().getSubscriptionsInGroup(this.mSubController.getGroupUuid(subId), this.mContext.getOpPackageName());
        if (infoList != null) {
            for (SubscriptionInfo info : infoList) {
                GlobalSettingsHelper.setBoolean(this.mContext, "data_roaming", info.getSubscriptionId(), enable);
            }
        }
    }

    private boolean updateDefaultValue(List<Integer> primarySubList, int oldValue, UpdateDefaultAction action) {
        int newValue = -1;
        if (primarySubList.size() > 0) {
            Iterator<Integer> it = primarySubList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                int subId = it.next().intValue();
                log("[updateDefaultValue] Record.id: " + subId);
                if (areSubscriptionsInSameGroup(subId, oldValue)) {
                    newValue = subId;
                    log("[updateDefaultValue] updates to subId=" + newValue);
                    break;
                }
            }
        }
        if (oldValue != newValue) {
            log("[updateDefaultValue: subId] from " + oldValue + " to " + newValue);
            action.update(newValue);
        }
        return SubscriptionManager.isValidSubscriptionId(newValue);
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Log.e(LOG_TAG, msg);
    }
}
