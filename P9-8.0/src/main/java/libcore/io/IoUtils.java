package libcore.io;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class IoUtils {

    private static class FileReader {
        private byte[] bytes;
        private int count;
        private FileDescriptor fd;
        private boolean unknownLength;

        public FileReader(String absolutePath) throws IOException {
            try {
                this.fd = IoBridge.open(absolutePath, OsConstants.O_RDONLY);
                try {
                    int capacity = (int) Libcore.os.fstat(this.fd).st_size;
                    if (capacity == 0) {
                        this.unknownLength = true;
                        capacity = 8192;
                    }
                    this.bytes = new byte[capacity];
                } catch (ErrnoException exception) {
                    IoUtils.closeQuietly(this.fd);
                    throw exception.rethrowAsIOException();
                }
            } catch (FileNotFoundException fnfe) {
                throw fnfe;
            }
        }

        public FileReader readFully() throws IOException {
            int capacity = this.bytes.length;
            while (true) {
                try {
                    int read = Libcore.os.read(this.fd, this.bytes, this.count, capacity - this.count);
                    if (read == 0) {
                        break;
                    }
                    this.count += read;
                    if (this.count == capacity) {
                        if (!this.unknownLength) {
                            break;
                        }
                        int newCapacity = capacity * 2;
                        byte[] newBytes = new byte[newCapacity];
                        System.arraycopy(this.bytes, 0, newBytes, 0, capacity);
                        this.bytes = newBytes;
                        capacity = newCapacity;
                    }
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                } catch (Throwable th) {
                    IoUtils.closeQuietly(this.fd);
                }
            }
            IoUtils.closeQuietly(this.fd);
            return this;
        }

        @FindBugsSuppressWarnings({"EI_EXPOSE_REP"})
        public byte[] toByteArray() {
            if (this.count == this.bytes.length) {
                return this.bytes;
            }
            byte[] result = new byte[this.count];
            System.arraycopy(this.bytes, 0, result, 0, this.count);
            return result;
        }

        public String toString(Charset cs) {
            return new String(this.bytes, 0, this.count, cs);
        }
    }

    private IoUtils() {
    }

    public static void close(FileDescriptor fd) throws IOException {
        if (fd != null) {
            try {
                if (fd.valid()) {
                    Libcore.os.close(fd);
                }
            } catch (ErrnoException errnoException) {
                throw errnoException.rethrowAsIOException();
            }
        }
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }

    public static void closeQuietly(FileDescriptor fd) {
        try {
            close(fd);
        } catch (IOException e) {
        }
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    public static void setBlocking(FileDescriptor fd, boolean blocking) throws IOException {
        try {
            int flags = Libcore.os.fcntlVoid(fd, OsConstants.F_GETFL);
            if (blocking) {
                flags &= ~OsConstants.O_NONBLOCK;
            } else {
                flags |= OsConstants.O_NONBLOCK;
            }
            Libcore.os.fcntlInt(fd, OsConstants.F_SETFL, flags);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public static byte[] readFileAsByteArray(String absolutePath) throws IOException {
        return new FileReader(absolutePath).readFully().toByteArray();
    }

    public static String readFileAsString(String absolutePath) throws IOException {
        return new FileReader(absolutePath).readFully().toString(StandardCharsets.UTF_8);
    }

    public static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                file.delete();
            }
        }
    }

    public static File createTemporaryDirectory(String prefix) {
        File result;
        do {
            result = new File(System.getProperty("java.io.tmpdir"), prefix + Math.randomIntInternal());
        } while (!result.mkdir());
        return result;
    }

    public static boolean canOpenReadOnly(String path) {
        try {
            Libcore.os.close(Libcore.os.open(path, OsConstants.O_RDONLY, 0));
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    public static void throwInterruptedIoException() throws InterruptedIOException {
        Thread.currentThread().interrupt();
        throw new InterruptedIOException();
    }
}
