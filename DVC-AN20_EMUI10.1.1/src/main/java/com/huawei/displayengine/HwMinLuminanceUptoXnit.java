package com.huawei.displayengine;

import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Xml;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.uikit.effect.BuildConfig;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwMinLuminanceUptoXnit {
    private static final int ADDED_POINT_MAX_SIZE = 3;
    private static final int FIRST_ADDED_BRIGHTNESS = 156;
    private static final int FOURTH_ADDED_BRIGHTNESS = 274;
    private static final int MAX_BRIGHTNESS = 10000;
    private static final int MIN_BRIGHTNESS = 156;
    private static final int NAME_SIZE = 128;
    private static final int NORMALIZED_DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int NORMALIZED_DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int NORMALIZED_MAX_BRIGHTNESS = 10000;
    private static final int SECOND_ADDED_BRIGHTNESS = 196;
    private static final String TAG = "HwMinLuminanceUptoXnit";
    private static final int THIRD_ADDED_BRIGHTNESS = 235;
    private static final int XCC_DEFAULT_VALUE = 32768;
    private static final float XCC_MAX = 1.0f;
    private static final float XCC_MIN = 0.1f;
    private static final int XCC_TABLE_SIZE = 12;
    private static final String XNIT_CONFIG_NAME = "XnitConfig.xml";
    private static final String XNIT_CONFIG_NAME_NOEXT = "XnitConfig";
    private int mActualMaxLuminance = 360;
    private boolean mActualMaxLuminanceLoaded = false;
    private int mActualMinLuminance = 4;
    private boolean mActualMinLuminanceLoaded = false;
    private int mAddedPoint = 3;
    private boolean mAddedPointsLoaded = false;
    private DisplayEngineManager mDisplayEngineManager;
    private int mExpectedMinLuminance = 2;
    private boolean mExpectedMinLuminanceLoaded = false;
    private int mIndex = 0;
    private boolean mIsXccCoefReseted = false;
    private String mLcdPanelName = null;
    private boolean mNeedAdjustMinLum = false;
    private boolean mNeedAdjustMinLumLoaded = false;
    private boolean mSupportXcc;
    private float[][] mXccCoef = {new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}};
    private boolean mXccCoefForExpectedMinLumLoadStarted = false;
    private boolean mXccCoefForExpectedMinLumLoaded = false;

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        if (binder == null) {
            DeLog.i(TAG, "getLcdModelName() binder is null!");
            return BuildConfig.FLAVOR;
        }
        IDisplayEngineServiceEx service = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (service == null) {
            DeLog.i(TAG, "getLcdModelName() service is null!");
            return BuildConfig.FLAVOR;
        }
        byte[] name = new byte[128];
        try {
            int ret = service.getEffect(14, 0, name, name.length);
            if (ret == 0) {
                return new String(name, StandardCharsets.UTF_8).trim().replace(' ', '_').replace("'", BuildConfig.FLAVOR);
            }
            DeLog.i(TAG, "getLcdModelName() getEffect failed! ret=" + ret);
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            DeLog.e(TAG, "getLcdModelName() RemoteException " + e);
            return BuildConfig.FLAVOR;
        }
    }

    private String getXmlPath() {
        String xmlPath = String.format(Locale.ENGLISH, "/display/effect/displayengine/%s", "XnitConfig_" + this.mLcdPanelName + ".xml");
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile == null && (xmlFile = HwCfgFilePolicy.getCfgFile((xmlPath = String.format(Locale.ENGLISH, "/display/effect/displayengine/%s", XNIT_CONFIG_NAME)), 0)) == null) {
            DeLog.i(TAG, "get xmlFile failed!");
            return BuildConfig.FLAVOR;
        }
        try {
            return xmlFile.getCanonicalPath();
        } catch (IOException e) {
            DeLog.e(TAG, "getXmlPath() IOException " + e);
            return xmlPath;
        }
    }

    public HwMinLuminanceUptoXnit(DisplayEngineManager context) {
        boolean z = false;
        this.mDisplayEngineManager = context;
        try {
            this.mSupportXcc = this.mDisplayEngineManager.getSupported(16) == 1 ? true : z;
            this.mLcdPanelName = getLcdPanelName();
            if (!getConfig(getXmlPath())) {
                DeLog.i(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (IOException e) {
            DeLog.e(TAG, "getConfig error! Exception " + e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002f, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0030, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        throw r5;
     */
    private boolean getConfig(String configFilePath) throws IOException {
        if (configFilePath == null || configFilePath.length() == 0) {
            DeLog.i(TAG, "getConfig configFilePath is null! use default config");
            return false;
        }
        try {
            FileInputStream inputStream = new FileInputStream(new File(configFilePath));
            if (getConfigFromXml(inputStream)) {
                checkConfigLoadedFromXml();
                inputStream.close();
                return true;
            }
            inputStream.close();
            return false;
        } catch (IOException e) {
            DeLog.e(TAG, "getConfig error! Exception " + e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003c, code lost:
        if (r0[0][0] <= 1.0f) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0053, code lost:
        if (r0[1][0] <= 1.0f) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0069, code lost:
        if (r0[2][0] <= 1.0f) goto L_0x006c;
     */
    private void checkConfigLoadedFromXml() {
        int i = this.mActualMinLuminance;
        int i2 = this.mActualMaxLuminance;
        if (i > i2) {
            loadDefaultConfig();
            DeLog.e(TAG, "checkConfig failed for mActualMinLuminance > mActualMaxLuminance , LoadDefaultConfig!");
        } else if (this.mExpectedMinLuminance > i2) {
            loadDefaultConfig();
            DeLog.e(TAG, "checkConfig failed for mExpectedMinLuminance > mActualMaxLuminance , LoadDefaultConfig!");
        } else {
            int i3 = this.mAddedPoint;
            if (i3 <= 3) {
                if (i3 >= 1) {
                    float[][] fArr = this.mXccCoef;
                    if (fArr[0][0] > 0.1f) {
                    }
                }
                if (this.mAddedPoint >= 2) {
                    float[][] fArr2 = this.mXccCoef;
                    if (fArr2[1][0] > 0.1f) {
                    }
                }
                if (this.mAddedPoint >= 3) {
                    float[][] fArr3 = this.mXccCoef;
                    if (fArr3[2][0] > 0.1f) {
                    }
                }
                DeLog.i(TAG, "checkConfig LoadedFromXML success!");
                return;
            }
            loadDefaultConfig();
            DeLog.e(TAG, "checkConfig failed for mAddedPoint > 4 , LoadDefaultConfig!");
        }
    }

    private void loadDefaultConfig() {
        DeLog.i(TAG, "loadDefaultConfig");
        this.mNeedAdjustMinLum = false;
        this.mActualMaxLuminance = 360;
        this.mActualMinLuminance = 4;
        this.mExpectedMinLuminance = 2;
        this.mAddedPoint = 3;
        for (int i = 0; i < this.mAddedPoint; i++) {
            for (int j = 0; j < this.mAddedPoint; j++) {
                this.mXccCoef[i][j] = 1.0f;
            }
        }
    }

    private boolean getConfigStartTagFromXml(XmlPullParser parser, int eventType) throws XmlPullParserException, IOException {
        int i;
        String name = parser.getName();
        if (name == null || name.length() == 0) {
            return false;
        }
        if (Objects.equals(name, "NeedAdjustMinLum")) {
            this.mNeedAdjustMinLum = Boolean.parseBoolean(parser.nextText());
            this.mNeedAdjustMinLumLoaded = true;
            DeLog.i(TAG, "NeedAdjustMinLum = " + this.mNeedAdjustMinLum);
        } else if (Objects.equals(name, "ActualMaxLuminance")) {
            this.mActualMaxLuminance = Integer.parseInt(parser.nextText());
            this.mActualMaxLuminanceLoaded = true;
            DeLog.i(TAG, "ActualMaxLuminance = " + this.mActualMaxLuminance);
        } else if (Objects.equals(name, "ActualMinLuminance")) {
            this.mActualMinLuminance = Integer.parseInt(parser.nextText());
            this.mActualMinLuminanceLoaded = true;
            DeLog.i(TAG, "ActualMinLuminance = " + this.mActualMinLuminance);
        } else if (Objects.equals(name, "ExpectedMinLuminance")) {
            this.mExpectedMinLuminance = Integer.parseInt(parser.nextText());
            this.mExpectedMinLuminanceLoaded = true;
            DeLog.i(TAG, "ExpectedMinLuminance = " + this.mExpectedMinLuminance);
        } else if (Objects.equals(name, "AddedPointsForExpectedMinLum")) {
            this.mAddedPoint = Integer.parseInt(parser.nextText());
            this.mAddedPointsLoaded = true;
            DeLog.i(TAG, "AddedPointsForExpectedMinLum = " + this.mAddedPoint);
        } else if (Objects.equals(name, "XccCoefForExpectedMinLum")) {
            this.mXccCoefForExpectedMinLumLoadStarted = true;
        } else if (!Objects.equals(name, "XccCoef") || !this.mAddedPointsLoaded || !this.mXccCoefForExpectedMinLumLoadStarted) {
            DeLog.i(TAG, "getConfigStartTagFromXml finished.");
        } else {
            String[] xccForRgbSplited = parser.nextText().split(",");
            if (xccForRgbSplited == null || xccForRgbSplited.length != 3 || (i = this.mIndex) > 2) {
                return false;
            }
            this.mXccCoef[i][0] = Float.parseFloat(xccForRgbSplited[0]);
            this.mXccCoef[this.mIndex][1] = Float.parseFloat(xccForRgbSplited[1]);
            this.mXccCoef[this.mIndex][2] = Float.parseFloat(xccForRgbSplited[2]);
            DeLog.i(TAG, "mXccCoef x = " + this.mXccCoef[this.mIndex][0] + ", y = " + this.mXccCoef[this.mIndex][1] + ", z = " + this.mXccCoef[this.mIndex][2]);
            this.mIndex = this.mIndex + 1;
        }
        return true;
    }

    private void getConfigEndTagFromXml(XmlPullParser parser, int eventType) {
        String name = parser.getName();
        if (name != null && name.length() != 0 && Objects.equals(name, "XccCoefForExpectedMinLum")) {
            this.mXccCoefForExpectedMinLumLoadStarted = false;
            this.mXccCoefForExpectedMinLumLoaded = true;
        }
    }

    private boolean getConfigFromXml(InputStream inStream) {
        DeLog.i(TAG, "getConfigFromXml");
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        getConfigStartTagFromXml(parser, eventType);
                    } else if (eventType == 3) {
                        getConfigEndTagFromXml(parser, eventType);
                    }
                }
            }
            if (this.mNeedAdjustMinLumLoaded && this.mActualMaxLuminanceLoaded && this.mActualMinLuminanceLoaded && this.mAddedPointsLoaded && this.mExpectedMinLuminanceLoaded && this.mXccCoefForExpectedMinLumLoaded) {
                DeLog.i(TAG, "xnit getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            DeLog.e(TAG, "getConfigFromXml error! Exception " + e);
        } catch (IOException e2) {
            DeLog.e(TAG, "getConfigFromXml error! Exception " + e2);
        }
        DeLog.e(TAG, "getConfigFromXml failed!");
        return false;
    }

    private void setXccCoef(float xccCoefRed, float xccCoefGreen, float xccCoefBlue) {
        if (xccCoefRed <= 1.0f && xccCoefRed >= 0.1f && xccCoefGreen <= 1.0f && xccCoefGreen >= 0.1f && xccCoefBlue <= 1.0f && xccCoefBlue >= 0.1f) {
            PersistableBundle bundle = new PersistableBundle();
            bundle.putIntArray("Buffer", new int[]{(int) (xccCoefRed * 32768.0f), (int) (xccCoefGreen * 32768.0f), (int) (32768.0f * xccCoefBlue)});
            bundle.putInt("BufferLength", 12);
            this.mDisplayEngineManager.setData(5, bundle);
        }
    }

    private int addOneXnitPoint(int minBrightness, int maxBrightness, int level) {
        if (level <= SECOND_ADDED_BRIGHTNESS) {
            float slope = ((float) (level - 156)) / 40.0f;
            float[][] fArr = this.mXccCoef;
            float xccCoefRed = fArr[0][0] + ((1.0f - fArr[0][0]) * slope);
            setXccCoef(xccCoefRed, fArr[0][1] + ((1.0f - fArr[0][1]) * slope), fArr[0][2] + ((1.0f - fArr[0][2]) * slope));
            this.mIsXccCoefReseted = false;
            DeLog.i(TAG, "xnit xccCoefRed = " + xccCoefRed + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPoint);
            return minBrightness;
        }
        if (!this.mIsXccCoefReseted) {
            setXccCoef(1.0f, 1.0f, 1.0f);
        }
        this.mIsXccCoefReseted = true;
        return minBrightness + (((level - 196) * (maxBrightness - minBrightness)) / 9804);
    }

    private int addTwoXnitPoint(int minBrightness, int maxBrightness, int level) {
        if (level < SECOND_ADDED_BRIGHTNESS) {
            float slope = ((float) (level - 156)) / 40.0f;
            float[][] fArr = this.mXccCoef;
            float xccCoefRed = fArr[0][0] + ((fArr[1][0] - fArr[0][0]) * slope);
            setXccCoef(xccCoefRed, fArr[0][1] + ((fArr[1][1] - fArr[0][1]) * slope), fArr[0][2] + ((fArr[1][2] - fArr[0][2]) * slope));
            this.mIsXccCoefReseted = false;
            DeLog.i(TAG, "xnit xccCoefRed = " + xccCoefRed + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPoint);
            return minBrightness;
        } else if (level <= THIRD_ADDED_BRIGHTNESS) {
            float slope2 = ((float) (level - 196)) / 39.0f;
            float[][] fArr2 = this.mXccCoef;
            float xccCoefRed2 = fArr2[1][0] + ((1.0f - fArr2[1][0]) * slope2);
            setXccCoef(xccCoefRed2, fArr2[1][1] + ((1.0f - fArr2[1][1]) * slope2), fArr2[1][2] + ((1.0f - fArr2[1][2]) * slope2));
            this.mIsXccCoefReseted = false;
            DeLog.i(TAG, "xnit xccCoefRed = " + xccCoefRed2 + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPoint);
            return minBrightness;
        } else {
            if (!this.mIsXccCoefReseted) {
                setXccCoef(1.0f, 1.0f, 1.0f);
            }
            this.mIsXccCoefReseted = true;
            return minBrightness + (((level - 235) * (maxBrightness - minBrightness)) / 9765);
        }
    }

    private int addThreeXnitPoint(int minBrightness, int maxBrightness, int level) {
        if (level < SECOND_ADDED_BRIGHTNESS) {
            float slope = ((float) (level - 156)) / 40.0f;
            float[][] fArr = this.mXccCoef;
            float xccCoefRed = fArr[0][0] + ((fArr[1][0] - fArr[0][0]) * slope);
            setXccCoef(xccCoefRed, fArr[0][1] + ((fArr[1][1] - fArr[0][1]) * slope), fArr[0][2] + ((fArr[1][2] - fArr[0][2]) * slope));
            this.mIsXccCoefReseted = false;
            DeLog.i(TAG, "xnit Coef = " + xccCoefRed + ", level = " + level + ", addNums = " + this.mAddedPoint);
            return minBrightness;
        } else if (level < THIRD_ADDED_BRIGHTNESS) {
            float slope2 = ((float) (level - 196)) / 39.0f;
            float[][] fArr2 = this.mXccCoef;
            float xccCoefRed2 = fArr2[1][0] + ((fArr2[2][0] - fArr2[1][0]) * slope2);
            setXccCoef(xccCoefRed2, fArr2[1][1] + ((fArr2[2][1] - fArr2[1][1]) * slope2), fArr2[1][2] + ((fArr2[2][2] - fArr2[1][2]) * slope2));
            this.mIsXccCoefReseted = false;
            DeLog.i(TAG, "xnit Coef = " + xccCoefRed2 + ", level = " + level + ", addNums = " + this.mAddedPoint);
            return minBrightness;
        } else if (level <= FOURTH_ADDED_BRIGHTNESS) {
            float slope3 = ((float) (level - 235)) / 39.0f;
            float[][] fArr3 = this.mXccCoef;
            float xccCoefRed3 = fArr3[2][0] + ((1.0f - fArr3[2][0]) * slope3);
            setXccCoef(xccCoefRed3, fArr3[2][1] + ((1.0f - fArr3[2][1]) * slope3), fArr3[2][2] + ((1.0f - fArr3[2][2]) * slope3));
            this.mIsXccCoefReseted = false;
            DeLog.i(TAG, "xnit Coef = " + xccCoefRed3 + ", level = " + level + ", addNums = " + this.mAddedPoint);
            return minBrightness;
        } else {
            if (!this.mIsXccCoefReseted) {
                setXccCoef(1.0f, 1.0f, 1.0f);
            }
            this.mIsXccCoefReseted = true;
            return minBrightness + (((level - 274) * (maxBrightness - minBrightness)) / 9726);
        }
    }

    public int setXnit(int normalizedMinBrightness, int normalizedMaxBrightness, int level) {
        int i;
        if (!this.mSupportXcc || !this.mNeedAdjustMinLum || (i = this.mAddedPoint) == 0 || this.mXccCoef[0][0] == 0.0f) {
            return (((level - 156) * (normalizedMaxBrightness - normalizedMinBrightness)) / 9844) + normalizedMinBrightness;
        }
        if (i == 0) {
            return normalizedMinBrightness + (((level - 156) * (normalizedMaxBrightness - normalizedMinBrightness)) / 9844);
        }
        if (i == 1) {
            return addOneXnitPoint(normalizedMinBrightness, normalizedMaxBrightness, level);
        }
        if (i == 2) {
            return addTwoXnitPoint(normalizedMinBrightness, normalizedMaxBrightness, level);
        }
        if (i != 3) {
            return level;
        }
        return addThreeXnitPoint(normalizedMinBrightness, normalizedMaxBrightness, level);
    }
}
