package ohos.security.sfp;

import java.io.IOException;
import ohos.aafwk.ability.Ability;
import ohos.rpc.IRemoteBroker;

interface ISfpPolicyProxy extends IRemoteBroker {
    public static final String DESCRIPTOR = "ISfpPolicyProxy";

    int getFlag(Ability ability, String str, String str2);

    String getLabel(Ability ability, String str, String str2);

    int getPolicyProtectType(Ability ability, String str) throws IllegalArgumentException, IllegalStateException, IllegalAccessException;

    void setEcePolicy(Ability ability, String str) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, IOException;

    int setLabel(Ability ability, String str, String str2, String str3, int i);

    void setSecePolicy(Ability ability, String str) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, IOException;
}
