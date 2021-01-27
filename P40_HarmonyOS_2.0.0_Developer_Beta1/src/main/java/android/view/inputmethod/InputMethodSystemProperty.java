package android.view.inputmethod;

import android.content.ComponentName;
import android.os.Build;
import android.os.SystemProperties;

public class InputMethodSystemProperty {
    public static final boolean MULTI_CLIENT_IME_ENABLED = (sMultiClientImeComponentName != null);
    public static final boolean PER_PROFILE_IME_ENABLED;
    private static final String PROP_DEBUG_MULTI_CLIENT_IME = "persist.debug.multi_client_ime";
    private static final String PROP_DEBUG_PER_PROFILE_IME = "persist.debug.per_profile_ime";
    private static final String PROP_PROD_MULTI_CLIENT_IME = "ro.sys.multi_client_ime";
    public static final ComponentName sMultiClientImeComponentName = getMultiClientImeComponentName();

    private static ComponentName getMultiClientImeComponentName() {
        ComponentName debugIme;
        if (!Build.IS_DEBUGGABLE || (debugIme = ComponentName.unflattenFromString(SystemProperties.get(PROP_DEBUG_MULTI_CLIENT_IME, ""))) == null) {
            return ComponentName.unflattenFromString(SystemProperties.get(PROP_PROD_MULTI_CLIENT_IME, ""));
        }
        return debugIme;
    }

    static {
        if (MULTI_CLIENT_IME_ENABLED) {
            PER_PROFILE_IME_ENABLED = true;
        } else if (Build.IS_DEBUGGABLE) {
            PER_PROFILE_IME_ENABLED = SystemProperties.getBoolean(PROP_DEBUG_PER_PROFILE_IME, true);
        } else {
            PER_PROFILE_IME_ENABLED = true;
        }
    }
}
