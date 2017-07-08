package com.android.server.devicepolicy;

import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class Owners {
    private static final String ATTR_COMPONENT_NAME = "component";
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
    private static final String TAG_PROFILE_OWNER = "profile-owner";
    private static final String TAG_ROOT = "root";
    private static final String TAG_SYSTEM_UPDATE_POLICY = "system-update-policy";
    private OwnerInfo mDeviceOwner;
    private int mDeviceOwnerUserId;
    private final Object mLock;
    private final PackageManagerInternal mPackageManagerInternal;
    private final ArrayMap<Integer, OwnerInfo> mProfileOwners;
    private SystemUpdatePolicy mSystemUpdatePolicy;
    private final UserManager mUserManager;
    private final UserManagerInternal mUserManagerInternal;

    private static abstract class FileReadWriter {
        private final File mFile;

        abstract boolean readInner(XmlPullParser xmlPullParser, int i, String str);

        abstract boolean shouldWrite();

        abstract void writeInner(XmlSerializer xmlSerializer) throws IOException;

        protected FileReadWriter(File file) {
            this.mFile = file;
        }

        void writeToFileLocked() {
            if (shouldWrite()) {
                AtomicFile f = new AtomicFile(this.mFile);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = f.startWrite();
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, Owners.TAG_ROOT);
                    writeInner(out);
                    out.endTag(null, Owners.TAG_ROOT);
                    out.endDocument();
                    out.flush();
                    f.finishWrite(fileOutputStream);
                } catch (IOException e) {
                    Slog.e(Owners.TAG, "Exception when writing", e);
                    if (fileOutputStream != null) {
                        f.failWrite(fileOutputStream);
                    }
                }
                return;
            }
            if (this.mFile.exists() && !this.mFile.delete()) {
                Slog.e(Owners.TAG, "Failed to remove " + this.mFile.getPath());
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void readFromFileLocked() {
            if (this.mFile.exists()) {
                AutoCloseable autoCloseable = null;
                try {
                    autoCloseable = new AtomicFile(this.mFile).openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(autoCloseable, StandardCharsets.UTF_8.name());
                    int depth = 0;
                    while (true) {
                        int type = parser.next();
                        if (type != 1) {
                            switch (type) {
                                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                                    depth++;
                                    String tag = parser.getName();
                                    if (depth != 1) {
                                        if (readInner(parser, depth, tag)) {
                                            break;
                                        }
                                        IoUtils.closeQuietly(autoCloseable);
                                        return;
                                    } else if (!Owners.TAG_ROOT.equals(tag)) {
                                        Slog.e(Owners.TAG, "Invalid root tag: " + tag);
                                        break;
                                    } else {
                                        continue;
                                    }
                                case H.REPORT_LOSING_FOCUS /*3*/:
                                    depth--;
                                    continue;
                                default:
                                    continue;
                            }
                            IoUtils.closeQuietly(autoCloseable);
                        } else {
                            IoUtils.closeQuietly(autoCloseable);
                            return;
                        }
                    }
                } catch (Exception e) {
                    Slog.e(Owners.TAG, "Error parsing device-owner file", e);
                } finally {
                }
            }
        }
    }

    private class DeviceOwnerReadWriter extends FileReadWriter {
        protected DeviceOwnerReadWriter() {
            super(Owners.this.getDeviceOwnerFileWithTestOverride());
        }

        boolean shouldWrite() {
            return (Owners.this.mDeviceOwner == null && Owners.this.mSystemUpdatePolicy == null) ? Owners.DEBUG : true;
        }

        void writeInner(XmlSerializer out) throws IOException {
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
        }

        boolean readInner(XmlPullParser parser, int depth, String tag) {
            if (depth > 2) {
                return true;
            }
            if (tag.equals(Owners.TAG_DEVICE_OWNER)) {
                Owners.this.mDeviceOwner = OwnerInfo.readFromXml(parser);
                Owners.this.mDeviceOwnerUserId = 0;
            } else if (tag.equals(Owners.TAG_DEVICE_OWNER_CONTEXT)) {
                String userIdString = parser.getAttributeValue(null, Owners.ATTR_USERID);
                try {
                    Owners.this.mDeviceOwnerUserId = Integer.parseInt(userIdString);
                } catch (NumberFormatException e) {
                    Slog.e(Owners.TAG, "Error parsing user-id " + userIdString);
                }
            } else if (!tag.equals(Owners.TAG_DEVICE_INITIALIZER)) {
                if (tag.equals(Owners.TAG_SYSTEM_UPDATE_POLICY)) {
                    Owners.this.mSystemUpdatePolicy = SystemUpdatePolicy.restoreFromXml(parser);
                } else {
                    Slog.e(Owners.TAG, "Unexpected tag: " + tag);
                    return Owners.DEBUG;
                }
            }
            return true;
        }
    }

    static class OwnerInfo {
        public final ComponentName admin;
        public final String name;
        public final String packageName;
        public String remoteBugreportHash;
        public String remoteBugreportUri;
        public boolean userRestrictionsMigrated;

        public OwnerInfo(String name, String packageName, boolean userRestrictionsMigrated, String remoteBugreportUri, String remoteBugreportHash) {
            this.name = name;
            this.packageName = packageName;
            this.admin = new ComponentName(packageName, "");
            this.userRestrictionsMigrated = userRestrictionsMigrated;
            this.remoteBugreportUri = remoteBugreportUri;
            this.remoteBugreportHash = remoteBugreportHash;
        }

        public OwnerInfo(String name, ComponentName admin, boolean userRestrictionsMigrated, String remoteBugreportUri, String remoteBugreportHash) {
            this.name = name;
            this.admin = admin;
            this.packageName = admin.getPackageName();
            this.userRestrictionsMigrated = userRestrictionsMigrated;
            this.remoteBugreportUri = remoteBugreportUri;
            this.remoteBugreportHash = remoteBugreportHash;
        }

        public void writeToXml(XmlSerializer out, String tag) throws IOException {
            out.startTag(null, tag);
            out.attribute(null, Owners.ATTR_PACKAGE, this.packageName);
            if (this.name != null) {
                out.attribute(null, Owners.ATTR_NAME, this.name);
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
            String packageName = parser.getAttributeValue(null, Owners.ATTR_PACKAGE);
            String name = parser.getAttributeValue(null, Owners.ATTR_NAME);
            String componentName = parser.getAttributeValue(null, Owners.ATTR_COMPONENT_NAME);
            boolean userRestrictionsMigrated = "true".equals(parser.getAttributeValue(null, Owners.ATTR_USER_RESTRICTIONS_MIGRATED));
            String remoteBugreportUri = parser.getAttributeValue(null, Owners.ATTR_REMOTE_BUGREPORT_URI);
            String remoteBugreportHash = parser.getAttributeValue(null, Owners.ATTR_REMOTE_BUGREPORT_HASH);
            if (componentName != null) {
                ComponentName admin = ComponentName.unflattenFromString(componentName);
                if (admin != null) {
                    return new OwnerInfo(name, admin, userRestrictionsMigrated, remoteBugreportUri, remoteBugreportHash);
                }
                Slog.e(Owners.TAG, "Error parsing owner file. Bad component name " + componentName);
            }
            return new OwnerInfo(name, packageName, userRestrictionsMigrated, remoteBugreportUri, remoteBugreportHash);
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
            super(Owners.this.getProfileOwnerFileWithTestOverride(userId));
            this.mUserId = userId;
        }

        boolean shouldWrite() {
            return Owners.this.mProfileOwners.get(Integer.valueOf(this.mUserId)) != null ? true : Owners.DEBUG;
        }

        void writeInner(XmlSerializer out) throws IOException {
            OwnerInfo profileOwner = (OwnerInfo) Owners.this.mProfileOwners.get(Integer.valueOf(this.mUserId));
            if (profileOwner != null) {
                profileOwner.writeToXml(out, Owners.TAG_PROFILE_OWNER);
            }
        }

        boolean readInner(XmlPullParser parser, int depth, String tag) {
            if (depth > 2) {
                return true;
            }
            if (tag.equals(Owners.TAG_PROFILE_OWNER)) {
                Owners.this.mProfileOwners.put(Integer.valueOf(this.mUserId), OwnerInfo.readFromXml(parser));
                return true;
            }
            Slog.e(Owners.TAG, "Unexpected tag: " + tag);
            return Owners.DEBUG;
        }
    }

    public Owners(UserManager userManager, UserManagerInternal userManagerInternal, PackageManagerInternal packageManagerInternal) {
        this.mDeviceOwnerUserId = -10000;
        this.mProfileOwners = new ArrayMap();
        this.mLock = new Object();
        this.mUserManager = userManager;
        this.mUserManagerInternal = userManagerInternal;
        this.mPackageManagerInternal = packageManagerInternal;
    }

    void load() {
        synchronized (this.mLock) {
            File legacy = getLegacyConfigFileWithTestOverride();
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
        }
    }

    private void pushToPackageManagerLocked() {
        String str;
        SparseArray<String> po = new SparseArray();
        for (int i = this.mProfileOwners.size() - 1; i >= 0; i--) {
            po.put(((Integer) this.mProfileOwners.keyAt(i)).intValue(), ((OwnerInfo) this.mProfileOwners.valueAt(i)).packageName);
        }
        PackageManagerInternal packageManagerInternal = this.mPackageManagerInternal;
        int i2 = this.mDeviceOwnerUserId;
        if (this.mDeviceOwner != null) {
            str = this.mDeviceOwner.packageName;
        } else {
            str = null;
        }
        packageManagerInternal.setDeviceAndProfileOwnerPackages(i2, str, po);
    }

    String getDeviceOwnerPackageName() {
        String str = null;
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                str = this.mDeviceOwner.packageName;
            }
        }
        return str;
    }

    int getDeviceOwnerUserId() {
        int i;
        synchronized (this.mLock) {
            i = this.mDeviceOwnerUserId;
        }
        return i;
    }

    Pair<Integer, ComponentName> getDeviceOwnerUserIdAndComponent() {
        synchronized (this.mLock) {
            if (this.mDeviceOwner == null) {
                return null;
            }
            Pair<Integer, ComponentName> create = Pair.create(Integer.valueOf(this.mDeviceOwnerUserId), this.mDeviceOwner.admin);
            return create;
        }
    }

    String getDeviceOwnerName() {
        String str = null;
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                str = this.mDeviceOwner.name;
            }
        }
        return str;
    }

    ComponentName getDeviceOwnerComponent() {
        ComponentName componentName = null;
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                componentName = this.mDeviceOwner.admin;
            }
        }
        return componentName;
    }

    String getDeviceOwnerRemoteBugreportUri() {
        String str = null;
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                str = this.mDeviceOwner.remoteBugreportUri;
            }
        }
        return str;
    }

    String getDeviceOwnerRemoteBugreportHash() {
        String str = null;
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                str = this.mDeviceOwner.remoteBugreportHash;
            }
        }
        return str;
    }

    void setDeviceOwner(ComponentName admin, String ownerName, int userId) {
        if (userId < 0) {
            Slog.e(TAG, "Invalid user id for device owner user: " + userId);
            return;
        }
        synchronized (this.mLock) {
            setDeviceOwnerWithRestrictionsMigrated(admin, ownerName, userId, true);
        }
    }

    void setDeviceOwnerWithRestrictionsMigrated(ComponentName admin, String ownerName, int userId, boolean userRestrictionsMigrated) {
        synchronized (this.mLock) {
            this.mDeviceOwner = new OwnerInfo(ownerName, admin, userRestrictionsMigrated, null, null);
            this.mDeviceOwnerUserId = userId;
            this.mUserManagerInternal.setDeviceManaged(true);
            pushToPackageManagerLocked();
        }
    }

    void clearDeviceOwner() {
        synchronized (this.mLock) {
            this.mDeviceOwner = null;
            this.mDeviceOwnerUserId = -10000;
            this.mUserManagerInternal.setDeviceManaged(DEBUG);
            pushToPackageManagerLocked();
        }
    }

    void setProfileOwner(ComponentName admin, String ownerName, int userId) {
        synchronized (this.mLock) {
            this.mProfileOwners.put(Integer.valueOf(userId), new OwnerInfo(ownerName, admin, true, null, null));
            this.mUserManagerInternal.setUserManaged(userId, true);
            pushToPackageManagerLocked();
        }
    }

    void removeProfileOwner(int userId) {
        synchronized (this.mLock) {
            this.mProfileOwners.remove(Integer.valueOf(userId));
            this.mUserManagerInternal.setUserManaged(userId, DEBUG);
            pushToPackageManagerLocked();
        }
    }

    ComponentName getProfileOwnerComponent(int userId) {
        ComponentName componentName = null;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = (OwnerInfo) this.mProfileOwners.get(Integer.valueOf(userId));
            if (profileOwner != null) {
                componentName = profileOwner.admin;
            }
        }
        return componentName;
    }

    String getProfileOwnerName(int userId) {
        String str = null;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = (OwnerInfo) this.mProfileOwners.get(Integer.valueOf(userId));
            if (profileOwner != null) {
                str = profileOwner.name;
            }
        }
        return str;
    }

    String getProfileOwnerPackage(int userId) {
        String str = null;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = (OwnerInfo) this.mProfileOwners.get(Integer.valueOf(userId));
            if (profileOwner != null) {
                str = profileOwner.packageName;
            }
        }
        return str;
    }

    Set<Integer> getProfileOwnerKeys() {
        Set<Integer> keySet;
        synchronized (this.mLock) {
            keySet = this.mProfileOwners.keySet();
        }
        return keySet;
    }

    SystemUpdatePolicy getSystemUpdatePolicy() {
        SystemUpdatePolicy systemUpdatePolicy;
        synchronized (this.mLock) {
            systemUpdatePolicy = this.mSystemUpdatePolicy;
        }
        return systemUpdatePolicy;
    }

    void setSystemUpdatePolicy(SystemUpdatePolicy systemUpdatePolicy) {
        synchronized (this.mLock) {
            this.mSystemUpdatePolicy = systemUpdatePolicy;
        }
    }

    void clearSystemUpdatePolicy() {
        synchronized (this.mLock) {
            this.mSystemUpdatePolicy = null;
        }
    }

    boolean hasDeviceOwner() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceOwner != null ? true : DEBUG;
        }
        return z;
    }

    boolean isDeviceOwnerUserId(int userId) {
        boolean z = DEBUG;
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null && this.mDeviceOwnerUserId == userId) {
                z = true;
            }
        }
        return z;
    }

    boolean hasProfileOwner(int userId) {
        boolean z;
        synchronized (this.mLock) {
            z = getProfileOwnerComponent(userId) != null ? true : DEBUG;
        }
        return z;
    }

    boolean getDeviceOwnerUserRestrictionsNeedsMigration() {
        boolean z = DEBUG;
        synchronized (this.mLock) {
            if (!(this.mDeviceOwner == null || this.mDeviceOwner.userRestrictionsMigrated)) {
                z = true;
            }
        }
        return z;
    }

    boolean getProfileOwnerUserRestrictionsNeedsMigration(int userId) {
        boolean z = DEBUG;
        synchronized (this.mLock) {
            OwnerInfo profileOwner = (OwnerInfo) this.mProfileOwners.get(Integer.valueOf(userId));
            if (!(profileOwner == null || profileOwner.userRestrictionsMigrated)) {
                z = true;
            }
        }
        return z;
    }

    void setDeviceOwnerUserRestrictionsMigrated() {
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                this.mDeviceOwner.userRestrictionsMigrated = true;
            }
            writeDeviceOwner();
        }
    }

    void setDeviceOwnerRemoteBugreportUriAndHash(String remoteBugreportUri, String remoteBugreportHash) {
        synchronized (this.mLock) {
            if (this.mDeviceOwner != null) {
                this.mDeviceOwner.remoteBugreportUri = remoteBugreportUri;
                this.mDeviceOwner.remoteBugreportHash = remoteBugreportHash;
            }
            writeDeviceOwner();
        }
    }

    void setProfileOwnerUserRestrictionsMigrated(int userId) {
        synchronized (this.mLock) {
            OwnerInfo profileOwner = (OwnerInfo) this.mProfileOwners.get(Integer.valueOf(userId));
            if (profileOwner != null) {
                profileOwner.userRestrictionsMigrated = true;
            }
            writeProfileOwner(userId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readLegacyOwnerFileLocked(File file) {
        if (!file.exists()) {
            return DEBUG;
        }
        try {
            String tag;
            InputStream input = new AtomicFile(file).openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, StandardCharsets.UTF_8.name());
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    break;
                } else if (type == 2) {
                    tag = parser.getName();
                    if (tag.equals(TAG_DEVICE_OWNER)) {
                        this.mDeviceOwner = new OwnerInfo(parser.getAttributeValue(null, ATTR_NAME), parser.getAttributeValue(null, ATTR_PACKAGE), (boolean) DEBUG, null, null);
                        this.mDeviceOwnerUserId = 0;
                    } else {
                        if (!tag.equals(TAG_DEVICE_INITIALIZER)) {
                            if (!tag.equals(TAG_PROFILE_OWNER)) {
                                if (!TAG_SYSTEM_UPDATE_POLICY.equals(tag)) {
                                    break;
                                }
                                this.mSystemUpdatePolicy = SystemUpdatePolicy.restoreFromXml(parser);
                            } else {
                                String profileOwnerPackageName = parser.getAttributeValue(null, ATTR_PACKAGE);
                                String profileOwnerName = parser.getAttributeValue(null, ATTR_NAME);
                                String profileOwnerComponentStr = parser.getAttributeValue(null, ATTR_COMPONENT_NAME);
                                int userId = Integer.parseInt(parser.getAttributeValue(null, ATTR_USERID));
                                Object obj = null;
                                if (profileOwnerComponentStr != null) {
                                    ComponentName admin = ComponentName.unflattenFromString(profileOwnerComponentStr);
                                    if (admin != null) {
                                        obj = new OwnerInfo(profileOwnerName, admin, (boolean) DEBUG, null, null);
                                    } else {
                                        Slog.e(TAG, "Error parsing device-owner file. Bad component name " + profileOwnerComponentStr);
                                    }
                                }
                                if (obj == null) {
                                    OwnerInfo ownerInfo = new OwnerInfo(profileOwnerName, profileOwnerPackageName, (boolean) DEBUG, null, null);
                                }
                                this.mProfileOwners.put(Integer.valueOf(userId), obj);
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            throw new XmlPullParserException("Unexpected tag in device owner file: " + tag);
        } catch (Exception e) {
            Slog.e(TAG, "Error parsing device-owner file", e);
        }
    }

    void writeDeviceOwner() {
        synchronized (this.mLock) {
            new DeviceOwnerReadWriter().writeToFileLocked();
        }
    }

    void writeProfileOwner(int userId) {
        synchronized (this.mLock) {
            new ProfileOwnerReadWriter(userId).writeToFileLocked();
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        boolean needBlank = DEBUG;
        if (this.mDeviceOwner != null) {
            pw.println(prefix + "Device Owner: ");
            this.mDeviceOwner.dump(prefix + "  ", pw);
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
            for (Entry<Integer, OwnerInfo> entry : this.mProfileOwners.entrySet()) {
                if (needBlank) {
                    pw.println();
                }
                pw.println(prefix + "Profile Owner (User " + entry.getKey() + "): ");
                ((OwnerInfo) entry.getValue()).dump(prefix + "  ", pw);
                needBlank = true;
            }
        }
    }

    File getLegacyConfigFileWithTestOverride() {
        return new File(Environment.getDataSystemDirectory(), DEVICE_OWNER_XML_LEGACY);
    }

    File getDeviceOwnerFileWithTestOverride() {
        return new File(Environment.getDataSystemDirectory(), DEVICE_OWNER_XML);
    }

    File getProfileOwnerFileWithTestOverride(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), PROFILE_OWNER_XML);
    }
}
