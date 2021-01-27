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
import com.android.server.wm.ActivityTaskManagerInternal;
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

/* access modifiers changed from: package-private */
public class Owners {
    private static final String ATTR_CAN_ACCESS_DEVICE_IDS = "canAccessDeviceIds";
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
    private final ActivityTaskManagerInternal mActivityTaskManagerInternal;
    private OwnerInfo mDeviceOwner;
    private int mDeviceOwnerUserId;
    private final Injector mInjector;
    private final Object mLock;
    private final PackageManagerInternal mPackageManagerInternal;
    private final ArrayMap<Integer, OwnerInfo> mProfileOwners;
    private boolean mSystemReady;
    private LocalDate mSystemUpdateFreezeEnd;
    private LocalDate mSystemUpdateFreezeStart;
    private SystemUpdateInfo mSystemUpdateInfo;
    private SystemUpdatePolicy mSystemUpdatePolicy;
    private final UserManager mUserManager;
    private final UserManagerInternal mUserManagerInternal;

    public Owners(UserManager userManager, UserManagerInternal userManagerInternal, PackageManagerInternal packageManagerInternal, ActivityTaskManagerInternal activityTaskManagerInternal) {
        this(userManager, userManagerInternal, packageManagerInternal, activityTaskManagerInternal, new Injector());
    }

    @VisibleForTesting
    Owners(UserManager userManager, UserManagerInternal userManagerInternal, PackageManagerInternal packageManagerInternal, ActivityTaskManagerInternal activityTaskManagerInternal, Injector injector) {
        this.mDeviceOwnerUserId = -10000;
        this.mProfileOwners = new ArrayMap<>();
        this.mLock = new Object();
        this.mUserManager = userManager;
        this.mUserManagerInternal = userManagerInternal;
        this.mPackageManagerInternal = packageManagerInternal;
        this.mActivityTaskManagerInternal = activityTaskManagerInternal;
        this.mInjector = injector;
    }

    /* access modifiers changed from: package-private */
    public void load() {
        synchronized (this.mLock) {
            File legacy = getLegacyConfigFile();
            List<UserInfo> users = this.mUserManager.getUsers(true);
            if (readLegacyOwnerFileLocked(legacy)) {
                writeDeviceOwner();
                for (Integer num : getProfileOwnerKeys()) {
                    writeProfileOwner(num.intValue());
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
                Slog.w(TAG, String.format("User %d has both DO and PO, which is not supported", Integer.valueOf(getDeviceOwnerUserId())));
            }
            pushToPackageManagerLocked();
            pushToActivityTaskManagerLocked();
            pushToAppOpsLocked();
        }
    }

    private void pushToPackageManagerLocked() {
        SparseArray<String> po = new SparseArray<>();
        for (int i = this.mProfileOwners.size() - 1; i >= 0; i--) {
            po.put(this.mProfileOwners.keyAt(i).intValue(), this.mProfileOwners.valueAt(i).packageName);
        }
        PackageManagerInternal packageManagerInternal = this.mPackageManagerInternal;
        int i2 = this.mDeviceOwnerUserId;
        OwnerInfo ownerInfo = this.mDeviceOwner;
        packageManagerInternal.setDeviceAndProfileOwnerPackages(i2, ownerInfo != null ? ownerInfo.packageName : null, po);
    }

    private void pushToActivityTaskManagerLocked() {
        int uid;
        OwnerInfo ownerInfo = this.mDeviceOwner;
        if (ownerInfo != null) {
            uid = this.mPackageManagerInternal.getPackageUid(ownerInfo.packageName, 4333568, this.mDeviceOwnerUserId);
        } else {
            uid = -1;
        }
        this.mActivityTaskManagerInternal.setDeviceOwnerUid(uid);
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
            return Pair.create(Integer.valueOf(this.mDeviceOwnerUserId), this.mDeviceOwner.admin);
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
            this.mDeviceOwner = new OwnerInfo(ownerName, admin, userRestrictionsMigrated, (String) null, (String) null, true);
            this.mDeviceOwnerUserId = userId;
            this.mUserManagerInternal.setDeviceManaged(true);
            pushToPackageManagerLocked();
            pushToActivityTaskManagerLocked();
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
            pushToActivityTaskManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void setProfileOwner(ComponentName admin, String ownerName, int userId) {
        synchronized (this.mLock) {
            this.mProfileOwners.put(Integer.valueOf(userId), new OwnerInfo(ownerName, admin, true, (String) null, (String) null, false));
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
            this.mProfileOwners.put(Integer.valueOf(userId), new OwnerInfo(target.getPackageName(), target, ownerInfo.userRestrictionsMigrated, ownerInfo.remoteBugreportUri, ownerInfo.remoteBugreportHash, ownerInfo.canAccessDeviceIds));
            pushToPackageManagerLocked();
            pushToAppOpsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void transferDeviceOwnership(ComponentName target) {
        synchronized (this.mLock) {
            this.mDeviceOwner = new OwnerInfo((String) null, target, this.mDeviceOwner.userRestrictionsMigrated, this.mDeviceOwner.remoteBugreportUri, this.mDeviceOwner.remoteBugreportHash, this.mDeviceOwner.canAccessDeviceIds);
            pushToPackageManagerLocked();
            pushToActivityTaskManagerLocked();
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
    public boolean canProfileOwnerAccessDeviceIds(int userId) {
        boolean z;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            z = profileOwner != null ? profileOwner.canAccessDeviceIds : false;
        }
        return z;
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
        LocalDate localDate = this.mSystemUpdateFreezeStart;
        if (localDate != null) {
            freezePeriodRecord.append(localDate.toString());
        } else {
            freezePeriodRecord.append("null");
        }
        freezePeriodRecord.append("; end: ");
        LocalDate localDate2 = this.mSystemUpdateFreezeEnd;
        if (localDate2 != null) {
            freezePeriodRecord.append(localDate2.toString());
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

    /* access modifiers changed from: package-private */
    public void setProfileOwnerCanAccessDeviceIds(int userId) {
        synchronized (this.mLock) {
            OwnerInfo profileOwner = this.mProfileOwners.get(Integer.valueOf(userId));
            if (profileOwner != null) {
                profileOwner.canAccessDeviceIds = true;
            } else {
                Slog.e(TAG, String.format("Cannot grant Device IDs access for user %d, no profile owner.", Integer.valueOf(userId)));
            }
            writeProfileOwner(userId);
        }
    }

    private boolean readLegacyOwnerFileLocked(File file) {
        Exception e;
        if (!file.exists()) {
            return false;
        }
        try {
            try {
                InputStream input = new AtomicFile(file).openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(input, StandardCharsets.UTF_8.name());
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        input.close();
                        break;
                    } else if (type == 2) {
                        String tag = parser.getName();
                        if (tag.equals(TAG_DEVICE_OWNER)) {
                            try {
                                this.mDeviceOwner = new OwnerInfo(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "package"), false, (String) null, (String) null, true);
                                this.mDeviceOwnerUserId = 0;
                            } catch (IOException | XmlPullParserException e2) {
                                e = e2;
                                Slog.e(TAG, "Error parsing device-owner file", e);
                                return true;
                            }
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
                                        profileOwnerInfo = new OwnerInfo(profileOwnerName, admin, false, (String) null, (String) null, false);
                                    } else {
                                        Slog.e(TAG, "Error parsing device-owner file. Bad component name " + profileOwnerComponentStr);
                                    }
                                }
                                if (profileOwnerInfo == null) {
                                    profileOwnerInfo = new OwnerInfo(profileOwnerName, profileOwnerPackageName, false, (String) null, (String) null, false);
                                }
                                this.mProfileOwners.put(Integer.valueOf(userId), profileOwnerInfo);
                            } else if (TAG_SYSTEM_UPDATE_POLICY.equals(tag)) {
                                this.mSystemUpdatePolicy = SystemUpdatePolicy.restoreFromXml(parser);
                            } else {
                                throw new XmlPullParserException("Unexpected tag in device owner file: " + tag);
                            }
                        }
                    }
                }
            } catch (NumberFormatException e3) {
                Slog.e(TAG, "NumberFormatException userId");
                return true;
            }
        } catch (IOException | XmlPullParserException e4) {
            e = e4;
            Slog.e(TAG, "Error parsing device-owner file", e);
            return true;
        } catch (NumberFormatException e5) {
            Slog.e(TAG, "NumberFormatException userId");
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
        int uid;
        if (this.mSystemReady) {
            long ident = Binder.clearCallingIdentity();
            try {
                SparseIntArray owners = new SparseIntArray();
                if (this.mDeviceOwner != null && (uid = this.mPackageManagerInternal.getPackageUid(this.mDeviceOwner.packageName, 4333568, this.mDeviceOwnerUserId)) >= 0) {
                    owners.put(this.mDeviceOwnerUserId, uid);
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
            if (shouldWrite()) {
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
                }
            } else if (this.mFile.exists() && !this.mFile.delete()) {
                Slog.e(Owners.TAG, "Failed to remove " + this.mFile.getPath());
            }
        }

        /* access modifiers changed from: package-private */
        public void readFromFileLocked() {
            if (this.mFile.exists()) {
                InputStream input = new AtomicFile(this.mFile).openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(input, StandardCharsets.UTF_8.name());
                int depth = 0;
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        break;
                    } else if (type == 2) {
                        depth++;
                        String tag = parser.getName();
                        if (depth != 1) {
                            try {
                                if (!readInner(parser, depth, tag)) {
                                    IoUtils.closeQuietly(input);
                                    return;
                                }
                            } catch (IOException | XmlPullParserException e) {
                                Slog.e(Owners.TAG, "Error parsing owners information file", e);
                            } catch (Throwable th) {
                                IoUtils.closeQuietly(input);
                                throw th;
                            }
                        } else if (!Owners.TAG_ROOT.equals(tag)) {
                            Slog.e(Owners.TAG, "Invalid root tag: " + tag);
                            IoUtils.closeQuietly(input);
                            return;
                        }
                    } else if (type == 3) {
                        depth--;
                    }
                }
                IoUtils.closeQuietly(input);
            }
        }
    }

    /* access modifiers changed from: private */
    public class DeviceOwnerReadWriter extends FileReadWriter {
        protected DeviceOwnerReadWriter() {
            super(Owners.this.getDeviceOwnerFile());
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.devicepolicy.Owners.FileReadWriter
        public boolean shouldWrite() {
            return (Owners.this.mDeviceOwner == null && Owners.this.mSystemUpdatePolicy == null && Owners.this.mSystemUpdateInfo == null) ? false : true;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.devicepolicy.Owners.FileReadWriter
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
        @Override // com.android.server.devicepolicy.Owners.FileReadWriter
        public boolean readInner(XmlPullParser parser, int depth, String tag) {
            if (depth > 2) {
                return true;
            }
            char c = 65535;
            switch (tag.hashCode()) {
                case -2101756875:
                    if (tag.equals(Owners.TAG_PENDING_OTA_INFO)) {
                        c = 4;
                        break;
                    }
                    break;
                case -2038823445:
                    if (tag.equals(Owners.TAG_DEVICE_INITIALIZER)) {
                        c = 2;
                        break;
                    }
                    break;
                case -2020438916:
                    if (tag.equals(Owners.TAG_DEVICE_OWNER)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1900517026:
                    if (tag.equals(Owners.TAG_DEVICE_OWNER_CONTEXT)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1303827527:
                    if (tag.equals(Owners.TAG_FREEZE_PERIOD_RECORD)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1748301720:
                    if (tag.equals(Owners.TAG_SYSTEM_UPDATE_POLICY)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                Owners.this.mDeviceOwner = OwnerInfo.readFromXml(parser);
                Owners.this.mDeviceOwnerUserId = 0;
            } else if (c == 1) {
                String userIdString = parser.getAttributeValue(null, Owners.ATTR_USERID);
                try {
                    Owners.this.mDeviceOwnerUserId = Integer.parseInt(userIdString);
                } catch (NumberFormatException e) {
                    Slog.e(Owners.TAG, "Error parsing user-id " + userIdString);
                }
            } else if (c != 2) {
                if (c == 3) {
                    Owners.this.mSystemUpdatePolicy = SystemUpdatePolicy.restoreFromXml(parser);
                } else if (c == 4) {
                    Owners.this.mSystemUpdateInfo = SystemUpdateInfo.readFromXml(parser);
                } else if (c != 5) {
                    Slog.e(Owners.TAG, "Unexpected tag: " + tag);
                    return false;
                } else {
                    String startDate = parser.getAttributeValue(null, Owners.ATTR_FREEZE_RECORD_START);
                    String endDate = parser.getAttributeValue(null, Owners.ATTR_FREEZE_RECORD_END);
                    if (!(startDate == null || endDate == null)) {
                        Owners.this.mSystemUpdateFreezeStart = LocalDate.parse(startDate);
                        Owners.this.mSystemUpdateFreezeEnd = LocalDate.parse(endDate);
                        if (Owners.this.mSystemUpdateFreezeStart.isAfter(Owners.this.mSystemUpdateFreezeEnd)) {
                            Slog.e(Owners.TAG, "Invalid system update freeze record loaded");
                            Owners.this.mSystemUpdateFreezeStart = null;
                            Owners.this.mSystemUpdateFreezeEnd = null;
                        }
                    }
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class ProfileOwnerReadWriter extends FileReadWriter {
        private final int mUserId;

        ProfileOwnerReadWriter(int userId) {
            super(Owners.this.getProfileOwnerFile(userId));
            this.mUserId = userId;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.devicepolicy.Owners.FileReadWriter
        public boolean shouldWrite() {
            return Owners.this.mProfileOwners.get(Integer.valueOf(this.mUserId)) != null;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.devicepolicy.Owners.FileReadWriter
        public void writeInner(XmlSerializer out) throws IOException {
            OwnerInfo profileOwner = (OwnerInfo) Owners.this.mProfileOwners.get(Integer.valueOf(this.mUserId));
            if (profileOwner != null) {
                profileOwner.writeToXml(out, Owners.TAG_PROFILE_OWNER);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.devicepolicy.Owners.FileReadWriter
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

    /* access modifiers changed from: package-private */
    public static class OwnerInfo {
        public final ComponentName admin;
        public boolean canAccessDeviceIds;
        public final String name;
        public final String packageName;
        public String remoteBugreportHash;
        public String remoteBugreportUri;
        public boolean userRestrictionsMigrated;

        public OwnerInfo(String name2, String packageName2, boolean userRestrictionsMigrated2, String remoteBugreportUri2, String remoteBugreportHash2, boolean canAccessDeviceIds2) {
            this.name = name2;
            this.packageName = packageName2;
            this.admin = new ComponentName(packageName2, "");
            this.userRestrictionsMigrated = userRestrictionsMigrated2;
            this.remoteBugreportUri = remoteBugreportUri2;
            this.remoteBugreportHash = remoteBugreportHash2;
            this.canAccessDeviceIds = canAccessDeviceIds2;
        }

        public OwnerInfo(String name2, ComponentName admin2, boolean userRestrictionsMigrated2, String remoteBugreportUri2, String remoteBugreportHash2, boolean canAccessDeviceIds2) {
            this.name = name2;
            this.admin = admin2;
            this.packageName = admin2.getPackageName();
            this.userRestrictionsMigrated = userRestrictionsMigrated2;
            this.remoteBugreportUri = remoteBugreportUri2;
            this.remoteBugreportHash = remoteBugreportHash2;
            this.canAccessDeviceIds = canAccessDeviceIds2;
        }

        public void writeToXml(XmlSerializer out, String tag) throws IOException {
            out.startTag(null, tag);
            out.attribute(null, "package", this.packageName);
            String str = this.name;
            if (str != null) {
                out.attribute(null, "name", str);
            }
            ComponentName componentName = this.admin;
            if (componentName != null) {
                out.attribute(null, Owners.ATTR_COMPONENT_NAME, componentName.flattenToString());
            }
            out.attribute(null, Owners.ATTR_USER_RESTRICTIONS_MIGRATED, String.valueOf(this.userRestrictionsMigrated));
            String str2 = this.remoteBugreportUri;
            if (str2 != null) {
                out.attribute(null, Owners.ATTR_REMOTE_BUGREPORT_URI, str2);
            }
            String str3 = this.remoteBugreportHash;
            if (str3 != null) {
                out.attribute(null, Owners.ATTR_REMOTE_BUGREPORT_HASH, str3);
            }
            boolean z = this.canAccessDeviceIds;
            if (z) {
                out.attribute(null, Owners.ATTR_CAN_ACCESS_DEVICE_IDS, String.valueOf(z));
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
            boolean canAccessDeviceIds2 = "true".equals(parser.getAttributeValue(null, Owners.ATTR_CAN_ACCESS_DEVICE_IDS));
            if (componentName != null) {
                ComponentName admin2 = ComponentName.unflattenFromString(componentName);
                if (admin2 != null) {
                    return new OwnerInfo(name2, admin2, userRestrictionsMigrated2, remoteBugreportUri2, remoteBugreportHash2, canAccessDeviceIds2);
                }
                Slog.e(Owners.TAG, "Error parsing owner file. Bad component name " + componentName);
            }
            return new OwnerInfo(name2, packageName2, userRestrictionsMigrated2, remoteBugreportUri2, remoteBugreportHash2, canAccessDeviceIds2);
        }

        public void dump(String prefix, PrintWriter pw) {
            pw.println(prefix + "admin=" + this.admin);
            pw.println(prefix + "name=" + this.name);
            pw.println(prefix + "package=" + this.packageName);
            pw.println(prefix + "canAccessDeviceIds=" + this.canAccessDeviceIds);
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
        ArrayMap<Integer, OwnerInfo> arrayMap = this.mProfileOwners;
        if (arrayMap != null) {
            for (Map.Entry<Integer, OwnerInfo> entry : arrayMap.entrySet()) {
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
        if (!(this.mSystemUpdateFreezeStart == null && this.mSystemUpdateFreezeEnd == null)) {
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
}
