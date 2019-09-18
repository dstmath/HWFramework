package com.android.server.pm;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.SigningInfo;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.backup.BackupUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import libcore.util.HexEncoding;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class ShortcutPackageInfo {
    private static final String ATTR_BACKUP_ALLOWED = "allow-backup";
    private static final String ATTR_BACKUP_ALLOWED_INITIALIZED = "allow-backup-initialized";
    private static final String ATTR_BACKUP_SOURCE_BACKUP_ALLOWED = "bk_src_backup-allowed";
    private static final String ATTR_BACKUP_SOURCE_VERSION = "bk_src_version";
    private static final String ATTR_LAST_UPDATE_TIME = "last_udpate_time";
    private static final String ATTR_SHADOW = "shadow";
    private static final String ATTR_SIGNATURE_HASH = "hash";
    private static final String ATTR_VERSION = "version";
    private static final String TAG = "ShortcutService";
    static final String TAG_ROOT = "package-info";
    private static final String TAG_SIGNATURE = "signature";
    private boolean mBackupAllowed;
    private boolean mBackupAllowedInitialized;
    private boolean mBackupSourceBackupAllowed;
    private long mBackupSourceVersionCode = -1;
    private boolean mIsShadow;
    private long mLastUpdateTime;
    private ArrayList<byte[]> mSigHashes;
    private long mVersionCode = -1;

    private ShortcutPackageInfo(long versionCode, long lastUpdateTime, ArrayList<byte[]> sigHashes, boolean isShadow) {
        this.mVersionCode = versionCode;
        this.mLastUpdateTime = lastUpdateTime;
        this.mIsShadow = isShadow;
        this.mSigHashes = sigHashes;
        this.mBackupAllowed = false;
        this.mBackupSourceBackupAllowed = false;
    }

    public static ShortcutPackageInfo newEmpty() {
        ShortcutPackageInfo shortcutPackageInfo = new ShortcutPackageInfo(-1, 0, new ArrayList(0), false);
        return shortcutPackageInfo;
    }

    public boolean isShadow() {
        return this.mIsShadow;
    }

    public void setShadow(boolean shadow) {
        this.mIsShadow = shadow;
    }

    public long getVersionCode() {
        return this.mVersionCode;
    }

    public long getBackupSourceVersionCode() {
        return this.mBackupSourceVersionCode;
    }

    @VisibleForTesting
    public boolean isBackupSourceBackupAllowed() {
        return this.mBackupSourceBackupAllowed;
    }

    public long getLastUpdateTime() {
        return this.mLastUpdateTime;
    }

    public boolean isBackupAllowed() {
        return this.mBackupAllowed;
    }

    public void updateFromPackageInfo(PackageInfo pi) {
        if (pi != null) {
            this.mVersionCode = pi.getLongVersionCode();
            this.mLastUpdateTime = pi.lastUpdateTime;
            this.mBackupAllowed = ShortcutService.shouldBackupApp(pi);
            this.mBackupAllowedInitialized = true;
        }
    }

    public boolean hasSignatures() {
        return this.mSigHashes.size() > 0;
    }

    public int canRestoreTo(ShortcutService s, PackageInfo currentPackage, boolean anyVersionOkay) {
        if (!BackupUtils.signaturesMatch(this.mSigHashes, currentPackage, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class))) {
            Slog.w(TAG, "Can't restore: Package signature mismatch");
            return 102;
        } else if (!ShortcutService.shouldBackupApp(currentPackage) || !this.mBackupSourceBackupAllowed) {
            Slog.w(TAG, "Can't restore: package didn't or doesn't allow backup");
            return 101;
        } else if (anyVersionOkay || currentPackage.getLongVersionCode() >= this.mBackupSourceVersionCode) {
            return 0;
        } else {
            Slog.w(TAG, String.format("Can't restore: package current version %d < backed up version %d", new Object[]{Long.valueOf(currentPackage.getLongVersionCode()), Long.valueOf(this.mBackupSourceVersionCode)}));
            return 100;
        }
    }

    @VisibleForTesting
    public static ShortcutPackageInfo generateForInstalledPackageForTest(ShortcutService s, String packageName, int packageUserId) {
        PackageInfo pi = s.getPackageInfoWithSignatures(packageName, packageUserId);
        SigningInfo signingInfo = pi.signingInfo;
        if (signingInfo == null) {
            Slog.e(TAG, "Can't get signatures: package=" + packageName);
            return null;
        }
        ShortcutPackageInfo ret = new ShortcutPackageInfo(pi.getLongVersionCode(), pi.lastUpdateTime, BackupUtils.hashSignatureArray(signingInfo.getApkContentsSigners()), false);
        ret.mBackupSourceBackupAllowed = ShortcutService.shouldBackupApp(pi);
        ret.mBackupSourceVersionCode = pi.getLongVersionCode();
        return ret;
    }

    public void refreshSignature(ShortcutService s, ShortcutPackageItem pkg) {
        if (this.mIsShadow) {
            s.wtf("Attempted to refresh package info for shadow package " + pkg.getPackageName() + ", user=" + pkg.getOwnerUserId());
            return;
        }
        PackageInfo pi = s.getPackageInfoWithSignatures(pkg.getPackageName(), pkg.getPackageUserId());
        if (pi == null) {
            Slog.w(TAG, "Package not found: " + pkg.getPackageName());
            return;
        }
        SigningInfo signingInfo = pi.signingInfo;
        if (signingInfo == null) {
            Slog.w(TAG, "Not refreshing signature for " + pkg.getPackageName() + " since it appears to have no signing info.");
            return;
        }
        this.mSigHashes = BackupUtils.hashSignatureArray(signingInfo.getApkContentsSigners());
    }

    public void saveToXml(ShortcutService s, XmlSerializer out, boolean forBackup) throws IOException {
        if (forBackup && !this.mBackupAllowedInitialized) {
            s.wtf("Backup happened before mBackupAllowed is initialized.");
        }
        out.startTag(null, TAG_ROOT);
        ShortcutService.writeAttr(out, ATTR_VERSION, this.mVersionCode);
        ShortcutService.writeAttr(out, ATTR_LAST_UPDATE_TIME, this.mLastUpdateTime);
        ShortcutService.writeAttr(out, ATTR_SHADOW, this.mIsShadow);
        ShortcutService.writeAttr(out, ATTR_BACKUP_ALLOWED, this.mBackupAllowed);
        ShortcutService.writeAttr(out, ATTR_BACKUP_ALLOWED_INITIALIZED, this.mBackupAllowedInitialized);
        ShortcutService.writeAttr(out, ATTR_BACKUP_SOURCE_VERSION, this.mBackupSourceVersionCode);
        ShortcutService.writeAttr(out, ATTR_BACKUP_SOURCE_BACKUP_ALLOWED, this.mBackupSourceBackupAllowed);
        for (int i = 0; i < this.mSigHashes.size(); i++) {
            out.startTag(null, TAG_SIGNATURE);
            ShortcutService.writeAttr(out, ATTR_SIGNATURE_HASH, (CharSequence) Base64.getEncoder().encodeToString(this.mSigHashes.get(i)));
            out.endTag(null, TAG_SIGNATURE);
        }
        out.endTag(null, TAG_ROOT);
    }

    public void loadFromXml(XmlPullParser parser, boolean fromBackup) throws IOException, XmlPullParserException {
        boolean shadow;
        int outerDepth;
        int outerDepth2;
        boolean shadow2;
        XmlPullParser xmlPullParser = parser;
        long versionCode = ShortcutService.parseLongAttribute(xmlPullParser, ATTR_VERSION, -1);
        long lastUpdateTime = ShortcutService.parseLongAttribute(xmlPullParser, ATTR_LAST_UPDATE_TIME);
        int outerDepth3 = 1;
        boolean shadow3 = fromBackup || ShortcutService.parseBooleanAttribute(xmlPullParser, ATTR_SHADOW);
        long backupSourceVersion = ShortcutService.parseLongAttribute(xmlPullParser, ATTR_BACKUP_SOURCE_VERSION, -1);
        boolean backupAllowed = ShortcutService.parseBooleanAttribute(xmlPullParser, ATTR_BACKUP_ALLOWED, true);
        boolean backupSourceBackupAllowed = ShortcutService.parseBooleanAttribute(xmlPullParser, ATTR_BACKUP_SOURCE_BACKUP_ALLOWED, true);
        ArrayList<byte[]> hashes = new ArrayList<>();
        int outerDepth4 = parser.getDepth();
        while (true) {
            int outerDepth5 = outerDepth4;
            int next = parser.next();
            int type = next;
            if (next == outerDepth3) {
                int i = type;
                shadow = shadow3;
                int i2 = outerDepth5;
                break;
            }
            if (type == 3) {
                outerDepth = outerDepth5;
                if (parser.getDepth() <= outerDepth) {
                    int i3 = type;
                    int i4 = outerDepth;
                    shadow = shadow3;
                    break;
                }
            } else {
                outerDepth = outerDepth5;
            }
            if (type != 2) {
                outerDepth2 = outerDepth;
                shadow2 = shadow3;
            } else {
                int depth = parser.getDepth();
                int i5 = type;
                String tag = parser.getName();
                shadow2 = shadow3;
                if (depth == outerDepth + 1) {
                    outerDepth2 = outerDepth;
                    if (((tag.hashCode() == 1073584312 && tag.equals(TAG_SIGNATURE)) ? (char) 0 : 65535) == 0) {
                        hashes.add(Base64.getDecoder().decode(ShortcutService.parseStringAttribute(xmlPullParser, ATTR_SIGNATURE_HASH)));
                    }
                } else {
                    outerDepth2 = outerDepth;
                }
                ShortcutService.warnForInvalidTag(depth, tag);
            }
            shadow3 = shadow2;
            outerDepth4 = outerDepth2;
            outerDepth3 = 1;
        }
        if (fromBackup) {
            this.mVersionCode = -1;
            this.mBackupSourceVersionCode = versionCode;
            this.mBackupSourceBackupAllowed = backupAllowed;
        } else {
            this.mVersionCode = versionCode;
            this.mBackupSourceVersionCode = backupSourceVersion;
            this.mBackupSourceBackupAllowed = backupSourceBackupAllowed;
        }
        this.mLastUpdateTime = lastUpdateTime;
        this.mIsShadow = shadow;
        this.mSigHashes = hashes;
        this.mBackupAllowed = false;
        this.mBackupAllowedInitialized = false;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println();
        pw.print(prefix);
        pw.println("PackageInfo:");
        pw.print(prefix);
        pw.print("  IsShadow: ");
        pw.print(this.mIsShadow);
        pw.print(this.mIsShadow ? " (not installed)" : " (installed)");
        pw.println();
        pw.print(prefix);
        pw.print("  Version: ");
        pw.print(this.mVersionCode);
        pw.println();
        if (this.mBackupAllowedInitialized) {
            pw.print(prefix);
            pw.print("  Backup Allowed: ");
            pw.print(this.mBackupAllowed);
            pw.println();
        }
        if (this.mBackupSourceVersionCode != -1) {
            pw.print(prefix);
            pw.print("  Backup source version: ");
            pw.print(this.mBackupSourceVersionCode);
            pw.println();
            pw.print(prefix);
            pw.print("  Backup source backup allowed: ");
            pw.print(this.mBackupSourceBackupAllowed);
            pw.println();
        }
        pw.print(prefix);
        pw.print("  Last package update time: ");
        pw.print(this.mLastUpdateTime);
        pw.println();
        for (int i = 0; i < this.mSigHashes.size(); i++) {
            pw.print(prefix);
            pw.print("    ");
            pw.print("SigHash: ");
            pw.println(HexEncoding.encode(this.mSigHashes.get(i)));
        }
    }
}
