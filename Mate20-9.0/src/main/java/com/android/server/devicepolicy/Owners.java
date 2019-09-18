package com.android.server.devicepolicy;

import android.app.AppOpsManagerInternal;
import android.app.admin.SystemUpdateInfo;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.LocalServices;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class Owners {
    private static final String ATTR_COMPONENT_NAME = "component";
    private static final String ATTR_FREEZE_RECORD_END = "end";
    private static final String ATTR_FREEZE_RECORD_START = "start";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PACKAGE = "package";
    private static final String ATTR_REMOTE_BUGREPORT_HASH = "remoteBugreportHash";
    private static final String ATTR_REMOTE_BUGREPORT_URI = "remoteBugreportUri";
    private static final String ATTR_USERID = "userId";
    private static final String ATTR_USER_RESTRICTIONS_MIGRATED = "userRestrictionsMigrated";
    private static final boolean DEBUG = false;
    private static final String DEVICE_OWNER_XML = "device_owner_2.xml";
    private static final String DEVICE_OWNER_XML_LEGACY = "device_owner.xml";
    private static final String PROFILE_OWNER_XML = "profile_owner.xml";
    private static final String TAG = "DevicePolicyManagerService";
    private static final String TAG_DEVICE_INITIALIZER = "device-initializer";
    private static final String TAG_DEVICE_OWNER = "device-owner";
    private static final String TAG_DEVICE_OWNER_CONTEXT = "device-owner-context";
    private static final String TAG_FREEZE_PERIOD_RECORD = "freeze-record";
    private static final String TAG_PENDING_OTA_INFO = "pending-ota-info";
    private static final String TAG_PROFILE_OWNER = "profile-owner";
    private static final String TAG_ROOT = "root";
    private static final String TAG_SYSTEM_UPDATE_POLICY = "system-update-policy";
    /* access modifiers changed from: private */
    public OwnerInfo mDeviceOwner;
    /* access modifiers changed from: private */
    public int mDeviceOwnerUserId;
    private final Injector mInjector;
    private final Object mLock;
    private final PackageManagerInternal mPackageManagerInternal;
    /* access modifiers changed from: private */
    public final ArrayMap<Integer, OwnerInfo> mProfileOwners;
    private boolean mSystemReady;
    /* access modifiers changed from: private */
    public LocalDate mSystemUpdateFreezeEnd;
    /* access modifiers changed from: private */
    public LocalDate mSystemUpdateFreezeStart;
    /* access modifiers changed from: private */
    public SystemUpdateInfo mSystemUpdateInfo;
    /* access modifiers changed from: private */
    public SystemUpdatePolicy mSystemUpdatePolicy;
    private final UserManager mUserManager;
    private final UserManagerInternal mUserManagerInternal;

    private class DeviceOwnerReadWriter extends FileReadWriter {
        protected DeviceOwnerReadWriter() {
            super(Owners.this.getDeviceOwnerFile());
        }

        /* access modifiers changed from: package-private */
        public boolean shouldWrite() {
            return (Owners.this.mDeviceOwner == null && Owners.this.mSystemUpdatePolicy == null && Owners.this.mSystemUpdateInfo == null) ? false : true;
        }

        /* access modifiers changed from: package-private */
        public void writeInner(XmlSerializer out) throws IOException {
            if (Owners.this.mDeviceOwner != null) {
                Owners.this.mDeviceOwner.writeToXml(out, Owners.TAG_DEVICE_OWNER);
                out.startTag(null, Owners.TAG_DEVICE_OWNER_CONTEXT);
                out.attribute(null, Owners.ATTR_USERID, String.valueOf(Owners.this.mDeviceOwnerUserId));
                out.endTag(null, Owners.TAG_DEVICE_OWNER_CONTEXT);
            }
            if (Owners.this.mSystemUpdatePolicy != null) {
                out.startTag(null, Owners.TAG_SYSTEM_UPDATE_POLICY);
                Owners.this.mSystemUpdatePolicy.saveToXml(out);
                out.endTag(null, Owners.TAG_SYSTEM_UPDATE_POLICY);
            }
            if (Owners.this.mSystemUpdateInfo != null) {
                Owners.this.mSystemUpdateInfo.writeToXml(out, Owners.TAG_PENDING_OTA_INFO);
            }
            if (Owners.this.mSystemUpdateFreezeStart != null || Owners.this.mSystemUpdateFreezeEnd != null) {
                out.startTag(null, Owners.TAG_FREEZE_PERIOD_RECORD);
                if (Owners.this.mSystemUpdateFreezeStart != null) {
                    out.attribute(null, Owners.ATTR_FREEZE_RECORD_START, Owners.this.mSystemUpdateFreezeStart.toString());
                }
                if (Owners.this.mSystemUpdateFreezeEnd != null) {
                    out.attribute(null, Owners.ATTR_FREEZE_RECORD_END, Owners.this.mSystemUpdateFreezeEnd.toString());
                }
                out.endTag(null, Owners.TAG_FREEZE_PERIOD_RECORD);
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x003e, code lost:
            if (r9.equals(com.android.server.devicepolicy.Owners.TAG_DEVICE_INITIALIZER) != false) goto L_0x004d;
         */
        public boolean readInner(XmlPullParser parser, int depth, String tag) {
            char c = 2;
            if (depth > 2) {
                return true;
            }
            switch (tag.hashCode()) {
                case -2101756875:
                    if (tag.equals(Owners.TAG_PENDING_OTA_INFO)) {
                        c = 4;
                        break;
                    }
                case -2038823445:
                    break;
                case -2020438916:
                    if (tag.equals(Owners.TAG_DEVICE_OWNER)) {
                        c = 0;
                        break;
                    }
                case -1900517026:
                    if (tag.equals(Owners.TAG_DEVICE_OWNER_CONTEXT)) {
                        c = 1;
                        break;
                    }
                case 1303827527:
                    if (tag.equals(Owners.TAG_FREEZE_PERIOD_RECORD)) {
                        c = 5;
                        break;
                    }
                case 1748301720:
                    if (tag.equals(Owners.TAG_SYSTEM_UPDATE_POLICY)) {
                        c = 3;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    OwnerInfo unused = Owners.this.mDeviceOwner = OwnerInfo.readFromXml(parser);
                    int unused2 = Owners.this.mDeviceOwnerUserId = 0;
                    break;
                case 1:
                    try {
                        int unused3 = Owners.this.mDeviceOwnerUserId = Integer.parseInt(parser.getAttributeValue(null, Owners.ATTR_USERID));
                        break;
                    } catch (NumberFormatException e) {
                        Slog.e(Owners.TAG, "Error parsing user-id " + userIdString);
                        break;
                    }
                case 2:
                    break;
                case 3:
                    SystemUpdatePolicy unused4 = Owners.this.mSystemUpdatePolicy = SystemUpdatePolicy.restoreFromXml(parser);
                    break;
                case 4:
                    SystemUpdateInfo unused5 = Owners.this.mSystemUpdateInfo = SystemUpdateInfo.readFromXml(parser);
                    break;
                case 5:
                    String startDate = parser.getAttributeValue(null, Owners.ATTR_FREEZE_RECORD_START);
                    String endDate = parser.getAttributeValue(null, Owners.ATTR_FREEZE_RECORD_END);
                    if (!(startDate == null || endDate == null)) {
                        LocalDate unused6 = Owners.this.mSystemUpdateFreezeStart = LocalDate.parse(startDate);
                        LocalDate unused7 = Owners.this.mSystemUpdateFreezeEnd = LocalDate.parse(endDate);
                        if (Owners.this.mSystemUpdateFreezeStart.isAfter(Owners.this.mSystemUpdateFreezeEnd)) {
                            Slog.e(Owners.TAG, "Invalid system update freeze record loaded");
                            LocalDate unused8 = Owners.this.mSystemUpdateFreezeStart = null;
                            LocalDate unused9 = Owners.this.mSystemUpdateFreezeEnd = null;
                            break;
                        }
                    }
                    break;
                default:
                    Slog.e(Owners.TAG, "Unexpected tag: " + tag);
                    return false;
            }
            return true;
        }
    }

    private static abstract class FileReadWriter {
        private final File mFile;

        /* access modifiers changed from: package-private */
        public abstract boolean readInner(XmlPullParser xmlPullParser, int i, String str);

        /* access modifiers changed from: package-private */
        public abstract boolean shouldWrite();

        /* access modifiers changed from: package-private */
        public abstract void writeInner(XmlSerializer xmlSerializer) throws IOException;

        protected FileReadWriter(File file) {
            this.mFile = file;
        }

        /* access modifiers changed from: package-private */
        public void writeToFileLocked() {
            if (!shouldWrite()) {
                if (this.mFile.exists() && !this.mFile.delete()) {
                    Slog.e(Owners.TAG, "Failed to remove " + this.mFile.getPath());
                }
                return;
            }
            AtomicFile f = new AtomicFile(this.mFile);
            FileOutputStream outputStream = null;
            try {
                outputStream = f.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(outputStream, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.startTag(null, Owners.TAG_ROOT);
                writeInner(out);
                out.endTag(null, Owners.TAG_ROOT);
                out.endDocument();
                out.flush();
                f.finishWrite(outputStream);
            } catch (IOException e) {
                Slog.e(Owners.TAG, "Exception when writing", e);
                if (outputStream != null) {
                    f.failWrite(outputStream);
                }
                FileOutputStream fileOutputStream = outputStream;
            }
        }

        /* access modifiers changed from: package-private */
        public void readFromFileLocked() {
            if (this.mFile.exists()) {
                InputStream input = null;
                try {
                    input = new AtomicFile(this.mFile).openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(input, StandardCharsets.UTF_8.name());
                    int depth = 0;
                    while (true) {
                        int next = parser.next();
                        int type = next;
                        if (next != 1) {
                            switch (type) {
                                case 2:
                                    depth++;
                                    String tag = parser.getName();
                                    if (depth != 1) {
                                        if (readInner(parser, depth, tag)) {
                                            break;
                                        } else {
                                            IoUtils.closeQuietly(input);
                                            return;
                                        }
                                    } else if (Owners.TAG_ROOT.equals(tag)) {
                                        break;
                                    } else {
                                        Slog.e(Owners.TAG, "Invalid root tag: " + tag);
                                        IoUtils.closeQuietly(input);
                                        return;
                                    }
                                case 3:
                                    depth--;
                                    break;
                            }
                        }
                    }
                } catch (IOException | XmlPullParserException e) {
                    Slog.e(Owners.TAG, "Error parsing owners information file", e);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(input);
                    throw th;
                }
                IoUtils.closeQuietly(input);
            }
        }
    }

    @VisibleForTesting
    public static class Injector {
        /* access modifiers changed from: package-private */
        public File environmentGetDataSystemDirectory() {
            return Environment.getDataSystemDirectory();
        }

        /* access modifiers changed from: package-private */
        public File environmentGetUserSystemDirectory(int userId) {
            return Environment.getUserSystemDirectory(userId);
        }
    }

    static class OwnerInfo {
        public final ComponentName admin;
        public final String name;
        public final String packageName;
        public String remoteBugreportHash;
        public String remoteBugreportUri;
        public boolean userRestrictionsMigrated;

        public OwnerInfo(String name2, String packageName2, boolean userRestrictionsMigrated2, String remoteBugreportUri2, String remoteBugreportHash2) {
            this.name = name2;
            this.packageName = packageName2;
            this.admin = new ComponentName(packageName2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            this.userRestrictionsMigrated = userRestrictionsMigrated2;
            this.remoteBugreportUri = remoteBugreportUri2;
            this.remoteBugreportHash = remoteBugreportHash2;
        }

        public OwnerInfo(String name2, ComponentName admin2, boolean userRestrictionsMigrated2, String remoteBugreportUri2, String remoteBugreportHash2) {
            this.name = name2;
            this.admin = admin2;
            this.packageName = admin2.getPackageName();
            this.userRestrictionsMigrated = userRestrictionsMigrated2;
            this.remoteBugreportUri = remoteBugreportUri2;
            this.remoteBugreportHash = remoteBugreportHash2;
        }

        public void writeToXml(XmlSerializer out, String tag) throws IOException {
            out.startTag(null, tag);
            out.attribute(null, "package", this.packageName);
            if (this.name != null) {
                out.attribute(null, "name", this.name);
            }
            if (this.admin != null) {
                out.attribute(null, Owners.ATTR_COMPONENT_NAME, this.admin.flattenToString());
            }
            out.attribute(null, Owners.ATTR_USER_RESTRICTIONS_MIGRATED, String.valueOf(this.userRestrictionsMigrated));
            if (this.remoteBugreportUri != null) {
                out.attribute(null, Owners.ATTR_REMOTE_BUGREPORT_URI, this.remoteBugreportUri);
            }
            if (this.remoteBugreportHash != null) {
                out.attribute(null, Owners.ATTR_REMOTE_BUGREPORT_HASH, this.remoteBugreportHash);
            }
            out.endTag(null, tag);
        }

        public static OwnerInfo readFromXml(XmlPullParser parser) {
            String packageName2 = parser.getAttributeValue(null, "package");
            String name2 = parser.getAttributeValue(null, "name");
            String componentName = parser.getAttributeValue(null, Owners.ATTR_COMPONENT_NAME);
            boolean userRestrictionsMigrated2 = "true".equals(parser.getAttributeValue(null, Owners.ATTR_USER_RESTRICTIONS_MIGRATED));
            String remoteBugreportUri2 = parser.getAttributeValue(null, Owners.ATTR_REMOTE_BUGREPORT_URI);
            String remoteBugreportHash2 = parser.getAttributeValue(null, Owners.ATTR_REMOTE_BUGREPORT_HASH);
            if (componentName != null) {
                ComponentName admin2 = ComponentName.unflattenFromString(componentName);
                if (admin2 != null) {
                    OwnerInfo ownerInfo = new OwnerInfo(name2, admin2, userRestrictionsMigrated2, remoteBugreportUri2, remoteBugreportHash2);
                    return ownerInfo;
                }
                Slog.e(Owners.TAG, "Error parsing owner file. Bad component name " + componentName);
            }
            OwnerInfo ownerInfo2 = new OwnerInfo(name2, packageName2, userRestrictionsMigrated2, remoteBugreportUri2, remoteBugreportHash2);
            return ownerInfo2;
        }

        public void dump(String prefix, PrintWriter pw) {
            pw.println(prefix + "admin=" + this.admin);
            pw.println(prefix + "name=" + this.name);
            pw.println(prefix + "package=" + this.packageName);
        }
    }

    private class ProfileOwnerReadWriter extends FileReadWriter {
        private final int mUserId;

        ProfileOwnerReadWriter(int userId) {
            super(Owners.this.getProfileOwnerFile(userId));
            this.mUserId = userId;
        }

        /* access modifiers changed from: package-private */
        public boolean shouldWrite() {
            return Owners.this.mProfileOwners.get(Integer.valueOf(this.mUserId)) != null;
        }

        /* access modifiers changed from: package-private */
        public void writeInner(XmlSerializer out) throws IOException {
            OwnerInfo profileOwner = (OwnerInfo) Owners.this.mProfileOwners.get(Integer.valueOf(this.mUserId));
            if (profileOwner != null) {
                profileOwner.writeToXml(out, Owners.TAG_PROFILE_OWNER);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean readInner(XmlPullParser parser, int depth, String tag) {
            if (depth > 2) {
                return true;
            }
            char c = 65535;
            if (tag.hashCode() == 2145316239 && tag.equals(Owners.TAG_PROFILE_OWNER)) {
                c = 0;
            }
            if (c != 0) {
                Slog.e(Owners.TAG, "Unexpected tag: " + tag);
                return false;
            }
            Owners.this.mProfileOwners.put(Integer.valueOf(this.mUserId), OwnerInfo.readFromXml(parser));
            return true;
        }
    }

    public Owners(UserManager userManager, UserManagerInternal userManagerInternal, PackageManagerInternal packageManagerInternal) {
        this(userManager, userManagerInternal, packageManagerInternal, new Injector());
    }

    @VisibleForTesting
    Owners(UserManager userManager, UserManagerInternal userManagerInternal, PackageManagerInternal packageManagerInternal, Injector injector) {
        this.mDeviceOwnerUserId = -10000;
        this.mProfileOwners = new ArrayMap<>();
        this.mLock = new Object();
        this.mUserManager = userManager;
        this.mUserManagerInternal = userManagerInternal;
        this.mPackageManagerInternal = packageManagerInternal;
        this.mInjector = injector;
    }

    /* access modifiers changed from: package-private */
    public void load() {
        synchronized (this.mLock) {
            File legacy = getLegacyConfigFile();
            List<UserInfo> users = this.mUserManager.getUsers(true);
            if (readLegacyOwnerFileLocked(legacy)) {
                writeDeviceOwner();
                for (Integer intValue : getProfileOwnerKeys()) {
                    writeProfileOwner(intValue.intValue());
                }
                if (!legacy.delete()) {
                    Slog.e(TAG, "Failed to remove the legacy setting file");
                }
            } else {
                new DeviceOwnerReadWriter().readFromFileLocked();
                for (UserInfo ui : users) {
                    new ProfileOwnerReadWriter(ui.id).readFromFileLocked();
                }
            }
            this.mUserManagerInternal.setDeviceManaged(hasDeviceOwner());
            for (UserInfo ui2 : users) {
                this.mUserManagerInternal.setUserManaged(ui2.id, hasProfileOwner(ui2.id));
            }
            if (hasDeviceOwner() && hasProfileOwner(getDeviceOwnerUserId())) {
                Slog.w(TAG, String.format("User %d has both DO and PO, which is not supported", new Object[]{Integer.valueOf(getDeviceOwnerUserId())}));
            }
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    private void pushToPackageManagerLocked() {
        SparseArray<String> po = new SparseArray<>();
        for (int i = this.mProfileOwners.size() - 1; i >= 0; i--) {
            po.put(this.mProfileOwners.keyAt(i).intValue(), this.mProfileOwners.valueAt(i).packageName);
        }
        this.mPackageManagerInternal.setDeviceAndProfileOwnerPackages(this.mDeviceOwnerUserId, this.mDeviceOwner != null ? this.mDeviceOwner.packageName : null, po);
    }

    /* access modifiers changed from: package-private */
    public String getDeviceOwnerPackageName() {
        String str;
        synchronized (this.mLock) {
            str = this.mDeviceOwner != null ? this.mDeviceOwner.packageName : null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public int getDeviceOwnerUserId() {
        int i;
        synchronized (this.mLock) {
            i = this.mDeviceOwnerUserId;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public Pair<Integer, ComponentName> getDeviceOwnerUserIdAndComponent() {
        synchronized (this.mLock) {
            if (this.mDeviceOwner == null) {
                return null;
            }
            Pair<Integer, ComponentName> create = Pair.create(Integer.valueOf(this.mDeviceOwnerUserId), this.mDeviceOwner.admin);
            return create;
        }
    }

    /* access modifiers changed from: package-private */
    public String getDeviceOwnerName() {
        String str;
        synchronized (this.mLock) {
            str = this.mDeviceOwner != null ? this.mDeviceOwner.name : null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public ComponentName getDeviceOwnerComponent() {
        ComponentName componentName;
        synchronized (this.mLock) {
            componentName = this.mDeviceOwner != null ? this.mDeviceOwner.admin : null;
        }
        return componentName;
    }

    /* access modifiers changed from: package-private */
    public String getDeviceOwnerRemoteBugreportUri() {
        String str;
        synchronized (this.mLock) {
            str = this.mDeviceOwner != null ? this.mDeviceOwner.remoteBugreportUri : null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public String getDeviceOwnerRemoteBugreportHash() {
        String str;
        synchronized (this.mLock) {
            str = this.mDeviceOwner != null ? this.mDeviceOwner.remoteBugreportHash : null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceOwner(ComponentName admin, String ownerName, int userId) {
        if (userId < 0) {
            Slog.e(TAG, "Invalid user id for device owner user: " + userId);
            return;
        }
        synchronized (this.mLock) {
            setDeviceOwnerWithRestrictionsMigrated(admin, ownerName, userId, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceOwnerWithRestrictionsMigrated(ComponentName admin, String ownerName, int userId, boolean userRestrictionsMigrated) {
        synchronized (this.mLock) {
            OwnerInfo ownerInfo = new OwnerInfo(ownerName, admin, userRestrictionsMigrated, (String) null, (String) null);
            this.mDeviceOwner = ownerInfo;
            this.mDeviceOwnerUserId = userId;
            this.mUserManagerInternal.setDeviceManaged(true);
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearDeviceOwner() {
        synchronized (this.mLock) {
            this.mDeviceOwner = null;
            this.mDeviceOwnerUserId = -10000;
            this.mUserManagerInternal.setDeviceManaged(false);
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void setProfileOwner(ComponentName admin, String ownerName, int userId) {
        synchronized (this.mLock) {
            ArrayMap<Integer, OwnerInfo> arrayMap = this.mProfileOwners;
            Integer valueOf = Integer.valueOf(userId);
            OwnerInfo ownerInfo = new OwnerInfo(ownerName, admin, true, (String) null, (String) null);
            arrayMap.put(valueOf, ownerInfo);
            this.mUserManagerInternal.setUserManaged(userId, true);
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeProfileOwner(int userId) {
        synchronized (this.mLock) {
            this.mProfileOwners.remove(Integer.valueOf(userId));
            this.mUserManagerInternal.setUserManaged(userId, false);
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void transferProfileOwner(ComponentName target, int userId) {
        synchronized (this.mLock) {
            OwnerInfo ownerInfo = this.mProfileOwners.get(Integer.valueOf(userId));
            OwnerInfo newOwnerInfo = new OwnerInfo(target.getPackageName(), target, ownerInfo.userRestrictionsMigrated, ownerInfo.remoteBugreportUri, ownerInfo.remoteBugreportHash);
            this.mProfileOwners.put(Integer.valueOf(userId), newOwnerInfo);
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void transferDeviceOwnership(ComponentName target) {
        synchronized (this.mLock) {
            OwnerInfo ownerInfo = new OwnerInfo((String) null, target, this.mDeviceOwner.userRestrictionsMigrated, this.mDeviceOwner.remoteBugreportUri, this.mDeviceOwner.remoteBugreportHash);
            this.mDeviceOwner = ownerInfo;
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public ComponentName getProfileOwnerComponent(int userId) {
        ComponentName componentName;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            componentName = profileOwner != null ? profileOwner.admin : null;
        }
        return componentName;
    }

    /* access modifiers changed from: package-private */
    public String getProfileOwnerName(int userId) {
        String str;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            str = profileOwner != null ? profileOwner.name : null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public String getProfileOwnerPackage(int userId) {
        String str;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            str = profileOwner != null ? profileOwner.packageName : null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> getProfileOwnerKeys() {
        Set<Integer> keySet;
        synchronized (this.mLock) {
            keySet = this.mProfileOwners.keySet();
        }
        return keySet;
    }

    /* access modifiers changed from: package-private */
    public SystemUpdatePolicy getSystemUpdatePolicy() {
        SystemUpdatePolicy systemUpdatePolicy;
        synchronized (this.mLock) {
            systemUpdatePolicy = this.mSystemUpdatePolicy;
        }
        return systemUpdatePolicy;
    }

    /* access modifiers changed from: package-private */
    public void setSystemUpdatePolicy(SystemUpdatePolicy systemUpdatePolicy) {
        synchronized (this.mLock) {
            this.mSystemUpdatePolicy = systemUpdatePolicy;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearSystemUpdatePolicy() {
        synchronized (this.mLock) {
            this.mSystemUpdatePolicy = null;
        }
    }

    /* access modifiers changed from: package-private */
    public Pair<LocalDate, LocalDate> getSystemUpdateFreezePeriodRecord() {
        Pair<LocalDate, LocalDate> pair;
        synchronized (this.mLock) {
            pair = new Pair<>(this.mSystemUpdateFreezeStart, this.mSystemUpdateFreezeEnd);
        }
        return pair;
    }

    /* access modifiers changed from: package-private */
    public String getSystemUpdateFreezePeriodRecordAsString() {
        StringBuilder freezePeriodRecord = new StringBuilder();
        freezePeriodRecord.append("start: ");
        if (this.mSystemUpdateFreezeStart != null) {
            freezePeriodRecord.append(this.mSystemUpdateFreezeStart.toString());
        } else {
            freezePeriodRecord.append("null");
        }
        freezePeriodRecord.append("; end: ");
        if (this.mSystemUpdateFreezeEnd != null) {
            freezePeriodRecord.append(this.mSystemUpdateFreezeEnd.toString());
        } else {
            freezePeriodRecord.append("null");
        }
        return freezePeriodRecord.toString();
    }

    /* access modifiers changed from: package-private */
    public boolean setSystemUpdateFreezePeriodRecord(LocalDate start, LocalDate end) {
        boolean changed = false;
        synchronized (this.mLock) {
            if (!Objects.equals(this.mSystemUpdateFreezeStart, start)) {
                this.mSystemUpdateFreezeStart = start;
                changed = true;
            }
            if (!Objects.equals(this.mSystemUpdateFreezeEnd, end)) {
                this.mSystemUpdateFreezeEnd = end;
                changed = true;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean hasDeviceOwner() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceOwner != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceOwnerUserId(int userId) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceOwner != null && this.mDeviceOwnerUserId == userId;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasProfileOwner(int userId) {
        boolean z;
        synchronized (this.mLock) {
            z = getProfileOwnerComponent(userId) != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean getDeviceOwnerUserRestrictionsNeedsMigration() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceOwner != null && !this.mDeviceOwner.userRestrictionsMigrated;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean getProfileOwnerUserRestrictionsNeedsMigration(int userId) {
        boolean z;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            z = profileOwner != null && !profileOwner.userRestrictionsMigrated;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceOwnerUserRestrictionsMigrated() {
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                this.mDeviceOwner.userRestrictionsMigrated = true;
            }
            writeDeviceOwner();
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceOwnerRemoteBugreportUriAndHash(String remoteBugreportUri, String remoteBugreportHash) {
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                this.mDeviceOwner.remoteBugreportUri = remoteBugreportUri;
                this.mDeviceOwner.remoteBugreportHash = remoteBugreportHash;
            }
            writeDeviceOwner();
        }
    }

    /* access modifiers changed from: package-private */
    public void setProfileOwnerUserRestrictionsMigrated(int userId) {
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            if (profileOwner != null) {
                profileOwner.userRestrictionsMigrated = true;
            }
            writeProfileOwner(userId);
        }
    }

    private boolean readLegacyOwnerFileLocked(File file) {
        if (!file.exists()) {
            return false;
        }
        try {
            try {
                InputStream input = new AtomicFile(file).openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(input, StandardCharsets.UTF_8.name());
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if (next == 1) {
                        input.close();
                        break;
                    } else if (type == 2) {
                        String tag = parser.getName();
                        if (tag.equals(TAG_DEVICE_OWNER)) {
                            OwnerInfo ownerInfo = new OwnerInfo(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "package"), false, (String) null, (String) null);
                            this.mDeviceOwner = ownerInfo;
                            this.mDeviceOwnerUserId = 0;
                        } else if (!tag.equals(TAG_DEVICE_INITIALIZER)) {
                            if (tag.equals(TAG_PROFILE_OWNER)) {
                                String profileOwnerPackageName = parser.getAttributeValue(null, "package");
                                String profileOwnerName = parser.getAttributeValue(null, "name");
                                String profileOwnerComponentStr = parser.getAttributeValue(null, ATTR_COMPONENT_NAME);
                                int userId = Integer.parseInt(parser.getAttributeValue(null, ATTR_USERID));
                                OwnerInfo profileOwnerInfo = null;
                                if (profileOwnerComponentStr != null) {
                                    ComponentName admin = ComponentName.unflattenFromString(profileOwnerComponentStr);
                                    if (admin != null) {
                                        OwnerInfo ownerInfo2 = new OwnerInfo(profileOwnerName, admin, false, (String) null, (String) null);
                                        profileOwnerInfo = ownerInfo2;
                                    } else {
                                        Slog.e(TAG, "Error parsing device-owner file. Bad component name " + profileOwnerComponentStr);
                                    }
                                }
                                OwnerInfo profileOwnerInfo2 = profileOwnerInfo;
                                if (profileOwnerInfo2 == null) {
                                    OwnerInfo ownerInfo3 = new OwnerInfo(profileOwnerName, profileOwnerPackageName, false, (String) null, (String) null);
                                    profileOwnerInfo2 = ownerInfo3;
                                }
                                this.mProfileOwners.put(Integer.valueOf(userId), profileOwnerInfo2);
                            } else if (TAG_SYSTEM_UPDATE_POLICY.equals(tag)) {
                                this.mSystemUpdatePolicy = SystemUpdatePolicy.restoreFromXml(parser);
                            } else {
                                throw new XmlPullParserException("Unexpected tag in device owner file: " + tag);
                            }
                        }
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e = e;
                Slog.e(TAG, "Error parsing device-owner file", e);
                return true;
            }
        } catch (IOException | XmlPullParserException e2) {
            e = e2;
            File file2 = file;
            Slog.e(TAG, "Error parsing device-owner file", e);
            return true;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void writeDeviceOwner() {
        synchronized (this.mLock) {
            new DeviceOwnerReadWriter().writeToFileLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeProfileOwner(int userId) {
        synchronized (this.mLock) {
            new ProfileOwnerReadWriter(userId).writeToFileLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean saveSystemUpdateInfo(SystemUpdateInfo newInfo) {
        synchronized (this.mLock) {
            if (Objects.equals(newInfo, this.mSystemUpdateInfo)) {
                return false;
            }
            this.mSystemUpdateInfo = newInfo;
            new DeviceOwnerReadWriter().writeToFileLocked();
            return true;
        }
    }

    public SystemUpdateInfo getSystemUpdateInfo() {
        SystemUpdateInfo systemUpdateInfo;
        synchronized (this.mLock) {
            systemUpdateInfo = this.mSystemUpdateInfo;
        }
        return systemUpdateInfo;
    }

    /* access modifiers changed from: package-private */
    public void pushToAppOpsLocked() {
        if (this.mSystemReady) {
            long ident = Binder.clearCallingIdentity();
            try {
                SparseIntArray owners = new SparseIntArray();
                if (this.mDeviceOwner != null) {
                    int uid = this.mPackageManagerInternal.getPackageUid(this.mDeviceOwner.packageName, 4333568, this.mDeviceOwnerUserId);
                    if (uid >= 0) {
                        owners.put(this.mDeviceOwnerUserId, uid);
                    }
                }
                if (this.mProfileOwners != null) {
                    for (int poi = this.mProfileOwners.size() - 1; poi >= 0; poi--) {
                        int uid2 = this.mPackageManagerInternal.getPackageUid(this.mProfileOwners.valueAt(poi).packageName, 4333568, this.mProfileOwners.keyAt(poi).intValue());
                        if (uid2 >= 0) {
                            owners.put(this.mProfileOwners.keyAt(poi).intValue(), uid2);
                        }
                    }
                }
                AppOpsManagerInternal appops = (AppOpsManagerInternal) LocalServices.getService(AppOpsManagerInternal.class);
                if (appops != null) {
                    appops.setDeviceAndProfileOwners(owners.size() > 0 ? owners : null);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void systemReady() {
        synchronized (this.mLock) {
            this.mSystemReady = true;
            pushToAppOpsLocked();
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        boolean needBlank = false;
        if (this.mDeviceOwner != null) {
            pw.println(prefix + "Device Owner: ");
            OwnerInfo ownerInfo = this.mDeviceOwner;
            ownerInfo.dump(prefix + "  ", pw);
            pw.println(prefix + "  User ID: " + this.mDeviceOwnerUserId);
            needBlank = true;
        }
        if (this.mSystemUpdatePolicy != null) {
            if (needBlank) {
                pw.println();
            }
            pw.println(prefix + "System Update Policy: " + this.mSystemUpdatePolicy);
            needBlank = true;
        }
        if (this.mProfileOwners != null) {
            for (Map.Entry<Integer, OwnerInfo> entry : this.mProfileOwners.entrySet()) {
                if (needBlank) {
                    pw.println();
                }
                pw.println(prefix + "Profile Owner (User " + entry.getKey() + "): ");
                StringBuilder sb = new StringBuilder();
                sb.append(prefix);
                sb.append("  ");
                entry.getValue().dump(sb.toString(), pw);
                needBlank = true;
            }
        }
        if (this.mSystemUpdateInfo != null) {
            if (needBlank) {
                pw.println();
            }
            pw.println(prefix + "Pending System Update: " + this.mSystemUpdateInfo);
            needBlank = true;
        }
        if (this.mSystemUpdateFreezeStart != null || this.mSystemUpdateFreezeEnd != null) {
            if (needBlank) {
                pw.println();
            }
            pw.println(prefix + "System update freeze record: " + getSystemUpdateFreezePeriodRecordAsString());
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public File getLegacyConfigFile() {
        return new File(this.mInjector.environmentGetDataSystemDirectory(), DEVICE_OWNER_XML_LEGACY);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public File getDeviceOwnerFile() {
        return new File(this.mInjector.environmentGetDataSystemDirectory(), DEVICE_OWNER_XML);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public File getProfileOwnerFile(int userId) {
        return new File(this.mInjector.environmentGetUserSystemDirectory(userId), PROFILE_OWNER_XML);
    }
}
