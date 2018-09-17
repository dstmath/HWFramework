package jcifs.smb;

public interface SmbFilenameFilter {
    boolean accept(SmbFile smbFile, String str) throws SmbException;
}
