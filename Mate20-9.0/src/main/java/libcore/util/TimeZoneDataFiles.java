package libcore.util;

public final class TimeZoneDataFiles {
    private static final String ANDROID_DATA_ENV = "ANDROID_DATA";
    private static final String ANDROID_ROOT_ENV = "ANDROID_ROOT";

    private TimeZoneDataFiles() {
    }

    public static String[] getTimeZoneFilePaths(String fileName) {
        return new String[]{getDataTimeZoneFile(fileName), getSystemTimeZoneFile(fileName)};
    }

    private static String getDataTimeZoneFile(String fileName) {
        return System.getenv(ANDROID_DATA_ENV) + "/misc/zoneinfo/current/" + fileName;
    }

    public static String getSystemTimeZoneFile(String fileName) {
        return System.getenv(ANDROID_ROOT_ENV) + "/usr/share/zoneinfo/" + fileName;
    }

    public static String generateIcuDataPath() {
        StringBuilder icuDataPathBuilder = new StringBuilder();
        String dataIcuDataPath = getEnvironmentPath(ANDROID_DATA_ENV, "/misc/zoneinfo/current/icu");
        if (dataIcuDataPath != null) {
            icuDataPathBuilder.append(dataIcuDataPath);
        }
        String systemIcuDataPath = getEnvironmentPath(ANDROID_ROOT_ENV, "/usr/icu");
        if (systemIcuDataPath != null) {
            if (icuDataPathBuilder.length() > 0) {
                icuDataPathBuilder.append(":");
            }
            icuDataPathBuilder.append(systemIcuDataPath);
        }
        return icuDataPathBuilder.toString();
    }

    private static String getEnvironmentPath(String environmentVariable, String path) {
        String variable = System.getenv(environmentVariable);
        if (variable == null) {
            return null;
        }
        return variable + path;
    }
}
