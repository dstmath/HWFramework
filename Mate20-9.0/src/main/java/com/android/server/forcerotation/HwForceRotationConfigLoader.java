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

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0064 A[Catch:{ FileNotFoundException -> 0x0058, XmlPullParserException -> 0x0055, IOException -> 0x0052, all -> 0x004f }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00c0 A[SYNTHETIC, Splitter:B:43:0x00c0] */
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
                        int xmlEventType = xmlParser.next();
                        while (true) {
                            if (xmlEventType == 1) {
                                break;
                            }
                            if (xmlEventType == 2 && XML_ELEMENT_PACKAGE_NAME.equals(xmlParser.getName())) {
                                config.addForceRotationAppName(xmlParser.getAttributeValue(null, "name"));
                            } else if (xmlEventType != 2 || !XML_ELEMENT_NOT_COMPONENT_NAME.equals(xmlParser.getName())) {
                                if (xmlEventType == 3 && XML_ELEMENT_BLACK_LIST.equals(xmlParser.getName())) {
                                    break;
                                }
                            } else {
                                config.addNotSupportForceRotationAppActivityName(xmlParser.getAttributeValue(null, "name"));
                            }
                            xmlEventType = xmlParser.next();
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
                    inputStream.close();
                }
            } catch (XmlPullParserException e5) {
                Log.e(TAG, "load force rotation config: ", e5);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e6) {
                Log.e(TAG, "load force rotation config: ", e6);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "load force rotation config: IO Exception while closing stream", e7);
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
