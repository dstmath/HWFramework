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
        int min;
        int max;
        int mid;
        if ((errcode & samr.SE_GROUP_LOGON_ID) == samr.SE_GROUP_LOGON_ID) {
            min = 1;
            max = NT_STATUS_CODES.length - 1;
            while (max >= min) {
                mid = (min + max) / 2;
                if (errcode > NT_STATUS_CODES[mid]) {
                    min = mid + 1;
                } else if (errcode >= NT_STATUS_CODES[mid]) {
                    return NT_STATUS_MESSAGES[mid];
                } else {
                    max = mid - 1;
                }
            }
        } else {
            min = 0;
            max = DOS_ERROR_CODES.length - 1;
            while (max >= min) {
                mid = (min + max) / 2;
                if (errcode > DOS_ERROR_CODES[mid][0]) {
                    min = mid + 1;
                } else if (errcode >= DOS_ERROR_CODES[mid][0]) {
                    return DOS_ERROR_MESSAGES[mid];
                } else {
                    max = mid - 1;
                }
            }
        }
        return "0x" + Hexdump.toHexString(errcode, 8);
    }

    static int getStatusByCode(int errcode) {
        if ((samr.SE_GROUP_LOGON_ID & errcode) != 0) {
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

    SmbException(int errcode, Throwable rootCause) {
        super(getMessageByCode(errcode));
        this.status = getStatusByCode(errcode);
        this.rootCause = rootCause;
    }

    SmbException(String msg) {
        super(msg);
        this.status = NtStatus.NT_STATUS_UNSUCCESSFUL;
    }

    SmbException(String msg, Throwable rootCause) {
        super(msg);
        this.rootCause = rootCause;
        this.status = NtStatus.NT_STATUS_UNSUCCESSFUL;
    }

    public SmbException(int errcode, boolean winerr) {
        super(winerr ? getMessageByWinerrCode(errcode) : getMessageByCode(errcode));
        if (!winerr) {
            errcode = getStatusByCode(errcode);
        }
        this.status = errcode;
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
