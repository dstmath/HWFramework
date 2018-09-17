package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;

public class AbstractIccCardProxy extends Handler {
    protected boolean IS_CHINA_TELECOM;
    IccCardProxyReference mReference = HwTelephonyFactory.getHwUiccManager().createHwIccCardProxyReference(this);

    public interface IccCardProxyReference {
        void custRegisterForNetworkLocked(Handler handler, int i, Object obj);

        void custResetExternalState(State state);

        void custSetExternalState(PersoSubState persoSubState);

        void custUnregisterForNetworkLocked(Handler handler);

        void custUpdateExternalState(State state);

        boolean getIccCardStateHW();

        String getIccStateIntentString(State state);

        Integer getUiccIndex(Message message);

        void handleCustMessage(Message message);

        void handleMessageExtend(Message message);

        boolean isSimAbsent(Context context, UiccCard uiccCard, boolean z);

        State modifySimStateForVsim(int i, State state);

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
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", ProxyController.MODEM_0).equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", ProxyController.MODEM_0).equals("156");
        } else {
            equals = false;
        }
        this.IS_CHINA_TELECOM = equals;
    }

    public void handleMessageExtend(Message msg) {
        this.mReference.handleMessageExtend(msg);
    }

    protected boolean updateExternalStateDeactived() {
        return this.mReference.updateExternalStateDeactived();
    }

    private String getIccStateIntentString(State state) {
        return this.mReference.getIccStateIntentString(state);
    }

    public void registerUiccCardEventsExtend() {
        this.mReference.registerUiccCardEventsExtend();
    }

    public void unregisterUiccCardEventsExtend() {
        this.mReference.unregisterUiccCardEventsExtend();
    }

    public void custSetExternalState(PersoSubState ps) {
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

    protected void setUiccApplication(UiccCardApplication uiccCardApplication) {
        this.mReference.setUiccApplication(uiccCardApplication);
    }

    protected void registerForFdnStatusChange(Handler h) {
        this.mReference.registerForFdnStatusChange(h);
    }

    protected void unregisterForFdnStatusChange(Handler h) {
        this.mReference.unregisterForFdnStatusChange(h);
    }

    protected void queryFdn() {
        this.mReference.queryFdn();
    }

    protected int processCurrentAppType(UiccCard uiccCard, int defaultValue, int cardIndex) {
        return this.mReference.processCurrentAppType(uiccCard, defaultValue, cardIndex);
    }

    public boolean getIccCardStateHW() {
        return this.mReference.getIccCardStateHW();
    }

    public void custUpdateExternalState(State s) {
        this.mReference.custUpdateExternalState(s);
    }

    protected Integer getUiccIndex(Message msg) {
        return this.mReference.getUiccIndex(msg);
    }

    public boolean isSimAbsent(Context context, UiccCard uiccCard, boolean radioOn) {
        return this.mReference.isSimAbsent(context, uiccCard, radioOn);
    }

    public void processSimLockStateForCT() {
        this.mReference.processSimLockStateForCT();
    }

    public void custResetExternalState(State s) {
        this.mReference.custResetExternalState(s);
    }

    public State modifySimStateForVsim(int phoneId, State s) {
        return this.mReference.modifySimStateForVsim(phoneId, s);
    }
}
