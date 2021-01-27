package android.telephony.ims;

import android.annotation.SystemApi;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class ImsException extends Exception {
    public static final int CODE_ERROR_SERVICE_UNAVAILABLE = 1;
    public static final int CODE_ERROR_UNSPECIFIED = 0;
    public static final int CODE_ERROR_UNSUPPORTED_OPERATION = 2;
    private int mCode = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ImsErrorCode {
    }

    public ImsException(String message) {
        super(getMessage(message, 0));
    }

    public ImsException(String message, int code) {
        super(getMessage(message, code));
        this.mCode = code;
    }

    public ImsException(String message, int code, Throwable cause) {
        super(getMessage(message, code), cause);
        this.mCode = code;
    }

    public int getCode() {
        return this.mCode;
    }

    private static String getMessage(String message, int code) {
        if (!TextUtils.isEmpty(message)) {
            return message + " (code: " + code + ")";
        }
        return "code: " + code;
    }
}
