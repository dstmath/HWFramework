package android.service.pm;

public final class PackageProto {
    public static final long INSTALLER_NAME = 1138166333447L;
    public static final long INSTALL_TIME_MS = 1112396529669L;
    public static final long NAME = 1138166333441L;
    public static final long SPLITS = 2246267895816L;
    public static final long UID = 1120986464258L;
    public static final long UPDATE_TIME_MS = 1112396529670L;
    public static final long USERS = 2246267895817L;
    public static final long VERSION_CODE = 1120986464259L;
    public static final long VERSION_STRING = 1138166333444L;

    public final class SplitProto {
        public static final long NAME = 1138166333441L;
        public static final long REVISION_CODE = 1120986464258L;

        public SplitProto() {
        }
    }

    public final class UserInfoProto {
        public static final int COMPONENT_ENABLED_STATE_DEFAULT = 0;
        public static final int COMPONENT_ENABLED_STATE_DISABLED = 2;
        public static final int COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED = 4;
        public static final int COMPONENT_ENABLED_STATE_DISABLED_USER = 3;
        public static final int COMPONENT_ENABLED_STATE_ENABLED = 1;
        public static final long DISTRACTION_FLAGS = 1120986464266L;
        public static final long ENABLED_STATE = 1159641169927L;
        public static final int FULL_APP_INSTALL = 1;
        public static final long ID = 1120986464257L;
        public static final long INSTALL_TYPE = 1159641169922L;
        public static final int INSTANT_APP_INSTALL = 2;
        public static final long IS_HIDDEN = 1133871366147L;
        public static final long IS_LAUNCHED = 1133871366150L;
        public static final long IS_STOPPED = 1133871366149L;
        public static final long IS_SUSPENDED = 1133871366148L;
        public static final long LAST_DISABLED_APP_CALLER = 1138166333448L;
        public static final int NOT_INSTALLED_FOR_USER = 0;
        public static final long SUSPENDING_PACKAGE = 1138166333449L;

        public UserInfoProto() {
        }
    }
}
