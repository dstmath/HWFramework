package tmsdk.common.utils;

import java.util.Properties;

public class m {
    private static Properties LO = new Properties();
    private static boolean LP;

    public static synchronized void T(boolean z) {
        synchronized (m.class) {
            LP = z;
            if (LP) {
                LO.setProperty("scan_item_empty_folders", "Empty Folder");
                LO.setProperty("broken_apk", "Broken File");
                LO.setProperty("deep_clean_other_rubbish", "Other Rubbish");
                LO.setProperty("deep_clean_initializing", "Initializing");
                LO.setProperty("scan_item_sys_camera_cache", "Camera Cache");
                LO.setProperty("scan_item_temp_files", "Temp Files");
                LO.setProperty("scan_item_temp_piture", "Temp Picture");
                LO.setProperty("in_recent_days", "in recent %d days");
                LO.setProperty("days_ago", "%d days ago");
                LO.setProperty("apk_old_version", "Old version");
                LO.setProperty("apk_installed", "Installed");
                LO.setProperty("apk_repeated", "Repeated");
                LO.setProperty("apk_new_version", "New version");
                LO.setProperty("apk_not_installed", "Not installed");
            } else {
                LO.setProperty("scan_item_empty_folders", "空文件夹");
                LO.setProperty("broken_apk", "破损安装包");
                LO.setProperty("deep_clean_other_rubbish", "其他垃圾");
                LO.setProperty("deep_clean_initializing", "初始化中");
                LO.setProperty("scan_item_sys_camera_cache", "系统相机缓存");
                LO.setProperty("scan_item_temp_files", "临时文件");
                LO.setProperty("scan_item_temp_piture", "系统相册缩略图");
                LO.setProperty("in_recent_days", "近 %d天");
                LO.setProperty("days_ago", "%d天前");
                LO.setProperty("apk_old_version", "旧版");
                LO.setProperty("apk_installed", "已安装");
                LO.setProperty("apk_repeated", "重复");
                LO.setProperty("apk_new_version", "新版");
                LO.setProperty("apk_not_installed", "未安装");
            }
        }
    }

    public static synchronized String cF(String str) {
        String property;
        synchronized (m.class) {
            property = LO.getProperty(str);
        }
        return property;
    }

    public static boolean iW() {
        return LP;
    }
}
