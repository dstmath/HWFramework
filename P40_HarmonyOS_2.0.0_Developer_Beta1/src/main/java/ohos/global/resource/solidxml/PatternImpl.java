package ohos.global.resource.solidxml;

import java.util.HashMap;

public class PatternImpl extends Pattern {
    private HashMap<String, TypedAttribute> patternHash = new HashMap<>();

    public PatternImpl(HashMap<String, TypedAttribute> hashMap) {
        if (hashMap != null) {
            this.patternHash = hashMap;
        }
    }

    public void setPatternHash(HashMap<String, TypedAttribute> hashMap) {
        if (hashMap != null) {
            this.patternHash = hashMap;
        }
    }

    @Override // ohos.global.resource.solidxml.Pattern
    public HashMap<String, TypedAttribute> getPatternHash() {
        return this.patternHash;
    }

    @Override // ohos.global.resource.solidxml.Pattern
    public Pattern getCombinedPattern(Pattern pattern) {
        HashMap<String, TypedAttribute> patternHash2 = pattern.getPatternHash();
        HashMap hashMap = new HashMap(this.patternHash.size() + patternHash2.size());
        hashMap.putAll(this.patternHash);
        hashMap.putAll(patternHash2);
        return new PatternImpl(hashMap);
    }

    @Override // ohos.global.resource.solidxml.Pattern
    public void set(Pattern pattern) {
        if (pattern != null) {
            if (this.patternHash == null) {
                this.patternHash = new HashMap<>();
            }
            this.patternHash.clear();
            this.patternHash.putAll(pattern.getPatternHash());
        }
    }

    @Override // ohos.global.resource.solidxml.Pattern
    public HashMap<String, TypedAttribute> getPatternHash(String[] strArr) {
        if (this.patternHash == null || strArr == null || strArr.length == 0) {
            return new HashMap<>();
        }
        HashMap<String, TypedAttribute> hashMap = new HashMap<>(strArr.length);
        for (String str : strArr) {
            TypedAttribute typedAttribute = this.patternHash.get(str);
            if (typedAttribute != null) {
                hashMap.put(str, typedAttribute);
            }
        }
        return hashMap;
    }
}
