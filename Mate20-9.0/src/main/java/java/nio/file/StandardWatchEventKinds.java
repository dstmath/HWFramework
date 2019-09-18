package java.nio.file;

import java.nio.file.WatchEvent;

public final class StandardWatchEventKinds {
    public static final WatchEvent.Kind<Path> ENTRY_CREATE = new StdWatchEventKind("ENTRY_CREATE", Path.class);
    public static final WatchEvent.Kind<Path> ENTRY_DELETE = new StdWatchEventKind("ENTRY_DELETE", Path.class);
    public static final WatchEvent.Kind<Path> ENTRY_MODIFY = new StdWatchEventKind("ENTRY_MODIFY", Path.class);
    public static final WatchEvent.Kind<Object> OVERFLOW = new StdWatchEventKind("OVERFLOW", Object.class);

    private static class StdWatchEventKind<T> implements WatchEvent.Kind<T> {
        private final String name;
        private final Class<T> type;

        StdWatchEventKind(String name2, Class<T> type2) {
            this.name = name2;
            this.type = type2;
        }

        public String name() {
            return this.name;
        }

        public Class<T> type() {
            return this.type;
        }

        public String toString() {
            return this.name;
        }
    }

    private StandardWatchEventKinds() {
    }
}
