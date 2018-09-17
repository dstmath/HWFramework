package sun.nio.fs;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

class UnixFileModeAttribute {
    private static final /* synthetic */ int[] -java-nio-file-attribute-PosixFilePermissionSwitchesValues = null;
    static final int ALL_PERMISSIONS = ((((((((UnixConstants.S_IRUSR | UnixConstants.S_IWUSR) | UnixConstants.S_IXUSR) | UnixConstants.S_IRGRP) | UnixConstants.S_IWGRP) | UnixConstants.S_IXGRP) | UnixConstants.S_IROTH) | UnixConstants.S_IWOTH) | UnixConstants.S_IXOTH);
    static final int ALL_READWRITE = (((((UnixConstants.S_IRUSR | UnixConstants.S_IWUSR) | UnixConstants.S_IRGRP) | UnixConstants.S_IWGRP) | UnixConstants.S_IROTH) | UnixConstants.S_IWOTH);
    static final int TEMPFILE_PERMISSIONS = ((UnixConstants.S_IRUSR | UnixConstants.S_IWUSR) | UnixConstants.S_IXUSR);

    private static /* synthetic */ int[] -getjava-nio-file-attribute-PosixFilePermissionSwitchesValues() {
        if (-java-nio-file-attribute-PosixFilePermissionSwitchesValues != null) {
            return -java-nio-file-attribute-PosixFilePermissionSwitchesValues;
        }
        int[] iArr = new int[PosixFilePermission.values().length];
        try {
            iArr[PosixFilePermission.GROUP_EXECUTE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PosixFilePermission.GROUP_READ.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PosixFilePermission.GROUP_WRITE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PosixFilePermission.OTHERS_EXECUTE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PosixFilePermission.OTHERS_READ.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PosixFilePermission.OTHERS_WRITE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PosixFilePermission.OWNER_EXECUTE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PosixFilePermission.OWNER_READ.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PosixFilePermission.OWNER_WRITE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        -java-nio-file-attribute-PosixFilePermissionSwitchesValues = iArr;
        return iArr;
    }

    private UnixFileModeAttribute() {
    }

    static int toUnixMode(Set<PosixFilePermission> perms) {
        int mode = 0;
        for (PosixFilePermission perm : perms) {
            if (perm != null) {
                switch (-getjava-nio-file-attribute-PosixFilePermissionSwitchesValues()[perm.ordinal()]) {
                    case 1:
                        mode |= UnixConstants.S_IXGRP;
                        break;
                    case 2:
                        mode |= UnixConstants.S_IRGRP;
                        break;
                    case 3:
                        mode |= UnixConstants.S_IWGRP;
                        break;
                    case 4:
                        mode |= UnixConstants.S_IXOTH;
                        break;
                    case 5:
                        mode |= UnixConstants.S_IROTH;
                        break;
                    case 6:
                        mode |= UnixConstants.S_IWOTH;
                        break;
                    case 7:
                        mode |= UnixConstants.S_IXUSR;
                        break;
                    case 8:
                        mode |= UnixConstants.S_IRUSR;
                        break;
                    case 9:
                        mode |= UnixConstants.S_IWUSR;
                        break;
                    default:
                        break;
                }
            }
            throw new NullPointerException();
        }
        return mode;
    }

    static int toUnixMode(int defaultMode, FileAttribute<?>... attrs) {
        int mode = defaultMode;
        int length = attrs.length;
        int i = 0;
        while (i < length) {
            FileAttribute<?> attr = attrs[i];
            String name = attr.name();
            if (name.equals("posix:permissions") || (name.equals("unix:permissions") ^ 1) == 0) {
                mode = toUnixMode((Set) attr.value());
                i++;
            } else {
                throw new UnsupportedOperationException("'" + attr.name() + "' not supported as initial attribute");
            }
        }
        return mode;
    }
}
