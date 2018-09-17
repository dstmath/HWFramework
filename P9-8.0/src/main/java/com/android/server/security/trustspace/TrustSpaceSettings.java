package com.android.server.security.trustspace;

import android.os.Environment;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class TrustSpaceSettings {
    private static final String ATTR_PACKAGE = "package";
    private static final String ATTR_PROTECTION = "protection";
    private static final String TAG = "TrustSpaceSettings";
    private static final String TAG_ITEM = "item";
    private static final String TAG_PROTECTED_PACKAGES = "protected-packages";
    private static final String TAG_TRUSTED_PACKAGES = "trusted-packages";
    public static final int TYPE_ACTIVITY = 0;
    public static final int TYPE_BROADCAST = 1;
    public static final int TYPE_PROVIDER = 3;
    public static final int TYPE_SERVICE = 2;
    private final File mBackupProtectedPackageFile;
    final ArrayMap<String, ProtectedPackage> mPackages = new ArrayMap();
    private final File mPreviousProtectedPackageFile;
    private final File mProtectedPackageFile;
    final ArraySet<String> mProtectionHighApps = new ArraySet();
    final ArraySet<String> mProtectionNormalApps = new ArraySet();
    final ArraySet<String> mTrustApps = new ArraySet();

    private class ProtectedPackage {
        String packageName;
        int protectionLevel;

        public ProtectedPackage(String packageName, int protection) {
            this.packageName = packageName;
            this.protectionLevel = protection;
        }
    }

    public static String componentTypeToString(int type) {
        String typeString = "????";
        switch (type) {
            case 0:
                return "activity";
            case 1:
                return "broadcast";
            case 2:
                return AwareAppMngSort.ADJTYPE_SERVICE;
            case 3:
                return "provider";
            default:
                return typeString;
        }
    }

    TrustSpaceSettings() {
        File systemDir = new File(Environment.getDataDirectory(), "system");
        this.mProtectedPackageFile = new File(systemDir, "trustspace.xml");
        this.mBackupProtectedPackageFile = new File(systemDir, "trustspace-backup.xml");
        this.mPreviousProtectedPackageFile = new File(systemDir, "trustspace.list");
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004c A:{SYNTHETIC, Splitter: B:15:0x004c} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0080 A:{SYNTHETIC, EDGE_INSN: B:79:0x0080->B:30:0x0080 ?: BREAK  , EDGE_INSN: B:79:0x0080->B:30:0x0080 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008f A:{SYNTHETIC, Splitter: B:34:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0082 A:{Catch:{ XmlPullParserException -> 0x00b7, IOException -> 0x00d2 }} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004c A:{SYNTHETIC, Splitter: B:15:0x004c} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e A:{LOOP_END, SKIP, Catch:{ XmlPullParserException -> 0x00b7, IOException -> 0x00d2 }, LOOP:0: B:27:0x0078->B:29:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0080 A:{SYNTHETIC, EDGE_INSN: B:79:0x0080->B:30:0x0080 ?: BREAK  , EDGE_INSN: B:79:0x0080->B:30:0x0080 ?: BREAK  , EDGE_INSN: B:79:0x0080->B:30:0x0080 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0082 A:{Catch:{ XmlPullParserException -> 0x00b7, IOException -> 0x00d2 }} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008f A:{SYNTHETIC, Splitter: B:34:0x008f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readPackages() {
        FileInputStream str;
        XmlPullParser parser;
        int type;
        Throwable th;
        if (this.mProtectedPackageFile.exists()) {
            FileInputStream str2 = null;
            if (this.mBackupProtectedPackageFile.exists()) {
                try {
                    str = new FileInputStream(this.mBackupProtectedPackageFile);
                    try {
                        Slog.i(TAG, "Need to read from backup settings file");
                        if (this.mProtectedPackageFile.exists()) {
                            Slog.w(TAG, "Cleaning up settings file");
                            if (!this.mProtectedPackageFile.delete()) {
                                Slog.w(TAG, "Failed to clean up settings file");
                            }
                        }
                    } catch (IOException e) {
                        str2 = str;
                        str = str2;
                        if (str == null) {
                        }
                        parser = Xml.newPullParser();
                        parser.setInput(str2, StandardCharsets.UTF_8.name());
                        do {
                            type = parser.next();
                            if (type != 2) {
                                break;
                            }
                        } while (type != 1);
                        if (type != 2) {
                        }
                    }
                } catch (IOException e2) {
                    str = str2;
                    if (str == null) {
                    }
                    parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type != 2) {
                        }
                    } while (type != 1);
                    if (type != 2) {
                    }
                }
            } else {
                str = null;
            }
            if (str == null) {
                try {
                    if (this.mProtectedPackageFile.exists()) {
                        str2 = new FileInputStream(this.mProtectedPackageFile);
                    } else {
                        Slog.i(TAG, "No settings file found");
                        IoUtils.closeQuietly(str);
                        return;
                    }
                } catch (XmlPullParserException e3) {
                    str2 = str;
                    try {
                        Slog.e(TAG, "read settings error duing to XmlPullParserException");
                        IoUtils.closeQuietly(str2);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(str2);
                        throw th;
                    }
                } catch (IOException e4) {
                    str2 = str;
                    Slog.e(TAG, "read settings error duing to IOException");
                    IoUtils.closeQuietly(str2);
                } catch (Throwable th3) {
                    th = th3;
                    str2 = str;
                    IoUtils.closeQuietly(str2);
                    throw th;
                }
            }
            str2 = str;
            try {
                parser = Xml.newPullParser();
                parser.setInput(str2, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type != 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    Slog.i(TAG, "No start tag found in settings file");
                    IoUtils.closeQuietly(str2);
                    return;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        IoUtils.closeQuietly(str2);
                    } else if (!(type == 3 || type == 4)) {
                        String tagName = parser.getName();
                        if (tagName.equals(TAG_PROTECTED_PACKAGES)) {
                            readProtectedPackages(parser);
                        } else if (tagName.equals(TAG_TRUSTED_PACKAGES)) {
                            readTrustedPackages(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <packages>: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
                IoUtils.closeQuietly(str2);
            } catch (XmlPullParserException e5) {
            } catch (IOException e6) {
            }
        } else {
            readPreviousFile();
        }
    }

    private void readPreviousFile() {
        if (readPreviousApps()) {
            if (!this.mPreviousProtectedPackageFile.delete()) {
                Slog.w(TAG, "Failed to clean up previous settings file");
            }
            writePackages();
            Slog.i(TAG, "Update from previous settings");
        }
    }

    private String readLine(InputStream in, StringBuffer sb) throws IOException {
        sb.setLength(0);
        while (true) {
            int ch = in.read();
            if (ch == -1) {
                if (sb.length() == 0) {
                    return null;
                }
                throw new IOException("Unexpected EOF");
            } else if (ch == 10) {
                return sb.toString();
            } else {
                sb.append((char) ch);
            }
        }
    }

    private boolean readPreviousApps() {
        Throwable th;
        Object in;
        AutoCloseable in2 = null;
        try {
            BufferedInputStream in3 = new BufferedInputStream(new AtomicFile(this.mPreviousProtectedPackageFile).openRead());
            try {
                StringBuffer sb = new StringBuffer();
                while (true) {
                    String line = readLine(in3, sb);
                    if (line != null) {
                        this.mPackages.put(line, new ProtectedPackage(line, 1));
                        this.mProtectionNormalApps.add(line);
                    } else {
                        IoUtils.closeQuietly(in3);
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                in2 = in3;
                try {
                    Slog.d(TAG, "Previous settings file not find");
                    IoUtils.closeQuietly(in2);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(in2);
                    throw th;
                }
            } catch (IOException e2) {
                in2 = in3;
                Slog.w(TAG, "read previous settings error duing to IOException");
                IoUtils.closeQuietly(in2);
                return false;
            } catch (Throwable th3) {
                th = th3;
                in2 = in3;
                IoUtils.closeQuietly(in2);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            Slog.d(TAG, "Previous settings file not find");
            IoUtils.closeQuietly(in2);
            return false;
        } catch (IOException e4) {
            Slog.w(TAG, "read previous settings error duing to IOException");
            IoUtils.closeQuietly(in2);
            return false;
        }
    }

    private void readProtectedPackages(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String packName = parser.getAttributeValue(null, "package");
                int protection = readInt(parser, null, ATTR_PROTECTION, -1);
                if (!TextUtils.isEmpty(packName)) {
                    this.mPackages.put(packName, new ProtectedPackage(packName, protection));
                    int level = protection & 255;
                    if (level == 1) {
                        this.mProtectionNormalApps.add(packName);
                    } else if (level == 2) {
                        this.mProtectionHighApps.add(packName);
                    }
                }
            }
        }
    }

    private void readTrustedPackages(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String packName = parser.getAttributeValue(null, "package");
                if (!TextUtils.isEmpty(packName)) {
                    this.mTrustApps.add(packName);
                }
            }
        }
    }

    private int readInt(XmlPullParser parser, String ns, String name, int defValue) {
        String v = parser.getAttributeValue(ns, name);
        if (v == null) {
            return defValue;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    void writePackages() {
        IOException e;
        Object fstr;
        Throwable th;
        if (this.mProtectedPackageFile.exists()) {
            if (this.mBackupProtectedPackageFile.exists()) {
                if (this.mProtectedPackageFile.delete()) {
                    Slog.i(TAG, "Failed to clean up settings file");
                }
                Slog.w(TAG, "Preserving older settings backup file");
            } else if (!this.mProtectedPackageFile.renameTo(this.mBackupProtectedPackageFile)) {
                Slog.e(TAG, "Unable to backup settings,  current changes will be lost at reboot");
                return;
            }
        }
        AutoCloseable fstr2 = null;
        BufferedOutputStream str = null;
        try {
            BufferedOutputStream str2;
            FileOutputStream fstr3 = new FileOutputStream(this.mProtectedPackageFile);
            try {
                str2 = new BufferedOutputStream(fstr3);
            } catch (IOException e2) {
                e = e2;
                fstr2 = fstr3;
                try {
                    Slog.e(TAG, "Unable to write settings, current changes will be lost at reboot", e);
                    IoUtils.closeQuietly(fstr2);
                    IoUtils.closeQuietly(str);
                    Slog.w(TAG, "Failed to clean up settings file");
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(fstr2);
                    IoUtils.closeQuietly(str);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fstr2 = fstr3;
                IoUtils.closeQuietly(fstr2);
                IoUtils.closeQuietly(str);
                throw th;
            }
            try {
                XmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(str2, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
                serializer.startTag(null, TAG_PROTECTED_PACKAGES);
                for (ProtectedPackage app : this.mPackages.values()) {
                    serializer.startTag(null, TAG_ITEM);
                    XmlUtils.writeStringAttribute(serializer, "package", app.packageName);
                    XmlUtils.writeIntAttribute(serializer, ATTR_PROTECTION, app.protectionLevel);
                    serializer.endTag(null, TAG_ITEM);
                }
                serializer.endTag(null, TAG_PROTECTED_PACKAGES);
                serializer.startTag(null, TAG_TRUSTED_PACKAGES);
                for (String packageName : this.mTrustApps) {
                    serializer.startTag(null, TAG_ITEM);
                    XmlUtils.writeStringAttribute(serializer, "package", packageName);
                    serializer.endTag(null, TAG_ITEM);
                }
                serializer.endTag(null, TAG_TRUSTED_PACKAGES);
                serializer.endTag(null, HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
                serializer.endDocument();
                str2.flush();
                FileUtils.sync(fstr3);
                if (this.mBackupProtectedPackageFile.exists() && !this.mBackupProtectedPackageFile.delete()) {
                    Slog.i(TAG, "Failed to clean up backup file");
                }
                IoUtils.closeQuietly(fstr3);
                IoUtils.closeQuietly(str2);
            } catch (IOException e3) {
                e = e3;
                str = str2;
                fstr2 = fstr3;
                Slog.e(TAG, "Unable to write settings, current changes will be lost at reboot", e);
                IoUtils.closeQuietly(fstr2);
                IoUtils.closeQuietly(str);
                Slog.w(TAG, "Failed to clean up settings file");
            } catch (Throwable th4) {
                th = th4;
                str = str2;
                fstr2 = fstr3;
                IoUtils.closeQuietly(fstr2);
                IoUtils.closeQuietly(str);
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            Slog.e(TAG, "Unable to write settings, current changes will be lost at reboot", e);
            IoUtils.closeQuietly(fstr2);
            IoUtils.closeQuietly(str);
            if (this.mProtectedPackageFile.exists() && !this.mProtectedPackageFile.delete()) {
                Slog.w(TAG, "Failed to clean up settings file");
            }
        }
    }

    private void clearIntentProtectedApp(String packageName) {
        this.mPackages.remove(packageName);
        this.mProtectionNormalApps.remove(packageName);
        this.mProtectionHighApps.remove(packageName);
    }

    void addIntentProtectedApp(String packageName, int flags) {
        if (packageName != null) {
            switch (flags & 255) {
                case 1:
                    clearIntentProtectedApp(packageName);
                    this.mProtectionNormalApps.add(packageName);
                    this.mPackages.put(packageName, new ProtectedPackage(packageName, flags));
                    break;
                case 2:
                    clearIntentProtectedApp(packageName);
                    this.mProtectionHighApps.add(packageName);
                    this.mPackages.put(packageName, new ProtectedPackage(packageName, flags));
                    break;
            }
        }
    }

    void removeIntentProtectedApp(String packageName) {
        clearIntentProtectedApp(packageName);
    }

    List<String> getIntentProtectedApps(int flags) {
        if ((flags & 1) != 0) {
            return new ArrayList(this.mPackages.keySet());
        }
        ArraySet<String> apps = new ArraySet();
        if ((flags & 4) != 0) {
            apps.addAll(this.mProtectionHighApps);
        }
        if ((flags & 2) != 0) {
            apps.addAll(this.mProtectionNormalApps);
        }
        return new ArrayList(apps);
    }

    boolean isIntentProtectedApp(String packageName) {
        return this.mPackages.containsKey(packageName);
    }

    void removeIntentProtectedApps(List<String> packages, int flags) {
        if ((flags & 1) != 0) {
            if (packages == null) {
                this.mPackages.clear();
                this.mProtectionNormalApps.clear();
                this.mProtectionHighApps.clear();
            } else {
                this.mPackages.removeAll(packages);
                this.mProtectionNormalApps.removeAll(packages);
                this.mProtectionHighApps.removeAll(packages);
            }
            return;
        }
        if ((flags & 4) != 0) {
            if (packages == null) {
                this.mPackages.removeAll(this.mProtectionHighApps);
                this.mProtectionHighApps.clear();
            } else {
                this.mPackages.removeAll(packages);
                this.mProtectionHighApps.removeAll(packages);
            }
        }
        if ((flags & 2) != 0) {
            if (packages == null) {
                this.mPackages.removeAll(this.mProtectionNormalApps);
                this.mProtectionNormalApps.clear();
            } else {
                this.mPackages.removeAll(packages);
                this.mProtectionNormalApps.removeAll(packages);
            }
        }
    }

    void removeTrustApp(String packageName) {
        this.mTrustApps.remove(packageName);
    }

    void updateTrustApps(List<String> packages, int flag) {
        switch (flag) {
            case 1:
                if (packages != null) {
                    this.mTrustApps.addAll(packages);
                    return;
                }
                return;
            case 2:
                this.mTrustApps.clear();
                if (packages != null) {
                    this.mTrustApps.addAll(packages);
                    return;
                }
                return;
            case 3:
                if (packages != null) {
                    this.mTrustApps.removeAll(packages);
                    return;
                }
                return;
            case 4:
                this.mTrustApps.clear();
                return;
            default:
                return;
        }
    }

    boolean isTrustApp(String packageName) {
        return this.mTrustApps.contains(packageName);
    }

    int getProtectionLevel(String packageName) {
        ProtectedPackage ts = (ProtectedPackage) this.mPackages.get(packageName);
        if (ts != null) {
            return ts.protectionLevel;
        }
        return 0;
    }
}
