package ohos.bundle;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;

public class ParseProfile {
    private static final String CONFIG_JSON = "config.json";
    private static final String HAP_SUFFIX = ".hap";

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0063, code lost:
        r7 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0065, code lost:
        r7 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0066, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:17:0x0043] */
    public static BundleInfo parse(Context context, String str, int i) {
        Throwable th;
        ZipFile zipFile;
        InputStream inputStream;
        BundleInfo bundleInfo;
        IOException e;
        BufferedReader bufferedReader;
        BufferedReader bufferedReader2 = null;
        if (str == null || str.isEmpty()) {
            AppLog.e("ParseProfile::parse hapFilePath is empty!", new Object[0]);
            return null;
        }
        try {
            zipFile = new ZipFile(new File(getFormattedPath(context, str)));
            try {
                ZipEntry entry = zipFile.getEntry(CONFIG_JSON);
                if (entry == null) {
                    AppLog.e("ParseProfile::parse config.json not found", new Object[0]);
                    closeStream(null);
                    closeStream(null);
                    closeStream(zipFile);
                    return null;
                }
                inputStream = zipFile.getInputStream(entry);
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                } catch (IOException e2) {
                    e = e2;
                    bundleInfo = null;
                    try {
                        AppLog.e("ParseProfile::parse io exception: %{public}s", e.getMessage());
                        closeStream(bufferedReader2);
                        closeStream(inputStream);
                        closeStream(zipFile);
                        return bundleInfo;
                    } catch (Throwable th2) {
                        th = th2;
                        closeStream(bufferedReader2);
                        closeStream(inputStream);
                        closeStream(zipFile);
                        throw th;
                    }
                }
                try {
                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        sb.append(readLine);
                    }
                    String str2 = new String(sb);
                    bundleInfo = new BundleInfo();
                    bundleInfo.parseBundle(str2, i);
                    closeStream(bufferedReader);
                } catch (IOException e3) {
                    e = e3;
                    bundleInfo = null;
                    bufferedReader2 = bufferedReader;
                    AppLog.e("ParseProfile::parse io exception: %{public}s", e.getMessage());
                    closeStream(bufferedReader2);
                    closeStream(inputStream);
                    closeStream(zipFile);
                    return bundleInfo;
                } catch (Throwable th3) {
                }
                closeStream(inputStream);
                closeStream(zipFile);
                return bundleInfo;
            } catch (IOException e4) {
                e = e4;
                inputStream = null;
                bundleInfo = null;
                AppLog.e("ParseProfile::parse io exception: %{public}s", e.getMessage());
                closeStream(bufferedReader2);
                closeStream(inputStream);
                closeStream(zipFile);
                return bundleInfo;
            } catch (Throwable th4) {
                th = th4;
                inputStream = null;
                closeStream(bufferedReader2);
                closeStream(inputStream);
                closeStream(zipFile);
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            inputStream = null;
            zipFile = null;
            bundleInfo = null;
            AppLog.e("ParseProfile::parse io exception: %{public}s", e.getMessage());
            closeStream(bufferedReader2);
            closeStream(inputStream);
            closeStream(zipFile);
            return bundleInfo;
        } catch (Throwable th5) {
            th = th5;
            inputStream = null;
            zipFile = null;
            closeStream(bufferedReader2);
            closeStream(inputStream);
            closeStream(zipFile);
            throw th;
        }
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                AppLog.e("ParseProfile::closeStream io close exception: %{public}s", e.getMessage());
            }
        }
    }

    private static String getFormattedPath(Context context, String str) {
        IOException e;
        String str2;
        if (context == null) {
            AppLog.e("ParseProfile::getFormattedPath context is null", new Object[0]);
            return "";
        } else if (str == null || str.isEmpty()) {
            AppLog.e("ParseProfile::getFormattedPath path is null or empty", new Object[0]);
            return "";
        } else {
            File file = new File(str);
            if (!isValidHapFile(file)) {
                AppLog.e("ParseProfile::getFormattedPath is not valid hap", new Object[0]);
                return "";
            }
            try {
                str2 = file.getCanonicalPath();
                try {
                    File dataDir = context.getDataDir();
                    if (dataDir == null) {
                        return "";
                    }
                    String canonicalPath = dataDir.getCanonicalPath();
                    AppLog.d("ParseProfile::getFormattedPath appPath = %{private}s", canonicalPath);
                    if (str2.startsWith(canonicalPath)) {
                        return str2;
                    }
                    return canonicalPath + str2;
                } catch (IOException e2) {
                    e = e2;
                    AppLog.e("ParseProfile::getFormattedPath exception: %{public}s", e.getMessage());
                    return str2;
                }
            } catch (IOException e3) {
                e = e3;
                str2 = "";
                AppLog.e("ParseProfile::getFormattedPath exception: %{public}s", e.getMessage());
                return str2;
            }
        }
    }

    private static boolean isValidHapFile(File file) {
        if (file.exists() && file.isFile() && file.canRead() && file.length() > 0 && file.getName().endsWith(HAP_SUFFIX)) {
            return true;
        }
        return false;
    }
}
