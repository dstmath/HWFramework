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
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPgSceneDetectionAppName {
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "HwPgSceneDetectionAppName";
    private static final String XML_NAME = "HwPgSceneDetectionAppName.xml";
    private static final HwPgSceneDetectionAppName mInstance = new HwPgSceneDetectionAppName();
    private static HashSet<String> mNoUsePowerCureUniqueAppName = new HashSet<>();
    private static Map<String, Integer> mQrCodeAppNameMap = Collections.synchronizedMap(new HashMap());

    public HwPgSceneDetectionAppName() {
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
        if (DEBUG) {
            Slog.i(TAG, "loadCotaConfig start");
        }
        try {
            if (!parseConfig(getCotaXmlFile())) {
                Slog.e(TAG, "loadCotaConfig failed");
            } else if (DEBUG) {
                Slog.i(TAG, "loadCotaConfig success");
            }
        } catch (IOException e) {
            Slog.e(TAG, "loadCotaConfig IOException");
        }
    }

    private static File getCotaXmlFile() {
        String[] dirs = HwCfgFilePolicy.getDownloadCfgFile("xml/lcd/QrCodePara", "xml/lcd/QrCodePara/QrCodeAppList.xml");
        if (dirs == null || dirs.length == 0) {
            Slog.w(TAG, "loadCotaConfig getDownloadCfgFile return null");
            return null;
        }
        for (String dir : dirs) {
            Slog.i(TAG, "getDownloadCfgFile dir:" + dir);
        }
        return new File(dirs[0]);
    }

    private void loadDefaultConfig() {
        if (!mNoUsePowerCureUniqueAppName.isEmpty()) {
            mNoUsePowerCureUniqueAppName.clear();
        }
        if (!mQrCodeAppNameMap.isEmpty()) {
            mQrCodeAppNameMap.clear();
        }
    }

    public static boolean isNoUsePowerCureAppEnable(String appName) {
        if (appName == null || mNoUsePowerCureUniqueAppName == null || mNoUsePowerCureUniqueAppName.isEmpty()) {
            return false;
        }
        return mNoUsePowerCureUniqueAppName.contains(appName);
    }

    public static boolean isQrCodeAppBoostBrightness(String appName, int brightnessLevel) {
        if (appName == null || mQrCodeAppNameMap.isEmpty() || !mQrCodeAppNameMap.containsKey(appName) || brightnessLevel < mQrCodeAppNameMap.get(appName).intValue()) {
            return false;
        }
        return true;
    }

    private static File getProductXmlFile() {
        try {
            String xmlPath = String.format("/xml/lcd/%s", new Object[]{XML_NAME});
            File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
            if (xmlFile != null) {
                return xmlFile;
            }
            Slog.w(TAG, "get xmlFile :" + xmlPath + " failed!");
            return null;
        } catch (NoClassDefFoundError e) {
            Slog.w(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        }
    }

    private static boolean parseConfig(File xmlFile) throws IOException {
        if (xmlFile == null) {
            return false;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            if (getConfigFromXML(inputStream2)) {
                printConfigFromXML();
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
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (SecurityException e4) {
            Slog.e(TAG, "parseConfig : SecurityException");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "parseConfig inputStream: IOException");
                }
            }
            throw th;
        }
    }

    private static void printConfigFromXML() {
        if (DEBUG) {
            if (!mNoUsePowerCureUniqueAppName.isEmpty()) {
                Iterator<String> it = mNoUsePowerCureUniqueAppName.iterator();
                while (it.hasNext()) {
                    Slog.d(TAG, "printConfig noUsePowerCureAppList apkName=" + it.next());
                }
            } else {
                Slog.d(TAG, "printConfig noUsePowerCureAppList is empty");
            }
            if (!mQrCodeAppNameMap.isEmpty()) {
                for (Map.Entry<String, Integer> entry : mQrCodeAppNameMap.entrySet()) {
                    Slog.d(TAG, "printConfig QrCodeApp name=" + entry.getKey() + ", value=" + entry.getValue());
                }
                return;
            }
            Slog.d(TAG, "printConfig mQrCodeAppNameMap is empty");
        }
    }

    private static boolean getConfigFromXML(FileInputStream inStream) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, StandardCharsets.UTF_8.name());
            for (int event = parser.getEventType(); event != 1; event = parser.next()) {
                parseEvent(event, parser);
            }
            if (!mNoUsePowerCureUniqueAppName.isEmpty() || !mQrCodeAppNameMap.isEmpty()) {
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
        switch (event) {
            case 2:
                String name = parser.getName();
                if ("NoUsePowerCureApp".equals(name)) {
                    mNoUsePowerCureUniqueAppName.add(parser.getAttributeValue(null, "AppName"));
                    return;
                } else if ("QrCodeApp".equals(name)) {
                    mQrCodeAppNameMap.put(parser.getAttributeValue(null, "Name"), Integer.valueOf(Integer.parseInt(parser.getAttributeValue(null, HwHiNetworkParmStatistics.LEVEL))));
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }
}
