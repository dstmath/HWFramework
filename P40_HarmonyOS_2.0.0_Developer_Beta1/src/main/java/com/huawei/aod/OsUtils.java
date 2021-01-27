package com.huawei.aod;

import android.content.Context;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.provider.SettingsEx;

public class OsUtils {
    private static final String TAG = "OsUtils";

    private static int getCurrentUser() {
        return ActivityManagerEx.getCurrentUser();
    }

    public static int getSecurityInt(Context context, String name, int def) {
        if (context == null) {
            return def;
        }
        return SettingsEx.Secure.getIntForUser(context.getContentResolver(), name, def, getCurrentUser());
    }
}
