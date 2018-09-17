package android.os;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;

public class Environment {
    public static String DIRECTORY_ALARMS = "Alarms";
    @Deprecated
    public static final String DIRECTORY_ANDROID = "Android";
    public static String DIRECTORY_DCIM = "DCIM";
    public static String DIRECTORY_DOCUMENTS = "Documents";
    public static String DIRECTORY_DOWNLOADS = "Download";
    public static String DIRECTORY_MOVIES = "Movies";
    public static String DIRECTORY_MUSIC = "Music";
    public static String DIRECTORY_NOTIFICATIONS = "Notifications";
    public static String DIRECTORY_PICTURES = "Pictures";
    public static String DIRECTORY_PODCASTS = "Podcasts";
    public static String DIRECTORY_RINGTONES = "Ringtones";
    public static final String DIR_ANDROID = "Android";
    private static final File DIR_ANDROID_DATA = getDirectory(ENV_ANDROID_DATA, "/data");
    private static final File DIR_ANDROID_EXPAND = getDirectory(ENV_ANDROID_EXPAND, "/mnt/expand");
    private static final File DIR_ANDROID_ROOT = getDirectory(ENV_ANDROID_ROOT, "/system");
    private static final File DIR_ANDROID_STORAGE = getDirectory(ENV_ANDROID_STORAGE, "/storage");
    private static final String DIR_CACHE = "cache";
    private static final String DIR_DATA = "data";
    private static final File DIR_DOWNLOAD_CACHE = getDirectory(ENV_DOWNLOAD_CACHE, "/cache");
    private static final String DIR_FILES = "files";
    private static final String DIR_MEDIA = "media";
    private static final String DIR_OBB = "obb";
    private static final File DIR_ODM_ROOT = getDirectory(ENV_ODM_ROOT, "/odm");
    private static final File DIR_OEM_ROOT = getDirectory(ENV_OEM_ROOT, "/oem");
    private static final File DIR_VENDOR_ROOT = getDirectory(ENV_VENDOR_ROOT, "/vendor");
    private static final String ENV_ANDROID_DATA = "ANDROID_DATA";
    private static final String ENV_ANDROID_EXPAND = "ANDROID_EXPAND";
    private static final String ENV_ANDROID_ROOT = "ANDROID_ROOT";
    private static final String ENV_ANDROID_STORAGE = "ANDROID_STORAGE";
    private static final String ENV_DOWNLOAD_CACHE = "DOWNLOAD_CACHE";
    private static final String ENV_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    private static final String ENV_ODM_ROOT = "ODM_ROOT";
    private static final String ENV_OEM_ROOT = "OEM_ROOT";
    private static final String ENV_VENDOR_ROOT = "VENDOR_ROOT";
    public static final String EXTRA_VOLUME_NEW_STATE = "android.os.storage.extra.VOLUME_NEW_STATE";
    public static final String EXTRA_VOLUME_OLD_STATE = "android.os.storage.extra.VOLUME_OLD_STATE";
    public static final String MEDIA_BAD_REMOVAL = "bad_removal";
    public static final String MEDIA_CHECKING = "checking";
    public static final String MEDIA_EJECTING = "ejecting";
    public static final String MEDIA_MOUNTED = "mounted";
    public static final String MEDIA_MOUNTED_READ_ONLY = "mounted_ro";
    public static final String MEDIA_NOFS = "nofs";
    public static final String MEDIA_REMOVED = "removed";
    public static final String MEDIA_SHARED = "shared";
    public static final String MEDIA_UNKNOWN = "unknown";
    public static final String MEDIA_UNMOUNTABLE = "unmountable";
    public static final String MEDIA_UNMOUNTED = "unmounted";
    public static final String[] STANDARD_DIRECTORIES = new String[]{DIRECTORY_MUSIC, DIRECTORY_PODCASTS, DIRECTORY_RINGTONES, DIRECTORY_ALARMS, DIRECTORY_NOTIFICATIONS, DIRECTORY_PICTURES, DIRECTORY_MOVIES, DIRECTORY_DOWNLOADS, DIRECTORY_DCIM, DIRECTORY_DOCUMENTS};
    private static final String TAG = "Environment";
    private static UserEnvironment sCurrentUser;
    private static boolean sUserRequired;

    public static class UserEnvironment {
        private final int mUserId;

        public UserEnvironment(int userId) {
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
            return getExternalDirs()[0];
        }

        @Deprecated
        public File getExternalStoragePublicDirectory(String type) {
            return buildExternalStoragePublicDirs(type)[0];
        }

        public File[] buildExternalStoragePublicDirs(String type) {
            return Environment.buildPaths(getExternalDirs(), type);
        }

        public File[] buildExternalStorageAndroidDataDirs() {
            return Environment.buildPaths(getExternalDirs(), "Android", "data");
        }

        public File[] buildExternalStorageAndroidObbDirs() {
            return Environment.buildPaths(getExternalDirs(), "Android", "obb");
        }

        public File[] buildExternalStorageAppDataDirs(String packageName) {
            return Environment.buildPaths(getExternalDirs(), "Android", "data", packageName);
        }

        public File[] buildExternalStorageAppMediaDirs(String packageName) {
            return Environment.buildPaths(getExternalDirs(), "Android", Environment.DIR_MEDIA, packageName);
        }

        public File[] buildExternalStorageAppObbDirs(String packageName) {
            return Environment.buildPaths(getExternalDirs(), "Android", "obb", packageName);
        }

        public File[] buildExternalStorageAppFilesDirs(String packageName) {
            return Environment.buildPaths(getExternalDirs(), "Android", "data", packageName, Environment.DIR_FILES);
        }

        public File[] buildExternalStorageAppCacheDirs(String packageName) {
            return Environment.buildPaths(getExternalDirs(), "Android", "data", packageName, Environment.DIR_CACHE);
        }
    }

    static {
        initForCurrentUser();
    }

    public static void initForCurrentUser() {
        int userId = UserHandle.myUserId();
        sCurrentUser = new UserEnvironment(userId);
        HwFrameworkFactory.getHwSettingsManager().initUserEnvironmentSD(userId);
    }

    public static File getRootDirectory() {
        return DIR_ANDROID_ROOT;
    }

    public static File getStorageDirectory() {
        return DIR_ANDROID_STORAGE;
    }

    public static File getOemDirectory() {
        return DIR_OEM_ROOT;
    }

    public static File getOdmDirectory() {
        return DIR_ODM_ROOT;
    }

    public static File getVendorDirectory() {
        return DIR_VENDOR_ROOT;
    }

    @Deprecated
    public static File getUserSystemDirectory(int userId) {
        return new File(new File(getDataSystemDirectory(), "users"), Integer.toString(userId));
    }

    @Deprecated
    public static File getUserConfigDirectory(int userId) {
        return new File(new File(new File(getDataDirectory(), "misc"), Context.USER_SERVICE), Integer.toString(userId));
    }

    public static File getDataDirectory() {
        return DIR_ANDROID_DATA;
    }

    public static File getDataDirectory(String volumeUuid) {
        if (TextUtils.isEmpty(volumeUuid)) {
            return DIR_ANDROID_DATA;
        }
        return new File("/mnt/expand/" + volumeUuid);
    }

    public static File getExpandDirectory() {
        return DIR_ANDROID_EXPAND;
    }

    public static File getDataSystemDirectory() {
        return new File(getDataDirectory(), "system");
    }

    public static File getDataSystemDeDirectory() {
        return buildPath(getDataDirectory(), "system_de");
    }

    public static File getDataSystemCeDirectory() {
        return buildPath(getDataDirectory(), "system_ce");
    }

    public static File getDataSystemCeDirectory(int userId) {
        return buildPath(getDataDirectory(), "system_ce", String.valueOf(userId));
    }

    public static File getDataSystemDeDirectory(int userId) {
        return buildPath(getDataDirectory(), "system_de", String.valueOf(userId));
    }

    public static File getDataMiscDirectory() {
        return new File(getDataDirectory(), "misc");
    }

    public static File getDataMiscCeDirectory() {
        return buildPath(getDataDirectory(), "misc_ce");
    }

    public static File getDataMiscCeDirectory(int userId) {
        return buildPath(getDataDirectory(), "misc_ce", String.valueOf(userId));
    }

    public static File getDataMiscDeDirectory(int userId) {
        return buildPath(getDataDirectory(), "misc_de", String.valueOf(userId));
    }

    private static File getDataProfilesDeDirectory(int userId) {
        return buildPath(getDataDirectory(), "misc", "profiles", "cur", String.valueOf(userId));
    }

    public static File getReferenceProfile(String packageName) {
        return buildPath(getDataDirectory(), "misc", "profiles", "ref", packageName);
    }

    public static File getDataProfilesDePackageDirectory(int userId, String packageName) {
        return buildPath(getDataProfilesDeDirectory(userId), packageName);
    }

    public static File getDataAppDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), "app");
    }

    public static File getDataUserCeDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), Context.USER_SERVICE);
    }

    public static File getDataUserCeDirectory(String volumeUuid, int userId) {
        return new File(getDataUserCeDirectory(volumeUuid), String.valueOf(userId));
    }

    public static File getDataUserCePackageDirectory(String volumeUuid, int userId, String packageName) {
        return new File(getDataUserCeDirectory(volumeUuid, userId), packageName);
    }

    public static File getDataUserDeDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), "user_de");
    }

    public static File getDataUserDeDirectory(String volumeUuid, int userId) {
        return new File(getDataUserDeDirectory(volumeUuid), String.valueOf(userId));
    }

    public static File getDataUserDePackageDirectory(String volumeUuid, int userId, String packageName) {
        return new File(getDataUserDeDirectory(volumeUuid, userId), packageName);
    }

    public static File getDataPreloadsDirectory() {
        return new File(getDataDirectory(), "preloads");
    }

    public static File getDataPreloadsDemoDirectory() {
        return new File(getDataPreloadsDirectory(), "demo");
    }

    public static File getDataPreloadsAppsDirectory() {
        return new File(getDataPreloadsDirectory(), "apps");
    }

    public static File getDataPreloadsMediaDirectory() {
        return new File(getDataPreloadsDirectory(), DIR_MEDIA);
    }

    public static File getDataPreloadsFileCacheDirectory(String packageName) {
        return new File(getDataPreloadsFileCacheDirectory(), packageName);
    }

    public static File getDataPreloadsFileCacheDirectory() {
        return new File(getDataPreloadsDirectory(), "file_cache");
    }

    public static File getExternalStorageDirectory() {
        throwIfUserRequired();
        if (!HwFrameworkFactory.getHwSettingsManager().checkPrimaryVolumeIsSD() || sCurrentUser.getExternalDirs().length <= 1) {
            return sCurrentUser.getExternalDirs()[0];
        }
        return HwFrameworkFactory.getHwSettingsManager().getExternalStorageDirectory();
    }

    public static File getLegacyExternalStorageDirectory() {
        return new File(System.getenv(ENV_EXTERNAL_STORAGE));
    }

    public static File getLegacyExternalStorageObbDirectory() {
        return buildPath(getLegacyExternalStorageDirectory(), "Android", "obb");
    }

    public static boolean isStandardDirectory(String dir) {
        for (String valid : STANDARD_DIRECTORIES) {
            if (valid.equals(dir)) {
                return true;
            }
        }
        return false;
    }

    public static File getExternalStoragePublicDirectory(String type) {
        throwIfUserRequired();
        if (!HwFrameworkFactory.getHwSettingsManager().checkPrimaryVolumeIsSD() || sCurrentUser.buildExternalStoragePublicDirs(type).length <= 1) {
            return sCurrentUser.buildExternalStoragePublicDirs(type)[0];
        }
        return HwFrameworkFactory.getHwSettingsManager().getExternalStoragePublicDirectory(type);
    }

    public static File[] buildExternalStorageAndroidDataDirs() {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAndroidDataDirs();
    }

    public static File[] buildExternalStorageAppDataDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppDataDirs(packageName);
    }

    public static File[] buildExternalStorageAppMediaDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppMediaDirs(packageName);
    }

    public static File[] buildExternalStorageAppObbDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppObbDirs(packageName);
    }

    public static File[] buildExternalStorageAppFilesDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppFilesDirs(packageName);
    }

    public static File[] buildExternalStorageAppCacheDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppCacheDirs(packageName);
    }

    public static File getDownloadCacheDirectory() {
        return DIR_DOWNLOAD_CACHE;
    }

    public static String getExternalStorageState() {
        File externalDir = null;
        File[] dirBuffer = sCurrentUser.getExternalDirs();
        if (dirBuffer.length > 0) {
            externalDir = dirBuffer[0];
        }
        if (HwFrameworkFactory.getHwSettingsManager().checkPrimaryVolumeIsSD() && sCurrentUser.getExternalDirs().length > 1) {
            externalDir = HwFrameworkFactory.getHwSettingsManager().getExternalStorageState();
        }
        return getExternalStorageState(externalDir);
    }

    @Deprecated
    public static String getStorageState(File path) {
        return getExternalStorageState(path);
    }

    public static String getExternalStorageState(File path) {
        StorageVolume volume = StorageManager.getStorageVolume(path, UserHandle.myUserId());
        if (volume != null) {
            return volume.getState();
        }
        return "unknown";
    }

    public static boolean isExternalStorageRemovable() {
        if (isStorageDisabled()) {
            return false;
        }
        return isExternalStorageRemovable(sCurrentUser.getExternalDirs()[0]);
    }

    public static boolean isExternalStorageRemovable(File path) {
        StorageVolume volume = StorageManager.getStorageVolume(path, UserHandle.myUserId());
        if (volume != null) {
            return volume.isRemovable();
        }
        throw new IllegalArgumentException("Failed to find storage device at " + path);
    }

    public static boolean isExternalStorageEmulated() {
        if (isStorageDisabled()) {
            return false;
        }
        return isExternalStorageEmulated(sCurrentUser.getExternalDirs()[0]);
    }

    public static boolean isExternalStorageEmulated(File path) {
        StorageVolume volume = StorageManager.getStorageVolume(path, UserHandle.myUserId());
        if (volume != null) {
            return volume.isEmulated();
        }
        throw new IllegalArgumentException("Failed to find storage device at " + path);
    }

    static File getDirectory(String variableName, String defaultPath) {
        String path = System.getenv(variableName);
        return path == null ? new File(defaultPath) : new File(path);
    }

    public static void setUserRequired(boolean userRequired) {
        sUserRequired = userRequired;
    }

    private static void throwIfUserRequired() {
        if (sUserRequired) {
            Log.wtf(TAG, "Path requests must specify a user by using UserEnvironment", new Throwable());
        }
    }

    public static File[] buildPaths(File[] base, String... segments) {
        File[] result = new File[base.length];
        for (int i = 0; i < base.length; i++) {
            result[i] = buildPath(base[i], segments);
        }
        return result;
    }

    public static File buildPath(File base, String... segments) {
        File cur = base;
        int i = 0;
        int length = segments.length;
        File cur2 = cur;
        while (i < length) {
            String segment = segments[i];
            if (cur2 == null) {
                cur = new File(segment);
            } else {
                cur = new File(cur2, segment);
            }
            i++;
            cur2 = cur;
        }
        return cur2;
    }

    private static boolean isStorageDisabled() {
        return SystemProperties.getBoolean("config.disable_storage", false);
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        return StorageManager.maybeTranslateEmulatedPathToInternal(path);
    }

    public static String getInternalStoragePath() {
        return sCurrentUser.getExternalStorageDirectory().toString();
    }

    public static String getEnvExternalStorage() {
        return ENV_EXTERNAL_STORAGE;
    }

    public static File[] getExternalDirsForApp() {
        return sCurrentUser.getExternalDirs();
    }
}
