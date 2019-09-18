package com.android.server.wifi;

import android.os.Build;

class SystemBuildProperties implements BuildProperties {
    SystemBuildProperties() {
    }

    public boolean isEngBuild() {
        return Build.TYPE.equals("eng");
    }

    public boolean isUserdebugBuild() {
        return Build.TYPE.equals("userdebug");
    }

    public boolean isUserBuild() {
        return Build.TYPE.equals("user");
    }
}
