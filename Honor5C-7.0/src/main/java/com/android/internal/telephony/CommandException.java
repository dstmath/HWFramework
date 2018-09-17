package com.android.internal.telephony;

import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.internal.telephony.HwRadarUtils;

public class CommandException extends RuntimeException {
    private Error mError;

    public enum Error {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.CommandException.Error.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.CommandException.Error.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CommandException.Error.<clinit>():void");
        }
    }

    public CommandException(Error e) {
        super(e.toString());
        this.mError = e;
    }

    public CommandException(Error e, String errString) {
        super(errString);
        this.mError = e;
    }

    public static CommandException fromRilErrno(int ril_errno) {
        switch (ril_errno) {
            case UiccCardApplication.AUTH_CONTEXT_UNDEFINED /*-1*/:
                return new CommandException(Error.INVALID_RESPONSE);
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return null;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return new CommandException(Error.RADIO_NOT_AVAILABLE);
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return new CommandException(Error.GENERIC_FAILURE);
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return new CommandException(Error.PASSWORD_INCORRECT);
            case CharacterSets.ISO_8859_1 /*4*/:
                return new CommandException(Error.SIM_PIN2);
            case CharacterSets.ISO_8859_2 /*5*/:
                return new CommandException(Error.SIM_PUK2);
            case CharacterSets.ISO_8859_3 /*6*/:
                return new CommandException(Error.REQUEST_NOT_SUPPORTED);
            case CharacterSets.ISO_8859_5 /*8*/:
                return new CommandException(Error.OP_NOT_ALLOWED_DURING_VOICE_CALL);
            case CharacterSets.ISO_8859_6 /*9*/:
                return new CommandException(Error.OP_NOT_ALLOWED_BEFORE_REG_NW);
            case CharacterSets.ISO_8859_7 /*10*/:
                return new CommandException(Error.SMS_FAIL_RETRY);
            case CharacterSets.ISO_8859_8 /*11*/:
                return new CommandException(Error.SIM_ABSENT);
            case CharacterSets.ISO_8859_9 /*12*/:
                return new CommandException(Error.SUBSCRIPTION_NOT_AVAILABLE);
            case UserData.ASCII_CR_INDEX /*13*/:
                return new CommandException(Error.MODE_NOT_SUPPORTED);
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                return new CommandException(Error.FDN_CHECK_FAILURE);
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                return new CommandException(Error.ILLEGAL_SIM_OR_ME);
            case PduHeaders.MMS_VERSION_1_0 /*16*/:
                return new CommandException(Error.MISSING_RESOURCE);
            case PduHeaders.MMS_VERSION_1_1 /*17*/:
                return new CommandException(Error.NO_SUCH_ELEMENT);
            case PduHeaders.MMS_VERSION_1_2 /*18*/:
                return new CommandException(Error.DIAL_MODIFIED_TO_USSD);
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                return new CommandException(Error.DIAL_MODIFIED_TO_SS);
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                return new CommandException(Error.DIAL_MODIFIED_TO_DIAL);
            case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                return new CommandException(Error.USSD_MODIFIED_TO_DIAL);
            case CallFailCause.NUMBER_CHANGED /*22*/:
                return new CommandException(Error.USSD_MODIFIED_TO_SS);
            case SmsHeader.ELT_ID_OBJECT_DISTR_INDICATOR /*23*/:
                return new CommandException(Error.USSD_MODIFIED_TO_USSD);
            case SmsHeader.ELT_ID_STANDARD_WVG_OBJECT /*24*/:
                return new CommandException(Error.SS_MODIFIED_TO_DIAL);
            case SmsHeader.ELT_ID_CHARACTER_SIZE_WVG_OBJECT /*25*/:
                return new CommandException(Error.SS_MODIFIED_TO_USSD);
            case SmsHeader.ELT_ID_EXTENDED_OBJECT_DATA_REQUEST_CMD /*26*/:
                return new CommandException(Error.SUBSCRIPTION_NOT_SUPPORTED);
            case CallFailCause.CALL_FAIL_DESTINATION_OUT_OF_ORDER /*27*/:
                return new CommandException(Error.SS_MODIFIED_TO_SS);
            case CallFailCause.FACILITY_REJECTED /*29*/:
                return new CommandException(Error.SIM_ALREADY_POWERED_OFF);
            case CallFailCause.STATUS_ENQUIRY /*30*/:
                return new CommandException(Error.SIM_ALREADY_POWERED_ON);
            case CallFailCause.NORMAL_UNSPECIFIED /*31*/:
                return new CommandException(Error.SIM_DATA_NOT_AVAILABLE);
            case UserData.PRINTABLE_ASCII_MIN_INDEX /*32*/:
                return new CommandException(Error.SIM_SAP_CONNECT_FAILURE);
            case SmsHeader.ELT_ID_HYPERLINK_FORMAT_ELEMENT /*33*/:
                return new CommandException(Error.SIM_SAP_MSG_SIZE_TOO_LARGE);
            case CallFailCause.NO_CIRCUIT_AVAIL /*34*/:
                return new CommandException(Error.SIM_SAP_MSG_SIZE_TOO_SMALL);
            case SmsHeader.ELT_ID_ENHANCED_VOICE_MAIL_INFORMATION /*35*/:
                return new CommandException(Error.SIM_SAP_CONNECT_OK_CALL_ONGOING);
            case CdmaSmsAddress.SMS_SUBADDRESS_MAX /*36*/:
                return new CommandException(Error.LCE_NOT_SUPPORTED);
            case SmsHeader.ELT_ID_NATIONAL_LANGUAGE_LOCKING_SHIFT /*37*/:
                return new CommandException(Error.NO_MEMORY);
            case RadioNVItems.RIL_NV_MIP_PROFILE_HA_SPI /*38*/:
                return new CommandException(Error.INTERNAL_ERR);
            case RadioNVItems.RIL_NV_MIP_PROFILE_AAA_SPI /*39*/:
                return new CommandException(Error.SYSTEM_ERR);
            case RadioNVItems.RIL_NV_MIP_PROFILE_MN_HA_SS /*40*/:
                return new CommandException(Error.MODEM_ERR);
            case CallFailCause.TEMPORARY_FAILURE /*41*/:
                return new CommandException(Error.INVALID_STATE);
            case CallFailCause.SWITCHING_CONGESTION /*42*/:
                return new CommandException(Error.NO_RESOURCES);
            case CallFailCause.ACCESS_INFORMATION_DISCARDED /*43*/:
                return new CommandException(Error.SIM_ERR);
            case CallFailCause.CHANNEL_NOT_AVAIL /*44*/:
                return new CommandException(Error.INVALID_ARGUMENTS);
            case 45:
                return new CommandException(Error.INVALID_SIM_STATE);
            case 46:
                return new CommandException(Error.INVALID_MODEM_STATE);
            case WspTypeDecoder.PARAMETER_ID_X_WAP_APPLICATION_ID /*47*/:
                return new CommandException(Error.INVALID_CALL_ID);
            case 48:
                return new CommandException(Error.NO_SMS_TO_ACK);
            case CallFailCause.QOS_NOT_AVAIL /*49*/:
                return new CommandException(Error.NETWORK_ERR);
            case SmsCbConstants.MESSAGE_ID_GSMA_ALLOCATED_CHANNEL_50 /*50*/:
                return new CommandException(Error.REQUEST_RATE_LIMITED);
            case RadioNVItems.RIL_NV_CDMA_PRL_VERSION /*51*/:
                return new CommandException(Error.SIM_BUSY);
            case RadioNVItems.RIL_NV_CDMA_BC10 /*52*/:
                return new CommandException(Error.SIM_FULL);
            case RadioNVItems.RIL_NV_CDMA_BC14 /*53*/:
                return new CommandException(Error.NETWORK_REJECT);
            case RadioNVItems.RIL_NV_CDMA_SO68 /*54*/:
                return new CommandException(Error.OPERATION_NOT_ALLOWED);
            case RadioNVItems.RIL_NV_CDMA_SO73_COP0 /*55*/:
                return new CommandException(Error.EMPTY_RECORD);
            case RadioNVItems.RIL_NV_CDMA_SO73_COP1TO7 /*56*/:
                return new CommandException(Error.INVALID_SMS_FORMAT);
            case RadioNVItems.RIL_NV_CDMA_1X_ADVANCED_ENABLED /*57*/:
                return new CommandException(Error.ENCODING_ERR);
            case CallFailCause.BEARER_NOT_AVAIL /*58*/:
                return new CommandException(Error.INVALID_SMSC_ADDRESS);
            case RadioNVItems.RIL_NV_CDMA_EHRPD_FORCED /*59*/:
                return new CommandException(Error.NO_SUCH_ENTRY);
            case 60:
                return new CommandException(Error.NETWORK_NOT_READY);
            case 61:
                return new CommandException(Error.NOT_PROVISIONED);
            case 62:
                return new CommandException(Error.NO_SUBSCRIPTION);
            case SignalToneUtil.IS95_CONST_IR_SIG_TONE_NO_TONE /*63*/:
                return new CommandException(Error.NO_NETWORK_FOUND);
            case CommandsInterface.SERVICE_CLASS_PACKET /*64*/:
                return new CommandException(Error.DEVICE_IN_USE);
            case HwRadarUtils.RADAR_LEVEL_A /*65*/:
                return new CommandException(Error.ABORTED);
            case 501:
                return new CommandException(Error.OEM_ERROR_1);
            case 502:
                return new CommandException(Error.OEM_ERROR_2);
            case 503:
                return new CommandException(Error.OEM_ERROR_3);
            case 504:
                return new CommandException(Error.OEM_ERROR_4);
            case 505:
                return new CommandException(Error.OEM_ERROR_5);
            case 506:
                return new CommandException(Error.OEM_ERROR_6);
            case 507:
                return new CommandException(Error.OEM_ERROR_7);
            case 508:
                return new CommandException(Error.OEM_ERROR_8);
            case 509:
                return new CommandException(Error.OEM_ERROR_9);
            case 510:
                return new CommandException(Error.OEM_ERROR_10);
            case 511:
                return new CommandException(Error.OEM_ERROR_11);
            case 512:
                return new CommandException(Error.OEM_ERROR_12);
            case 513:
                return new CommandException(Error.OEM_ERROR_13);
            case 514:
                return new CommandException(Error.OEM_ERROR_14);
            case 515:
                return new CommandException(Error.OEM_ERROR_15);
            case 516:
                return new CommandException(Error.OEM_ERROR_16);
            case 517:
                return new CommandException(Error.OEM_ERROR_17);
            case 518:
                return new CommandException(Error.OEM_ERROR_18);
            case 519:
                return new CommandException(Error.OEM_ERROR_19);
            case 520:
                return new CommandException(Error.OEM_ERROR_20);
            case 521:
                return new CommandException(Error.OEM_ERROR_21);
            case 522:
                return new CommandException(Error.OEM_ERROR_22);
            case 523:
                return new CommandException(Error.OEM_ERROR_23);
            case 524:
                return new CommandException(Error.OEM_ERROR_24);
            case 525:
                return new CommandException(Error.OEM_ERROR_25);
            default:
                return HwTelephonyFactory.getHwTelephonyBaseManager().fromRilErrnoEx(ril_errno);
        }
    }

    public Error getCommandError() {
        return this.mError;
    }
}
