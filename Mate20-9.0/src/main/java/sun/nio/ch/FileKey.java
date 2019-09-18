package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;

public class FileKey {
    private long st_dev;
    private long st_ino;

    private native void init(FileDescriptor fileDescriptor) throws IOException;

    private FileKey() {
    }

    public static FileKey create(FileDescriptor fd) {
        FileKey fk = new FileKey();
        try {
            fk.init(fd);
            return fk;
        } catch (IOException ioe) {
            throw new Error((Throwable) ioe);
        }
    }

    public int hashCode() {
        return ((int) (this.st_dev ^ (this.st_dev >>> 32))) + ((int) (this.st_ino ^ (this.st_ino >>> 32)));
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FileKey)) {
            return false;
        }
        FileKey other = (FileKey) obj;
        if (this.st_dev == other.st_dev && this.st_ino == other.st_ino) {
            return true;
        }
        return false;
    }
}
