package java.nio.file;

import java.nio.file.WatchEvent.Kind;

public final class StandardWatchEventKinds {
    public static final Kind<Path> ENTRY_CREATE = new StdWatchEventKind("ENTRY_CREATE", Path.class);
    public static final Kind<Path> ENTRY_DELETE = new StdWatchEventKind("ENTRY_DELETE", Path.class);
    public static final Kind<Path> ENTRY_MODIFY = new StdWatchEventKind("ENTRY_MODIFY", Path.class);
    public static final Kind<Object> OVERFLOW = new StdWatchEventKind("OVERFLOW", Object.class);

    private static class StdWatchEventKind<T> implements Kind<T> {
        private final String name;
        private final Class<T> type;

        StdWatchEventKind(String name, Class<T> type) {
            this.name = name;
            this.type = type;
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
