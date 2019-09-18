package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwPersistentAppManager {
    private static final boolean PERSISTENT_CONFIG_DISABLED = SystemProperties.get("ro.hw_persistent.disable", "0").equals("1");
    private static final String PERSISTENT_CONFIG_FILE_PATH = "xml/hw_persistent_config.xml";
    private static final String TAG = "HwPersistentAppManager";
    private static HashMap<String, PersistentInfo> mPersistentConfigMap = null;

    private static final class PersistentInfo {
        public String original;
        public String packageName;
        public String persistent;
        public String updatable;

        private PersistentInfo() {
            this.packageName = "";
            this.original = "";
            this.persistent = "";
            this.updatable = "";
        }

        public String toString() {
            return "packageName:" + this.packageName + " original:" + this.original + " persistent:" + this.persistent + " updatable:" + this.updatable;
        }
    }

    private static ArrayList<File> getPersistentConfigFileList(String filePath) {
        ArrayList<File> fileList = new ArrayList<>();
        if (TextUtils.isEmpty(filePath)) {
            Log.e(TAG, "Error: file = [" + filePath + "]");
            return fileList;
        }
        String[] policyDir = null;
        try {
            policyDir = HwCfgFilePolicy.getCfgPolicyDir(0);
        } catch (NoClassDefFoundError e) {
            Slog.w(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (policyDir == null) {
            return fileList;
        }
        for (String file : policyDir) {
            File file2 = new File(file, filePath);
            if (file2.exists()) {
                fileList.add(file2);
                Slog.d(TAG, "getPersistentConfigFileList from  i=" + i + "| " + file2.getAbsolutePath());
            }
        }
        if (fileList.size() == 0) {
            Log.w(TAG, "No persistent config file found for:" + filePath);
        }
        return fileList;
    }

    private static HashMap<String, PersistentInfo> loadPersistentConfigInfo() {
        ArrayList<File> fileList = null;
        try {
            fileList = getPersistentConfigFileList(PERSISTENT_CONFIG_FILE_PATH);
        } catch (NoClassDefFoundError er) {
            Slog.e(TAG, er.getMessage());
        }
        if (fileList == null || fileList.size() == 0) {
            return null;
        }
        HashMap<String, PersistentInfo> persistentConfigMap = new HashMap<>();
        int leng = fileList.size();
        for (int i = 0; i < leng; i++) {
            File file = fileList.get(i);
            if (file != null && file.exists()) {
                persistentConfigMap.putAll(readPersistentConfigFile(file));
            }
        }
        return persistentConfigMap;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r2.close();
     */
    private static HashMap<String, PersistentInfo> readPersistentConfigFile(File file) {
        String str;
        StringBuilder sb;
        HashMap<String, PersistentInfo> result = new HashMap<>();
        if (file == null || !file.exists()) {
            return result;
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1 || type == 2) {
                } else {
                    Slog.d(TAG, "readPersistentConfigFile");
                }
            }
            if ("persistent-config".equals(parser.getName())) {
                parser.next();
                int outerDepth = parser.getDepth();
                while (true) {
                    int next2 = parser.next();
                    int type2 = next2;
                    if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        try {
                            break;
                        } catch (IOException e) {
                            e = e;
                            str = TAG;
                            sb = new StringBuilder();
                        }
                    } else if (type2 != 3) {
                        if (type2 != 4) {
                            if ("item".equals(parser.getName())) {
                                PersistentInfo info = new PersistentInfo();
                                info.packageName = parser.getAttributeValue(null, "package");
                                info.original = parser.getAttributeValue(null, "original");
                                info.persistent = parser.getAttributeValue(null, "persistent");
                                info.updatable = parser.getAttributeValue(null, "updatable");
                                result.put(info.packageName, info);
                                Slog.d(TAG, info.toString());
                            }
                        }
                    }
                }
                return result;
            }
            throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
        } catch (FileNotFoundException e2) {
            Slog.w(TAG, "file is not exist " + e2.getMessage());
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "failed parsing " + file + " " + e4.getMessage());
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e6) {
            Slog.w(TAG, "failed parsing " + file + " " + e6.getMessage());
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e8) {
                    Slog.e(TAG, "readPersistentConfigFile, failed to close FileInputStream" + e8.getMessage());
                }
            }
            throw th;
        }
        sb.append("readPersistentConfigFile, failed to close FileInputStream");
        sb.append(e.getMessage());
        Slog.e(str, sb.toString());
        return result;
    }

    private static boolean fixPkgPersistentFlag(PackageParser.Package pkg) {
        if (mPersistentConfigMap != null) {
            PersistentInfo persistentInfo = mPersistentConfigMap.get(pkg.packageName);
            PersistentInfo info = persistentInfo;
            if (persistentInfo != null && ("true".equals(info.original) || "true".equals(info.persistent))) {
                return false;
            }
        }
        pkg.applicationInfo.flags &= -9;
        return true;
    }

    public static void readPersistentConfig() {
        if (!PERSISTENT_CONFIG_DISABLED) {
            mPersistentConfigMap = loadPersistentConfigInfo();
        }
    }

    public static void resolvePersistentFlagForPackage(int oldFlags, PackageParser.Package pkg) {
        if (!PERSISTENT_CONFIG_DISABLED && pkg != null && pkg.applicationInfo != null) {
            boolean oldNonPersistent = false;
            boolean newPersistent = (pkg.applicationInfo.flags & 8) != 0;
            if (newPersistent) {
                if ((oldFlags & 8) == 0) {
                    oldNonPersistent = true;
                }
                if (oldNonPersistent && newPersistent && fixPkgPersistentFlag(pkg)) {
                    Slog.i(TAG, pkg.packageName + " does not allow to become a persistent app since old appis not a persistent app!");
                }
            }
        }
    }

    public static boolean isPersistentUpdatable(PackageParser.Package pkg) {
        if (!(PERSISTENT_CONFIG_DISABLED || pkg == null || mPersistentConfigMap == null)) {
            PersistentInfo persistentInfo = mPersistentConfigMap.get(pkg.packageName);
            PersistentInfo info = persistentInfo;
            if (persistentInfo == null || !"true".equals(info.updatable)) {
                return false;
            }
            Slog.i(TAG, pkg.packageName + " is marked as a updatable persistent app!");
            return true;
        }
        return false;
    }
}
