package java.nio.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Iterator;

public interface Path extends Comparable<Path>, Iterable<Path>, Watchable {
    int compareTo(Path path);

    boolean endsWith(String str);

    boolean endsWith(Path path);

    boolean equals(Object obj);

    Path getFileName();

    FileSystem getFileSystem();

    Path getName(int i);

    int getNameCount();

    Path getParent();

    Path getRoot();

    int hashCode();

    boolean isAbsolute();

    Iterator<Path> iterator();

    Path normalize();

    WatchKey register(WatchService watchService, Kind<?>... kindArr) throws IOException;

    WatchKey register(WatchService watchService, Kind<?>[] kindArr, Modifier... modifierArr) throws IOException;

    Path relativize(Path path);

    Path resolve(String str);

    Path resolve(Path path);

    Path resolveSibling(String str);

    Path resolveSibling(Path path);

    boolean startsWith(String str);

    boolean startsWith(Path path);

    Path subpath(int i, int i2);

    Path toAbsolutePath();

    File toFile();

    Path toRealPath(LinkOption... linkOptionArr) throws IOException;

    String toString();

    URI toUri();
}
