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
                return map.getAsBoolean(feature).booleanValue();
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
                        String feature = confparser.getAttributeValue(null, "name");
                        map.put(feature, confparser.getAttributeValue(null, "value"));
                        String str = TAG;
                        Log.d(str, "getAllFeatures  from confFileHap feature:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, "value"));
                    }
                }
                boolean z = true;
                hasReadFileHap = true;
                InputStreamReader confreader3 = new InputStreamReader(new FileInputStream(confFileGp), Charset.defaultCharset());
                XmlPullParser confparser2 = Xml.newPullParser();
                confparser2.setInput(confreader3);
                XmlUtils.beginDocument(confparser2, "features");
                while (true) {
                    XmlUtils.nextElement(confparser2);
                    String prof_type = confparser2.getName();
                    String str2 = TAG;
                    Log.d(str2, "getAllFeatures prof_type" + prof_type);
                    if (!"feature".equals(prof_type)) {
                        hasReadFileGp = z;
                        try {
                            confreader3.close();
                            return;
                        } catch (Exception e) {
                            Log.e(TAG, "getAllFeatures catch Exception");
                            return;
                        }
                    } else {
                        if (confparser2.getAttributeValue(null, "value") != null) {
                            String feature2 = confparser2.getAttributeValue(null, "name");
                            map.put(feature2, confparser2.getAttributeValue(null, "value"));
                            String str3 = TAG;
                            Log.d(str3, "getAllFeatures feature from confFileGp:" + feature2 + "AttributeValue:" + confparser2.getAttributeValue(null, "value"));
                        }
                        z = true;
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "File not found");
                if (0 != 0) {
                    confreader.close();
                }
            } catch (RuntimeException e3) {
                String str4 = TAG;
                Log.e(str4, "Exception while parsing '" + e3);
                if (0 != 0) {
                    confreader.close();
                }
            } catch (XmlPullParserException e4) {
                String str5 = TAG;
                Log.e(str5, "Exception while parsing '" + e4);
                if (0 != 0) {
                    confreader.close();
                }
            } catch (IOException e5) {
                String str6 = TAG;
                Log.e(str6, "Exception while parsing '" + e5);
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
