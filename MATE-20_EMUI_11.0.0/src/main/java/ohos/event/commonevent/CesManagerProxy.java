package ohos.event.commonevent;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.sysability.samgr.SysAbilityManager;

public class CesManagerProxy extends RemoteObject implements ICesManager {
    private static final CesManagerProxy INSTANCE = new CesManagerProxy(ICesManager.descriptor);
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "CesManagerProxy";
    private final Object REMOTE_LOCK = new Object();
    private IRemoteObject remoteObj;

    public static CesManagerProxy getCesManagerProxy() {
        return INSTANCE;
    }

    private CesManagerProxy(String str) {
        super(str);
    }

    public IRemoteObject asObject() {
        synchronized (this.REMOTE_LOCK) {
            if (this.remoteObj != null) {
                return this.remoteObj;
            }
            this.remoteObj = SysAbilityManager.getSysAbility(3202);
            if (this.remoteObj == null) {
                HiLog.warn(LABEL, "getSysAbility(%{public}d) failed.", new Object[]{3202});
                return this.remoteObj;
            }
            this.remoteObj.addDeathRecipient(new CesManagerDeathRecipient(), 0);
            HiLog.info(LABEL, "CesManagerProxy get %{public}d completed.", new Object[]{3202});
            return this.remoteObj;
        }
    }

    @Override // ohos.event.commonevent.ICesManager
    public CommonEventConvertInfo getActionClassName(String str) throws RemoteException {
        if (str == null || str.isEmpty()) {
            HiLog.debug(LABEL, "getActionClassName param is invalid.", new Object[0]);
            return null;
        }
        IRemoteObject asObject = asObject();
        if (asObject == null) {
            HiLog.debug(LABEL, "getActionClassName remote object is null.", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(ICesManager.descriptor)) {
                HiLog.debug(LABEL, "getActionClassName writeInterfaceToken failed.", new Object[0]);
                return null;
            } else if (!obtain.writeString(str)) {
                HiLog.debug(LABEL, "getActionClassName write action failed.", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return null;
            } else if (!asObject.sendRequest(5, obtain, obtain2, messageOption) || obtain2.readInt() != 0) {
                HiLog.debug(LABEL, "getActionClassName failed.", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return null;
            } else {
                CommonEventConvertInfo commonEventConvertInfo = new CommonEventConvertInfo();
                if (!obtain2.readSequenceable(commonEventConvertInfo)) {
                    HiLog.debug(LABEL, "getActionClassName read parcel failed.", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return null;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return commonEventConvertInfo;
            }
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.event.commonevent.ICesManager
    public void registerActionConvert(CommonEventConvertInfo commonEventConvertInfo) throws RemoteException {
        if (commonEventConvertInfo == null) {
            HiLog.debug(LABEL, "registerActionConvert convertInfo is null.", new Object[0]);
            return;
        }
        IRemoteObject asObject = asObject();
        if (asObject == null) {
            HiLog.debug(LABEL, "registerActionConvert remote object is null.", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(ICesManager.descriptor)) {
                HiLog.debug(LABEL, "registerActionConvert writeInterfaceToken failed.", new Object[0]);
                return;
            }
            obtain.writeSequenceable(commonEventConvertInfo);
            if (!asObject.sendRequest(4, obtain, obtain2, messageOption) || obtain2.readInt() != 0) {
                HiLog.debug(LABEL, "registerActionConvert failed.", new Object[0]);
            }
            obtain.reclaim();
            obtain2.reclaim();
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.REMOTE_LOCK) {
            getCesManagerProxy().remoteObj = iRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public static class CesManagerDeathRecipient implements IRemoteObject.DeathRecipient {
        private CesManagerDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.debug(CesManagerProxy.LABEL, "CesManagerDeathRecipient::onRemoteDied.", new Object[0]);
            CesManagerProxy.getCesManagerProxy().setRemoteObject(null);
        }
    }
}
