package jcifs.netbios;

import java.io.IOException;

public class NbtException extends IOException {
    public static final int ACT_ERR = 6;
    public static final int CALLED_NOT_PRESENT = 130;
    public static final int CFT_ERR = 7;
    public static final int CONNECTION_REFUSED = -1;
    public static final int ERR_NAM_SRVC = 1;
    public static final int ERR_SSN_SRVC = 2;
    public static final int FMT_ERR = 1;
    public static final int IMP_ERR = 4;
    public static final int NOT_LISTENING_CALLED = 128;
    public static final int NOT_LISTENING_CALLING = 129;
    public static final int NO_RESOURCES = 131;
    public static final int RFS_ERR = 5;
    public static final int SRV_ERR = 2;
    public static final int SUCCESS = 0;
    public static final int UNSPECIFIED = 143;
    public int errorClass;
    public int errorCode;

    public static String getErrorString(int errorClass2, int errorCode2) {
        switch (errorClass2) {
            case 0:
                return "" + "SUCCESS";
            case 1:
                String result = "" + "ERR_NAM_SRVC/";
                switch (errorCode2) {
                    case 1:
                        result = result + "FMT_ERR: Format Error";
                        break;
                }
                return result + "Unknown error code: " + errorCode2;
            case 2:
                String result2 = "" + "ERR_SSN_SRVC/";
                switch (errorCode2) {
                    case CONNECTION_REFUSED /*-1*/:
                        return result2 + "Connection refused";
                    case 128:
                        return result2 + "Not listening on called name";
                    case NOT_LISTENING_CALLING /*129*/:
                        return result2 + "Not listening for calling name";
                    case 130:
                        return result2 + "Called name not present";
                    case 131:
                        return result2 + "Called name present, but insufficient resources";
                    case UNSPECIFIED /*143*/:
                        return result2 + "Unspecified error";
                    default:
                        return result2 + "Unknown error code: " + errorCode2;
                }
            default:
                return "" + "unknown error class: " + errorClass2;
        }
    }

    public NbtException(int errorClass2, int errorCode2) {
        super(getErrorString(errorClass2, errorCode2));
        this.errorClass = errorClass2;
        this.errorCode = errorCode2;
    }

    public String toString() {
        return new String("errorClass=" + this.errorClass + ",errorCode=" + this.errorCode + ",errorString=" + getErrorString(this.errorClass, this.errorCode));
    }
}
