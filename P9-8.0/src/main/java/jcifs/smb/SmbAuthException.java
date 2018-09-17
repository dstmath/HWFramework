package jcifs.smb;

public class SmbAuthException extends SmbException {
    SmbAuthException(int errcode) {
        super(errcode, null);
    }
}
