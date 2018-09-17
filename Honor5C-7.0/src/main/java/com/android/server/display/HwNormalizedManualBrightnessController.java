package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks;
import com.android.server.wifipro.WifiProCommonDefs;
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

public class HwNormalizedManualBrightnessController extends ManualBrightnessController implements LightSensorCallbacks {
    private static boolean DEBUG = false;
    private static final int DEFAULT = 0;
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private static final int INDOOR = 1;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 300;
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int OUTDOOR = 2;
    private static String TAG;
    private String mConfigFilePath;
    List<Point> mDefaultBrighnessLinePointsList;
    private float mDefaultBrightness;
    private HwLightSensorController mLightSensorController;
    private boolean mLightSensorEnable;
    private int mLightSensorRateMillis;
    private int mManualAmbientLux;
    private int mManualBrightnessMaxLimit;
    private int mManualBrightnessMinLimit;
    private boolean mManualMode;
    private boolean mManualModeEnable;
    private int mManualbrightness;
    private int mManualbrightnessOut;
    private boolean mNeedUpdateManualBrightness;
    private HwNormalizedManualBrightnessThresholdDetector mOutdoorDetector;
    private int mOutdoorScene;

    private static class Point {
        float x;
        float y;

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
    }

    static {
        TAG = "HwNormalizedManualBrightnessController";
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwNormalizedManualBrightnessController(ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
        super(callbacks);
        this.mLightSensorController = null;
        this.mLightSensorRateMillis = LIGHT_SENSOR_RATE_MILLIS;
        this.mOutdoorDetector = null;
        this.mConfigFilePath = null;
        this.mManualMode = false;
        this.mManualbrightness = -1;
        this.mManualbrightnessOut = -1;
        this.mDefaultBrighnessLinePointsList = null;
        this.mDefaultBrightness = 100.0f;
        this.mManualBrightnessMaxLimit = MAXDEFAULTBRIGHTNESS;
        this.mManualBrightnessMinLimit = 4;
        this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mLightSensorRateMillis);
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultConfig();
        }
        this.mOutdoorDetector = new HwNormalizedManualBrightnessThresholdDetector(this.mConfigFilePath);
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case OUTDOOR /*2*/:
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                return true;
            default:
                return false;
        }
    }

    public void updatePowerState(int state, boolean enable) {
        if (this.mManualModeEnable != enable) {
            if (DEBUG) {
                Slog.i(TAG, "HBM SensorEnable change " + this.mManualModeEnable + " -> " + enable);
            }
            this.mManualModeEnable = enable;
        }
        if (this.mManualModeEnable) {
            setLightSensorEnabled(wantScreenOn(state));
        } else {
            setLightSensorEnabled(this.mManualModeEnable);
        }
    }

    private void setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnable) {
                this.mLightSensorEnable = true;
                this.mLightSensorController.enableSensor();
                if (DEBUG) {
                    Slog.i(TAG, "HBM ManualMode sensor enable");
                }
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            this.mLightSensorController.disableSensor();
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
            if (DEBUG) {
                Slog.i(TAG, "HBM ManualMode sensor disenable");
            }
        }
    }

    public void updateManualBrightness(int brightness) {
        this.mManualbrightness = brightness;
        this.mManualbrightnessOut = brightness;
    }

    public int getManualBrightness() {
        if (!this.mManualMode) {
            this.mManualbrightnessOut = this.mManualbrightness;
            if (DEBUG) {
                Slog.i(TAG, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",mManualMode=" + this.mManualMode);
            }
        } else if (this.mManualbrightnessOut >= this.mManualBrightnessMaxLimit) {
            float defaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, (float) this.mManualAmbientLux);
            if (this.mOutdoorScene == OUTDOOR) {
                this.mManualbrightnessOut = Math.max(Math.min(this.mManualbrightnessOut, this.mManualBrightnessMaxLimit), (int) defaultBrightness);
                if (DEBUG) {
                    Slog.i(TAG, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            } else {
                this.mManualbrightnessOut = Math.min(this.mManualbrightnessOut, this.mManualBrightnessMaxLimit);
                if (DEBUG) {
                    Slog.i(TAG, "mManualbrightnessOut1=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            }
        }
        return this.mManualbrightnessOut;
    }

    public int getMaxBrightnessForSeekbar() {
        if (!this.mManualMode) {
            this.mManualBrightnessMaxLimit = MAXDEFAULTBRIGHTNESS;
        }
        return this.mManualBrightnessMaxLimit;
    }

    public void processSensorData(long timeInMs, int lux) {
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForHBM();
        this.mNeedUpdateManualBrightness = this.mOutdoorDetector.getLuxChangedFlagForHBM();
        this.mManualAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForHBM();
        if (this.mNeedUpdateManualBrightness) {
            this.mCallbacks.updateManualBrightnessForLux();
            this.mOutdoorDetector.setLuxChangedFlagForHBM();
            if (DEBUG) {
                Slog.i(TAG, "mManualAmbientLux =" + this.mManualAmbientLux + ",mNeedUpdateManualBrightness1=" + this.mNeedUpdateManualBrightness);
            }
        }
    }

    private boolean getConfig() throws IOException {
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        if (version == null || version.length() == 0) {
            Slog.e(TAG, "get ro.build.version.emui failed!");
            return false;
        } else if (version.split("EmotionUI_").length < OUTDOOR) {
            Slog.e(TAG, "split failed! version = " + version);
            return false;
        } else {
            String emuiVersion = version.split("EmotionUI_")[INDOOR];
            if (emuiVersion == null || emuiVersion.length() == 0) {
                Slog.e(TAG, "get emuiVersion failed!");
                return false;
            }
            Object[] objArr = new Object[OUTDOOR];
            objArr[DEFAULT] = emuiVersion;
            objArr[INDOOR] = HW_LABC_CONFIG_FILE;
            File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", objArr), DEFAULT);
            if (xmlFile == null) {
                objArr = new Object[INDOOR];
                objArr[DEFAULT] = HW_LABC_CONFIG_FILE;
                xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", objArr), DEFAULT);
                if (xmlFile == null) {
                    return false;
                }
            }
            FileInputStream fileInputStream = null;
            try {
                FileInputStream inputStream = new FileInputStream(xmlFile);
                try {
                    if (getConfigFromXML(inputStream)) {
                        checkConfigLoadedFromXML();
                        printConfigFromXML();
                        this.mConfigFilePath = xmlFile.getAbsolutePath();
                        if (DEBUG) {
                            Slog.i(TAG, "get xmlFile :" + this.mConfigFilePath);
                        }
                        inputStream.close();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        return true;
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    fileInputStream = inputStream;
                    return false;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    fileInputStream = inputStream;
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return false;
                } catch (IOException e5) {
                    e2 = e5;
                    fileInputStream = inputStream;
                    e2.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return false;
                } catch (Exception e6) {
                    e3 = e6;
                    fileInputStream = inputStream;
                    try {
                        e3.printStackTrace();
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = inputStream;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                e.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (IOException e8) {
                e2 = e8;
                e2.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (Exception e9) {
                e3 = e9;
                e3.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            }
        }
    }

    private void checkConfigLoadedFromXML() {
        if (this.mManualBrightnessMaxLimit <= 0 || this.mManualBrightnessMinLimit > MAXDEFAULTBRIGHTNESS) {
            loadDefaultConfig();
            Slog.e(TAG, "checkConfig failed for mManualBrightnessMaxLimit > mManualBrightnessMinLimit , LoadDefaultConfig!");
            return;
        }
        Slog.i(TAG, "checkConfig LoadedFromXML success!");
    }

    private void loadDefaultConfig() {
        Slog.i(TAG, "loadDefaultConfig");
        this.mLightSensorRateMillis = LIGHT_SENSOR_RATE_MILLIS;
        this.mManualMode = false;
        this.mManualBrightnessMaxLimit = MAXDEFAULTBRIGHTNESS;
        this.mManualBrightnessMinLimit = 4;
        if (this.mDefaultBrighnessLinePointsList != null) {
            this.mDefaultBrighnessLinePointsList.clear();
        } else {
            this.mDefaultBrighnessLinePointsList = new ArrayList();
        }
        this.mDefaultBrighnessLinePointsList.add(new Point(0.0f, 4.0f));
        this.mDefaultBrighnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mDefaultBrighnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mDefaultBrighnessLinePointsList.add(new Point(4000.0f, 255.0f));
        this.mDefaultBrighnessLinePointsList.add(new Point(40000.0f, 255.0f));
        if (DEBUG) {
            printConfigFromXML();
        }
    }

    private void printConfigFromXML() {
        Slog.i(TAG, "LoadXMLConfig_mLightSensorRateMillis=" + this.mLightSensorRateMillis + ",mManualMode=" + this.mManualMode + ",mManualBrightnessMaxLimit=" + this.mManualBrightnessMaxLimit + ",mManualBrightnessMinLimit=" + this.mManualBrightnessMinLimit);
        for (Point temp : this.mDefaultBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_BrightenlinePoints x = " + temp.x + ", y = " + temp.y);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfigFromXML(InputStream inStream) {
        boolean DefaultBrighnessLinePointsListsLoadStarted = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != INDOOR; eventType = parser.next()) {
                switch (eventType) {
                    case OUTDOOR /*2*/:
                        String name = parser.getName();
                        if (name != null && name.length() != 0) {
                            if (!name.equals("ManualMode")) {
                                if (!name.equals("LightSensorRateMills")) {
                                    if (!name.equals("ManualBrightnessMaxLimit")) {
                                        if (!name.equals("DefaultBrightnessPoints")) {
                                            if (name.equals("Point") && DefaultBrighnessLinePointsListsLoadStarted) {
                                                Point currentPoint = new Point();
                                                String s = parser.nextText();
                                                currentPoint.x = Float.parseFloat(s.split(",")[DEFAULT]);
                                                currentPoint.y = Float.parseFloat(s.split(",")[INDOOR]);
                                                if (this.mDefaultBrighnessLinePointsList == null) {
                                                    this.mDefaultBrighnessLinePointsList = new ArrayList();
                                                }
                                                this.mDefaultBrighnessLinePointsList.add(currentPoint);
                                                break;
                                            }
                                        }
                                        DefaultBrighnessLinePointsListsLoadStarted = true;
                                        break;
                                    }
                                    this.mManualBrightnessMaxLimit = Integer.parseInt(parser.nextText());
                                    break;
                                }
                                this.mLightSensorRateMillis = Integer.parseInt(parser.nextText());
                                break;
                            } else if (Integer.parseInt(parser.nextText()) != INDOOR) {
                                break;
                            } else {
                                this.mManualMode = true;
                                break;
                            }
                        }
                        return false;
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        if (parser.getName().equals("DefaultBrightnessPoints")) {
                            DefaultBrighnessLinePointsListsLoadStarted = false;
                            if (this.mDefaultBrighnessLinePointsList != null) {
                                break;
                            }
                            Slog.e(TAG, "no DefaultBrightnessPoints loaded!");
                            return false;
                        }
                        continue;
                    default:
                        break;
                }
            }
            if (!this.mManualMode) {
                return false;
            }
            Slog.i(TAG, "getConfigFromeXML success!");
            return true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
    }

    public float getDefaultBrightnessLevelNew(List<Point> linePointsList, float lux) {
        List<Point> linePointsListIn = linePointsList;
        int count = DEFAULT;
        float brightnessLevel = this.mDefaultBrightness;
        Point temp1 = null;
        for (Point temp : linePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                Point temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                brightnessLevel = this.mDefaultBrightness;
                if (!DEBUG) {
                    return brightnessLevel;
                }
                Slog.i(TAG, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel;
            }
            temp1 = temp;
            brightnessLevel = temp.y;
            count += INDOOR;
        }
        return brightnessLevel;
    }
}
