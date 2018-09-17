package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import sun.nio.fs.BasicFileAttributesHolder;

class FileTreeWalker implements Closeable {
    static final /* synthetic */ boolean -assertionsDisabled = (FileTreeWalker.class.desiredAssertionStatus() ^ 1);
    private static final /* synthetic */ int[] -java-nio-file-FileVisitOptionSwitchesValues = null;
    private boolean closed;
    private final boolean followLinks;
    private final LinkOption[] linkOptions;
    private final int maxDepth;
    private final ArrayDeque<DirectoryNode> stack = new ArrayDeque();

    private static class DirectoryNode {
        private final Path dir;
        private final Iterator<Path> iterator;
        private final Object key;
        private boolean skipped;
        private final DirectoryStream<Path> stream;

        DirectoryNode(Path dir, Object key, DirectoryStream<Path> stream) {
            this.dir = dir;
            this.key = key;
            this.stream = stream;
            this.iterator = stream.iterator();
        }

        Path directory() {
            return this.dir;
        }

        Object key() {
            return this.key;
        }

        DirectoryStream<Path> stream() {
            return this.stream;
        }

        Iterator<Path> iterator() {
            return this.iterator;
        }

        void skip() {
            this.skipped = true;
        }

        boolean skipped() {
            return this.skipped;
        }
    }

    static class Event {
        private final BasicFileAttributes attrs;
        private final Path file;
        private final IOException ioe;
        private final EventType type;

        private Event(EventType type, Path file, BasicFileAttributes attrs, IOException ioe) {
            this.type = type;
            this.file = file;
            this.attrs = attrs;
            this.ioe = ioe;
        }

        Event(EventType type, Path file, BasicFileAttributes attrs) {
            this(type, file, attrs, null);
        }

        Event(EventType type, Path file, IOException ioe) {
            this(type, file, null, ioe);
        }

        EventType type() {
            return this.type;
        }

        /* renamed from: file */
        Path lambda$-java_nio_file_Files_166757() {
            return this.file;
        }

        BasicFileAttributes attributes() {
            return this.attrs;
        }

        IOException ioeException() {
            return this.ioe;
        }
    }

    enum EventType {
        START_DIRECTORY,
        END_DIRECTORY,
        ENTRY
    }

    private static /* synthetic */ int[] -getjava-nio-file-FileVisitOptionSwitchesValues() {
        if (-java-nio-file-FileVisitOptionSwitchesValues != null) {
            return -java-nio-file-FileVisitOptionSwitchesValues;
        }
        int[] iArr = new int[FileVisitOption.values().length];
        try {
            iArr[FileVisitOption.FOLLOW_LINKS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        -java-nio-file-FileVisitOptionSwitchesValues = iArr;
        return iArr;
    }

    FileTreeWalker(Collection<FileVisitOption> options, int maxDepth) {
        boolean fl = false;
        for (FileVisitOption option : options) {
            switch (-getjava-nio-file-FileVisitOptionSwitchesValues()[option.ordinal()]) {
                case 1:
                    fl = true;
                default:
                    throw new AssertionError((Object) "Should not get here");
            }
        }
        if (maxDepth < 0) {
            throw new IllegalArgumentException("'maxDepth' is negative");
        }
        this.followLinks = fl;
        this.linkOptions = fl ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        this.maxDepth = maxDepth;
    }

    private BasicFileAttributes getAttributes(Path file, boolean canUseCached) throws IOException {
        BasicFileAttributes attrs;
        if (canUseCached && (file instanceof BasicFileAttributesHolder) && System.getSecurityManager() == null) {
            BasicFileAttributes cached = ((BasicFileAttributesHolder) file).get();
            if (!(cached == null || (this.followLinks && (cached.isSymbolicLink() ^ 1) == 0))) {
                return cached;
            }
        }
        try {
            attrs = Files.readAttributes(file, BasicFileAttributes.class, this.linkOptions);
        } catch (IOException ioe) {
            if (this.followLinks) {
                attrs = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } else {
                throw ioe;
            }
        }
        return attrs;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x002f A:{Splitter: B:9:0x0022, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean wouldLoop(Path dir, Object key) {
        for (DirectoryNode ancestor : this.stack) {
            Object ancestorKey = ancestor.key();
            if (key == null || ancestorKey == null) {
                try {
                    if (Files.isSameFile(dir, ancestor.directory())) {
                        return true;
                    }
                } catch (IOException e) {
                }
            } else if (key.lambda$-java_util_function_Predicate_4628(ancestorKey)) {
                return true;
            }
        }
        return false;
    }

    private Event visit(Path entry, boolean ignoreSecurityException, boolean canUseCached) {
        try {
            BasicFileAttributes attrs = getAttributes(entry, canUseCached);
            if (this.stack.size() >= this.maxDepth || (attrs.isDirectory() ^ 1) != 0) {
                return new Event(EventType.ENTRY, entry, attrs);
            }
            if (this.followLinks && wouldLoop(entry, attrs.fileKey())) {
                return new Event(EventType.ENTRY, entry, new FileSystemLoopException(entry.toString()));
            }
            try {
                this.stack.push(new DirectoryNode(entry, attrs.fileKey(), Files.newDirectoryStream(entry)));
                return new Event(EventType.START_DIRECTORY, entry, attrs);
            } catch (IOException ioe) {
                return new Event(EventType.ENTRY, entry, ioe);
            } catch (SecurityException se) {
                if (ignoreSecurityException) {
                    return null;
                }
                throw se;
            }
        } catch (IOException ioe2) {
            return new Event(EventType.ENTRY, entry, ioe2);
        } catch (SecurityException se2) {
            if (ignoreSecurityException) {
                return null;
            }
            throw se2;
        }
    }

    Event walk(Path file) {
        if (this.closed) {
            throw new IllegalStateException("Closed");
        }
        Event ev = visit(file, false, false);
        if (-assertionsDisabled || ev != null) {
            return ev;
        }
        throw new AssertionError();
    }

    Event next() {
        DirectoryNode top = (DirectoryNode) this.stack.peek();
        if (top == null) {
            return null;
        }
        Event ev;
        do {
            Path entry = null;
            IOException ioe = null;
            if (!top.skipped()) {
                Iterator<Path> iterator = top.iterator();
                try {
                    if (iterator.hasNext()) {
                        entry = (Path) iterator.next();
                    }
                } catch (DirectoryIteratorException x) {
                    ioe = x.getCause();
                }
            }
            if (entry == null) {
                try {
                    top.stream().close();
                } catch (IOException e) {
                    if (ioe != null) {
                        ioe = e;
                    } else {
                        ioe.addSuppressed(e);
                    }
                }
                this.stack.pop();
                return new Event(EventType.END_DIRECTORY, top.directory(), ioe);
            }
            ev = visit(entry, true, true);
        } while (ev == null);
        return ev;
    }

    void pop() {
        if (!this.stack.isEmpty()) {
            try {
                ((DirectoryNode) this.stack.pop()).stream().close();
            } catch (IOException e) {
            }
        }
    }

    void skipRemainingSiblings() {
        if (!this.stack.isEmpty()) {
            ((DirectoryNode) this.stack.peek()).skip();
        }
    }

    boolean isOpen() {
        return this.closed ^ 1;
    }

    public void close() {
        if (!this.closed) {
            while (!this.stack.isEmpty()) {
                pop();
            }
            this.closed = true;
        }
    }
}
