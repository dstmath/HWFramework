package com.huawei.server.security.trustspace;

import android.os.Environment;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Xml;
import com.huawei.internal.util.FastXmlSerializerEx;
import com.huawei.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.util.LogEx;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class TrustSpaceSettings {
    private static final String ATTR_PACKAGE = "package";
    private static final String ATTR_PROTECTION = "protection";
    private static final int DEFAULT_COLLECTION_SIZE = 10;
    private static final int DEFAULT_PROTECTION_LEVEL = -1;
    private static final int FLAG_APPEND_HARMFUL_APP = 7;
    private static final int FLAG_REMOVE_ALL_HARMFUL_APP = 8;
    private static final int FLAG_REMOVE_HARMFUL_APP = 6;
    private static final int FLAG_REPLACE_ALL_HARMFUL_APP = 5;
    private static final int INVALID_DATA = -1;
    private static final boolean IS_HW_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final String TAG = "TrustSpaceSettings";
    private static final String TAG_HARMFUL_PACKAGES = "harmful-packages";
    private static final String TAG_ITEM = "item";
    private static final String TAG_PROTECTED_PACKAGES = "protected-packages";
    private static final String TAG_TRUSTED_PACKAGES = "trusted-packages";
    public static final int TYPE_ACTIVITY = 0;
    public static final int TYPE_BROADCAST = 1;
    public static final int TYPE_PROVIDER = 3;
    public static final int TYPE_SERVICE = 2;
    private final File mBackupProtectedPackageFile;
    private final ArraySet<String> mHarmfulApps = new ArraySet<>(10);
    private final ArrayMap<String, ProtectedPackage> mPackages = new ArrayMap<>(10);
    private final File mProtectedPackageFile;
    private final ArraySet<String> mProtectionHighApps = new ArraySet<>(10);
    private final ArraySet<String> mProtectionNormalApps = new ArraySet<>(10);
    private final ArraySet<String> mTrustApps = new ArraySet<>(10);

    TrustSpaceSettings() {
        File systemDir = new File(Environment.getDataDirectory(), "system");
        this.mProtectedPackageFile = new File(systemDir, "trustspace.xml");
        this.mBackupProtectedPackageFile = new File(systemDir, "trustspace-backup.xml");
    }

    public static String componentTypeToString(int type) {
        if (type == 0) {
            return "activity";
        }
        if (type == 1) {
            return "broadcast";
        }
        if (type == 2) {
            return "service";
        }
        if (type != 3) {
            return "????";
        }
        return "provider";
    }

    /* access modifiers changed from: private */
    public class ProtectedPackage {
        private String mPackageName;
        private int mProtectionLevel;

        ProtectedPackage(String packageName, int protection) {
            this.mPackageName = packageName;
            this.mProtectionLevel = protection;
        }

        private void setPackageName(String packageName) {
            this.mPackageName = packageName;
        }

        private String getPackageName() {
            return this.mPackageName;
        }

        private void setProtectionLevel(int protectionLevel) {
            this.mProtectionLevel = protectionLevel;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getProtectionLevel() {
            return this.mProtectionLevel;
        }
    }

    /* access modifiers changed from: package-private */
    public void readPackages() {
        FileInputStream fileInputStream = getBackupFileInputStream();
        if (fileInputStream == null) {
            try {
                if (!this.mProtectedPackageFile.exists()) {
                    Log.w(TAG, "No settings file found");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                            return;
                        } catch (IOException e) {
                            Log.e(TAG, "close input stream error during to IOException");
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    fileInputStream = new FileInputStream(this.mProtectedPackageFile);
                }
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "read settings error due to XmlPullParserException");
                if (fileInputStream != null) {
                    fileInputStream.close();
                    return;
                }
                return;
            } catch (IOException e3) {
                Log.e(TAG, "read settings error due to IOException");
                if (fileInputStream != null) {
                    fileInputStream.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "close input stream error during to IOException");
                    }
                }
                throw th;
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
        int type = parser.next();
        while (type != 2 && type != 1) {
            type = parser.next();
        }
        if (type != 2) {
            Log.w(TAG, "No start tag found in settings file");
            try {
                fileInputStream.close();
            } catch (IOException e5) {
                Log.e(TAG, "close input stream error during to IOException");
            }
        } else {
            int outerDepth = parser.getDepth();
            int type2 = parser.next();
            while (type2 != 1 && (type2 != 3 || parser.getDepth() > outerDepth)) {
                if (type2 == 3 || type2 == 4) {
                    type2 = parser.next();
                } else {
                    handleTagName(parser.getName(), parser);
                    type2 = parser.next();
                }
            }
            try {
                fileInputStream.close();
            } catch (IOException e6) {
                Log.e(TAG, "close input stream error during to IOException");
            }
        }
    }

    private void handleTagName(String tagName, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (tagName != null && parser != null) {
            char c = 65535;
            int hashCode = tagName.hashCode();
            if (hashCode != -1367232468) {
                if (hashCode != 990190129) {
                    if (hashCode == 2100914435 && tagName.equals(TAG_TRUSTED_PACKAGES)) {
                        c = 1;
                    }
                } else if (tagName.equals(TAG_HARMFUL_PACKAGES)) {
                    c = 2;
                }
            } else if (tagName.equals(TAG_PROTECTED_PACKAGES)) {
                c = 0;
            }
            if (c == 0) {
                readProtectedPackages(parser);
            } else if (c == 1) {
                readTrustedPackages(parser);
            } else if (c != 2) {
                Log.w(TAG, "Unknown element under <packages>: " + tagName);
                int depth = parser.getDepth();
                int temp = parser.next();
                while (temp != 1) {
                    if (temp != 3 || parser.getDepth() > depth) {
                        temp = parser.next();
                    } else {
                        return;
                    }
                }
            } else {
                readHarmfulPackages(parser);
            }
        }
    }

    private FileInputStream getBackupFileInputStream() {
        FileInputStream fileInputStream = null;
        if (this.mBackupProtectedPackageFile.exists()) {
            try {
                fileInputStream = new FileInputStream(this.mBackupProtectedPackageFile);
                if (IS_HW_DEBUG) {
                    Log.i(TAG, "Need to read from backup settings file");
                }
                if (this.mProtectedPackageFile.exists()) {
                    Log.w(TAG, "Cleaning up settings file");
                    if (!this.mProtectedPackageFile.delete()) {
                        Log.w(TAG, "Failed to clean up settings file");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getBackupFileInputStream IOException!");
            }
        }
        return fileInputStream;
    }

    private void readProtectedPackages(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (type != 1) {
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                if (parser.getName().equals(TAG_ITEM)) {
                    String packageName = parser.getAttributeValue(null, ATTR_PACKAGE);
                    int protection = readInt(parser, null, ATTR_PROTECTION, -1);
                    if (!TextUtils.isEmpty(packageName)) {
                        this.mPackages.put(packageName, new ProtectedPackage(packageName, protection));
                        int level = protection & 255;
                        if (level == 1) {
                            this.mProtectionNormalApps.add(packageName);
                        } else if (level == 2) {
                            this.mProtectionHighApps.add(packageName);
                        } else {
                            Log.w(TAG, "readProtectedPackages unexpected level!");
                        }
                    }
                }
                type = parser.next();
            }
        }
    }

    private void readTrustedPackages(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (type != 1) {
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                if (parser.getName().equals(TAG_ITEM)) {
                    String packageName = parser.getAttributeValue(null, ATTR_PACKAGE);
                    if (!TextUtils.isEmpty(packageName)) {
                        this.mTrustApps.add(packageName);
                    }
                }
                type = parser.next();
            }
        }
    }

    private void readHarmfulPackages(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (type != 1) {
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                if (parser.getName().equals(TAG_ITEM)) {
                    String packageName = parser.getAttributeValue(null, ATTR_PACKAGE);
                    if (!TextUtils.isEmpty(packageName)) {
                        this.mHarmfulApps.add(packageName);
                    }
                }
                type = parser.next();
            }
        }
    }

    private int readInt(XmlPullParser parser, String namespace, String name, int defValue) {
        String value = parser.getAttributeValue(namespace, name);
        if (value == null) {
            return defValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "readInt number format is incorrect!");
            return defValue;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0053, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0056, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0059, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005f, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0062, code lost:
        throw r3;
     */
    public void writePackages() {
        if (!this.mProtectedPackageFile.exists() || backupSettings()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(this.mProtectedPackageFile);
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                writeProtectedPackageFile(bos);
                bos.flush();
                fileOutputStream.getFD().sync();
                if (this.mBackupProtectedPackageFile.exists() && !this.mBackupProtectedPackageFile.delete() && IS_HW_DEBUG) {
                    Log.i(TAG, "Failed to clean up backup file");
                }
                bos.close();
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to write settings, current changes will be lost at reboot");
                if (this.mProtectedPackageFile.exists() && !this.mProtectedPackageFile.delete()) {
                    Log.w(TAG, "Failed to clean up settings file");
                }
            }
        }
    }

    private boolean backupSettings() {
        if (this.mBackupProtectedPackageFile.exists()) {
            if (this.mProtectedPackageFile.delete() && IS_HW_DEBUG) {
                Log.i(TAG, "Failed to clean up settings file");
            }
            Log.w(TAG, "Preserving older settings backup file");
            return true;
        } else if (this.mProtectedPackageFile.renameTo(this.mBackupProtectedPackageFile)) {
            return true;
        } else {
            Log.e(TAG, "Unable to backup settings, current changes will be lost at reboot");
            return false;
        }
    }

    private void writeProtectedPackageFile(BufferedOutputStream bos) throws IOException {
        XmlSerializer serializer = FastXmlSerializerEx.getFastXmlSerializer();
        serializer.setOutput(bos, StandardCharsets.UTF_8.name());
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
        serializer.startTag(null, TAG_PROTECTED_PACKAGES);
        for (ProtectedPackage app : this.mPackages.values()) {
            serializer.startTag(null, TAG_ITEM);
            serializer.attribute(null, ATTR_PACKAGE, app.mPackageName);
            serializer.attribute(null, ATTR_PROTECTION, Integer.toString(convertLevel(app.mProtectionLevel)));
            serializer.endTag(null, TAG_ITEM);
        }
        serializer.endTag(null, TAG_PROTECTED_PACKAGES);
        serializer.startTag(null, TAG_TRUSTED_PACKAGES);
        Iterator<String> it = this.mTrustApps.iterator();
        while (it.hasNext()) {
            serializer.startTag(null, TAG_ITEM);
            serializer.attribute(null, ATTR_PACKAGE, it.next());
            serializer.endTag(null, TAG_ITEM);
        }
        serializer.endTag(null, TAG_TRUSTED_PACKAGES);
        serializer.startTag(null, TAG_HARMFUL_PACKAGES);
        Iterator<String> it2 = this.mHarmfulApps.iterator();
        while (it2.hasNext()) {
            serializer.startTag(null, TAG_ITEM);
            serializer.attribute(null, ATTR_PACKAGE, it2.next());
            serializer.endTag(null, TAG_ITEM);
        }
        serializer.endTag(null, TAG_HARMFUL_PACKAGES);
        serializer.endTag(null, HwSecDiagnoseConstant.MALAPP_APK_PACKAGES);
        serializer.endDocument();
    }

    private void clearIntentProtectedApp(String packageName) {
        this.mPackages.remove(packageName);
        this.mProtectionNormalApps.remove(packageName);
        this.mProtectionHighApps.remove(packageName);
    }

    /* access modifiers changed from: package-private */
    public void addIntentProtectedApp(String packageName, int flag) {
        if (packageName != null) {
            int level = flag & 255;
            if (level == 1) {
                clearIntentProtectedApp(packageName);
                this.mProtectionNormalApps.add(packageName);
                this.mPackages.put(packageName, new ProtectedPackage(packageName, flag));
            } else if (level == 2) {
                clearIntentProtectedApp(packageName);
                this.mProtectionHighApps.add(packageName);
                this.mPackages.put(packageName, new ProtectedPackage(packageName, flag));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeIntentProtectedApp(String packageName) {
        clearIntentProtectedApp(packageName);
    }

    /* access modifiers changed from: package-private */
    public List<String> getIntentProtectedApps(int flag) {
        if ((flag & 1) != 0) {
            return new ArrayList(this.mPackages.keySet());
        }
        ArraySet<String> apps = new ArraySet<>();
        if ((flag & 4) != 0) {
            apps.addAll(this.mProtectionHighApps);
        }
        if ((flag & 2) != 0) {
            apps.addAll(this.mProtectionNormalApps);
        }
        return new ArrayList(apps);
    }

    /* access modifiers changed from: package-private */
    public boolean isIntentProtectedApp(String packageName) {
        return this.mPackages.containsKey(packageName);
    }

    /* access modifiers changed from: package-private */
    public void removeIntentProtectedApps(List<String> packages, int flag) {
        if ((flag & 1) == 0) {
            if ((flag & 4) != 0) {
                if (packages == null) {
                    this.mPackages.removeAll(this.mProtectionHighApps);
                    this.mProtectionHighApps.clear();
                } else {
                    this.mPackages.removeAll(packages);
                    this.mProtectionHighApps.removeAll(packages);
                }
            }
            if ((flag & 2) == 0) {
                return;
            }
            if (packages == null) {
                this.mPackages.removeAll(this.mProtectionNormalApps);
                this.mProtectionNormalApps.clear();
                return;
            }
            this.mPackages.removeAll(packages);
            this.mProtectionNormalApps.removeAll(packages);
        } else if (packages == null) {
            this.mPackages.clear();
            this.mProtectionNormalApps.clear();
            this.mProtectionHighApps.clear();
        } else {
            this.mPackages.removeAll(packages);
            this.mProtectionNormalApps.removeAll(packages);
            this.mProtectionHighApps.removeAll(packages);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeTrustApp(String packageName) {
        this.mTrustApps.remove(packageName);
    }

    /* access modifiers changed from: package-private */
    public void updateTrustApps(List<String> packages, int flag) {
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
            case 5:
                this.mHarmfulApps.clear();
                if (packages != null) {
                    this.mHarmfulApps.addAll(packages);
                    return;
                }
                return;
            case FLAG_REMOVE_HARMFUL_APP /* 6 */:
                if (packages != null) {
                    this.mHarmfulApps.removeAll(packages);
                    return;
                }
                return;
            case FLAG_APPEND_HARMFUL_APP /* 7 */:
                if (packages != null) {
                    this.mHarmfulApps.addAll(packages);
                    return;
                }
                return;
            case 8:
                this.mHarmfulApps.clear();
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isTrustApp(String packageName) {
        return this.mTrustApps.contains(packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean isHarmfulApp(String packageName) {
        return this.mHarmfulApps.contains(packageName);
    }

    /* access modifiers changed from: package-private */
    public int getProtectionLevel(String packageName) {
        ProtectedPackage protectedPackage = this.mPackages.get(packageName);
        if (protectedPackage != null) {
            return convertLevel(protectedPackage.getProtectionLevel());
        }
        return 0;
    }

    private int convertLevel(int level) {
        if (level == 2) {
            return 1;
        }
        return level;
    }
}
