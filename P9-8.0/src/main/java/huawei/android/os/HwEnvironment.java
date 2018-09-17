package huawei.android.os;

import android.os.Environment;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.io.File;

public class HwEnvironment {
    private static final boolean IS_SWITCH_SD_ENABLED = "true".equals(SystemProperties.get("ro.config.switchPrimaryVolume", "false"));
    private static final String TAG = "HwEnvironment";
    private static UserEnvironmentSD sCurrentUserSd;

    public static class UserEnvironmentSD {
        private final int mUserId;

        public UserEnvironmentSD(int userId) {
            this.mUserId = userId;
        }

        public File[] getExternalDirs() {
            StorageVolume[] volumes = StorageManager.getVolumeList(this.mUserId, 256);
            File[] files = new File[volumes.length];
            for (int i = 0; i < volumes.length; i++) {
                files[i] = volumes[i].getPathFile();
            }
            return files;
        }

        @Deprecated
        public File getExternalStorageDirectory() {
            File[] files = getExternalDirs();
            if (files.length == 1) {
                return files[0];
            }
            return files[1];
        }

        @Deprecated
        public File getExternalStoragePublicDirectory(String type) {
            File[] files = buildExternalStoragePublicDirs(type);
            if (files.length == 1) {
                return files[0];
            }
            return files[1];
        }

        public File[] getExternalDirsForApp() {
            return getExternalDirs();
        }

        public File getMediaDir() {
            return null;
        }

        public File[] buildExternalStoragePublicDirs(String type) {
            if (type == null) {
                return null;
            }
            return Environment.buildPaths(getExternalDirs(), new String[]{type});
        }
    }

    public static void initUserEnvironmentSD(int userId) {
        if (IS_SWITCH_SD_ENABLED) {
            sCurrentUserSd = new UserEnvironmentSD(userId);
        }
    }

    public static File getMediaStorageDirectory() {
        return sCurrentUserSd.getMediaDir();
    }

    public static File getExternalStorageDirectory() {
        File[] files = sCurrentUserSd.getExternalDirsForApp();
        if (files.length == 1) {
            return files[0];
        }
        return files[1];
    }

    public static File getExternalStoragePublicDirectory(String type) {
        File[] files = sCurrentUserSd.buildExternalStoragePublicDirs(type);
        if (files.length == 1) {
            return files[0];
        }
        return files[1];
    }

    public static File getExternalStorageState() {
        File[] files = sCurrentUserSd.getExternalDirs();
        if (files.length == 1) {
            return files[0];
        }
        return files[1];
    }

    public static boolean checkPrimaryVolumeIsSD() {
        return IS_SWITCH_SD_ENABLED && 1 == SystemProperties.getInt("persist.sys.primarysd", 0);
    }
}
