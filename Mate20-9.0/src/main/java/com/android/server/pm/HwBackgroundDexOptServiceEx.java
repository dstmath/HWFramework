package com.android.server.pm;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwBackgroundDexOptServiceEx implements IHwBackgroundDexOptServiceEx {
    private static final String ATTR_NAME = "name";
    private static final String CUST_FILE_PATH = "/xml/hw_aot_compile_apps_config.xml";
    static final String TAG = "HwBackgroundDexOptServiceEx";
    private static final String TAG_NAME = "speed";
    private ArraySet<String> mSpeedModePkgs;

    public HwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner bdos, Context context) {
        this.mSpeedModePkgs = null;
        this.mSpeedModePkgs = getAllNeedForSpeedApps();
    }

    public int getReason(int reason, int reasonBackgroudDexopt, int reasonSpeedDexopt, String pkg) {
        if (this.mSpeedModePkgs == null) {
            this.mSpeedModePkgs = getAllNeedForSpeedApps();
        }
        if (this.mSpeedModePkgs == null || !this.mSpeedModePkgs.contains(pkg)) {
            return reasonBackgroudDexopt;
        }
        return reasonSpeedDexopt;
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    private ArraySet<String> getAllNeedForSpeedApps() {
        ArraySet<String> speedPkgs = null;
        try {
            File file = HwCfgFilePolicy.getCfgFile(CUST_FILE_PATH, 0);
            if (file == null) {
                Log.i(TAG, "hw_aot_compile_apps_config not exist");
            } else {
                speedPkgs = readSpeedAppsFromXml(file);
            }
        } catch (NoClassDefFoundError e) {
            Log.i(TAG, "get speed apps failed:" + e);
        } catch (Throwable th) {
            return null;
        }
        return speedPkgs;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x003a A[Catch:{ XmlPullParserException -> 0x0125, IOException -> 0x00e8, all -> 0x00e5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0066 A[SYNTHETIC, Splitter:B:17:0x0066] */
    private ArraySet<String> readSpeedAppsFromXml(File config) {
        int type;
        FileInputStream stream = null;
        if (!config.exists() || !config.canRead()) {
            return null;
        }
        try {
            stream = new FileInputStream(config);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            while (true) {
                int next = parser.next();
                type = next;
                if (next != 2 && type != 1) {
                    Slog.e(TAG, "readSpeedAppsFromXml");
                } else if (type == 2) {
                    Log.w(TAG, "Failed parsing config, can't find start tag");
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "readSpeedAppsFromXml, failed to close FileInputStream" + e.getMessage());
                    }
                    return null;
                } else {
                    ArraySet<String> speedApps = new ArraySet<>();
                    int outerDepth = parser.getDepth();
                    while (true) {
                        int next2 = parser.next();
                        int type2 = next2;
                        if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                            try {
                                stream.close();
                            } catch (IOException e2) {
                                Slog.e(TAG, "readSpeedAppsFromXml, failed to close FileInputStream" + e2.getMessage());
                            }
                            return speedApps;
                        } else if (type2 != 3) {
                            if (type2 != 4) {
                                if (parser.getName().equals(TAG_NAME)) {
                                    String name = parser.getAttributeValue(null, "name");
                                    if (!TextUtils.isEmpty(name)) {
                                        speedApps.add(name);
                                    }
                                } else {
                                    Log.w(TAG, "Unknown element under <config>: " + parser.getName());
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                        }
                    }
                }
            }
            if (type == 2) {
            }
        } catch (XmlPullParserException e3) {
            Log.w(TAG, "Failed parsing config " + e3);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "readSpeedAppsFromXml, failed to close FileInputStream" + e4.getMessage());
                }
            }
            return null;
        } catch (IOException e5) {
            Log.w(TAG, "Failed parsing config " + e5);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "readSpeedAppsFromXml, failed to close FileInputStream" + e6.getMessage());
                }
            }
            return null;
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    Slog.e(TAG, "readSpeedAppsFromXml, failed to close FileInputStream" + e7.getMessage());
                }
            }
            return null;
        }
    }
}
