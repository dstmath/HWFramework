package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.PackageParser;
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
    private static final String INFO_TYPE_SHELL_BY_PACKAGE_NAME = "shellByPackageName";
    private static final String INFO_TYPE_SHELL_CLEAR_OAT = "shellClearOat";
    private static final String INFO_TYPE_SHELL_IDENTIFY = "shellIdentify";
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
        this.mShellClearOats = new ArrayList<>();
        this.mShellIdentifies = new ArrayList<>();
        this.mShellByPackageName = new ArrayList<>();
        parseShellConfig();
        File dataDir = Environment.getDataDirectory();
        this.mAppDataDir = new File(dataDir, "data");
        this.mUserAppDataDir = new File(dataDir, "user");
        this.mInstaller = installer;
    }

    @SuppressLint({"PreferForInArrayList"})
    public String analyseShell(PackageParser.Package pkg) {
        ArrayList<ShellItem> tmpShellIdentifies = copyShellItem(this.mShellIdentifies);
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
                Log.e(TAG, "fail to process apk file: " + pkg.baseCodePath);
            }
            file.close();
        } catch (IOException e2) {
            Log.e(TAG, "fail to process apk file: " + pkg.baseCodePath);
        }
        Iterator<ShellItem> it = tmpShellIdentifies.iterator();
        while (it.hasNext()) {
            ShellItem shellIdentify = it.next();
            if (shellIdentify.getFiles().size() == 0) {
                return shellIdentify.getName();
            }
        }
        return analyseShellByPackageName(pkg);
    }

    @SuppressLint({"PreferForInArrayList"})
    private String analyseShellByPackageName(PackageParser.Package pkg) {
        if (this.mShellByPackageName == null || pkg == null || pkg.packageName == null) {
            return null;
        }
        Iterator<ShellItem> it = this.mShellByPackageName.iterator();
        while (it.hasNext()) {
            ShellItem shellIdentify = it.next();
            Iterator<String> it2 = shellIdentify.getFiles().iterator();
            while (true) {
                if (it2.hasNext()) {
                    String identifyPackageName = it2.next();
                    if (identifyPackageName != null && identifyPackageName.equals(pkg.packageName)) {
                        return shellIdentify.getName();
                    }
                }
            }
        }
        return null;
    }

    public void processShellApp(PackageParser.Package pkg) {
    }

    @SuppressLint({"PreferForInArrayList"})
    private void filterOutIdentifyFiles(String fileName, ArrayList<ShellItem> tmpShellIdentifies) {
        Iterator<ShellItem> it = tmpShellIdentifies.iterator();
        while (it.hasNext()) {
            Iterator<String> i = it.next().getFiles().iterator();
            while (i.hasNext()) {
                String identifyFileName = i.next();
                if (!fileName.equals(identifyFileName)) {
                    if (!fileName.endsWith("/" + identifyFileName)) {
                    }
                }
                i.remove();
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
    private String dumpIdentifies() {
        StringBuffer sb = new StringBuffer();
        sb.append(System.lineSeparator());
        sb.append("INFO_TYPE_SHELL_IDENTIFY");
        Iterator<ShellItem> it = this.mShellIdentifies.iterator();
        while (it.hasNext()) {
            ShellItem shellItem = it.next();
            sb.append("    ");
            sb.append(shellItem.getName());
            sb.append(System.lineSeparator());
            Iterator<String> it2 = shellItem.getFiles().iterator();
            while (it2.hasNext()) {
                sb.append("        ");
                sb.append(it2.next());
                sb.append(System.lineSeparator());
            }
        }
        sb.append(System.lineSeparator());
        sb.append("INFO_TYPE_ClearOat");
        Iterator<ShellItem> it3 = this.mShellClearOats.iterator();
        while (it3.hasNext()) {
            ShellItem shellItem2 = it3.next();
            sb.append("    ");
            sb.append(shellItem2.getName());
            sb.append(System.lineSeparator());
            Iterator<String> it4 = shellItem2.getFiles().iterator();
            while (it4.hasNext()) {
                sb.append("        ");
                sb.append(it4.next());
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private void parseShellConfig() {
        File file = new File("/system/etc", "dexopt/shell_identify_and_oat_clean.xml");
        FileInputStream stream = null;
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("dexopt/shell_identify_and_oat_clean.xml", 0);
            if (cfg != null) {
                file = cfg;
            }
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            parseXml(parser);
            try {
                stream2.close();
            } catch (IOException e) {
                Log.d(TAG, "parseShellConfig stream close FAIL!");
            }
        } catch (NoClassDefFoundError e2) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (stream != null) {
                stream.close();
            }
        } catch (FileNotFoundException e3) {
            Log.w(TAG, "file is not exist " + e3);
            if (stream != null) {
                stream.close();
            }
        } catch (XmlPullParserException e4) {
            Log.w(TAG, "failed parsing " + file + " " + e4);
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e5) {
            Log.w(TAG, "failed parsing " + file + " " + e5);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e6) {
                    Log.d(TAG, "parseShellConfig stream close FAIL!");
                }
            }
            throw th;
        }
        Iterator<ShellItem> i = this.mShellIdentifies.iterator();
        while (i.hasNext()) {
            if (i.next().getFiles().size() == 0) {
                i.remove();
            }
        }
    }

    private void parseXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        ShellItem currentShell = null;
        ArrayList<ShellItem> currentArray = null;
        while (true) {
            int next = parser.next();
            int type = next;
            if (next != 1) {
                if (2 == type) {
                    String tag = parser.getName();
                    switch (parser.getDepth()) {
                        case 0:
                            continue;
                        case 1:
                            if ("shell".equals(tag)) {
                                break;
                            } else {
                                Log.w(TAG, "invalid file: /system/etc/dexopt/shell_identify_and_oat_clean.xml");
                                return;
                            }
                        case 2:
                            if (!INFO_TYPE_SHELL_IDENTIFY.equals(tag)) {
                                if (!INFO_TYPE_SHELL_CLEAR_OAT.equals(tag)) {
                                    if (!INFO_TYPE_SHELL_BY_PACKAGE_NAME.equals(tag)) {
                                        currentArray = null;
                                        Log.e(TAG, "Parsing Shell Identify Xml, Find an unknown infoType: " + tag);
                                        break;
                                    } else {
                                        currentArray = this.mShellByPackageName;
                                        break;
                                    }
                                } else {
                                    currentArray = this.mShellClearOats;
                                    break;
                                }
                            } else {
                                currentArray = this.mShellIdentifies;
                                break;
                            }
                        case 3:
                            if (currentArray == null) {
                                Log.e(TAG, "Parsing Shell Identify Xml, Find an unknown Tag " + tag + " before a InfoType Tag.");
                                currentShell = null;
                                break;
                            } else {
                                currentShell = addShellItem(tag, currentArray);
                                break;
                            }
                        case 4:
                            if (currentShell != null) {
                                if (!"File".equals(tag) && !"Packagename".equals(tag)) {
                                    Log.e(TAG, "Find an unknown Tag: " + tag);
                                    break;
                                } else {
                                    currentShell.getFiles().add(parser.nextText());
                                    break;
                                }
                            } else {
                                Log.e(TAG, "Parsing Shell Identify Xml, Find File Tag before a Shell Tag.");
                                break;
                            }
                        default:
                            Log.e(TAG, "Parse Shell Identify Xml Reach a invalid Depth: " + String.valueOf(0));
                            break;
                    }
                }
            } else {
                return;
            }
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    private ShellItem addShellItem(String shellName, ArrayList<ShellItem> shellItems) {
        Iterator<ShellItem> it = shellItems.iterator();
        while (it.hasNext()) {
            ShellItem shellItem = it.next();
            if (shellItem.getName().equals(shellName)) {
                return shellItem;
            }
        }
        ShellItem newShell = new ShellItem(shellName, new ArrayList());
        shellItems.add(newShell);
        return newShell;
    }

    @SuppressLint({"PreferForInArrayList"})
    private ArrayList<ShellItem> copyShellItem(ArrayList<ShellItem> oriShellItems) {
        if (oriShellItems == null) {
            return null;
        }
        ArrayList<ShellItem> shellItems = new ArrayList<>();
        Iterator<ShellItem> it = oriShellItems.iterator();
        while (it.hasNext()) {
            ShellItem oriShellItem = it.next();
            ShellItem shellItem = new ShellItem(oriShellItem.getName(), new ArrayList());
            Iterator<String> it2 = oriShellItem.getFiles().iterator();
            while (it2.hasNext()) {
                shellItem.getFiles().add(it2.next());
            }
            shellItems.add(shellItem);
        }
        return shellItems;
    }
}
