package com.huawei.internal.telephony;

import android.util.Log;
import com.android.internal.telephony.CommandException;

public class CommandExceptionEx {

    public enum Error {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.CommandExceptionEx.Error.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.CommandExceptionEx.Error.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.CommandExceptionEx.Error.<clinit>():void");
        }
    }

    public CommandExceptionEx() {
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
        Log.e("GSM", "Unrecognized RIL errno " + ce.getCommandError());
        return Error.INVALID_RESPONSE;
    }
}
