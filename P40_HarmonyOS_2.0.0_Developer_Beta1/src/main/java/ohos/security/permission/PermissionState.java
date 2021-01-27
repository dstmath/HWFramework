package ohos.security.permission;

public final class PermissionState {
    public static final int STATE_ALLOWED = 0;
    public static final int STATE_DEFAULT = 3;
    public static final int STATE_ERRORED = 2;
    public static final int STATE_FOREGROUND = 4;
    public static final int STATE_IGNORED = 1;

    private PermissionState() {
    }
}
