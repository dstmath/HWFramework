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
    private static final String TAG_ITEM = "item";
    private static IHwPowerProfileManager mHwPowerProfileManager = null;

    public static IHwPowerProfileManager getDefault() {
        if (mHwPowerProfileManager == null) {
            mHwPowerProfileManager = new HwPowerProfileManagerImpl();
        }
        return mHwPowerProfileManager;
    }

    public boolean readHwPowerValuesFromXml(HashMap<String, Double> powerItemMap, HashMap<String, Double[]> powerArrayMap) {
        if (readHwPowerValuesFromXml(powerItemMap, powerArrayMap, CUST_FILE_DIR, POWER_PROFILE_NAME) || readHwPowerValuesFromXml(powerItemMap, powerArrayMap, SYSTEM_FILE_DIR, POWER_PROFILE_NAME)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x017f A[SYNTHETIC, Splitter:B:82:0x017f] */
    private boolean readHwPowerValuesFromXml(HashMap<String, Double> powerItemMap, HashMap<String, Double[]> powerArrayMap, String fileDir, String fileName) {
        XmlPullParserException e;
        IOException e2;
        boolean isParsingArray;
        XmlPullParser parser;
        String name;
        HashMap<String, Double[]> hashMap = powerArrayMap;
        File powerProfile = HwCfgFilePolicy.getCfgFile("xml/" + fileName, 0);
        if (powerProfile == null) {
            Log.w(TAG, "power_profile.xml not found! ");
            return false;
        }
        Log.v(TAG, powerProfile.getAbsolutePath() + " be read ! ");
        InputStream inputStream = null;
        if (powerProfile.canRead()) {
            try {
                inputStream = new FileInputStream(powerProfile);
                XmlPullParser parser2 = Xml.newPullParser();
                parser2.setInput(inputStream, null);
                boolean isParsingArray2 = false;
                ArrayList<Double> array = new ArrayList<>();
                String arrayName = null;
                while (true) {
                    XmlUtils.nextElement(parser2);
                    String element = parser2.getName();
                    if (element == null) {
                        break;
                    }
                    if (isParsingArray2) {
                        try {
                            if (!element.equals(TAG_ARRAYITEM) && arrayName != null) {
                                hashMap.put(arrayName, (Double[]) array.toArray(new Double[array.size()]));
                                isParsingArray = false;
                                if (!element.equals(TAG_ARRAY)) {
                                    isParsingArray2 = true;
                                    array.clear();
                                    arrayName = parser2.getAttributeValue(null, ATTR_NAME);
                                    parser = parser2;
                                } else {
                                    if (element.equals(TAG_ITEM) || element.equals(TAG_ARRAYITEM)) {
                                        if (!isParsingArray) {
                                            name = parser2.getAttributeValue(null, ATTR_NAME);
                                        } else {
                                            name = null;
                                        }
                                        if (parser2.next() == 4) {
                                            double value = 0.0d;
                                            try {
                                                value = Double.valueOf(parser2.getText()).doubleValue();
                                            } catch (NumberFormatException e3) {
                                                Log.e(TAG, "there is a NumberFormatException");
                                            }
                                            if (name == null || !element.equals(TAG_ITEM)) {
                                                parser = parser2;
                                                if (isParsingArray) {
                                                    array.add(Double.valueOf(value));
                                                }
                                            } else {
                                                parser = parser2;
                                                try {
                                                    powerItemMap.put(name, Double.valueOf(value));
                                                } catch (XmlPullParserException e4) {
                                                    e = e4;
                                                    throw new RuntimeException(e);
                                                } catch (IOException e5) {
                                                    e2 = e5;
                                                    throw new RuntimeException(e2);
                                                }
                                            }
                                        } else {
                                            parser = parser2;
                                        }
                                    } else {
                                        parser = parser2;
                                    }
                                    isParsingArray2 = isParsingArray;
                                }
                                hashMap = powerArrayMap;
                                parser2 = parser;
                            }
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            throw new RuntimeException(e);
                        } catch (IOException e7) {
                            e2 = e7;
                            throw new RuntimeException(e2);
                        } catch (Throwable th) {
                            e = th;
                            if (inputStream != null) {
                            }
                            throw e;
                        }
                    }
                    isParsingArray = isParsingArray2;
                    if (!element.equals(TAG_ARRAY)) {
                    }
                    hashMap = powerArrayMap;
                    parser2 = parser;
                }
                if (arrayName != null && isParsingArray2) {
                    hashMap.put(arrayName, (Double[]) array.toArray(new Double[array.size()]));
                }
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    Log.e(TAG, "Fail to close!");
                }
                Log.w(TAG, fileDir + "/" + POWER_PROFILE_NAME + " be read ! ");
                return true;
            } catch (XmlPullParserException e9) {
                e = e9;
                throw new RuntimeException(e);
            } catch (IOException e10) {
                e2 = e10;
                throw new RuntimeException(e2);
            } catch (Throwable th2) {
                e = th2;
                if (inputStream != null) {
                }
                throw e;
            }
        } else {
            Log.w(TAG, "power_profile.xml 11111not found! power profile maybe not right!");
            return false;
        }
    }
}
