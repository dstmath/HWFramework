package com.huawei.internal.telephony;

import android.util.Log;
import com.android.internal.telephony.CommandException;

public class CommandExceptionExt extends Exception {
    private static final String TAG = "CommandExceptionExt";
    private Error mError;

    public enum Error {
        INVALID_RESPONSE,
        RADIO_NOT_AVAILABLE,
        GENERIC_FAILURE,
        PASSWORD_INCORRECT,
        SIM_PIN2,
        SIM_PUK2,
        REQUEST_NOT_SUPPORTED,
        OP_NOT_ALLOWED_DURING_VOICE_CALL,
        OP_NOT_ALLOWED_BEFORE_REG_NW,
        SMS_FAIL_RETRY,
        SIM_ABSENT,
        SUBSCRIPTION_NOT_AVAILABLE,
        MODE_NOT_SUPPORTED,
        FDN_CHECK_FAILURE,
        ILLEGAL_SIM_OR_ME,
        MISSING_RESOURCE,
        NO_SUCH_ELEMENT,
        DIAL_MODIFIED_TO_USSD,
        DIAL_MODIFIED_TO_SS,
        DIAL_MODIFIED_TO_DIAL,
        USSD_MODIFIED_TO_DIAL,
        USSD_MODIFIED_TO_SS,
        USSD_MODIFIED_TO_USSD,
        SS_MODIFIED_TO_DIAL,
        SS_MODIFIED_TO_USSD,
        SS_MODIFIED_TO_SS,
        INVALID_PARAMETER,
        SUBSCRIPTION_NOT_SUPPORTED,
        UT_NO_CONNECTION
    }

    public CommandExceptionExt() {
    }

    public CommandExceptionExt(Error error) {
        super(error.toString());
        this.mError = error;
    }

    public static Error getCommandError(CommandException ce) {
        String commandError = ce.getCommandError().toString();
        Error[] values = Error.values();
        for (Error error : values) {
            if (error.toString().equals(commandError)) {
                return error;
            }
        }
        Log.e(TAG, "Unrecognized RIL errno " + ce.getCommandError());
        return Error.INVALID_RESPONSE;
    }

    public Error getCommandError() {
        return this.mError;
    }

    public static CommandException.Error getCommandErrorFromEx(CommandExceptionExt ce) {
        String commandError = ce.getCommandError().toString();
        CommandException.Error[] values = CommandException.Error.values();
        for (CommandException.Error error : values) {
            if (error.toString().equals(commandError)) {
                return error;
            }
        }
        Log.e(TAG, "Unrecognized RIL errno " + ce.getCommandError());
        return CommandException.Error.INVALID_RESPONSE;
    }

    public static CommandExceptionExt getCommandException(Throwable throwable) {
        if (throwable instanceof CommandException) {
            return new CommandExceptionExt(getCommandError((CommandException) throwable));
        }
        return new CommandExceptionExt(Error.GENERIC_FAILURE);
    }

    public static CommandException getCommandExceptionFromEx(Throwable throwable) {
        if (throwable instanceof CommandExceptionExt) {
            return new CommandException(getCommandErrorFromEx((CommandExceptionExt) throwable));
        }
        return new CommandException(CommandException.Error.GENERIC_FAILURE);
    }

    public static boolean isCommandException(Throwable throwable) {
        return throwable instanceof CommandException;
    }
}
