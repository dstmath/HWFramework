package com.android.server.display;

import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPgSceneDetectionAppName {
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "HwPgSceneDetectionAppName";
    private static final String XML_NAME = "HwPgSceneDetectionAppName.xml";
    private static HwPgSceneDetectionAppName sInstance = new HwPgSceneDetectionAppName();
    private static HashSet<String> sPowerSavingKeepBrightnessApp = new HashSet<>();
    private static Map<String, Integer> sQrCodeAppNameMap = Collections.synchronizedMap(new HashMap());

    private HwPgSceneDetectionAppName() {
        Slog.i(TAG, "HwPgSceneDetectionAppName begin " + this);
        try {
            if (!parseConfig(getProductXmlFile())) {
                loadDefaultConfig();
                Slog.i(TAG, "init, no need PgSceneDetectionApp");
            }
            parseConfig(getCotaXmlFile());
        } catch (IOException e) {
            loadDefaultConfig();
            Slog.i(TAG, "init IOException, no need PgSceneDetectionApp");
        }
        Slog.i(TAG, "HwPgSceneDetectionAppName end " + this);
    }

    public static void loadCotaConfig() {
        if (HWFLOW) {
            Slog.i(TAG, "loadCotaConfig start");
        }
        try {
            if (!parseConfig(getCotaXmlFile())) {
                Slog.e(TAG, "loadCotaConfig failed");
            } else if (HWFLOW) {
                Slog.i(TAG, "loadCotaConfig success");
            }
        } catch (IOException e) {
            Slog.e(TAG, "loadCotaConfig IOException");
        }
    }

    private static Optional<File> getCotaXmlFile() {
        String[] dirs = HwCfgFilePolicy.getDownloadCfgFile("xml/lcd/QrCodePara", "xml/lcd/QrCodePara/QrCodeAppList.xml");
        if (dirs == null || dirs.length == 0) {
            Slog.w(TAG, "loadCotaConfig getDownloadCfgFile return null");
            return Optional.empty();
        }
        for (String dir : dirs) {
            Slog.i(TAG, "getDownloadCfgFile dir:" + dir);
        }
        return Optional.of(new File(dirs[0]));
    }

    private void loadDefaultConfig() {
        if (!sPowerSavingKeepBrightnessApp.isEmpty()) {
            sPowerSavingKeepBrightnessApp.clear();
        }
        if (!sQrCodeAppNameMap.isEmpty()) {
            sQrCodeAppNameMap.clear();
        }
    }

    static boolean getPowerSavingKeepBrightnessAppEnable(String appName) {
        HashSet<String> hashSet;
        if (appName == null || (hashSet = sPowerSavingKeepBrightnessApp) == null || hashSet.isEmpty()) {
            return false;
        }
        return sPowerSavingKeepBrightnessApp.contains(appName);
    }

    public static boolean isQrCodeAppBoostBrightness(String appName, int brightnessLevel) {
        if (appName == null || sQrCodeAppNameMap.isEmpty() || !sQrCodeAppNameMap.containsKey(appName) || brightnessLevel < sQrCodeAppNameMap.get(appName).intValue()) {
            return false;
        }
        return true;
    }

    private static Optional<File> getProductXmlFile() {
        try {
            String xmlPath = String.format(Locale.ROOT, "/xml/lcd/%s", XML_NAME);
            File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
            if (xmlFile != null) {
                return Optional.of(xmlFile);
            }
            Slog.w(TAG, "get xmlFile :" + xmlPath + " failed!");
            return Optional.empty();
        } catch (NoClassDefFoundError e) {
            Slog.w(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return Optional.empty();
        }
    }

    private static boolean parseConfig(Optional<File> xmlFile) throws IOException {
        if (!xmlFile.isPresent()) {
            return false;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile.get());
            if (getConfigFromXml(inputStream2)) {
                printConfigFromXml();
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    Slog.e(TAG, "parseConfig inputStream: IOException");
                }
                return true;
            }
            try {
                inputStream2.close();
            } catch (IOException e2) {
                Slog.e(TAG, "parseConfig inputStream: IOException");
            }
            return false;
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "parseConfig : FileNotFoundException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (SecurityException e4) {
            Slog.e(TAG, "parseConfig : SecurityException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "parseConfig inputStream: IOException");
                }
            }
            throw th;
        }
    }

    private static void printConfigFromXml() {
        if (HWFLOW) {
            if (!sPowerSavingKeepBrightnessApp.isEmpty()) {
                Iterator<String> it = sPowerSavingKeepBrightnessApp.iterator();
                while (it.hasNext()) {
                    Slog.i(TAG, "printConfig noUsePowerCureAppList apkName=" + it.next());
                }
            } else {
                Slog.i(TAG, "printConfig noUsePowerCureAppList is empty");
            }
            if (!sQrCodeAppNameMap.isEmpty()) {
                for (Map.Entry<String, Integer> entry : sQrCodeAppNameMap.entrySet()) {
                    Slog.i(TAG, "printConfig QrCodeApp name=" + entry.getKey() + ", value=" + entry.getValue());
                }
                return;
            }
            Slog.i(TAG, "printConfig sQrCodeAppNameMap is empty");
        }
    }

    private static boolean getConfigFromXml(FileInputStream inStream) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, StandardCharsets.UTF_8.name());
            for (int event = parser.getEventType(); event != 1; event = parser.next()) {
                parseEvent(event, parser);
            }
            if (!sPowerSavingKeepBrightnessApp.isEmpty() || !sQrCodeAppNameMap.isEmpty()) {
                return true;
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "getXmlFile error FileNotFoundException!");
        } catch (NumberFormatException e2) {
            Slog.e(TAG, "getXmlFile error NumberFormatException!");
        } catch (IOException e3) {
            Slog.e(TAG, "getXmlFile error IOException!");
        } catch (XmlPullParserException e4) {
            Slog.e(TAG, "getXmlFile error XmlPullParserException!");
        }
        Slog.w(TAG, "getConfigFromeXML false!");
        return false;
    }

    private static void parseEvent(int event, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (event != 2) {
            if (event != 3) {
            }
            return;
        }
        String name = parser.getName();
        if ("NoUsePowerCureApp".equals(name)) {
            sPowerSavingKeepBrightnessApp.add(parser.getAttributeValue(null, "AppName"));
        }
        if ("QrCodeApp".equals(name)) {
            sQrCodeAppNameMap.put(parser.getAttributeValue(null, "Name"), Integer.valueOf(Integer.parseInt(parser.getAttributeValue(null, HwHiNetworkParmStatistics.LEVEL))));
        }
    }
}
