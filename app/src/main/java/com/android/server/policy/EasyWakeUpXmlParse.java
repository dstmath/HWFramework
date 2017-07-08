package com.android.server.policy;

import android.util.Log;
import android.util.Xml;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
    private static String TAG;
    private static int coverScreenCode;
    private static int coverScreenIndex;
    private static int doubleTouchCode;
    private static int doubleTouchIndex;
    private static String driverControlPath;
    private static int driverFileLength;
    private static String driverGesturePath;
    private static String driverPostionPath;
    private static int flickALLIndex;
    private static int flickDownCode;
    private static int flickDownIndex;
    private static int flickLeftCode;
    private static int flickLeftIndex;
    private static int flickRightCode;
    private static int flickRightIndex;
    private static int flickUpCode;
    private static int flickUpIndex;
    private static List<HwEasyWakeUpDate> hweasywakeupdates;
    private static int letterAllIndex;
    private static int letterCCode;
    private static int letterCIndex;
    private static int letterECode;
    private static int letterEIndex;
    private static int letterMCode;
    private static int letterMIndex;
    private static int letterWCode;
    private static int letterWIndex;
    private static int maxKeyCode;
    private static int minKeyCode;
    private static int powerOptimize;
    private static int sensorCheckTimes;
    private static float sensorFarValue;
    private static float sensorNearValue;
    private static long sensorWatchTime;

    public static class HwEasyWakeUpDate {
        private int flag;
        private String name;
        private int support;
        private int value;

        public HwEasyWakeUpDate(String name, int support, int value, int flag) {
            this.name = AppHibernateCst.INVALID_PKG;
            this.value = EasyWakeUpXmlParse.EASYWAKE_ENABLE_SURPPORT_VALUSE;
            this.support = EasyWakeUpXmlParse.EASYWAKE_ENABLE_SURPPORT_VALUSE;
            this.flag = EasyWakeUpXmlParse.EASYWAKE_ENABLE_SURPPORT_VALUSE;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.EasyWakeUpXmlParse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.EasyWakeUpXmlParse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.EasyWakeUpXmlParse.<clinit>():void");
    }

    public static int getDefaultSupportValueFromCust() {
        int defaultsupportvalue = EASYWAKE_ENABLE_SURPPORT_VALUSE;
        if (hweasywakeupdates == null) {
            try {
                parseHwEasyWakeUpdatesFile();
            } catch (Exception e) {
                Log.e(TAG, "used default support value = 0");
                return EASYWAKE_ENABLE_SURPPORT_VALUSE;
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
        int defaultvalue = EASYWAKE_ENABLE_SURPPORT_VALUSE;
        if (hweasywakeupdates == null) {
            try {
                parseHwEasyWakeUpdatesFile();
            } catch (Exception e) {
                Log.e(TAG, "used default value = 0");
                return EASYWAKE_ENABLE_SURPPORT_VALUSE;
            }
        }
        if (hweasywakeupdates != null) {
            for (HwEasyWakeUpDate hweasywakeupdate : hweasywakeupdates) {
                defaultvalue += hweasywakeupdate.getValue() << hweasywakeupdate.getFlag();
            }
        }
        return defaultvalue;
    }

    private static void parseHwEasyWakeUpdatesFile() throws Exception {
        File mCustFile;
        File mCustFile2;
        Throwable th;
        InputStream inputstream;
        InputStream inputstream2 = null;
        try {
            mCustFile = HwCfgFilePolicy.getCfgFile("xml/hw_easywakeupmotion_config.xml", EASYWAKE_ENABLE_SURPPORT_VALUSE);
            if (mCustFile == null) {
                try {
                } catch (FileNotFoundException e) {
                } catch (XmlPullParserException e2) {
                    Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
                    if (inputstream2 == null) {
                        inputstream2.close();
                    }
                } catch (IOException e3) {
                    Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
                    if (inputstream2 == null) {
                        inputstream2.close();
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
                        if (inputstream2 != null) {
                            inputstream2.close();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputstream2 != null) {
                            inputstream2.close();
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    mCustFile = mCustFile2;
                    Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
                    if (inputstream2 == null) {
                        inputstream2.close();
                    }
                } catch (IOException e7) {
                    mCustFile = mCustFile2;
                    Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
                    if (inputstream2 == null) {
                        inputstream2.close();
                    }
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
                } else {
                    inputstream.close();
                }
            }
            mCustFile = mCustFile2;
            inputstream = new FileInputStream(mCustFile);
            parseHwEasyWakeUpDate(inputstream);
            if (inputstream != null) {
                inputstream.close();
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
            if (inputstream2 != null) {
                inputstream2.close();
            }
        } catch (XmlPullParserException e11) {
            inputstream2 = inputstream;
            Log.e(TAG, "hw_easywakeupmotion_config.xml XmlPullParserException");
            if (inputstream2 == null) {
                inputstream2.close();
            }
        } catch (IOException e12) {
            inputstream2 = inputstream;
            Log.e(TAG, "hw_easywakeupmotion_config.xml IOException");
            if (inputstream2 == null) {
                inputstream2.close();
            }
        } catch (Exception e13) {
            inputstream2 = inputstream;
            throw new Exception("Exception");
        } catch (Throwable th4) {
            th = th4;
            inputstream2 = inputstream;
            if (inputstream2 != null) {
                inputstream2.close();
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
                case EASYWAKE_ENABLE_SURPPORT_VALUSE /*0*/:
                    hweasywakeupdates = new ArrayList();
                    break;
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                    if ("EasyWakeUpMotion".equals(pullParser.getName())) {
                        String name = pullParser.getAttributeValue(EASYWAKE_ENABLE_SURPPORT_VALUSE);
                        int support = Integer.parseInt(pullParser.getAttributeValue(1));
                        int value = Integer.parseInt(pullParser.getAttributeValue(2));
                        int flag = Integer.parseInt(pullParser.getAttributeValue(3));
                        int keyCode = Integer.parseInt(pullParser.getAttributeValue(4));
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
                        hweasywakeupdate = new HwEasyWakeUpDate(name, support, value, flag);
                    }
                    if ("MaxKeyCode".equals(pullParser.getName())) {
                        maxKeyCode = Integer.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE)).intValue();
                    }
                    if ("MinKeyCode".equals(pullParser.getName())) {
                        minKeyCode = Integer.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE)).intValue();
                    }
                    if ("DriverFileLength".equals(pullParser.getName())) {
                        driverFileLength = Integer.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE)).intValue();
                    }
                    if ("DriverPostionPath".equals(pullParser.getName())) {
                        driverPostionPath = String.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE));
                    }
                    if ("DriverGesturePath".equals(pullParser.getName())) {
                        driverGesturePath = String.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE));
                    }
                    if ("DriverControlPath".equals(pullParser.getName())) {
                        driverControlPath = String.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE));
                    }
                    if ("SensorNear".equals(pullParser.getName())) {
                        sensorNearValue = Float.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE)).floatValue();
                    }
                    if ("SensorFar".equals(pullParser.getName())) {
                        sensorFarValue = Float.valueOf(pullParser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE)).floatValue();
                    }
                    if ("SensorWatchTime".equals(pullParser.getName())) {
                        sensorWatchTime = Long.parseLong(pullParser.getAttributeValue(null, "support"));
                    }
                    if ("SensorCheckTimes".equals(pullParser.getName())) {
                        sensorCheckTimes = Integer.valueOf(pullParser.getAttributeValue(null, "support")).intValue();
                    }
                    if (!"PowerOptimize".equals(pullParser.getName())) {
                        break;
                    }
                    powerOptimize = Integer.valueOf(pullParser.getAttributeValue(null, "support")).intValue();
                    break;
                case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
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

    public static String getDriverControlPath() {
        return driverControlPath;
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
