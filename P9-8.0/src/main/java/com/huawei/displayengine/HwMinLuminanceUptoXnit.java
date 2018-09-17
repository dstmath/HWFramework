package com.huawei.displayengine;

import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Xml;
import com.huawei.displayengine.IDisplayEngineServiceEx.Stub;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwMinLuminanceUptoXnit {
    private static final int FIRST_ADDED_BRIGHTNESS = 156;
    private static final int FOURTH_ADDED_BRIGHTNESS = 274;
    private static final int MAX_BRIGHTNESS = 10000;
    private static final int MIN_BRIGHTNESS = 156;
    private static final int NORMALIZED_DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int NORMALIZED_DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int NORMALIZED_MAX_BRIGHTNESS = 10000;
    private static final int SECOND_ADDED_BRIGHTNESS = 196;
    private static String TAG = "HwMinLuminanceUptoXnit";
    private static final int THIRD_ADDED_BRIGHTNESS = 235;
    private static final String XNIT_CONFIG_NAME = "XnitConfig.xml";
    private static final String XNIT_CONFIG_NAME_NOEXT = "XnitConfig";
    private int mActualMaxLuminance = 360;
    private int mActualMinLuminance = 4;
    private int mAddedPointForExpectedMinLum = 3;
    private DisplayEngineManager mDisplayEngineManager;
    private int mExpectedMinLuminance = 2;
    private String mLcdPanelName = null;
    private boolean mNeedAdjustMinLum = false;
    private int mSupportXCC = 0;
    private float[][] mXccCoefForExpectedMinLum = new float[][]{new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}};
    private boolean mXccCoef_is_reseted = false;

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        if (binder == null) {
            DElog.i(TAG, "getLcdModelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx service = Stub.asInterface(binder);
        if (service == null) {
            DElog.i(TAG, "getLcdModelName() service is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = service.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                DElog.i(TAG, "getLcdModelName() getEffect failed! ret=" + ret);
                return null;
            }
            String panelName = new String(name).trim().replace(' ', '_').replace("'", "");
            DElog.i(TAG, "getLcdModelName() panelName=" + panelName);
            return panelName;
        } catch (RemoteException e) {
            DElog.e(TAG, "getLcdModelName() RemoteException " + e);
            return null;
        }
    }

    private String getXmlPath() {
        String lcdXnitConfigFile = "XnitConfig_" + this.mLcdPanelName + ".xml";
        String xmlPath = String.format("/display/effect/displayengine/%s", new Object[]{lcdXnitConfigFile});
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile == null) {
            xmlPath = String.format("/display/effect/displayengine/%s", new Object[]{XNIT_CONFIG_NAME});
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
            if (xmlFile == null) {
                DElog.i(TAG, "get xmlFile :" + xmlPath + " failed!");
                return null;
            }
        }
        DElog.i(TAG, "getXmlPath = " + xmlPath + ",xmlFile = " + xmlFile);
        return xmlFile.getAbsolutePath();
    }

    public HwMinLuminanceUptoXnit(DisplayEngineManager context) {
        this.mDisplayEngineManager = context;
        try {
            this.mSupportXCC = this.mDisplayEngineManager.getSupported(16);
            this.mLcdPanelName = getLcdPanelName();
            if (!getConfig(getXmlPath())) {
                DElog.i(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (IOException e) {
            DElog.e(TAG, "getConfig error! Exception " + e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b4 A:{SYNTHETIC, Splitter: B:33:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d6 A:{SYNTHETIC, Splitter: B:39:0x00d6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfig(String configFilePath) throws IOException {
        IOException e;
        Throwable th;
        if (configFilePath == null || configFilePath.length() == 0) {
            DElog.i(TAG, "getConfig configFilePath is null! use default config");
            return false;
        }
        DElog.i(TAG, "configFilePath = " + configFilePath + ",length = " + configFilePath.length());
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(new File(configFilePath));
            try {
                if (getConfigFromXML(inputStream2)) {
                    checkConfigLoadedFromXML();
                    inputStream2.close();
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e1) {
                            DElog.e(TAG, "e1 is " + e1);
                        }
                    }
                    inputStream = inputStream2;
                    return true;
                }
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e12) {
                        DElog.e(TAG, "e1 is " + e12);
                        return false;
                    }
                }
                inputStream = inputStream2;
                return false;
            } catch (IOException e2) {
                e = e2;
                inputStream = inputStream2;
                try {
                    DElog.e(TAG, "getConfig error! Exception " + e);
                    if (inputStream != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e122) {
                            DElog.e(TAG, "e1 is " + e122);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = inputStream2;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            DElog.e(TAG, "getConfig error! Exception " + e);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1222) {
                    DElog.e(TAG, "e1 is " + e1222);
                }
            }
            return false;
        }
    }

    private void checkConfigLoadedFromXML() {
        if (this.mActualMinLuminance > this.mActualMaxLuminance) {
            loadDefaultConfig();
            DElog.e(TAG, "checkConfig failed for mActualMinLuminance > mActualMaxLuminance , LoadDefaultConfig!");
        } else if (this.mExpectedMinLuminance > this.mActualMaxLuminance) {
            loadDefaultConfig();
            DElog.e(TAG, "checkConfig failed for mExpectedMinLuminance > mActualMaxLuminance , LoadDefaultConfig!");
        } else if (this.mAddedPointForExpectedMinLum > 3 || ((this.mAddedPointForExpectedMinLum >= 1 && (this.mXccCoefForExpectedMinLum[0][0] <= 0.0f || this.mXccCoefForExpectedMinLum[0][0] > 1.0f)) || ((this.mAddedPointForExpectedMinLum >= 2 && (this.mXccCoefForExpectedMinLum[1][0] <= 0.0f || this.mXccCoefForExpectedMinLum[1][0] > 1.0f)) || (this.mAddedPointForExpectedMinLum >= 3 && (this.mXccCoefForExpectedMinLum[2][0] <= 0.0f || this.mXccCoefForExpectedMinLum[2][0] > 1.0f))))) {
            loadDefaultConfig();
            DElog.e(TAG, "checkConfig failed for mAddedPointForExpectedMinLum > 4 , LoadDefaultConfig!");
        } else {
            DElog.i(TAG, "checkConfig LoadedFromXML success!");
        }
    }

    private void loadDefaultConfig() {
        DElog.i(TAG, "loadDefaultConfig");
        this.mNeedAdjustMinLum = false;
        this.mActualMaxLuminance = 350;
        this.mActualMinLuminance = 4;
        this.mExpectedMinLuminance = 2;
        this.mAddedPointForExpectedMinLum = 2;
        this.mXccCoefForExpectedMinLum[0][0] = 1.0f;
        this.mXccCoefForExpectedMinLum[0][1] = 1.0f;
        this.mXccCoefForExpectedMinLum[0][2] = 1.0f;
        this.mXccCoefForExpectedMinLum[1][0] = 1.0f;
        this.mXccCoefForExpectedMinLum[1][1] = 1.0f;
        this.mXccCoefForExpectedMinLum[1][2] = 1.0f;
        this.mXccCoefForExpectedMinLum[2][0] = 1.0f;
        this.mXccCoefForExpectedMinLum[2][1] = 1.0f;
        this.mXccCoefForExpectedMinLum[2][2] = 1.0f;
    }

    private boolean getConfigFromXML(InputStream inStream) {
        DElog.i(TAG, "getConfigFromXML");
        boolean NeedAdjustMinLumLoaded = false;
        boolean ActualMaxLuminanceLoaded = false;
        boolean ActualMinLuminanceLoaded = false;
        boolean ExpectedMinLuminanceLoaded = false;
        boolean AddedPointForExpectedMinLumLoaded = false;
        boolean XccCoefForExpectedMinLumLoaded = false;
        boolean XccCoefForExpectedMinLumLoadStarted = false;
        int index = 0;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 2:
                        String name = parser.getName();
                        if (name != null && name.length() != 0) {
                            if (!name.equals("NeedAdjustMinLum")) {
                                if (!name.equals("ActualMaxLuminance")) {
                                    if (!name.equals("ActualMinLuminance")) {
                                        if (!name.equals("ExpectedMinLuminance")) {
                                            if (!name.equals("AddedPointsForExpectedMinLum")) {
                                                if (!name.equals("XccCoefForExpectedMinLum")) {
                                                    if (name.equals("XccCoef") && AddedPointForExpectedMinLumLoaded && XccCoefForExpectedMinLumLoadStarted) {
                                                        String s = parser.nextText();
                                                        String[] XccForRGBSplited = s.split(",");
                                                        if (XccForRGBSplited != null && XccForRGBSplited.length == 3) {
                                                            this.mXccCoefForExpectedMinLum[index][0] = Float.parseFloat(XccForRGBSplited[0]);
                                                            this.mXccCoefForExpectedMinLum[index][1] = Float.parseFloat(XccForRGBSplited[1]);
                                                            this.mXccCoefForExpectedMinLum[index][2] = Float.parseFloat(XccForRGBSplited[2]);
                                                            DElog.i(TAG, "mXccCoefForExpectedMinLum x = " + this.mXccCoefForExpectedMinLum[index][0] + ", y = " + this.mXccCoefForExpectedMinLum[index][1] + ", z = " + this.mXccCoefForExpectedMinLum[index][2]);
                                                            index++;
                                                            break;
                                                        }
                                                        DElog.e(TAG, "split failed! s = " + s);
                                                        return false;
                                                    }
                                                }
                                                XccCoefForExpectedMinLumLoadStarted = true;
                                                break;
                                            }
                                            this.mAddedPointForExpectedMinLum = Integer.parseInt(parser.nextText());
                                            AddedPointForExpectedMinLumLoaded = true;
                                            DElog.i(TAG, "AddedPointsForExpectedMinLum = " + this.mAddedPointForExpectedMinLum);
                                            break;
                                        }
                                        this.mExpectedMinLuminance = Integer.parseInt(parser.nextText());
                                        ExpectedMinLuminanceLoaded = true;
                                        DElog.i(TAG, "ExpectedMinLuminance = " + this.mExpectedMinLuminance);
                                        break;
                                    }
                                    this.mActualMinLuminance = Integer.parseInt(parser.nextText());
                                    ActualMinLuminanceLoaded = true;
                                    DElog.i(TAG, "ActualMinLuminance = " + this.mActualMinLuminance);
                                    break;
                                }
                                this.mActualMaxLuminance = Integer.parseInt(parser.nextText());
                                ActualMaxLuminanceLoaded = true;
                                DElog.i(TAG, "ActualMaxLuminance = " + this.mActualMaxLuminance);
                                break;
                            }
                            this.mNeedAdjustMinLum = Boolean.parseBoolean(parser.nextText());
                            NeedAdjustMinLumLoaded = true;
                            DElog.i(TAG, "NeedAdjustMinLum = " + this.mNeedAdjustMinLum);
                            break;
                        }
                        return false;
                        break;
                    case 3:
                        if (!parser.getName().equals("XccCoefForExpectedMinLum")) {
                            break;
                        }
                        XccCoefForExpectedMinLumLoadStarted = false;
                        XccCoefForExpectedMinLumLoaded = true;
                        break;
                    default:
                        break;
                }
            }
            if (NeedAdjustMinLumLoaded && ActualMaxLuminanceLoaded && ActualMinLuminanceLoaded && AddedPointForExpectedMinLumLoaded && ExpectedMinLuminanceLoaded && XccCoefForExpectedMinLumLoaded) {
                DElog.i(TAG, "xnit getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            DElog.e(TAG, "getConfigFromXML error! Exception " + e);
        } catch (IOException e2) {
            DElog.e(TAG, "getConfigFromXML error! Exception " + e2);
        }
        DElog.e(TAG, "getConfigFromXML failed!");
        return false;
    }

    private void xccCoef_setting(float xccCoef_R, float xccCoef_G, float xccCoef_B) {
        if (xccCoef_R <= 1.0f && xccCoef_R >= 0.1f && xccCoef_G <= 1.0f && xccCoef_G >= 0.1f && xccCoef_B <= 1.0f && xccCoef_B >= 0.1f) {
            int[] xccCoef = new int[]{(int) (xccCoef_R * 32768.0f), (int) (xccCoef_G * 32768.0f), (int) (xccCoef_B * 32768.0f)};
            PersistableBundle bundle = new PersistableBundle();
            bundle.putIntArray("Buffer", xccCoef);
            bundle.putInt("BufferLength", 12);
            this.mDisplayEngineManager.setData(5, bundle);
        }
    }

    public int setXnit(int mNormalizedMinBrightness, int mNormalizedMaxBrightness, int level) {
        int brightnessvalue = level;
        if (this.mSupportXCC == 0 || !this.mNeedAdjustMinLum || this.mAddedPointForExpectedMinLum == 0 || 0.0f == this.mXccCoefForExpectedMinLum[0][0]) {
            return mNormalizedMinBrightness + (((level - 156) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9844);
        }
        float xccCoef_R;
        switch (this.mAddedPointForExpectedMinLum) {
            case 0:
                brightnessvalue = mNormalizedMinBrightness + (((level - 156) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9844);
                break;
            case 1:
                if (level > 196) {
                    if (!this.mXccCoef_is_reseted) {
                        xccCoef_setting(1.0f, 1.0f, 1.0f);
                    }
                    this.mXccCoef_is_reseted = true;
                    brightnessvalue = mNormalizedMinBrightness + (((level - 196) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9804);
                    break;
                }
                brightnessvalue = mNormalizedMinBrightness;
                xccCoef_R = this.mXccCoefForExpectedMinLum[0][0] + ((((float) (level - 156)) / 40.0f) * (1.0f - this.mXccCoefForExpectedMinLum[0][0]));
                xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[0][1] + ((((float) (level - 156)) / 40.0f) * (1.0f - this.mXccCoefForExpectedMinLum[0][1])), this.mXccCoefForExpectedMinLum[0][2] + ((((float) (level - 156)) / 40.0f) * (1.0f - this.mXccCoefForExpectedMinLum[0][2])));
                this.mXccCoef_is_reseted = false;
                DElog.i(TAG, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                break;
            case 2:
                if (level >= 196) {
                    if (level > 235) {
                        if (!this.mXccCoef_is_reseted) {
                            xccCoef_setting(1.0f, 1.0f, 1.0f);
                        }
                        this.mXccCoef_is_reseted = true;
                        brightnessvalue = mNormalizedMinBrightness + (((level - 235) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9765);
                        break;
                    }
                    brightnessvalue = mNormalizedMinBrightness;
                    xccCoef_R = this.mXccCoefForExpectedMinLum[1][0] + ((((float) (level - 196)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[1][0]));
                    xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[1][1] + ((((float) (level - 196)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[1][1])), this.mXccCoefForExpectedMinLum[1][2] + ((((float) (level - 196)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[1][2])));
                    this.mXccCoef_is_reseted = false;
                    DElog.i(TAG, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                    break;
                }
                brightnessvalue = mNormalizedMinBrightness;
                xccCoef_R = this.mXccCoefForExpectedMinLum[0][0] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][0] - this.mXccCoefForExpectedMinLum[0][0])) / 40.0f);
                xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[0][1] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][1] - this.mXccCoefForExpectedMinLum[0][1])) / 40.0f), this.mXccCoefForExpectedMinLum[0][2] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][2] - this.mXccCoefForExpectedMinLum[0][2])) / 40.0f));
                this.mXccCoef_is_reseted = false;
                DElog.i(TAG, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                break;
            case 3:
                if (level >= 196) {
                    if (level >= 235) {
                        if (level > FOURTH_ADDED_BRIGHTNESS) {
                            if (!this.mXccCoef_is_reseted) {
                                xccCoef_setting(1.0f, 1.0f, 1.0f);
                            }
                            this.mXccCoef_is_reseted = true;
                            brightnessvalue = mNormalizedMinBrightness + (((level - 274) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9726);
                            break;
                        }
                        brightnessvalue = mNormalizedMinBrightness;
                        xccCoef_R = this.mXccCoefForExpectedMinLum[2][0] + ((((float) (level - 235)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[2][0]));
                        xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[2][1] + ((((float) (level - 235)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[2][1])), this.mXccCoefForExpectedMinLum[2][2] + ((((float) (level - 235)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[2][2])));
                        this.mXccCoef_is_reseted = false;
                        DElog.i(TAG, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                        break;
                    }
                    brightnessvalue = mNormalizedMinBrightness;
                    xccCoef_R = this.mXccCoefForExpectedMinLum[1][0] + ((((float) (level - 196)) * (this.mXccCoefForExpectedMinLum[2][0] - this.mXccCoefForExpectedMinLum[1][0])) / 39.0f);
                    xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[1][1] + ((((float) (level - 196)) * (this.mXccCoefForExpectedMinLum[2][1] - this.mXccCoefForExpectedMinLum[1][1])) / 39.0f), this.mXccCoefForExpectedMinLum[1][2] + ((((float) (level - 196)) * (this.mXccCoefForExpectedMinLum[2][2] - this.mXccCoefForExpectedMinLum[1][2])) / 39.0f));
                    this.mXccCoef_is_reseted = false;
                    DElog.i(TAG, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                    break;
                }
                brightnessvalue = mNormalizedMinBrightness;
                xccCoef_R = this.mXccCoefForExpectedMinLum[0][0] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][0] - this.mXccCoefForExpectedMinLum[0][0])) / 40.0f);
                xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[0][1] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][1] - this.mXccCoefForExpectedMinLum[0][1])) / 40.0f), this.mXccCoefForExpectedMinLum[0][2] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][2] - this.mXccCoefForExpectedMinLum[0][2])) / 40.0f));
                this.mXccCoef_is_reseted = false;
                DElog.i(TAG, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                break;
        }
        return brightnessvalue;
    }
}
