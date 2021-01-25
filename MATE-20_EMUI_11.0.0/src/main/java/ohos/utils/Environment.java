package ohos.utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Environment {
    public static final String DIRECTORY_ALARMS = "Alarms";
    public static final String DIRECTORY_AUDIOBOOKS = "Audiobooks";
    public static final String DIRECTORY_DCIM = "DCIM";
    public static final String DIRECTORY_DOCUMENTS = "Documents";
    public static final String DIRECTORY_DOWNLOAD = "Download";
    public static final String DIRECTORY_MOVIES = "Movies";
    public static final String DIRECTORY_MUSIC = "Music";
    public static final String DIRECTORY_NOTIFICATIONS = "Notifications";
    public static final String DIRECTORY_PICTURES = "Pictures";
    public static final String DIRECTORY_PODCASTS = "Podcasts";
    public static final String DIRECTORY_RINGTONES = "Ringtones";
    public static final String DIRECTORY_SCREENSHOTS = "Screenshots";
    private static final String ENV_DOWNLOAD_CACHE = "DOWNLOAD_CACHE";
    private static final String ENV_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    private static final String ENV_ODM_ROOT = "ODM_ROOT";
    private static final String ENV_OEM_ROOT = "OEM_ROOT";
    private static final String ENV_PRODUCT_ROOT = "PRODUCT_ROOT";
    private static final String ENV_PRODUCT_SERVICES_ROOT = "PRODUCT_SERVICES_ROOT";
    private static final String ENV_VENDOR_ROOT = "VENDOR_ROOT";
    private static final String ENV_ZIDANE_DATA = "ZIDANE_DATA";
    private static final String ENV_ZIDANE_EXPAND = "ZIDANE_EXPAND";
    private static final String ENV_ZIDANE_ROOT = "ZIDANE_ROOT";
    private static final String ENV_ZIDANE_STORAGE = "ZIDANE_STORAGE";
    private static final String MIC_DIR = "misc";
    public static final Set<String> STANDARD_DIRECTORIES = new HashSet();
    private static final Map<String, String> SYS_ENV_DEFAULT_DIR_MAP = new HashMap();
    private static final Map<String, File> SYS_ENV_DIR_MAP = new HashMap();

    static {
        STANDARD_DIRECTORIES.add(DIRECTORY_MUSIC);
        STANDARD_DIRECTORIES.add(DIRECTORY_PODCASTS);
        STANDARD_DIRECTORIES.add(DIRECTORY_RINGTONES);
        STANDARD_DIRECTORIES.add(DIRECTORY_ALARMS);
        STANDARD_DIRECTORIES.add(DIRECTORY_NOTIFICATIONS);
        STANDARD_DIRECTORIES.add(DIRECTORY_PICTURES);
        STANDARD_DIRECTORIES.add(DIRECTORY_MOVIES);
        STANDARD_DIRECTORIES.add(DIRECTORY_DOWNLOAD);
        STANDARD_DIRECTORIES.add(DIRECTORY_DCIM);
        STANDARD_DIRECTORIES.add(DIRECTORY_DOCUMENTS);
        STANDARD_DIRECTORIES.add(DIRECTORY_AUDIOBOOKS);
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_ZIDANE_ROOT, "/system");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_ZIDANE_DATA, "/data");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_ZIDANE_EXPAND, "/mnt/expand");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_ZIDANE_STORAGE, "/storage");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_DOWNLOAD_CACHE, "/cache");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_OEM_ROOT, "/oem");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_ODM_ROOT, "/odm");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_VENDOR_ROOT, "/vendor");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_PRODUCT_ROOT, "/product");
        SYS_ENV_DEFAULT_DIR_MAP.put(ENV_PRODUCT_SERVICES_ROOT, "/product_services");
        SYS_ENV_DIR_MAP.put(ENV_ZIDANE_ROOT, initDirectoryByEnv(ENV_ZIDANE_ROOT));
        SYS_ENV_DIR_MAP.put(ENV_ZIDANE_DATA, initDirectoryByEnv(ENV_ZIDANE_DATA));
        SYS_ENV_DIR_MAP.put(ENV_ZIDANE_EXPAND, initDirectoryByEnv(ENV_ZIDANE_EXPAND));
        SYS_ENV_DIR_MAP.put(ENV_ZIDANE_STORAGE, initDirectoryByEnv(ENV_ZIDANE_STORAGE));
        SYS_ENV_DIR_MAP.put(ENV_DOWNLOAD_CACHE, initDirectoryByEnv(ENV_DOWNLOAD_CACHE));
        SYS_ENV_DIR_MAP.put(ENV_OEM_ROOT, initDirectoryByEnv(ENV_OEM_ROOT));
        SYS_ENV_DIR_MAP.put(ENV_ODM_ROOT, initDirectoryByEnv(ENV_ODM_ROOT));
        SYS_ENV_DIR_MAP.put(ENV_VENDOR_ROOT, initDirectoryByEnv(ENV_VENDOR_ROOT));
        SYS_ENV_DIR_MAP.put(ENV_PRODUCT_ROOT, initDirectoryByEnv(ENV_PRODUCT_ROOT));
        SYS_ENV_DIR_MAP.put(ENV_PRODUCT_SERVICES_ROOT, initDirectoryByEnv(ENV_PRODUCT_SERVICES_ROOT));
    }

    private Environment() {
    }

    public static File getRootDirectory() {
        return getDirectoryByEnv(ENV_ZIDANE_ROOT);
    }

    public static File getDataDirectory() {
        return getDirectoryByEnv(ENV_ZIDANE_DATA);
    }

    public static File getExpandDirectory() {
        return getDirectoryByEnv(ENV_ZIDANE_EXPAND);
    }

    public static File getStorageDirectory() {
        return getDirectoryByEnv(ENV_ZIDANE_STORAGE);
    }

    public static File getDownloadCacheDirectory() {
        return getDirectoryByEnv(ENV_DOWNLOAD_CACHE);
    }

    public static File getOemDirectory() {
        return getDirectoryByEnv(ENV_OEM_ROOT);
    }

    public static File getOdmDirectory() {
        return getDirectoryByEnv(ENV_ODM_ROOT);
    }

    public static File getVendorDirectory() {
        return getDirectoryByEnv(ENV_VENDOR_ROOT);
    }

    public static File getProductDirectory() {
        return getDirectoryByEnv(ENV_PRODUCT_ROOT);
    }

    public static File getProductServicesDirectory() {
        return getDirectoryByEnv(ENV_PRODUCT_SERVICES_ROOT);
    }

    public static File getDataSystemDirectory() {
        return new File(getDirectoryByEnv(ENV_ZIDANE_DATA), "system");
    }

    public static File getDataSystemDeDirectory() {
        return new File(getDirectoryByEnv(ENV_ZIDANE_DATA), "system_de");
    }

    public static File getDataSystemDeDirectory(int i) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{"system_de", String.valueOf(i)});
    }

    public static File getDataSystemCeDirectory() {
        return new File(getDirectoryByEnv(ENV_ZIDANE_DATA), "system_ce");
    }

    public static File getDataSystemCeDirectory(int i) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{"system_ce", String.valueOf(i)});
    }

    public static File getDataMiscDirectory() {
        return new File(getDirectoryByEnv(ENV_ZIDANE_DATA), MIC_DIR);
    }

    public static File getDataMiscCeDirectory() {
        return new File(getDirectoryByEnv(ENV_ZIDANE_DATA), "misc_ce");
    }

    public static File getDataMiscCeDirectory(int i) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{"misc_ce", String.valueOf(i)});
    }

    public static File getDataMiscDeDirectory(int i) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{"misc_de", String.valueOf(i)});
    }

    public static File getDataVendorCeDirectory(int i) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{"vendor_ce", String.valueOf(i)});
    }

    public static File getDataVendorDeDirectory(int i) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{"vendor_de", String.valueOf(i)});
    }

    public static File getDataRefProfilesDePackageDirectory(String str) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{MIC_DIR, "profiles", "ref", str});
    }

    public static File getDataProfilesDePackageDirectory(int i, String str) {
        return buildPath(getDirectoryByEnv(ENV_ZIDANE_DATA), new String[]{MIC_DIR, "profiles", "cur", String.valueOf(i), str});
    }

    public static File getDataAppDirectory(String str) {
        return new File(getDataDirectoryByFixVu(str), "app");
    }

    public static File getDataStagingDirectory(String str) {
        return new File(getDataDirectoryByFixVu(str), "app-staging");
    }

    public static File getDataUserCeDirectory(String str) {
        return new File(getDataDirectoryByFixVu(str), "user");
    }

    public static File getDataUserCeDirectory(String str, int i) {
        return new File(getDataUserCeDirectory(str), String.valueOf(i));
    }

    public static File getDataUserCePackageDirectory(String str, int i, String str2) {
        return new File(getDataUserCeDirectory(str, i), str2);
    }

    public static File getDataUserDeDirectory(String str) {
        return new File(getDataDirectoryByFixVu(str), "user_de");
    }

    public static File getDataUserDeDirectory(String str, int i) {
        return new File(getDataUserDeDirectory(str), String.valueOf(i));
    }

    public static File getDataUserDePackageDirectory(String str, int i, String str2) {
        return new File(getDataUserDeDirectory(str, i), str2);
    }

    private static File getDataDirectoryByFixVu(String str) {
        if (str == null || str.length() == 0) {
            return getDirectoryByEnv(ENV_ZIDANE_DATA);
        }
        return new File("/mnt/expand/" + str);
    }

    public static File getDataPreloadsDirectory() {
        return new File(getDirectoryByEnv(ENV_ZIDANE_DATA), "preloads");
    }

    public static File getDataPreloadsDemoDirectory() {
        return new File(getDataPreloadsDirectory(), "demo");
    }

    public static File getDataPreloadsAppsDirectory() {
        return new File(getDataPreloadsDirectory(), "apps");
    }

    public static File getDataPreloadsMediaDirectory() {
        return new File(getDataPreloadsDirectory(), "media");
    }

    public static File getPackageCacheDirectory() {
        return new File(getDataSystemDirectory(), "package_cache");
    }

    public static File getDataPreloadsFileCacheDirectory() {
        return new File(getDataPreloadsDirectory(), "file_cache");
    }

    public static File getDataPreloadsFileCacheDirectory(String str) {
        return new File(getDataPreloadsFileCacheDirectory(), str);
    }

    public static File getLegacyExternalStorageDirectory() {
        String str = System.getenv(ENV_EXTERNAL_STORAGE);
        return str == null ? new File("") : new File(str);
    }

    private static File getDirectoryByEnv(String str) {
        return SYS_ENV_DIR_MAP.get(str);
    }

    private static File initDirectoryByEnv(String str) {
        String str2 = System.getenv(str);
        return str2 == null ? new File(SYS_ENV_DEFAULT_DIR_MAP.get(str)) : new File(str2);
    }

    public static boolean isStandardDirectoryName(String str) {
        return STANDARD_DIRECTORIES.contains(str);
    }

    private static File buildPath(File file, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        if (file != null) {
            sb.append(file.getPath());
            sb.append(File.separator);
        }
        for (String str : strArr) {
            sb.append(str);
            sb.append(File.separator);
        }
        return new File(sb.toString());
    }
}
