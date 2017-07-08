package com.android.server;

import android.content.ComponentName;
import android.content.pm.FeatureInfo;
import android.os.Environment;
import android.os.Process;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SystemConfig {
    private static final int ALLOW_ALL = -1;
    private static final int ALLOW_APP_CONFIGS = 8;
    private static final int ALLOW_FEATURES = 1;
    private static final int ALLOW_LIBS = 2;
    private static final int ALLOW_PERMISSIONS = 4;
    static final String TAG = "SystemConfig";
    static SystemConfig sInstance;
    final int CUST_TYPE_CONFIG;
    final ArraySet<String> mAllowInDataUsageSave;
    final ArraySet<String> mAllowInPowerSave;
    final ArraySet<String> mAllowInPowerSaveExceptIdle;
    final ArrayMap<String, FeatureInfo> mAvailableFeatures;
    final ArraySet<ComponentName> mBackupTransportWhitelist;
    final ArraySet<ComponentName> mDefaultVrComponents;
    int[] mGlobalGids;
    final ArraySet<String> mLinkedApps;
    final ArrayMap<String, PermissionEntry> mPermissions;
    final ArrayMap<String, String> mSharedLibraries;
    final SparseArray<ArraySet<String>> mSystemPermissions;
    final ArraySet<String> mSystemUserBlacklistedApps;
    final ArraySet<String> mSystemUserWhitelistedApps;
    final ArraySet<String> mUnavailableFeatures;

    public static final class PermissionEntry {
        public int[] gids;
        public final String name;
        public boolean perUser;

        PermissionEntry(String name, boolean perUser) {
            this.name = name;
            this.perUser = perUser;
        }
    }

    private void readPermissionsFromXml(java.io.File r38, int r39) {
        /* JADX: method processing error */
/*
        Error: java.lang.OutOfMemoryError: Java heap space
	at java.util.Arrays.copyOf(Arrays.java:3181)
	at java.util.ArrayList.grow(ArrayList.java:261)
	at java.util.ArrayList.ensureExplicitCapacity(ArrayList.java:235)
	at java.util.ArrayList.ensureCapacityInternal(ArrayList.java:227)
	at java.util.ArrayList.add(ArrayList.java:458)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:447)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
*/
        /*
        r37 = this;
        r27 = 0;
        r27 = new java.io.FileReader;	 Catch:{ FileNotFoundException -> 0x008e }
        r0 = r27;	 Catch:{ FileNotFoundException -> 0x008e }
        r1 = r38;	 Catch:{ FileNotFoundException -> 0x008e }
        r0.<init>(r1);	 Catch:{ FileNotFoundException -> 0x008e }
        r22 = android.app.ActivityManager.isLowRamDeviceStatic();
        r25 = android.util.Xml.newPullParser();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r27;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.setInput(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x001a:
        r31 = r25.next();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = 2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r31;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r0 == r1) goto L_0x002e;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0026:
        r34 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r31;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r0 != r1) goto L_0x001a;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x002e:
        r34 = 2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r31;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r0 == r1) goto L_0x00ae;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0036:
        r34 = new org.xmlpull.v1.XmlPullParserException;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = "No start tag found";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34.<init>(r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        throw r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x003f:
        r13 = move-exception;
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = "Got exception parsing permissions.";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r0, r1, r13);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        libcore.io.IoUtils.closeQuietly(r27);
    L_0x0050:
        r34 = android.os.storage.StorageManager.isFileEncryptedNativeOnly();
        if (r34 == 0) goto L_0x0072;
    L_0x0056:
        r34 = "android.software.file_based_encryption";
        r35 = 0;
        r0 = r37;
        r1 = r34;
        r2 = r35;
        r0.addFeature(r1, r2);
        r34 = "android.software.securely_removes_users";
        r35 = 0;
        r0 = r37;
        r1 = r34;
        r2 = r35;
        r0.addFeature(r1, r2);
    L_0x0072:
        r0 = r37;
        r0 = r0.mUnavailableFeatures;
        r34 = r0;
        r15 = r34.iterator();
    L_0x007c:
        r34 = r15.hasNext();
        if (r34 == 0) goto L_0x084c;
    L_0x0082:
        r14 = r15.next();
        r14 = (java.lang.String) r14;
        r0 = r37;
        r0.removeFeature(r14);
        goto L_0x007c;
    L_0x008e:
        r11 = move-exception;
        r34 = "SystemConfig";
        r35 = new java.lang.StringBuilder;
        r35.<init>();
        r36 = "Couldn't find or open permissions file ";
        r35 = r35.append(r36);
        r0 = r35;
        r1 = r38;
        r35 = r0.append(r1);
        r35 = r35.toString();
        android.util.Slog.w(r34, r35);
        return;
    L_0x00ae:
        r34 = r25.getName();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = "permissions";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r34.equals(r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 != 0) goto L_0x00c8;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00bb:
        r34 = r25.getName();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = "config";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r34.equals(r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x00f9;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00c8:
        r34 = -1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r39;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r0 != r1) goto L_0x0140;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00d0:
        r3 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00d1:
        r34 = r39 & 2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0142;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00d5:
        r6 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00d6:
        r34 = r39 & 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0144;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00da:
        r5 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00db:
        r34 = r39 & 4;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0146;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00df:
        r7 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00e0:
        r34 = r39 & 8;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0148;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00e4:
        r4 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x00e5:
        com.android.internal.util.XmlUtils.nextElement(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r25.getEventType();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 1;
        r0 = r34;
        r1 = r35;
        if (r0 != r1) goto L_0x014a;
    L_0x00f4:
        libcore.io.IoUtils.closeQuietly(r27);
        goto L_0x0050;
    L_0x00f9:
        r34 = new org.xmlpull.v1.XmlPullParserException;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "Unexpected start tag in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = ": found ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getName();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = ", expected 'permissions' or 'config'";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34.<init>(r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        throw r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x012d:
        r12 = move-exception;
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = "Got exception parsing permissions.";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r0, r1, r12);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        libcore.io.IoUtils.closeQuietly(r27);
        goto L_0x0050;
    L_0x0140:
        r3 = 0;
        goto L_0x00d1;
    L_0x0142:
        r6 = 0;
        goto L_0x00d6;
    L_0x0144:
        r5 = 0;
        goto L_0x00db;
    L_0x0146:
        r7 = 0;
        goto L_0x00e0;
    L_0x0148:
        r4 = 0;
        goto L_0x00e5;
    L_0x014a:
        r23 = r25.getName();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = "group";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x01be;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x015b:
        if (r3 == 0) goto L_0x01be;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x015d:
        r34 = "gid";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r19 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r19 == 0) goto L_0x0190;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x016e:
        r18 = android.os.Process.getGidForName(r19);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mGlobalGids;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r18;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = com.android.internal.util.ArrayUtils.appendInt(r0, r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1.mGlobalGids = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0186:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;
    L_0x018b:
        r34 = move-exception;
        libcore.io.IoUtils.closeQuietly(r27);
        throw r34;
    L_0x0190:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<group> without gid in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x0186;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x01be:
        r34 = "permission";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x021f;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x01cb:
        if (r7 == 0) goto L_0x021f;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x01cd:
        r34 = "name";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r26 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r26 != 0) goto L_0x0210;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x01de:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<permission> without name in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0210:
        r26 = r26.intern();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r26;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.readPermission(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x021f:
        r34 = "assign-permission";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0331;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x022c:
        if (r7 == 0) goto L_0x0331;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x022e:
        r34 = "name";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r26 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r26 != 0) goto L_0x0271;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x023f:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<assign-permission> without name in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0271:
        r34 = "uid";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r33 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r33 != 0) goto L_0x02b4;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0282:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<assign-permission> without uid in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x02b4:
        r32 = android.os.Process.getUidForName(r33);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r32 >= 0) goto L_0x02fb;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x02ba:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<assign-permission> with unknown uid \"";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r33;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "  in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x02fb:
        r26 = r26.intern();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mSystemPermissions;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r32;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r28 = r0.get(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r28 = (android.util.ArraySet) r28;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r28 != 0) goto L_0x0325;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0311:
        r28 = new android.util.ArraySet;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r28.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mSystemPermissions;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r32;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r28;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.put(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0325:
        r0 = r28;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r26;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0331:
        r34 = "library";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x03d2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x033e:
        if (r6 == 0) goto L_0x03d2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0340:
        r34 = "name";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r21 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = "file";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r20 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r21 != 0) goto L_0x0392;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0360:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<library> without name in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x038d:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0392:
        if (r20 != 0) goto L_0x03c2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0394:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<library> without file in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x038d;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x03c2:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mSharedLibraries;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r21;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r20;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.put(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x038d;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x03d2:
        r34 = "feature";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0462;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x03df:
        if (r5 == 0) goto L_0x0462;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x03e1:
        r34 = "name";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r16 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = "version";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r17 = com.android.internal.util.XmlUtils.readIntAttribute(r0, r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r22 != 0) goto L_0x0436;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0401:
        r8 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0402:
        if (r16 != 0) goto L_0x0456;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0404:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<feature> without name in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0431:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0436:
        r34 = "notLowRam";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r24 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = "true";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r24;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0454;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0452:
        r8 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x0402;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0454:
        r8 = 1;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x0402;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0456:
        if (r8 == 0) goto L_0x0431;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0458:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r16;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r17;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.addFeature(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x0431;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0462:
        r34 = "unavailable-feature";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x04c2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x046f:
        if (r5 == 0) goto L_0x04c2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0471:
        r34 = "name";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r16 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r16 != 0) goto L_0x04b4;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0482:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<unavailable-feature> without name in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x04af:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x04b4:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mUnavailableFeatures;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r16;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x04af;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x04c2:
        r34 = "allow-in-power-save-except-idle";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0522;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x04cf:
        if (r3 == 0) goto L_0x0522;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x04d1:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x0514;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x04e2:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<allow-in-power-save-except-idle> without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x050f:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0514:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mAllowInPowerSaveExceptIdle;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x050f;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0522:
        r34 = "allow-in-power-save";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0582;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x052f:
        if (r3 == 0) goto L_0x0582;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0531:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x0574;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0542:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<allow-in-power-save> without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x056f:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0574:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mAllowInPowerSave;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x056f;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0582:
        r34 = "allow-in-data-usage-save";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x05e2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x058f:
        if (r3 == 0) goto L_0x05e2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0591:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x05d4;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x05a2:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<allow-in-data-usage-save> without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x05cf:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x05d4:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mAllowInDataUsageSave;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x05cf;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x05e2:
        r34 = "app-link";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0642;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x05ef:
        if (r4 == 0) goto L_0x0642;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x05f1:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x0634;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0602:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<app-link> without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x062f:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0634:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mLinkedApps;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x062f;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0642:
        r34 = "system-user-whitelisted-app";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x06a2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x064f:
        if (r4 == 0) goto L_0x06a2;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0651:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x0694;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0662:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<system-user-whitelisted-app> without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x068f:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0694:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mSystemUserWhitelistedApps;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x068f;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x06a2:
        r34 = "system-user-blacklisted-app";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0702;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x06af:
        if (r4 == 0) goto L_0x0702;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x06b1:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x06f4;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x06c2:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<system-user-blacklisted-app without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x06ef:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x06f4:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mSystemUserBlacklistedApps;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x06ef;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0702:
        r34 = "default-enabled-vr-app";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x07a6;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x070f:
        if (r4 == 0) goto L_0x07a6;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0711:
        r34 = "package";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r29 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = "class";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r9 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r29 != 0) goto L_0x0763;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0731:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<default-enabled-vr-app without package in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x075e:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0763:
        if (r9 != 0) goto L_0x0793;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0765:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<default-enabled-vr-app without class in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x075e;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0793:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mDefaultVrComponents;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new android.content.ComponentName;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r29;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.<init>(r1, r9);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34.add(r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x075e;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07a6:
        r34 = "backup-transport-whitelisted-service";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r23;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0.equals(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r34 == 0) goto L_0x0847;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07b3:
        if (r5 == 0) goto L_0x0847;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07b5:
        r34 = "service";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = 0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r25;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r2 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r30 = r0.getAttributeValue(r1, r2);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r30 != 0) goto L_0x07f8;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07c6:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<backup-transport-whitelisted-service> without service in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07f3:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07f8:
        r10 = android.content.ComponentName.unflattenFromString(r30);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        if (r10 != 0) goto L_0x083b;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x07fe:
        r34 = "SystemConfig";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35.<init>();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = "<backup-transport-whitelisted-service> with invalid service name ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r30;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " in ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r35;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r1 = r38;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r0.append(r1);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = " at ";	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r36 = r25.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.append(r36);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r35 = r35.toString();	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        android.util.Slog.w(r34, r35);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x07f3;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x083b:
        r0 = r37;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r0.mBackupTransportWhitelist;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r34 = r0;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0 = r34;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        r0.add(r10);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x07f3;	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
    L_0x0847:
        com.android.internal.util.XmlUtils.skipCurrentTag(r25);	 Catch:{ XmlPullParserException -> 0x003f, IOException -> 0x012d, all -> 0x018b }
        goto L_0x00e5;
    L_0x084c:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.SystemConfig.readPermissionsFromXml(java.io.File, int):void");
    }

    public static SystemConfig getInstance() {
        SystemConfig systemConfig;
        synchronized (SystemConfig.class) {
            if (sInstance == null) {
                sInstance = new SystemConfig();
            }
            systemConfig = sInstance;
        }
        return systemConfig;
    }

    public int[] getGlobalGids() {
        return this.mGlobalGids;
    }

    public SparseArray<ArraySet<String>> getSystemPermissions() {
        return this.mSystemPermissions;
    }

    public ArrayMap<String, String> getSharedLibraries() {
        return this.mSharedLibraries;
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeatures() {
        return this.mAvailableFeatures;
    }

    public ArrayMap<String, PermissionEntry> getPermissions() {
        return this.mPermissions;
    }

    public ArraySet<String> getAllowInPowerSaveExceptIdle() {
        return this.mAllowInPowerSaveExceptIdle;
    }

    public ArraySet<String> getAllowInPowerSave() {
        return this.mAllowInPowerSave;
    }

    public ArraySet<String> getAllowInDataUsageSave() {
        return this.mAllowInDataUsageSave;
    }

    public ArraySet<String> getLinkedApps() {
        return this.mLinkedApps;
    }

    public ArraySet<String> getSystemUserWhitelistedApps() {
        return this.mSystemUserWhitelistedApps;
    }

    public ArraySet<String> getSystemUserBlacklistedApps() {
        return this.mSystemUserBlacklistedApps;
    }

    public ArraySet<ComponentName> getDefaultVrComponents() {
        return this.mDefaultVrComponents;
    }

    public ArraySet<ComponentName> getBackupTransportWhitelist() {
        return this.mBackupTransportWhitelist;
    }

    SystemConfig() {
        this.CUST_TYPE_CONFIG = 0;
        this.mSystemPermissions = new SparseArray();
        this.mSharedLibraries = new ArrayMap();
        this.mAvailableFeatures = new ArrayMap();
        this.mUnavailableFeatures = new ArraySet();
        this.mPermissions = new ArrayMap();
        this.mAllowInPowerSaveExceptIdle = new ArraySet();
        this.mAllowInPowerSave = new ArraySet();
        this.mAllowInDataUsageSave = new ArraySet();
        this.mLinkedApps = new ArraySet();
        this.mSystemUserWhitelistedApps = new ArraySet();
        this.mSystemUserBlacklistedApps = new ArraySet();
        this.mDefaultVrComponents = new ArraySet();
        this.mBackupTransportWhitelist = new ArraySet();
        File rootDirectory = Environment.getRootDirectory();
        String[] strArr = new String[ALLOW_LIBS];
        strArr[0] = "etc";
        strArr[ALLOW_FEATURES] = "sysconfig";
        readPermissions(Environment.buildPath(rootDirectory, strArr), ALLOW_ALL);
        rootDirectory = Environment.getRootDirectory();
        strArr = new String[ALLOW_LIBS];
        strArr[0] = "etc";
        strArr[ALLOW_FEATURES] = "permissions";
        readPermissions(Environment.buildPath(rootDirectory, strArr), ALLOW_ALL);
        rootDirectory = Environment.getOdmDirectory();
        strArr = new String[ALLOW_LIBS];
        strArr[0] = "etc";
        strArr[ALLOW_FEATURES] = "sysconfig";
        readPermissions(Environment.buildPath(rootDirectory, strArr), 11);
        rootDirectory = Environment.getOdmDirectory();
        strArr = new String[ALLOW_LIBS];
        strArr[0] = "etc";
        strArr[ALLOW_FEATURES] = "permissions";
        readPermissions(Environment.buildPath(rootDirectory, strArr), 11);
        rootDirectory = Environment.getOemDirectory();
        strArr = new String[ALLOW_LIBS];
        strArr[0] = "etc";
        strArr[ALLOW_FEATURES] = "sysconfig";
        readPermissions(Environment.buildPath(rootDirectory, strArr), ALLOW_FEATURES);
        rootDirectory = Environment.getOemDirectory();
        strArr = new String[ALLOW_LIBS];
        strArr[0] = "etc";
        strArr[ALLOW_FEATURES] = "permissions";
        readPermissions(Environment.buildPath(rootDirectory, strArr), ALLOW_FEATURES);
        readCustPermissions();
    }

    void readCustPermissions() {
        String[] dirs = new String[0];
        File rootDirectory = Environment.getRootDirectory();
        String[] strArr = new String[ALLOW_FEATURES];
        strArr[0] = "etc";
        String sysPath = getCanonicalPathOrNull(Environment.buildPath(rootDirectory, strArr));
        try {
            dirs = HwCfgFilePolicy.getCfgPolicyDir(0);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (sysPath != null && dirs.length > 0) {
            int length = dirs.length;
            for (int i = 0; i < length; i += ALLOW_FEATURES) {
                File file = new File(dirs[i]);
                String dirPath = getCanonicalPathOrNull(file);
                if (!(dirPath == null || dirPath.equals(sysPath))) {
                    String[] strArr2 = new String[ALLOW_FEATURES];
                    strArr2[0] = "sysconfig";
                    readPermissions(Environment.buildPath(file, strArr2), ALLOW_ALL);
                    strArr2 = new String[ALLOW_FEATURES];
                    strArr2[0] = "permissions";
                    readPermissions(Environment.buildPath(file, strArr2), ALLOW_ALL);
                }
            }
        }
    }

    String getCanonicalPathOrNull(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            Slog.d(TAG, "Unable to resolve canonical");
            return null;
        }
    }

    void readPermissions(File libraryDir, int permissionFlag) {
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            if (permissionFlag == ALLOW_ALL) {
                Slog.w(TAG, "No directory " + libraryDir + ", skipping");
            }
        } else if (libraryDir.canRead()) {
            File platformFile = null;
            File[] listFiles = libraryDir.listFiles();
            int length = listFiles.length;
            for (int i = 0; i < length; i += ALLOW_FEATURES) {
                File f = listFiles[i];
                if (f.getPath().endsWith("etc/permissions/platform.xml")) {
                    platformFile = f;
                } else if (!f.getPath().endsWith(".xml")) {
                    Slog.i(TAG, "Non-xml file " + f + " in " + libraryDir + " directory, ignoring");
                } else if (f.canRead()) {
                    readPermissionsFromXml(f, permissionFlag);
                } else {
                    Slog.w(TAG, "Permissions library file " + f + " cannot be read");
                }
            }
            if (platformFile != null) {
                readPermissionsFromXml(platformFile, permissionFlag);
            }
        } else {
            Slog.w(TAG, "Directory " + libraryDir + " cannot be read");
        }
    }

    private void addFeature(String name, int version) {
        FeatureInfo fi = (FeatureInfo) this.mAvailableFeatures.get(name);
        if (fi == null) {
            fi = new FeatureInfo();
            fi.name = name;
            fi.version = version;
            this.mAvailableFeatures.put(name, fi);
            return;
        }
        fi.version = Math.max(fi.version, version);
    }

    private void removeFeature(String name) {
        if (this.mAvailableFeatures.remove(name) != null) {
            Slog.d(TAG, "Removed unavailable feature " + name);
        }
    }

    void readPermission(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        if (this.mPermissions.containsKey(name)) {
            throw new IllegalStateException("Duplicate permission definition for " + name);
        }
        PermissionEntry perm = new PermissionEntry(name, XmlUtils.readBooleanAttribute(parser, "perUser", false));
        this.mPermissions.put(name, perm);
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == ALLOW_FEATURES) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == ALLOW_PERMISSIONS)) {
                if ("group".equals(parser.getName())) {
                    String gidStr = parser.getAttributeValue(null, "gid");
                    if (gidStr != null) {
                        perm.gids = ArrayUtils.appendInt(perm.gids, Process.getGidForName(gidStr));
                    } else {
                        Slog.w(TAG, "<group> without gid at " + parser.getPositionDescription());
                    }
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }
}
