package ohos.appexecfwk.utils;

import java.io.File;

public class FileUtils {
    private static final int DEDALUT_NUM = 0;
    private static final int MAX_FILE_NAME_LENGTH = 255;

    public static String getExpectedPath(String... strArr) {
        if (strArr.length == 0) {
            AppLog.e("FileUtils::getExpectedPath failed, args is null", new Object[0]);
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strArr) {
            if (!StringUtils.isBlank(str)) {
                sb.append(str.trim());
            }
        }
        return sb.toString();
    }

    public static boolean createDirectory(String str) {
        if (!StringUtils.isBlank(str)) {
            return createDirectory(new File(str.trim()));
        }
        AppLog.e("FileUtils::createFolder failed, the directory is illegal", new Object[0]);
        return false;
    }

    public static boolean createDirectory(File file) {
        if (file == null) {
            AppLog.e("FileUtils::createFolder directory is null", new Object[0]);
            return false;
        } else if (file.exists()) {
            AppLog.i("FileUtils::createFolder directory exists", new Object[0]);
            return true;
        } else {
            createDirectory(file.getParentFile());
            try {
                return file.mkdir();
            } catch (SecurityException e) {
                AppLog.e("FileUtils::createFolder failed, mkdir SecurityException %{public}s", e.getMessage());
                return false;
            }
        }
    }

    public static boolean deleteDirectory(String str) {
        if (!StringUtils.isBlank(str)) {
            return deleteDirectory(new File(str.trim()));
        }
        AppLog.e("FileUtils::deleteDirectory failed, the dirPath is illegal", new Object[0]);
        return false;
    }

    public static boolean deleteDirectory(File file) {
        if (file == null) {
            AppLog.e("FileUtils::deleteDirectory failed, the directory is null", new Object[0]);
            return false;
        } else if (!file.exists()) {
            AppLog.e("FileUtils::deleteDirectory failed, the directory does not exist", new Object[0]);
            return false;
        } else if (!file.isDirectory()) {
            AppLog.e("FileUtils::deleteDirectory failed, the dirPath is not directory", new Object[0]);
            return false;
        } else {
            String[] list = file.list();
            int length = list != null ? list.length : 0;
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    if (!deleteDirectory(new File(file, list[i]))) {
                        AppLog.e("FileUtils::deleteDirectory failed, delete file failed", new Object[0]);
                        return false;
                    }
                }
            }
            try {
                return file.delete();
            } catch (SecurityException e) {
                AppLog.e("FileUtils::deleteDirectory failed, delete directory SecurityException %{public}s", e.getMessage());
                return false;
            }
        }
    }

    public static boolean isLegalFileName(String str) {
        if (StringUtils.isBlank(str)) {
            AppLog.e("FileUtils::isLegalFileName the file name is empty", new Object[0]);
            return false;
        }
        String trim = str.trim();
        if (!trim.isEmpty() && trim.length() <= 255) {
            return trim.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|]*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
        }
        AppLog.e("FileUtils::isLegalFileName the file name is too long", new Object[0]);
        return false;
    }
}
