package android.service.pm;

public final class PackageServiceDumpProto {
    public static final long FEATURES = 2272037699588L;
    public static final long MESSAGES = 2259152797703L;
    public static final long PACKAGES = 2272037699589L;
    public static final long REQUIRED_VERIFIER_PACKAGE = 1172526071809L;
    public static final long SHARED_LIBRARIES = 2272037699587L;
    public static final long SHARED_USERS = 2272037699590L;
    public static final long VERIFIER_PACKAGE = 1172526071810L;

    public final class FeatureProto {
        public static final long NAME = 1159641169921L;
        public static final long VERSION = 1112396529666L;
    }

    public final class PackageShortProto {
        public static final long NAME = 1159641169921L;
        public static final long UID = 1112396529666L;
    }

    public final class SharedLibraryProto {
        public static final long APK = 1159641169924L;
        public static final long IS_JAR = 1155346202626L;
        public static final long NAME = 1159641169921L;
        public static final long PATH = 1159641169923L;
    }

    public final class SharedUserProto {
        public static final long NAME = 1159641169922L;
        public static final long USER_ID = 1112396529665L;
    }
}
