package com.android.server.policy;

import android.util.Log;
import android.util.Xml;
import com.android.server.gesture.GestureNavConst;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
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
    private static float sensorNearValue = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private static long sensorWatchTime = 50;

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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0025, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0056, code lost:
        if (0 == 0) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0064, code lost:
        if (new java.io.File(HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH).exists() == false) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0066, code lost:
        new java.io.File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0079, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0082, code lost:
        throw new java.lang.Exception("Exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0083, code lost:
        android.util.Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008b, code lost:
        if (r1 != null) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008e, code lost:
        android.util.Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0096, code lost:
        if (r1 != null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0099, code lost:
        android.util.Log.e(TAG, "hw_easywakeupmotion_config.xml FileNotFoundException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00a1, code lost:
        if (r1 == null) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00a4, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00a6, code lost:
        if (r1 != null) goto L_0x00a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a8, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ac, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:5:0x000e, B:12:0x0028] */
    private static void parseHwEasyWakeUpdatesFile() throws Exception {
        File file;
        File mCustFile = null;
        FileInputStream fileInputStream = null;
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
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (0 == 0) {
                mCustFile = new File(HWEASYWAKEUP_MOTION_CONFIG_CUST_PATH);
                if (!mCustFile.exists()) {
                    file = new File(HWEASYWAKEUP_MOTION_CONFIG_SYSTEM_PATH);
                }
            }
        }
        fileInputStream = new FileInputStream(mCustFile);
        parseHwEasyWakeUpDate(fileInputStream);
        fileInputStream.close();
    }

    private static void parseHwEasyWakeUpDate(InputStream inputstream) throws XmlPullParserException, IOException {
        HwEasyWakeUpDate hweasywakeupdate = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputstream, "UTF-8");
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            if (event != 0) {
                switch (event) {
                    case 2:
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
                        break;
                    case 3:
                        if (!"EasyWakeUpMotion".equals(pullParser.getName())) {
                            break;
                        } else {
                            hweasywakeupdates.add(hweasywakeupdate);
                            hweasywakeupdate = null;
                            break;
                        }
                }
            } else {
                hweasywakeupdates = new ArrayList();
            }
        }
    }

    private static void getValueByName(XmlPullParser pullParser) {
        try {
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
        } catch (NumberFormatException e) {
            Log.e(TAG, "numberformat exception");
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
