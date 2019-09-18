package android.os;

/* compiled from: ZygoteProcess */
class ZygoteStartFailedEx extends Exception {
    ZygoteStartFailedEx(String s) {
        super(s);
    }

    ZygoteStartFailedEx(Throwable cause) {
        super(cause);
    }

    ZygoteStartFailedEx(String s, Throwable cause) {
        super(s, cause);
    }
}
