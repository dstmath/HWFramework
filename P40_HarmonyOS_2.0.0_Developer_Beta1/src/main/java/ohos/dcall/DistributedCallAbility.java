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
    private static final String OHOS_CONNECT_CALL_ABILITY_PERMISSION = "ohos.permission.CONNECT_CALL_ABILITY";
    private static final String TAG = "DistributedCallAbility";
    private DistributedCallRemote mRemote = null;

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
            if (!DistributedCallAbility.this.isValidRequest(messageParcel, getCallingPid(), getCallingUid())) {
                return false;
            }
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
                    PreciseObserverProxy.getInstance().triggerOnCallEventChanged((DistributedCall) DistributedCall.CREATOR.createFromParcel(messageParcel), messageParcel.readString(), DistributedCallUtils.readPacMapFromParcel(messageParcel));
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
        this.mRemote = new DistributedCallRemote();
        return this.mRemote.asObject();
    }

    @Override // ohos.aafwk.ability.Ability
    public void onDisconnect(Intent intent) {
        HiLog.info(LABEL, "onDisconnect", new Object[0]);
        this.mRemote = null;
    }

    /* access modifiers changed from: package-private */
    public boolean isValidRequest(MessageParcel messageParcel, int i, int i2) {
        if (!checkPermission(i, i2)) {
            HiLog.error(LABEL, "isValidRequest: no permission.", new Object[0]);
            return false;
        } else if (!isValidRequestData(messageParcel)) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkPermission(int i, int i2) {
        return verifyPermission(OHOS_CONNECT_CALL_ABILITY_PERMISSION, i, i2) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isValidRequestData(MessageParcel messageParcel) {
        if (messageParcel == null) {
            HiLog.error(LABEL, "isValidRequestData: data is null.", new Object[0]);
            return false;
        } else if (DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            return true;
        } else {
            HiLog.error(LABEL, "isValidRequestData: invalid interface token.", new Object[0]);
            return false;
        }
    }
}
