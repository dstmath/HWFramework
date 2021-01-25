package ohos.abilityshell;

import ohos.abilityshell.utils.DisplayResolveInfo;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;

public class AbilityShellData {
    private AbilityInfo abilityInfo;
    private String deviceName = "";
    private DisplayResolveInfo displayResolveInfo;
    private boolean isLocal = false;
    private ShellInfo shellInfo;

    public AbilityShellData(boolean z, AbilityInfo abilityInfo2, ShellInfo shellInfo2) {
        this.isLocal = z;
        this.abilityInfo = abilityInfo2;
        this.shellInfo = shellInfo2;
    }

    public void setLocal(boolean z) {
        this.isLocal = z;
    }

    public void setAbilityInfo(AbilityInfo abilityInfo2) {
        this.abilityInfo = abilityInfo2;
    }

    public void setShellInfo(ShellInfo shellInfo2) {
        this.shellInfo = shellInfo2;
    }

    public void setDeviceName(String str) {
        this.deviceName = str;
    }

    public void setDisplayResolveInfo(DisplayResolveInfo displayResolveInfo2) {
        this.displayResolveInfo = displayResolveInfo2;
    }

    public boolean getLocal() {
        return this.isLocal;
    }

    public AbilityInfo getAbilityInfo() {
        return this.abilityInfo;
    }

    public ShellInfo getShellInfo() {
        return this.shellInfo;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public DisplayResolveInfo getDisplayResolveInfo() {
        return this.displayResolveInfo;
    }
}
