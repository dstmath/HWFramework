package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.os.BaseBundle;
import android.os.PersistableBundle;
import android.util.ArraySet;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import java.util.Arrays;
import java.util.Objects;

public class PackageUserState {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "PackageUserState";
    public int appLinkGeneration;
    public int categoryHint;
    public long ceDataInode;
    public SuspendDialogInfo dialogInfo;
    public ArraySet<String> disabledComponents;
    public int distractionFlags;
    public int domainVerificationStatus;
    public int enabled;
    public ArraySet<String> enabledComponents;
    public String harmfulAppWarning;
    public boolean hidden;
    public int installReason;
    public boolean installed;
    public boolean instantApp;
    public String lastDisableAppCaller;
    public boolean notLaunched;
    public String[] overlayPaths;
    public boolean stopped;
    public boolean suspended;
    public PersistableBundle suspendedAppExtras;
    public PersistableBundle suspendedLauncherExtras;
    public String suspendingPackage;
    public boolean virtualPreload;

    @UnsupportedAppUsage
    public PackageUserState() {
        this.categoryHint = -1;
        this.installed = true;
        this.hidden = false;
        this.suspended = false;
        this.enabled = 0;
        this.domainVerificationStatus = 0;
        this.installReason = 0;
    }

    @VisibleForTesting
    public PackageUserState(PackageUserState o) {
        this.categoryHint = -1;
        this.ceDataInode = o.ceDataInode;
        this.installed = o.installed;
        this.stopped = o.stopped;
        this.notLaunched = o.notLaunched;
        this.hidden = o.hidden;
        this.distractionFlags = o.distractionFlags;
        this.suspended = o.suspended;
        this.suspendingPackage = o.suspendingPackage;
        this.dialogInfo = o.dialogInfo;
        this.suspendedAppExtras = o.suspendedAppExtras;
        this.suspendedLauncherExtras = o.suspendedLauncherExtras;
        this.instantApp = o.instantApp;
        this.virtualPreload = o.virtualPreload;
        this.enabled = o.enabled;
        this.lastDisableAppCaller = o.lastDisableAppCaller;
        this.domainVerificationStatus = o.domainVerificationStatus;
        this.appLinkGeneration = o.appLinkGeneration;
        this.categoryHint = o.categoryHint;
        this.installReason = o.installReason;
        this.disabledComponents = ArrayUtils.cloneOrNull(o.disabledComponents);
        this.enabledComponents = ArrayUtils.cloneOrNull(o.enabledComponents);
        String[] strArr = o.overlayPaths;
        this.overlayPaths = strArr == null ? null : (String[]) Arrays.copyOf(strArr, strArr.length);
        this.harmfulAppWarning = o.harmfulAppWarning;
    }

    public boolean isAvailable(int flags) {
        boolean matchAnyUser = (4194304 & flags) != 0;
        boolean matchUninstalled = (flags & 8192) != 0;
        if (!matchAnyUser) {
            return this.installed && (!this.hidden || matchUninstalled);
        }
        return true;
    }

    public boolean isMatch(ComponentInfo componentInfo, int flags) {
        boolean isSystemApp = componentInfo.applicationInfo.isSystemApp();
        boolean z = true;
        boolean matchUninstalled = (4202496 & flags) != 0;
        if (!isAvailable(flags) && (!isSystemApp || !matchUninstalled)) {
            return reportIfDebug(false, flags);
        }
        if (!isEnabled(componentInfo, flags)) {
            return reportIfDebug(false, flags);
        }
        if ((1048576 & flags) != 0 && !isSystemApp) {
            return reportIfDebug(false, flags);
        }
        boolean matchesUnaware = (262144 & flags) != 0 && !componentInfo.directBootAware;
        boolean matchesAware = (524288 & flags) != 0 && componentInfo.directBootAware;
        if (!matchesUnaware && !matchesAware) {
            z = false;
        }
        return reportIfDebug(z, flags);
    }

    private boolean reportIfDebug(boolean result, int flags) {
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002e A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x002f  */
    public boolean isEnabled(ComponentInfo componentInfo, int flags) {
        if ((flags & 512) != 0) {
            return true;
        }
        int i = this.enabled;
        if (i != 0) {
            if (i == 2 || i == 3) {
                return false;
            }
            if (i == 4) {
                if ((32768 & flags) == 0) {
                    return false;
                }
            }
            if (!ArrayUtils.contains(this.enabledComponents, componentInfo.name)) {
                return true;
            }
            if (ArrayUtils.contains(this.disabledComponents, componentInfo.name)) {
                return false;
            }
            return componentInfo.enabled;
        }
        if (!componentInfo.applicationInfo.enabled) {
            return false;
        }
        if (!ArrayUtils.contains(this.enabledComponents, componentInfo.name)) {
        }
    }

    public final boolean equals(Object obj) {
        boolean z;
        String str;
        String str2;
        String str3;
        if (!(obj instanceof PackageUserState)) {
            return false;
        }
        PackageUserState oldState = (PackageUserState) obj;
        if (!(this.ceDataInode == oldState.ceDataInode && this.installed == oldState.installed && this.stopped == oldState.stopped && this.notLaunched == oldState.notLaunched && this.hidden == oldState.hidden && this.distractionFlags == oldState.distractionFlags && (z = this.suspended) == oldState.suspended)) {
            return false;
        }
        if (!((!z || ((str3 = this.suspendingPackage) != null && str3.equals(oldState.suspendingPackage) && Objects.equals(this.dialogInfo, oldState.dialogInfo) && BaseBundle.kindofEquals(this.suspendedAppExtras, oldState.suspendedAppExtras) && BaseBundle.kindofEquals(this.suspendedLauncherExtras, oldState.suspendedLauncherExtras))) && this.instantApp == oldState.instantApp && this.virtualPreload == oldState.virtualPreload && this.enabled == oldState.enabled)) {
            return false;
        }
        if ((this.lastDisableAppCaller == null && oldState.lastDisableAppCaller != null) || !(((str = this.lastDisableAppCaller) == null || str.equals(oldState.lastDisableAppCaller)) && this.domainVerificationStatus == oldState.domainVerificationStatus && this.appLinkGeneration == oldState.appLinkGeneration && this.categoryHint == oldState.categoryHint && this.installReason == oldState.installReason)) {
            return false;
        }
        if ((this.disabledComponents == null && oldState.disabledComponents != null) || (this.disabledComponents != null && oldState.disabledComponents == null)) {
            return false;
        }
        ArraySet<String> arraySet = this.disabledComponents;
        if (arraySet != null) {
            if (arraySet.size() != oldState.disabledComponents.size()) {
                return false;
            }
            for (int i = this.disabledComponents.size() - 1; i >= 0; i--) {
                if (!oldState.disabledComponents.contains(this.disabledComponents.valueAt(i))) {
                    return false;
                }
            }
        }
        if ((this.enabledComponents == null && oldState.enabledComponents != null) || (this.enabledComponents != null && oldState.enabledComponents == null)) {
            return false;
        }
        ArraySet<String> arraySet2 = this.enabledComponents;
        if (arraySet2 != null) {
            if (arraySet2.size() != oldState.enabledComponents.size()) {
                return false;
            }
            for (int i2 = this.enabledComponents.size() - 1; i2 >= 0; i2--) {
                if (!oldState.enabledComponents.contains(this.enabledComponents.valueAt(i2))) {
                    return false;
                }
            }
        }
        if ((this.harmfulAppWarning != null || oldState.harmfulAppWarning == null) && ((str2 = this.harmfulAppWarning) == null || str2.equals(oldState.harmfulAppWarning))) {
            return true;
        }
        return false;
    }
}
