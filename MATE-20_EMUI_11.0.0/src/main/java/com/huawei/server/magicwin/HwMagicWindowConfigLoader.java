package com.huawei.server.magicwin;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.HwMwUtils;
import android.util.Log;
import android.util.Xml;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.internal.util.FastXmlSerializerEx;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.utils.Utils;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class HwMagicWindowConfigLoader {
    public static final String CLOUD_CONFIG_FILE_PATH = "/data/system/";
    public static final String CLOUD_CONFIG_FILE_PREFIX = "magicWindowFeature_";
    public static final String CLOUD_MULTISCREEN_PROJECTION_LIMIT_FILE_NAME = "magicWindowFeature_multiscreen_projection_limit.xml";
    public static final String CLOUD_PACKAGE_CONFIG_FILE_NAME = "magicWindowFeature_magic_window_application_list.xml";
    public static final String HOME_CONFIG_FILE_NAME = "magic_window_homepage_list.xml";
    public static final String LAST_VERSION = "lastVersion";
    public static final String LOCAL_PART_CONFIG_FILE_PATH = "/xml/";
    public static final String MULTISCREEN_PROJECTION_LIMIT_FILE_NAME = "multiscreen_projection_limit.xml";
    public static final String PACKAGE_CONFIG_FILE_NAME = "magic_window_application_list.xml";
    private static final String SETTING_CONFIG_BACKUP_FILE_NAME = "magic_window_setting_config-backup.xml";
    private static final String SETTING_CONFIG_FILE_NAME = "magic_window_setting_config.xml";
    public static final String SYSTEM_CONFIG_FILE_NAME = "magic_window_system_config.xml";
    private static final String TAG = "HWMW_HwMagicWindowConfigLoader";
    private static final String XML_ATTRIBUTE_BACKGROUND = "support_background";
    private static final String XML_ATTRIBUTE_BACK_MIDDLE = "is_back_to_middle";
    private static final String XML_ATTRIBUTE_BOTTOM_PADDING = "bottom_padding";
    private static final String XML_ATTRIBUTE_CAPABILITY = "multi_dev_capability";
    private static final String XML_ATTRIBUTE_CMAERA_PREVIEW = "support_camera_preview";
    private static final String XML_ATTRIBUTE_CORNER_RADIUS = "corner_radius";
    private static final String XML_ATTRIBUTE_DEFAULT_SETTING = "default_setting";
    private static final String XML_ATTRIBUTE_DRAG_FS = "drag_to_fullscreen";
    private static final String XML_ATTRIBUTE_DRAG_TO_FULLSCREEN = "enable_drag_to_fullscreen";
    private static final String XML_ATTRIBUTE_DYNAMIC_EFFECT = "support_dynamic_effect";
    private static final String XML_ATTRIBUTE_FULLSCREEN_VIDEO = "support_fullscreen_video";
    private static final String XML_ATTRIBUTE_HOME = "home";
    private static final String XML_ATTRIBUTE_HOST_VIEW_THRESHOLD = "host_view_threshold";
    private static final String XML_ATTRIBUTE_IS_DRAGABLE = "is_dragable";
    private static final String XML_ATTRIBUTE_LEFT_PADDING = "left_padding";
    private static final String XML_ATTRIBUTE_LEFT_RESUME = "support_multi_resume";
    private static final String XML_ATTRIBUTE_MID_DRAG_PADDING = "mid_drag_padding";
    private static final String XML_ATTRIBUTE_MID_PADDING = "mid_padding";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_ATTRIBUTE_NEED_RELAUNCH = "need_relaunch";
    private static final String XML_ATTRIBUTE_NOTCH_ADAPT = "notch_adapt";
    private static final String XML_ATTRIBUTE_OPEN_CAPABILITY = "support_open_capability";
    private static final String XML_ATTRIBUTE_RIGHT_PADDING = "right_padding";
    private static final String XML_ATTRIBUTE_ROUND_ANGLE = "support_round_angle";
    private static final String XML_ATTRIBUTE_SCALE_ENABLED = "is_scaled";
    private static final String XML_ATTRIBUTE_SETTING_CONDIG = "setting_config";
    private static final String XML_ATTRIBUTE_SETTING_DRAG_MODE = "hwDragMode";
    private static final String XML_ATTRIBUTE_SETTING_ENABLED = "hwMagicWinEnabled";
    private static final String XML_ATTRIBUTE_SETTING_SHOWN = "hwDialogShown";
    private static final String XML_ATTRIBUTE_TOP_PADDING = "top_padding";
    private static final String XML_ATTRIBUTE_WINDOW_MODE = "window_mode";
    private static final String XML_ELEMENT_PACKAGE = "package";
    private static final String XML_ELEMENT_SETTING = "setting";
    private static final String XML_ELEMENT_SYSTEM = "system";
    private static File sBackupSettingFilename;
    private static File sSettingFilename;
    private static File sSystemDir;
    private DeviceAttribute mAttribute;
    private Context mContext;

    public HwMagicWindowConfigLoader(Context cxt, int userId, DeviceAttribute attribute) {
        this.mContext = cxt;
        this.mAttribute = attribute;
        String currentVersion = SystemPropertiesEx.get("ro.build.version.incremental", "");
        if (!currentVersion.equals(Settings.Global.getString(this.mContext.getContentResolver(), LAST_VERSION))) {
            SlogEx.i(TAG, "This is a new version");
            Settings.Global.putString(this.mContext.getContentResolver(), LAST_VERSION, currentVersion);
            clearDownloadConfig(CLOUD_CONFIG_FILE_PATH, CLOUD_PACKAGE_CONFIG_FILE_NAME);
            clearDownloadConfig(CLOUD_CONFIG_FILE_PATH, CLOUD_MULTISCREEN_PROJECTION_LIMIT_FILE_NAME);
        }
        initSettingsDirForUser(userId);
    }

    public void initSettingsDirForUser(int userId) {
        File dataDirectory = Environment.getDataDirectory();
        sSystemDir = new File(dataDirectory, "system/users/" + userId);
        boolean isSuccess = false;
        try {
            isSuccess = sSystemDir.mkdirs();
        } catch (SecurityException e) {
            SlogEx.e(TAG, "Exception throw while Making dir");
        }
        if (!isSuccess) {
            SlogEx.e(TAG, "Making dir failed");
        }
        FileUtilsEx.setPermissions(sSystemDir.toString(), FileUtilsEx.getSIRWXU() | FileUtilsEx.getSIRWXG() | FileUtilsEx.getSIROTH() | FileUtilsEx.getSIXOTH(), -1, -1);
        if (this.mAttribute.isLocalContainer()) {
            sSettingFilename = new File(sSystemDir, SETTING_CONFIG_FILE_NAME);
            sBackupSettingFilename = new File(sSystemDir, SETTING_CONFIG_BACKUP_FILE_NAME);
        }
    }

    public static File getLocalConfigFile(int type) {
        if (type == 0) {
            try {
                File cfgFile = new File(CLOUD_CONFIG_FILE_PATH, CLOUD_PACKAGE_CONFIG_FILE_NAME);
                if (!cfgFile.exists() || !cfgFile.isFile()) {
                    return HwCfgFilePolicy.getCfgFile("/xml/magic_window_application_list.xml", 0);
                }
                return cfgFile;
            } catch (SecurityException e) {
                SlogEx.e(TAG, "Config file can not read. type:" + type);
                return null;
            }
        } else if (type == 1) {
            return HwCfgFilePolicy.getCfgFile("/xml/magic_window_system_config.xml", 0);
        } else {
            if (type == 2) {
                return sBackupSettingFilename.exists() ? sBackupSettingFilename : sSettingFilename;
            } else if (type == 3) {
                return new File(CLOUD_CONFIG_FILE_PATH, CLOUD_MULTISCREEN_PROJECTION_LIMIT_FILE_NAME);
            } else {
                SlogEx.e(TAG, "Get local config file with unknonw type:" + type);
                return null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:120:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0100, code lost:
        com.huawei.android.util.SlogEx.e(com.huawei.server.magicwin.HwMagicWindowConfigLoader.TAG, "loadPackage execepton 1");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0106, code lost:
        if (0 != 0) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x010d, code lost:
        com.huawei.android.util.SlogEx.e(com.huawei.server.magicwin.HwMagicWindowConfigLoader.TAG, "loadPackage: exception while closing stream");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0112, code lost:
        r4.close();
     */
    /* JADX WARNING: Removed duplicated region for block: B:120:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00ff A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:16:0x002d] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0112  */
    public void loadPackage(HwMagicWindowConfig config, String pkgName) {
        InputStream inputStreamPkg;
        InputStream inputStreamPkg2 = null;
        ParcelFileDescriptor inputPFD = null;
        try {
            if (this.mAttribute.isVirtualContainer()) {
                Uri uri = config.getUri(0);
                if (uri == null) {
                    if (0 != 0) {
                        try {
                            inputStreamPkg2.close();
                        } catch (IOException e) {
                            SlogEx.e(TAG, "loadPackage: exception while closing stream");
                        }
                    }
                    if (0 != 0) {
                        try {
                            inputPFD.close();
                            return;
                        } catch (IOException e2) {
                            SlogEx.e(TAG, "loadPackage: exception while closing pfd");
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    try {
                        inputPFD = this.mContext.getContentResolver().openFileDescriptor(uri, "r");
                        if (inputPFD == null) {
                            if (0 != 0) {
                                try {
                                    inputStreamPkg2.close();
                                } catch (IOException e3) {
                                    SlogEx.e(TAG, "loadPackage: exception while closing stream");
                                }
                            }
                            if (inputPFD != null) {
                                try {
                                    inputPFD.close();
                                    return;
                                } catch (IOException e4) {
                                    SlogEx.e(TAG, "loadPackage: exception while closing pfd");
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            inputStreamPkg = new FileInputStream(inputPFD.getFileDescriptor());
                        }
                    } catch (IOException | NumberFormatException | XmlPullParserException e5) {
                    }
                }
            } else {
                File appList = getLocalConfigFile(0);
                if (appList == null) {
                    SlogEx.e(TAG, "loadPackage: app list null");
                    if (0 != 0) {
                        try {
                            inputStreamPkg2.close();
                        } catch (IOException e6) {
                            SlogEx.e(TAG, "loadPackage: exception while closing stream");
                        }
                    }
                    if (0 != 0) {
                        try {
                            inputPFD.close();
                            return;
                        } catch (IOException e7) {
                            SlogEx.e(TAG, "loadPackage: exception while closing pfd");
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    inputStreamPkg = new FileInputStream(appList);
                }
            }
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStreamPkg, null);
            SlogEx.i(TAG, "loadPackage start");
            long startMs = System.currentTimeMillis();
            for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                if (xmlEventType == 2 && XML_ELEMENT_PACKAGE.equals(xmlParser.getName())) {
                    parsePackageXml(config, xmlParser, pkgName);
                }
            }
            SlogEx.i(TAG, "loadPackage end. Cost " + (System.currentTimeMillis() - startMs) + " ms");
            try {
                inputStreamPkg.close();
            } catch (IOException e8) {
                SlogEx.e(TAG, "loadPackage: exception while closing stream");
            }
            if (inputPFD != null) {
                try {
                    inputPFD.close();
                    return;
                } catch (IOException e9) {
                    SlogEx.e(TAG, "loadPackage: exception while closing pfd");
                    return;
                }
            } else {
                return;
            }
            if (0 == 0) {
            }
        } catch (FileNotFoundException e10) {
            SlogEx.e(TAG, "loadPackage execepton 0");
            if (0 != 0) {
                try {
                    inputStreamPkg2.close();
                } catch (IOException e11) {
                    SlogEx.e(TAG, "loadPackage: exception while closing stream");
                }
            }
            if (0 != 0) {
                inputPFD.close();
            }
        } catch (Exception e12) {
            SlogEx.e(TAG, "loadPackage execepton 2");
            if (0 != 0) {
                try {
                    inputStreamPkg2.close();
                } catch (IOException e13) {
                    SlogEx.e(TAG, "loadPackage: exception while closing stream");
                }
            }
            if (0 != 0) {
                inputPFD.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStreamPkg2.close();
                } catch (IOException e14) {
                    SlogEx.e(TAG, "loadPackage: exception while closing stream");
                }
            }
            if (0 != 0) {
                try {
                    inputPFD.close();
                } catch (IOException e15) {
                    SlogEx.e(TAG, "loadPackage: exception while closing pfd");
                }
            }
            throw th;
        }
    }

    private void parsePackageXml(HwMagicWindowConfig config, XmlPullParser xmlParser, String pkgName) {
        String packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME);
        String defaultSetting = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_DEFAULT_SETTING);
        if (TextUtils.isEmpty(pkgName)) {
            if (!config.getAllInstalledPkgList().contains(packageName)) {
                return;
            }
        } else if (!pkgName.equals(packageName)) {
            return;
        }
        String windowMode = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_WINDOW_MODE);
        String fullscreenVideo = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_FULLSCREEN_VIDEO);
        String leftResume = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_LEFT_RESUME);
        String cameraPreview = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_CMAERA_PREVIEW);
        String isScaleEnabled = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_SCALE_ENABLED);
        String needRelaunch = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NEED_RELAUNCH);
        String isDragable = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_IS_DRAGABLE);
        String isNotchAdapted = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NOTCH_ADAPT);
        String packageHome = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_HOME);
        String isDragFs = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_DRAG_FS);
        if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
            SlogEx.d(TAG, "parser: packageName = " + packageName + " windowMode = " + windowMode);
            SlogEx.d(TAG, "parser: fullscreenVideo = " + fullscreenVideo + " leftResume = " + leftResume);
            SlogEx.d(TAG, "parser: cameraPreview = " + cameraPreview + " isScaleEnabled = " + isScaleEnabled);
            SlogEx.d(TAG, "parser: needRelaunch = " + needRelaunch + " defaultSetting = " + defaultSetting);
            SlogEx.d(TAG, "parser: isDragable = " + isDragable + " isNotchAdapted = " + isNotchAdapted);
            SlogEx.d(TAG, "parser: home = " + packageHome + " isDragFs = " + isDragFs);
        }
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(windowMode) && !TextUtils.isEmpty(fullscreenVideo)) {
            if (!TextUtils.isEmpty(leftResume) && !TextUtils.isEmpty(cameraPreview) && !TextUtils.isEmpty(needRelaunch)) {
                if (!TextUtils.isEmpty(isScaleEnabled)) {
                    LocalConfig localCfg = new LocalConfig(packageName, windowMode, fullscreenVideo, leftResume, cameraPreview, isScaleEnabled, needRelaunch, defaultSetting, isDragable, isNotchAdapted, isDragFs, this.mAttribute.isFoldableDevice());
                    if (config.getIsSupportOpenCap() || !(localCfg.mMode == -2 || localCfg.mMode == -1)) {
                        config.createPackage(localCfg);
                        if (!TextUtils.isEmpty(packageHome)) {
                            config.createHome(packageName, packageHome.split(",", 0));
                        }
                        if (TextUtils.isEmpty(pkgName)) {
                            return;
                        }
                        if (pkgName.equals(packageName)) {
                            config.updateSettingForPkg(packageName, "install_app");
                        }
                    }
                }
            }
        }
    }

    public static HwMagicWindowConfig.SystemConfig loadSystem(HwMagicWindowConfig config) {
        HwMagicWindowConfig.SystemConfig systemConfig = new HwMagicWindowConfig.SystemConfig();
        InputStream inputStreamSystem = null;
        ParcelFileDescriptor inputPFD = null;
        if (config != null) {
            try {
                Uri uri = config.getUri(1);
                if (uri == null) {
                    if (0 != 0) {
                        try {
                            inputStreamSystem.close();
                        } catch (IOException e) {
                            SlogEx.e(TAG, "magic_window_system_config.xmlload  config: IO Exception while closing stream");
                        }
                    }
                    if (0 != 0) {
                        try {
                            inputPFD.close();
                        } catch (IOException e2) {
                            SlogEx.e(TAG, "loadSystem: exception while closing pfd");
                        }
                    }
                    return systemConfig;
                }
                try {
                    inputPFD = config.getContext().getContentResolver().openFileDescriptor(uri, "r");
                    if (inputPFD == null) {
                        if (0 != 0) {
                            try {
                                inputStreamSystem.close();
                            } catch (IOException e3) {
                                SlogEx.e(TAG, "magic_window_system_config.xmlload  config: IO Exception while closing stream");
                            }
                        }
                        if (inputPFD != null) {
                            try {
                                inputPFD.close();
                            } catch (IOException e4) {
                                SlogEx.e(TAG, "loadSystem: exception while closing pfd");
                            }
                        }
                        return systemConfig;
                    }
                    inputStreamSystem = new FileInputStream(inputPFD.getFileDescriptor());
                } catch (FileNotFoundException | XmlPullParserException e5) {
                    SlogEx.e(TAG, "magic_window_system_config.xmlload config: exception 1");
                    if (0 != 0) {
                        try {
                            inputStreamSystem.close();
                        } catch (IOException e6) {
                            SlogEx.e(TAG, "magic_window_system_config.xmlload  config: IO Exception while closing stream");
                        }
                    }
                    if (0 != 0) {
                        inputPFD.close();
                    }
                }
            } catch (IOException e7) {
                SlogEx.e(TAG, "magic_window_system_config.xmlload config: exception 2");
                if (0 != 0) {
                    try {
                        inputStreamSystem.close();
                    } catch (IOException e8) {
                        SlogEx.e(TAG, "magic_window_system_config.xmlload  config: IO Exception while closing stream");
                    }
                }
                if (0 != 0) {
                    inputPFD.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStreamSystem.close();
                    } catch (IOException e9) {
                        SlogEx.e(TAG, "magic_window_system_config.xmlload  config: IO Exception while closing stream");
                    }
                }
                if (0 != 0) {
                    try {
                        inputPFD.close();
                    } catch (IOException e10) {
                        SlogEx.e(TAG, "loadSystem: exception while closing pfd");
                    }
                }
                throw th;
            }
        } else {
            File sysCfgFile = getLocalConfigFile(1);
            if (sysCfgFile != null) {
                inputStreamSystem = new FileInputStream(sysCfgFile);
            } else {
                SlogEx.v(TAG, "Local system config file does not exist");
            }
        }
        if (inputStreamSystem != null) {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStreamSystem, null);
            SlogEx.i(TAG, "loadSystem start");
            for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                if (xmlEventType == 2 && XML_ELEMENT_SYSTEM.equals(xmlParser.getName())) {
                    systemConfig = parseSystemXml(xmlParser);
                }
            }
            SlogEx.i(TAG, "loadSystem end");
        } else {
            SlogEx.e(TAG, "No system config.");
        }
        if (inputStreamSystem != null) {
            try {
                inputStreamSystem.close();
            } catch (IOException e11) {
                SlogEx.e(TAG, "magic_window_system_config.xmlload  config: IO Exception while closing stream");
            }
        }
        if (inputPFD != null) {
            try {
                inputPFD.close();
            } catch (IOException e12) {
                SlogEx.e(TAG, "loadSystem: exception while closing pfd");
            }
        }
        return systemConfig;
    }

    private static HwMagicWindowConfig.SystemConfig parseSystemXml(XmlPullParser xmlParser) {
        String capability;
        String supportDraggingToFullScreen;
        String leftPadding = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_LEFT_PADDING);
        String topPadding = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_TOP_PADDING);
        String rightPadding = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_RIGHT_PADDING);
        String bottomPadding = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_BOTTOM_PADDING);
        String midPadding = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_MID_PADDING);
        String midDragPadding = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_MID_DRAG_PADDING);
        String supportRoundAngle = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_ROUND_ANGLE);
        String supportDynamicEffect = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_DYNAMIC_EFFECT);
        String supportBackground = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_BACKGROUND);
        String supportOpenCapability = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_OPEN_CAPABILITY);
        String isBackToMiddle = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_BACK_MIDDLE);
        String cornerRadius = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_CORNER_RADIUS);
        String hostViewThreshold = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_HOST_VIEW_THRESHOLD);
        String supportDraggingToFullScreen2 = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_DRAG_TO_FULLSCREEN);
        String capability2 = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_CAPABILITY);
        if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
            SlogEx.d(TAG, "parser: leftPadding = " + leftPadding + " topPadding = " + topPadding);
            StringBuilder sb = new StringBuilder();
            capability = capability2;
            sb.append("parser: rightPadding = ");
            sb.append(rightPadding);
            sb.append(" bottomPadding = ");
            sb.append(bottomPadding);
            SlogEx.d(TAG, sb.toString());
            SlogEx.d(TAG, "parser: midPadding = " + midPadding + " midDragPadding = " + midDragPadding);
            SlogEx.d(TAG, "parser: supportRoundAngle = " + supportRoundAngle + " supportDynamicEffect = " + supportDynamicEffect + " supportBackground = " + supportBackground);
            SlogEx.d(TAG, "parser: supportOpenCapability = " + supportOpenCapability + " isBackToMiddle = " + isBackToMiddle + " cornerRadius = " + cornerRadius);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("parser: hostViewThreshold = ");
            sb2.append(hostViewThreshold);
            SlogEx.d(TAG, sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append("parser: supportDraggingToFullScreen = ");
            supportDraggingToFullScreen = supportDraggingToFullScreen2;
            sb3.append(supportDraggingToFullScreen);
            SlogEx.d(TAG, sb3.toString());
        } else {
            capability = capability2;
            supportDraggingToFullScreen = supportDraggingToFullScreen2;
        }
        HwMagicWindowConfig.SystemConfig systemConfig = new HwMagicWindowConfig.SystemConfig(leftPadding, topPadding, rightPadding, bottomPadding, midPadding, midDragPadding, supportRoundAngle, supportDynamicEffect, supportBackground, isBackToMiddle, cornerRadius, supportDraggingToFullScreen);
        systemConfig.setHostViewThreshold(hostViewThreshold);
        systemConfig.setOpenCapability(supportOpenCapability);
        systemConfig.setCapability(capability);
        return systemConfig;
    }

    /* JADX WARNING: Removed duplicated region for block: B:102:0x0250 A[SYNTHETIC, Splitter:B:102:0x0250] */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x026f  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01d1 A[SYNTHETIC, Splitter:B:82:0x01d1] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01f0  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0210 A[SYNTHETIC, Splitter:B:92:0x0210] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x022f  */
    public void readSetting(HwMagicWindowConfig config) {
        FileNotFoundException e;
        XmlPullParserException e2;
        IOException e3;
        FileInputStream settingsFileStream;
        String str;
        FileInputStream settingsFileStream2 = null;
        ParcelFileDescriptor inputPFD = null;
        try {
            int i = 2;
            if (this.mAttribute.isLocalContainer()) {
                File usrDatFile = getLocalConfigFile(2);
                if (usrDatFile == null) {
                    if (0 != 0) {
                        try {
                            settingsFileStream2.close();
                        } catch (IOException e4) {
                            SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e4));
                        }
                    }
                    if (0 != 0) {
                        try {
                            inputPFD.close();
                            return;
                        } catch (IOException e5) {
                            SlogEx.e(TAG, "readSetting: exception while closing pfd");
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    settingsFileStream = new FileInputStream(usrDatFile);
                }
            } else {
                try {
                    Uri uri = config.getUri(2);
                    if (uri == null) {
                        if (0 != 0) {
                            try {
                                settingsFileStream2.close();
                            } catch (IOException e6) {
                                SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e6));
                            }
                        }
                        if (0 != 0) {
                            try {
                                inputPFD.close();
                                return;
                            } catch (IOException e7) {
                                SlogEx.e(TAG, "readSetting: exception while closing pfd");
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        inputPFD = this.mContext.getContentResolver().openFileDescriptor(uri, "r");
                        if (inputPFD == null) {
                            if (0 != 0) {
                                try {
                                    settingsFileStream2.close();
                                } catch (IOException e8) {
                                    SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e8));
                                }
                            }
                            if (inputPFD != null) {
                                try {
                                    inputPFD.close();
                                    return;
                                } catch (IOException e9) {
                                    SlogEx.e(TAG, "readSetting: exception while closing pfd");
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            settingsFileStream = new FileInputStream(inputPFD.getFileDescriptor());
                        }
                    }
                } catch (FileNotFoundException e10) {
                    e = e10;
                    SlogEx.e(TAG, "readSetting, settingsFile load config: \n" + Log.getStackTraceString(e));
                    if (0 != 0) {
                    }
                    if (0 != 0) {
                    }
                    printMagicWinLog("readSetting, settingsFile load config out");
                } catch (XmlPullParserException e11) {
                    e2 = e11;
                    SlogEx.e(TAG, "readSetting, settingsFile load config: \n" + Log.getStackTraceString(e2));
                    if (0 != 0) {
                    }
                    if (0 != 0) {
                    }
                    printMagicWinLog("readSetting, settingsFile load config out");
                } catch (IOException e12) {
                    e3 = e12;
                    try {
                        SlogEx.e(TAG, "readSetting, settingsFile load config: \n" + Log.getStackTraceString(e3));
                        if (0 != 0) {
                        }
                        if (0 != 0) {
                        }
                        printMagicWinLog("readSetting, settingsFile load config out");
                    } catch (Throwable th) {
                        e = th;
                    }
                }
            }
            XmlPullParser xmlParser = Xml.newPullParser();
            String str2 = null;
            xmlParser.setInput(settingsFileStream, null);
            int xmlEventType = xmlParser.next();
            while (xmlEventType != 1) {
                if (xmlEventType != i || !XML_ELEMENT_SETTING.equals(xmlParser.getName())) {
                    str = str2;
                } else {
                    String packageName = xmlParser.getAttributeValue(str2, XML_ATTRIBUTE_NAME);
                    if (!config.getAllInstalledPkgList().contains(packageName)) {
                        xmlEventType = xmlParser.next();
                    } else {
                        String hwMagicWinEnabled = xmlParser.getAttributeValue(str2, XML_ATTRIBUTE_SETTING_ENABLED);
                        String hwDialogShown = xmlParser.getAttributeValue(str2, XML_ATTRIBUTE_SETTING_SHOWN);
                        String hwDragMode = xmlParser.getAttributeValue(str2, XML_ATTRIBUTE_SETTING_DRAG_MODE);
                        if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                            SlogEx.d(TAG, "parser: packageName=" + packageName + " hwMagicWinEnabled=" + hwMagicWinEnabled + " hwDialogShown=" + hwDialogShown + " hwDragMode=" + hwDragMode);
                        }
                        str = null;
                        config.createSetting(packageName, HwMagicWindowConfig.strToBoolean(hwMagicWinEnabled), HwMagicWindowConfig.strToBoolean(hwDialogShown), HwMagicWindowConfig.strToInt(hwDragMode, 0), "read_setting");
                    }
                }
                xmlEventType = xmlParser.next();
                str2 = str;
                i = 2;
            }
            try {
                settingsFileStream.close();
            } catch (IOException e13) {
                SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e13));
            }
            if (inputPFD != null) {
                try {
                    inputPFD.close();
                } catch (IOException e14) {
                    SlogEx.e(TAG, "readSetting: exception while closing pfd");
                }
            }
        } catch (FileNotFoundException e15) {
            e = e15;
            SlogEx.e(TAG, "readSetting, settingsFile load config: \n" + Log.getStackTraceString(e));
            if (0 != 0) {
                try {
                    settingsFileStream2.close();
                } catch (IOException e16) {
                    SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e16));
                }
            }
            if (0 != 0) {
                inputPFD.close();
            }
            printMagicWinLog("readSetting, settingsFile load config out");
        } catch (XmlPullParserException e17) {
            e2 = e17;
            SlogEx.e(TAG, "readSetting, settingsFile load config: \n" + Log.getStackTraceString(e2));
            if (0 != 0) {
                try {
                    settingsFileStream2.close();
                } catch (IOException e18) {
                    SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e18));
                }
            }
            if (0 != 0) {
                inputPFD.close();
            }
            printMagicWinLog("readSetting, settingsFile load config out");
        } catch (IOException e19) {
            e3 = e19;
            SlogEx.e(TAG, "readSetting, settingsFile load config: \n" + Log.getStackTraceString(e3));
            if (0 != 0) {
                try {
                    settingsFileStream2.close();
                } catch (IOException e20) {
                    SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e20));
                }
            }
            if (0 != 0) {
                inputPFD.close();
            }
            printMagicWinLog("readSetting, settingsFile load config out");
        } catch (Throwable th2) {
            e = th2;
            if (0 != 0) {
                try {
                    settingsFileStream2.close();
                } catch (IOException e21) {
                    SlogEx.e(TAG, "readSetting, settingsFile load config: IO Exception while closing stream\n" + Log.getStackTraceString(e21));
                }
            }
            if (0 != 0) {
                try {
                    inputPFD.close();
                } catch (IOException e22) {
                    SlogEx.e(TAG, "readSetting: exception while closing pfd");
                }
            }
            throw e;
        }
        printMagicWinLog("readSetting, settingsFile load config out");
    }

    private void printMagicWinLog(String log) {
        if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
            SlogEx.d(TAG, "MWLog:" + log);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0105, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x010a, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x010b, code lost:
        r0.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x010e, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0111, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0116, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0117, code lost:
        r0.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x011a, code lost:
        throw r1;
     */
    public void writeSetting(HwMagicWindowConfig config, String reason) {
        if (this.mAttribute.isVirtualContainer()) {
            SlogEx.i(TAG, "Not save virtual setting config.");
            return;
        }
        long startTime = SystemClock.uptimeMillis();
        if (checkSettingFile()) {
            try {
                FileOutputStream fstr = new FileOutputStream(sSettingFilename);
                BufferedOutputStream settingsFileStream = new BufferedOutputStream(fstr);
                XmlSerializer serializer = FastXmlSerializerEx.getFastXmlSerializer();
                serializer.setOutput(settingsFileStream, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, XML_ATTRIBUTE_SETTING_CONDIG);
                for (SettingConfig host : config.getHwMagicWinSettingConfigs().values()) {
                    serializer.startTag(null, XML_ELEMENT_SETTING);
                    serializer.attribute(null, XML_ATTRIBUTE_NAME, host.getName());
                    serializer.attribute(null, XML_ATTRIBUTE_SETTING_ENABLED, String.valueOf(host.getHwMagicWinEnabled()));
                    serializer.attribute(null, XML_ATTRIBUTE_SETTING_SHOWN, String.valueOf(host.getHwDialogShown()));
                    serializer.attribute(null, XML_ATTRIBUTE_SETTING_DRAG_MODE, String.valueOf(host.getDragMode()));
                    serializer.endTag(null, XML_ELEMENT_SETTING);
                }
                serializer.endTag(null, XML_ATTRIBUTE_SETTING_CONDIG);
                serializer.endDocument();
                settingsFileStream.flush();
                FileUtilsEx.sync(fstr);
                Utils.dbg(Utils.TAG_SETTING, "write setting:" + reason);
                sBackupSettingFilename.delete();
                FileUtilsEx.setPermissions(sSettingFilename.toString(), FileUtilsEx.getSIRUSR() | FileUtilsEx.getSIWUSR() | FileUtilsEx.getSIRGRP() | FileUtilsEx.getSIWGRP(), -1, -1);
                SlogEx.v(TAG, "write setting, took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                settingsFileStream.close();
                fstr.close();
            } catch (IOException e) {
                SlogEx.e(TAG, "IO Exception while writing stream" + Log.getStackTraceString(e));
                cleanupPartiallyWrittenFiles();
            }
        }
    }

    private boolean checkSettingFile() {
        if (!sSettingFilename.exists()) {
            return true;
        }
        if (sBackupSettingFilename.exists()) {
            sSettingFilename.delete();
            SlogEx.v(TAG, "SETTING_CONFIG_FILE_NAME delete old file");
            return true;
        } else if (sSettingFilename.renameTo(sBackupSettingFilename)) {
            return true;
        } else {
            SlogEx.e(TAG, "Unable to backup magic_window_setting_config,  current changes will be lost at reboot");
            return false;
        }
    }

    private void cleanupPartiallyWrittenFiles() {
        if (sSettingFilename.exists() && !sSettingFilename.delete()) {
            SlogEx.w(TAG, "Failed to clean up mangled file: " + sSettingFilename);
        }
        if (sBackupSettingFilename.exists() && !sBackupSettingFilename.renameTo(sSettingFilename)) {
            SlogEx.e(TAG, "Unable to restore usr dat file.");
        }
    }

    private void clearDownloadConfig(String path, String file) {
        try {
            File configFile = new File(path, file);
            if (configFile.exists() && configFile.isFile()) {
                configFile.delete();
            }
        } catch (SecurityException e) {
            SlogEx.w(TAG, "Delete file failed. Path: " + path + file);
        }
    }
}
