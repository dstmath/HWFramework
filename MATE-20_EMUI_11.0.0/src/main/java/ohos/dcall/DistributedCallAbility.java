package ohos.dcall;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteObject;

@SystemApi
public abstract class DistributedCallAbility extends Ability {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 0, TAG);
    private static final String TAG = "DistributedCallAbility";

    public void onCallAudioDeviceChanged(CallAudioDevice callAudioDevice) {
    }

    public void onCallCreated(DistributedCall distributedCall) {
    }

    public void onCallDeleted(DistributedCall distributedCall) {
    }

    public void onIsNewCallAllowedChanged(boolean z) {
    }

    public void onRingtoneMuted() {
    }

    private final class DistributedCallRemote extends RemoteObject implements IRemoteBroker {
        private static final String TAG = "DistributedCallRemote";

        public IRemoteObject asObject() {
            return this;
        }

        DistributedCallRemote() {
            super(TAG);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) {
            HiLog.info(DistributedCallAbility.LABEL, "%{public}s", new Object[]{DistributedCallUtils.msgCodeToString(i)});
            switch (i) {
                case 1:
                    DistributedCallAbility.this.onCallAudioDeviceChanged((CallAudioDevice) CallAudioDevice.CREATOR.createFromParcel(messageParcel));
                    break;
                case 2:
                    DistributedCallAbility.this.onCallCreated((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel));
                    break;
                case 3:
                    DistributedCallAbility.this.onCallDeleted((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel));
                    break;
                case 4:
                    DistributedCallAbility.this.onIsNewCallAllowedChanged(messageParcel.readBoolean());
                    break;
                case 5:
                    DistributedCallAbility.this.onRingtoneMuted();
                    break;
                case 6:
                    PreciseObserverProxy.getInstance().triggerStatusChanged((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel), messageParcel.readInt());
                    break;
                case 7:
                    DistributedCall distributedCall = (DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel);
                    PreciseObserverProxy.getInstance().triggerInfoChanged(distributedCall, distributedCall.getInfo());
                    break;
                case 8:
                    PreciseObserverProxy.getInstance().triggerPostDialWait((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel), messageParcel.readString());
                    break;
                case 9:
                    PreciseObserverProxy.getInstance().triggerCallCompleted((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel));
                    break;
                case 10:
                    PreciseObserverProxy.getInstance().triggerOnCallEventChanged((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel), messageParcel.readString(), null);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public IRemoteObject onConnect(Intent intent) {
        HiLog.info(LABEL, "onConnect", new Object[0]);
        return new DistributedCallRemote().asObject();
    }

    @Override // ohos.aafwk.ability.Ability
    public void onDisconnect(Intent intent) {
        HiLog.info(LABEL, "onDisconnect", new Object[0]);
    }
}
