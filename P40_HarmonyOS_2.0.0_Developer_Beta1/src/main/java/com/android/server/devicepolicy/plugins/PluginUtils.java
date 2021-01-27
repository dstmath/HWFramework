package com.android.server.devicepolicy.plugins;

import android.os.Environment;
import com.android.server.devicepolicy.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PluginUtils {
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String ENCODING = "UTF-8";
    private static final String TAG = PluginUtils.class.getSimpleName();

    private PluginUtils() {
    }

    public static String readValueFromXml(String tag) {
        String result = null;
        File file = new File(Environment.getDataSystemDirectory(), DEVICE_POLICIES_XML);
        if (tag == null || !file.exists()) {
            return null;
        }
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            xmlParser.setInput(fis2, ENCODING);
            int outerDepth = xmlParser.getDepth();
            while (true) {
                int outerType = xmlParser.next();
                if (outerType == 1 || (outerType == 3 && xmlParser.getDepth() <= outerDepth)) {
                    try {
                        fis2.close();
                        break;
                    } catch (IOException e) {
                        HwLog.e(TAG, "Unable to close the inputStream");
                    }
                } else if (outerType != 3) {
                    if (outerType != 4) {
                        if (tag.equals(xmlParser.getName())) {
                            result = xmlParser.getAttributeValue(0);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            HwLog.e(TAG, "FileNotFoundException when try to parse device_policies");
            if (0 != 0) {
                fis.close();
            }
        } catch (XmlPullParserException e3) {
            HwLog.e(TAG, "XmlPullParserException when try to parse device_policies");
            if (0 != 0) {
                fis.close();
            }
        } catch (IOException e4) {
            HwLog.e(TAG, "IOException when try to parse device_policies");
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e5) {
                    HwLog.e(TAG, "Unable to close the inputStream");
                }
            }
            throw th;
        }
        return result;
    }
}
