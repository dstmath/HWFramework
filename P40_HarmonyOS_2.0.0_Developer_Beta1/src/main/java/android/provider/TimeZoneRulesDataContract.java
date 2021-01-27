package android.provider;

import android.annotation.SystemApi;
import android.net.Uri;

@SystemApi
public final class TimeZoneRulesDataContract {
    public static final String AUTHORITY = "com.android.timezone";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.android.timezone");

    private TimeZoneRulesDataContract() {
    }

    public static final class Operation {
        public static final String COLUMN_DISTRO_MAJOR_VERSION = "distro_major_version";
        public static final String COLUMN_DISTRO_MINOR_VERSION = "distro_minor_version";
        public static final String COLUMN_REVISION = "revision";
        public static final String COLUMN_RULES_VERSION = "rules_version";
        public static final String COLUMN_TYPE = "type";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(TimeZoneRulesDataContract.AUTHORITY_URI, "operation");
        public static final String TYPE_INSTALL = "INSTALL";
        public static final String TYPE_NO_OP = "NOOP";
        public static final String TYPE_UNINSTALL = "UNINSTALL";

        private Operation() {
        }
    }
}
