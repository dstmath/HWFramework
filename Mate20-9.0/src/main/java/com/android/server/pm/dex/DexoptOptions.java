package com.android.server.pm.dex;

import com.android.server.pm.PackageManagerServiceCompilerMapping;

public final class DexoptOptions {
    public static final int DEXOPT_AS_SHARED_LIBRARY = 64;
    public static final int DEXOPT_BOOT_COMPLETE = 4;
    public static final int DEXOPT_CHECK_FOR_PROFILES_UPDATES = 1;
    public static final int DEXOPT_DOWNGRADE = 32;
    public static final int DEXOPT_FORCE = 2;
    public static final int DEXOPT_IDLE_BACKGROUND_JOB = 512;
    public static final int DEXOPT_INSTALL_WITH_DEX_METADATA_FILE = 1024;
    public static final int DEXOPT_ONLY_SECONDARY_DEX = 8;
    public static final int DEXOPT_ONLY_SHARED_DEX = 16;
    private final int mCompilationReason;
    private final String mCompilerFilter;
    private final int mFlags;
    private final String mPackageName;
    private final String mSplitName;

    public DexoptOptions(String packageName, String compilerFilter, int flags) {
        this(packageName, -1, compilerFilter, null, flags);
    }

    public DexoptOptions(String packageName, int compilationReason, int flags) {
        this(packageName, compilationReason, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(compilationReason), null, flags);
    }

    public DexoptOptions(String packageName, int compilationReason, String compilerFilter, String splitName, int flags) {
        if (((~1663) & flags) == 0) {
            this.mPackageName = packageName;
            this.mCompilerFilter = compilerFilter;
            this.mFlags = flags;
            this.mSplitName = splitName;
            this.mCompilationReason = compilationReason;
            return;
        }
        throw new IllegalArgumentException("Invalid flags : " + Integer.toHexString(flags));
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public boolean isCheckForProfileUpdates() {
        return (this.mFlags & 1) != 0;
    }

    public String getCompilerFilter() {
        return this.mCompilerFilter;
    }

    public boolean isForce() {
        return (this.mFlags & 2) != 0;
    }

    public boolean isBootComplete() {
        return (this.mFlags & 4) != 0;
    }

    public boolean isDexoptOnlySecondaryDex() {
        return (this.mFlags & 8) != 0;
    }

    public boolean isDexoptOnlySharedDex() {
        return (this.mFlags & 16) != 0;
    }

    public boolean isDowngrade() {
        return (this.mFlags & 32) != 0;
    }

    public boolean isDexoptAsSharedLibrary() {
        return (this.mFlags & 64) != 0;
    }

    public boolean isDexoptIdleBackgroundJob() {
        return (this.mFlags & 512) != 0;
    }

    public boolean isDexoptInstallWithDexMetadata() {
        return (this.mFlags & 1024) != 0;
    }

    public String getSplitName() {
        return this.mSplitName;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public int getCompilationReason() {
        return this.mCompilationReason;
    }
}
