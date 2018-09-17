package com.huawei.android.os;

import android.os.Environment;
import android.os.storage.StorageVolume;
import java.io.File;

public class EnvironmentEx {

    public static class UserEnvironmentSD {
        private huawei.android.os.HwEnvironment.UserEnvironmentSD sCurrentUserSd;

        public UserEnvironmentSD(int userId) {
            this.sCurrentUserSd = new huawei.android.os.HwEnvironment.UserEnvironmentSD(userId);
        }

        @Deprecated
        public File getExternalStorageDirectory() {
            return this.sCurrentUserSd.getExternalStorageDirectory();
        }

        @Deprecated
        public File getExternalStoragePublicDirectory(String type) {
            return this.sCurrentUserSd.getExternalStoragePublicDirectory(type);
        }

        public File[] getExternalDirsForApp() {
            return this.sCurrentUserSd.getExternalDirsForApp();
        }

        public File getMediaDir() {
            return this.sCurrentUserSd.getMediaDir();
        }

        public File[] buildExternalStoragePublicDirs(String type) {
            return this.sCurrentUserSd.buildExternalStoragePublicDirs(type);
        }
    }

    public static boolean isVolumeUsb(StorageVolume storageVolume) {
        if (storageVolume == null) {
            return false;
        }
        String VolumeId = storageVolume.getId();
        if (VolumeId == null || VolumeId.contains("public:179") || !VolumeId.contains("public:")) {
            return false;
        }
        return true;
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        return Environment.maybeTranslateEmulatedPathToInternal(path);
    }
}
