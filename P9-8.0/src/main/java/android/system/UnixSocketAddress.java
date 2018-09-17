package android.system;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class UnixSocketAddress extends SocketAddress {
    private static final int NAMED_PATH_LENGTH = OsConstants.UNIX_PATH_MAX;
    private static final byte[] UNNAMED_PATH = new byte[0];
    private byte[] sun_path;

    private UnixSocketAddress(byte[] sun_path) {
        if (sun_path == null) {
            throw new IllegalArgumentException("sun_path must not be null");
        } else if (sun_path.length > NAMED_PATH_LENGTH) {
            throw new IllegalArgumentException("sun_path exceeds the maximum length");
        } else if (sun_path.length == 0) {
            this.sun_path = UNNAMED_PATH;
        } else {
            this.sun_path = new byte[sun_path.length];
            System.arraycopy(sun_path, 0, this.sun_path, 0, sun_path.length);
        }
    }

    public static UnixSocketAddress createAbstract(String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] path = new byte[(nameBytes.length + 1)];
        System.arraycopy(nameBytes, 0, path, 1, nameBytes.length);
        return new UnixSocketAddress(path);
    }

    public static UnixSocketAddress createFileSystem(String pathName) {
        byte[] pathNameBytes = pathName.getBytes(StandardCharsets.UTF_8);
        byte[] path = new byte[(pathNameBytes.length + 1)];
        System.arraycopy(pathNameBytes, 0, path, 0, pathNameBytes.length);
        return new UnixSocketAddress(path);
    }

    public static UnixSocketAddress createUnnamed() {
        return new UnixSocketAddress(UNNAMED_PATH);
    }

    public byte[] getSunPath() {
        if (this.sun_path.length == 0) {
            return this.sun_path;
        }
        byte[] sunPathCopy = new byte[this.sun_path.length];
        System.arraycopy(this.sun_path, 0, sunPathCopy, 0, this.sun_path.length);
        return sunPathCopy;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(this.sun_path, ((UnixSocketAddress) o).sun_path);
    }

    public int hashCode() {
        return Arrays.hashCode(this.sun_path);
    }

    public String toString() {
        return "UnixSocketAddress[sun_path=" + Arrays.toString(this.sun_path) + ']';
    }
}
