package com.android.server.forcerotation;

import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwForceRotationConfigLoader {
    private static final String FORCE_ROTATION_CFG_FILE = "force_rotation_application_list.xml";
    private static final int FORCE_ROTATION_TYPE = 0;
    private static final String TAG = "HwForceRotationConfigLoader";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_ELEMENT_BLACK_LIST = "forcerotation_applications";
    private static final String XML_ELEMENT_NOT_COMPONENT_NAME = "not_component_name";
    private static final String XML_ELEMENT_PACKAGE_NAME = "package_name";

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0041 A:{Catch:{ FileNotFoundException -> 0x0090, XmlPullParserException -> 0x00bc, IOException -> 0x00f8, all -> 0x011e }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00e9 A:{SYNTHETIC, Splitter: B:52:0x00e9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HwForceRotationConfig load() {
        HwForceRotationConfig config = new HwForceRotationConfig();
        InputStream inputStream = null;
        File forceRotationCfgFile = null;
        try {
            forceRotationCfgFile = HwCfgFilePolicy.getCfgFile("xml/force_rotation_application_list.xml", 0);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        } catch (Exception e2) {
            Log.d(TAG, "HwCfgFilePolicy get force_rotation_application_list exception");
        }
        if (forceRotationCfgFile != null) {
            try {
                if (forceRotationCfgFile.exists()) {
                    Slog.v(TAG, "blackList:" + forceRotationCfgFile + " is exist");
                    inputStream = new FileInputStream(forceRotationCfgFile);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                            if (xmlEventType != 2 || !XML_ELEMENT_PACKAGE_NAME.equals(xmlParser.getName())) {
                                if (xmlEventType == 2) {
                                    if (XML_ELEMENT_NOT_COMPONENT_NAME.equals(xmlParser.getName())) {
                                        config.addNotSupportForceRotationAppActivityName(xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME));
                                    }
                                }
                                if (xmlEventType == 3) {
                                    if (XML_ELEMENT_BLACK_LIST.equals(xmlParser.getName())) {
                                        break;
                                    }
                                }
                            }
                            config.addForceRotationAppName(xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME));
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "load force rotation config: IO Exception while closing stream", e3);
                        }
                    }
                    return config;
                }
            } catch (FileNotFoundException e4) {
                Log.e(TAG, "load force rotation config: ", e4);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e32) {
                        Log.e(TAG, "load force rotation config: IO Exception while closing stream", e32);
                    }
                }
            } catch (XmlPullParserException e5) {
                Log.e(TAG, "load force rotation config: ", e5);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "load force rotation config: IO Exception while closing stream", e322);
                    }
                }
            } catch (IOException e3222) {
                Log.e(TAG, "load force rotation config: ", e3222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e32222) {
                        Log.e(TAG, "load force rotation config: IO Exception while closing stream", e32222);
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322222) {
                        Log.e(TAG, "load force rotation config: IO Exception while closing stream", e322222);
                    }
                }
            }
        }
        Slog.w(TAG, "force_rotation_application_list.xml is not exist");
        if (inputStream != null) {
        }
        if (inputStream != null) {
        }
        return config;
    }
}
