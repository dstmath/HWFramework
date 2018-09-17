package huawei.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
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

public class HwCustEyeProtectionSplineImpl extends HwCustEyeProtectionSpline {
    private static boolean DEBUG = false;
    private static final float DEFAULT_BRIGHTNESS = 100.0f;
    private static final int EYE_PROTECTIION_MODE = SystemProperties.getInt("ro.config.hw_eyes_protection", EYE_PROTECTIION_MODE);
    private static final String EYE_PROTECTION_SCENE_SWITCH = "eye_protection_scene_switch";
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final int MODE_BACKLIGHT = 2;
    private static final String TAG = "EyeProtectionSpline";
    private boolean mBootCompleted = false;
    private BootCompletedReceiver mBootCompletedReceiver;
    private String mConfigFilePath = null;
    private Context mContext;
    List<Point> mEyeProtectionBrighnessLinePointsList = null;
    private ContentObserver mEyeProtectionModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwCustEyeProtectionSplineImpl.this.updateBrightnessMode();
        }
    };
    private boolean mIsEyeProtectionBrightnessLineOK = false;
    private boolean mIsEyeProtectionMode = false;

    private class BootCompletedReceiver extends BroadcastReceiver {
        public BootCompletedReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            HwCustEyeProtectionSplineImpl.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Slog.i(HwCustEyeProtectionSplineImpl.TAG, "onReceive intent action = " + intent.getAction());
                if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                    HwCustEyeProtectionSplineImpl.this.mBootCompleted = true;
                    if ((HwCustEyeProtectionSplineImpl.EYE_PROTECTIION_MODE & HwCustEyeProtectionSplineImpl.MODE_BACKLIGHT) != 0) {
                        HwCustEyeProtectionSplineImpl.this.updateBrightnessMode();
                    }
                }
            }
        }
    }

    private class Point {
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

    public HwCustEyeProtectionSplineImpl(Context context) {
        super(context);
        this.mContext = context;
        if ((EYE_PROTECTIION_MODE & MODE_BACKLIGHT) != 0) {
            this.mBootCompletedReceiver = new BootCompletedReceiver();
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_EYES_PROTECTION), true, this.mEyeProtectionModeObserver, -1);
            try {
                if (!getConfig()) {
                    Slog.e(TAG, "getConfig failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBrightnessMode() {
        boolean z = true;
        if (System.getIntForUser(this.mContext.getContentResolver(), KEY_EYES_PROTECTION, EYE_PROTECTIION_MODE, -2) != 1) {
            z = false;
        }
        this.mIsEyeProtectionMode = z;
        Slog.i(TAG, "updateBrightnessMode mIsEyeProtectionMode = " + this.mIsEyeProtectionMode);
    }

    public boolean IsEyeProtectionMode() {
        return this.mIsEyeProtectionMode ? this.mIsEyeProtectionBrightnessLineOK : false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00ee  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00ff  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfig() throws IOException {
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        if (version == null || version.length() == 0) {
            Slog.e(TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited.length < MODE_BACKLIGHT) {
            Slog.e(TAG, "split failed! version = " + version);
            return false;
        }
        String emuiVersion = versionSplited[1];
        if (emuiVersion == null || emuiVersion.length() == 0) {
            Slog.e(TAG, "get emuiVersion failed!");
            return false;
        }
        Object[] objArr = new Object[MODE_BACKLIGHT];
        objArr[EYE_PROTECTIION_MODE] = emuiVersion;
        objArr[1] = HW_LABC_CONFIG_FILE;
        String xmlPath = String.format("/xml/lcd/%s/%s", objArr);
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, EYE_PROTECTIION_MODE);
        if (xmlFile == null) {
            Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
            return false;
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
            } catch (FileNotFoundException e4) {
                e = e4;
                inputStream = inputStream2;
                e.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                return false;
            } catch (IOException e5) {
                e2 = e5;
                inputStream = inputStream2;
                e2.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                return false;
            } catch (Exception e6) {
                e3 = e6;
                inputStream = inputStream2;
                try {
                    e3.printStackTrace();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = inputStream2;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (inputStream != null) {
            }
            return false;
        } catch (IOException e8) {
            e2 = e8;
            e2.printStackTrace();
            if (inputStream != null) {
            }
            return false;
        } catch (Exception e9) {
            e3 = e9;
            e3.printStackTrace();
            if (inputStream != null) {
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
        } else if (LinePointsList.size() <= MODE_BACKLIGHT || LinePointsList.size() >= 100) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return false;
        } else {
            int mDrkenNum = EYE_PROTECTIION_MODE;
            Point lastPoint = null;
            for (Point tmpPoint : LinePointsList) {
                if (mDrkenNum != 0 && lastPoint.x >= tmpPoint.x) {
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return false;
                }
                lastPoint = tmpPoint;
                mDrkenNum++;
            }
            return true;
        }
    }

    private void printConfigFromXML() {
        for (Point temp : this.mEyeProtectionBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_EyeProtectionBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x002c A:{Catch:{ XmlPullParserException -> 0x0097, IOException -> 0x00e6, NumberFormatException -> 0x00e1, Exception -> 0x00dc }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfigFromXML(InputStream inStream) {
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean EyeProtectionBrighnessLinePointsListsLoadStarted = false;
        boolean EyeProtectionBrighnessLinePointsListLoaded = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String name;
                switch (eventType) {
                    case MODE_BACKLIGHT /*2*/:
                        name = parser.getName();
                        if (!name.equals("EyeProtectionBrightnessPoints")) {
                            if (name.equals("Point") && EyeProtectionBrighnessLinePointsListsLoadStarted) {
                                Point curPoint = new Point();
                                String s = parser.nextText();
                                curPoint.x = Float.parseFloat(s.split(",")[EYE_PROTECTIION_MODE]);
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
                        name = parser.getName();
                        if (name.equals("LABCConfig") && false) {
                            loadFinished = true;
                            break;
                        } else if (name.equals("EyeProtectionBrightnessPoints")) {
                            EyeProtectionBrighnessLinePointsListsLoadStarted = false;
                            if (this.mEyeProtectionBrighnessLinePointsList != null) {
                                EyeProtectionBrighnessLinePointsListLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no EyeProtectionBrightnessPoints loaded!");
                            return false;
                        }
                        break;
                }
                if (loadFinished) {
                    if (EyeProtectionBrighnessLinePointsListLoaded) {
                        if (DEBUG) {
                            Slog.i(TAG, "getConfigFromeXML success!");
                        }
                        return true;
                    }
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                }
            }
            if (EyeProtectionBrighnessLinePointsListLoaded) {
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        Slog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    public float getEyeProtectionBrightnessLevel(float lux) {
        int count = EYE_PROTECTIION_MODE;
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
