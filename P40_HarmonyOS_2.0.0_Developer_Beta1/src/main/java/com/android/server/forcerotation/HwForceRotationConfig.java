package com.android.server.forcerotation;

import java.util.ArrayList;
import java.util.List;

public class HwForceRotationConfig {
    private List<String> forceRotationAppNames = new ArrayList();
    private List<String> notSupportForceRotationAppActivityNames = new ArrayList();

    public boolean isAppSupportForceRotation(String appPkgName) {
        return this.forceRotationAppNames.contains(appPkgName);
    }

    public boolean isActivitySupportForceRotation(String activityName) {
        return !this.notSupportForceRotationAppActivityNames.contains(activityName);
    }

    public void addForceRotationAppName(String appPkgName) {
        if (!this.forceRotationAppNames.contains(appPkgName)) {
            this.forceRotationAppNames.add(appPkgName);
        }
    }

    public void addNotSupportForceRotationAppActivityName(String activityName) {
        if (!this.notSupportForceRotationAppActivityNames.contains(activityName)) {
            this.notSupportForceRotationAppActivityNames.add(activityName);
        }
    }

    public List<String> getAllForceRotationAppNames() {
        return this.forceRotationAppNames;
    }

    public List<String> getAllNotSupportForceRotationAppActivityNames() {
        return this.notSupportForceRotationAppActivityNames;
    }
}
