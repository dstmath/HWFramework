package com.huawei.aod;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.util.Xml;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AodGmpConfigParser {
    private static final boolean DEBUG = false;
    private static final String ENGINE_GMP_PATH = "/odm/etc/display/effect/displayengine/";
    private static final String GMP_FILE = "engine_gmp.xml";
    private static final int HEX_DECIMAL = 16;
    private static final int MAX_GMP_LUT_LENTH = 4913;
    private static final int MAX_TAG_COUNT = 10;
    private static final String TAG = "AodGmpConfigParser";
    private static final int TWO_RADIO = 2;
    private static int[] sDisplayGmpHighParam = new int[MAX_GMP_LUT_LENTH];
    private static long[] sDisplayGmpLowParam = new long[MAX_GMP_LUT_LENTH];
    private static AodGmpConfigParser sInstance = null;
    private static String sStringGmpHighConfig;
    private static String sStringGmpLowConfig;

    private AodGmpConfigParser() {
    }

    public static synchronized AodGmpConfigParser getInstance() {
        AodGmpConfigParser aodGmpConfigParser;
        synchronized (AodGmpConfigParser.class) {
            if (sInstance == null) {
                sInstance = new AodGmpConfigParser();
            }
            aodGmpConfigParser = sInstance;
        }
        return aodGmpConfigParser;
    }

    public long[] getGmpLowPara() {
        return sDisplayGmpLowParam;
    }

    public int[] getGmpHighPara() {
        return sDisplayGmpHighParam;
    }

    public int getGmpParamLength() {
        return MAX_GMP_LUT_LENTH;
    }

    public static boolean isGmpCfgExist() {
        return new File("/odm/etc/display/effect/displayengine/engine_gmp.xml").exists();
    }

    public static boolean parserThemeXml() {
        String tag = getGmpTagName();
        if (tag == null) {
            Slog.w(TAG, "Gmp tag doesn't exist!");
            return false;
        }
        File file = new File(getGmpFileName());
        if (!file.exists()) {
            Slog.e(TAG, "Gmp file doesn't exist!");
            return false;
        }
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = new FileInputStream(file);
            XmlPullParser xmlPullParser = Xml.newPullParser();
            String str = null;
            xmlPullParser.setInput(inputStream2, null);
            int eventType = xmlPullParser.getEventType();
            while (eventType != 1) {
                String tagName = xmlPullParser.getName();
                int i = 2;
                if (eventType != 2) {
                    if (eventType != 3) {
                    }
                } else if ("mode".equals(tagName) && tag.equals(xmlPullParser.getAttributeValue(str, AodThemeConst.THEME_NAME_KEY))) {
                    Slog.i(TAG, "parserThemeXml enter MODE_UI");
                    int i2 = 0;
                    while (i2 < 10) {
                        if (xmlPullParser.next() == i && "param".equals(xmlPullParser.getName())) {
                            String paraName = xmlPullParser.getAttributeValue(str, AodThemeConst.THEME_NAME_KEY);
                            Slog.i(TAG, "parserThemeXml() paraName :" + paraName);
                            if ("gmp_lut_low32".equals(paraName)) {
                                sStringGmpLowConfig = xmlPullParser.nextText();
                            } else if ("gmp_lut_high4".equals(paraName)) {
                                sStringGmpHighConfig = xmlPullParser.nextText();
                            }
                        }
                        i2++;
                        str = null;
                        i = 2;
                    }
                }
                eventType = xmlPullParser.next();
                str = null;
            }
            try {
                inputStream2.close();
            } catch (IOException e) {
                Slog.e(TAG, "AodGmpConfigParser() --> close input stream makes IOException");
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "AodGmpConfigParser() --> FileNotFoundException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (XmlPullParserException e3) {
            Slog.e(TAG, "AodGmpConfigParser() --> XmlPullParserException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e4) {
            Slog.e(TAG, "AodGmpConfigParser() --> IOException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable e5) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "AodGmpConfigParser() --> close input stream makes IOException");
                }
            }
            throw e5;
        }
        setEngineGmpPara();
        return true;
    }

    private static void setEngineGmpPara() {
        setEngineGmpPara(sStringGmpLowConfig, 0);
        setEngineGmpPara(sStringGmpHighConfig, 1);
    }

    private static void setEngineGmpPara(String gmpPara, int type) {
        if (gmpPara != null) {
            String[] stringSplited = gmpPara.replaceAll("\\s*", "").split(",");
            if (stringSplited.length > 0) {
                for (int i = 0; i < stringSplited.length; i++) {
                    String gmpStr = stringSplited[i];
                    if ("0x".equals(gmpStr.substring(0, 2))) {
                        if (type == 0) {
                            sDisplayGmpLowParam[i] = Long.parseLong(gmpStr.substring(2, gmpStr.length()), 16);
                        } else {
                            sDisplayGmpHighParam[i] = Integer.parseInt(gmpStr.substring(2, gmpStr.length()), 16);
                        }
                    }
                }
            }
        }
    }

    private static String toGmpString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AodGmpConfigParser. gmp_lut_low32 :");
        String[] stringSplited = sStringGmpLowConfig.replaceAll("\\s*", "").split(",");
        if (stringSplited.length > 0) {
            for (String str : stringSplited) {
                stringBuilder.append(str);
            }
        }
        return stringBuilder.toString();
    }

    private static String getGmpFileName() {
        return getGmpFeatureName(3, 5);
    }

    private static String getGmpTagName() {
        return getGmpFeatureName(3, 11);
    }

    private static String getGmpFeatureName(int feature, int type) {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        if (binder == null) {
            Slog.w(TAG, "getGMPFileName binder is null");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.w(TAG, "getGMPFileName mService is null");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(feature, type, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getGMPFileName getEffect failed ret=" + ret);
                return null;
            }
            String panelName = null;
            try {
                panelName = new String(name, "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type");
            }
            Slog.i(TAG, "getGmpFeatureName, feature = " + feature + ", type = " + type + ", Name =" + panelName);
            return panelName;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getGMPFileName RemoteException " + e2);
            return null;
        }
    }
}
