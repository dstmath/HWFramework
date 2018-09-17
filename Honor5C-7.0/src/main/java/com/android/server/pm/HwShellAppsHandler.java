package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class HwShellAppsHandler {
    private static final String CONFIG_FILE = "/system/etc/dexopt/shell_identify_and_oat_clean.xml";
    private static final boolean DEBUG = false;
    private static final String INFO_TYPE_ShellByPackageName = "shellByPackageName";
    private static final String INFO_TYPE_ShellClearOat = "shellClearOat";
    private static final String INFO_TYPE_ShellIdentify = "shellIdentify";
    private static final String TAG = "HwShellAppsHandler";
    private File mAppDataDir;
    private final Installer mInstaller;
    private ArrayList<ShellItem> mShellByPackageName;
    private ArrayList<ShellItem> mShellClearOats;
    private ArrayList<ShellItem> mShellIdentifies;
    private File mUserAppDataDir;
    private UserManagerService mUserManager;

    private static class ShellItem {
        private ArrayList<String> mFiles;
        private String mName;

        public ShellItem(String name, ArrayList<String> files) {
            this.mName = name;
            this.mFiles = files;
        }

        public String getName() {
            return this.mName;
        }

        public ArrayList<String> getFiles() {
            return this.mFiles;
        }
    }

    public HwShellAppsHandler(Installer installer, UserManagerService userManager) {
        this.mShellIdentifies = null;
        this.mShellClearOats = null;
        this.mShellByPackageName = null;
        this.mShellClearOats = new ArrayList();
        this.mShellIdentifies = new ArrayList();
        this.mShellByPackageName = new ArrayList();
        parseShellConfig();
        File dataDir = Environment.getDataDirectory();
        this.mAppDataDir = new File(dataDir, "data");
        this.mUserAppDataDir = new File(dataDir, "user");
        this.mInstaller = installer;
        this.mUserManager = userManager;
    }

    public String AnalyseShell(Package pkg) {
        ArrayList<ShellItem> tmpShellIdentifies = DeepcopyShellItem(this.mShellIdentifies);
        try {
            ZipFile file = new ZipFile(pkg.baseCodePath);
            try {
                Enumeration<? extends ZipEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (!entry.isDirectory()) {
                        filterOutIdentifyFiles(entry.getName(), tmpShellIdentifies);
                    }
                }
            } catch (Exception e) {
                try {
                    Log.e(TAG, "fail to process apk file: " + pkg.baseCodePath);
                } catch (IOException e2) {
                    Log.e(TAG, "fail to process apk file: " + pkg.baseCodePath);
                    for (ShellItem shellIdentify : tmpShellIdentifies) {
                        if (shellIdentify.getFiles().size() == 0) {
                            return shellIdentify.getName();
                        }
                    }
                    return AnalyseShellByPackageName(pkg);
                }
            }
            file.close();
            ZipFile zipFile = file;
        } catch (IOException e3) {
            Log.e(TAG, "fail to process apk file: " + pkg.baseCodePath);
            for (ShellItem shellIdentify2 : tmpShellIdentifies) {
                if (shellIdentify2.getFiles().size() == 0) {
                    return shellIdentify2.getName();
                }
            }
            return AnalyseShellByPackageName(pkg);
        }
        for (ShellItem shellIdentify22 : tmpShellIdentifies) {
            if (shellIdentify22.getFiles().size() == 0) {
                return shellIdentify22.getName();
            }
        }
        return AnalyseShellByPackageName(pkg);
    }

    private String AnalyseShellByPackageName(Package pkg) {
        if (this.mShellByPackageName == null || pkg == null || pkg.packageName == null) {
            return null;
        }
        for (ShellItem shellIdentify : this.mShellByPackageName) {
            Iterator<String> i = shellIdentify.getFiles().iterator();
            while (i.hasNext()) {
                String identifyPackageName = (String) i.next();
                if (identifyPackageName != null && identifyPackageName.equals(pkg.packageName)) {
                    return shellIdentify.getName();
                }
            }
        }
        return null;
    }

    public void ProcessShellApp(Package pkg) {
        for (UserInfo userInfo : this.mUserManager.getUsers(true)) {
            for (ShellItem shellItem : this.mShellClearOats) {
                for (String fileName : shellItem.getFiles()) {
                    File file = new File(getDataPathForPackage(pkg.packageName, userInfo.id), fileName);
                    try {
                        this.mInstaller.unlink(file.getAbsolutePath());
                    } catch (InstallerException e) {
                        Log.i(TAG, "unsuccessfully unlink " + file);
                    }
                    Log.i(TAG, "successfully unlink " + file);
                }
            }
        }
    }

    private void filterOutIdentifyFiles(String fileName, ArrayList<ShellItem> tmpShellIdentifies) {
        for (ShellItem shellIdentify : tmpShellIdentifies) {
            Iterator<String> i = shellIdentify.getFiles().iterator();
            while (i.hasNext()) {
                String identifyFileName = (String) i.next();
                if (fileName.equals(identifyFileName) || fileName.endsWith("/" + identifyFileName)) {
                    i.remove();
                }
            }
        }
    }

    private File getDataPathForPackage(String packageName, int userId) {
        if (userId == 0) {
            return new File(this.mAppDataDir, packageName);
        }
        return new File(this.mUserAppDataDir.getAbsolutePath() + File.separator + userId + File.separator + packageName);
    }

    public String DumpIdentifies() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nINFO_TYPE_ShellIdentify");
        for (ShellItem shellItem : this.mShellIdentifies) {
            sb.append("    " + shellItem.getName() + "\n");
            for (String fileName : shellItem.getFiles()) {
                sb.append("        " + fileName + "\n");
            }
        }
        sb.append("\nINFO_TYPE_ClearOat");
        for (ShellItem shellItem2 : this.mShellClearOats) {
            sb.append("    " + shellItem2.getName() + "\n");
            for (String fileName2 : shellItem2.getFiles()) {
                sb.append("        " + fileName2 + "\n");
            }
        }
        return sb.toString();
    }

    private void parseShellConfig() {
        IOException e;
        Iterator<ShellItem> i;
        FileNotFoundException e2;
        XmlPullParserException e3;
        Throwable th;
        File file = new File("/system/etc", "dexopt/shell_identify_and_oat_clean.xml");
        FileInputStream fileInputStream = null;
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("dexopt/shell_identify_and_oat_clean.xml", 0);
            if (cfg != null) {
                file = cfg;
            }
            FileInputStream stream = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                ParseXml(parser);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                fileInputStream = stream;
            } catch (NoClassDefFoundError e5) {
                fileInputStream = stream;
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
                i = this.mShellIdentifies.iterator();
                while (i.hasNext()) {
                    if (((ShellItem) i.next()).getFiles().size() == 0) {
                        i.remove();
                    }
                }
            } catch (FileNotFoundException e6) {
                e2 = e6;
                fileInputStream = stream;
                Log.w(TAG, "file is not exist " + e2);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
                i = this.mShellIdentifies.iterator();
                while (i.hasNext()) {
                    if (((ShellItem) i.next()).getFiles().size() == 0) {
                        i.remove();
                    }
                }
            } catch (XmlPullParserException e7) {
                e3 = e7;
                fileInputStream = stream;
                Log.w(TAG, "failed parsing " + file + " " + e3);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4222) {
                        e4222.printStackTrace();
                    }
                }
                i = this.mShellIdentifies.iterator();
                while (i.hasNext()) {
                    if (((ShellItem) i.next()).getFiles().size() == 0) {
                        i.remove();
                    }
                }
            } catch (IOException e8) {
                e4222 = e8;
                fileInputStream = stream;
                try {
                    Log.w(TAG, "failed parsing " + file + " " + e4222);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e42222) {
                            e42222.printStackTrace();
                        }
                    }
                    i = this.mShellIdentifies.iterator();
                    while (i.hasNext()) {
                        if (((ShellItem) i.next()).getFiles().size() == 0) {
                            i.remove();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e422222) {
                            e422222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = stream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (NoClassDefFoundError e9) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
                if (((ShellItem) i.next()).getFiles().size() == 0) {
                    i.remove();
                }
            }
        } catch (FileNotFoundException e10) {
            e2 = e10;
            Log.w(TAG, "file is not exist " + e2);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
                if (((ShellItem) i.next()).getFiles().size() == 0) {
                    i.remove();
                }
            }
        } catch (XmlPullParserException e11) {
            e3 = e11;
            Log.w(TAG, "failed parsing " + file + " " + e3);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
                if (((ShellItem) i.next()).getFiles().size() == 0) {
                    i.remove();
                }
            }
        } catch (IOException e12) {
            e422222 = e12;
            Log.w(TAG, "failed parsing " + file + " " + e422222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
                if (((ShellItem) i.next()).getFiles().size() == 0) {
                    i.remove();
                }
            }
        }
        i = this.mShellIdentifies.iterator();
        while (i.hasNext()) {
            if (((ShellItem) i.next()).getFiles().size() == 0) {
                i.remove();
            }
        }
    }

    private void ParseXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        String tag = AppHibernateCst.INVALID_PKG;
        String fileName = AppHibernateCst.INVALID_PKG;
        ShellItem currentShell = null;
        ArrayList arrayList = null;
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (2 == type) {
                tag = parser.getName();
                switch (parser.getDepth()) {
                    case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                        continue;
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                        if ("shell".equals(tag)) {
                            break;
                        }
                        Log.w(TAG, "invalid file: /system/etc/dexopt/shell_identify_and_oat_clean.xml");
                        return;
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                        if (!INFO_TYPE_ShellIdentify.equals(tag)) {
                            if (!INFO_TYPE_ShellClearOat.equals(tag)) {
                                if (!INFO_TYPE_ShellByPackageName.equals(tag)) {
                                    arrayList = null;
                                    Log.e(TAG, "Parsing Shell Identify Xml, Find an unknown infoType: " + tag);
                                    break;
                                }
                                arrayList = this.mShellByPackageName;
                                break;
                            }
                            arrayList = this.mShellClearOats;
                            break;
                        }
                        arrayList = this.mShellIdentifies;
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        if (arrayList == null) {
                            Log.e(TAG, "Parsing Shell Identify Xml, Find an unknown Tag " + tag + " before a InfoType Tag.");
                            currentShell = null;
                            break;
                        }
                        currentShell = AddShellItem(tag, arrayList);
                        break;
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                        if (currentShell != null) {
                            if (!"File".equals(tag) && !"Packagename".equals(tag)) {
                                Log.e(TAG, "Find an unknown Tag: " + tag);
                                break;
                            }
                            currentShell.getFiles().add(parser.nextText());
                            break;
                        }
                        Log.e(TAG, "Parsing Shell Identify Xml, Find File Tag before a Shell Tag.");
                        break;
                        break;
                    default:
                        Log.e(TAG, "Parse Shell Identify Xml Reach a invalid Depth: " + String.valueOf(0));
                        break;
                }
            }
        }
    }

    private ShellItem AddShellItem(String shellName, ArrayList<ShellItem> shellItems) {
        for (ShellItem shellItem : shellItems) {
            if (shellItem.getName().equals(shellName)) {
                return shellItem;
            }
        }
        ShellItem newShell = new ShellItem(shellName, new ArrayList());
        shellItems.add(newShell);
        return newShell;
    }

    private ArrayList<ShellItem> DeepcopyShellItem(ArrayList<ShellItem> oriShellItems) {
        if (oriShellItems == null) {
            return null;
        }
        ArrayList<ShellItem> shellItems = new ArrayList();
        for (ShellItem oriShellItem : oriShellItems) {
            ShellItem shellItem = new ShellItem(oriShellItem.getName(), new ArrayList());
            for (String str : oriShellItem.getFiles()) {
                shellItem.getFiles().add(str);
            }
            shellItems.add(shellItem);
        }
        return shellItems;
    }
}
