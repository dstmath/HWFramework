package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.PackageParser.Package;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
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
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0057  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressLint({"PreferForInArrayList"})
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
                    }
                    return AnalyseShellByPackageName(pkg);
                }
            }
            file.close();
            ZipFile zipFile = file;
        } catch (IOException e3) {
            Log.e(TAG, "fail to process apk file: " + pkg.baseCodePath);
            for (ShellItem shellIdentify2 : tmpShellIdentifies) {
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

    @SuppressLint({"PreferForInArrayList"})
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
    }

    @SuppressLint({"PreferForInArrayList"})
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

    @SuppressLint({"PreferForInArrayList"})
    public String DumpIdentifies() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nINFO_TYPE_ShellIdentify");
        for (ShellItem shellItem : this.mShellIdentifies) {
            sb.append("    ").append(shellItem.getName()).append("\n");
            for (String fileName : shellItem.getFiles()) {
                sb.append("        ").append(fileName).append("\n");
            }
        }
        sb.append("\nINFO_TYPE_ClearOat");
        for (ShellItem shellItem2 : this.mShellClearOats) {
            sb.append("    ").append(shellItem2.getName()).append("\n");
            for (String fileName2 : shellItem2.getFiles()) {
                sb.append("        ").append(fileName2).append("\n");
            }
        }
        return sb.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0102 A:{SYNTHETIC, Splitter: B:48:0x0102} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00e5 A:{SYNTHETIC, Splitter: B:40:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00b7 A:{SYNTHETIC, Splitter: B:32:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0080 A:{SYNTHETIC, Splitter: B:24:0x0080} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0116 A:{SYNTHETIC, Splitter: B:54:0x0116} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseShellConfig() {
        Iterator<ShellItem> i;
        FileNotFoundException e;
        XmlPullParserException e2;
        IOException e3;
        Throwable th;
        File file = new File("/system/etc", "dexopt/shell_identify_and_oat_clean.xml");
        FileInputStream stream = null;
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("dexopt/shell_identify_and_oat_clean.xml", 0);
            if (cfg != null) {
                file = cfg;
            }
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                ParseXml(parser);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e4) {
                        Log.d(TAG, "parseShellConfig stream close FAIL!");
                    }
                }
                stream = stream2;
            } catch (NoClassDefFoundError e5) {
                stream = stream2;
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                if (stream != null) {
                }
                i = this.mShellIdentifies.iterator();
                while (i.hasNext()) {
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                stream = stream2;
                Log.w(TAG, "file is not exist " + e);
                if (stream != null) {
                }
                i = this.mShellIdentifies.iterator();
                while (i.hasNext()) {
                }
            } catch (XmlPullParserException e7) {
                e2 = e7;
                stream = stream2;
                Log.w(TAG, "failed parsing " + file + " " + e2);
                if (stream != null) {
                }
                i = this.mShellIdentifies.iterator();
                while (i.hasNext()) {
                }
            } catch (IOException e8) {
                e3 = e8;
                stream = stream2;
                try {
                    Log.w(TAG, "failed parsing " + file + " " + e3);
                    if (stream != null) {
                    }
                    i = this.mShellIdentifies.iterator();
                    while (i.hasNext()) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e9) {
                            Log.d(TAG, "parseShellConfig stream close FAIL!");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (NoClassDefFoundError e10) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    Log.d(TAG, "parseShellConfig stream close FAIL!");
                }
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
            }
        } catch (FileNotFoundException e12) {
            e = e12;
            Log.w(TAG, "file is not exist " + e);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e13) {
                    Log.d(TAG, "parseShellConfig stream close FAIL!");
                }
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
            }
        } catch (XmlPullParserException e14) {
            e2 = e14;
            Log.w(TAG, "failed parsing " + file + " " + e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e15) {
                    Log.d(TAG, "parseShellConfig stream close FAIL!");
                }
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
            }
        } catch (IOException e16) {
            e3 = e16;
            Log.w(TAG, "failed parsing " + file + " " + e3);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e17) {
                    Log.d(TAG, "parseShellConfig stream close FAIL!");
                }
            }
            i = this.mShellIdentifies.iterator();
            while (i.hasNext()) {
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
        String tag = "";
        String fileName = "";
        ShellItem currentShell = null;
        ArrayList currentArray = null;
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (2 == type) {
                tag = parser.getName();
                switch (parser.getDepth()) {
                    case 0:
                        continue;
                    case 1:
                        if ("shell".equals(tag)) {
                            break;
                        }
                        Log.w(TAG, "invalid file: /system/etc/dexopt/shell_identify_and_oat_clean.xml");
                        return;
                    case 2:
                        if (!INFO_TYPE_ShellIdentify.equals(tag)) {
                            if (!INFO_TYPE_ShellClearOat.equals(tag)) {
                                if (!INFO_TYPE_ShellByPackageName.equals(tag)) {
                                    currentArray = null;
                                    Log.e(TAG, "Parsing Shell Identify Xml, Find an unknown infoType: " + tag);
                                    break;
                                }
                                currentArray = this.mShellByPackageName;
                                break;
                            }
                            currentArray = this.mShellClearOats;
                            break;
                        }
                        currentArray = this.mShellIdentifies;
                        break;
                    case 3:
                        if (currentArray == null) {
                            Log.e(TAG, "Parsing Shell Identify Xml, Find an unknown Tag " + tag + " before a InfoType Tag.");
                            currentShell = null;
                            break;
                        }
                        currentShell = AddShellItem(tag, currentArray);
                        break;
                    case 4:
                        if (currentShell != null) {
                            if (!"File".equals(tag) && ("Packagename".equals(tag) ^ 1) != 0) {
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

    @SuppressLint({"PreferForInArrayList"})
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

    @SuppressLint({"PreferForInArrayList"})
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
