package com.android.server.security.securitydiagnose;

import android.content.pm.PackageParser.Package;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AntiMalApkInfo implements Parcelable, Comparable<AntiMalApkInfo> {
    public static final Creator<AntiMalApkInfo> CREATOR = null;
    private static final String TAG = "AntiMalApkInfo";
    public final String mApkName;
    public String mFrom;
    public final String mLastModifyTime;
    public final String mPackageName;
    public final String mPath;
    public final int mType;
    public int mVersion;

    public interface AntiMalType {
        public static final int APK_DELETED = 3;
        public static final int NORMAL = 0;
        public static final int NOT_IN_WHITE_LIST = 1;
        public static final int SIGNATURE_CHANGED = 2;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.securitydiagnose.AntiMalApkInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.securitydiagnose.AntiMalApkInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.securitydiagnose.AntiMalApkInfo.<clinit>():void");
    }

    private String formatTime(long minSec) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(minSec));
    }

    private String formatPath(String path) {
        if (path == null || !path.startsWith("/")) {
            return path;
        }
        return path.substring(1, path.length());
    }

    private String getFileLastModifiedTime(String fileName) {
        File apkFile = new File(fileName);
        try {
            if (apkFile.exists()) {
                return formatTime(apkFile.lastModified());
            }
        } catch (Exception e) {
            Log.e(TAG, "getFileLastModifiedTime e:" + e);
        }
        return null;
    }

    public AntiMalApkInfo(Package pkg, int type) {
        if (pkg != null) {
            this.mPackageName = pkg.packageName;
            this.mPath = formatPath(pkg.baseCodePath);
            this.mApkName = null;
            this.mType = type;
            this.mLastModifyTime = getFileLastModifiedTime(pkg.codePath);
            this.mVersion = pkg.mVersionCode;
            return;
        }
        this.mPackageName = null;
        this.mPath = null;
        this.mApkName = null;
        this.mType = -1;
        this.mLastModifyTime = null;
        this.mFrom = null;
        this.mVersion = -1;
    }

    public AntiMalApkInfo(String packageName, String path, String apkName, int type, String time, String from, int version) {
        this.mPackageName = packageName;
        this.mPath = path;
        this.mApkName = apkName;
        this.mType = type;
        this.mLastModifyTime = time;
        this.mFrom = from;
        this.mVersion = version;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeString(this.mPackageName);
            dest.writeString(this.mPath);
            dest.writeString(this.mApkName);
            dest.writeInt(this.mType);
            dest.writeString(this.mLastModifyTime);
            dest.writeString(this.mFrom);
            dest.writeInt(this.mVersion);
        }
    }

    private AntiMalApkInfo(Parcel source) {
        if (source != null) {
            this.mPackageName = source.readString();
            this.mPath = source.readString();
            this.mApkName = source.readString();
            this.mType = source.readInt();
            this.mLastModifyTime = source.readString();
            this.mFrom = source.readString();
            this.mVersion = source.readInt();
            return;
        }
        this.mPackageName = null;
        this.mPath = null;
        this.mApkName = null;
        this.mType = -1;
        this.mLastModifyTime = null;
        this.mFrom = null;
        this.mVersion = -1;
    }

    private boolean stringEquals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        } else {
            if (str1.equalsIgnoreCase(str2)) {
                return true;
            }
            if (str1.length() >= 1 || str2 != null) {
                return false;
            }
            return true;
        }
    }

    public int compareTo(AntiMalApkInfo apkInfo) {
        if (apkInfo == null) {
            return 1;
        }
        return this.mPackageName != null ? this.mPackageName.compareTo(apkInfo.mPackageName) : -1;
    }

    public int hashCode() {
        return (super.hashCode() + this.mPackageName.hashCode()) + this.mApkName.hashCode();
    }

    public boolean equals(Object in) {
        boolean z = false;
        if (in == null || !(in instanceof AntiMalApkInfo)) {
            return false;
        }
        AntiMalApkInfo apkInfo = (AntiMalApkInfo) in;
        if (stringEquals(this.mPackageName, apkInfo.mPackageName) && stringEquals(this.mPath, apkInfo.mPath) && stringEquals(this.mApkName, apkInfo.mApkName) && this.mType == apkInfo.mType && stringEquals(this.mLastModifyTime, apkInfo.mLastModifyTime) && stringEquals(this.mFrom, apkInfo.mFrom) && this.mVersion == apkInfo.mVersion) {
            z = true;
        }
        return z;
    }

    public String toString() {
        return "PackageName : " + this.mPackageName + " Path : " + this.mPath + " ApkName : " + this.mApkName + " Type : " + this.mType + " LastModifyTime : " + this.mLastModifyTime + " From : " + this.mFrom + " Version : " + this.mVersion;
    }
}
