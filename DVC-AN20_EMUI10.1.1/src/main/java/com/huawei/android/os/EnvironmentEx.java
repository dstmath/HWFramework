package com.huawei.android.os;

import android.os.Environment;
import android.os.storage.StorageVolume;
import huawei.android.os.HwEnvironment;
import java.io.File;

public class EnvironmentEx {

    public static class UserEnvironmentSD {
        private HwEnvironment.UserEnvironmentSD sCurrentUserSd;

        public UserEnvironmentSD(int userId) {
            this.sCurrentUserSd = new HwEnvironment.UserEnvironmentSD(userId);
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
        String VolumeId;
        if (storageVolume != null && (VolumeId = storageVolume.getId()) != null && !VolumeId.contains("public:179") && VolumeId.contains("public:")) {
            return true;
        }
        return false;
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        return Environment.maybeTranslateEmulatedPathToInternal(path);
    }
}
