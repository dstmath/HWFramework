package com.huawei.internal.telephony;

import android.util.Log;
import com.android.internal.telephony.CommandException;

public class CommandExceptionExt extends Exception {
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
        if ("INVALID_RESPONSE".equals(ce.getCommandError().toString())) {
            return Error.INVALID_RESPONSE;
        }
        if ("RADIO_NOT_AVAILABLE".equals(ce.getCommandError().toString())) {
            return Error.RADIO_NOT_AVAILABLE;
        }
        if ("GENERIC_FAILURE".equals(ce.getCommandError().toString())) {
            return Error.GENERIC_FAILURE;
        }
        if ("PASSWORD_INCORRECT".equals(ce.getCommandError().toString())) {
            return Error.PASSWORD_INCORRECT;
        }
        if ("SIM_PIN2".equals(ce.getCommandError().toString())) {
            return Error.SIM_PIN2;
        }
        if ("SIM_PUK2".equals(ce.getCommandError().toString())) {
            return Error.SIM_PUK2;
        }
        if ("REQUEST_NOT_SUPPORTED".equals(ce.getCommandError().toString())) {
            return Error.REQUEST_NOT_SUPPORTED;
        }
        if ("OP_NOT_ALLOWED_DURING_VOICE_CALL".equals(ce.getCommandError().toString())) {
            return Error.OP_NOT_ALLOWED_DURING_VOICE_CALL;
        }
        if ("OP_NOT_ALLOWED_BEFORE_REG_NW".equals(ce.getCommandError().toString())) {
            return Error.OP_NOT_ALLOWED_BEFORE_REG_NW;
        }
        if ("SMS_FAIL_RETRY".equals(ce.getCommandError().toString())) {
            return Error.SMS_FAIL_RETRY;
        }
        if ("SIM_ABSENT".equals(ce.getCommandError().toString())) {
            return Error.SIM_ABSENT;
        }
        if ("SUBSCRIPTION_NOT_AVAILABLE".equals(ce.getCommandError().toString())) {
            return Error.SUBSCRIPTION_NOT_AVAILABLE;
        }
        if ("MODE_NOT_SUPPORTED".equals(ce.getCommandError().toString())) {
            return Error.MODE_NOT_SUPPORTED;
        }
        if ("FDN_CHECK_FAILURE".equals(ce.getCommandError().toString())) {
            return Error.FDN_CHECK_FAILURE;
        }
        if ("ILLEGAL_SIM_OR_ME".equals(ce.getCommandError().toString())) {
            return Error.ILLEGAL_SIM_OR_ME;
        }
        if ("MISSING_RESOURCE".equals(ce.getCommandError().toString())) {
            return Error.MISSING_RESOURCE;
        }
        if ("NO_SUCH_ELEMENT".equals(ce.getCommandError().toString())) {
            return Error.NO_SUCH_ELEMENT;
        }
        if ("DIAL_MODIFIED_TO_USSD".equals(ce.getCommandError().toString())) {
            return Error.DIAL_MODIFIED_TO_USSD;
        }
        if ("DIAL_MODIFIED_TO_SS".equals(ce.getCommandError().toString())) {
            return Error.DIAL_MODIFIED_TO_SS;
        }
        if ("DIAL_MODIFIED_TO_DIAL".equals(ce.getCommandError().toString())) {
            return Error.DIAL_MODIFIED_TO_DIAL;
        }
        if ("USSD_MODIFIED_TO_DIAL".equals(ce.getCommandError().toString())) {
            return Error.USSD_MODIFIED_TO_DIAL;
        }
        if ("USSD_MODIFIED_TO_SS".equals(ce.getCommandError().toString())) {
            return Error.USSD_MODIFIED_TO_SS;
        }
        if ("USSD_MODIFIED_TO_USSD".equals(ce.getCommandError().toString())) {
            return Error.USSD_MODIFIED_TO_USSD;
        }
        if ("SS_MODIFIED_TO_DIAL".equals(ce.getCommandError().toString())) {
            return Error.SS_MODIFIED_TO_DIAL;
        }
        if ("SS_MODIFIED_TO_USSD".equals(ce.getCommandError().toString())) {
            return Error.SS_MODIFIED_TO_USSD;
        }
        if ("SS_MODIFIED_TO_SS".equals(ce.getCommandError().toString())) {
            return Error.SS_MODIFIED_TO_SS;
        }
        if ("INVALID_PARAMETER".equals(ce.getCommandError().toString())) {
            return Error.INVALID_PARAMETER;
        }
        if ("SUBSCRIPTION_NOT_SUPPORTED".equals(ce.getCommandError().toString())) {
            return Error.SUBSCRIPTION_NOT_SUPPORTED;
        }
        if ("UT_NO_CONNECTION".equals(ce.getCommandError().toString())) {
            return Error.UT_NO_CONNECTION;
        }
        Log.e("GSM", "Unrecognized RIL errno " + ce.getCommandError());
        return Error.INVALID_RESPONSE;
    }

    public static CommandException.Error getCommandErrorFromEx(CommandExceptionExt ce) {
        if ("INVALID_RESPONSE".equals(ce.getCommandError().toString())) {
            return CommandException.Error.INVALID_RESPONSE;
        }
        if ("RADIO_NOT_AVAILABLE".equals(ce.getCommandError().toString())) {
            return CommandException.Error.RADIO_NOT_AVAILABLE;
        }
        if ("GENERIC_FAILURE".equals(ce.getCommandError().toString())) {
            return CommandException.Error.GENERIC_FAILURE;
        }
        if ("PASSWORD_INCORRECT".equals(ce.getCommandError().toString())) {
            return CommandException.Error.PASSWORD_INCORRECT;
        }
        if ("SIM_PIN2".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SIM_PIN2;
        }
        if ("SIM_PUK2".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SIM_PUK2;
        }
        if ("REQUEST_NOT_SUPPORTED".equals(ce.getCommandError().toString())) {
            return CommandException.Error.REQUEST_NOT_SUPPORTED;
        }
        if ("OP_NOT_ALLOWED_DURING_VOICE_CALL".equals(ce.getCommandError().toString())) {
            return CommandException.Error.OP_NOT_ALLOWED_DURING_VOICE_CALL;
        }
        if ("OP_NOT_ALLOWED_BEFORE_REG_NW".equals(ce.getCommandError().toString())) {
            return CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW;
        }
        if ("SMS_FAIL_RETRY".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SMS_FAIL_RETRY;
        }
        if ("SIM_ABSENT".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SIM_ABSENT;
        }
        if ("SUBSCRIPTION_NOT_AVAILABLE".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SUBSCRIPTION_NOT_AVAILABLE;
        }
        if ("MODE_NOT_SUPPORTED".equals(ce.getCommandError().toString())) {
            return CommandException.Error.MODE_NOT_SUPPORTED;
        }
        if ("FDN_CHECK_FAILURE".equals(ce.getCommandError().toString())) {
            return CommandException.Error.FDN_CHECK_FAILURE;
        }
        if ("ILLEGAL_SIM_OR_ME".equals(ce.getCommandError().toString())) {
            return CommandException.Error.ILLEGAL_SIM_OR_ME;
        }
        if ("MISSING_RESOURCE".equals(ce.getCommandError().toString())) {
            return CommandException.Error.MISSING_RESOURCE;
        }
        if ("NO_SUCH_ELEMENT".equals(ce.getCommandError().toString())) {
            return CommandException.Error.NO_SUCH_ELEMENT;
        }
        if ("DIAL_MODIFIED_TO_USSD".equals(ce.getCommandError().toString())) {
            return CommandException.Error.DIAL_MODIFIED_TO_USSD;
        }
        if ("DIAL_MODIFIED_TO_SS".equals(ce.getCommandError().toString())) {
            return CommandException.Error.DIAL_MODIFIED_TO_SS;
        }
        if ("DIAL_MODIFIED_TO_DIAL".equals(ce.getCommandError().toString())) {
            return CommandException.Error.DIAL_MODIFIED_TO_DIAL;
        }
        if ("USSD_MODIFIED_TO_DIAL".equals(ce.getCommandError().toString())) {
            return CommandException.Error.USSD_MODIFIED_TO_DIAL;
        }
        if ("USSD_MODIFIED_TO_SS".equals(ce.getCommandError().toString())) {
            return CommandException.Error.USSD_MODIFIED_TO_SS;
        }
        if ("USSD_MODIFIED_TO_USSD".equals(ce.getCommandError().toString())) {
            return CommandException.Error.USSD_MODIFIED_TO_USSD;
        }
        if ("SS_MODIFIED_TO_DIAL".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SS_MODIFIED_TO_DIAL;
        }
        if ("SS_MODIFIED_TO_USSD".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SS_MODIFIED_TO_USSD;
        }
        if ("SS_MODIFIED_TO_SS".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SS_MODIFIED_TO_SS;
        }
        if ("INVALID_PARAMETER".equals(ce.getCommandError().toString())) {
            return CommandException.Error.INVALID_PARAMETER;
        }
        if ("SUBSCRIPTION_NOT_SUPPORTED".equals(ce.getCommandError().toString())) {
            return CommandException.Error.SUBSCRIPTION_NOT_SUPPORTED;
        }
        if ("UT_NO_CONNECTION".equals(ce.getCommandError().toString())) {
            return CommandException.Error.UT_NO_CONNECTION;
        }
        Log.e("GSM", "Unrecognized RIL errno " + ce.getCommandError());
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

    public Error getCommandError() {
        return this.mError;
    }
}
