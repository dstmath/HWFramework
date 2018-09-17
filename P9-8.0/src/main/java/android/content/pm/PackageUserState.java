package android.content.pm;

import android.util.ArraySet;
import com.android.internal.util.ArrayUtils;
import java.util.Arrays;

public class PackageUserState {
    public int appLinkGeneration;
    public int categoryHint;
    public long ceDataInode;
    public ArraySet<String> disabledComponents;
    public int domainVerificationStatus;
    public int enabled;
    public ArraySet<String> enabledComponents;
    public boolean hidden;
    public int installReason;
    public boolean installed;
    public boolean instantApp;
    public String lastDisableAppCaller;
    public boolean notLaunched;
    public String[] overlayPaths;
    public boolean stopped;
    public boolean suspended;

    public PackageUserState() {
        this.categoryHint = -1;
        this.installed = true;
        this.hidden = false;
        this.suspended = false;
        this.enabled = 0;
        this.domainVerificationStatus = 0;
        this.installReason = 0;
    }

    public PackageUserState(PackageUserState o) {
        String[] strArr = null;
        this.categoryHint = -1;
        this.ceDataInode = o.ceDataInode;
        this.installed = o.installed;
        this.stopped = o.stopped;
        this.notLaunched = o.notLaunched;
        this.hidden = o.hidden;
        this.suspended = o.suspended;
        this.instantApp = o.instantApp;
        this.enabled = o.enabled;
        this.lastDisableAppCaller = o.lastDisableAppCaller;
        this.domainVerificationStatus = o.domainVerificationStatus;
        this.appLinkGeneration = o.appLinkGeneration;
        this.categoryHint = o.categoryHint;
        this.installReason = o.installReason;
        this.disabledComponents = ArrayUtils.cloneOrNull(o.disabledComponents);
        this.enabledComponents = ArrayUtils.cloneOrNull(o.enabledComponents);
        if (o.overlayPaths != null) {
            strArr = (String[]) Arrays.copyOf(o.overlayPaths, o.overlayPaths.length);
        }
        this.overlayPaths = strArr;
    }

    public boolean isAvailable(int flags) {
        boolean matchAnyUser = (4194304 & flags) != 0;
        boolean matchUninstalled = (flags & 8192) != 0;
        if (matchAnyUser) {
            return true;
        }
        if (this.installed) {
            return this.hidden ? matchUninstalled : true;
        } else {
            return false;
        }
    }

    public boolean isMatch(ComponentInfo componentInfo, int flags) {
        boolean isSystemApp = componentInfo.applicationInfo.isSystemApp();
        boolean matchUninstalled = (PackageManager.MATCH_KNOWN_PACKAGES & flags) != 0;
        if (!isAvailable(flags)) {
            if (!isSystemApp) {
                matchUninstalled = false;
            }
            if ((matchUninstalled ^ 1) != 0) {
                return false;
            }
        }
        if (!isEnabled(componentInfo, flags)) {
            return false;
        }
        if ((1048576 & flags) != 0 && !isSystemApp) {
            return false;
        }
        int matchesUnaware;
        boolean matchesAware;
        if ((262144 & flags) != 0) {
            matchesUnaware = componentInfo.directBootAware ^ 1;
        } else {
            matchesUnaware = 0;
        }
        if ((524288 & flags) != 0) {
            matchesAware = componentInfo.directBootAware;
        } else {
            matchesAware = false;
        }
        if (matchesUnaware != 0) {
            matchesAware = true;
        }
        return matchesAware;
    }

    public boolean isEnabled(ComponentInfo componentInfo, int flags) {
        if ((flags & 512) != 0) {
            return true;
        }
        switch (this.enabled) {
            case 0:
                break;
            case 2:
            case 3:
                return false;
            case 4:
                if ((32768 & flags) == 0) {
                    return false;
                }
                break;
        }
        if (!componentInfo.applicationInfo.enabled) {
            return false;
        }
        if (ArrayUtils.contains(this.enabledComponents, componentInfo.name)) {
            return true;
        }
        if (ArrayUtils.contains(this.disabledComponents, componentInfo.name)) {
            return false;
        }
        return componentInfo.enabled;
    }

    /* JADX WARNING: Missing block: B:31:0x004b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean equals(Object obj) {
        if (!(obj instanceof PackageUserState)) {
            return false;
        }
        PackageUserState oldState = (PackageUserState) obj;
        if (this.ceDataInode != oldState.ceDataInode || this.installed != oldState.installed || this.stopped != oldState.stopped || this.notLaunched != oldState.notLaunched || this.hidden != oldState.hidden || this.suspended != oldState.suspended || this.instantApp != oldState.instantApp || this.enabled != oldState.enabled) {
            return false;
        }
        if ((this.lastDisableAppCaller == null && oldState.lastDisableAppCaller != null) || ((this.lastDisableAppCaller != null && (this.lastDisableAppCaller.equals(oldState.lastDisableAppCaller) ^ 1) != 0) || this.domainVerificationStatus != oldState.domainVerificationStatus || this.appLinkGeneration != oldState.appLinkGeneration || this.categoryHint != oldState.categoryHint || this.installReason != oldState.installReason)) {
            return false;
        }
        if ((this.disabledComponents == null && oldState.disabledComponents != null) || (this.disabledComponents != null && oldState.disabledComponents == null)) {
            return false;
        }
        int i;
        if (this.disabledComponents != null) {
            if (this.disabledComponents.size() != oldState.disabledComponents.size()) {
                return false;
            }
            for (i = this.disabledComponents.size() - 1; i >= 0; i--) {
                if (!oldState.disabledComponents.contains(this.disabledComponents.valueAt(i))) {
                    return false;
                }
            }
        }
        if ((this.enabledComponents == null && oldState.enabledComponents != null) || (this.enabledComponents != null && oldState.enabledComponents == null)) {
            return false;
        }
        if (this.enabledComponents != null) {
            if (this.enabledComponents.size() != oldState.enabledComponents.size()) {
                return false;
            }
            for (i = this.enabledComponents.size() - 1; i >= 0; i--) {
                if (!oldState.enabledComponents.contains(this.enabledComponents.valueAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
