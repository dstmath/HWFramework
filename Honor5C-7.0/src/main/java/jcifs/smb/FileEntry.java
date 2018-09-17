package jcifs.smb;

public interface FileEntry {
    long createTime();

    int getAttributes();

    String getName();

    int getType();

    long lastModified();

    long length();
}
