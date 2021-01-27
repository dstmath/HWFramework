package ohos.ace.featureabilityplugin;

import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteObject;

public class AbilityConnection implements IAbilityConnection {
    private static final String TAG = AbilityConnection.class.getSimpleName();
    private int abilityId = 0;
    private AbilityProxy abilityProxy;
    private AbilityStub abilityStub;
    private boolean firstConnectedCallAbility = false;
    private String firstConnectedData;
    private EventNotifier firstConnectedEventNotifier;
    private int firstConnectedMessageCode;
    private Result firstConnectedResult;
    private boolean firstConnectedSubscribeAbility = false;
    private boolean firstConnectedSync = true;

    public AbilityConnection(int i) {
        this.abilityId = i;
    }

    @Override // ohos.aafwk.ability.IAbilityConnection
    public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
        this.abilityProxy = new AbilityProxy(iRemoteObject);
        if (this.firstConnectedCallAbility) {
            callAbility(this.firstConnectedMessageCode, this.firstConnectedData, this.firstConnectedResult, this.firstConnectedSync);
            this.firstConnectedCallAbility = false;
        } else if (this.firstConnectedSubscribeAbility) {
            subscribeAbility(this.firstConnectedMessageCode, this.firstConnectedEventNotifier, this.firstConnectedResult, this.firstConnectedSync);
            this.firstConnectedSubscribeAbility = false;
        }
    }

    @Override // ohos.aafwk.ability.IAbilityConnection
    public void onAbilityDisconnectDone(ElementName elementName, int i) {
        String str = elementName.getBundleName() + "." + elementName.getAbilityName();
        ALog.i(TAG, "Ability disconnected:" + str);
        this.abilityProxy = null;
        this.firstConnectedCallAbility = false;
        this.firstConnectedSubscribeAbility = false;
        AbilityStub abilityStub2 = this.abilityStub;
        if (abilityStub2 != null) {
            abilityStub2.reportEventMessage(2005, str + " disconnect notify");
            this.abilityStub.setEventNotifier(null);
            this.abilityStub = null;
        }
        AbilityManager.getInstance().removeAbilityConnection(this.abilityId, str);
    }

    public boolean isAvailable() {
        return this.abilityProxy != null;
    }

    public void callAbility(int i, String str, Result result, boolean z) {
        if (result == null) {
            ALog.e(TAG, "result handler is null.");
            return;
        }
        AbilityProxy abilityProxy2 = this.abilityProxy;
        if (abilityProxy2 == null) {
            ALog.e(TAG, "Call ability error, connection is disconnected.");
            result.error(2005, "Call ability error, connection is disconnected.");
        } else if (!z) {
            ALog.e(TAG, "Ability type can't support async option.");
            result.error(2002, "Ability type can't support async option.");
        } else {
            abilityProxy2.callAbility(i, str, result, z);
        }
    }

    public void subscribeAbility(int i, EventNotifier eventNotifier, Result result, boolean z) {
        if (eventNotifier == null || result == null) {
            ALog.e(TAG, "eventNotifier or result handler is null.");
        } else if (this.abilityProxy == null) {
            ALog.e(TAG, "Subscribe ability error, connection is disconnected.");
            result.error(2005, "Subscribe ability error, connection is disconnected.");
        } else if (!z) {
            ALog.e(TAG, "Ability type can't support async option.");
            result.error(2002, "Ability type can't support async option.");
        } else {
            RemoteObject abilityStub2 = new AbilityStub(eventNotifier);
            if (this.abilityProxy.subscribeAbility(i, abilityStub2, result, z)) {
                this.abilityStub = abilityStub2;
            }
        }
    }

    public void unsubscribeAbility(int i, Result result, boolean z) {
        AbilityProxy abilityProxy2 = this.abilityProxy;
        if (abilityProxy2 == null) {
            ALog.e(TAG, "Unsubscribe ability error, abilityProxy is null.");
            result.error(2008, "Unsubscribe ability proxy is not exist");
            return;
        }
        AbilityStub abilityStub2 = this.abilityStub;
        if (abilityStub2 == null) {
            ALog.e(TAG, "Unsubscribe ability error, abilityStub is null.");
            result.error(2008, "Unsubscribe ability stub is not exist");
        } else if (!z) {
            ALog.e(TAG, "Ability type can't support async option.");
            result.error(2002, "Ability type can't support async option.");
        } else if (abilityProxy2.unsubscribeAbility(i, abilityStub2, result, z)) {
            this.abilityStub.setEventNotifier(null);
        }
    }

    public void setFirstConnectedCallAbility(int i, String str, Result result, boolean z) {
        this.firstConnectedMessageCode = i;
        this.firstConnectedData = str;
        this.firstConnectedResult = result;
        this.firstConnectedSync = z;
        this.firstConnectedCallAbility = true;
    }

    public void setFirstConnectedSubscribeAbility(int i, EventNotifier eventNotifier, Result result, boolean z) {
        this.firstConnectedMessageCode = i;
        this.firstConnectedEventNotifier = eventNotifier;
        this.firstConnectedResult = result;
        this.firstConnectedSync = z;
        this.firstConnectedSubscribeAbility = true;
    }
}
