package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.cat.BearerDescription.BearerType;
import com.android.internal.telephony.cat.InterfaceTransportLevel.TransportProtocol;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import java.util.Iterator;
import java.util.List;

public class CommandParamsFactory extends AbstractCommandParamsFactory {
    private static final /* synthetic */ int[] -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues = null;
    static final int DTTZ_SETTING = 3;
    static final int LANGUAGE_SETTING = 4;
    static final int LOAD_MULTI_ICONS = 2;
    static final int LOAD_NO_ICON = 0;
    static final int LOAD_SINGLE_ICON = 1;
    private static final int MAX_GSM7_DEFAULT_CHARS = 239;
    private static final int MAX_UCS2_CHARS = 118;
    static final int MSG_ID_LOAD_ICON_DONE = 1;
    static final int REFRESH_NAA_INIT = 3;
    static final int REFRESH_NAA_INIT_AND_FILE_CHANGE = 2;
    static final int REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE = 0;
    static final int REFRESH_UICC_RESET = 4;
    private static CommandParamsFactory sInstance;
    private RilMessageDecoder mCaller;
    private CommandParams mCmdParams;
    private int mIconLoadState;
    private IconLoader mIconLoader;
    private boolean mloadIcon;
    private boolean stkSupportIcon;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues() {
        if (-com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues != null) {
            return -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = MSG_ID_LOAD_ICON_DONE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = REFRESH_NAA_INIT_AND_FILE_CHANGE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = REFRESH_NAA_INIT;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = REFRESH_UICC_RESET;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.LANGUAGE_NOTIFICATION.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CommandType.PLAY_TONE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CommandType.RECEIVE_DATA.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CommandType.REFRESH.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CommandType.SELECT_ITEM.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CommandType.SEND_DATA.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CommandType.SEND_DTMF.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CommandType.SEND_SMS.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CommandType.SEND_SS.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CommandType.SEND_USSD.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CommandType.SET_POLL_INTERVALL.ordinal()] = 23;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[CommandType.SET_UP_CALL.ordinal()] = 19;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[CommandType.SET_UP_EVENT_LIST.ordinal()] = 20;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 21;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[CommandType.SET_UP_MENU.ordinal()] = 22;
        } catch (NoSuchFieldError e23) {
        }
        -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.CommandParamsFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.CommandParamsFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CommandParamsFactory.<clinit>():void");
    }

    static synchronized CommandParamsFactory getInstance(RilMessageDecoder caller, IccFileHandler fh) {
        synchronized (CommandParamsFactory.class) {
            CommandParamsFactory commandParamsFactory;
            if (sInstance != null) {
                commandParamsFactory = sInstance;
                return commandParamsFactory;
            } else if (fh != null) {
                commandParamsFactory = new CommandParamsFactory(caller, fh);
                return commandParamsFactory;
            } else {
                return null;
            }
        }
    }

    private CommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh) {
        this.mCmdParams = null;
        this.mIconLoadState = REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE;
        this.mCaller = null;
        this.mloadIcon = false;
        this.stkSupportIcon = SystemProperties.getBoolean("ro.config.hw_stk_icon", false);
        this.mCaller = caller;
        this.mIconLoader = IconLoader.getInstance(this, fh);
    }

    private CommandDetails processCommandDetails(List<ComprehensionTlv> ctlvs) {
        CommandDetails cmdDet = null;
        if (ctlvs != null) {
            ComprehensionTlv ctlvCmdDet = searchForTag(ComprehensionTlvTag.COMMAND_DETAILS, ctlvs);
            if (ctlvCmdDet != null) {
                try {
                    cmdDet = ValueParser.retrieveCommandDetails(ctlvCmdDet);
                } catch (ResultException e) {
                    CatLog.d((Object) this, "processCommandDetails: Failed to procees command details e=" + e);
                }
            }
        }
        return cmdDet;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void make(BerTlv berTlv) {
        if (berTlv != null) {
            this.mCmdParams = null;
            this.mIconLoadState = REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE;
            if (berTlv.getTag() != BerTlv.BER_PROACTIVE_COMMAND_TAG) {
                sendCmdParams(ResultCode.CMD_TYPE_NOT_UNDERSTOOD);
                return;
            }
            boolean cmdPending = false;
            List<ComprehensionTlv> ctlvs = berTlv.getComprehensionTlvs();
            CommandDetails cmdDet = processCommandDetails(ctlvs);
            if (cmdDet == null) {
                sendCmdParams(ResultCode.CMD_TYPE_NOT_UNDERSTOOD);
                return;
            }
            CommandType cmdType = CommandType.fromInt(cmdDet.typeOfCommand);
            if (cmdType == null) {
                this.mCmdParams = new CommandParams(cmdDet);
                sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
            } else if (berTlv.isLengthValid()) {
                try {
                    switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[cmdType.ordinal()]) {
                        case MSG_ID_LOAD_ICON_DONE /*1*/:
                        case CharacterSets.ISO_8859_5 /*8*/:
                        case CharacterSets.ISO_8859_8 /*11*/:
                        case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                            if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                                if (cmdType != CommandType.OPEN_CHANNEL) {
                                    if (cmdType != CommandType.CLOSE_CHANNEL) {
                                        if (cmdType != CommandType.RECEIVE_DATA) {
                                            if (cmdType == CommandType.SEND_DATA) {
                                                cmdPending = processSendData(cmdDet, ctlvs);
                                                break;
                                            }
                                        }
                                        cmdPending = processReceiveData(cmdDet, ctlvs);
                                        break;
                                    }
                                    cmdPending = processCloseChannel(cmdDet, ctlvs);
                                    break;
                                }
                                cmdPending = processOpenChannel(cmdDet, ctlvs);
                                break;
                            }
                            cmdPending = processBIPClient(cmdDet, ctlvs);
                            break;
                            break;
                        case REFRESH_NAA_INIT_AND_FILE_CHANGE /*2*/:
                            cmdPending = processDisplayText(cmdDet, ctlvs);
                            break;
                        case REFRESH_NAA_INIT /*3*/:
                            if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                                cmdPending = processGetChannelStatus(cmdDet, ctlvs);
                                break;
                            }
                        case REFRESH_UICC_RESET /*4*/:
                            cmdPending = processGetInkey(cmdDet, ctlvs);
                            break;
                        case CharacterSets.ISO_8859_2 /*5*/:
                            cmdPending = processGetInput(cmdDet, ctlvs);
                            break;
                        case CharacterSets.ISO_8859_3 /*6*/:
                            cmdPending = processLanguageNotification(cmdDet, ctlvs);
                            break;
                        case CharacterSets.ISO_8859_4 /*7*/:
                            cmdPending = processLaunchBrowser(cmdDet, ctlvs);
                            break;
                        case CharacterSets.ISO_8859_6 /*9*/:
                            cmdPending = processPlayTone(cmdDet, ctlvs);
                            break;
                        case CharacterSets.ISO_8859_7 /*10*/:
                            cmdPending = processProvideLocalInfo(cmdDet, ctlvs);
                            break;
                        case CharacterSets.ISO_8859_9 /*12*/:
                            processRefresh(cmdDet, ctlvs);
                            cmdPending = false;
                            break;
                        case UserData.ASCII_CR_INDEX /*13*/:
                            cmdPending = processSelectItem(cmdDet, ctlvs);
                            break;
                        case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                        case PduHeaders.MMS_VERSION_1_0 /*16*/:
                        case PduHeaders.MMS_VERSION_1_1 /*17*/:
                        case PduHeaders.MMS_VERSION_1_2 /*18*/:
                            cmdPending = processEventNotify(cmdDet, ctlvs);
                            break;
                        case PduHeaders.MMS_VERSION_1_3 /*19*/:
                            cmdPending = processSetupCall(cmdDet, ctlvs);
                            break;
                        case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                            cmdPending = processSetUpEventList(cmdDet, ctlvs);
                            break;
                        case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                            cmdPending = processSetUpIdleModeText(cmdDet, ctlvs);
                            break;
                        case CallFailCause.NUMBER_CHANGED /*22*/:
                            cmdPending = processSelectItem(cmdDet, ctlvs);
                            break;
                        default:
                            this.mCmdParams = new CommandParams(cmdDet);
                            sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                            return;
                    }
                    if (!cmdPending) {
                        sendCmdParams(ResultCode.OK);
                    }
                } catch (ResultException e) {
                    CatLog.d((Object) this, "make: caught ResultException e=" + e);
                    this.mCmdParams = new CommandParams(cmdDet);
                    sendCmdParams(e.result());
                }
            } else {
                this.mCmdParams = new CommandParams(cmdDet);
                sendCmdParams(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ID_LOAD_ICON_DONE /*1*/:
                if (this.mIconLoader != null) {
                    sendCmdParams(setIcons(msg.obj));
                }
            default:
        }
    }

    private ResultCode setIcons(Object data) {
        int i = REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE;
        if (data == null) {
            CatLog.d((Object) this, "Optional Icon data is NULL");
            this.mCmdParams.mLoadIconFailed = true;
            this.mloadIcon = false;
            return ResultCode.OK;
        }
        switch (this.mIconLoadState) {
            case MSG_ID_LOAD_ICON_DONE /*1*/:
                this.mCmdParams.setIcon((Bitmap) data);
                break;
            case REFRESH_NAA_INIT_AND_FILE_CHANGE /*2*/:
                Bitmap[] icons = (Bitmap[]) data;
                int length = icons.length;
                while (i < length) {
                    Bitmap icon = icons[i];
                    this.mCmdParams.setIcon(icon);
                    if (icon == null && this.mloadIcon) {
                        CatLog.d((Object) this, "Optional Icon data is NULL while loading multi icons");
                        this.mCmdParams.mLoadIconFailed = true;
                    }
                    i += MSG_ID_LOAD_ICON_DONE;
                }
                break;
        }
        return ResultCode.OK;
    }

    private void sendCmdParams(ResultCode resCode) {
        if (this.mCaller != null) {
            this.mCaller.sendMsgParamsDecoded(resCode, this.mCmdParams);
        }
    }

    private ComprehensionTlv searchForTag(ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        return searchForNextTag(tag, ctlvs.iterator());
    }

    private ComprehensionTlv searchForNextTag(ComprehensionTlvTag tag, Iterator<ComprehensionTlv> iter) {
        int tagValue = tag.value();
        while (iter.hasNext()) {
            ComprehensionTlv ctlv = (ComprehensionTlv) iter.next();
            if (ctlv.getTag() == tagValue) {
                return ctlv;
            }
        }
        return null;
    }

    private boolean processDisplayText(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process DisplayText");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveTextString(ctlv);
        }
        if (textMsg.text == null) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
        boolean z;
        if (searchForTag(ComprehensionTlvTag.IMMEDIATE_RESPONSE, ctlvs) != null) {
            textMsg.responseNeeded = false;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
        if (ctlv != null) {
            textMsg.duration = ValueParser.retrieveDuration(ctlv);
        }
        textMsg.isHighPriority = (cmdDet.commandQualifier & MSG_ID_LOAD_ICON_DONE) != 0;
        if ((cmdDet.commandQualifier & PduPart.P_Q) != 0) {
            z = true;
        } else {
            z = false;
        }
        textMsg.userClear = z;
        this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
        if (iconId != null) {
            if (this.stkSupportIcon) {
                this.mloadIcon = true;
                this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                return true;
            }
            CatLog.d((Object) this, "Close load icon feature.");
            this.mCmdParams.mLoadIconFailed = true;
        }
        return false;
    }

    private boolean processSetUpIdleModeText(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetUpIdleModeText");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveTextString(ctlv);
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        if (textMsg.text != null || iconId == null || textMsg.iconSelfExplanatory) {
            this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
            if (iconId != null) {
                if (this.stkSupportIcon) {
                    this.mloadIcon = true;
                    this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                    return true;
                }
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
            }
            return false;
        }
        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
    }

    private boolean processGetInkey(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process GetInkey");
        Input input = new Input();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            boolean z;
            input.text = ValueParser.retrieveTextString(ctlv);
            ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv != null) {
                iconId = ValueParser.retrieveIconId(ctlv);
            }
            ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
            if (ctlv != null) {
                input.duration = ValueParser.retrieveDuration(ctlv);
            }
            input.minLen = MSG_ID_LOAD_ICON_DONE;
            input.maxLen = MSG_ID_LOAD_ICON_DONE;
            if ((cmdDet.commandQualifier & MSG_ID_LOAD_ICON_DONE) == 0) {
                z = true;
            } else {
                z = false;
            }
            input.digitOnly = z;
            if ((cmdDet.commandQualifier & REFRESH_NAA_INIT_AND_FILE_CHANGE) != 0) {
                z = true;
            } else {
                z = false;
            }
            input.ucs2 = z;
            if ((cmdDet.commandQualifier & REFRESH_UICC_RESET) != 0) {
                z = true;
            } else {
                z = false;
            }
            input.yesNo = z;
            if ((cmdDet.commandQualifier & PduPart.P_Q) != 0) {
                z = true;
            } else {
                z = false;
            }
            input.helpAvailable = z;
            input.echo = true;
            this.mCmdParams = new GetInputParams(cmdDet, input);
            if (iconId != null) {
                if (this.stkSupportIcon) {
                    this.mloadIcon = true;
                    this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                    return true;
                }
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
            }
            return false;
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processGetInput(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process GetInput");
        Input input = new Input();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            input.text = ValueParser.retrieveTextString(ctlv);
            ctlv = searchForTag(ComprehensionTlvTag.RESPONSE_LENGTH, ctlvs);
            if (ctlv != null) {
                try {
                    boolean z;
                    byte[] rawValue = ctlv.getRawValue();
                    int valueIndex = ctlv.getValueIndex();
                    input.minLen = rawValue[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
                    input.maxLen = rawValue[valueIndex + MSG_ID_LOAD_ICON_DONE] & PduHeaders.STORE_STATUS_ERROR_END;
                    ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
                    if (ctlv != null) {
                        input.duration = ValueParser.retrieveDuration(ctlv);
                    }
                    ctlv = searchForTag(ComprehensionTlvTag.DEFAULT_TEXT, ctlvs);
                    if (ctlv != null) {
                        input.defaultText = ValueParser.retrieveTextString(ctlv);
                    }
                    ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
                    if (ctlv != null) {
                        iconId = ValueParser.retrieveIconId(ctlv);
                    }
                    if ((cmdDet.commandQualifier & MSG_ID_LOAD_ICON_DONE) == 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    input.digitOnly = z;
                    if ((cmdDet.commandQualifier & REFRESH_NAA_INIT_AND_FILE_CHANGE) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    input.ucs2 = z;
                    if ((cmdDet.commandQualifier & REFRESH_UICC_RESET) == 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    input.echo = z;
                    if ((cmdDet.commandQualifier & 8) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    input.packed = z;
                    if ((cmdDet.commandQualifier & PduPart.P_Q) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    input.helpAvailable = z;
                    if (input.ucs2 && input.maxLen > MAX_UCS2_CHARS) {
                        CatLog.d((Object) this, "UCS2: received maxLen = " + input.maxLen + ", truncating to " + MAX_UCS2_CHARS);
                        input.maxLen = MAX_UCS2_CHARS;
                    } else if (!input.packed && input.maxLen > MAX_GSM7_DEFAULT_CHARS) {
                        CatLog.d((Object) this, "GSM 7Bit Default: received maxLen = " + input.maxLen + ", truncating to " + MAX_GSM7_DEFAULT_CHARS);
                        input.maxLen = MAX_GSM7_DEFAULT_CHARS;
                    }
                    this.mCmdParams = new GetInputParams(cmdDet, input);
                    if (iconId != null) {
                        if (this.stkSupportIcon) {
                            this.mloadIcon = true;
                            this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                            this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                            return true;
                        }
                        CatLog.d((Object) this, "Close load icon feature.");
                        this.mCmdParams.mLoadIconFailed = true;
                    }
                    return false;
                } catch (IndexOutOfBoundsException e) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            }
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processRefresh(CommandDetails cmdDet, List<ComprehensionTlv> list) {
        CatLog.d((Object) this, "process Refresh");
        switch (cmdDet.commandQualifier) {
            case REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE /*0*/:
            case REFRESH_NAA_INIT_AND_FILE_CHANGE /*2*/:
            case REFRESH_NAA_INIT /*3*/:
            case REFRESH_UICC_RESET /*4*/:
                this.mCmdParams = new DisplayTextParams(cmdDet, null);
                break;
        }
        return false;
    }

    private boolean processSelectItem(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SelectItem");
        Menu menu = new Menu();
        IconId iconId = null;
        ItemsIconId itemsIconId = null;
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            menu.title = ValueParser.retrieveAlphaId(ctlv);
        }
        while (true) {
            ctlv = searchForNextTag(ComprehensionTlvTag.ITEM, iter);
            if (ctlv == null) {
                break;
            }
            menu.items.add(ValueParser.retrieveItem(ctlv));
        }
        if (menu.items.size() == 0) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        boolean presentTypeSpecified;
        boolean z;
        ctlv = searchForTag(ComprehensionTlvTag.ITEM_ID, ctlvs);
        if (ctlv != null) {
            menu.defaultItem = ValueParser.retrieveItemId(ctlv) - 1;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
            iconId = ValueParser.retrieveIconId(ctlv);
            menu.titleIconSelfExplanatory = iconId.selfExplanatory;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ITEM_ICON_ID_LIST, ctlvs);
        if (ctlv != null) {
            this.mIconLoadState = REFRESH_NAA_INIT_AND_FILE_CHANGE;
            itemsIconId = ValueParser.retrieveItemsIconId(ctlv);
            menu.itemsIconSelfExplanatory = itemsIconId.selfExplanatory;
        }
        if ((cmdDet.commandQualifier & MSG_ID_LOAD_ICON_DONE) != 0) {
            presentTypeSpecified = true;
        } else {
            presentTypeSpecified = false;
        }
        if (presentTypeSpecified) {
            if ((cmdDet.commandQualifier & REFRESH_NAA_INIT_AND_FILE_CHANGE) == 0) {
                menu.presentationType = PresentationType.DATA_VALUES;
            } else {
                menu.presentationType = PresentationType.NAVIGATION_OPTIONS;
            }
        }
        if ((cmdDet.commandQualifier & REFRESH_UICC_RESET) != 0) {
            z = true;
        } else {
            z = false;
        }
        menu.softKeyPreferred = z;
        if ((cmdDet.commandQualifier & PduPart.P_Q) != 0) {
            z = true;
        } else {
            z = false;
        }
        menu.helpAvailable = z;
        if (iconId != null) {
            z = true;
        } else {
            z = false;
        }
        this.mCmdParams = new SelectItemParams(cmdDet, menu, z);
        switch (this.mIconLoadState) {
            case REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE /*0*/:
                return false;
            case MSG_ID_LOAD_ICON_DONE /*1*/:
                if (this.stkSupportIcon) {
                    this.mloadIcon = true;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                    break;
                }
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
                return false;
            case REFRESH_NAA_INIT_AND_FILE_CHANGE /*2*/:
                if (this.stkSupportIcon) {
                    int[] recordNumbers = itemsIconId.recordNumbers;
                    if (iconId != null) {
                        recordNumbers = new int[(itemsIconId.recordNumbers.length + MSG_ID_LOAD_ICON_DONE)];
                        recordNumbers[REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE] = iconId.recordNumber;
                        System.arraycopy(itemsIconId.recordNumbers, REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE, recordNumbers, MSG_ID_LOAD_ICON_DONE, itemsIconId.recordNumbers.length);
                    }
                    this.mloadIcon = true;
                    this.mIconLoader.loadIcons(recordNumbers, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                    break;
                }
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
                return false;
        }
        return true;
    }

    private boolean processEventNotify(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process EventNotify");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        textMsg.text = ValueParser.retrieveAlphaId(searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs));
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        textMsg.responseNeeded = false;
        this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
        if (iconId != null) {
            if (this.stkSupportIcon) {
                this.mloadIcon = true;
                this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                return true;
            }
            CatLog.d((Object) this, "Close load icon feature.");
            this.mCmdParams.mLoadIconFailed = true;
        }
        return false;
    }

    private boolean processSetUpEventList(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) {
        CatLog.d((Object) this, "process SetUpEventList");
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.EVENT_LIST, ctlvs);
        if (ctlv != null) {
            try {
                byte[] rawValue = ctlv.getRawValue();
                int valueIndex = ctlv.getValueIndex();
                int valueLen = ctlv.getLength();
                int[] eventList = new int[valueLen];
                int i = REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE;
                while (valueLen > 0) {
                    int eventValue = rawValue[valueIndex] & PduHeaders.STORE_STATUS_ERROR_END;
                    valueIndex += MSG_ID_LOAD_ICON_DONE;
                    valueLen--;
                    switch (eventValue) {
                        case REFRESH_UICC_RESET /*4*/:
                        case CharacterSets.ISO_8859_2 /*5*/:
                        case CharacterSets.ISO_8859_4 /*7*/:
                        case CharacterSets.ISO_8859_5 /*8*/:
                        case CharacterSets.ISO_8859_6 /*9*/:
                        case CharacterSets.ISO_8859_7 /*10*/:
                        case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                            eventList[i] = eventValue;
                            i += MSG_ID_LOAD_ICON_DONE;
                            break;
                        default:
                            break;
                    }
                }
                this.mCmdParams = new SetEventListParams(cmdDet, eventList);
            } catch (IndexOutOfBoundsException e) {
                CatLog.e((Object) this, " IndexOutofBoundException in processSetUpEventList");
            }
        }
        return false;
    }

    private boolean processLaunchBrowser(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        LaunchBrowserMode mode;
        CatLog.d((Object) this, "process LaunchBrowser");
        TextMessage confirmMsg = new TextMessage();
        IconId iconId = null;
        String url = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.URL, ctlvs);
        if (ctlv != null) {
            try {
                byte[] rawValue = ctlv.getRawValue();
                int valueIndex = ctlv.getValueIndex();
                int valueLen = ctlv.getLength();
                if (valueLen > 0) {
                    url = HwTelephonyFactory.getHwTelephonyBaseManager().gsm8BitUnpackedToString(rawValue, valueIndex, valueLen, true);
                } else {
                    url = null;
                }
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        confirmMsg.text = ValueParser.retrieveAlphaId(searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs));
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            confirmMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        switch (cmdDet.commandQualifier) {
            case REFRESH_NAA_INIT_AND_FILE_CHANGE /*2*/:
                mode = LaunchBrowserMode.USE_EXISTING_BROWSER;
                break;
            case REFRESH_NAA_INIT /*3*/:
                mode = LaunchBrowserMode.LAUNCH_NEW_BROWSER;
                break;
            default:
                mode = LaunchBrowserMode.LAUNCH_IF_NOT_ALREADY_LAUNCHED;
                break;
        }
        this.mCmdParams = new LaunchBrowserParams(cmdDet, confirmMsg, url, mode);
        if (iconId != null) {
            if (this.stkSupportIcon) {
                this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                return true;
            }
            CatLog.d((Object) this, "Close load icon feature.");
            this.mCmdParams.mLoadIconFailed = true;
        }
        return false;
    }

    private boolean processPlayTone(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process PlayTone");
        Tone tone = null;
        TextMessage textMsg = new TextMessage();
        Duration duration = null;
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TONE, ctlvs);
        if (ctlv != null && ctlv.getLength() > 0) {
            try {
                tone = Tone.fromInt(ctlv.getRawValue()[ctlv.getValueIndex()]);
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveAlphaId(ctlv);
            if (textMsg.text == null) {
                textMsg.text = "";
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
        if (ctlv != null) {
            duration = ValueParser.retrieveDuration(ctlv);
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        boolean vibrate = (cmdDet.commandQualifier & MSG_ID_LOAD_ICON_DONE) != 0;
        textMsg.responseNeeded = false;
        this.mCmdParams = new PlayToneParams(cmdDet, textMsg, tone, duration, vibrate);
        if (iconId != null) {
            if (this.stkSupportIcon) {
                this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
                this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                return true;
            }
            CatLog.d((Object) this, "Close load icon feature.");
            this.mCmdParams.mLoadIconFailed = true;
        }
        return false;
    }

    private boolean processSetupCall(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetupCall");
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        TextMessage confirmMsg = new TextMessage();
        TextMessage callMsg = new TextMessage();
        IconId iconId = null;
        IconId iconId2 = null;
        TextMessage TempMsg = new TextMessage();
        IconId iconId3 = null;
        ComprehensionTlv ctlv = searchForNextTag(ComprehensionTlvTag.ALPHA_ID, iter);
        if (ctlv != null) {
            TempMsg.text = ValueParser.retrieveAlphaId(ctlv);
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId3 = ValueParser.retrieveIconId(ctlv);
            TempMsg.iconSelfExplanatory = iconId3.selfExplanatory;
        }
        if (searchForNextTag(ComprehensionTlvTag.ADDRESS, iter) != null) {
            CatLog.d((Object) this, "ADDRESS_ID parse entered");
            confirmMsg.text = TempMsg.text;
            iconId = iconId3;
            if (iconId3 != null) {
                confirmMsg.iconSelfExplanatory = iconId.selfExplanatory;
            }
            ctlv = searchForNextTag(ComprehensionTlvTag.ALPHA_ID, iter);
            if (ctlv != null) {
                callMsg.text = ValueParser.retrieveAlphaId(ctlv);
            }
            ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv != null) {
                iconId2 = ValueParser.retrieveIconId(ctlv);
                callMsg.iconSelfExplanatory = iconId2.selfExplanatory;
            }
        } else {
            callMsg.text = TempMsg.text;
            iconId2 = iconId3;
            if (iconId3 != null) {
                callMsg.iconSelfExplanatory = iconId2.selfExplanatory;
            }
        }
        CatLog.d((Object) this, "callMsg.text" + callMsg.text);
        CatLog.d((Object) this, "confirmMsg.text" + confirmMsg.text);
        this.mCmdParams = new CallSetupParams(cmdDet, confirmMsg, callMsg);
        if (!(iconId == null && iconId2 == null)) {
            if (this.stkSupportIcon) {
                int i;
                this.mIconLoadState = REFRESH_NAA_INIT_AND_FILE_CHANGE;
                int[] recordNumbers = new int[REFRESH_NAA_INIT_AND_FILE_CHANGE];
                recordNumbers[REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE] = iconId != null ? iconId.recordNumber : -1;
                if (iconId2 != null) {
                    i = iconId2.recordNumber;
                } else {
                    i = -1;
                }
                recordNumbers[MSG_ID_LOAD_ICON_DONE] = i;
                this.mIconLoader.loadIcons(recordNumbers, obtainMessage(MSG_ID_LOAD_ICON_DONE));
                return true;
            }
            CatLog.d((Object) this, "Close load icon feature.");
            this.mCmdParams.mLoadIconFailed = true;
        }
        return false;
    }

    private boolean processProvideLocalInfo(CommandDetails cmdDet, List<ComprehensionTlv> list) throws ResultException {
        CatLog.d((Object) this, "process ProvideLocalInfo");
        switch (cmdDet.commandQualifier) {
            case REFRESH_NAA_INIT /*3*/:
                CatLog.d((Object) this, "PLI [DTTZ_SETTING]");
                this.mCmdParams = new CommandParams(cmdDet);
                break;
            case REFRESH_UICC_RESET /*4*/:
                CatLog.d((Object) this, "PLI [LANGUAGE_SETTING]");
                this.mCmdParams = new CommandParams(cmdDet);
                break;
            default:
                CatLog.d((Object) this, "PLI[" + cmdDet.commandQualifier + "] Command Not Supported");
                this.mCmdParams = new CommandParams(cmdDet);
                throw new ResultException(ResultCode.BEYOND_TERMINAL_CAPABILITY);
        }
        return false;
    }

    private boolean processBIPClient(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CommandType commandType = CommandType.fromInt(cmdDet.typeOfCommand);
        if (commandType != null) {
            CatLog.d((Object) this, "process " + commandType.name());
        }
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        boolean has_alpha_id = false;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveAlphaId(ctlv);
            CatLog.d((Object) this, "alpha TLV text=" + textMsg.text);
            has_alpha_id = true;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        textMsg.responseNeeded = false;
        this.mCmdParams = new BIPClientParams(cmdDet, textMsg, has_alpha_id);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = MSG_ID_LOAD_ICON_DONE;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(MSG_ID_LOAD_ICON_DONE));
        return true;
    }

    public void dispose() {
        this.mIconLoader.dispose();
        this.mIconLoader = null;
        this.mCmdParams = null;
        this.mCaller = null;
        sInstance = null;
    }

    private boolean processOpenChannel(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process OpenChannel");
        TextMessage confirmMsg = new TextMessage();
        InterfaceTransportLevel interfaceTransportLevel = null;
        BearerDescription bearerDescription = null;
        byte[] destinationAddress = null;
        String networkAccessName = null;
        String userLogin = null;
        String userPassword = null;
        confirmMsg.responseNeeded = false;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            confirmMsg.text = ValueParser.retrieveBIPAlphaId(ctlv);
            if (confirmMsg.text != null) {
                confirmMsg.responseNeeded = true;
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            confirmMsg.iconSelfExplanatory = ValueParser.retrieveIconId(ctlv).selfExplanatory;
        }
        ctlv = searchForTag(ComprehensionTlvTag.BUFFER_SIZE, ctlvs);
        if (ctlv != null) {
            int bufSize = ValueParser.retrieveBufferSize(ctlv);
            Iterator<ComprehensionTlv> iter = ctlvs.iterator();
            ctlv = searchForNextTag(ComprehensionTlvTag.IF_TRANS_LEVEL, iter);
            if (ctlv != null) {
                interfaceTransportLevel = ValueParser.retrieveInterfaceTransportLevel(ctlv);
                ctlv = searchForNextTag(ComprehensionTlvTag.OTHER_ADDRESS, iter);
                if (ctlv != null) {
                    destinationAddress = ValueParser.retrieveOtherAddress(ctlv);
                }
            }
            ctlv = searchForTag(ComprehensionTlvTag.BEARER_DESC, ctlvs);
            if (ctlv != null) {
                bearerDescription = ValueParser.retrieveBearerDescription(ctlv);
                CatLog.d((Object) this, "processOpenChannel bearer: " + bearerDescription.type.value() + " param.len: " + bearerDescription.parameters.length);
            }
            ctlv = searchForNextTag(ComprehensionTlvTag.NETWORK_ACCESS_NAME, ctlvs.iterator());
            if (ctlv != null) {
                networkAccessName = ValueParser.retrieveNetworkAccessName(ctlv);
            }
            iter = ctlvs.iterator();
            ctlv = searchForNextTag(ComprehensionTlvTag.TEXT_STRING, iter);
            if (ctlv != null) {
                userLogin = ValueParser.retrieveTextString(ctlv);
            }
            ctlv = searchForNextTag(ComprehensionTlvTag.TEXT_STRING, iter);
            if (ctlv != null) {
                userPassword = ValueParser.retrieveTextString(ctlv);
            }
            if (interfaceTransportLevel == null || bearerDescription != null) {
                if (bearerDescription == null) {
                    throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                } else if (bearerDescription.type != BearerType.DEFAULT_BEARER && bearerDescription.type != BearerType.MOBILE_PS && bearerDescription.type != BearerType.MOBILE_PS_EXTENDED_QOS && bearerDescription.type != BearerType.E_UTRAN) {
                    throw new ResultException(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                } else if (interfaceTransportLevel == null) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                } else if (interfaceTransportLevel.protocol != TransportProtocol.TCP_CLIENT_REMOTE && interfaceTransportLevel.protocol != TransportProtocol.UDP_CLIENT_REMOTE) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                } else if (destinationAddress == null) {
                    throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                }
            } else if (!(interfaceTransportLevel.protocol == TransportProtocol.TCP_SERVER || interfaceTransportLevel.protocol == TransportProtocol.TCP_CLIENT_LOCAL || interfaceTransportLevel.protocol == TransportProtocol.UDP_CLIENT_LOCAL)) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
            CatLog.d((Object) this, "processOpenChannel bufSize=" + bufSize + " protocol=" + interfaceTransportLevel.protocol + " APN=" + (networkAccessName != null ? networkAccessName : "undefined") + " user/password=" + (userLogin != null ? userLogin : "---") + "/" + (userPassword != null ? userPassword : "---"));
            this.mCmdParams = new OpenChannelParams(cmdDet, confirmMsg, bufSize, interfaceTransportLevel, destinationAddress, bearerDescription, networkAccessName, userLogin, userPassword);
            return false;
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processCloseChannel(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process CloseChannel");
        TextMessage alertMsg = new TextMessage();
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.DEVICE_IDENTITIES, ctlvs);
        if (ctlv != null) {
            int channel = ValueParser.retrieveDeviceIdentities(ctlv).destinationId;
            if (channel < 33 || channel > 39) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
            channel &= 15;
            ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
            if (ctlv != null) {
                alertMsg.text = ValueParser.retrieveBIPAlphaId(ctlv);
            }
            ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv != null) {
                alertMsg.iconSelfExplanatory = ValueParser.retrieveIconId(ctlv).selfExplanatory;
            }
            this.mCmdParams = new CloseChannelParams(cmdDet, alertMsg, channel);
            return false;
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processReceiveData(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process ReceiveData");
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.DEVICE_IDENTITIES, ctlvs);
        if (ctlv != null) {
            int channel = ValueParser.retrieveDeviceIdentities(ctlv).destinationId;
            if (channel < 33 || channel > 39) {
                CatLog.d((Object) this, "Invalid Channel number given: " + channel);
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
            channel &= 15;
            TextMessage textMessage = null;
            ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
            if (ctlv != null) {
                textMessage = new TextMessage();
                textMessage.text = ValueParser.retrieveBIPAlphaId(ctlv);
                textMessage.responseNeeded = false;
            }
            ctlv = searchForTag(ComprehensionTlvTag.CHANNEL_DATA_LENGTH, ctlvs);
            if (ctlv != null) {
                this.mCmdParams = new ReceiveDataParams(cmdDet, channel, ValueParser.retrieveChannelDataLength(ctlv), textMessage);
                return false;
            }
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processSendData(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SendData");
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.DEVICE_IDENTITIES, ctlvs);
        if (ctlv != null) {
            int channel = ValueParser.retrieveDeviceIdentities(ctlv).destinationId;
            if (channel < 33 || channel > 39) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
            channel &= 15;
            TextMessage textMessage = null;
            ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
            if (ctlv != null) {
                textMessage = new TextMessage();
                textMessage.text = ValueParser.retrieveBIPAlphaId(ctlv);
                textMessage.responseNeeded = false;
            }
            ctlv = searchForTag(ComprehensionTlvTag.CHANNEL_DATA, ctlvs);
            if (ctlv != null) {
                this.mCmdParams = new SendDataParams(cmdDet, channel, ValueParser.retrieveChannelData(ctlv), textMessage);
                return false;
            }
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processGetChannelStatus(CommandDetails cmdDet, List<ComprehensionTlv> list) throws ResultException {
        CatLog.d((Object) this, "process GetChannelStatus");
        this.mCmdParams = new GetChannelStatusParams(cmdDet);
        return false;
    }
}
