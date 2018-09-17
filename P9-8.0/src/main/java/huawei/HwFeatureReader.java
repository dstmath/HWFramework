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

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00fe A:{SYNTHETIC, Splitter: B:31:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01bf A:{SYNTHETIC, Splitter: B:62:0x01bf} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0198 A:{SYNTHETIC, Splitter: B:54:0x0198} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0173 A:{SYNTHETIC, Splitter: B:46:0x0173} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01d3 A:{SYNTHETIC, Splitter: B:70:0x01d3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void getAllFeatures() {
        Throwable th;
        RuntimeException e;
        XmlPullParserException e2;
        IOException e3;
        Log.d(TAG, "getAllFeatures begin");
        if (!(hasReadFileGp || hasReadFileHap)) {
            InputStreamReader confreader;
            File confFileGp = new File(Environment.getDataDirectory(), XMLPATHGP);
            InputStreamReader inputStreamReader = null;
            try {
                confreader = new InputStreamReader(new FileInputStream(new File(Environment.getDataDirectory(), XMLPATHHAP)), Charset.defaultCharset());
            } catch (FileNotFoundException e4) {
                try {
                    Log.e(TAG, "File not found");
                    if (inputStreamReader != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStreamReader != null) {
                    }
                    throw th;
                }
            } catch (RuntimeException e5) {
                e = e5;
                Log.e(TAG, "Exception while parsing '" + e);
                if (inputStreamReader != null) {
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                Log.e(TAG, "Exception while parsing '" + e2);
                if (inputStreamReader != null) {
                }
            } catch (IOException e7) {
                e3 = e7;
                Log.e(TAG, "Exception while parsing '" + e3);
                if (inputStreamReader != null) {
                }
            }
            try {
                String feature;
                XmlPullParser confparser = Xml.newPullParser();
                confparser.setInput(confreader);
                XmlUtils.beginDocument(confparser, "features");
                while (true) {
                    XmlUtils.nextElement(confparser);
                    if (!"feature".equals(confparser.getName())) {
                        break;
                    } else if (confparser.getAttributeValue(null, "value") != null) {
                        feature = confparser.getAttributeValue(null, "name");
                        map.put(feature, confparser.getAttributeValue(null, "value"));
                        Log.d(TAG, "getAllFeatures  from confFileHap feature:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, "value"));
                    }
                }
                hasReadFileHap = true;
                inputStreamReader = new InputStreamReader(new FileInputStream(confFileGp), Charset.defaultCharset());
                confparser = Xml.newPullParser();
                confparser.setInput(inputStreamReader);
                XmlUtils.beginDocument(confparser, "features");
                while (true) {
                    XmlUtils.nextElement(confparser);
                    String prof_type = confparser.getName();
                    Log.d(TAG, "getAllFeatures prof_type" + prof_type);
                    if (!"feature".equals(prof_type)) {
                        break;
                    } else if (confparser.getAttributeValue(null, "value") != null) {
                        feature = confparser.getAttributeValue(null, "name");
                        map.put(feature, confparser.getAttributeValue(null, "value"));
                        Log.d(TAG, "getAllFeatures feature from confFileGp:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, "value"));
                    }
                }
                hasReadFileGp = true;
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e8) {
                        e8.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e9) {
                inputStreamReader = confreader;
                Log.e(TAG, "File not found");
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e82) {
                        e82.printStackTrace();
                    }
                }
            } catch (RuntimeException e10) {
                e = e10;
                inputStreamReader = confreader;
                Log.e(TAG, "Exception while parsing '" + e);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e822) {
                        e822.printStackTrace();
                    }
                }
            } catch (XmlPullParserException e11) {
                e2 = e11;
                inputStreamReader = confreader;
                Log.e(TAG, "Exception while parsing '" + e2);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e8222) {
                        e8222.printStackTrace();
                    }
                }
            } catch (IOException e12) {
                e3 = e12;
                inputStreamReader = confreader;
                Log.e(TAG, "Exception while parsing '" + e3);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e82222) {
                        e82222.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                inputStreamReader = confreader;
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e822222) {
                        e822222.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }
}
