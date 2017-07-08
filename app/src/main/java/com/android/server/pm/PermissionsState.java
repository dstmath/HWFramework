package com.android.server.pm;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.util.ArrayUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class PermissionsState {
    private static final int[] NO_GIDS = null;
    public static final int PERMISSION_OPERATION_FAILURE = -1;
    public static final int PERMISSION_OPERATION_SUCCESS = 0;
    public static final int PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED = 1;
    private int[] mGlobalGids;
    private SparseBooleanArray mPermissionReviewRequired;
    private ArrayMap<String, PermissionData> mPermissions;

    private static final class PermissionData {
        private final BasePermission mPerm;
        private SparseArray<PermissionState> mUserStates;

        public boolean updateFlags(int r1, int r2, int r3) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.PermissionsState.PermissionData.updateFlags(int, int, int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PermissionsState.PermissionData.updateFlags(int, int, int):boolean");
        }

        public PermissionData(BasePermission perm) {
            this.mUserStates = new SparseArray();
            this.mPerm = perm;
        }

        public PermissionData(PermissionData other) {
            this(other.mPerm);
            int otherStateCount = other.mUserStates.size();
            for (int i = PermissionsState.PERMISSION_OPERATION_SUCCESS; i < otherStateCount; i += PermissionsState.PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
                this.mUserStates.put(other.mUserStates.keyAt(i), new PermissionState((PermissionState) other.mUserStates.valueAt(i)));
            }
        }

        public int[] computeGids(int userId) {
            return this.mPerm.computeGids(userId);
        }

        public boolean isGranted(int userId) {
            if (isInstallPermission()) {
                userId = PermissionsState.PERMISSION_OPERATION_FAILURE;
            }
            PermissionState userState = (PermissionState) this.mUserStates.get(userId);
            if (userState == null) {
                return false;
            }
            return userState.mGranted;
        }

        public boolean grant(int userId) {
            if (!isCompatibleUserId(userId) || isGranted(userId)) {
                return false;
            }
            PermissionState userState = (PermissionState) this.mUserStates.get(userId);
            if (userState == null) {
                userState = new PermissionState(this.mPerm.name);
                this.mUserStates.put(userId, userState);
            }
            userState.mGranted = true;
            return true;
        }

        public boolean revoke(int userId) {
            if (!isCompatibleUserId(userId) || !isGranted(userId)) {
                return false;
            }
            PermissionState userState = (PermissionState) this.mUserStates.get(userId);
            userState.mGranted = false;
            if (userState.isDefault()) {
                this.mUserStates.remove(userId);
            }
            return true;
        }

        public PermissionState getPermissionState(int userId) {
            return (PermissionState) this.mUserStates.get(userId);
        }

        public int getFlags(int userId) {
            PermissionState userState = (PermissionState) this.mUserStates.get(userId);
            if (userState != null) {
                return userState.mFlags;
            }
            return PermissionsState.PERMISSION_OPERATION_SUCCESS;
        }

        public boolean isDefault() {
            return this.mUserStates.size() <= 0;
        }

        public static boolean isInstallPermissionKey(int userId) {
            return userId == PermissionsState.PERMISSION_OPERATION_FAILURE;
        }

        private boolean isCompatibleUserId(int userId) {
            return isDefault() || (isInstallPermission() ^ isInstallPermissionKey(userId)) == 0;
        }

        private boolean isInstallPermission() {
            if (this.mUserStates.size() == PermissionsState.PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
                return this.mUserStates.get(PermissionsState.PERMISSION_OPERATION_FAILURE) != null;
            } else {
                return false;
            }
        }
    }

    public static final class PermissionState {
        private int mFlags;
        private boolean mGranted;
        private final String mName;

        public PermissionState(String name) {
            this.mName = name;
        }

        public PermissionState(PermissionState other) {
            this.mName = other.mName;
            this.mGranted = other.mGranted;
            this.mFlags = other.mFlags;
        }

        public boolean isDefault() {
            return !this.mGranted && this.mFlags == 0;
        }

        public String getName() {
            return this.mName;
        }

        public boolean isGranted() {
            return this.mGranted;
        }

        public int getFlags() {
            return this.mFlags;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.PermissionsState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.PermissionsState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PermissionsState.<clinit>():void");
    }

    public PermissionsState() {
        this.mGlobalGids = NO_GIDS;
    }

    public PermissionsState(PermissionsState prototype) {
        this.mGlobalGids = NO_GIDS;
        copyFrom(prototype);
    }

    public void setGlobalGids(int[] globalGids) {
        if (!ArrayUtils.isEmpty(globalGids)) {
            this.mGlobalGids = Arrays.copyOf(globalGids, globalGids.length);
        }
    }

    public void copyFrom(PermissionsState other) {
        if (other != this) {
            int i;
            if (this.mPermissions != null) {
                if (other.mPermissions == null) {
                    this.mPermissions = null;
                } else {
                    this.mPermissions.clear();
                }
            }
            if (other.mPermissions != null) {
                if (this.mPermissions == null) {
                    this.mPermissions = new ArrayMap();
                }
                int permissionCount = other.mPermissions.size();
                for (i = PERMISSION_OPERATION_SUCCESS; i < permissionCount; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
                    this.mPermissions.put((String) other.mPermissions.keyAt(i), new PermissionData((PermissionData) other.mPermissions.valueAt(i)));
                }
            }
            this.mGlobalGids = NO_GIDS;
            if (other.mGlobalGids != NO_GIDS) {
                this.mGlobalGids = Arrays.copyOf(other.mGlobalGids, other.mGlobalGids.length);
            }
            if (this.mPermissionReviewRequired != null) {
                if (other.mPermissionReviewRequired == null) {
                    this.mPermissionReviewRequired = null;
                } else {
                    this.mPermissionReviewRequired.clear();
                }
            }
            if (other.mPermissionReviewRequired != null) {
                if (this.mPermissionReviewRequired == null) {
                    this.mPermissionReviewRequired = new SparseBooleanArray();
                }
                int userCount = other.mPermissionReviewRequired.size();
                for (i = PERMISSION_OPERATION_SUCCESS; i < userCount; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
                    this.mPermissionReviewRequired.put(i, other.mPermissionReviewRequired.valueAt(i));
                }
            }
        }
    }

    public boolean isPermissionReviewRequired(int userId) {
        return this.mPermissionReviewRequired != null ? this.mPermissionReviewRequired.get(userId) : false;
    }

    public int grantInstallPermission(BasePermission permission) {
        return grantPermission(permission, PERMISSION_OPERATION_FAILURE);
    }

    public int revokeInstallPermission(BasePermission permission) {
        return revokePermission(permission, PERMISSION_OPERATION_FAILURE);
    }

    public int grantRuntimePermission(BasePermission permission, int userId) {
        enforceValidUserId(userId);
        if (userId == PERMISSION_OPERATION_FAILURE) {
            return PERMISSION_OPERATION_FAILURE;
        }
        return grantPermission(permission, userId);
    }

    public int revokeRuntimePermission(BasePermission permission, int userId) {
        enforceValidUserId(userId);
        if (userId == PERMISSION_OPERATION_FAILURE) {
            return PERMISSION_OPERATION_FAILURE;
        }
        return revokePermission(permission, userId);
    }

    public boolean hasRuntimePermission(String name, int userId) {
        enforceValidUserId(userId);
        return !hasInstallPermission(name) ? hasPermission(name, userId) : false;
    }

    public boolean hasInstallPermission(String name) {
        return hasPermission(name, PERMISSION_OPERATION_FAILURE);
    }

    public boolean hasPermission(String name, int userId) {
        boolean z = false;
        enforceValidUserId(userId);
        if (this.mPermissions == null) {
            return false;
        }
        PermissionData permissionData = (PermissionData) this.mPermissions.get(name);
        if (permissionData != null) {
            z = permissionData.isGranted(userId);
        }
        return z;
    }

    public boolean hasRequestedPermission(ArraySet<String> names) {
        if (this.mPermissions == null) {
            return false;
        }
        for (int i = names.size() + PERMISSION_OPERATION_FAILURE; i >= 0; i += PERMISSION_OPERATION_FAILURE) {
            if (this.mPermissions.get(names.valueAt(i)) != null) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getPermissions(int userId) {
        enforceValidUserId(userId);
        if (this.mPermissions == null) {
            return Collections.emptySet();
        }
        Set<String> permissions = new ArraySet(this.mPermissions.size());
        int permissionCount = this.mPermissions.size();
        for (int i = PERMISSION_OPERATION_SUCCESS; i < permissionCount; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
            String permission = (String) this.mPermissions.keyAt(i);
            if (hasInstallPermission(permission)) {
                permissions.add(permission);
            } else if (userId != PERMISSION_OPERATION_FAILURE && hasRuntimePermission(permission, userId)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public PermissionState getInstallPermissionState(String name) {
        return getPermissionState(name, PERMISSION_OPERATION_FAILURE);
    }

    public PermissionState getRuntimePermissionState(String name, int userId) {
        enforceValidUserId(userId);
        return getPermissionState(name, userId);
    }

    public List<PermissionState> getInstallPermissionStates() {
        return getPermissionStatesInternal(PERMISSION_OPERATION_FAILURE);
    }

    public List<PermissionState> getRuntimePermissionStates(int userId) {
        enforceValidUserId(userId);
        return getPermissionStatesInternal(userId);
    }

    public int getPermissionFlags(String name, int userId) {
        PermissionState installPermState = getInstallPermissionState(name);
        if (installPermState != null) {
            return installPermState.getFlags();
        }
        PermissionState runtimePermState = getRuntimePermissionState(name, userId);
        if (runtimePermState != null) {
            return runtimePermState.getFlags();
        }
        return PERMISSION_OPERATION_SUCCESS;
    }

    public boolean updatePermissionFlags(BasePermission permission, int userId, int flagMask, int flagValues) {
        enforceValidUserId(userId);
        boolean mayChangeFlags = (flagValues == 0 && flagMask == 0) ? false : true;
        if (this.mPermissions == null) {
            if (!mayChangeFlags) {
                return false;
            }
            ensurePermissionData(permission);
        }
        PermissionData permissionData = (PermissionData) this.mPermissions.get(permission.name);
        if (permissionData == null) {
            if (!mayChangeFlags) {
                return false;
            }
            permissionData = ensurePermissionData(permission);
        }
        int oldFlags = permissionData.getFlags(userId);
        boolean updated = permissionData.updateFlags(userId, flagMask, flagValues);
        if (updated) {
            int newFlags = permissionData.getFlags(userId);
            if ((oldFlags & 64) == 0 && (newFlags & 64) != 0) {
                if (this.mPermissionReviewRequired == null) {
                    this.mPermissionReviewRequired = new SparseBooleanArray();
                }
                this.mPermissionReviewRequired.put(userId, true);
            } else if (!((oldFlags & 64) == 0 || (newFlags & 64) != 0 || this.mPermissionReviewRequired == null)) {
                this.mPermissionReviewRequired.delete(userId);
                if (this.mPermissionReviewRequired.size() <= 0) {
                    this.mPermissionReviewRequired = null;
                }
            }
        }
        return updated;
    }

    public boolean updatePermissionFlagsForAllPermissions(int userId, int flagMask, int flagValues) {
        enforceValidUserId(userId);
        if (this.mPermissions == null) {
            return false;
        }
        boolean changed = false;
        int permissionCount = this.mPermissions.size();
        for (int i = PERMISSION_OPERATION_SUCCESS; i < permissionCount; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
            changed |= ((PermissionData) this.mPermissions.valueAt(i)).updateFlags(userId, flagMask, flagValues);
        }
        return changed;
    }

    public int[] computeGids(int userId) {
        enforceValidUserId(userId);
        int[] gids = this.mGlobalGids;
        if (this.mPermissions != null) {
            int permissionCount = this.mPermissions.size();
            for (int i = PERMISSION_OPERATION_SUCCESS; i < permissionCount; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
                if (hasPermission((String) this.mPermissions.keyAt(i), userId)) {
                    int[] permGids = ((PermissionData) this.mPermissions.valueAt(i)).computeGids(userId);
                    if (permGids != NO_GIDS) {
                        gids = appendInts(gids, permGids);
                    }
                }
            }
        }
        return gids;
    }

    public int[] computeGids(int[] userIds) {
        int[] gids = this.mGlobalGids;
        int length = userIds.length;
        for (int i = PERMISSION_OPERATION_SUCCESS; i < length; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
            gids = appendInts(gids, computeGids(userIds[i]));
        }
        return gids;
    }

    public void reset() {
        this.mGlobalGids = NO_GIDS;
        this.mPermissions = null;
        this.mPermissionReviewRequired = null;
    }

    private PermissionState getPermissionState(String name, int userId) {
        if (this.mPermissions == null) {
            return null;
        }
        PermissionData permissionData = (PermissionData) this.mPermissions.get(name);
        if (permissionData == null) {
            return null;
        }
        return permissionData.getPermissionState(userId);
    }

    private List<PermissionState> getPermissionStatesInternal(int userId) {
        enforceValidUserId(userId);
        if (this.mPermissions == null) {
            return Collections.emptyList();
        }
        List<PermissionState> permissionStates = new ArrayList();
        int permissionCount = this.mPermissions.size();
        for (int i = PERMISSION_OPERATION_SUCCESS; i < permissionCount; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
            PermissionState permissionState = ((PermissionData) this.mPermissions.valueAt(i)).getPermissionState(userId);
            if (permissionState != null) {
                permissionStates.add(permissionState);
            }
        }
        return permissionStates;
    }

    private int grantPermission(BasePermission permission, int userId) {
        if (hasPermission(permission.name, userId)) {
            return PERMISSION_OPERATION_FAILURE;
        }
        boolean hasGids;
        if (ArrayUtils.isEmpty(permission.computeGids(userId))) {
            hasGids = false;
        } else {
            hasGids = true;
        }
        int[] oldGids = hasGids ? computeGids(userId) : NO_GIDS;
        if (!ensurePermissionData(permission).grant(userId)) {
            return PERMISSION_OPERATION_FAILURE;
        }
        if (hasGids) {
            if (oldGids.length != computeGids(userId).length) {
                return PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED;
            }
        }
        return PERMISSION_OPERATION_SUCCESS;
    }

    private int revokePermission(BasePermission permission, int userId) {
        if (!hasPermission(permission.name, userId)) {
            return PERMISSION_OPERATION_FAILURE;
        }
        boolean hasGids;
        if (ArrayUtils.isEmpty(permission.computeGids(userId))) {
            hasGids = false;
        } else {
            hasGids = true;
        }
        int[] oldGids = hasGids ? computeGids(userId) : NO_GIDS;
        PermissionData permissionData = (PermissionData) this.mPermissions.get(permission.name);
        if (!permissionData.revoke(userId)) {
            return PERMISSION_OPERATION_FAILURE;
        }
        if (permissionData.isDefault()) {
            ensureNoPermissionData(permission.name);
        }
        if (hasGids) {
            if (oldGids.length != computeGids(userId).length) {
                return PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED;
            }
        }
        return PERMISSION_OPERATION_SUCCESS;
    }

    private static int[] appendInts(int[] current, int[] added) {
        if (!(current == null || added == null)) {
            int length = added.length;
            for (int i = PERMISSION_OPERATION_SUCCESS; i < length; i += PERMISSION_OPERATION_SUCCESS_GIDS_CHANGED) {
                current = ArrayUtils.appendInt(current, added[i]);
            }
        }
        return current;
    }

    private static void enforceValidUserId(int userId) {
        if (userId != PERMISSION_OPERATION_FAILURE && userId < 0) {
            throw new IllegalArgumentException("Invalid userId:" + userId);
        }
    }

    private PermissionData ensurePermissionData(BasePermission permission) {
        if (this.mPermissions == null) {
            this.mPermissions = new ArrayMap();
        }
        PermissionData permissionData = (PermissionData) this.mPermissions.get(permission.name);
        if (permissionData != null) {
            return permissionData;
        }
        permissionData = new PermissionData(permission);
        this.mPermissions.put(permission.name, permissionData);
        return permissionData;
    }

    private void ensureNoPermissionData(String name) {
        if (this.mPermissions != null) {
            this.mPermissions.remove(name);
            if (this.mPermissions.isEmpty()) {
                this.mPermissions = null;
            }
        }
    }
}
