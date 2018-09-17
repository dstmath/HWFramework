package android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HuaweiProperties {
    private static String TAG = "HuaweiProperties";
    public static String VENDOR_HUAWEI_CONFIG_PATH = "/vendor/etc/framework_res_configs.xml";
    private static ArrayMap<String, String> mVendorPropertiesArrayMap = new ArrayMap();

    static {
        loadAllProperties(VENDOR_HUAWEI_CONFIG_PATH);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0078 A:{SYNTHETIC, Splitter: B:25:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009a A:{SYNTHETIC, Splitter: B:31:0x009a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void loadAllProperties(String filePath) {
        IOException e;
        Throwable th;
        Iterable propertiesString = null;
        if (new File(filePath).exists()) {
            Properties prop = new Properties();
            FileInputStream str = null;
            try {
                FileInputStream str2 = new FileInputStream(filePath);
                try {
                    prop.loadFromXML(str2);
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e2) {
                            Slog.w(TAG, "get Hw property execption : " + e2);
                        }
                    }
                    str = str2;
                } catch (IOException e3) {
                    e2 = e3;
                    str = str2;
                    try {
                        Slog.w(TAG, "close file execption : " + e2);
                        if (str != null) {
                        }
                        propertiesString = prop.stringPropertyNames();
                        if (propertiesString != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (str != null) {
                            try {
                                str.close();
                            } catch (IOException e22) {
                                Slog.w(TAG, "get Hw property execption : " + e22);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    str = str2;
                    if (str != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e22 = e4;
                Slog.w(TAG, "close file execption : " + e22);
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e222) {
                        Slog.w(TAG, "get Hw property execption : " + e222);
                    }
                }
                propertiesString = prop.stringPropertyNames();
                if (propertiesString != null) {
                }
            }
            try {
                propertiesString = prop.stringPropertyNames();
            } catch (NumberFormatException e5) {
                Slog.w(TAG, "get Hw property execption : " + e5);
            }
            if (propertiesString != null) {
                for (String key : propertiesString) {
                    mVendorPropertiesArrayMap.put(key, prop.getProperty(key));
                }
            }
        }
    }

    public static String getProperties(String filePath, String searchProperty) {
        if (new File(filePath).exists()) {
            return (String) mVendorPropertiesArrayMap.get(searchProperty);
        }
        return null;
    }
}
