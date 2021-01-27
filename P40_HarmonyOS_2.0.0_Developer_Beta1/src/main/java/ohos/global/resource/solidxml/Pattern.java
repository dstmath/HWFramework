package ohos.global.resource.solidxml;

import java.util.HashMap;

public abstract class Pattern {
    public abstract Pattern getCombinedPattern(Pattern pattern);

    public abstract HashMap<String, TypedAttribute> getPatternHash();

    public abstract HashMap<String, TypedAttribute> getPatternHash(String[] strArr);

    public abstract void set(Pattern pattern);
}
