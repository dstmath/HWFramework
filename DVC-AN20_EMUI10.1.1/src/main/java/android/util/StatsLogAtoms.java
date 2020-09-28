package android.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StatsLogAtoms {
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED = 170;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__AUTO_DENIED = 8;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__AUTO_GRANTED = 5;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__IGNORED = 1;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__IGNORED_POLICY_FIXED = 3;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__IGNORED_RESTRICTED_PERMISSION = 9;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__IGNORED_USER_FIXED = 2;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__USER_DENIED = 6;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__USER_DENIED_WITH_PREJUDICE = 7;
    public static final int PERMISSION_GRANT_REQUEST_RESULT_REPORTED__RESULT__USER_GRANTED = 4;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionGrantRequestResultReported_Result {
    }

    private StatsLogAtoms() {
    }
}
