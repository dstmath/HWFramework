package android.service.pm;

public final class PackageServiceDumpProto {
    public static final long FEATURES = 2246267895812L;
    public static final long MESSAGES = 2237677961223L;
    public static final long PACKAGES = 2246267895813L;
    public static final long REQUIRED_VERIFIER_PACKAGE = 1146756268033L;
    public static final long SHARED_LIBRARIES = 2246267895811L;
    public static final long SHARED_USERS = 2246267895814L;
    public static final long VERIFIER_PACKAGE = 1146756268034L;

    public final class PackageShortProto {
        public static final long NAME = 1138166333441L;
        public static final long UID = 1120986464258L;

        public PackageShortProto() {
        }
    }

    public final class SharedLibraryProto {
        public static final long APK = 1138166333444L;
        public static final long IS_JAR = 1133871366146L;
        public static final long NAME = 1138166333441L;
        public static final long PATH = 1138166333443L;

        public SharedLibraryProto() {
        }
    }

    public final class SharedUserProto {
        public static final long NAME = 1138166333442L;
        public static final long UID = 1120986464257L;

        public SharedUserProto() {
        }
    }
}
