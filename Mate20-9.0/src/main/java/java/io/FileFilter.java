package java.io;

@FunctionalInterface
public interface FileFilter {
    boolean accept(File file);
}
