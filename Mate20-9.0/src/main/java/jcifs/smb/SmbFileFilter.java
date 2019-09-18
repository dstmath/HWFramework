package jcifs.smb;

public interface SmbFileFilter {
    boolean accept(SmbFile smbFile) throws SmbException;
}
