package ohos.global.resource.solidxml;

import java.util.HashMap;

public abstract class Theme {
    public abstract Theme getCombinedTheme(Theme theme);

    public abstract HashMap<String, TypedAttribute> getThemeHash();

    public abstract HashMap<String, TypedAttribute> getThemeHash(String[] strArr);

    public abstract void set(Theme theme);
}
