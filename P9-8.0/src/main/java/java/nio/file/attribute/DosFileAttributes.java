package java.nio.file.attribute;

public interface DosFileAttributes extends BasicFileAttributes {
    boolean isArchive();

    boolean isHidden();

    boolean isReadOnly();

    boolean isSystem();
}
