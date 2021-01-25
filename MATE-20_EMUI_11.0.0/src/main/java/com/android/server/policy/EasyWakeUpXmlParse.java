package com.android.server.policy;

import android.util.Log;
import android.util.Xml;
import com.android.server.gesture.GestureNavConst;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class EasyWakeUpXmlParse {
    private static final String CONFIGURATION_SUPPORT_FIELD_NAME = "support";
    private static final String CONFIGURATION_VALUE_FIELD_NALE = "value";
    public static final String COVER_SCREEN = "Cover_Screen";
    private static final int DEFAULT_INDEX = -1;
    private static final int DEFAULT_KEY_CODE = -1;
    private static final int DEFAULT_SENSOR_CHECK_TIMES = 6;
    private static final float DEFAULT_SENSOR_FAR = 5.0f;
    private static final long DEFAULT_SENSOR_WATCH_TIME = 50;
    public static final String DOUBLE_TOUCH = "Double_Touch";
    public static final String EASYWAKEUP_FLICK_ALL = "EasyWakeUp_Flick_ALL";
    public static final String EASYWAKEUP_FLICK_DOWN = "EasyWakeUp_Flick_Down";
    public static final String EASYWAKEUP_FLICK_LEFT = "EasyWakeUp_Flick_left";
    public static final String EASYWAKEUP_FLICK_RIGHT = "EasyWakeUp_Flick_Right";
    public static final String EASYWAKEUP_FLICK_UP = "EasyWakeUp_Flick_Up";
    public static final String EASYWAKEUP_LETTER_ALL = "EasyWakeUp_Letter_ALL";
    public static final String EASYWAKEUP_LETTER_C = "EasyWakeUp_Letter_C";
    public static final String EASYWAKEUP_LETTER_E = "EasyWakeUp_Letter_E";
    public static final String EASYWAKEUP_LETTER_M = "EasyWakeUp_Letter_M";
    public static final String EASYWAKEUP_LETTER_W = "EasyWakeUp_Letter_W";
    private static final int EASYWAKE_ENABLE_DEFAULT_VALUES = 0;
    private static final int EASYWAKE_ENABLE_SURPPORT_VALUSE = 0;
    private static final String HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH = "/data/cust/xml/hw_easywakeupmotion_config.xml";
    private static final String HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH = "/system/etc/xml/hw_easywakeupmotion_config.xml";
    private static final int MAX_FLAG_BIT = 20;
    public static final String SINGLE_TOUCH = "Single_Touch";
    private static final String TAG = "EasyWakeUpXmlParse";
    private static final int XML_ATTRIBUTE_INDEX_0 = 0;
    private static final int XML_ATTRIBUTE_INDEX_1 = 1;
    private static final int XML_ATTRIBUTE_INDEX_2 = 2;
    private static final int XML_ATTRIBUTE_INDEX_3 = 3;
    private static final int XML_ATTRIBUTE_INDEX_4 = 4;
    private static int sCodeLetterC = -1;
    private static int sCodeLetterE = -1;
    private static int sCodeLetterM = -1;
    private static int sCodeLetterW = -1;
    private static int sCoverScreenCode = -1;
    private static int sCoverScreenIndex = -1;
    private static int sDoubleTouchCode = -1;
    private static int sDoubleTouchIndex = -1;
    private static int sDriverFileLength = 0;
    private static String sDriverGesturePath = "";
    private static String sDriverPostionPath = "";
    private static int sFlickAllIndex = -1;
    private static int sFlickDownCode = -1;
    private static int sFlickDownIndex = -1;
    private static int sFlickLeftCode = -1;
    private static int sFlickLeftIndex = -1;
    private static int sFlickRightCode = -1;
    private static int sFlickRightIndex = -1;
    private static int sFlickUpCode = -1;
    private static int sFlickUpIndex = -1;
    private static List<HwEasyWakeUpDate> sHwEasyWakeUpDates = null;
    private static int sIndexLetterC = -1;
    private static int sIndexLetterE = -1;
    private static int sIndexLetterM = -1;
    private static int sIndexLetterW = -1;
    private static int sLetterAllIndex = -1;
    private static int sMaxKeyCode = -1;
    private static int sMinKeyCode = -1;
    private static int sPowerOptimize = 0;
    private static int sSensorCheckTimes = 6;
    private static float sSensorFarValue = DEFAULT_SENSOR_FAR;
    private static float sSensorNearValue = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private static long sSensorWatchTime = DEFAULT_SENSOR_WATCH_TIME;
    private static int sSingleTouchCode = -1;
    private static int sSingleTouchIndex = -1;

    private EasyWakeUpXmlParse() {
        throw new RuntimeException("Tool class should not be initialized or generate an object");
    }

    public static int getDefaultSupportValueFromCust() {
        int defaultsupportvalue = 0;
        if (sHwEasyWakeUpDates == null) {
            parseHwEasyWakeUpdatesFile();
        }
        List<HwEasyWakeUpDate> list = sHwEasyWakeUpDates;
        if (list != null) {
            for (HwEasyWakeUpDate hweasywakeupdate : list) {
                int support = hweasywakeupdate.getSupport();
                int flag = hweasywakeupdate.getFlag();
                if (flag < 20) {
                    defaultsupportvalue += support << flag;
                }
            }
        }
        Log.i(TAG, "wakeup support value = " + defaultsupportvalue);
        return defaultsupportvalue;
    }

    public static int getDefaultValueFromCust() {
        int defaultvalue = 0;
        if (sHwEasyWakeUpDates == null) {
            parseHwEasyWakeUpdatesFile();
        }
        List<HwEasyWakeUpDate> list = sHwEasyWakeUpDates;
        if (list != null) {
            for (HwEasyWakeUpDate hweasywakeupdate : list) {
                defaultvalue += hweasywakeupdate.getValue() << hweasywakeupdate.getFlag();
            }
        }
        Log.i(TAG, "getDefaultValueFromCust wakeup value = " + defaultvalue);
        return defaultvalue;
    }

    public static boolean checkTouchSupport(String touchValue) {
        boolean z = false;
        if (!SINGLE_TOUCH.equals(touchValue) && !DOUBLE_TOUCH.equals(touchValue)) {
            return false;
        }
        boolean isSupportTouch = false;
        if (sHwEasyWakeUpDates == null) {
            parseHwEasyWakeUpdatesFile();
        }
        List<HwEasyWakeUpDate> list = sHwEasyWakeUpDates;
        if (list == null) {
            Log.e(TAG, "checkTouchSupport return false");
            return false;
        }
        Iterator<HwEasyWakeUpDate> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            HwEasyWakeUpDate hweasywakeupdate = it.next();
            if (touchValue.equals(hweasywakeupdate.getName())) {
                if (hweasywakeupdate.getSupport() == 1) {
                    z = true;
                }
                isSupportTouch = z;
            }
        }
        Log.i(TAG, "checkTouchSupport touchValue:" + touchValue + ",isSupportTouch:" + isSupportTouch);
        return isSupportTouch;
    }

    public static int getTouchEnableValue(String touchValue) {
        if (!SINGLE_TOUCH.equals(touchValue) && !DOUBLE_TOUCH.equals(touchValue)) {
            return 0;
        }
        if (sHwEasyWakeUpDates == null) {
            parseHwEasyWakeUpdatesFile();
        }
        List<HwEasyWakeUpDate> list = sHwEasyWakeUpDates;
        if (list == null) {
            Log.e(TAG, "getTouchEnableValue return 0");
            return 0;
        }
        int result = 0;
        Iterator<HwEasyWakeUpDate> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            HwEasyWakeUpDate hweasywakeupdate = it.next();
            if (touchValue.equals(hweasywakeupdate.getName())) {
                result = 1 << hweasywakeupdate.getFlag();
                break;
            }
        }
        Log.i(TAG, "getTouchEnableValue touchValue:" + touchValue + ",enable:" + result);
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0054, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0055, code lost:
        r1.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0058, code lost:
        throw r4;
     */
    private static void parseHwEasyWakeUpdatesFile() {
        File file;
        File mCustFile = null;
        try {
            mCustFile = HwCfgFilePolicy.getCfgFile("xml/hw_easywakeupmotion_config.xml", 0);
            if (mCustFile == null) {
                mCustFile = new File(HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH);
                if (!mCustFile.exists()) {
                    file = new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
                    mCustFile = file;
                }
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (0 == 0) {
                mCustFile = new File(HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH);
                if (!mCustFile.exists()) {
                    file = new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
                }
            }
        } catch (Throwable th) {
            if (0 == 0 && !new File(HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH).exists()) {
                new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
            }
            throw th;
        }
        try {
            InputStream inputstream = new FileInputStream(mCustFile);
            parseHwEasyWakeUpDate(inputstream);
            inputstream.close();
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "hw_easywakeupmotion_config.xml FileNotFoundException");
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
        } catch (IOException e4) {
            Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
        }
    }

    private static void parseHwEasyWakeUpDate(InputStream inputstream) throws XmlPullParserException, IOException {
        HwEasyWakeUpDate hweasywakeupdate = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputstream, "UTF-8");
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            if (event == 0) {
                sHwEasyWakeUpDates = new ArrayList();
            } else if (event == 2) {
                if ("EasyWakeUpMotion".equals(pullParser.getName())) {
                    String name = pullParser.getAttributeValue(0);
                    try {
                        int support = Integer.parseInt(pullParser.getAttributeValue(1));
                        int value = Integer.parseInt(pullParser.getAttributeValue(2));
                        int flag = Integer.parseInt(pullParser.getAttributeValue(3));
                        getCodeAndIndexByName(name, Integer.parseInt(pullParser.getAttributeValue(4)), flag);
                        hweasywakeupdate = new HwEasyWakeUpDate(name, support, value, flag);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "numberformat exception");
                    }
                }
                getValueByName(pullParser);
            } else if (event == 3 && "EasyWakeUpMotion".equals(pullParser.getName())) {
                sHwEasyWakeUpDates.add(hweasywakeupdate);
                hweasywakeupdate = null;
            }
        }
    }

    private static void getValueByName(XmlPullParser pullParser) {
        try {
            if ("MaxKeyCode".equals(pullParser.getName())) {
                sMaxKeyCode = Integer.parseInt(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("MinKeyCode".equals(pullParser.getName())) {
                sMinKeyCode = Integer.parseInt(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("DriverFileLength".equals(pullParser.getName())) {
                sDriverFileLength = Integer.parseInt(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("DriverPostionPath".equals(pullParser.getName())) {
                sDriverPostionPath = String.valueOf(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("DriverGesturePath".equals(pullParser.getName())) {
                sDriverGesturePath = String.valueOf(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("SensorNear".equals(pullParser.getName())) {
                sSensorNearValue = Float.parseFloat(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("SensorFar".equals(pullParser.getName())) {
                sSensorFarValue = Float.parseFloat(pullParser.getAttributeValue(null, CONFIGURATION_VALUE_FIELD_NALE));
            }
            if ("SensorWatchTime".equals(pullParser.getName())) {
                sSensorWatchTime = Long.parseLong(pullParser.getAttributeValue(null, CONFIGURATION_SUPPORT_FIELD_NAME));
            }
            if ("SensorCheckTimes".equals(pullParser.getName())) {
                sSensorCheckTimes = Integer.parseInt(pullParser.getAttributeValue(null, CONFIGURATION_SUPPORT_FIELD_NAME));
            }
            if ("PowerOptimize".equals(pullParser.getName())) {
                sPowerOptimize = Integer.parseInt(pullParser.getAttributeValue(null, CONFIGURATION_SUPPORT_FIELD_NAME));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "numberformat exception");
        }
    }

    private static void getCodeAndIndexByName(String name, int keyCode, int flag) {
        if (COVER_SCREEN.equals(name)) {
            sCoverScreenCode = keyCode;
            sCoverScreenIndex = flag;
        } else if (DOUBLE_TOUCH.equals(name)) {
            sDoubleTouchCode = keyCode;
            sDoubleTouchIndex = flag;
        } else if (SINGLE_TOUCH.equals(name)) {
            sSingleTouchCode = keyCode;
            sSingleTouchIndex = flag;
        } else if (EASYWAKEUP_FLICK_ALL.equals(name)) {
            sFlickAllIndex = flag;
        } else if (EASYWAKEUP_FLICK_UP.equals(name)) {
            sFlickUpCode = keyCode;
            sFlickUpIndex = flag;
        } else if (EASYWAKEUP_FLICK_DOWN.equals(name)) {
            sFlickDownCode = keyCode;
            sFlickDownIndex = flag;
        } else if (EASYWAKEUP_FLICK_LEFT.equals(name)) {
            sFlickLeftCode = keyCode;
            sFlickLeftIndex = flag;
        } else if (EASYWAKEUP_FLICK_RIGHT.equals(name)) {
            sFlickRightCode = keyCode;
            sFlickRightIndex = flag;
        } else if (EASYWAKEUP_LETTER_ALL.equals(name)) {
            sLetterAllIndex = flag;
        } else if (EASYWAKEUP_LETTER_C.equals(name)) {
            sCodeLetterC = keyCode;
            sIndexLetterC = flag;
        } else if (EASYWAKEUP_LETTER_E.equals(name)) {
            sCodeLetterE = keyCode;
            sIndexLetterE = flag;
        } else if (EASYWAKEUP_LETTER_M.equals(name)) {
            sCodeLetterM = keyCode;
            sIndexLetterM = flag;
        } else if (EASYWAKEUP_LETTER_W.equals(name)) {
            sCodeLetterW = keyCode;
            sIndexLetterW = flag;
        }
    }

    public static int getKeyCodeByString(String str) {
        if ("maxKeyCode".equals(str)) {
            return sMaxKeyCode;
        }
        if ("minKeyCode".equals(str)) {
            return sMinKeyCode;
        }
        if (COVER_SCREEN.equals(str)) {
            return sCoverScreenCode;
        }
        if (DOUBLE_TOUCH.equals(str)) {
            return sDoubleTouchCode;
        }
        if (SINGLE_TOUCH.equals(str)) {
            return sSingleTouchCode;
        }
        if (EASYWAKEUP_FLICK_UP.equals(str)) {
            return sFlickUpCode;
        }
        if (EASYWAKEUP_FLICK_DOWN.equals(str)) {
            return sFlickDownCode;
        }
        if (EASYWAKEUP_FLICK_LEFT.equals(str)) {
            return sFlickLeftCode;
        }
        if (EASYWAKEUP_FLICK_RIGHT.equals(str)) {
            return sFlickRightCode;
        }
        if (EASYWAKEUP_LETTER_C.equals(str)) {
            return sCodeLetterC;
        }
        if (EASYWAKEUP_LETTER_E.equals(str)) {
            return sCodeLetterE;
        }
        if (EASYWAKEUP_LETTER_M.equals(str)) {
            return sCodeLetterM;
        }
        if (EASYWAKEUP_LETTER_W.equals(str)) {
            return sCodeLetterW;
        }
        return -1;
    }

    public static int getCoverScreenIndex() {
        return sCoverScreenIndex;
    }

    public static int getDoubleTouchIndex() {
        return sDoubleTouchIndex;
    }

    public static int getSingleTouchIndex() {
        return sSingleTouchIndex;
    }

    public static int getIndexOfFlickAll() {
        return sFlickAllIndex;
    }

    public static int getIndexOfFlickUp() {
        return sFlickUpIndex;
    }

    public static int getIndexOfFlickDownE() {
        return sFlickDownIndex;
    }

    public static int getIndexOfFlickLeft() {
        return sFlickLeftIndex;
    }

    public static int getIndexOfFlickRight() {
        return sFlickRightIndex;
    }

    public static int getIndexOfLetterAll() {
        return sLetterAllIndex;
    }

    public static int getIndexOfLetterC() {
        return sIndexLetterC;
    }

    public static int getIndexOfLetterE() {
        return sIndexLetterE;
    }

    public static int getIndexOfLetterM() {
        return sIndexLetterM;
    }

    public static int getIndexOfLetterW() {
        return sIndexLetterW;
    }

    public static int getDriverFileLength() {
        return sDriverFileLength;
    }

    public static String getDriverPostionPath() {
        return sDriverPostionPath;
    }

    public static String getDriverGesturePath() {
        return sDriverGesturePath;
    }

    public static float getSensorNearValue() {
        return sSensorNearValue;
    }

    public static float getSensorFarValue() {
        return sSensorFarValue;
    }

    public static long getSensorWatchTime() {
        return sSensorWatchTime;
    }

    public static int getSensorCheckTimes() {
        return sSensorCheckTimes;
    }

    public static int getPowerOptimizeState() {
        return sPowerOptimize;
    }

    public static class HwEasyWakeUpDate {
        private int flag = 0;
        private String name = "";
        private int support = 0;
        private int value = 0;

        public HwEasyWakeUpDate(String name2, int support2, int value2, int flag2) {
            this.name = name2;
            this.support = support2;
            this.value = value2;
            this.flag = flag2;
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
}
