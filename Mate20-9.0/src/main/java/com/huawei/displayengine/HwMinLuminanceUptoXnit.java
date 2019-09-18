package com.huawei.displayengine;

import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Xml;
import com.huawei.displayengine.IDisplayEngineServiceEx;
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
    private float[][] mXccCoefForExpectedMinLum = {new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}};
    private boolean mXccCoef_is_reseted = false;

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        if (binder == null) {
            DElog.i(TAG, "getLcdModelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx service = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (service == null) {
            DElog.i(TAG, "getLcdModelName() service is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = service.getEffect(14, 0, name, name.length);
            if (ret == 0) {
                return new String(name).trim().replace(' ', '_').replace("'", "");
            }
            String str = TAG;
            DElog.i(str, "getLcdModelName() getEffect failed! ret=" + ret);
            return null;
        } catch (RemoteException e) {
            String str2 = TAG;
            DElog.e(str2, "getLcdModelName() RemoteException " + e);
            return null;
        }
    }

    private String getXmlPath() {
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/display/effect/displayengine/%s", new Object[]{"XnitConfig_" + this.mLcdPanelName + ".xml"}), 0);
        if (xmlFile == null) {
            xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/display/effect/displayengine/%s", new Object[]{XNIT_CONFIG_NAME}), 0);
            if (xmlFile == null) {
                DElog.i(TAG, "get xmlFile failed!");
                return null;
            }
        }
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
            String str = TAG;
            DElog.e(str, "getConfig error! Exception " + e);
        }
    }

    private boolean getConfig(String configFilePath) throws IOException {
        String str;
        StringBuilder sb;
        if (configFilePath == null || configFilePath.length() == 0) {
            DElog.i(TAG, "getConfig configFilePath is null! use default config");
            return false;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(new File(configFilePath));
            if (getConfigFromXML(inputStream2)) {
                checkConfigLoadedFromXML();
                inputStream2.close();
                try {
                    inputStream2.close();
                } catch (IOException e1) {
                    String str2 = TAG;
                    DElog.e(str2, "e1 is " + e1);
                }
                return true;
            }
            try {
                inputStream2.close();
            } catch (IOException e) {
                e1 = e;
                str = TAG;
                sb = new StringBuilder();
            }
            return false;
        } catch (IOException e2) {
            String str3 = TAG;
            DElog.e(str3, "getConfig error! Exception " + e2);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e1 = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e12) {
                    String str4 = TAG;
                    DElog.e(str4, "e1 is " + e12);
                }
            }
            throw th;
        }
        sb.append("e1 is ");
        sb.append(e1);
        DElog.e(str, sb.toString());
        return false;
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003d, code lost:
        r18 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0029, code lost:
        r18 = r0;
        r19 = r8;
     */
    private boolean getConfigFromXML(InputStream inStream) {
        boolean XccCoefForExpectedMinLumLoadStarted;
        boolean z;
        DElog.i(TAG, "getConfigFromXML");
        boolean NeedAdjustMinLumLoaded = false;
        boolean ActualMaxLuminanceLoaded = false;
        boolean ActualMinLuminanceLoaded = false;
        boolean ExpectedMinLuminanceLoaded = false;
        boolean AddedPointForExpectedMinLumLoaded = false;
        boolean XccCoefForExpectedMinLumLoaded = false;
        boolean XccCoefForExpectedMinLumLoadStarted2 = false;
        int index = 0;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    switch (eventType) {
                        case 2:
                            try {
                                String name = parser.getName();
                                if (name == null) {
                                    boolean z2 = XccCoefForExpectedMinLumLoadStarted2;
                                } else if (name.length() == 0) {
                                    int i = eventType;
                                    boolean z3 = XccCoefForExpectedMinLumLoadStarted2;
                                } else if (name.equals("NeedAdjustMinLum")) {
                                    this.mNeedAdjustMinLum = Boolean.parseBoolean(parser.nextText());
                                    NeedAdjustMinLumLoaded = true;
                                    String str = TAG;
                                    DElog.i(str, "NeedAdjustMinLum = " + this.mNeedAdjustMinLum);
                                    break;
                                } else if (name.equals("ActualMaxLuminance")) {
                                    this.mActualMaxLuminance = Integer.parseInt(parser.nextText());
                                    ActualMaxLuminanceLoaded = true;
                                    String str2 = TAG;
                                    DElog.i(str2, "ActualMaxLuminance = " + this.mActualMaxLuminance);
                                    break;
                                } else if (name.equals("ActualMinLuminance")) {
                                    this.mActualMinLuminance = Integer.parseInt(parser.nextText());
                                    ActualMinLuminanceLoaded = true;
                                    String str3 = TAG;
                                    DElog.i(str3, "ActualMinLuminance = " + this.mActualMinLuminance);
                                    break;
                                } else if (name.equals("ExpectedMinLuminance")) {
                                    this.mExpectedMinLuminance = Integer.parseInt(parser.nextText());
                                    ExpectedMinLuminanceLoaded = true;
                                    String str4 = TAG;
                                    DElog.i(str4, "ExpectedMinLuminance = " + this.mExpectedMinLuminance);
                                    break;
                                } else if (name.equals("AddedPointsForExpectedMinLum")) {
                                    this.mAddedPointForExpectedMinLum = Integer.parseInt(parser.nextText());
                                    AddedPointForExpectedMinLumLoaded = true;
                                    String str5 = TAG;
                                    DElog.i(str5, "AddedPointsForExpectedMinLum = " + this.mAddedPointForExpectedMinLum);
                                    break;
                                } else if (name.equals("XccCoefForExpectedMinLum")) {
                                    XccCoefForExpectedMinLumLoadStarted2 = true;
                                    break;
                                } else if (!name.equals("XccCoef") || !AddedPointForExpectedMinLumLoaded || !XccCoefForExpectedMinLumLoadStarted2) {
                                    z = XccCoefForExpectedMinLumLoadStarted2;
                                    break;
                                } else {
                                    String s = parser.nextText();
                                    String[] XccForRGBSplited = s.split(",");
                                    if (XccForRGBSplited != null) {
                                        int i2 = eventType;
                                        if (XccForRGBSplited.length != 3) {
                                            boolean z4 = XccCoefForExpectedMinLumLoadStarted2;
                                        } else {
                                            XccCoefForExpectedMinLumLoadStarted = XccCoefForExpectedMinLumLoadStarted2;
                                            try {
                                                this.mXccCoefForExpectedMinLum[index][0] = Float.parseFloat(XccForRGBSplited[0]);
                                                this.mXccCoefForExpectedMinLum[index][1] = Float.parseFloat(XccForRGBSplited[1]);
                                                this.mXccCoefForExpectedMinLum[index][2] = Float.parseFloat(XccForRGBSplited[2]);
                                                String str6 = TAG;
                                                DElog.i(str6, "mXccCoefForExpectedMinLum x = " + this.mXccCoefForExpectedMinLum[index][0] + ", y = " + this.mXccCoefForExpectedMinLum[index][1] + ", z = " + this.mXccCoefForExpectedMinLum[index][2]);
                                                index++;
                                                XccCoefForExpectedMinLumLoadStarted2 = XccCoefForExpectedMinLumLoadStarted;
                                                break;
                                            } catch (XmlPullParserException e) {
                                                e = e;
                                                boolean z5 = XccCoefForExpectedMinLumLoadStarted;
                                                String str7 = TAG;
                                                DElog.e(str7, "getConfigFromXML error! Exception " + e);
                                                DElog.e(TAG, "getConfigFromXML failed!");
                                                return false;
                                            } catch (IOException e2) {
                                                e = e2;
                                                boolean z6 = XccCoefForExpectedMinLumLoadStarted;
                                                String str8 = TAG;
                                                DElog.e(str8, "getConfigFromXML error! Exception " + e);
                                                DElog.e(TAG, "getConfigFromXML failed!");
                                                return false;
                                            }
                                        }
                                    } else {
                                        boolean z7 = XccCoefForExpectedMinLumLoadStarted2;
                                    }
                                    String str9 = TAG;
                                    DElog.e(str9, "split failed! s = " + s);
                                    return false;
                                }
                                return false;
                            } catch (XmlPullParserException e3) {
                                e = e3;
                                boolean z8 = XccCoefForExpectedMinLumLoadStarted2;
                                String str72 = TAG;
                                DElog.e(str72, "getConfigFromXML error! Exception " + e);
                                DElog.e(TAG, "getConfigFromXML failed!");
                                return false;
                            } catch (IOException e4) {
                                e = e4;
                                boolean z9 = XccCoefForExpectedMinLumLoadStarted2;
                                String str82 = TAG;
                                DElog.e(str82, "getConfigFromXML error! Exception " + e);
                                DElog.e(TAG, "getConfigFromXML failed!");
                                return false;
                            }
                        case 3:
                            if (parser.getName().equals("XccCoefForExpectedMinLum")) {
                                XccCoefForExpectedMinLumLoadStarted2 = false;
                                XccCoefForExpectedMinLumLoaded = true;
                                break;
                            }
                            break;
                    }
                } else {
                    int i3 = eventType;
                    z = XccCoefForExpectedMinLumLoadStarted2;
                }
                XccCoefForExpectedMinLumLoadStarted2 = z;
                eventType = parser.next();
            }
            int i4 = eventType;
            XccCoefForExpectedMinLumLoadStarted = XccCoefForExpectedMinLumLoadStarted2;
            if (!NeedAdjustMinLumLoaded || !ActualMaxLuminanceLoaded || !ActualMinLuminanceLoaded || !AddedPointForExpectedMinLumLoaded || !ExpectedMinLuminanceLoaded || !XccCoefForExpectedMinLumLoaded) {
                boolean z10 = XccCoefForExpectedMinLumLoadStarted;
                DElog.e(TAG, "getConfigFromXML failed!");
                return false;
            }
            DElog.i(TAG, "xnit getConfigFromeXML success!");
            return true;
        } catch (XmlPullParserException e5) {
            e = e5;
        } catch (IOException e6) {
            e = e6;
            String str822 = TAG;
            DElog.e(str822, "getConfigFromXML error! Exception " + e);
            DElog.e(TAG, "getConfigFromXML failed!");
            return false;
        }
    }

    private void xccCoef_setting(float xccCoef_R, float xccCoef_G, float xccCoef_B) {
        if (xccCoef_R <= 1.0f && xccCoef_R >= 0.1f && xccCoef_G <= 1.0f && xccCoef_G >= 0.1f && xccCoef_B <= 1.0f && xccCoef_B >= 0.1f) {
            PersistableBundle bundle = new PersistableBundle();
            bundle.putIntArray("Buffer", new int[]{(int) (xccCoef_R * 32768.0f), (int) (xccCoef_G * 32768.0f), (int) (32768.0f * xccCoef_B)});
            bundle.putInt("BufferLength", 12);
            this.mDisplayEngineManager.setData(5, bundle);
        }
    }

    public int setXnit(int mNormalizedMinBrightness, int mNormalizedMaxBrightness, int level) {
        int brightnessvalue = level;
        if (this.mSupportXCC == 0 || !this.mNeedAdjustMinLum || this.mAddedPointForExpectedMinLum == 0 || 0.0f == this.mXccCoefForExpectedMinLum[0][0]) {
            return (((level - 156) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9844) + mNormalizedMinBrightness;
        }
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
                } else {
                    brightnessvalue = mNormalizedMinBrightness;
                    float xccCoef_R = this.mXccCoefForExpectedMinLum[0][0] + ((((float) (level - 156)) / 40.0f) * (1.0f - this.mXccCoefForExpectedMinLum[0][0]));
                    xccCoef_setting(xccCoef_R, this.mXccCoefForExpectedMinLum[0][1] + ((((float) (level - 156)) / 40.0f) * (1.0f - this.mXccCoefForExpectedMinLum[0][1])), this.mXccCoefForExpectedMinLum[0][2] + ((((float) (level - 156)) / 40.0f) * (1.0f - this.mXccCoefForExpectedMinLum[0][2])));
                    this.mXccCoef_is_reseted = false;
                    String str = TAG;
                    DElog.i(str, "xnit xccCoef_R = " + xccCoef_R + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                    break;
                }
            case 2:
                if (level >= 196) {
                    if (level > 235) {
                        if (!this.mXccCoef_is_reseted) {
                            xccCoef_setting(1.0f, 1.0f, 1.0f);
                        }
                        this.mXccCoef_is_reseted = true;
                        brightnessvalue = mNormalizedMinBrightness + (((level - 235) * (mNormalizedMaxBrightness - mNormalizedMinBrightness)) / 9765);
                        break;
                    } else {
                        brightnessvalue = mNormalizedMinBrightness;
                        float xccCoef_R2 = this.mXccCoefForExpectedMinLum[1][0] + ((((float) (level - 196)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[1][0]));
                        xccCoef_setting(xccCoef_R2, this.mXccCoefForExpectedMinLum[1][1] + ((((float) (level - 196)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[1][1])), this.mXccCoefForExpectedMinLum[1][2] + ((((float) (level - 196)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[1][2])));
                        this.mXccCoef_is_reseted = false;
                        String str2 = TAG;
                        DElog.i(str2, "xnit xccCoef_R = " + xccCoef_R2 + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                        break;
                    }
                } else {
                    brightnessvalue = mNormalizedMinBrightness;
                    float xccCoef_R3 = this.mXccCoefForExpectedMinLum[0][0] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][0] - this.mXccCoefForExpectedMinLum[0][0])) / 40.0f);
                    xccCoef_setting(xccCoef_R3, this.mXccCoefForExpectedMinLum[0][1] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][1] - this.mXccCoefForExpectedMinLum[0][1])) / 40.0f), this.mXccCoefForExpectedMinLum[0][2] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][2] - this.mXccCoefForExpectedMinLum[0][2])) / 40.0f));
                    this.mXccCoef_is_reseted = false;
                    String str3 = TAG;
                    DElog.i(str3, "xnit xccCoef_R = " + xccCoef_R3 + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                    break;
                }
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
                        } else {
                            brightnessvalue = mNormalizedMinBrightness;
                            float xccCoef_R4 = this.mXccCoefForExpectedMinLum[2][0] + ((((float) (level - 235)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[2][0]));
                            xccCoef_setting(xccCoef_R4, this.mXccCoefForExpectedMinLum[2][1] + ((((float) (level - 235)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[2][1])), this.mXccCoefForExpectedMinLum[2][2] + ((((float) (level - 235)) / 39.0f) * (1.0f - this.mXccCoefForExpectedMinLum[2][2])));
                            this.mXccCoef_is_reseted = false;
                            String str4 = TAG;
                            DElog.i(str4, "xnit xccCoef_R = " + xccCoef_R4 + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                            break;
                        }
                    } else {
                        brightnessvalue = mNormalizedMinBrightness;
                        float xccCoef_R5 = this.mXccCoefForExpectedMinLum[1][0] + ((((float) (level - 196)) * (this.mXccCoefForExpectedMinLum[2][0] - this.mXccCoefForExpectedMinLum[1][0])) / 39.0f);
                        xccCoef_setting(xccCoef_R5, this.mXccCoefForExpectedMinLum[1][1] + ((((float) (level - 196)) * (this.mXccCoefForExpectedMinLum[2][1] - this.mXccCoefForExpectedMinLum[1][1])) / 39.0f), this.mXccCoefForExpectedMinLum[1][2] + ((((float) (level - 196)) * (this.mXccCoefForExpectedMinLum[2][2] - this.mXccCoefForExpectedMinLum[1][2])) / 39.0f));
                        this.mXccCoef_is_reseted = false;
                        String str5 = TAG;
                        DElog.i(str5, "xnit xccCoef_R = " + xccCoef_R5 + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                        break;
                    }
                } else {
                    brightnessvalue = mNormalizedMinBrightness;
                    float xccCoef_R6 = this.mXccCoefForExpectedMinLum[0][0] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][0] - this.mXccCoefForExpectedMinLum[0][0])) / 40.0f);
                    xccCoef_setting(xccCoef_R6, this.mXccCoefForExpectedMinLum[0][1] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][1] - this.mXccCoefForExpectedMinLum[0][1])) / 40.0f), this.mXccCoefForExpectedMinLum[0][2] + ((((float) (level - 156)) * (this.mXccCoefForExpectedMinLum[1][2] - this.mXccCoefForExpectedMinLum[0][2])) / 40.0f));
                    this.mXccCoef_is_reseted = false;
                    String str6 = TAG;
                    DElog.i(str6, "xnit xccCoef_R = " + xccCoef_R6 + ", input_level = " + level + ", addedXccNumbers = " + this.mAddedPointForExpectedMinLum);
                    break;
                }
        }
        return brightnessvalue;
    }
}
