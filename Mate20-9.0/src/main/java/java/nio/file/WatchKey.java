package java.nio.file;

import java.util.List;

public interface WatchKey {
    void cancel();

    boolean isValid();

    List<WatchEvent<?>> pollEvents();

    boolean reset();

    Watchable watchable();
}
