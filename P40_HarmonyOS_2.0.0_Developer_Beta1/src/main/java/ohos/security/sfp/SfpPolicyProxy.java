package ohos.security.sfp;

import android.content.Context;
import com.huawei.fileprotect.HwSfpPolicyManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import ohos.aafwk.ability.Ability;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.rpc.IRemoteObject;

class SfpPolicyProxy implements ISfpPolicyProxy {
    private static final String ERROR_MESSAGE = "Invalid input value!";
    private final IRemoteObject mRemote;

    SfpPolicyProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.sfp.ISfpPolicyProxy
    public void setEcePolicy(Ability ability, String str) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, IOException {
        Context transferAbililtyToContext = transferAbililtyToContext(ability);
        if (transferAbililtyToContext == null || str == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        try {
            HwSfpPolicyManager.getDefault().setEcePolicy(transferAbililtyToContext, str);
        } catch (FileNotFoundException unused) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
    }

    @Override // ohos.security.sfp.ISfpPolicyProxy
    public void setSecePolicy(Ability ability, String str) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, IOException {
        Context transferAbililtyToContext = transferAbililtyToContext(ability);
        if (transferAbililtyToContext == null || str == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        try {
            HwSfpPolicyManager.getDefault().setSecePolicy(transferAbililtyToContext, str);
        } catch (FileNotFoundException unused) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
    }

    @Override // ohos.security.sfp.ISfpPolicyProxy
    public int getPolicyProtectType(Ability ability, String str) throws IllegalArgumentException, IllegalStateException, IllegalAccessException {
        Context transferAbililtyToContext = transferAbililtyToContext(ability);
        if (transferAbililtyToContext == null || str == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        try {
            return HwSfpPolicyManager.getPolicyProtectType(transferAbililtyToContext, str);
        } catch (FileNotFoundException unused) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
    }

    @Override // ohos.security.sfp.ISfpPolicyProxy
    public int setLabel(Ability ability, String str, String str2, String str3, int i) {
        return HwSfpPolicyManager.getDefault().setLabel(transferAbililtyToContext(ability), str, str2, str3, i);
    }

    @Override // ohos.security.sfp.ISfpPolicyProxy
    public String getLabel(Ability ability, String str, String str2) {
        return HwSfpPolicyManager.getDefault().getLabel(transferAbililtyToContext(ability), str, str2);
    }

    @Override // ohos.security.sfp.ISfpPolicyProxy
    public int getFlag(Ability ability, String str, String str2) {
        return HwSfpPolicyManager.getDefault().getFlag(transferAbililtyToContext(ability), str, str2);
    }

    private Context transferAbililtyToContext(Ability ability) throws IllegalArgumentException {
        if (ability != null) {
            Object androidContext = AbilityContextUtils.getAndroidContext(ability);
            if (androidContext instanceof Context) {
                return (Context) androidContext;
            }
        }
        return null;
    }
}
