package com.huawei.wallet.sdk.business.idcard.commonbase.server;

import java.util.HashMap;
import java.util.Map;

public class AddressName {
    private String addressName;
    private Map<String, String> conditionMap = new HashMap();
    private String moduleName;
    private String serverAddressName;

    public AddressName(String addressName2, String serverAddressName2, String moduleName2) {
        this.addressName = addressName2;
        this.serverAddressName = serverAddressName2;
        this.moduleName = moduleName2;
    }

    public String getAddressName() {
        return this.addressName;
    }

    public String getServerAddressName() {
        return this.serverAddressName;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public void addCondition(String key, String value) {
        this.conditionMap.put(key, value);
    }

    public boolean conditionMatch(Map<String, String> conditionMap2) {
        if (conditionMap2 == null || conditionMap2.isEmpty()) {
            if (this.conditionMap.isEmpty()) {
                return true;
            }
            return false;
        } else if (this.conditionMap.isEmpty() || conditionMap2.size() != this.conditionMap.size()) {
            return false;
        } else {
            for (Map.Entry<String, String> entry : conditionMap2.entrySet()) {
                if (!this.conditionMap.containsKey(entry.getKey())) {
                    return false;
                }
                if (!this.conditionMap.get(entry.getKey()).equalsIgnoreCase(entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
    }
}
