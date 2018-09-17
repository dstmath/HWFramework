package com.android.server.pm;

public class SettingsUtils {
    public static SharedUserSetting addSharedUserLPw(Object settings, String name, int uid, int pkgFlags, int pkgPrivateFlags) {
        if (settings instanceof Settings) {
            return ((Settings) settings).addSharedUserLPw(name, uid, pkgFlags, pkgPrivateFlags);
        }
        return null;
    }
}
