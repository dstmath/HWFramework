package ohos.ace.featureabilityplugin;

import com.huawei.ace.runtime.ALog;
import java.util.HashMap;
import java.util.Map;
import ohos.ace.ability.AceInternalAbility;

public class AbilityManager {
    private static final int ABILITY = 0;
    private static final int ASYNC_REQ = 1;
    private static final int INTERNAL_ABILITY = 1;
    private static final int SYNC_REQ = 0;
    private static final String TAG = AbilityManager.class.getSimpleName();
    private static Map<Integer, Map<String, AbilityConnection>> abilityConnections = new HashMap();
    private static AbilityManager instance = new AbilityManager();
    private static Map<String, AceInternalAbility.AceInternalAbilityHandler> internalAbilityHandlers = new HashMap();
    private static Map<String, AbilityProxy> internalAbilityProxy = new HashMap();
    private static Map<String, AbilityStub> internalAbilityStub = new HashMap();

    public static AbilityManager getInstance() {
        return instance;
    }

    public AbilityConnection getAbilityConnection(int i, String str) {
        if (!abilityConnections.containsKey(Integer.valueOf(i)) || !abilityConnections.get(Integer.valueOf(i)).containsKey(str)) {
            return null;
        }
        return abilityConnections.get(Integer.valueOf(i)).get(str);
    }

    public void addAbilityConnection(int i, String str, AbilityConnection abilityConnection) {
        if (abilityConnections.containsKey(Integer.valueOf(i))) {
            abilityConnections.get(Integer.valueOf(i)).put(str, abilityConnection);
            return;
        }
        HashMap hashMap = new HashMap();
        hashMap.put(str, abilityConnection);
        abilityConnections.put(Integer.valueOf(i), hashMap);
    }

    public void removeAbilityConnection(int i, String str) {
        if (abilityConnections.containsKey(Integer.valueOf(i))) {
            Map<String, AbilityConnection> map = abilityConnections.get(Integer.valueOf(i));
            map.remove(str);
            if (map.isEmpty()) {
                abilityConnections.remove(Integer.valueOf(i));
            }
        }
    }

    public void removeConnectionsByAbilityId(int i) {
        if (abilityConnections.containsKey(Integer.valueOf(i))) {
            abilityConnections.get(Integer.valueOf(i)).clear();
            abilityConnections.remove(Integer.valueOf(i));
        }
    }

    public boolean checkAbilityConnectionExist(int i, String str) {
        return abilityConnections.containsKey(Integer.valueOf(i)) && abilityConnections.get(Integer.valueOf(i)).containsKey(str);
    }

    public Map<String, AceInternalAbility.AceInternalAbilityHandler> getInternalAbilityHandlers() {
        return internalAbilityHandlers;
    }

    public Map<String, AbilityProxy> getInternalAbilityProxy() {
        return internalAbilityProxy;
    }

    public Map<String, AbilityStub> getInternalAbilityStub() {
        return internalAbilityStub;
    }

    public void setAceInternalAbilityHandler(String str, AceInternalAbility.AceInternalAbilityHandler aceInternalAbilityHandler) {
        if (aceInternalAbilityHandler == null) {
            String str2 = TAG;
            ALog.d(str2, "Removing internal ability handler '" + str + "'");
            internalAbilityHandlers.remove(str);
            internalAbilityProxy.remove(str);
            internalAbilityStub.remove(str);
            return;
        }
        if (internalAbilityHandlers.containsKey(str)) {
            String str3 = TAG;
            ALog.i(str3, "Resetting internal ability handler '" + str + "', delete stub first.");
            internalAbilityHandlers.remove(str);
            internalAbilityProxy.remove(str);
            internalAbilityStub.remove(str);
        }
        String str4 = TAG;
        ALog.d(str4, "Setting internal ability handler '" + str + "'");
        internalAbilityHandlers.put(str, aceInternalAbilityHandler);
    }

    public int getAbilityHandlersNum() {
        return internalAbilityHandlers.size();
    }
}
