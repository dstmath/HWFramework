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

    public boolean readHwPowerValuesFromXml(HashMap<String, Double> sPowerItemMap, HashMap<String, Double[]> sPowerArrayMap) {
        if (readHwPowerValuesFromXml(sPowerItemMap, sPowerArrayMap, CUST_FILE_DIR, POWER_PROFILE_NAME) || readHwPowerValuesFromXml(sPowerItemMap, sPowerArrayMap, SYSTEM_FILE_DIR, POWER_PROFILE_NAME)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:84:0x017b A[SYNTHETIC, Splitter:B:84:0x017b] */
    private boolean readHwPowerValuesFromXml(HashMap<String, Double> sPowerItemMap, HashMap<String, Double[]> sPowerArrayMap, String fileDir, String fileName) {
        String arrayName;
        String name;
        HashMap<String, Double[]> hashMap = sPowerArrayMap;
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
                XmlPullParser parser = Xml.newPullParser();
                String str = null;
                parser.setInput(inputStream, null);
                ArrayList<Double> array = new ArrayList<>();
                boolean parsingArray = false;
                String arrayName2 = null;
                while (true) {
                    arrayName = arrayName2;
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        break;
                    }
                    String str2 = fileDir;
                    if (parsingArray) {
                        try {
                            if (!element.equals("value")) {
                                hashMap.put(arrayName, (Double[]) array.toArray(new Double[array.size()]));
                                parsingArray = false;
                            }
                        } catch (XmlPullParserException e) {
                            e = e;
                            HashMap<String, Double> hashMap2 = sPowerItemMap;
                            throw new RuntimeException(e);
                        } catch (IOException e2) {
                            e = e2;
                            HashMap<String, Double> hashMap3 = sPowerItemMap;
                            throw new RuntimeException(e);
                        } catch (Throwable th) {
                            e = th;
                            HashMap<String, Double> hashMap4 = sPowerItemMap;
                            XmlPullParserException xmlPullParserException = e;
                            if (inputStream != null) {
                            }
                            throw xmlPullParserException;
                        }
                    }
                    if (element.equals(TAG_ARRAY)) {
                        array.clear();
                        String arrayName3 = parser.getAttributeValue(str, ATTR_NAME);
                        HashMap<String, Double> hashMap5 = sPowerItemMap;
                        String str3 = arrayName3;
                        parsingArray = true;
                        arrayName2 = str3;
                    } else {
                        if (!element.equals(TAG_ITEM)) {
                            if (element.equals("value")) {
                            }
                            HashMap<String, Double> hashMap6 = sPowerItemMap;
                            arrayName2 = arrayName;
                        }
                        if (!parsingArray) {
                            name = parser.getAttributeValue(str, ATTR_NAME);
                        } else {
                            name = null;
                        }
                        if (parser.next() == 4) {
                            double value = 0.0d;
                            try {
                                value = Double.valueOf(parser.getText()).doubleValue();
                            } catch (NumberFormatException nfe) {
                                NumberFormatException numberFormatException = nfe;
                                Log.e(TAG, "there is a NumberFormatException");
                            }
                            if (element.equals(TAG_ITEM)) {
                                try {
                                    sPowerItemMap.put(name, Double.valueOf(value));
                                } catch (XmlPullParserException e3) {
                                    e = e3;
                                    throw new RuntimeException(e);
                                } catch (IOException e4) {
                                    e = e4;
                                    throw new RuntimeException(e);
                                }
                            } else {
                                HashMap<String, Double> hashMap7 = sPowerItemMap;
                                if (parsingArray) {
                                    array.add(Double.valueOf(value));
                                }
                            }
                            arrayName2 = arrayName;
                        }
                        HashMap<String, Double> hashMap62 = sPowerItemMap;
                        arrayName2 = arrayName;
                    }
                    str = null;
                }
                if (parsingArray) {
                    hashMap.put(arrayName, (Double[]) array.toArray(new Double[array.size()]));
                }
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    IOException iOException = e5;
                    Log.e(TAG, "Fail to close!", e5);
                }
                Log.w(TAG, fileDir + "/" + POWER_PROFILE_NAME + " be read ! ");
                return true;
            } catch (XmlPullParserException e6) {
                e = e6;
                HashMap<String, Double> hashMap8 = sPowerItemMap;
                String str4 = fileDir;
                throw new RuntimeException(e);
            } catch (IOException e7) {
                e = e7;
                HashMap<String, Double> hashMap9 = sPowerItemMap;
                String str5 = fileDir;
                throw new RuntimeException(e);
            } catch (Throwable th2) {
                e = th2;
                XmlPullParserException xmlPullParserException2 = e;
                if (inputStream != null) {
                }
                throw xmlPullParserException2;
            }
        } else {
            HashMap<String, Double> hashMap10 = sPowerItemMap;
            String str6 = fileDir;
            Log.w(TAG, "power_profile.xml 11111not found! power profile maybe not right!");
            return false;
        }
    }
}
