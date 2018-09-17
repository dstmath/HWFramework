package huawei.android.utils;

import android.content.Context;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
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

public class HwEyeProtectionSplineImpl extends HwEyeProtectionSpline {
    private static boolean DEBUG = false;
    private static final float DEFAULT_BRIGHTNESS = 100.0f;
    private static final int EYE_PROTECTIION_MODE = SystemProperties.getInt("ro.config.hw_eyes_protection", 7);
    private static final String HW_EYEPROTECTION_CONFIG_FILE = "EyeProtectionConfig.xml";
    private static final String HW_EYEPROTECTION_CONFIG_FILE_NAME = "EyeProtectionConfig";
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final int MODE_BACKLIGHT = 2;
    private static final String TAG = "EyeProtectionSpline";
    private String mConfigFilePath = null;
    List<Point> mEyeProtectionBrighnessLinePointsList = null;
    private boolean mIsEyeProtectionBrightnessLineOK = false;
    private String mLcdPanelName = null;

    private static class Point {
        float x;
        float y;

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwEyeProtectionSplineImpl(Context context) {
        super(context);
        if ((EYE_PROTECTIION_MODE & 2) != 0) {
            this.mLcdPanelName = getLcdPanelName();
            try {
                if (!getConfig()) {
                    Slog.e(TAG, "getConfig failed!");
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean isEyeProtectionMode() {
        return this.mEyeProtectionControlFlag ? this.mIsEyeProtectionBrightnessLineOK : false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0172  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0162  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0179  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfig() throws IOException {
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        if (version == null || version.length() == 0) {
            Slog.e(TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited.length < 2) {
            Slog.e(TAG, "split failed! version = " + version);
            return false;
        }
        String emuiVersion = versionSplited[1];
        if (emuiVersion == null || emuiVersion.length() == 0) {
            Slog.e(TAG, "get emuiVersion failed!");
            return false;
        }
        String lcdEyeProtectionConfigFile = "EyeProtectionConfig_" + this.mLcdPanelName + ".xml";
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, HW_EYEPROTECTION_CONFIG_FILE}), 0);
        if (xmlFile == null) {
            xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{HW_EYEPROTECTION_CONFIG_FILE}), 0);
            if (xmlFile == null) {
                xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, lcdEyeProtectionConfigFile}), 0);
                if (xmlFile == null) {
                    String xmlPath = String.format("/xml/lcd/%s", new Object[]{lcdEyeProtectionConfigFile});
                    xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
                    if (xmlFile == null) {
                        Slog.w(TAG, "get xmlFile :" + xmlPath + " failed!");
                        return false;
                    }
                }
            }
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream2)) {
                    if (checkConfigLoadedFromXML() && DEBUG) {
                        printConfigFromXML();
                    }
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    if (DEBUG) {
                        Slog.i(TAG, "get xmlFile :" + this.mConfigFilePath);
                    }
                    inputStream2.close();
                    if (inputStream2 != null) {
                        inputStream2.close();
                    }
                    return true;
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                inputStream = inputStream2;
                return false;
            } catch (FileNotFoundException e) {
                inputStream = inputStream2;
                Slog.i(TAG, "get xmlFile file not found");
                if (inputStream != null) {
                }
                return false;
            } catch (IOException e2) {
                inputStream = inputStream2;
                Slog.i(TAG, "get xmlFile has IO exception");
                if (inputStream != null) {
                }
                return false;
            } catch (Exception e3) {
                inputStream = inputStream2;
                try {
                    Slog.i(TAG, "get xmlFile has exception");
                    if (inputStream != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        inputStream.close();
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
        } catch (FileNotFoundException e4) {
            Slog.i(TAG, "get xmlFile file not found");
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        } catch (IOException e5) {
            Slog.i(TAG, "get xmlFile has IO exception");
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        } catch (Exception e6) {
            Slog.i(TAG, "get xmlFile has exception");
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        }
    }

    private boolean checkConfigLoadedFromXML() {
        if (checkPointsListIsOK(this.mEyeProtectionBrighnessLinePointsList)) {
            this.mIsEyeProtectionBrightnessLineOK = true;
            if (DEBUG) {
                Slog.i(TAG, "checkConfigLoadedFromXML success!");
            }
            return true;
        }
        this.mIsEyeProtectionBrightnessLineOK = false;
        Slog.e(TAG, "checkPointsList mEyeProtectionBrighnessLinePointsList is wrong, use DefaultBrighnessLine!");
        return false;
    }

    private boolean checkPointsListIsOK(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        if (LinePointsList == null) {
            Slog.e(TAG, "LoadXML false for mLinePointsList == null");
            return false;
        } else if (LinePointsList.size() <= 2 || LinePointsList.size() >= 100) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return false;
        } else {
            int mDrkenNum = 0;
            Point lastPoint = null;
            for (Point tmpPoint : LinePointsList) {
                if (mDrkenNum == 0 || lastPoint == null || lastPoint.x < tmpPoint.x) {
                    lastPoint = tmpPoint;
                    mDrkenNum++;
                } else {
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return false;
                }
            }
            return true;
        }
    }

    private void printConfigFromXML() {
        for (Point temp : this.mEyeProtectionBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_EyeProtectionBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
        }
    }

    private String getLcdPanelName() {
        String str = null;
        try {
            str = FileUtils.readTextFile(new File(LCD_PANEL_TYPE_PATH), 0, null).trim().replace(' ', '_');
            Slog.d(TAG, "panelName is:" + str);
            return str;
        } catch (IOException e) {
            Slog.e(TAG, "Error reading lcd panel name", e);
            return str;
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean EyeProtectionBrighnessLinePointsListsLoadStarted = false;
        boolean EyeProtectionBrighnessLinePointsListLoaded = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 2:
                        String name = parser.getName();
                        if (!name.equals("EyeProtectionBrightnessPoints")) {
                            if (name.equals("Point") && EyeProtectionBrighnessLinePointsListsLoadStarted) {
                                Point curPoint = new Point();
                                String s = parser.nextText();
                                curPoint.x = Float.parseFloat(s.split(",")[0]);
                                curPoint.y = Float.parseFloat(s.split(",")[1]);
                                if (this.mEyeProtectionBrighnessLinePointsList == null) {
                                    this.mEyeProtectionBrighnessLinePointsList = new ArrayList();
                                }
                                this.mEyeProtectionBrighnessLinePointsList.add(curPoint);
                                break;
                            }
                        }
                        EyeProtectionBrighnessLinePointsListsLoadStarted = true;
                        break;
                    case 3:
                        if (parser.getName().equals("EyeProtectionBrightnessPoints")) {
                            EyeProtectionBrighnessLinePointsListsLoadStarted = false;
                            if (this.mEyeProtectionBrighnessLinePointsList != null) {
                                EyeProtectionBrighnessLinePointsListLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no EyeProtectionBrightnessPoints loaded!");
                            return false;
                        }
                        continue;
                    default:
                        break;
                }
            }
            if (EyeProtectionBrighnessLinePointsListLoaded) {
                Slog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            Slog.i(TAG, "getConfigFromeXML has XmlPullParserException!");
        } catch (IOException e2) {
            Slog.i(TAG, "getConfigFromeXML has IOException!");
        } catch (NumberFormatException e3) {
            Slog.i(TAG, "getConfigFromeXML has NumberFormatException!");
        } catch (Exception e4) {
            Slog.i(TAG, "getConfigFromeXML has Exception!");
        }
        Slog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    public float getEyeProtectionBrightnessLevel(float lux) {
        int count = 0;
        float brightnessLevel = DEFAULT_BRIGHTNESS;
        Point temp1 = null;
        for (Point temp : this.mEyeProtectionBrighnessLinePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                Point temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                if (!DEBUG) {
                    return DEFAULT_BRIGHTNESS;
                }
                Slog.i(TAG, "Brighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return DEFAULT_BRIGHTNESS;
            }
            temp1 = temp;
            brightnessLevel = temp.y;
            count++;
        }
        return brightnessLevel;
    }
}
