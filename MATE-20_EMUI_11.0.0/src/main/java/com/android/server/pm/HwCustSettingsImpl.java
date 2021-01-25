package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustSettingsImpl extends HwCustSettings {
    private static final String FILE_SUB_USER_NOSYSAPPS_LIST = "hdn_subuser_nosysapps_config.xml";
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    static final String TAG = "HwCustSettingsImpl";
    private static AtomicBoolean sIsCheckNosysAppsFinished = new AtomicBoolean(false);
    private ArrayList<String> mNosysAppLists = new ArrayList<>();

    public boolean isInNosysAppList(String packageName) {
        if (!IS_DOCOMO) {
            return false;
        }
        if (sIsCheckNosysAppsFinished.compareAndSet(false, true)) {
            readNosysAppsFiles();
        }
        return this.mNosysAppLists.contains(packageName);
    }

    private void readNosysAppsFiles() {
        ArrayList<File> nosysAppsFileList = new ArrayList<>();
        try {
            nosysAppsFileList = HwCfgFilePolicy.getCfgFileList("xml/hdn_subuser_nosysapps_config.xml", 0);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        Iterator<File> it = nosysAppsFileList.iterator();
        while (it.hasNext()) {
            loadNosysAppsFromXml(it.next());
        }
    }

    private void loadNosysAppsFromXml(File configFile) {
        StringBuilder sb;
        if (configFile.exists()) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(configFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int depth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                        try {
                            stream2.close();
                            return;
                        } catch (IOException e) {
                            e = e;
                            sb = new StringBuilder();
                        }
                    } else if (type == 2) {
                        if (parser.getName().equals("add_app")) {
                            this.mNosysAppLists.add(parser.getAttributeValue(0));
                        }
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "file is not exist " + e2);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (XmlPullParserException e4) {
                Slog.e(TAG, "failed parsing " + configFile + " " + e4);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e6) {
                Slog.e(TAG, "failed parsing " + configFile + " " + e6);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e7) {
                        e = e7;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "failed close stream " + e8);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.e(TAG, sb.toString());
    }
}
