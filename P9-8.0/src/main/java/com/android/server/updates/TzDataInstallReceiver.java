package com.android.server.updates;

import android.util.Slog;
import java.io.File;
import java.io.IOException;
import libcore.tzdata.update2.TimeZoneDistroInstaller;

public class TzDataInstallReceiver extends ConfigUpdateInstallReceiver {
    private static final File SYSTEM_TZ_DATA_FILE = new File("/system/usr/share/zoneinfo/tzdata");
    private static final String TAG = "TZDataInstallReceiver";
    private static final File TZ_DATA_DIR = new File("/data/misc/zoneinfo");
    private static final String UPDATE_CONTENT_FILE_NAME = "tzdata_distro.zip";
    private static final String UPDATE_DIR_NAME = (TZ_DATA_DIR.getPath() + "/updates/");
    private static final String UPDATE_METADATA_DIR_NAME = "metadata/";
    private static final String UPDATE_VERSION_FILE_NAME = "version";
    private final TimeZoneDistroInstaller installer = new TimeZoneDistroInstaller(TAG, SYSTEM_TZ_DATA_FILE, TZ_DATA_DIR);

    public TzDataInstallReceiver() {
        super(UPDATE_DIR_NAME, UPDATE_CONTENT_FILE_NAME, UPDATE_METADATA_DIR_NAME, UPDATE_VERSION_FILE_NAME);
    }

    protected void install(byte[] content, int version) throws IOException {
        Slog.i(TAG, "Timezone data install valid for this device: " + this.installer.install(content));
        super.install(content, version);
    }
}
