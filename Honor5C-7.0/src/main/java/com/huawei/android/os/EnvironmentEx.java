package com.huawei.android.os;

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
}
