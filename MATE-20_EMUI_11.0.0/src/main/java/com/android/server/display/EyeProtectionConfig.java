package com.android.server.display;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.displayengine.DeLog;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class EyeProtectionConfig {
    private static final boolean DEBUG = false;
    private static final String HW_EYEPROTECTION_CONFIG_FILE = "EyeProtectionConfig.xml";
    private static final String HW_EYEPROTECTION_CONFIG_FILE_NAME = "EyeProtectionConfig";
    private static final String KEY_EYE_COMFORT_LESSWARM = "eye_comfort_lesswarm";
    private static final String KEY_EYE_COMFORT_MOREWARM = "eye_comfort_morewarm";
    private static final int MIN_VERSION_SEGMENTS = 2;
    private static final String TAG = "DE J EyeProtectionConfig";
    private static boolean sConfigGroupLoadStarted = false;
    private static int sLessWarm = 0;
    private static boolean sLoadFinished = false;
    private static int sMoreWarm = 0;

    public static void initSettingsRange(String lcdPanelName, Context context) {
        String lcdPanelNameTrimed = null;
        if (lcdPanelName != null) {
            lcdPanelNameTrimed = lcdPanelName.replace(' ', '_');
        }
        try {
            if (!getConfig(lcdPanelNameTrimed)) {
                DeLog.i(TAG, "getConfig failed.");
            }
        } catch (IOException e) {
            DeLog.e(TAG, "IOException.");
        }
        setDefaultColorTemptureValue(context);
    }

    private static boolean getConfig(String lcdPanelName) throws IOException {
        String version = SystemProperties.get("ro.build.version.emui", (String) null);
        DeLog.i(TAG, "HwEyeProtectionControllerImpl getConfig");
        if (TextUtils.isEmpty(version)) {
            DeLog.w(TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] splitedVersions = version.split("EmotionUI_");
        if (splitedVersions.length < 2) {
            DeLog.w(TAG, "split failed! version = " + version);
            return false;
        }
        String emuiVersion = splitedVersions[1];
        if (TextUtils.isEmpty(emuiVersion)) {
            DeLog.w(TAG, "get emuiVersion failed!");
            return false;
        }
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format(Locale.ENGLISH, "/xml/lcd/%s/%s", emuiVersion, HW_EYEPROTECTION_CONFIG_FILE), 0);
        if (xmlFile != null) {
            return updateConfigFromXml(xmlFile);
        }
        File xmlFile2 = HwCfgFilePolicy.getCfgFile(String.format(Locale.ENGLISH, "/xml/lcd/%s", HW_EYEPROTECTION_CONFIG_FILE), 0);
        if (xmlFile2 != null) {
            return updateConfigFromXml(xmlFile2);
        }
        String lcdEyeProtectionConfigFile = "EyeProtectionConfig_" + lcdPanelName + ".xml";
        File xmlFile3 = HwCfgFilePolicy.getCfgFile(String.format(Locale.ENGLISH, "/xml/lcd/%s/%s", emuiVersion, lcdEyeProtectionConfigFile), 0);
        if (xmlFile3 != null) {
            return updateConfigFromXml(xmlFile3);
        }
        File xmlFile4 = HwCfgFilePolicy.getCfgFile(String.format(Locale.ENGLISH, "/xml/lcd/%s", lcdEyeProtectionConfigFile), 0);
        if (xmlFile4 != null) {
            return updateConfigFromXml(xmlFile4);
        }
        DeLog.w(TAG, "get xmlFile failed!");
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0016, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0017, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001a, code lost:
        throw r3;
     */
    private static boolean updateConfigFromXml(File xmlFile) {
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            getConfigFromXml(inputStream);
            inputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            DeLog.e(TAG, "File not found exception occured");
            return false;
        } catch (IOException e2) {
            DeLog.e(TAG, "some IOException occured");
            return false;
        }
    }

    private static boolean getConfigFromXml(InputStream inStream) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType == 1) {
                    break;
                }
                if (eventType == 2) {
                    handleDifferentNameFromStart(parser.getName(), parser);
                } else if (eventType == 3) {
                    handleDifferentNameFromEnd(parser.getName());
                }
                if (sLoadFinished) {
                    break;
                }
                eventType = parser.next();
            }
            if (sLoadFinished) {
                DeLog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            DeLog.e(TAG, "some xml pull parser exception happened");
        } catch (IOException e2) {
            DeLog.e(TAG, "some IO exception happened");
        } catch (NumberFormatException e3) {
            DeLog.e(TAG, "some number format exception happened");
        }
        DeLog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    private static void handleDifferentNameFromStart(String name, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
        if (HW_EYEPROTECTION_CONFIG_FILE_NAME.equals(name)) {
            sConfigGroupLoadStarted = true;
        } else if ("LessWarm".equals(name)) {
            sLessWarm = Integer.parseInt(parser.nextText());
        } else if ("MoreWarm".equals(name)) {
            sMoreWarm = Integer.parseInt(parser.nextText());
        }
    }

    private static void handleDifferentNameFromEnd(String name) throws IOException, XmlPullParserException {
        if (HW_EYEPROTECTION_CONFIG_FILE_NAME.equals(name) && sConfigGroupLoadStarted) {
            sLoadFinished = true;
        }
    }

    public static void setDefaultColorTemptureValue(Context context) {
        if (sLessWarm != 0 || sMoreWarm != 0) {
            DeLog.i(TAG, "setDefaultColorTemptureValue sLessWarm = " + sLessWarm + ", sMoreWarm = " + sMoreWarm);
            Settings.System.putIntForUser(context.getContentResolver(), KEY_EYE_COMFORT_LESSWARM, sLessWarm, -2);
            Settings.System.putIntForUser(context.getContentResolver(), KEY_EYE_COMFORT_MOREWARM, sMoreWarm, -2);
        }
    }
}
