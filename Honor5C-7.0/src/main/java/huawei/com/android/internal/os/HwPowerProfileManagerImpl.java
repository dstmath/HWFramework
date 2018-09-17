package huawei.com.android.internal.os;

import android.util.Log;
import android.util.Xml;
import com.android.internal.os.IHwPowerProfileManager;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPowerProfileManagerImpl implements IHwPowerProfileManager {
    private static final String ATTR_NAME = "name";
    private static final String CUST_FILE_DIR = "/data/cust/xml";
    private static final String POWER_PROFILE_NAME = "power_profile.xml";
    private static final String SYSTEM_FILE_DIR = "/system/etc/xml";
    private static final String TAG = "PowerProfile";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    private static IHwPowerProfileManager mHwPowerProfileManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.internal.os.HwPowerProfileManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.internal.os.HwPowerProfileManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.internal.os.HwPowerProfileManagerImpl.<clinit>():void");
    }

    public static IHwPowerProfileManager getDefault() {
        if (mHwPowerProfileManager == null) {
            mHwPowerProfileManager = new HwPowerProfileManagerImpl();
        }
        return mHwPowerProfileManager;
    }

    public boolean readHwPowerValuesFromXml(HashMap<String, Object> sPowerMap) {
        if (readHwPowerValuesFromXml(sPowerMap, CUST_FILE_DIR, POWER_PROFILE_NAME) || readHwPowerValuesFromXml(sPowerMap, SYSTEM_FILE_DIR, POWER_PROFILE_NAME)) {
            return true;
        }
        return false;
    }

    private boolean readHwPowerValuesFromXml(HashMap<String, Object> sPowerMap, String fileDir, String fileName) {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        File powerProfile = HwCfgFilePolicy.getCfgFile("xml/" + fileName, 0);
        if (powerProfile == null) {
            Log.w(TAG, "power_profile.xml not found! ");
            return false;
        }
        Log.v(TAG, powerProfile.getAbsolutePath() + " be read ! ");
        InputStream inputStream = null;
        if (powerProfile.canRead()) {
            try {
                InputStream inputStream2 = new FileInputStream(powerProfile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inputStream2, null);
                    boolean parsingArray = false;
                    ArrayList<Double> array = new ArrayList();
                    Object obj = null;
                    while (true) {
                        XmlUtils.nextElement(parser);
                        String element = parser.getName();
                        if (element == null) {
                            break;
                        }
                        if (parsingArray) {
                            if (!element.equals(TAG_ARRAYITEM)) {
                                sPowerMap.put(obj, array.toArray(new Double[array.size()]));
                                parsingArray = false;
                            }
                        }
                        if (element.equals(TAG_ARRAY)) {
                            parsingArray = true;
                            array.clear();
                            obj = parser.getAttributeValue(null, ATTR_NAME);
                        } else if (element.equals(TAG_ITEM) || element.equals(TAG_ARRAYITEM)) {
                            Object name = null;
                            if (!parsingArray) {
                                name = parser.getAttributeValue(null, ATTR_NAME);
                            }
                            if (parser.next() == 4) {
                                double value = 0.0d;
                                try {
                                    value = Double.valueOf(parser.getText()).doubleValue();
                                } catch (NumberFormatException e3) {
                                }
                                if (element.equals(TAG_ITEM)) {
                                    sPowerMap.put(name, Double.valueOf(value));
                                } else if (parsingArray) {
                                    array.add(Double.valueOf(value));
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                    if (parsingArray) {
                        sPowerMap.put(obj, array.toArray(new Double[array.size()]));
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e4) {
                            throw new RuntimeException(e4);
                        }
                    }
                    Log.w(TAG, fileDir + "/" + POWER_PROFILE_NAME + " be read ! ");
                    return true;
                } catch (XmlPullParserException e5) {
                    e2 = e5;
                    inputStream = inputStream2;
                } catch (IOException e6) {
                    e4 = e6;
                    inputStream = inputStream2;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = inputStream2;
                }
            } catch (XmlPullParserException e7) {
                e2 = e7;
                try {
                    throw new RuntimeException(e2);
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e42) {
                            throw new RuntimeException(e42);
                        }
                    }
                    throw th;
                }
            } catch (IOException e8) {
                e42 = e8;
                throw new RuntimeException(e42);
            }
        }
        Log.w(TAG, "power_profile.xml 11111not found! power profile maybe not right!");
        return false;
    }
}
