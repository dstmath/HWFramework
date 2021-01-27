package ohos.security.permission;

public final class PermissionFlags {
    public static final int PERMISSION_APPLY_RESTRICTION_FLAG = 16384;
    public static final int PERMISSION_FLAGS_ALL_MASK = 64511;
    public static final int PERMISSION_GRANTED_BY_DEFAULT_FLAG = 32;
    public static final int PERMISSION_GRANTED_BY_ROLE_FLAG = 32768;
    public static final int PERMISSION_POLICY_FIXED_FLAG = 4;
    public static final int PERMISSION_RESTRICTION_ANY_EXEMPT_FLAGS = 14336;
    public static final int PERMISSION_RESTRICTION_INSTALLER_EXEMPT_FLAG = 2048;
    public static final int PERMISSION_RESTRICTION_SYSTEM_EXEMPT_FLAG = 4096;
    public static final int PERMISSION_RESTRICTION_UPGRADE_EXEMPT_FLAG = 8192;
    public static final int PERMISSION_REVIEW_REQUIRED_FLAG = 64;
    public static final int PERMISSION_REVOKE_ON_UPGRADE_FLAG = 8;
    public static final int PERMISSION_REVOKE_WHEN_REQUESTED_FLAG = 128;
    public static final int PERMISSION_SYSTEM_FIXED_FLAG = 16;
    public static final int PERMISSION_USER_FIXED_FLAG = 2;
    public static final int PERMISSION_USER_SENSITIVE_WHEN_DENIED_FLAG = 512;
    public static final int PERMISSION_USER_SENSITIVE_WHEN_GRANTED_FLAG = 256;
    public static final int PERMISSION_USER_SET_FLAG = 1;

    private PermissionFlags() {
    }
}
