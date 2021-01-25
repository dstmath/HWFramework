package com.android.server.role;

import android.os.Environment;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.pm.HwPackageManagerServiceUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class RoleUserState {
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_PACKAGES_HASH = "packagesHash";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String LOG_TAG = RoleUserState.class.getSimpleName();
    private static final String ROLES_FILE_NAME = "roles.xml";
    private static final String TAG_HOLDER = "holder";
    private static final String TAG_ROLE = "role";
    private static final String TAG_ROLES = "roles";
    public static final int VERSION_UNDEFINED = -1;
    private static final long WRITE_DELAY_MILLIS = 200;
    private final Callback mCallback;
    @GuardedBy({"mLock"})
    private boolean mDestroyed;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private String mPackagesHash;
    @GuardedBy({"mLock"})
    private ArrayMap<String, ArraySet<String>> mRoles = new ArrayMap<>();
    private final int mUserId;
    @GuardedBy({"mLock"})
    private int mVersion = -1;
    private final Handler mWriteHandler = new Handler(BackgroundThread.getHandler().getLooper());
    @GuardedBy({"mLock"})
    private boolean mWriteScheduled;

    public interface Callback {
        void onRoleHoldersChanged(String str, int i, String str2, String str3);
    }

    public RoleUserState(int userId, Callback callback) {
        this.mUserId = userId;
        this.mCallback = callback;
        readFile();
    }

    public int getVersion() {
        int i;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            i = this.mVersion;
        }
        return i;
    }

    public void setVersion(int version) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mVersion != version) {
                this.mVersion = version;
                scheduleWriteFileLocked();
            }
        }
    }

    public String getPackagesHash() {
        String str;
        synchronized (this.mLock) {
            str = this.mPackagesHash;
        }
        return str;
    }

    public void setPackagesHash(String packagesHash) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (!Objects.equals(this.mPackagesHash, packagesHash)) {
                this.mPackagesHash = packagesHash;
                scheduleWriteFileLocked();
            }
        }
    }

    public boolean isRoleAvailable(String roleName) {
        boolean containsKey;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            containsKey = this.mRoles.containsKey(roleName);
        }
        return containsKey;
    }

    public ArraySet<String> getRoleHolders(String roleName) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            ArraySet<String> packageNames = this.mRoles.get(roleName);
            if (packageNames == null) {
                return null;
            }
            return new ArraySet<>(packageNames);
        }
    }

    public boolean addRoleName(String roleName) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            if (this.mRoles.containsKey(roleName)) {
                return false;
            }
            this.mRoles.put(roleName, new ArraySet<>());
            String str = LOG_TAG;
            Slog.i(str, "Added new role: " + roleName);
            scheduleWriteFileLocked();
            return true;
        }
    }

    public void setRoleNames(List<String> roleNames) {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            boolean changed = false;
            for (int i = this.mRoles.size() - 1; i >= 0; i--) {
                String roleName = this.mRoles.keyAt(i);
                if (!roleNames.contains(roleName)) {
                    ArraySet<String> packageNames = this.mRoles.valueAt(i);
                    if (!packageNames.isEmpty()) {
                        Slog.e(LOG_TAG, "Holders of a removed role should have been cleaned up, role: " + roleName + ", holders: " + packageNames);
                    }
                    this.mRoles.removeAt(i);
                    changed = true;
                }
            }
            int roleNamesSize = roleNames.size();
            for (int i2 = 0; i2 < roleNamesSize; i2++) {
                changed |= addRoleName(roleNames.get(i2));
            }
            if (changed) {
                scheduleWriteFileLocked();
            }
        }
    }

    public boolean addRoleHolder(String roleName, String packageName) {
        boolean changed;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            ArraySet<String> roleHolders = this.mRoles.get(roleName);
            if (roleHolders == null) {
                String str = LOG_TAG;
                Slog.e(str, "Cannot add role holder for unknown role, role: " + roleName + ", package: " + packageName);
                return false;
            }
            changed = roleHolders.add(packageName);
            if (changed) {
                scheduleWriteFileLocked();
            }
        }
        if (!changed) {
            return true;
        }
        this.mCallback.onRoleHoldersChanged(roleName, this.mUserId, null, packageName);
        return true;
    }

    public boolean removeRoleHolder(String roleName, String packageName) {
        boolean changed;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            ArraySet<String> roleHolders = this.mRoles.get(roleName);
            if (roleHolders == null) {
                String str = LOG_TAG;
                Slog.e(str, "Cannot remove role holder for unknown role, role: " + roleName + ", package: " + packageName);
                return false;
            }
            changed = roleHolders.remove(packageName);
            if (changed) {
                scheduleWriteFileLocked();
            }
        }
        if (!changed) {
            return true;
        }
        this.mCallback.onRoleHoldersChanged(roleName, this.mUserId, packageName, null);
        return true;
    }

    public List<String> getHeldRoles(String packageName) {
        ArrayList<String> result = new ArrayList<>();
        int size = this.mRoles.size();
        for (int i = 0; i < size; i++) {
            if (this.mRoles.valueAt(i).contains(packageName)) {
                result.add(this.mRoles.keyAt(i));
            }
        }
        return result;
    }

    @GuardedBy({"mLock"})
    private void scheduleWriteFileLocked() {
        throwIfDestroyedLocked();
        if (!this.mWriteScheduled) {
            this.mWriteHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$RoleUserState$e8W_Zaq_FyocW_DX1qcbN0ld0co.INSTANCE, this), WRITE_DELAY_MILLIS);
            this.mWriteScheduled = true;
        }
    }

    /* access modifiers changed from: private */
    public void writeFile() {
        int version;
        String packagesHash;
        ArrayMap<String, ArraySet<String>> roles;
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                this.mWriteScheduled = false;
                version = this.mVersion;
                packagesHash = this.mPackagesHash;
                roles = snapshotRolesLocked();
            } else {
                return;
            }
        }
        File file = getFile(this.mUserId);
        AtomicFile atomicFile = new AtomicFile(file, "roles-" + this.mUserId);
        FileOutputStream out = null;
        try {
            out = atomicFile.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, StandardCharsets.UTF_8.name());
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializeRoles(serializer, version, packagesHash, roles);
            serializer.endDocument();
            atomicFile.finishWrite(out);
            Slog.i(LOG_TAG, "Wrote roles.xml successfully");
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            Slog.wtf(LOG_TAG, "Failed to write roles.xml, restoring backup", e);
            if (out != null) {
                atomicFile.failWrite(out);
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly(out);
            throw th;
        }
        IoUtils.closeQuietly(out);
    }

    private void serializeRoles(XmlSerializer serializer, int version, String packagesHash, ArrayMap<String, ArraySet<String>> roles) throws IOException {
        serializer.startTag(null, TAG_ROLES);
        serializer.attribute(null, ATTRIBUTE_VERSION, Integer.toString(version));
        if (packagesHash != null) {
            serializer.attribute(null, ATTRIBUTE_PACKAGES_HASH, packagesHash);
        }
        int size = roles.size();
        for (int i = 0; i < size; i++) {
            serializer.startTag(null, TAG_ROLE);
            serializer.attribute(null, "name", roles.keyAt(i));
            serializeRoleHolders(serializer, roles.valueAt(i));
            serializer.endTag(null, TAG_ROLE);
        }
        serializer.endTag(null, TAG_ROLES);
    }

    private void serializeRoleHolders(XmlSerializer serializer, ArraySet<String> roleHolders) throws IOException {
        int size = roleHolders.size();
        for (int i = 0; i < size; i++) {
            serializer.startTag(null, TAG_HOLDER);
            serializer.attribute(null, "name", roleHolders.valueAt(i));
            serializer.endTag(null, TAG_HOLDER);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002d, code lost:
        if (1 == 0) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
        if (r4 != null) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0039, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003a, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004b, code lost:
        if (0 != 0) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004d, code lost:
        handleReadFileException(r1, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x007d, code lost:
        if (1 != 0) goto L_0x0080;
     */
    private void readFile() {
        synchronized (this.mLock) {
            File file = getFile(this.mUserId);
            String exceptionName = "";
            try {
                FileInputStream in = new AtomicFile(file).openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, null);
                parseXmlLocked(parser);
                Slog.i(LOG_TAG, "Read roles.xml successfully");
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException | XmlPullParserException e) {
                        e.getClass().toString();
                        throw new IllegalStateException("Failed to parse roles.xml: " + file, e);
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.i(LOG_TAG, "roles.xml not found");
            } catch (Exception e3) {
                exceptionName = e3.getClass().toString();
            } catch (Throwable th) {
                if (0 == 0) {
                    handleReadFileException(file, exceptionName);
                }
                throw th;
            }
        }
    }

    private void handleReadFileException(File file, String exceptionName) {
        if (file != null && file.exists()) {
            file.delete();
            this.mVersion = -1;
            this.mPackagesHash = "";
            this.mRoles.clear();
            String str = LOG_TAG;
            Slog.e(str, "Failed to parse roles.xml of user:" + this.mUserId + ", occur to " + exceptionName + ", so delete the xml and clear data");
            HwPackageManagerServiceUtils.reportPmsParseFileException(ROLES_FILE_NAME, exceptionName, this.mUserId, null);
        }
    }

    private void parseXmlLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int depth;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type == 1 || ((depth = parser.getDepth()) < innerDepth && type == 3)) {
                break;
            } else if (depth <= innerDepth && type == 2 && parser.getName().equals(TAG_ROLES)) {
                parseRolesLocked(parser);
                return;
            }
        }
        Slog.w(LOG_TAG, "Missing <roles> in roles.xml");
    }

    private void parseRolesLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        this.mVersion = Integer.parseInt(parser.getAttributeValue(null, ATTRIBUTE_VERSION));
        this.mPackagesHash = parser.getAttributeValue(null, ATTRIBUTE_PACKAGES_HASH);
        this.mRoles.clear();
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (depth <= innerDepth && type == 2 && parser.getName().equals(TAG_ROLE)) {
                    this.mRoles.put(parser.getAttributeValue(null, "name"), parseRoleHoldersLocked(parser));
                }
            } else {
                return;
            }
        }
    }

    private ArraySet<String> parseRoleHoldersLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int depth;
        ArraySet<String> roleHolders = new ArraySet<>();
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type == 1 || ((depth = parser.getDepth()) < innerDepth && type == 3)) {
                break;
            } else if (depth <= innerDepth && type == 2 && parser.getName().equals(TAG_HOLDER)) {
                roleHolders.add(parser.getAttributeValue(null, "name"));
            }
        }
        return roleHolders;
    }

    public void dump(DualDumpOutputStream dumpOutputStream, String fieldName, long fieldId) {
        int version;
        String packagesHash;
        ArrayMap<String, ArraySet<String>> roles;
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            version = this.mVersion;
            packagesHash = this.mPackagesHash;
            roles = snapshotRolesLocked();
        }
        long fieldToken = dumpOutputStream.start(fieldName, fieldId);
        dumpOutputStream.write("user_id", 1120986464257L, this.mUserId);
        dumpOutputStream.write(ATTRIBUTE_VERSION, 1120986464258L, version);
        dumpOutputStream.write("packages_hash", 1138166333443L, packagesHash);
        int rolesSize = roles.size();
        for (int rolesIndex = 0; rolesIndex < rolesSize; rolesIndex++) {
            ArraySet<String> roleHolders = roles.valueAt(rolesIndex);
            long rolesToken = dumpOutputStream.start(TAG_ROLES, 2246267895812L);
            dumpOutputStream.write("name", 1138166333441L, roles.keyAt(rolesIndex));
            int roleHoldersSize = roleHolders.size();
            int roleHoldersIndex = 0;
            while (roleHoldersIndex < roleHoldersSize) {
                dumpOutputStream.write("holders", 2237677961218L, roleHolders.valueAt(roleHoldersIndex));
                roleHoldersIndex++;
                version = version;
                rolesSize = rolesSize;
            }
            dumpOutputStream.end(rolesToken);
        }
        dumpOutputStream.end(fieldToken);
    }

    public ArrayMap<String, ArraySet<String>> getRolesAndHolders() {
        ArrayMap<String, ArraySet<String>> snapshotRolesLocked;
        synchronized (this.mLock) {
            snapshotRolesLocked = snapshotRolesLocked();
        }
        return snapshotRolesLocked;
    }

    @GuardedBy({"mLock"})
    private ArrayMap<String, ArraySet<String>> snapshotRolesLocked() {
        ArrayMap<String, ArraySet<String>> roles = new ArrayMap<>();
        int size = CollectionUtils.size(this.mRoles);
        for (int i = 0; i < size; i++) {
            roles.put(this.mRoles.keyAt(i), new ArraySet<>(this.mRoles.valueAt(i)));
        }
        return roles;
    }

    public void destroy() {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mWriteHandler.removeCallbacksAndMessages(null);
            getFile(this.mUserId).delete();
            this.mDestroyed = true;
        }
    }

    @GuardedBy({"mLock"})
    private void throwIfDestroyedLocked() {
        if (this.mDestroyed) {
            throw new IllegalStateException("This RoleUserState has already been destroyed");
        }
    }

    public boolean getDestroyedState() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDestroyed;
        }
        return z;
    }

    private static File getFile(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), ROLES_FILE_NAME);
    }
}
