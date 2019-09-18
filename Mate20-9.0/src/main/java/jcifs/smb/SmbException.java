package jcifs.smb;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import jcifs.dcerpc.msrpc.samr;
import jcifs.util.Hexdump;

public class SmbException extends IOException implements NtStatus, DosError, WinError {
    private Throwable rootCause;
    private int status;

    static String getMessageByCode(int errcode) {
        if (errcode == 0) {
            return "NT_STATUS_SUCCESS";
        }
        if ((errcode & samr.SE_GROUP_LOGON_ID) == -1073741824) {
            int min = 1;
            int max = NT_STATUS_CODES.length - 1;
            while (max >= min) {
                int mid = (min + max) / 2;
                if (errcode > NT_STATUS_CODES[mid]) {
                    min = mid + 1;
                } else if (errcode >= NT_STATUS_CODES[mid]) {
                    return NT_STATUS_MESSAGES[mid];
                } else {
                    max = mid - 1;
                }
            }
        } else {
            int min2 = 0;
            int max2 = DOS_ERROR_CODES.length - 1;
            while (max2 >= min2) {
                int mid2 = (min2 + max2) / 2;
                if (errcode > DOS_ERROR_CODES[mid2][0]) {
                    min2 = mid2 + 1;
                } else if (errcode >= DOS_ERROR_CODES[mid2][0]) {
                    return DOS_ERROR_MESSAGES[mid2];
                } else {
                    max2 = mid2 - 1;
                }
            }
        }
        return "0x" + Hexdump.toHexString(errcode, 8);
    }

    static int getStatusByCode(int errcode) {
        if ((-1073741824 & errcode) != 0) {
            return errcode;
        }
        int min = 0;
        int max = DOS_ERROR_CODES.length - 1;
        while (max >= min) {
            int mid = (min + max) / 2;
            if (errcode > DOS_ERROR_CODES[mid][0]) {
                min = mid + 1;
            } else if (errcode >= DOS_ERROR_CODES[mid][0]) {
                return DOS_ERROR_CODES[mid][1];
            } else {
                max = mid - 1;
            }
        }
        return NtStatus.NT_STATUS_UNSUCCESSFUL;
    }

    static String getMessageByWinerrCode(int errcode) {
        int min = 0;
        int max = WINERR_CODES.length - 1;
        while (max >= min) {
            int mid = (min + max) / 2;
            if (errcode > WINERR_CODES[mid]) {
                min = mid + 1;
            } else if (errcode >= WINERR_CODES[mid]) {
                return WINERR_MESSAGES[mid];
            } else {
                max = mid - 1;
            }
        }
        return errcode + "";
    }

    SmbException() {
    }

    SmbException(int errcode, Throwable rootCause2) {
        super(getMessageByCode(errcode));
        this.status = getStatusByCode(errcode);
        this.rootCause = rootCause2;
    }

    SmbException(String msg) {
        super(msg);
        this.status = NtStatus.NT_STATUS_UNSUCCESSFUL;
    }

    SmbException(String msg, Throwable rootCause2) {
        super(msg);
        this.rootCause = rootCause2;
        this.status = NtStatus.NT_STATUS_UNSUCCESSFUL;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SmbException(int errcode, boolean winerr) {
        super(winerr ? getMessageByWinerrCode(errcode) : getMessageByCode(errcode));
        this.status = !winerr ? getStatusByCode(errcode) : errcode;
    }

    public int getNtStatus() {
        return this.status;
    }

    public Throwable getRootCause() {
        return this.rootCause;
    }

    public String toString() {
        if (this.rootCause == null) {
            return super.toString();
        }
        StringWriter sw = new StringWriter();
        this.rootCause.printStackTrace(new PrintWriter(sw));
        return super.toString() + "\n" + sw;
    }
}
