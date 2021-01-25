package jcifs.smb;

/* access modifiers changed from: package-private */
public interface Info {
    int getAttributes();

    long getCreateTime();

    long getLastWriteTime();

    long getSize();
}
