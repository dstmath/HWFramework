package com.huawei.android.os;

import android.os.Environment;
import android.os.storage.StorageVolume;
import com.huawei.annotation.HwSystemApi;
import huawei.android.os.HwEnvironment;
import java.io.File;

public class EnvironmentEx {

    public static class UserEnvironmentSD {
        private HwEnvironment.UserEnvironmentSD mCurrentUserSd;

        public UserEnvironmentSD(int userId) {
            this.mCurrentUserSd = new HwEnvironment.UserEnvironmentSD(userId);
        }

        @Deprecated
        public File getExternalStorageDirectory() {
            return this.mCurrentUserSd.getExternalStorageDirectory();
        }

        @Deprecated
        public File getExternalStoragePublicDirectory(String type) {
            return this.mCurrentUserSd.getExternalStoragePublicDirectory(type);
        }

        public File[] getExternalDirsForApp() {
            return this.mCurrentUserSd.getExternalDirsForApp();
        }

        public File getMediaDir() {
            return this.mCurrentUserSd.getMediaDir();
        }

        public File[] buildExternalStoragePublicDirs(String type) {
            return this.mCurrentUserSd.buildExternalStoragePublicDirs(type);
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

    @HwSystemApi
    public static File getDataSystemDirectory() {
        return Environment.getDataSystemDirectory();
    }
}
