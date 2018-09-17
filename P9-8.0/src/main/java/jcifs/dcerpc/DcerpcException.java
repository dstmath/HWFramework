package jcifs.dcerpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import jcifs.smb.WinError;
import jcifs.util.Hexdump;

public class DcerpcException extends IOException implements DcerpcError, WinError {
    private int error;
    private Throwable rootCause;

    static String getMessageByDcerpcError(int errcode) {
        int min = 0;
        int max = DCERPC_FAULT_CODES.length;
        while (max >= min) {
            int mid = (min + max) / 2;
            if (errcode > DCERPC_FAULT_CODES[mid]) {
                min = mid + 1;
            } else if (errcode >= DCERPC_FAULT_CODES[mid]) {
                return DCERPC_FAULT_MESSAGES[mid];
            } else {
                max = mid - 1;
            }
        }
        return "0x" + Hexdump.toHexString(errcode, 8);
    }

    DcerpcException(int error) {
        super(getMessageByDcerpcError(error));
        this.error = error;
    }

    public DcerpcException(String msg) {
        super(msg);
    }

    public DcerpcException(String msg, Throwable rootCause) {
        super(msg);
        this.rootCause = rootCause;
    }

    public int getErrorCode() {
        return this.error;
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
