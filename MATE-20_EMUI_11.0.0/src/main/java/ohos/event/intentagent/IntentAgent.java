package ohos.event.intentagent;

import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.annotation.SystemApi;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class IntentAgent implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.INTENTAGENT_DOMAIN, TAG);
    public static final Sequenceable.Producer<IntentAgent> PRODUCER = $$Lambda$IntentAgent$dEtgBG72a1fDppLF26uPRajDvXQ.INSTANCE;
    private static final String TAG = "IntentAgent";
    private Object obj = null;

    public interface OnCompleted {
        void onSendCompleted(IntentAgent intentAgent, Intent intent, int i, String str, IntentParams intentParams);
    }

    static /* synthetic */ IntentAgent lambda$static$0(Parcel parcel) {
        IntentAgent intentAgent = new IntentAgent(null);
        intentAgent.unmarshalling(parcel);
        return intentAgent;
    }

    @SystemApi
    public IntentAgent(Object obj2) {
        this.obj = obj2;
    }

    @SystemApi
    public Object getObject() {
        return this.obj;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel instanceof MessageParcel) {
            return IntentAgentUtil.writeToParcel(this.obj, (MessageParcel) parcel);
        }
        HiLog.warn(LABEL, "out is not the class of MessageParcel.", new Object[0]);
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (!(parcel instanceof MessageParcel)) {
            HiLog.warn(LABEL, "in is not the class of MessageParcel.", new Object[0]);
            return false;
        }
        this.obj = IntentAgentUtil.readFromParcel((MessageParcel) parcel);
        if (this.obj != null) {
            return true;
        }
        return false;
    }
}
