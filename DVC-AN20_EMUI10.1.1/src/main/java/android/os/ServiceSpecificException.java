package android.os;

import android.annotation.SystemApi;

@SystemApi
public class ServiceSpecificException extends RuntimeException {
    public final int errorCode;

    public ServiceSpecificException(int errorCode2, String message) {
        super(message);
        this.errorCode = errorCode2;
    }

    public ServiceSpecificException(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public String toString() {
        return super.toString() + " (code " + this.errorCode + ")";
    }
}
