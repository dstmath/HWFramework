package ohos.wifi;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public final class StreamListenerProxy extends RemoteObject implements IRemoteBroker {
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "StreamListenerProxy");
    private EventHandler handler = null;
    private StreamListener listener = null;
    private int mAbilityId;
    private final Object mLock = new Object();
    private IRemoteObject mRemoteAbility;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    StreamListenerProxy(EventRunner eventRunner, StreamListener streamListener, String str) {
        super(str);
        HiLog.info(LABEL, "init StreamListenerProxy", new Object[0]);
        initEventHandler(eventRunner);
        this.listener = streamListener;
        this.mAbilityId = SystemAbilityDefinition.WIFI_DEVICE_SYS_ABILITY_ID;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
        int readInt = messageParcel.readInt();
        InnerEvent innerEvent = InnerEvent.get();
        innerEvent.param = (long) readInt;
        this.handler.sendEvent(innerEvent);
        return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
    }

    private void initEventHandler(EventRunner eventRunner) {
        this.handler = new EventHandler(eventRunner) {
            /* class ohos.wifi.StreamListenerProxy.AnonymousClass1 */

            public void processEvent(InnerEvent innerEvent) {
                if (innerEvent != null) {
                    StreamListenerProxy.this.listener.onStreamChanged((int) innerEvent.param);
                }
            }
        };
    }
}
