package ohos.global.resource.solidxml;

import java.util.HashMap;

public abstract class Theme {
    public abstract Theme getCombinedTheme(Theme theme);

    public abstract HashMap<String, TypedAttribute> getThemeHash();
}
