package tmsdk.common.utils;

import java.util.Properties;

/* compiled from: Unknown */
public class i {
    private static Properties Lf;

    public static synchronized String dh(String str) {
        String property;
        synchronized (i.class) {
            if (Lf == null) {
                Lf = new Properties();
                Lf.setProperty("cn_scan_item_empty_folders", "\u7a7a\u6587\u4ef6\u5939");
                Lf.setProperty("cn_broken_apk", "\u7834\u635f\u5b89\u88c5\u5305");
                Lf.setProperty("cn_deep_clean_other_rubbish", "\u5176\u4ed6\u5783\u573e");
                Lf.setProperty("cn_deep_clean_initializing", "\u521d\u59cb\u5316\u4e2d");
                Lf.setProperty("cn_scan_item_sys_camera_cache", "\u7cfb\u7edf\u76f8\u673a\u7f13\u5b58");
                Lf.setProperty("cn_scan_item_temp_files", "\u4e34\u65f6\u6587\u4ef6");
                Lf.setProperty("cn_scan_item_temp_piture", "\u7cfb\u7edf\u76f8\u518c\u7f29\u7565\u56fe");
                Lf.setProperty("cn_in_recent_days", "\u8fd1 %d\u5929");
                Lf.setProperty("cn_days_ago", "%d\u5929\u524d");
                Lf.setProperty("cn_apk_old_version", "\u65e7\u7248");
                Lf.setProperty("cn_apk_installed", "\u5df2\u5b89\u88c5");
                Lf.setProperty("cn_apk_repeated", "\u91cd\u590d");
                Lf.setProperty("cn_apk_new_version", "\u65b0\u7248");
                Lf.setProperty("cn_apk_not_installed", "\u672a\u5b89\u88c5");
                Lf.setProperty("eng_scan_item_empty_folders", "Empty Folder");
                Lf.setProperty("eng_broken_apk", "Broken File");
                Lf.setProperty("eng_deep_clean_other_rubbish", "Other Rubbish");
                Lf.setProperty("eng_deep_clean_initializing", "Initializing");
                Lf.setProperty("eng_scan_item_sys_camera_cache", "Camera Cache");
                Lf.setProperty("eng_scan_item_temp_files", "Temp Files");
                Lf.setProperty("eng_scan_item_temp_piture", "Temp Picture");
                Lf.setProperty("eng_in_recent_days", "in recent %d days");
                Lf.setProperty("eng_days_ago", "%d days ago");
                Lf.setProperty("eng_apk_old_version", "Old version");
                Lf.setProperty("eng_apk_installed", "Installed");
                Lf.setProperty("eng_apk_repeated", "Repeated");
                Lf.setProperty("eng_apk_new_version", "New version");
                Lf.setProperty("eng_apk_not_installed", "Not installed");
            }
            property = Lf.getProperty(str);
        }
        return property;
    }
}
