package ohos.ace.ability;

import com.huawei.ace.runtime.ALog;
import ohos.ace.featureabilityplugin.AbilityManager;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class AceInternalAbility {
    private static final String TAG = "AceInternalAbility";
    private final String abilityName;
    private final String bundleName;

    public interface AceInternalAbilityHandler {
        boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException;
    }

    public AceInternalAbility(String str, String str2) {
        if (str == null || "".equals(str) || str2 == null || "".equals(str2)) {
            ALog.e(TAG, "bundleName or abilityName name must not be null or empty string");
            throw new IllegalArgumentException("bundleName or abilityName is null or empty string");
        }
        this.bundleName = str;
        this.abilityName = str2;
    }

    public void setInternalAbilityHandler(AceInternalAbilityHandler aceInternalAbilityHandler) {
        AbilityManager instance = AbilityManager.getInstance();
        instance.setAceInternalAbilityHandler(this.bundleName + "." + this.abilityName, aceInternalAbilityHandler);
    }
}
