package jcifs.smb;

interface Info {
    int getAttributes();

    long getCreateTime();

    long getLastWriteTime();

    long getSize();
}
