package com.android.internal.telephony.uicc;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.vsim.VSimUtilsInner;

public class AbstractIccCardProxy extends IccCard {
    private static final String TAG = "AbstractIccCardProxy";
    protected boolean IS_CHINA_TELECOM;
    IccCardProxyReference mReference = HwTelephonyFactory.getHwUiccManager().createHwIccCardProxyReference(this);

    public interface IccCardProxyReference {
        void custRegisterForNetworkLocked(Handler handler, int i, Object obj);

        void custResetExternalState(IccCardConstants.State state);

        void custSetExternalState(IccCardApplicationStatus.PersoSubState persoSubState);

        void custUnregisterForNetworkLocked(Handler handler);

        void custUpdateExternalState(IccCardConstants.State state);

        boolean getIccCardStateHW();

        String getIccStateIntentString(IccCardConstants.State state);

        Integer getUiccIndex(Message message);

        void handleCustMessage(Message message);

        void handleMessageExtend(Message message);

        boolean isSimAbsent(Context context, UiccCard uiccCard, boolean z);

        IccCardConstants.State modifySimStateForVsim(int i, IccCardConstants.State state);

        int processCurrentAppType(UiccCard uiccCard, int i, int i2);

        void processSimLockStateForCT();

        void queryFdn();

        void registerForFdnStatusChange(Handler handler);

        void registerUiccCardEventsExtend();

        void setUiccApplication(UiccCardApplication uiccCardApplication);

        void supplyDepersonalization(String str, int i, Message message);

        void unregisterForFdnStatusChange(Handler handler);

        void unregisterUiccCardEventsExtend();

        boolean updateExternalStateDeactived();
    }

    public AbstractIccCardProxy() {
        this.IS_CHINA_TELECOM = SystemProperties.get("ro.config.hw_opta", ProxyController.MODEM_0).equals("92") && SystemProperties.get("ro.config.hw_optb", ProxyController.MODEM_0).equals("156");
    }

    public void handleMessageExtend(Message msg) {
        this.mReference.handleMessageExtend(msg);
    }

    /* access modifiers changed from: protected */
    public boolean updateExternalStateDeactived() {
        return this.mReference.updateExternalStateDeactived();
    }

    private String getIccStateIntentString(IccCardConstants.State state) {
        return this.mReference.getIccStateIntentString(state);
    }

    public void registerUiccCardEventsExtend() {
        this.mReference.registerUiccCardEventsExtend();
    }

    public void unregisterUiccCardEventsExtend() {
        this.mReference.unregisterUiccCardEventsExtend();
    }

    public void custSetExternalState(IccCardApplicationStatus.PersoSubState ps) {
        this.mReference.custSetExternalState(ps);
    }

    public void supplyDepersonalization(String pin, int type, Message onComplete) {
        this.mReference.supplyDepersonalization(pin, type, onComplete);
    }

    public void custRegisterForNetworkLocked(Handler h, int what, Object obj) {
        this.mReference.custRegisterForNetworkLocked(h, what, obj);
    }

    public void custUnregisterForNetworkLocked(Handler h) {
        this.mReference.custUnregisterForNetworkLocked(h);
    }

    public void handleCustMessage(Message msg) {
        this.mReference.handleCustMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void setUiccApplication(UiccCardApplication uiccCardApplication) {
        this.mReference.setUiccApplication(uiccCardApplication);
    }

    /* access modifiers changed from: protected */
    public void registerForFdnStatusChange(Handler h) {
        this.mReference.registerForFdnStatusChange(h);
    }

    /* access modifiers changed from: protected */
    public void unregisterForFdnStatusChange(Handler h) {
        this.mReference.unregisterForFdnStatusChange(h);
    }

    /* access modifiers changed from: protected */
    public void queryFdn() {
        this.mReference.queryFdn();
    }

    /* access modifiers changed from: protected */
    public int processCurrentAppType(UiccCard uiccCard, int defaultValue, int cardIndex) {
        return this.mReference.processCurrentAppType(uiccCard, defaultValue, cardIndex);
    }

    public boolean getIccCardStateHW() {
        return this.mReference.getIccCardStateHW();
    }

    public void custUpdateExternalState(IccCardConstants.State s) {
        this.mReference.custUpdateExternalState(s);
    }

    /* access modifiers changed from: protected */
    public Integer getUiccIndex(Message msg) {
        return this.mReference.getUiccIndex(msg);
    }

    public boolean isSimAbsent(Context context, UiccCard uiccCard, boolean radioOn) {
        return this.mReference.isSimAbsent(context, uiccCard, radioOn);
    }

    public void processSimLockStateForCT() {
        this.mReference.processSimLockStateForCT();
    }

    public void custResetExternalState(IccCardConstants.State s) {
        this.mReference.custResetExternalState(s);
    }

    public IccCardConstants.State modifySimStateForVsim(int phoneId, IccCardConstants.State s) {
        return this.mReference.modifySimStateForVsim(phoneId, s);
    }

    public static void broadcastIccStateChangedIntentInternal(String value, String reason, int phoneId) {
        if (!SubscriptionManager.isValidSlotIndex(phoneId) || !SystemProperties.getBoolean("ro.hwpp.cmcc_4G_dsdx_enable", false)) {
            Rlog.e(TAG, "broadcastIccStateChangedIntentInternal: phoneId=" + phoneId + " is invalid or ro.hwpp.cmcc_4G_dsdx_enable is false, Return!!");
            return;
        }
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SIM_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("phoneName", "Phone");
        intent.putExtra("ss", value);
        intent.putExtra("reason", reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
        VSimUtilsInner.putVSimExtraForIccStateChanged(intent, phoneId, value);
        Rlog.d(TAG, "broadcastIccStateChangedIntentInternal intent ACTION_SIM_STATE_CHANGED_INTERNAL value=" + value + " reason=" + reason + " for phoneId=" + phoneId);
        ActivityManager.broadcastStickyIntent(intent, 51, -1);
    }
}
