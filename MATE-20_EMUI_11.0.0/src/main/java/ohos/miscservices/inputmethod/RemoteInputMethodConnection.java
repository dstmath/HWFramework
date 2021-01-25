package ohos.miscservices.inputmethod;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.idn.BasicInfo;
import ohos.idn.DeviceManager;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class RemoteInputMethodConnection implements IAbilityConnection {
    private static final String FAIL = "fail";
    private static final String SUCCESS = "successful";
    private static final int SYS_ABILITY_PROXY_ID = 4396;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "RemoteInputMethodConnection");
    private String mRemoteDeviceId;
    private IRemoteObject mRemoteInputDataChannel;
    private IRemoteObject mRemoteObject;
    private RemoteInputMethodAgentProxy remoteInputMethodAgentProxy;

    public RemoteInputMethodConnection(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, String str) {
        this.mRemoteInputDataChannel = iRemoteObject;
        this.mRemoteObject = iRemoteObject2;
        this.mRemoteDeviceId = str;
    }

    public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
        HiLog.info(TAG, "ability connect done, elementName:", new Object[0]);
        this.remoteInputMethodAgentProxy = new RemoteInputMethodAgentProxy(iRemoteObject);
        try {
            this.remoteInputMethodAgentProxy.setRemoteObject(this.mRemoteInputDataChannel, this.mRemoteObject, this.mRemoteDeviceId);
            getSysAbility();
        } catch (RemoteException e) {
            HiLog.error(TAG, "take failed,Exception = %s", e.toString());
        }
    }

    private void getSysAbility() {
        HiLog.debug(TAG, "getSysAbility begin", new Object[0]);
        List<String> onlineDevices = getOnlineDevices();
        if (onlineDevices.size() == 0) {
            HiLog.error(TAG, "No online devices found", new Object[0]);
            return;
        }
        IRemoteObject sysAbility = SysAbilityManager.getSysAbility(SYS_ABILITY_PROXY_ID, onlineDevices.get(0));
        if (sysAbility == null) {
            HiLog.error(TAG, " sysAbilityMgrProxy get from SAMGR is null!", new Object[0]);
            return;
        }
        HiLog.debug(TAG, "sysAbilityMgrProxy get from SAMGR: %{public}s", sysAbility);
        HiLog.debug(TAG, "Death notification register %{public}s", sysAbility.addDeathRecipient(new DeathNotificationCallBack(), 0) ? SUCCESS : FAIL);
    }

    private List<String> getOnlineDevices() {
        ArrayList arrayList = new ArrayList();
        for (BasicInfo basicInfo : new DeviceManager().getNodesBasicInfo()) {
            arrayList.add(basicInfo.getNodeId());
        }
        HiLog.debug(TAG, "online device num is: %d", Integer.valueOf(arrayList.size()));
        return arrayList;
    }

    /* access modifiers changed from: private */
    public static class DeathNotificationCallBack implements IRemoteObject.DeathRecipient {
        private DeathNotificationCallBack() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.info(RemoteInputMethodConnection.TAG, "Remote Service is dead!", new Object[0]);
        }
    }

    public void onAbilityDisconnectDone(ElementName elementName, int i) {
        HiLog.info(TAG, "ability disconnect done, elementName: ", new Object[0]);
        this.remoteInputMethodAgentProxy = null;
    }

    public boolean isAvailable() {
        return this.remoteInputMethodAgentProxy != null;
    }
}
