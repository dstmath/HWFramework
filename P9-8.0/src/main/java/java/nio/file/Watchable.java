package java.nio.file;

import java.io.IOException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;

public interface Watchable {
    WatchKey register(WatchService watchService, Kind<?>... kindArr) throws IOException;

    WatchKey register(WatchService watchService, Kind<?>[] kindArr, Modifier... modifierArr) throws IOException;
}
