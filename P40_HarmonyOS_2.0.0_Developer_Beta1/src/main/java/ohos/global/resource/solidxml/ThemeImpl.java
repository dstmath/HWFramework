package ohos.global.resource.solidxml;

import java.util.HashMap;

public class ThemeImpl extends Theme {
    private HashMap<String, TypedAttribute> themeHash = new HashMap<>();

    public ThemeImpl(HashMap<String, TypedAttribute> hashMap) {
        if (hashMap != null) {
            this.themeHash = hashMap;
        }
    }

    public void setThemeHash(HashMap<String, TypedAttribute> hashMap) {
        if (hashMap != null) {
            this.themeHash = hashMap;
        }
    }

    @Override // ohos.global.resource.solidxml.Theme
    public HashMap<String, TypedAttribute> getThemeHash() {
        return this.themeHash;
    }

    @Override // ohos.global.resource.solidxml.Theme
    public Theme getCombinedTheme(Theme theme) {
        HashMap<String, TypedAttribute> themeHash2 = theme.getThemeHash();
        HashMap hashMap = new HashMap(this.themeHash.size() + themeHash2.size());
        hashMap.putAll(this.themeHash);
        hashMap.putAll(themeHash2);
        return new ThemeImpl(hashMap);
    }

    @Override // ohos.global.resource.solidxml.Theme
    public void set(Theme theme) {
        if (theme != null) {
            if (this.themeHash == null) {
                this.themeHash = new HashMap<>();
            }
            this.themeHash.clear();
            this.themeHash.putAll(theme.getThemeHash());
        }
    }

    @Override // ohos.global.resource.solidxml.Theme
    public HashMap<String, TypedAttribute> getThemeHash(String[] strArr) {
        if (this.themeHash == null || strArr == null || strArr.length == 0) {
            return new HashMap<>();
        }
        HashMap<String, TypedAttribute> hashMap = new HashMap<>(strArr.length);
        for (String str : strArr) {
            TypedAttribute typedAttribute = this.themeHash.get(str);
            if (typedAttribute != null) {
                hashMap.put(str, typedAttribute);
            }
        }
        return hashMap;
    }
}
