package huawei.android.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import android.util.Xml;
import huawei.com.android.internal.widget.HwFragmentContainer;
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
    private static final int EYE_PROTECTIION_MODE = 0;
    private static final String EYE_PROTECTION_SCENE_SWITCH = "eye_protection_scene_switch";
    private static final String HW_EYEPROTECTION_CONFIG_FILE = "EyeProtectionConfig.xml";
    private static final String HW_EYEPROTECTION_CONFIG_FILE_NAME = "EyeProtectionConfig";
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final int MODE_BACKLIGHT = 2;
    private static final String TAG = "EyeProtectionSpline";
    private String mConfigFilePath;
    private Context mContext;
    List<Point> mEyeProtectionBrighnessLinePointsList;
    private ContentObserver mEyeProtectionModeObserver;
    private boolean mIsEyeProtectionBrightnessLineOK;
    private boolean mIsEyeProtectionMode;
    private String mLcdPanelName;

    /* renamed from: huawei.android.utils.HwEyeProtectionSplineImpl.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwEyeProtectionSplineImpl.this.updateBrightnessMode();
        }
    }

    private class BootCompletedReceiver extends BroadcastReceiver {
        public BootCompletedReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            HwEyeProtectionSplineImpl.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Slog.i(HwEyeProtectionSplineImpl.TAG, "onReceive intent action = " + intent.getAction());
                if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && (HwEyeProtectionSplineImpl.EYE_PROTECTIION_MODE & HwEyeProtectionSplineImpl.MODE_BACKLIGHT) != 0) {
                    HwEyeProtectionSplineImpl.this.updateBrightnessMode();
                }
            }
        }
    }

    private static class Point {
        float x;
        float y;

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.utils.HwEyeProtectionSplineImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.utils.HwEyeProtectionSplineImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.utils.HwEyeProtectionSplineImpl.<clinit>():void");
    }

    public HwEyeProtectionSplineImpl(Context context) {
        super(context);
        this.mLcdPanelName = null;
        this.mEyeProtectionBrighnessLinePointsList = null;
        this.mConfigFilePath = null;
        this.mIsEyeProtectionMode = false;
        this.mIsEyeProtectionBrightnessLineOK = false;
        this.mEyeProtectionModeObserver = new AnonymousClass1(new Handler());
        this.mContext = context;
        if ((EYE_PROTECTIION_MODE & MODE_BACKLIGHT) != 0) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_EYES_PROTECTION), true, this.mEyeProtectionModeObserver, -1);
            this.mLcdPanelName = getLcdPanelName();
            try {
                if (!getConfig()) {
                    Slog.e(TAG, "getConfig failed!");
                }
            } catch (Exception e) {
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

    public boolean isEyeProtectionMode() {
        return this.mEyeProtectionControlFlag ? this.mIsEyeProtectionBrightnessLineOK : false;
    }

    private boolean getConfig() throws IOException {
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
        String lcdEyeProtectionConfigFile = "EyeProtectionConfig_" + this.mLcdPanelName + ".xml";
        Object[] objArr = new Object[MODE_BACKLIGHT];
        objArr[EYE_PROTECTIION_MODE] = emuiVersion;
        objArr[1] = HW_EYEPROTECTION_CONFIG_FILE;
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", objArr), EYE_PROTECTIION_MODE);
        if (xmlFile == null) {
            xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{HW_EYEPROTECTION_CONFIG_FILE}), EYE_PROTECTIION_MODE);
            if (xmlFile == null) {
                objArr = new Object[MODE_BACKLIGHT];
                objArr[EYE_PROTECTIION_MODE] = emuiVersion;
                objArr[1] = lcdEyeProtectionConfigFile;
                xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", objArr), EYE_PROTECTIION_MODE);
                if (xmlFile == null) {
                    String xmlPath = String.format("/xml/lcd/%s", new Object[]{lcdEyeProtectionConfigFile});
                    xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, EYE_PROTECTIION_MODE);
                    if (xmlFile == null) {
                        Slog.w(TAG, "get xmlFile :" + xmlPath + " failed!");
                        return false;
                    }
                }
            }
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    if (checkConfigLoadedFromXML() && DEBUG) {
                        printConfigFromXML();
                    }
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
            } catch (FileNotFoundException e) {
                fileInputStream = inputStream;
                Slog.i(TAG, "get xmlFile file not found");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (IOException e2) {
                fileInputStream = inputStream;
                Slog.i(TAG, "get xmlFile has IO exception");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (Exception e3) {
                fileInputStream = inputStream;
                try {
                    Slog.i(TAG, "get xmlFile has exception");
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
        } catch (FileNotFoundException e4) {
            Slog.i(TAG, "get xmlFile file not found");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (IOException e5) {
            Slog.i(TAG, "get xmlFile has IO exception");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (Exception e6) {
            Slog.i(TAG, "get xmlFile has exception");
            if (fileInputStream != null) {
                fileInputStream.close();
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
                if (mDrkenNum == 0) {
                    lastPoint = tmpPoint;
                } else if (lastPoint == null || lastPoint.x < tmpPoint.x) {
                    lastPoint = tmpPoint;
                } else {
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return false;
                }
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

    private String getLcdPanelName() {
        String str = null;
        try {
            str = FileUtils.readTextFile(new File(LCD_PANEL_TYPE_PATH), EYE_PROTECTIION_MODE, null).trim().replace(' ', '_');
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
                    case MODE_BACKLIGHT /*2*/:
                        String name = parser.getName();
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
                    case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
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
