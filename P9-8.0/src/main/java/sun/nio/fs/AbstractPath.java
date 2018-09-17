package sun.nio.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class AbstractPath implements Path {
    protected AbstractPath() {
    }

    public final boolean startsWith(String other) {
        return startsWith(getFileSystem().getPath(other, new String[0]));
    }

    public final boolean endsWith(String other) {
        return endsWith(getFileSystem().getPath(other, new String[0]));
    }

    public final Path resolve(String other) {
        return resolve(getFileSystem().getPath(other, new String[0]));
    }

    public final Path resolveSibling(Path other) {
        if (other == null) {
            throw new NullPointerException();
        }
        Path parent = getParent();
        return parent == null ? other : parent.resolve(other);
    }

    public final Path resolveSibling(String other) {
        return resolveSibling(getFileSystem().getPath(other, new String[0]));
    }

    public final Iterator<Path> iterator() {
        return new Iterator<Path>() {
            private int i = 0;

            public boolean hasNext() {
                return this.i < AbstractPath.this.getNameCount();
            }

            public Path next() {
                if (this.i < AbstractPath.this.getNameCount()) {
                    Path result = AbstractPath.this.getName(this.i);
                    this.i++;
                    return result;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public final File toFile() {
        return new File(toString());
    }

    public final WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return register(watcher, events, new Modifier[0]);
    }
}
