package com.android.server.pm;

import android.content.pm.PackageInfo;
import android.util.Slog;
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
    private static final String ATTR_LAST_UPDATE_TIME = "last_udpate_time";
    private static final String ATTR_SHADOW = "shadow";
    private static final String ATTR_SIGNATURE_HASH = "hash";
    private static final String ATTR_VERSION = "version";
    private static final String TAG = "ShortcutService";
    static final String TAG_ROOT = "package-info";
    private static final String TAG_SIGNATURE = "signature";
    private static final int VERSION_UNKNOWN = -1;
    private boolean mIsShadow;
    private long mLastUpdateTime;
    private ArrayList<byte[]> mSigHashes;
    private int mVersionCode = -1;

    private ShortcutPackageInfo(int versionCode, long lastUpdateTime, ArrayList<byte[]> sigHashes, boolean isShadow) {
        this.mVersionCode = versionCode;
        this.mLastUpdateTime = lastUpdateTime;
        this.mIsShadow = isShadow;
        this.mSigHashes = sigHashes;
    }

    public static ShortcutPackageInfo newEmpty() {
        return new ShortcutPackageInfo(-1, 0, new ArrayList(0), false);
    }

    public boolean isShadow() {
        return this.mIsShadow;
    }

    public void setShadow(boolean shadow) {
        this.mIsShadow = shadow;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public long getLastUpdateTime() {
        return this.mLastUpdateTime;
    }

    public void updateVersionInfo(PackageInfo pi) {
        if (pi != null) {
            this.mVersionCode = pi.versionCode;
            this.mLastUpdateTime = pi.lastUpdateTime;
        }
    }

    public boolean hasSignatures() {
        return this.mSigHashes.size() > 0;
    }

    public boolean canRestoreTo(ShortcutService s, PackageInfo target) {
        if (!s.shouldBackupApp(target)) {
            Slog.w(TAG, "Can't restore: package no longer allows backup");
            return false;
        } else if (target.versionCode < this.mVersionCode) {
            Slog.w(TAG, String.format("Can't restore: package current version %d < backed up version %d", new Object[]{Integer.valueOf(target.versionCode), Integer.valueOf(this.mVersionCode)}));
            return false;
        } else if (BackupUtils.signaturesMatch(this.mSigHashes, target)) {
            return true;
        } else {
            Slog.w(TAG, "Can't restore: Package signature mismatch");
            return false;
        }
    }

    public static ShortcutPackageInfo generateForInstalledPackageForTest(ShortcutService s, String packageName, int packageUserId) {
        PackageInfo pi = s.getPackageInfoWithSignatures(packageName, packageUserId);
        if (pi.signatures != null && pi.signatures.length != 0) {
            return new ShortcutPackageInfo(pi.versionCode, pi.lastUpdateTime, BackupUtils.hashSignatureArray(pi.signatures), false);
        }
        Slog.e(TAG, "Can't get signatures: package=" + packageName);
        return null;
    }

    public void refreshSignature(ShortcutService s, ShortcutPackageItem pkg) {
        if (this.mIsShadow) {
            s.wtf("Attempted to refresh package info for shadow package " + pkg.getPackageName() + ", user=" + pkg.getOwnerUserId());
            return;
        }
        PackageInfo pi = s.getPackageInfoWithSignatures(pkg.getPackageName(), pkg.getPackageUserId());
        if (pi == null) {
            Slog.w(TAG, "Package not found: " + pkg.getPackageName());
        } else {
            this.mSigHashes = BackupUtils.hashSignatureArray(pi.signatures);
        }
    }

    public void saveToXml(XmlSerializer out) throws IOException {
        out.startTag(null, TAG_ROOT);
        ShortcutService.writeAttr(out, ATTR_VERSION, (long) this.mVersionCode);
        ShortcutService.writeAttr(out, ATTR_LAST_UPDATE_TIME, this.mLastUpdateTime);
        ShortcutService.writeAttr(out, ATTR_SHADOW, this.mIsShadow);
        for (int i = 0; i < this.mSigHashes.size(); i++) {
            out.startTag(null, TAG_SIGNATURE);
            ShortcutService.writeAttr(out, ATTR_SIGNATURE_HASH, Base64.getEncoder().encodeToString((byte[]) this.mSigHashes.get(i)));
            out.endTag(null, TAG_SIGNATURE);
        }
        out.endTag(null, TAG_ROOT);
    }

    public void loadFromXml(XmlPullParser parser, boolean fromBackup) throws IOException, XmlPullParserException {
        int versionCode = ShortcutService.parseIntAttribute(parser, ATTR_VERSION);
        long lastUpdateTime = ShortcutService.parseLongAttribute(parser, ATTR_LAST_UPDATE_TIME);
        boolean shadow = !fromBackup ? ShortcutService.parseBooleanAttribute(parser, ATTR_SHADOW) : true;
        ArrayList<byte[]> hashes = new ArrayList();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                this.mVersionCode = versionCode;
                this.mLastUpdateTime = lastUpdateTime;
                this.mIsShadow = shadow;
                this.mSigHashes = hashes;
            } else if (type == 2) {
                int depth = parser.getDepth();
                String tag = parser.getName();
                if (depth == outerDepth + 1 && tag.equals(TAG_SIGNATURE)) {
                    hashes.add(Base64.getDecoder().decode(ShortcutService.parseStringAttribute(parser, ATTR_SIGNATURE_HASH)));
                } else {
                    ShortcutService.warnForInvalidTag(depth, tag);
                }
            }
        }
        this.mVersionCode = versionCode;
        this.mLastUpdateTime = lastUpdateTime;
        this.mIsShadow = shadow;
        this.mSigHashes = hashes;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println();
        pw.print(prefix);
        pw.println("PackageInfo:");
        pw.print(prefix);
        pw.print("  IsShadow: ");
        pw.print(this.mIsShadow);
        pw.println();
        pw.print(prefix);
        pw.print("  Version: ");
        pw.print(this.mVersionCode);
        pw.println();
        pw.print(prefix);
        pw.print("  Last package update time: ");
        pw.print(this.mLastUpdateTime);
        pw.println();
        for (int i = 0; i < this.mSigHashes.size(); i++) {
            pw.print(prefix);
            pw.print("    ");
            pw.print("SigHash: ");
            pw.println(HexEncoding.encode((byte[]) this.mSigHashes.get(i)));
        }
    }
}
