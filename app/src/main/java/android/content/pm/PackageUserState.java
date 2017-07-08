package android.content.pm;

import android.os.Process;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.ArraySet;
import com.android.internal.util.ArrayUtils;

public class PackageUserState {
    public int appLinkGeneration;
    public boolean blockUninstall;
    public long ceDataInode;
    public ArraySet<String> disabledComponents;
    public int domainVerificationStatus;
    public int enabled;
    public ArraySet<String> enabledComponents;
    public boolean hidden;
    public boolean installed;
    public String lastDisableAppCaller;
    public boolean notLaunched;
    public boolean stopped;
    public boolean suspended;

    public PackageUserState() {
        this.installed = true;
        this.hidden = false;
        this.suspended = false;
        this.enabled = 0;
        this.domainVerificationStatus = 0;
    }

    public PackageUserState(PackageUserState o) {
        this.ceDataInode = o.ceDataInode;
        this.installed = o.installed;
        this.stopped = o.stopped;
        this.notLaunched = o.notLaunched;
        this.hidden = o.hidden;
        this.suspended = o.suspended;
        this.blockUninstall = o.blockUninstall;
        this.enabled = o.enabled;
        this.lastDisableAppCaller = o.lastDisableAppCaller;
        this.domainVerificationStatus = o.domainVerificationStatus;
        this.appLinkGeneration = o.appLinkGeneration;
        this.disabledComponents = ArrayUtils.cloneOrNull(o.disabledComponents);
        this.enabledComponents = ArrayUtils.cloneOrNull(o.enabledComponents);
    }

    public boolean isInstalled(int flags) {
        return (this.installed && !this.hidden) || (flags & Process.PROC_OUT_LONG) != 0;
    }

    public boolean isMatch(ComponentInfo componentInfo, int flags) {
        if (!isInstalled(flags) || !isEnabled(componentInfo, flags)) {
            return false;
        }
        if ((Root.FLAG_REMOVABLE_USB & flags) != 0 && !componentInfo.applicationInfo.isSystemApp()) {
            return false;
        }
        boolean z;
        boolean matchesUnaware = (Root.FLAG_HAS_SETTINGS & flags) != 0 ? !componentInfo.directBootAware : false;
        if ((Root.FLAG_REMOVABLE_SD & flags) != 0) {
            z = componentInfo.directBootAware;
        } else {
            z = false;
        }
        if (matchesUnaware) {
            z = true;
        }
        return z;
    }

    public boolean isEnabled(ComponentInfo componentInfo, int flags) {
        if ((flags & Document.FLAG_VIRTUAL_DOCUMENT) != 0) {
            return true;
        }
        switch (this.enabled) {
            case TextToSpeech.SUCCESS /*0*/:
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
                return false;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                if ((Document.FLAG_ARCHIVE & flags) == 0) {
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
}
