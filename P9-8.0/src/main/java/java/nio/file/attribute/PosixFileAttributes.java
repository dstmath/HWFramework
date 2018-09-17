package java.nio.file.attribute;

import java.util.Set;

public interface PosixFileAttributes extends BasicFileAttributes {
    GroupPrincipal group();

    UserPrincipal owner();

    Set<PosixFilePermission> permissions();
}
