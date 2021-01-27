package ohos.interwork.event.intentagent;

import ohos.annotation.SystemApi;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentUtil;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.ParcelableEx;
import ohos.rpc.MessageParcel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class IntentAgentEx implements Sequenceable, ParcelableEx {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108547, TAG);
    public static final Sequenceable.Producer<IntentAgentEx> PRODUCER = $$Lambda$IntentAgentEx$XBvMt2ajBjgcpf87zV6w7ysMNwc.INSTANCE;
    private static final String TAG = "IntentAgentEx";
    private Object obj = null;

    static /* synthetic */ IntentAgentEx lambda$static$0(Parcel parcel) {
        IntentAgentEx intentAgentEx = new IntentAgentEx(null);
        intentAgentEx.unmarshalling(parcel);
        return intentAgentEx;
    }

    public IntentAgentEx(IntentAgent intentAgent) {
        if (intentAgent != null) {
            this.obj = intentAgent.getObject();
        }
    }

    @SystemApi
    public Object getObject() {
        return this.obj;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel instanceof MessageParcel) {
            return IntentAgentUtil.writeToParcel(this.obj, (MessageParcel) parcel);
        }
        HiLog.warn(LABEL, "out is not the class of MessageParcel.", new Object[0]);
        return false;
    }

    @Override // ohos.utils.Sequenceable
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

    @Override // ohos.interwork.utils.ParcelableEx
    public void marshallingEx(Parcel parcel) {
        if (!(parcel instanceof MessageParcel)) {
            HiLog.warn(LABEL, "marshallingEx,out is not the class of MessageParcel.", new Object[0]);
        } else {
            IntentAgentUtil.writeToParcelEx(this.obj, (MessageParcel) parcel);
        }
    }
}
