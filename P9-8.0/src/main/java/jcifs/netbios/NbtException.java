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

    public static String getErrorString(int errorClass, int errorCode) {
        String result = "";
        switch (errorClass) {
            case 0:
                return result + "SUCCESS";
            case 1:
                result = result + "ERR_NAM_SRVC/";
                switch (errorCode) {
                    case 1:
                        result = result + "FMT_ERR: Format Error";
                        break;
                }
                return result + "Unknown error code: " + errorCode;
            case 2:
                result = result + "ERR_SSN_SRVC/";
                switch (errorCode) {
                    case CONNECTION_REFUSED /*-1*/:
                        return result + "Connection refused";
                    case 128:
                        return result + "Not listening on called name";
                    case NOT_LISTENING_CALLING /*129*/:
                        return result + "Not listening for calling name";
                    case 130:
                        return result + "Called name not present";
                    case 131:
                        return result + "Called name present, but insufficient resources";
                    case UNSPECIFIED /*143*/:
                        return result + "Unspecified error";
                    default:
                        return result + "Unknown error code: " + errorCode;
                }
            default:
                return result + "unknown error class: " + errorClass;
        }
    }

    public NbtException(int errorClass, int errorCode) {
        super(getErrorString(errorClass, errorCode));
        this.errorClass = errorClass;
        this.errorCode = errorCode;
    }

    public String toString() {
        return new String("errorClass=" + this.errorClass + ",errorCode=" + this.errorCode + ",errorString=" + getErrorString(this.errorClass, this.errorCode));
    }
}
