package ohos.data.dataability;

import ohos.data.rdb.RdbAbortException;
import ohos.data.rdb.RdbBusyException;
import ohos.data.rdb.RdbCantOpenException;
import ohos.data.rdb.RdbConstraintException;
import ohos.data.rdb.RdbCorruptException;
import ohos.data.rdb.RdbDoneException;
import ohos.data.rdb.RdbException;
import ohos.data.rdb.RdbFullException;
import ohos.data.rdb.RdbIoErrException;
import ohos.data.rdb.RdbLockedException;
import ohos.data.rdb.RdbMismatchException;
import ohos.data.rdb.RdbMisuseException;
import ohos.data.rdb.RdbNoMemException;
import ohos.data.rdb.RdbPermException;
import ohos.data.rdb.RdbRangeException;
import ohos.data.rdb.RdbReadOnlyException;
import ohos.data.rdb.RdbTooBigException;
import ohos.utils.Parcel;

public class SqliteExceptionUtils {
    private static final int BIND_OR_COLUMN_INDEX_OUT_OF_RANGE_ERROR_CODE = 2;
    private static final int SQLITE_ABORT_EXCEPTION_ERROR_CODE = 4;
    private static final int SQLITE_ACCESS_PERM_EXCEPTION_ERROR_CODE = 5;
    private static final int SQLITE_BLOB_TOO_BIG_EXCEPTION_ERROR_CODE = 6;
    private static final int SQLITE_CANT_OPEN_DATABASE_EXCEPTION_ERROR_CODE = 7;
    private static final int SQLITE_CONSTRAINT_EXCEPTION_ERROR_CODE = 8;
    private static final int SQLITE_DATABASE_CORRUPT_EXCEPTION_ERROR_CODE = 9;
    private static final int SQLITE_DATABASE_LOCKED_EXCEPTION_ERROR_CODE = 10;
    private static final int SQLITE_DATATYPE_MISMATCH_EXCEPTION_ERROR_CODE = 11;
    private static final int SQLITE_DISK_IOEXCEPTION_ERROR_CODE = 12;
    private static final int SQLITE_DONE_EXCEPTION_ERROR_CODE = 13;
    private static final int SQLITE_EXCEPTION_ERROR_CODE = 19;
    private static final int SQLITE_FULL_EXCEPTION_ERROR_CODE = 14;
    private static final int SQLITE_MISUSE_EXCEPTION_ERROR_CODE = 15;
    private static final int SQLITE_OUT_OF_MEMORY_EXCEPTION_ERROR_CODE = 17;
    private static final int SQLITE_READ_ONLY_DATABASE_EXCEPTION_ERROR_CODE = 18;
    private static final int SQLITE_TABLE_LOCKED_EXCEPTION_ERROR_CODE = 1;

    public static void writeExceptionToParcel(Parcel parcel, Exception exc) {
        int i;
        if (exc instanceof RdbLockedException) {
            i = 1;
        } else if (exc instanceof RdbRangeException) {
            i = 2;
        } else if (exc instanceof RdbAbortException) {
            i = 4;
        } else if (exc instanceof RdbPermException) {
            i = 5;
        } else if (exc instanceof RdbTooBigException) {
            i = 6;
        } else if (exc instanceof RdbCantOpenException) {
            i = 7;
        } else if (exc instanceof RdbConstraintException) {
            i = 8;
        } else if (exc instanceof RdbCorruptException) {
            i = 9;
        } else if (exc instanceof RdbBusyException) {
            i = 10;
        } else {
            i = getCode(exc);
        }
        parcel.writeInt(i);
        parcel.writeString(exc.getMessage());
    }

    private static int getCode(Exception exc) {
        if (exc instanceof RdbMismatchException) {
            return 11;
        }
        if (exc instanceof RdbIoErrException) {
            return 12;
        }
        if (exc instanceof RdbDoneException) {
            return 13;
        }
        if (exc instanceof RdbFullException) {
            return 14;
        }
        if (exc instanceof RdbMisuseException) {
            return 15;
        }
        if (exc instanceof RdbNoMemException) {
            return 17;
        }
        return exc instanceof RdbReadOnlyException ? 18 : 19;
    }

    public static void writeNoExceptionToParcel(Parcel parcel) {
        parcel.writeInt(0);
    }

    public static void readExceptionFromParcel(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt != 0) {
            String readString = parcel.readString();
            switch (readInt) {
                case 1:
                    throw new RdbLockedException(readString);
                case 2:
                    throw new RdbRangeException(readString);
                case 3:
                default:
                    throwIfSqliteException(readInt, readString);
                    return;
                case 4:
                    throw new RdbAbortException(readString);
                case 5:
                    throw new RdbPermException(readString);
                case 6:
                    throw new RdbTooBigException(readString);
                case 7:
                    throw new RdbCantOpenException(readString);
                case 8:
                    throw new RdbConstraintException(readString);
                case 9:
                    throw new RdbCorruptException(readString);
                case 10:
                    throw new RdbBusyException(readString);
            }
        }
    }

    private static void throwIfSqliteException(int i, String str) {
        switch (i) {
            case 11:
                throw new RdbMismatchException(str);
            case 12:
                throw new RdbIoErrException(str);
            case 13:
                throw new RdbDoneException(str);
            case 14:
                throw new RdbFullException(str);
            case 15:
                throw new RdbMisuseException(str);
            case 16:
            default:
                throw new RdbException(str);
            case 17:
                throw new RdbNoMemException(str);
            case 18:
                throw new RdbReadOnlyException(str);
        }
    }
}
