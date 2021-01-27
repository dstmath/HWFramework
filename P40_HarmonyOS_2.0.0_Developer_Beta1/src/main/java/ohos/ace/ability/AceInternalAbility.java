package ohos.ace.ability;

import com.huawei.ace.runtime.ALog;
import ohos.ace.featureabilityplugin.AbilityManager;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class AceInternalAbility {
    private static final String TAG = "AceInternalAbility";
    private String abilityName;

    public interface AceInternalAbilityHandler {
        boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException;
    }

    public AceInternalAbility(String str, String str2) {
        if (str2 == null || "".equals(str2)) {
            ALog.e(TAG, "abilityName name must not be null or empty string");
            throw new IllegalArgumentException("abilityName is null or empty string");
        } else if (str == null || "".equals(str)) {
            this.abilityName = str2;
        } else {
            ALog.e(TAG, "the constructor has been discard (bundleName is not in used)");
            this.abilityName = str + "." + str2;
        }
    }

    public AceInternalAbility(String str) {
        this(null, str);
    }

    public void setInternalAbilityHandler(AceInternalAbilityHandler aceInternalAbilityHandler) {
        AbilityManager.getInstance().setAceInternalAbilityHandler(this.abilityName, aceInternalAbilityHandler);
    }
}
