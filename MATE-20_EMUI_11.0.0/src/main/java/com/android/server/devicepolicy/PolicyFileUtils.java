package com.android.server.devicepolicy;

import android.os.Environment;
import android.util.Xml;
import com.android.internal.util.JournaledFile;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PolicyFileUtils {
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String HW_POLICY = "hw_policy";
    private static final String TAG = "PolicyFileUtils";

    public static JournaledFile getJournaledFile(int userHandle, String fileName) {
        String base;
        String path = SettingsMDMPlugin.EMPTY_STRING;
        try {
            path = new File(Environment.getUserSystemDirectory(userHandle), fileName).getCanonicalPath();
        } catch (IOException e) {
            HwLog.e(TAG, "hasHwPolicy : Invalid file path");
        }
        if (userHandle == 0) {
            base = "/data/system/" + fileName;
        } else {
            base = path;
        }
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    private static boolean hasHwPolicy(XmlPullParser parser) {
        int type;
        int type2;
        while (true) {
            try {
                type = parser.next();
            } catch (IOException | XmlPullParserException e) {
                HwLog.e(TAG, "hasHwPolicy IOException");
            }
            if (type == 1) {
                break;
            } else if (type == 2) {
                if (HW_POLICY.equals(parser.getName())) {
                    while (true) {
                        type2 = parser.next();
                        if (type2 == 1 || type2 == 3) {
                            break;
                        } else if (type2 == 2) {
                            HwLog.d(TAG, "find HwPolicy");
                            return true;
                        }
                    }
                    if (type2 == 1) {
                        break;
                    }
                }
            }
        }
        HwLog.d(TAG, "Can't find HwPolicy");
        return false;
    }

    public static boolean hasHwPolicy(int userHandle) {
        HwLog.d(TAG, "hasHwPolicy, userHandle :" + userHandle);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(getJournaledFile(userHandle, DEVICE_POLICIES_XML).chooseForRead());
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            boolean hasHwPolicy = hasHwPolicy(parser);
            try {
                stream.close();
            } catch (IOException e) {
                HwLog.e(TAG, "cannot close the stream.");
            }
            return hasHwPolicy;
        } catch (IOException | XmlPullParserException e2) {
            HwLog.e(TAG, "XmlPullParserException | IOException");
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    HwLog.e(TAG, "cannot close the stream.");
                }
            }
            HwLog.d(TAG, "Can't find HwPolicy");
            return false;
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    HwLog.e(TAG, "cannot close the stream.");
                }
            }
            throw th;
        }
    }
}
