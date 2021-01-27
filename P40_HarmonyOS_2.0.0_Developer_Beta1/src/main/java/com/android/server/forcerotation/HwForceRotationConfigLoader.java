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

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0063 A[Catch:{ FileNotFoundException -> 0x0059, XmlPullParserException -> 0x0056, IOException -> 0x0053, all -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00bd A[SYNTHETIC, Splitter:B:42:0x00bd] */
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
                            if (xmlEventType == 2 && XML_ELEMENT_PACKAGE_NAME.equals(xmlParser.getName())) {
                                config.addForceRotationAppName(xmlParser.getAttributeValue(null, "name"));
                            } else if (xmlEventType != 2 || !XML_ELEMENT_NOT_COMPONENT_NAME.equals(xmlParser.getName())) {
                                if (xmlEventType == 3 && XML_ELEMENT_BLACK_LIST.equals(xmlParser.getName())) {
                                    break;
                                }
                            } else {
                                config.addNotSupportForceRotationAppActivityName(xmlParser.getAttributeValue(null, "name"));
                            }
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "load force rotation config: IO Exception while closing stream");
                        }
                    }
                    return config;
                }
            } catch (FileNotFoundException e4) {
                Log.e(TAG, "load force rotation config : file not found");
                if (0 != 0) {
                    inputStream.close();
                }
            } catch (XmlPullParserException e5) {
                Log.e(TAG, "load force rotation config: ", e5);
                if (0 != 0) {
                    inputStream.close();
                }
            } catch (IOException e6) {
                Log.e(TAG, "load force rotation config: ", e6);
                if (0 != 0) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "load force rotation config: IO Exception while closing stream");
                    }
                }
                throw th;
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
