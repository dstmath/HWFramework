package java.io;

@FunctionalInterface
public interface FilenameFilter {
    boolean accept(File file, String str);
}
