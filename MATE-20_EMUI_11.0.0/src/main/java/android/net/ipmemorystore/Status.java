package android.net.ipmemorystore;

import com.android.internal.annotations.VisibleForTesting;

public class Status {
    public static final int ERROR_DATABASE_CANNOT_BE_OPENED = -3;
    public static final int ERROR_GENERIC = -1;
    public static final int ERROR_ILLEGAL_ARGUMENT = -2;
    public static final int ERROR_STORAGE = -4;
    public static final int ERROR_UNKNOWN = -5;
    public static final int SUCCESS = 0;
    public final int resultCode;

    public Status(int resultCode2) {
        this.resultCode = resultCode2;
    }

    @VisibleForTesting
    public Status(StatusParcelable parcelable) {
        this(parcelable.resultCode);
    }

    public StatusParcelable toParcelable() {
        StatusParcelable parcelable = new StatusParcelable();
        parcelable.resultCode = this.resultCode;
        return parcelable;
    }

    public boolean isSuccess() {
        return this.resultCode == 0;
    }

    public String toString() {
        int i = this.resultCode;
        if (i == -4) {
            return "DATABASE STORAGE ERROR";
        }
        if (i == -3) {
            return "DATABASE CANNOT BE OPENED";
        }
        if (i == -2) {
            return "ILLEGAL ARGUMENT";
        }
        if (i == -1) {
            return "GENERIC ERROR";
        }
        if (i != 0) {
            return "Unknown value ?!";
        }
        return "SUCCESS";
    }
}
