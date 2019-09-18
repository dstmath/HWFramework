package android.perf;

import android.os.Environment;
import android.os.SystemProperties;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class HwOptPackageParserImpl implements HwOptPackageParser {
    private static final String TAG = "HwOptPackageParserImpl";
    private static final boolean mOptPackageListEnable = SystemProperties.getBoolean("persist.kirin.perfoptpackage_list", false);
    private Map<Integer, ArrayList<String>> mOptPackageMap = null;

    public void getOptPackages() {
        String str;
        StringBuilder sb;
        FileInputStream fis = null;
        String optType = null;
        ArrayList<String> packageList = null;
        try {
            FileInputStream fis2 = new AtomicFile(new File(new File(Environment.getRootDirectory(), "/etc"), "packages-perfopt.xml")).openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis2, null);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    switch (eventType) {
                        case 2:
                            if (!parser.getName().equals("opt_type")) {
                                if (parser.getName().equals("pkg")) {
                                    String pkgName = parser.getAttributeValue(null, "name");
                                    if (!(pkgName == null || packageList == null)) {
                                        packageList.add(pkgName);
                                    }
                                    break;
                                }
                            } else {
                                optType = parser.getAttributeValue(null, "typeid");
                                packageList = new ArrayList<>();
                                break;
                            }
                            break;
                        case 3:
                            if (parser.getName().equals("opt_type")) {
                                addOptPackageList(optType, packageList);
                                optType = null;
                                packageList = null;
                                break;
                            }
                            break;
                    }
                } else {
                    this.mOptPackageMap = new HashMap();
                }
            }
            if (fis2 != null) {
                try {
                    fis2.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    str = TAG;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
            sb.append("Error close fis: ");
            sb.append(e.getMessage());
            Slog.e(str, sb.toString());
        } catch (Exception e2) {
            Slog.e(TAG, "Error parse packages-opt: " + e2.getMessage());
            this.mOptPackageMap = null;
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3) {
                    e = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "Error close fis: " + e4.getMessage());
                }
            }
            throw th;
        }
    }

    public boolean isPerfOptEnable(String pkgName, int optTypeId) {
        if (!mOptPackageListEnable) {
            return true;
        }
        if (this.mOptPackageMap == null) {
            return false;
        }
        ArrayList<String> pkgList = this.mOptPackageMap.get(Integer.valueOf(optTypeId));
        if (pkgList == null) {
            return false;
        }
        return pkgList.contains(pkgName);
    }

    private void addOptPackageList(String optType, ArrayList<String> packageList) {
        int optTypeId;
        if (this.mOptPackageMap != null && optType != null && packageList != null) {
            try {
                optTypeId = Integer.parseInt(optType);
            } catch (NumberFormatException e) {
                Slog.e(TAG, "Errot opt type: " + e.getMessage());
                optTypeId = 0;
            }
            if (optTypeId != 0) {
                this.mOptPackageMap.put(Integer.valueOf(optTypeId), packageList);
            }
        }
    }
}
