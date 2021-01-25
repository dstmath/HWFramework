package com.android.server.backup.params;

import android.os.ParcelFileDescriptor;

public class AdbBackupParams extends AdbParams {
    public boolean allApps;
    public boolean doCompress;
    public boolean doWidgets;
    public boolean includeApks;
    public boolean includeKeyValue;
    public boolean includeObbs;
    public boolean includeShared;
    public boolean includeSystem;
    public String[] packages;

    public AdbBackupParams(ParcelFileDescriptor output, boolean saveApks, boolean saveObbs, boolean saveShared, boolean alsoWidgets, boolean doAllApps, boolean doSystem, boolean compress, boolean doKeyValue, String[] pkgList) {
        this.fd = output;
        this.includeApks = saveApks;
        this.includeObbs = saveObbs;
        this.includeShared = saveShared;
        this.doWidgets = alsoWidgets;
        this.allApps = doAllApps;
        this.includeSystem = doSystem;
        this.doCompress = compress;
        this.includeKeyValue = doKeyValue;
        this.packages = pkgList;
    }
}
