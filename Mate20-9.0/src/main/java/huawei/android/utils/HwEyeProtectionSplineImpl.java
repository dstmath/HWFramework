package huawei.android.utils;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwEyeProtectionSplineImpl extends HwEyeProtectionSpline {
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final float DEFAULT_BRIGHTNESS = 100.0f;
    private static final int EYE_PROTECTIION_MODE = SystemProperties.getInt("ro.config.hw_eyes_protection", 7);
    private static final String HW_EYEPROTECTION_CONFIG_FILE = "EyeProtectionConfig.xml";
    private static final String HW_EYEPROTECTION_CONFIG_FILE_NAME = "EyeProtectionConfig";
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final int MODE_BACKLIGHT = 2;
    private static final String TAG = "EyeProtectionSpline";
    List<Point> mEyeProtectionBrighnessLinePointsList = null;
    private boolean mIsEyeProtectionBrightnessLineOK = false;
    private String mLcdPanelName = null;

    private static class Point {
        float x;
        float y;

        public Point() {
        }

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
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
        return this.mEyeProtectionControlFlag && this.mIsEyeProtectionBrightnessLineOK;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [java.lang.String, java.io.FileInputStream] */
    /* JADX WARNING: type inference failed for: r1v3, types: [java.io.FileInputStream] */
    /* JADX WARNING: type inference failed for: r1v5 */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f1, code lost:
        if (r1 == 0) goto L_0x00f4;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private boolean getConfig() throws IOException {
        ? r1 = 0;
        String version = SystemProperties.get("ro.build.version.emui", r1);
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
                    xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{lcdEyeProtectionConfigFile}), 0);
                    if (xmlFile == null) {
                        Slog.w(TAG, "get xmlFile failed!");
                        return false;
                    }
                }
            }
        }
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            boolean configFromXML = getConfigFromXML(inputStream);
            r1 = inputStream;
            if (configFromXML) {
                if (true == checkConfigLoadedFromXML() && DEBUG) {
                    printConfigFromXML();
                }
                inputStream.close();
                inputStream.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            Slog.i(TAG, "get xmlFile file not found");
        } catch (IOException e2) {
            Slog.i(TAG, "get xmlFile has IO exception");
            if (r1 != 0) {
            }
        } catch (Exception e3) {
            Slog.i(TAG, "get xmlFile has exception");
            if (r1 != 0) {
            }
        } catch (Throwable th) {
            if (r1 != 0) {
                r1.close();
            }
            throw th;
        }
        r1.close();
        return false;
    }

    private boolean checkConfigLoadedFromXML() {
        if (!checkPointsListIsOK(this.mEyeProtectionBrighnessLinePointsList)) {
            this.mIsEyeProtectionBrightnessLineOK = false;
            Slog.e(TAG, "checkPointsList mEyeProtectionBrighnessLinePointsList is wrong, use DefaultBrighnessLine!");
            return false;
        }
        this.mIsEyeProtectionBrightnessLineOK = true;
        if (DEBUG) {
            Slog.i(TAG, "checkConfigLoadedFromXML success!");
        }
        return true;
    }

    private boolean checkPointsListIsOK(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        if (mLinePointsList == null) {
            Slog.e(TAG, "LoadXML false for mLinePointsList == null");
            return false;
        } else if (mLinePointsList.size() <= 2 || mLinePointsList.size() >= 100) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return false;
        } else {
            int mDrkenNum = 0;
            Point lastPoint = null;
            for (Point tmpPoint : mLinePointsList) {
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
        String panelName = null;
        if (getLcdPanelNameFromDisplayEngine() != null) {
            panelName = getLcdPanelNameFromDisplayEngine().trim();
        }
        if (panelName != null) {
            return panelName.replace(' ', '_');
        }
        return panelName;
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
                        } else {
                            EyeProtectionBrighnessLinePointsListsLoadStarted = true;
                            break;
                        }
                    case 3:
                        if (parser.getName().equals("EyeProtectionBrightnessPoints")) {
                            EyeProtectionBrighnessLinePointsListsLoadStarted = false;
                            if (this.mEyeProtectionBrighnessLinePointsList != null) {
                                EyeProtectionBrighnessLinePointsListLoaded = true;
                                break;
                            } else {
                                Slog.e(TAG, "no EyeProtectionBrightnessPoints loaded!");
                                return false;
                            }
                        } else {
                            continue;
                        }
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
                if (temp2.x > temp1.x) {
                    return (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                if (!DEBUG) {
                    return DEFAULT_BRIGHTNESS;
                }
                Slog.i(TAG, "Brighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return DEFAULT_BRIGHTNESS;
            }
            temp1 = temp;
            brightnessLevel = temp1.y;
            count++;
        }
        return brightnessLevel;
    }

    private String getLcdPanelNameFromDisplayEngine() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        String panelName = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            return panelName;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }
}
