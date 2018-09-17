package com.android.server.wifi;

import android.content.pm.UserInfo;
import android.net.wifi.WifiConfiguration;
import android.os.UserHandle;
import java.util.List;

public class WifiConfigurationUtil {
    public static boolean isVisibleToAnyProfile(WifiConfiguration config, List<UserInfo> profiles) {
        if (config.shared) {
            return true;
        }
        int creatorUserId = UserHandle.getUserId(config.creatorUid);
        for (UserInfo profile : profiles) {
            if (profile.id == creatorUserId) {
                return true;
            }
        }
        return false;
    }
}
