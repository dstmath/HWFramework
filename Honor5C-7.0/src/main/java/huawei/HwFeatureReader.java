package huawei;

import android.content.ContentValues;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.huawei.hsm.permission.StubController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwFeatureReader {
    private static String TAG = null;
    private static final String XMLPATHGP = "/cust/xml/HwFeatureConfig_Gp.xml";
    private static final String XMLPATHHAP = "/cust/xml/HwFeatureConfig_Hap.xml";
    private static boolean hasReadFileGp;
    private static boolean hasReadFileHap;
    private static ContentValues map;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.HwFeatureReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.HwFeatureReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.HwFeatureReader.<clinit>():void");
    }

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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void getAllFeatures() {
        RuntimeException e;
        XmlPullParserException e2;
        IOException e3;
        Log.d(TAG, "getAllFeatures begin");
        if (!(hasReadFileGp || hasReadFileHap)) {
            File confFileGp = new File(Environment.getDataDirectory(), XMLPATHGP);
            InputStreamReader inputStreamReader = null;
            try {
                InputStreamReader confreader = new InputStreamReader(new FileInputStream(new File(Environment.getDataDirectory(), XMLPATHHAP)), Charset.defaultCharset());
                try {
                    String feature;
                    XmlPullParser confparser = Xml.newPullParser();
                    confparser.setInput(confreader);
                    XmlUtils.beginDocument(confparser, "features");
                    while (true) {
                        XmlUtils.nextElement(confparser);
                        if (!"feature".equals(confparser.getName())) {
                            break;
                        } else if (confparser.getAttributeValue(null, StubController.TABLE_COMMON_COLUM_VALUE) != null) {
                            feature = confparser.getAttributeValue(null, "name");
                            map.put(feature, confparser.getAttributeValue(null, StubController.TABLE_COMMON_COLUM_VALUE));
                            Log.d(TAG, "getAllFeatures  from confFileHap feature:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, StubController.TABLE_COMMON_COLUM_VALUE));
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
                        } else if (confparser.getAttributeValue(null, StubController.TABLE_COMMON_COLUM_VALUE) != null) {
                            feature = confparser.getAttributeValue(null, "name");
                            map.put(feature, confparser.getAttributeValue(null, StubController.TABLE_COMMON_COLUM_VALUE));
                            Log.d(TAG, "getAllFeatures feature from confFileGp:" + feature + "AttributeValue:" + confparser.getAttributeValue(null, StubController.TABLE_COMMON_COLUM_VALUE));
                        }
                    }
                    hasReadFileGp = true;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e5) {
                    inputStreamReader = confreader;
                } catch (RuntimeException e6) {
                    e = e6;
                    inputStreamReader = confreader;
                } catch (XmlPullParserException e7) {
                    e2 = e7;
                    inputStreamReader = confreader;
                } catch (IOException e8) {
                    e3 = e8;
                    inputStreamReader = confreader;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    inputStreamReader = confreader;
                }
            } catch (FileNotFoundException e9) {
            } catch (RuntimeException e10) {
                e = e10;
            } catch (XmlPullParserException e11) {
                e2 = e11;
            } catch (IOException e12) {
                e3 = e12;
            }
        }
    }
}
