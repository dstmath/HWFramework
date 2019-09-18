package com.huawei.pgmng.plug;

import android.util.Log;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BackLightAdjCfg {
    private static final String TAG = "BackLightAdjCfg";
    private static ArrayList<String> mNeedAdjBackLightApp;

    static {
        mNeedAdjBackLightApp = null;
        mNeedAdjBackLightApp = getNeedAdjBackLightAppFromCfg("xml/backlight_adj_app.xml");
    }

    /* access modifiers changed from: package-private */
    public boolean isApkShouldAdjBackLight(String pkgName) {
        if (pkgName == null || mNeedAdjBackLightApp == null) {
            return false;
        }
        return mNeedAdjBackLightApp.contains(pkgName);
    }

    private static ArrayList<String> getNeedAdjBackLightAppFromCfg(String fileName) {
        FileInputStream stream = null;
        ArrayList<String> retList = new ArrayList<>();
        try {
            File file = HwCfgFilePolicy.getCfgFile(fileName, 0);
            if (file == null || !file.exists()) {
                return retList;
            }
            XmlPullParser mParser = Xml.newPullParser();
            try {
                FileInputStream stream2 = new FileInputStream(file);
                mParser.setInput(stream2, null);
                for (int event = mParser.getEventType(); event != 1; event = mParser.next()) {
                    String name = mParser.getName();
                    switch (event) {
                        case 2:
                            break;
                        case 3:
                            if (name.equals("popular_app")) {
                                retList.add(mParser.getAttributeValue(null, "pkg_name"));
                                break;
                            }
                            break;
                    }
                }
                try {
                    stream2.close();
                } catch (IOException e) {
                    Log.e(TAG, "File Stream close IOException!");
                }
            } catch (FileNotFoundException e2) {
                retList.clear();
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e3) {
                retList.clear();
                if (stream != null) {
                    stream.close();
                }
            } catch (XmlPullParserException e4) {
                retList.clear();
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "File Stream close IOException!");
                    }
                }
                throw th;
            }
            return retList;
        } catch (NoClassDefFoundError e6) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return retList;
        }
    }
}
