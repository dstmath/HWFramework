package com.android.server.policy;

import android.util.Log;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class EasyWakeUpXmlParse {
    public static final String Cover_Screen = "Cover_Screen";
    private static final boolean DEBUG = false;
    public static final String Double_Touch = "Double_Touch";
    private static final int EASYWAKE_ENABLE_DEFAULT_VALUES = 0;
    private static final int EASYWAKE_ENABLE_SURPPORT_VALUSE = 0;
    public static final String EasyWakeUp_Flick_ALL = "EasyWakeUp_Flick_ALL";
    public static final String EasyWakeUp_Flick_DOWN = "EasyWakeUp_Flick_Down";
    public static final String EasyWakeUp_Flick_LEFT = "EasyWakeUp_Flick_left";
    public static final String EasyWakeUp_Flick_RIGHT = "EasyWakeUp_Flick_Right";
    public static final String EasyWakeUp_Flick_UP = "EasyWakeUp_Flick_Up";
    public static final String EasyWakeUp_Letter_ALL = "EasyWakeUp_Letter_ALL";
    public static final String EasyWakeUp_Letter_C = "EasyWakeUp_Letter_C";
    public static final String EasyWakeUp_Letter_E = "EasyWakeUp_Letter_E";
    public static final String EasyWakeUp_Letter_M = "EasyWakeUp_Letter_M";
    public static final String EasyWakeUp_Letter_W = "EasyWakeUp_Letter_W";
    private static final String HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH = "/data/cust/xml/hw_easywakeupmotion_config.xml";
    private static final String HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH = "/system/etc/xml/hw_easywakeupmotion_config.xml";
    private static String TAG = "EasyWakeUpXmlParse";
    private static int coverScreenCode = -1;
    private static int coverScreenIndex = -1;
    private static int doubleTouchCode = -1;
    private static int doubleTouchIndex = -1;
    private static int driverFileLength = 0;
    private static String driverGesturePath = "";
    private static String driverPostionPath = "";
    private static int flickALLIndex = -1;
    private static int flickDownCode = -1;
    private static int flickDownIndex = -1;
    private static int flickLeftCode = -1;
    private static int flickLeftIndex = -1;
    private static int flickRightCode = -1;
    private static int flickRightIndex = -1;
    private static int flickUpCode = -1;
    private static int flickUpIndex = -1;
    private static List<HwEasyWakeUpDate> hweasywakeupdates = null;
    private static int letterAllIndex = -1;
    private static int letterCCode = -1;
    private static int letterCIndex = -1;
    private static int letterECode = -1;
    private static int letterEIndex = -1;
    private static int letterMCode = -1;
    private static int letterMIndex = -1;
    private static int letterWCode = -1;
    private static int letterWIndex = -1;
    private static int maxKeyCode = -1;
    private static int minKeyCode = -1;
    private static int powerOptimize = 0;
    private static int sensorCheckTimes = 6;
    private static float sensorFarValue = 5.0f;
    private static float sensorNearValue = 0.0f;
    private static long sensorWatchTime = 50;

    public static class HwEasyWakeUpDate {
        private int flag = 0;
        private String name = "";
        private int support = 0;
        private int value = 0;

        public HwEasyWakeUpDate(String name, int support, int value, int flag) {
            this.name = name;
            this.support = support;
            this.value = value;
            this.flag = flag;
        }

        public String getName() {
            return this.name;
        }

        public int getValue() {
            return this.value;
        }

        public int getSupport() {
            return this.support;
        }

        public int getFlag() {
            return this.flag;
        }
    }

    public static int getDefaultSupportValueFromCust() {
        int defaultsupportvalue = 0;
        if (hweasywakeupdates == null) {
            try {
                parseHwEasyWakeUpdatesFile();
            } catch (Exception e) {
                Log.e(TAG, "used default support value = 0");
                return 0;
            }
        }
        if (hweasywakeupdates != null) {
            for (HwEasyWakeUpDate hweasywakeupdate : hweasywakeupdates) {
                int support = hweasywakeupdate.getSupport();
                int flag = hweasywakeupdate.getFlag();
                if (flag < 20) {
                    defaultsupportvalue += support << flag;
                }
            }
        }
        return defaultsupportvalue;
    }

    public static int getDefaultValueFromCust() {
        int defaultvalue = 0;
        if (hweasywakeupdates == null) {
            try {
                parseHwEasyWakeUpdatesFile();
            } catch (Exception e) {
                Log.e(TAG, "used default value = 0");
                return 0;
            }
        }
        if (hweasywakeupdates != null) {
            for (HwEasyWakeUpDate hweasywakeupdate : hweasywakeupdates) {
                defaultvalue += hweasywakeupdate.getValue() << hweasywakeupdate.getFlag();
            }
        }
        return defaultvalue;
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:82:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a7  */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a7  */
    /* JADX WARNING: Removed duplicated region for block: B:82:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0088  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void parseHwEasyWakeUpdatesFile() throws Exception {
        File mCustFile;
        Throwable th;
        InputStream inputstream;
        InputStream inputstream2 = null;
        File mCustFile2;
        try {
            mCustFile = HwCfgFilePolicy.getCfgFile("xml/hw_easywakeupmotion_config.xml", 0);
            if (mCustFile == null) {
                try {
                } catch (FileNotFoundException e) {
                } catch (XmlPullParserException e2) {
                    Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
                    if (inputstream2 != null) {
                    }
                } catch (IOException e3) {
                    Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
                    if (inputstream2 != null) {
                    }
                } catch (Exception e4) {
                    throw new Exception("Exception");
                }
                try {
                    if (!mCustFile2.exists()) {
                        mCustFile = new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
                    }
                    mCustFile = mCustFile2;
                } catch (FileNotFoundException e5) {
                    mCustFile = mCustFile2;
                    try {
                        Log.e(TAG, "hw_easywakeupmotion_config.xml FileNotFoundException");
                        if (inputstream2 == null) {
                            inputstream2.close();
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputstream2 != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    mCustFile = mCustFile2;
                    Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
                    if (inputstream2 != null) {
                        inputstream2.close();
                    }
                    return;
                } catch (IOException e7) {
                    mCustFile = mCustFile2;
                    Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
                    if (inputstream2 != null) {
                        inputstream2.close();
                    }
                    return;
                } catch (Exception e8) {
                    mCustFile = mCustFile2;
                    throw new Exception("Exception");
                } catch (Throwable th3) {
                    th = th3;
                    if (inputstream2 != null) {
                        inputstream2.close();
                    }
                    throw th;
                }
            }
        } catch (NoClassDefFoundError e9) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (!mCustFile2.exists()) {
                mCustFile = new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
                inputstream = new FileInputStream(mCustFile);
                parseHwEasyWakeUpDate(inputstream);
                if (inputstream != null) {
                }
            }
            mCustFile = mCustFile2;
            inputstream = new FileInputStream(mCustFile);
            parseHwEasyWakeUpDate(inputstream);
            if (inputstream != null) {
            }
        } finally {
            mCustFile2 = new File(HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH);
            if (mCustFile2.exists()) {
            } else {
                mCustFile = new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
            }
        }
        inputstream = new FileInputStream(mCustFile);
        try {
            parseHwEasyWakeUpDate(inputstream);
            if (inputstream != null) {
                inputstream.close();
            }
        } catch (FileNotFoundException e10) {
            inputstream2 = inputstream;
            Log.e(TAG, "hw_easywakeupmotion_config.xml FileNotFoundException");
            if (inputstream2 == null) {
            }
        } catch (XmlPullParserException e11) {
            inputstream2 = inputstream;
            Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
            if (inputstream2 != null) {
            }
        } catch (IOException e12) {
            inputstream2 = inputstream;
            Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
            if (inputstream2 != null) {
            }
        } catch (Exception e13) {
            inputstream2 = inputstream;
            throw new Exception("Exception");
        } catch (Throwable th4) {
            th = th4;
            inputstream2 = inputstream;
            if (inputstream2 != null) {
            }
            throw th;
        }
    }

    private static void parseHwEasyWakeUpDate(InputStream inputstream) throws XmlPullParserException, IOException {
        Object hweasywakeupdate = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputstream, "UTF-8");
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            switch (event) {
                case 0:
                    hweasywakeupdates = new ArrayList();
                    break;
                case 2:
                    if ("EasyWakeUpMotion".equals(pullParser.getName())) {
                        String name = pullParser.getAttributeValue(0);
                        int support = Integer.parseInt(pullParser.getAttributeValue(1));
                        int value = Integer.parseInt(pullParser.getAttributeValue(2));
                        int flag = Integer.parseInt(pullParser.getAttributeValue(3));
                        getCodeAndIndexByName(name, Integer.parseInt(pullParser.getAttributeValue(4)), flag);
                        hweasywakeupdate = new HwEasyWakeUpDate(name, support, value, flag);
                    }
                    getValueByName(pullParser);
                    break;
                case 3:
                    if (!"EasyWakeUpMotion".equals(pullParser.getName())) {
                        break;
                    }
                    hweasywakeupdates.add(hweasywakeupdate);
                    hweasywakeupdate = null;
                    break;
                default:
                    break;
            }
        }
    }

    private static void getValueByName(XmlPullParser pullParser) {
        if ("MaxKeyCode".equals(pullParser.getName())) {
            maxKeyCode = Integer.parseInt(pullParser.getAttributeValue(null, "value"));
        }
        if ("MinKeyCode".equals(pullParser.getName())) {
            minKeyCode = Integer.parseInt(pullParser.getAttributeValue(null, "value"));
        }
        if ("DriverFileLength".equals(pullParser.getName())) {
            driverFileLength = Integer.parseInt(pullParser.getAttributeValue(null, "value"));
        }
        if ("DriverPostionPath".equals(pullParser.getName())) {
            driverPostionPath = String.valueOf(pullParser.getAttributeValue(null, "value"));
        }
        if ("DriverGesturePath".equals(pullParser.getName())) {
            driverGesturePath = String.valueOf(pullParser.getAttributeValue(null, "value"));
        }
        if ("SensorNear".equals(pullParser.getName())) {
            sensorNearValue = Float.parseFloat(pullParser.getAttributeValue(null, "value"));
        }
        if ("SensorFar".equals(pullParser.getName())) {
            sensorFarValue = Float.parseFloat(pullParser.getAttributeValue(null, "value"));
        }
        if ("SensorWatchTime".equals(pullParser.getName())) {
            sensorWatchTime = Long.parseLong(pullParser.getAttributeValue(null, "support"));
        }
        if ("SensorCheckTimes".equals(pullParser.getName())) {
            sensorCheckTimes = Integer.parseInt(pullParser.getAttributeValue(null, "support"));
        }
        if ("PowerOptimize".equals(pullParser.getName())) {
            powerOptimize = Integer.parseInt(pullParser.getAttributeValue(null, "support"));
        }
    }

    private static void getCodeAndIndexByName(String name, int keyCode, int flag) {
        if (Cover_Screen.equals(name)) {
            coverScreenCode = keyCode;
            coverScreenIndex = flag;
        }
        if (Double_Touch.equals(name)) {
            doubleTouchCode = keyCode;
            doubleTouchIndex = flag;
        }
        if (EasyWakeUp_Flick_ALL.equals(name)) {
            flickALLIndex = flag;
        }
        if (EasyWakeUp_Flick_UP.equals(name)) {
            flickUpCode = keyCode;
            flickUpIndex = flag;
        }
        if (EasyWakeUp_Flick_DOWN.equals(name)) {
            flickDownCode = keyCode;
            flickDownIndex = flag;
        }
        if (EasyWakeUp_Flick_LEFT.equals(name)) {
            flickLeftCode = keyCode;
            flickLeftIndex = flag;
        }
        if (EasyWakeUp_Flick_RIGHT.equals(name)) {
            flickRightCode = keyCode;
            flickRightIndex = flag;
        }
        if (EasyWakeUp_Letter_ALL.equals(name)) {
            letterAllIndex = flag;
        }
        if (EasyWakeUp_Letter_C.equals(name)) {
            letterCCode = keyCode;
            letterCIndex = flag;
        }
        if (EasyWakeUp_Letter_E.equals(name)) {
            letterECode = keyCode;
            letterEIndex = flag;
        }
        if (EasyWakeUp_Letter_M.equals(name)) {
            letterMCode = keyCode;
            letterMIndex = flag;
        }
        if (EasyWakeUp_Letter_W.equals(name)) {
            letterWCode = keyCode;
            letterWIndex = flag;
        }
    }

    public static int getKeyCodeByString(String str) {
        if ("maxKeyCode".equals(str)) {
            return maxKeyCode;
        }
        if ("minKeyCode".equals(str)) {
            return minKeyCode;
        }
        if (Cover_Screen.equals(str)) {
            return coverScreenCode;
        }
        if (Double_Touch.equals(str)) {
            return doubleTouchCode;
        }
        if (EasyWakeUp_Flick_UP.equals(str)) {
            return flickUpCode;
        }
        if (EasyWakeUp_Flick_DOWN.equals(str)) {
            return flickDownCode;
        }
        if (EasyWakeUp_Flick_LEFT.equals(str)) {
            return flickLeftCode;
        }
        if (EasyWakeUp_Flick_RIGHT.equals(str)) {
            return flickRightCode;
        }
        if (EasyWakeUp_Letter_C.equals(str)) {
            return letterCCode;
        }
        if (EasyWakeUp_Letter_E.equals(str)) {
            return letterECode;
        }
        if (EasyWakeUp_Letter_M.equals(str)) {
            return letterMCode;
        }
        if (EasyWakeUp_Letter_W.equals(str)) {
            return letterWCode;
        }
        return -1;
    }

    public static int getCoverScreenIndex() {
        return coverScreenIndex;
    }

    public static int getDoubleTouchIndex() {
        return doubleTouchIndex;
    }

    public static int getFlickAllIndex() {
        return flickALLIndex;
    }

    public static int getFlickUpIndex() {
        return flickUpIndex;
    }

    public static int getFlickDownEIndex() {
        return flickDownIndex;
    }

    public static int getFlickLeftIndex() {
        return flickLeftIndex;
    }

    public static int getFlickRightIndex() {
        return flickRightIndex;
    }

    public static int getLetterAllIndex() {
        return letterAllIndex;
    }

    public static int getLetterCIndex() {
        return letterCIndex;
    }

    public static int getLetterEIndex() {
        return letterEIndex;
    }

    public static int getLetterMIndex() {
        return letterMIndex;
    }

    public static int getLetterWIndex() {
        return letterWIndex;
    }

    public static int getDriverFileLength() {
        return driverFileLength;
    }

    public static String getDriverPostionPath() {
        return driverPostionPath;
    }

    public static String getDriverGesturePath() {
        return driverGesturePath;
    }

    public static float getSensorNearValue() {
        return sensorNearValue;
    }

    public static float getSensorFarValue() {
        return sensorFarValue;
    }

    public static long getSensorWatchTime() {
        return sensorWatchTime;
    }

    public static int getSensorCheckTimes() {
        return sensorCheckTimes;
    }

    public static int getPowerOptimizeState() {
        return powerOptimize;
    }
}
