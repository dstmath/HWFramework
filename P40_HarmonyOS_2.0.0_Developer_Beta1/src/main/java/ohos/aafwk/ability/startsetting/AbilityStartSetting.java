package ohos.aafwk.ability.startsetting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbilityStartSetting {
    private Map<String, Object> startProperties = new HashMap();

    protected AbilityStartSetting() {
    }

    public static AbilityStartSetting getEmptySetting() {
        return new AbilityStartSetting();
    }

    public boolean isEmpty() {
        return this.startProperties.isEmpty();
    }

    /* access modifiers changed from: protected */
    public void addProperty(String str, Object obj) {
        this.startProperties.put(str, obj);
    }

    public Object getProperty(String str) {
        if (!this.startProperties.containsKey(str)) {
            return null;
        }
        Object obj = this.startProperties.get(str);
        this.startProperties.remove(str);
        return obj;
    }

    public Set<String> getPropertiesKey() {
        return this.startProperties.keySet();
    }
}
