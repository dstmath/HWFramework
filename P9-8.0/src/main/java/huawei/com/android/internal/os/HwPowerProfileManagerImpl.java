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
    private static IHwPowerProfileManager mHwPowerProfileManager = null;

    public static IHwPowerProfileManager getDefault() {
        if (mHwPowerProfileManager == null) {
            mHwPowerProfileManager = new HwPowerProfileManagerImpl();
        }
        return mHwPowerProfileManager;
    }

    public boolean readHwPowerValuesFromXml(HashMap<String, Object> sPowerMap) {
        if (readHwPowerValuesFromXml(sPowerMap, CUST_FILE_DIR, POWER_PROFILE_NAME) || (readHwPowerValuesFromXml(sPowerMap, SYSTEM_FILE_DIR, POWER_PROFILE_NAME) ^ 1) == 0) {
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
                    Object arrayName = null;
                    while (true) {
                        XmlUtils.nextElement(parser);
                        String element = parser.getName();
                        if (element == null) {
                            if (parsingArray) {
                                sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
                            }
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (IOException e3) {
                                    throw new RuntimeException(e3);
                                }
                            }
                            Log.w(TAG, fileDir + "/" + POWER_PROFILE_NAME + " be read ! ");
                            return true;
                        }
                        if (parsingArray) {
                            if ((element.equals("value") ^ 1) != 0) {
                                sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
                                parsingArray = false;
                            }
                        }
                        if (element.equals(TAG_ARRAY)) {
                            parsingArray = true;
                            array.clear();
                            arrayName = parser.getAttributeValue(null, ATTR_NAME);
                        } else if (element.equals(TAG_ITEM) || element.equals("value")) {
                            Object name = null;
                            if (!parsingArray) {
                                name = parser.getAttributeValue(null, ATTR_NAME);
                            }
                            if (parser.next() == 4) {
                                double value = 0.0d;
                                try {
                                    value = Double.valueOf(parser.getText()).doubleValue();
                                } catch (NumberFormatException e4) {
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
                } catch (XmlPullParserException e5) {
                    e2 = e5;
                    inputStream = inputStream2;
                } catch (IOException e6) {
                    e3 = e6;
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
                        } catch (IOException e32) {
                            throw new RuntimeException(e32);
                        }
                    }
                    throw th;
                }
            } catch (IOException e8) {
                e32 = e8;
                throw new RuntimeException(e32);
            }
        }
        Log.w(TAG, "power_profile.xml 11111not found! power profile maybe not right!");
        return false;
    }
}
