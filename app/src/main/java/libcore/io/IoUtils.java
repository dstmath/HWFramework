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
import libcore.icu.DateUtilsBridge;

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
                        capacity = DateUtilsBridge.FORMAT_UTC;
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

    public static void setBlocking(java.io.FileDescriptor r1, boolean r2) throws java.io.IOException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.io.IoUtils.setBlocking(java.io.FileDescriptor, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.io.IoUtils.setBlocking(java.io.FileDescriptor, boolean):void");
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
