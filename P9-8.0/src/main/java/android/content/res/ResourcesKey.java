package android.content.res;

import android.text.TextUtils;
import java.util.Arrays;
import java.util.Objects;

public final class ResourcesKey {
    public final CompatibilityInfo mCompatInfo;
    public final int mDisplayId;
    private final int mHash;
    public final String[] mLibDirs;
    public final String[] mOverlayDirs;
    public final Configuration mOverrideConfiguration;
    public final String mResDir;
    public final String[] mSplitResDirs;

    public ResourcesKey(String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo) {
        this.mResDir = resDir;
        this.mSplitResDirs = splitResDirs;
        this.mOverlayDirs = overlayDirs;
        this.mLibDirs = libDirs;
        this.mDisplayId = displayId;
        if (overrideConfig == null) {
            overrideConfig = Configuration.EMPTY;
        }
        this.mOverrideConfiguration = new Configuration(overrideConfig);
        if (compatInfo == null) {
            compatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        }
        this.mCompatInfo = compatInfo;
        this.mHash = ((((((((((((Objects.hashCode(this.mResDir) + 527) * 31) + Arrays.hashCode(this.mSplitResDirs)) * 31) + Arrays.hashCode(this.mOverlayDirs)) * 31) + Arrays.hashCode(this.mLibDirs)) * 31) + this.mDisplayId) * 31) + Objects.hashCode(this.mOverrideConfiguration)) * 31) + Objects.hashCode(this.mCompatInfo);
    }

    public boolean hasOverrideConfiguration() {
        return Configuration.EMPTY.equals(this.mOverrideConfiguration) ^ 1;
    }

    public boolean isPathReferenced(String path) {
        boolean z = true;
        if (this.mResDir != null && this.mResDir.startsWith(path)) {
            return true;
        }
        if (!(anyStartsWith(this.mSplitResDirs, path) || anyStartsWith(this.mOverlayDirs, path))) {
            z = anyStartsWith(this.mLibDirs, path);
        }
        return z;
    }

    private static boolean anyStartsWith(String[] list, String prefix) {
        if (list != null) {
            for (String s : list) {
                if (s != null && s.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return this.mHash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ResourcesKey)) {
            return false;
        }
        ResourcesKey peer = (ResourcesKey) obj;
        if (this.mHash == peer.mHash && Objects.equals(this.mResDir, peer.mResDir) && Arrays.equals(this.mSplitResDirs, peer.mSplitResDirs) && Arrays.equals(this.mOverlayDirs, peer.mOverlayDirs) && Arrays.equals(this.mLibDirs, peer.mLibDirs) && this.mDisplayId == peer.mDisplayId && Objects.equals(this.mOverrideConfiguration, peer.mOverrideConfiguration) && Objects.equals(this.mCompatInfo, peer.mCompatInfo)) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append("ResourcesKey{");
        builder.append(" mHash=").append(Integer.toHexString(this.mHash));
        builder.append(" mResDir=").append(this.mResDir);
        builder.append(" mSplitDirs=[");
        if (this.mSplitResDirs != null) {
            builder.append(TextUtils.join(",", this.mSplitResDirs));
        }
        builder.append("]");
        builder.append(" mOverlayDirs=[");
        if (this.mOverlayDirs != null) {
            builder.append(TextUtils.join(",", this.mOverlayDirs));
        }
        builder.append("]");
        builder.append(" mLibDirs=[");
        if (this.mLibDirs != null) {
            builder.append(TextUtils.join(",", this.mLibDirs));
        }
        builder.append("]");
        builder.append(" mDisplayId=").append(this.mDisplayId);
        builder.append(" mOverrideConfig=").append(Configuration.resourceQualifierString(this.mOverrideConfiguration));
        builder.append(" mCompatInfo=").append(this.mCompatInfo);
        builder.append("}");
        return builder.toString();
    }
}
