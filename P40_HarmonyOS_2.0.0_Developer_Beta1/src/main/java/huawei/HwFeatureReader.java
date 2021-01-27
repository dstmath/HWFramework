package huawei;

import android.content.ContentValues;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwFeatureReader {
    private static final String ATTR_VALUE = "value";
    private static final String TAG = "HwFeatureReader";
    private static final String XMLPATHGP = "/cust/xml/HwFeatureConfig_Gp.xml";
    private static final String XMLPATHHAP = "/cust/xml/HwFeatureConfig_Hap.xml";
    private static ContentValues map = null;
    private static boolean sIsHasReadFileGp = false;
    private static boolean sIshasReadFileHap = false;

    public static synchronized boolean getFeature(String feature) {
        synchronized (HwFeatureReader.class) {
            Log.d(TAG, "getFeature begin");
            if (map == null) {
                map = new ContentValues();
                getAllFeatures();
            }
            if (map.getAsBoolean(feature) != null) {
                return map.getAsBoolean(feature).booleanValue();
            }
            Log.d(TAG, "getFeature finish");
            return false;
        }
    }

    private static void getAllFeatures() {
        Log.d(TAG, "getAllFeatures begin");
        if (!sIsHasReadFileGp && !sIshasReadFileHap) {
            InputStreamReader confreader = null;
            try {
                InputStreamReader confreader2 = new InputStreamReader(new FileInputStream(new File(Environment.getDataDirectory(), XMLPATHHAP)), Charset.defaultCharset());
                XmlPullParser confparser = Xml.newPullParser();
                confparser.setInput(confreader2);
                XmlUtils.beginDocument(confparser, "features");
                while (true) {
                    XmlUtils.nextElement(confparser);
                    if (!"feature".equals(confparser.getName())) {
                        break;
                    } else if (confparser.getAttributeValue(null, ATTR_VALUE) != null) {
                        String feature = confparser.getAttributeValue(null, "name");
                        map.put(feature, confparser.getAttributeValue(null, ATTR_VALUE));
                        Log.d(TAG, "getAllFeatures  from confFileHap feature:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, ATTR_VALUE));
                    }
                }
                sIshasReadFileHap = true;
                InputStreamReader confreader3 = new InputStreamReader(new FileInputStream(new File(Environment.getDataDirectory(), XMLPATHGP)), Charset.defaultCharset());
                XmlPullParser confparser2 = Xml.newPullParser();
                confparser2.setInput(confreader3);
                XmlUtils.beginDocument(confparser2, "features");
                while (true) {
                    XmlUtils.nextElement(confparser2);
                    String profType = confparser2.getName();
                    Log.d(TAG, "getAllFeatures profType" + profType);
                    if (!"feature".equals(profType)) {
                        sIsHasReadFileGp = true;
                        try {
                            confreader3.close();
                            return;
                        } catch (Exception e) {
                            Log.e(TAG, "getAllFeatures catch Exception");
                            return;
                        }
                    } else if (confparser2.getAttributeValue(null, ATTR_VALUE) != null) {
                        String feature2 = confparser2.getAttributeValue(null, "name");
                        map.put(feature2, confparser2.getAttributeValue(null, ATTR_VALUE));
                        Log.d(TAG, "getAllFeatures feature from confFileGp:" + feature2 + "AttributeValue:" + confparser2.getAttributeValue(null, ATTR_VALUE));
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "File not found");
                if (0 != 0) {
                    confreader.close();
                }
            } catch (RuntimeException e3) {
                Log.e(TAG, "RuntimeException while parsing.");
                if (0 != 0) {
                    confreader.close();
                }
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "XmlPullParserException while parsing.");
                if (0 != 0) {
                    confreader.close();
                }
            } catch (IOException e5) {
                Log.e(TAG, "IOException while parsing.");
                if (0 != 0) {
                    try {
                        confreader.close();
                    } catch (Exception e6) {
                        Log.e(TAG, "getAllFeatures catch Exception");
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        confreader.close();
                    } catch (Exception e7) {
                        Log.e(TAG, "getAllFeatures catch Exception");
                    }
                }
                throw th;
            }
        }
    }
}
