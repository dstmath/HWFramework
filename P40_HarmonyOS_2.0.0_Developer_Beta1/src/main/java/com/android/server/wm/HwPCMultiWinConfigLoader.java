package com.android.server.wm;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.Xml;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwPCMultiWinConfigLoader {
    private static final Object M_LOCK = new Object();
    public static final int PARSE_XML_CONFIG_FAILED = 1;
    public static final int PARSE_XML_CONFIG_SUCCESS = 0;
    private static final int PREFIX = 2;
    private static final int RADIX_HEX = 16;
    public static final int SPECIAL_PACKAGE_TYPE_LANDSCAPE_ONLY = 32;
    public static final int SPECIAL_PACKAGE_TYPE_SUPPORT_MAGIC_WINDOW = 64;
    public static final int SPECIAL_PACKAGE_TYPE_SUPPORT_PCSIZE_FULLSCREEN = 8;
    public static final int SPECIAL_PACKAGE_TYPE_SUPPORT_PCSIZE_MAXIMIZED = 16;
    public static final int SPECIAL_PACKAGE_TYPE_SUPPORT_PC_MULTICAST_MODE = 1;
    public static final int SPECIAL_PACKAGE_TYPE_SUPPORT_ROTATE_WINDOW = 128;
    public static final int SPECIAL_PACKAGE_TYPE_UNSUPPORT_PC_MULTICAST_MODE = 2;
    public static final int SPECIAL_PACKAGE_TYPE_VIDEO_NEED_FULLSCREEN = 4;
    private static final String TAG = HwPCMultiWinConfigLoader.class.getSimpleName();
    private static final String XML_ATTRIBUTE_ABILITY = "ability";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "packagename";
    private static final String XML_ELEMENT_APP_ITEM = "app";
    private static volatile HwPCMultiWinConfigLoader sSingleInstance = null;
    private Context mContext = null;
    List<Pair<String, Integer>> mPcMultiWinConfigList = new ArrayList();

    public HwPCMultiWinConfigLoader(Context context) {
        this.mContext = context;
    }

    public static HwPCMultiWinConfigLoader getInstance(Context context) {
        if (sSingleInstance == null) {
            synchronized (M_LOCK) {
                if (sSingleInstance == null) {
                    sSingleInstance = new HwPCMultiWinConfigLoader(context);
                }
            }
        }
        return sSingleInstance;
    }

    public List<Pair<String, Integer>> getPcMultiWinConfigList() {
        return this.mPcMultiWinConfigList;
    }

    public int loadPcMultiWinConfigListFromXml(String configXml) {
        Slog.i(TAG, "loadPcMultiWinConfigListFromXml begin");
        this.mPcMultiWinConfigList.clear();
        this.mPcMultiWinConfigList = new ArrayList();
        if (TextUtils.isEmpty(configXml)) {
            return 1;
        }
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(configXml.getBytes("utf-8"));
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parserXml(parser);
            try {
                in.close();
            } catch (IOException e) {
                Log.e(TAG, "loadPcMultiWinConfigListFromXml:- IOE while closing stream");
            }
            Slog.i(TAG, "loadPcMultiWinConfigListFromXml end");
            return 0;
        } catch (IOException | XmlPullParserException e2) {
            Log.e(TAG, "loadPcMultiWinConfigListFromXml XmlPullParserException");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    Log.e(TAG, "loadPcMultiWinConfigListFromXml:- IOE while closing stream");
                }
            }
            return 1;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    Log.e(TAG, "loadPcMultiWinConfigListFromXml:- IOE while closing stream");
                }
            }
            throw th;
        }
    }

    private void parserXml(XmlPullParser parser) {
        Slog.i(TAG, "parserXml begin");
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType != 2) {
                        if (eventType != 3) {
                        }
                    } else if (XML_ELEMENT_APP_ITEM.equals(parser.getName())) {
                        String pkName = parser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                        int ability = 0;
                        try {
                            String hexAbility = parser.getAttributeValue(null, XML_ATTRIBUTE_ABILITY);
                            ability = Integer.valueOf(Integer.parseInt(hexAbility.substring(2, hexAbility.length()), 16));
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "parserXml occurs numberformat error");
                        }
                        if (pkName != null) {
                            this.mPcMultiWinConfigList.add(new Pair<>(pkName, ability));
                        }
                    } else {
                        Log.e(TAG, "parserXml getName maybe has other type");
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e2) {
            Log.e(TAG, "parserXml Exception");
        } catch (IOException e3) {
            Log.e(TAG, "parserXml IOException");
        }
        Slog.i(TAG, "parserXml end");
    }
}
