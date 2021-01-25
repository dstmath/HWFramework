package com.android.server.policy;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import com.android.server.pm.DumpState;

public abstract class SoftRestrictedPermissionPolicy {
    private static final SoftRestrictedPermissionPolicy DUMMY_POLICY = new SoftRestrictedPermissionPolicy() {
        /* class com.android.server.policy.SoftRestrictedPermissionPolicy.AnonymousClass1 */

        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
        public int resolveAppOp() {
            return -1;
        }

        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
        public int getDesiredOpMode() {
            return 3;
        }

        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
        public boolean shouldSetAppOpIfNotDefault() {
            return false;
        }

        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
        public boolean canBeGranted() {
            return true;
        }
    };
    private static final int FLAGS_PERMISSION_RESTRICTION_ANY_EXEMPT = 14336;

    public abstract boolean canBeGranted();

    public abstract int getDesiredOpMode();

    public abstract int resolveAppOp();

    public abstract boolean shouldSetAppOpIfNotDefault();

    private static int getMinimumTargetSDK(Context context, ApplicationInfo appInfo, UserHandle user) {
        PackageManager pm = context.getPackageManager();
        int minimumTargetSDK = appInfo.targetSdkVersion;
        String[] uidPkgs = pm.getPackagesForUid(appInfo.uid);
        if (uidPkgs == null) {
            return minimumTargetSDK;
        }
        int minimumTargetSDK2 = minimumTargetSDK;
        for (String uidPkg : uidPkgs) {
            if (!uidPkg.equals(appInfo.packageName)) {
                try {
                    minimumTargetSDK2 = Integer.min(minimumTargetSDK2, pm.getApplicationInfoAsUser(uidPkg, 0, user).targetSdkVersion);
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
        return minimumTargetSDK2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052  */
    public static SoftRestrictedPermissionPolicy forPermission(Context context, ApplicationInfo appInfo, UserHandle user, String permission) {
        char c;
        final int targetSDK;
        final boolean applyRestriction;
        final boolean hasAnyRequestedLegacyExternalStorage;
        boolean hasAnyRequestedLegacyExternalStorage2;
        final boolean isWhiteListed;
        final int flags;
        int hashCode = permission.hashCode();
        boolean z = false;
        final boolean isWhiteListed2 = true;
        if (hashCode != -406040016) {
            if (hashCode == 1365911975 && permission.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                c = 1;
                if (c == 0) {
                    if (appInfo != null) {
                        PackageManager pm = context.getPackageManager();
                        int flags2 = pm.getPermissionFlags(permission, appInfo.packageName, user);
                        applyRestriction = (flags2 & DumpState.DUMP_KEYSETS) != 0;
                        if ((flags2 & FLAGS_PERMISSION_RESTRICTION_ANY_EXEMPT) == 0) {
                            isWhiteListed2 = false;
                        }
                        targetSDK = getMinimumTargetSDK(context, appInfo, user);
                        boolean hasAnyRequestedLegacyExternalStorage3 = appInfo.hasRequestedLegacyExternalStorage();
                        String[] uidPkgs = pm.getPackagesForUid(appInfo.uid);
                        if (uidPkgs != null) {
                            hasAnyRequestedLegacyExternalStorage2 = hasAnyRequestedLegacyExternalStorage3;
                            for (String uidPkg : uidPkgs) {
                                if (!uidPkg.equals(appInfo.packageName)) {
                                    try {
                                        hasAnyRequestedLegacyExternalStorage2 |= pm.getApplicationInfoAsUser(uidPkg, 0, user).hasRequestedLegacyExternalStorage();
                                    } catch (PackageManager.NameNotFoundException e) {
                                    }
                                }
                            }
                        } else {
                            hasAnyRequestedLegacyExternalStorage2 = hasAnyRequestedLegacyExternalStorage3;
                        }
                        hasAnyRequestedLegacyExternalStorage = hasAnyRequestedLegacyExternalStorage2;
                    } else {
                        applyRestriction = false;
                        isWhiteListed2 = false;
                        hasAnyRequestedLegacyExternalStorage = false;
                        targetSDK = 0;
                    }
                    return new SoftRestrictedPermissionPolicy() {
                        /* class com.android.server.policy.SoftRestrictedPermissionPolicy.AnonymousClass2 */

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public int resolveAppOp() {
                            return 87;
                        }

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public int getDesiredOpMode() {
                            if (applyRestriction) {
                                return 3;
                            }
                            if (hasAnyRequestedLegacyExternalStorage) {
                                return 0;
                            }
                            return 1;
                        }

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public boolean shouldSetAppOpIfNotDefault() {
                            return getDesiredOpMode() != 1;
                        }

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public boolean canBeGranted() {
                            if (isWhiteListed2 || targetSDK >= 29) {
                                return true;
                            }
                            return false;
                        }
                    };
                } else if (c != 1) {
                    return DUMMY_POLICY;
                } else {
                    if (appInfo != null) {
                        if ((context.getPackageManager().getPermissionFlags(permission, appInfo.packageName, user) & FLAGS_PERMISSION_RESTRICTION_ANY_EXEMPT) != 0) {
                            z = true;
                        }
                        isWhiteListed = z;
                        flags = getMinimumTargetSDK(context, appInfo, user);
                    } else {
                        isWhiteListed = false;
                        flags = 0;
                    }
                    return new SoftRestrictedPermissionPolicy() {
                        /* class com.android.server.policy.SoftRestrictedPermissionPolicy.AnonymousClass3 */

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public int resolveAppOp() {
                            return -1;
                        }

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public int getDesiredOpMode() {
                            return 3;
                        }

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public boolean shouldSetAppOpIfNotDefault() {
                            return false;
                        }

                        @Override // com.android.server.policy.SoftRestrictedPermissionPolicy
                        public boolean canBeGranted() {
                            return isWhiteListed || flags >= 29;
                        }
                    };
                }
            }
        } else if (permission.equals("android.permission.READ_EXTERNAL_STORAGE")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }
}
