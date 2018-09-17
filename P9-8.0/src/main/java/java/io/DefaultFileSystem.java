package java.io;

class DefaultFileSystem {
    DefaultFileSystem() {
    }

    public static FileSystem getFileSystem() {
        return new UnixFileSystem();
    }
}
