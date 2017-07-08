package huawei.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
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
    private static final int EYE_PROTECTIION_MODE = 0;
    private static final String EYE_PROTECTION_SCENE_SWITCH = "eye_protection_scene_switch";
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final int MODE_BACKLIGHT = 2;
    private static final String TAG = "EyeProtectionSpline";
    private boolean mBootCompleted;
    private BootCompletedReceiver mBootCompletedReceiver;
    private String mConfigFilePath;
    private Context mContext;
    List<Point> mEyeProtectionBrighnessLinePointsList;
    private ContentObserver mEyeProtectionModeObserver;
    private boolean mIsEyeProtectionBrightnessLineOK;
    private boolean mIsEyeProtectionMode;

    /* renamed from: huawei.android.util.HwCustEyeProtectionSplineImpl.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwCustEyeProtectionSplineImpl.this.updateBrightnessMode();
        }
    }

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.util.HwCustEyeProtectionSplineImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.util.HwCustEyeProtectionSplineImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.util.HwCustEyeProtectionSplineImpl.<clinit>():void");
    }

    public HwCustEyeProtectionSplineImpl(Context context) {
        super(context);
        this.mEyeProtectionBrighnessLinePointsList = null;
        this.mConfigFilePath = null;
        this.mIsEyeProtectionMode = false;
        this.mIsEyeProtectionBrightnessLineOK = false;
        this.mBootCompleted = false;
        this.mEyeProtectionModeObserver = new AnonymousClass1(new Handler());
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
                } else if (lastPoint.x >= tmpPoint.x) {
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return false;
                } else {
                    lastPoint = tmpPoint;
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
                if (DEBUG) {
                    Slog.i(TAG, "getConfigFromeXML success!");
                }
                return true;
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
