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
    private static String TAG = "HwFeatureReader";
    private static final String XMLPATHGP = "/cust/xml/HwFeatureConfig_Gp.xml";
    private static final String XMLPATHHAP = "/cust/xml/HwFeatureConfig_Hap.xml";
    private static boolean hasReadFileGp = false;
    private static boolean hasReadFileHap = false;
    private static ContentValues map = null;

    public static synchronized boolean getFeature(String feature) {
        synchronized (HwFeatureReader.class) {
            Log.d(TAG, "getFeature begin");
            if (map == null) {
                map = new ContentValues();
                getAllFeatures();
            }
            if (map.getAsBoolean(feature) != null) {
                boolean booleanValue = map.getAsBoolean(feature).booleanValue();
                return booleanValue;
            }
            Log.d(TAG, "getFeature finish");
            return false;
        }
    }

    private static void getAllFeatures() {
        Log.d(TAG, "getAllFeatures begin");
        if (!hasReadFileGp && !hasReadFileHap) {
            File confFileGp = new File(Environment.getDataDirectory(), XMLPATHGP);
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
                    } else if (confparser.getAttributeValue(null, "value") != null) {
                        map.put(confparser.getAttributeValue(null, "name"), confparser.getAttributeValue(null, "value"));
                        Log.d(TAG, "getAllFeatures  from confFileHap feature:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, "value"));
                    }
                }
                hasReadFileHap = true;
                confreader = new InputStreamReader(new FileInputStream(confFileGp), Charset.defaultCharset());
                XmlPullParser confparser2 = Xml.newPullParser();
                confparser2.setInput(confreader);
                XmlUtils.beginDocument(confparser2, "features");
                while (true) {
                    XmlUtils.nextElement(confparser2);
                    String prof_type = confparser2.getName();
                    Log.d(TAG, "getAllFeatures prof_type" + prof_type);
                    if (!"feature".equals(prof_type)) {
                        break;
                    } else if (confparser2.getAttributeValue(null, "value") != null) {
                        map.put(confparser2.getAttributeValue(null, "name"), confparser2.getAttributeValue(null, "value"));
                        Log.d(TAG, "getAllFeatures feature from confFileGp:" + feature + "AttributeValue:" + confparser2.getAttributeValue(null, "value"));
                    }
                }
                hasReadFileGp = true;
                try {
                    confreader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "File not found");
                if (confreader != null) {
                    confreader.close();
                }
            } catch (RuntimeException e3) {
                Log.e(TAG, "Exception while parsing '" + e3);
                if (confreader != null) {
                    confreader.close();
                }
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "Exception while parsing '" + e4);
                if (confreader != null) {
                    confreader.close();
                }
            } catch (IOException e5) {
                Log.e(TAG, "Exception while parsing '" + e5);
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (Exception e6) {
                        e6.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (Exception e7) {
                        e7.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }
}
