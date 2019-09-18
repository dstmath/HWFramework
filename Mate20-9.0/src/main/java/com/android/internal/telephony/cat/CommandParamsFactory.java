package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.BearerDescription;
import com.android.internal.telephony.cat.InterfaceTransportLevel;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CommandParamsFactory extends AbstractCommandParamsFactory {
    static final int DTTZ_SETTING = 3;
    static final int LANGUAGE_SETTING = 4;
    static final int LOAD_MULTI_ICONS = 2;
    static final int LOAD_NO_ICON = 0;
    static final int LOAD_SINGLE_ICON = 1;
    private static final int MAX_GSM7_DEFAULT_CHARS = 239;
    private static final int MAX_UCS2_CHARS = 118;
    static final int MSG_ID_LOAD_ICON_DONE = 1;
    static final int NON_SPECIFIC_LANGUAGE = 0;
    static final int REFRESH_FILE_CHANGE_NOTIFICATION = 1;
    static final int REFRESH_NAA_INIT = 3;
    static final int REFRESH_NAA_INIT_AND_FILE_CHANGE = 2;
    static final int REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE = 0;
    static final int REFRESH_UICC_RESET = 4;
    static final int SPECIFIC_LANGUAGE = 1;
    private static CommandParamsFactory sInstance = null;
    private RilMessageDecoder mCaller = null;
    private CommandParams mCmdParams = null;
    private int mIconLoadState = 0;
    private IconLoader mIconLoader;
    private String mRequestedLanguage;
    private String mSavedLanguage;
    private boolean mloadIcon = false;
    private boolean stkSupportIcon = SystemProperties.getBoolean("ro.config.hw_stk_icon", false);

    static synchronized CommandParamsFactory getInstance(RilMessageDecoder caller, IccFileHandler fh) {
        synchronized (CommandParamsFactory.class) {
            if (sInstance != null) {
                CommandParamsFactory commandParamsFactory = sInstance;
                return commandParamsFactory;
            } else if (fh == null) {
                return null;
            } else {
                CommandParamsFactory commandParamsFactory2 = new CommandParamsFactory(caller, fh);
                return commandParamsFactory2;
            }
        }
    }

    private CommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh) {
        this.mCaller = caller;
        this.mIconLoader = IconLoader.getInstance(this, fh);
    }

    private CommandDetails processCommandDetails(List<ComprehensionTlv> ctlvs) {
        if (ctlvs == null) {
            return null;
        }
        ComprehensionTlv ctlvCmdDet = searchForTag(ComprehensionTlvTag.COMMAND_DETAILS, ctlvs);
        if (ctlvCmdDet == null) {
            return null;
        }
        try {
            return ValueParser.retrieveCommandDetails(ctlvCmdDet);
        } catch (ResultException e) {
            CatLog.d((Object) this, "processCommandDetails: Failed to procees command details e=" + e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void make(BerTlv berTlv) {
        if (berTlv != null) {
            this.mCmdParams = null;
            this.mIconLoadState = 0;
            if (berTlv.getTag() != 208) {
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
            AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
            if (cmdType == null) {
                this.mCmdParams = new CommandParams(cmdDet);
                sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
            } else if (!berTlv.isLengthValid()) {
                this.mCmdParams = new CommandParams(cmdDet);
                sendCmdParams(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            } else {
                try {
                    switch (cmdType) {
                        case SET_UP_MENU:
                            cmdPending = processSelectItem(cmdDet, ctlvs);
                            break;
                        case SELECT_ITEM:
                            cmdPending = processSelectItem(cmdDet, ctlvs);
                            break;
                        case DISPLAY_TEXT:
                            cmdPending = processDisplayText(cmdDet, ctlvs);
                            break;
                        case SET_UP_IDLE_MODE_TEXT:
                            cmdPending = processSetUpIdleModeText(cmdDet, ctlvs);
                            break;
                        case GET_INKEY:
                            cmdPending = processGetInkey(cmdDet, ctlvs);
                            break;
                        case GET_INPUT:
                            cmdPending = processGetInput(cmdDet, ctlvs);
                            break;
                        case SEND_DTMF:
                        case SEND_SMS:
                        case SEND_SS:
                        case SEND_USSD:
                            cmdPending = processEventNotify(cmdDet, ctlvs);
                            break;
                        case GET_CHANNEL_STATUS:
                            if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                                cmdPending = processGetChannelStatus(cmdDet, ctlvs);
                                break;
                            }
                        case SET_UP_CALL:
                            cmdPending = processSetupCall(cmdDet, ctlvs);
                            break;
                        case REFRESH:
                            processRefresh(cmdDet, ctlvs);
                            cmdPending = false;
                            break;
                        case LAUNCH_BROWSER:
                            cmdPending = processLaunchBrowser(cmdDet, ctlvs);
                            break;
                        case PLAY_TONE:
                            cmdPending = processPlayTone(cmdDet, ctlvs);
                            break;
                        case SET_UP_EVENT_LIST:
                            cmdPending = processSetUpEventList(cmdDet, ctlvs);
                            break;
                        case PROVIDE_LOCAL_INFORMATION:
                            cmdPending = processProvideLocalInfo(cmdDet, ctlvs);
                            break;
                        case LANGUAGE_NOTIFICATION:
                            cmdPending = processLanguageNotification(cmdDet, ctlvs);
                            break;
                        case OPEN_CHANNEL:
                        case CLOSE_CHANNEL:
                        case RECEIVE_DATA:
                        case SEND_DATA:
                            if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                                if (cmdType != AppInterface.CommandType.OPEN_CHANNEL) {
                                    if (cmdType != AppInterface.CommandType.CLOSE_CHANNEL) {
                                        if (cmdType != AppInterface.CommandType.RECEIVE_DATA) {
                                            if (cmdType == AppInterface.CommandType.SEND_DATA) {
                                                cmdPending = processSendData(cmdDet, ctlvs);
                                                break;
                                            }
                                        } else {
                                            cmdPending = processReceiveData(cmdDet, ctlvs);
                                            break;
                                        }
                                    } else {
                                        cmdPending = processCloseChannel(cmdDet, ctlvs);
                                        break;
                                    }
                                } else {
                                    cmdPending = processOpenChannel(cmdDet, ctlvs);
                                    break;
                                }
                            } else {
                                cmdPending = processBIPClient(cmdDet, ctlvs);
                                break;
                            }
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
            }
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1 && this.mIconLoader != null) {
            sendCmdParams(setIcons(msg.obj));
        }
    }

    private ResultCode setIcons(Object data) {
        if (data == null) {
            CatLog.d((Object) this, "Optional Icon data is NULL");
            this.mCmdParams.mLoadIconFailed = true;
            this.mloadIcon = false;
            return ResultCode.OK;
        }
        switch (this.mIconLoadState) {
            case 1:
                this.mCmdParams.setIcon((Bitmap) data);
                break;
            case 2:
                for (Bitmap icon : (Bitmap[]) data) {
                    this.mCmdParams.setIcon(icon);
                    if (icon == null && this.mloadIcon) {
                        CatLog.d((Object) this, "Optional Icon data is NULL while loading multi icons");
                        this.mCmdParams.mLoadIconFailed = true;
                    }
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
            ComprehensionTlv ctlv = iter.next();
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
        if (textMsg.text != null) {
            if (searchForTag(ComprehensionTlvTag.IMMEDIATE_RESPONSE, ctlvs) != null) {
                textMsg.responseNeeded = false;
            }
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv2 != null) {
                iconId = ValueParser.retrieveIconId(ctlv2);
                textMsg.iconSelfExplanatory = iconId.selfExplanatory;
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
            if (ctlv3 != null) {
                textMsg.duration = ValueParser.retrieveDuration(ctlv3);
            }
            textMsg.isHighPriority = (cmdDet.commandQualifier & 1) != 0;
            textMsg.userClear = (cmdDet.commandQualifier & 128) != 0;
            this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
            if (iconId != null) {
                if (!this.stkSupportIcon) {
                    CatLog.d((Object) this, "Close load icon feature.");
                    this.mCmdParams.mLoadIconFailed = true;
                } else {
                    this.mloadIcon = true;
                    this.mIconLoadState = 1;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                    return true;
                }
            }
            return false;
        }
        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
    }

    private boolean processSetUpIdleModeText(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetUpIdleModeText");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveTextString(ctlv);
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            iconId = ValueParser.retrieveIconId(ctlv2);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        if (textMsg.text != null || iconId == null || textMsg.iconSelfExplanatory) {
            this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
            if (iconId != null) {
                if (!this.stkSupportIcon) {
                    CatLog.d((Object) this, "Close load icon feature.");
                    this.mCmdParams.mLoadIconFailed = true;
                } else {
                    this.mloadIcon = true;
                    this.mIconLoadState = 1;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                    return true;
                }
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
            input.text = ValueParser.retrieveTextString(ctlv);
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv2 != null) {
                iconId = ValueParser.retrieveIconId(ctlv2);
                input.iconSelfExplanatory = iconId.selfExplanatory;
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
            if (ctlv3 != null) {
                input.duration = ValueParser.retrieveDuration(ctlv3);
            }
            input.minLen = 1;
            input.maxLen = 1;
            input.digitOnly = (cmdDet.commandQualifier & 1) == 0;
            input.ucs2 = (cmdDet.commandQualifier & 2) != 0;
            input.yesNo = (cmdDet.commandQualifier & 4) != 0;
            input.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
            input.echo = true;
            this.mCmdParams = new GetInputParams(cmdDet, input);
            if (iconId != null) {
                if (!this.stkSupportIcon) {
                    CatLog.d((Object) this, "Close load icon feature.");
                    this.mCmdParams.mLoadIconFailed = true;
                } else {
                    this.mloadIcon = true;
                    this.mIconLoadState = 1;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                    return true;
                }
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
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.RESPONSE_LENGTH, ctlvs);
            if (ctlv2 != null) {
                try {
                    byte[] rawValue = ctlv2.getRawValue();
                    int valueIndex = ctlv2.getValueIndex();
                    input.minLen = rawValue[valueIndex] & 255;
                    input.maxLen = rawValue[valueIndex + 1] & 255;
                    ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
                    if (ctlv3 != null) {
                        input.duration = ValueParser.retrieveDuration(ctlv3);
                    }
                    ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.DEFAULT_TEXT, ctlvs);
                    if (ctlv4 != null) {
                        input.defaultText = ValueParser.retrieveTextString(ctlv4);
                    }
                    ComprehensionTlv ctlv5 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
                    if (ctlv5 != null) {
                        iconId = ValueParser.retrieveIconId(ctlv5);
                        input.iconSelfExplanatory = iconId.selfExplanatory;
                    }
                    ComprehensionTlv ctlv6 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
                    if (ctlv6 != null) {
                        input.duration = ValueParser.retrieveDuration(ctlv6);
                    }
                    input.digitOnly = (cmdDet.commandQualifier & 1) == 0;
                    input.ucs2 = (cmdDet.commandQualifier & 2) != 0;
                    input.echo = (cmdDet.commandQualifier & 4) == 0;
                    input.packed = (cmdDet.commandQualifier & 8) != 0;
                    input.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
                    if (input.ucs2 && input.maxLen > 118) {
                        CatLog.d((Object) this, "UCS2: received maxLen = " + input.maxLen + ", truncating to " + 118);
                        input.maxLen = 118;
                    } else if (!input.packed && input.maxLen > MAX_GSM7_DEFAULT_CHARS) {
                        CatLog.d((Object) this, "GSM 7Bit Default: received maxLen = " + input.maxLen + ", truncating to " + MAX_GSM7_DEFAULT_CHARS);
                        input.maxLen = MAX_GSM7_DEFAULT_CHARS;
                    }
                    this.mCmdParams = new GetInputParams(cmdDet, input);
                    if (iconId != null) {
                        if (!this.stkSupportIcon) {
                            CatLog.d((Object) this, "Close load icon feature.");
                            this.mCmdParams.mLoadIconFailed = true;
                        } else {
                            this.mloadIcon = true;
                            this.mIconLoadState = 1;
                            this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                            return true;
                        }
                    }
                    return false;
                } catch (IndexOutOfBoundsException e) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } else {
                throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
            }
        } else {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
    }

    private boolean processRefresh(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) {
        CatLog.d((Object) this, "process Refresh");
        switch (cmdDet.commandQualifier) {
            case 0:
            case 2:
            case 3:
            case 4:
                this.mCmdParams = new DisplayTextParams(cmdDet, null);
                break;
            case 1:
                processFileChangeNotification(cmdDet, ctlvs);
                break;
        }
        return false;
    }

    private boolean processSelectItem(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SelectItem");
        Menu menu = new Menu();
        IconId titleIconId = null;
        ItemsIconId itemsIconId = null;
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            menu.title = ValueParser.retrieveAlphaId(ctlv);
        } else if (cmdType == AppInterface.CommandType.SET_UP_MENU) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        while (true) {
            ComprehensionTlv ctlv2 = searchForNextTag(ComprehensionTlvTag.ITEM, iter);
            if (ctlv2 == null) {
                break;
            }
            menu.items.add(ValueParser.retrieveItem(ctlv2));
        }
        if (menu.items.size() != 0) {
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ITEM_ID, ctlvs);
            if (ctlv3 != null) {
                menu.defaultItem = ValueParser.retrieveItemId(ctlv3) - 1;
            }
            ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv4 != null) {
                this.mIconLoadState = 1;
                titleIconId = ValueParser.retrieveIconId(ctlv4);
                menu.titleIconSelfExplanatory = titleIconId.selfExplanatory;
            }
            ComprehensionTlv ctlv5 = searchForTag(ComprehensionTlvTag.ITEM_ICON_ID_LIST, ctlvs);
            if (ctlv5 != null) {
                this.mIconLoadState = 2;
                itemsIconId = ValueParser.retrieveItemsIconId(ctlv5);
                menu.itemsIconSelfExplanatory = itemsIconId.selfExplanatory;
            }
            if ((cmdDet.commandQualifier & 1) != 0) {
                if ((2 & cmdDet.commandQualifier) == 0) {
                    menu.presentationType = PresentationType.DATA_VALUES;
                } else {
                    menu.presentationType = PresentationType.NAVIGATION_OPTIONS;
                }
            }
            menu.softKeyPreferred = (cmdDet.commandQualifier & 4) != 0;
            menu.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
            this.mCmdParams = new SelectItemParams(cmdDet, menu, titleIconId != null);
            switch (this.mIconLoadState) {
                case 0:
                    return false;
                case 1:
                    if (this.stkSupportIcon) {
                        this.mloadIcon = true;
                        this.mIconLoader.loadIcon(titleIconId.recordNumber, obtainMessage(1));
                        break;
                    } else {
                        CatLog.d((Object) this, "Close load icon feature.");
                        this.mCmdParams.mLoadIconFailed = true;
                        return false;
                    }
                case 2:
                    if (this.stkSupportIcon) {
                        int[] recordNumbers = itemsIconId.recordNumbers;
                        if (titleIconId != null) {
                            recordNumbers = new int[(itemsIconId.recordNumbers.length + 1)];
                            recordNumbers[0] = titleIconId.recordNumber;
                            System.arraycopy(itemsIconId.recordNumbers, 0, recordNumbers, 1, itemsIconId.recordNumbers.length);
                        }
                        this.mloadIcon = true;
                        this.mIconLoader.loadIcons(recordNumbers, obtainMessage(1));
                        break;
                    } else {
                        CatLog.d((Object) this, "Close load icon feature.");
                        this.mCmdParams.mLoadIconFailed = true;
                        return false;
                    }
            }
            return true;
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
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
            if (!this.stkSupportIcon) {
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
            } else {
                this.mloadIcon = true;
                this.mIconLoadState = 1;
                this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                return true;
            }
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
                int valueIndex2 = valueIndex;
                int i = 0;
                while (valueLen > 0) {
                    int eventValue = rawValue[valueIndex2] & 255;
                    valueIndex2++;
                    valueLen--;
                    if (eventValue != 15) {
                        switch (eventValue) {
                            case 4:
                            case 5:
                                break;
                            default:
                                switch (eventValue) {
                                    case 7:
                                    case 8:
                                    case 9:
                                    case 10:
                                        break;
                                    default:
                                        continue;
                                        continue;
                                }
                        }
                    }
                    eventList[i] = eventValue;
                    i++;
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
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            iconId = ValueParser.retrieveIconId(ctlv2);
            confirmMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        switch (cmdDet.commandQualifier) {
            case 2:
                mode = LaunchBrowserMode.USE_EXISTING_BROWSER;
                break;
            case 3:
                mode = LaunchBrowserMode.LAUNCH_NEW_BROWSER;
                break;
            default:
                mode = LaunchBrowserMode.LAUNCH_IF_NOT_ALREADY_LAUNCHED;
                break;
        }
        this.mCmdParams = new LaunchBrowserParams(cmdDet, confirmMsg, url, mode);
        if (iconId != null) {
            if (!this.stkSupportIcon) {
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
            } else {
                this.mIconLoadState = 1;
                this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                return true;
            }
        }
        return false;
    }

    private boolean processPlayTone(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        List<ComprehensionTlv> list = ctlvs;
        CatLog.d((Object) this, "process PlayTone");
        Tone tone = null;
        TextMessage textMsg = new TextMessage();
        Duration duration = null;
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TONE, list);
        if (ctlv != null && ctlv.getLength() > 0) {
            try {
                tone = Tone.fromInt(ctlv.getRawValue()[ctlv.getValueIndex()]);
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, list);
        if (ctlv2 != null) {
            textMsg.text = ValueParser.retrieveAlphaId(ctlv2);
            if (textMsg.text == null) {
                textMsg.text = "";
            }
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DURATION, list);
        if (ctlv3 != null) {
            duration = ValueParser.retrieveDuration(ctlv3);
        }
        Duration duration2 = duration;
        ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.ICON_ID, list);
        if (ctlv4 != null) {
            iconId = ValueParser.retrieveIconId(ctlv4);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        IconId iconId2 = iconId;
        CommandDetails commandDetails = cmdDet;
        boolean vibrate = (commandDetails.commandQualifier & 1) != 0;
        textMsg.responseNeeded = false;
        PlayToneParams playToneParams = r4;
        PlayToneParams playToneParams2 = new PlayToneParams(commandDetails, textMsg, tone, duration2, vibrate);
        this.mCmdParams = playToneParams;
        if (iconId2 != null) {
            if (!this.stkSupportIcon) {
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
            } else {
                this.mIconLoadState = 1;
                this.mIconLoader.loadIcon(iconId2.recordNumber, obtainMessage(1));
                return true;
            }
        }
        return false;
    }

    private boolean processSetupCall(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetupCall");
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        TextMessage confirmMsg = new TextMessage();
        TextMessage callMsg = new TextMessage();
        IconId confirmIconId = null;
        IconId callIconId = null;
        TextMessage TempMsg = new TextMessage();
        IconId tempIconId = null;
        ComprehensionTlv ctlv = searchForNextTag(ComprehensionTlvTag.ALPHA_ID, iter);
        if (ctlv != null) {
            TempMsg.text = ValueParser.retrieveAlphaId(ctlv);
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            tempIconId = ValueParser.retrieveIconId(ctlv2);
            TempMsg.iconSelfExplanatory = tempIconId.selfExplanatory;
        }
        if (searchForNextTag(ComprehensionTlvTag.ADDRESS, iter) != null) {
            CatLog.d((Object) this, "ADDRESS_ID parse entered");
            confirmMsg.text = TempMsg.text;
            confirmIconId = tempIconId;
            if (confirmIconId != null) {
                confirmMsg.iconSelfExplanatory = confirmIconId.selfExplanatory;
            }
            ComprehensionTlv ctlv3 = searchForNextTag(ComprehensionTlvTag.ALPHA_ID, iter);
            if (ctlv3 != null) {
                callMsg.text = ValueParser.retrieveAlphaId(ctlv3);
            }
            ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv4 != null) {
                callIconId = ValueParser.retrieveIconId(ctlv4);
                callMsg.iconSelfExplanatory = callIconId.selfExplanatory;
            }
        } else {
            callMsg.text = TempMsg.text;
            callIconId = tempIconId;
            if (callIconId != null) {
                callMsg.iconSelfExplanatory = callIconId.selfExplanatory;
            }
        }
        CatLog.d((Object) this, "callMsg.text" + callMsg.text);
        CatLog.d((Object) this, "confirmMsg.text" + confirmMsg.text);
        this.mCmdParams = new CallSetupParams(cmdDet, confirmMsg, callMsg);
        if (!(confirmIconId == null && callIconId == null)) {
            if (!this.stkSupportIcon) {
                CatLog.d((Object) this, "Close load icon feature.");
                this.mCmdParams.mLoadIconFailed = true;
            } else {
                this.mIconLoadState = 2;
                int[] recordNumbers = new int[2];
                int i = -1;
                recordNumbers[0] = confirmIconId != null ? confirmIconId.recordNumber : -1;
                if (callIconId != null) {
                    i = callIconId.recordNumber;
                }
                recordNumbers[1] = i;
                this.mIconLoader.loadIcons(recordNumbers, obtainMessage(1));
                return true;
            }
        }
        return false;
    }

    private boolean processProvideLocalInfo(CommandDetails cmdDet, List<ComprehensionTlv> list) throws ResultException {
        CatLog.d((Object) this, "process ProvideLocalInfo");
        switch (cmdDet.commandQualifier) {
            case 3:
                CatLog.d((Object) this, "PLI [DTTZ_SETTING]");
                this.mCmdParams = new CommandParams(cmdDet);
                break;
            case 4:
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

    public boolean processLanguageNotification(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process Language Notification");
        String desiredLanguage = null;
        String currentLanguage = Locale.getDefault().getLanguage();
        switch (cmdDet.commandQualifier) {
            case 0:
                if (!TextUtils.isEmpty(this.mSavedLanguage) && !TextUtils.isEmpty(this.mRequestedLanguage) && this.mRequestedLanguage.equals(currentLanguage)) {
                    CatLog.d((Object) this, "Non-specific language notification changes the language setting back to " + this.mSavedLanguage);
                    desiredLanguage = this.mSavedLanguage;
                }
                this.mSavedLanguage = null;
                this.mRequestedLanguage = null;
                break;
            case 1:
                ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.LANGUAGE, ctlvs);
                if (ctlv != null) {
                    if (ctlv.getLength() == 2) {
                        desiredLanguage = GsmAlphabet.gsm8BitUnpackedToString(ctlv.getRawValue(), ctlv.getValueIndex(), 2);
                        if (TextUtils.isEmpty(this.mSavedLanguage) || (!TextUtils.isEmpty(this.mRequestedLanguage) && !this.mRequestedLanguage.equals(currentLanguage))) {
                            this.mSavedLanguage = currentLanguage;
                        }
                        this.mRequestedLanguage = desiredLanguage;
                        CatLog.d((Object) this, "Specific language notification changes the language setting to " + this.mRequestedLanguage);
                        break;
                    } else {
                        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                    }
                }
                break;
            default:
                CatLog.d((Object) this, "LN[" + cmdDet.commandQualifier + "] Command Not Supported");
                break;
        }
        this.mCmdParams = new LanguageParams(cmdDet, desiredLanguage);
        return false;
    }

    private boolean processBIPClient(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        AppInterface.CommandType commandType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
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
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            iconId = ValueParser.retrieveIconId(ctlv2);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        textMsg.responseNeeded = false;
        this.mCmdParams = new BIPClientParams(cmdDet, textMsg, has_alpha_id);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    public void dispose() {
        if (this.mIconLoader != null) {
            this.mIconLoader.dispose();
        }
        this.mIconLoader = null;
        this.mCmdParams = null;
        this.mCaller = null;
        sInstance = null;
    }

    private boolean processOpenChannel(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        BearerDescription bearerDescription;
        String networkAccessName;
        String userLogin;
        String userPassword;
        BearerDescription bearerDescription2;
        List<ComprehensionTlv> list = ctlvs;
        CatLog.d((Object) this, "process OpenChannel");
        TextMessage confirmMsg = new TextMessage();
        IconId confirmIconId = null;
        InterfaceTransportLevel itl = null;
        byte[] destinationAddress = null;
        confirmMsg.responseNeeded = false;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, list);
        if (ctlv != null) {
            confirmMsg.text = ValueParser.retrieveBIPAlphaId(ctlv);
            if (confirmMsg.text != null) {
                confirmMsg.responseNeeded = true;
            }
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, list);
        if (ctlv2 != null) {
            confirmIconId = ValueParser.retrieveIconId(ctlv2);
            confirmMsg.iconSelfExplanatory = confirmIconId.selfExplanatory;
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.BUFFER_SIZE, list);
        if (ctlv3 != null) {
            int bufSize = ValueParser.retrieveBufferSize(ctlv3);
            Iterator<ComprehensionTlv> iter = ctlvs.iterator();
            ComprehensionTlv ctlv4 = searchForNextTag(ComprehensionTlvTag.IF_TRANS_LEVEL, iter);
            if (ctlv4 != null) {
                itl = ValueParser.retrieveInterfaceTransportLevel(ctlv4);
                ComprehensionTlv ctlv5 = searchForNextTag(ComprehensionTlvTag.OTHER_ADDRESS, iter);
                if (ctlv5 != null) {
                    destinationAddress = ValueParser.retrieveOtherAddress(ctlv5);
                }
            }
            InterfaceTransportLevel itl2 = itl;
            byte[] destinationAddress2 = destinationAddress;
            ComprehensionTlv ctlv6 = searchForTag(ComprehensionTlvTag.BEARER_DESC, list);
            if (ctlv6 != null) {
                CatLog.d((Object) this, "processOpenChannel bearer: " + bearerDescription2.type.value() + " param.len: " + ValueParser.retrieveBearerDescription(ctlv6).parameters.length);
                bearerDescription = bearerDescription2;
            } else {
                bearerDescription = null;
            }
            ComprehensionTlv ctlv7 = searchForNextTag(ComprehensionTlvTag.NETWORK_ACCESS_NAME, ctlvs.iterator());
            if (ctlv7 != null) {
                networkAccessName = ValueParser.retrieveNetworkAccessName(ctlv7);
            } else {
                networkAccessName = null;
            }
            Iterator<ComprehensionTlv> iter2 = ctlvs.iterator();
            ComprehensionTlv ctlv8 = searchForNextTag(ComprehensionTlvTag.TEXT_STRING, iter2);
            if (ctlv8 != null) {
                userLogin = ValueParser.retrieveTextString(ctlv8);
            } else {
                userLogin = null;
            }
            ComprehensionTlv ctlv9 = searchForNextTag(ComprehensionTlvTag.TEXT_STRING, iter2);
            if (ctlv9 != null) {
                userPassword = ValueParser.retrieveTextString(ctlv9);
            } else {
                userPassword = null;
            }
            if (itl2 == null || bearerDescription != null) {
                if (bearerDescription == null) {
                    ComprehensionTlv comprehensionTlv = ctlv9;
                    BearerDescription bearerDescription3 = bearerDescription;
                    InterfaceTransportLevel interfaceTransportLevel = itl2;
                    throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                } else if (bearerDescription.type != BearerDescription.BearerType.DEFAULT_BEARER && bearerDescription.type != BearerDescription.BearerType.MOBILE_PS && bearerDescription.type != BearerDescription.BearerType.MOBILE_PS_EXTENDED_QOS && bearerDescription.type != BearerDescription.BearerType.E_UTRAN) {
                    throw new ResultException(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                } else if (itl2 == null) {
                    ComprehensionTlv comprehensionTlv2 = ctlv9;
                    BearerDescription bearerDescription4 = bearerDescription;
                    InterfaceTransportLevel interfaceTransportLevel2 = itl2;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                } else if (itl2.protocol != InterfaceTransportLevel.TransportProtocol.TCP_CLIENT_REMOTE && itl2.protocol != InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_REMOTE) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                } else if (destinationAddress2 == null) {
                    ComprehensionTlv comprehensionTlv3 = ctlv9;
                    BearerDescription bearerDescription5 = bearerDescription;
                    InterfaceTransportLevel interfaceTransportLevel3 = itl2;
                    throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                }
            } else if (!(itl2.protocol == InterfaceTransportLevel.TransportProtocol.TCP_SERVER || itl2.protocol == InterfaceTransportLevel.TransportProtocol.TCP_CLIENT_LOCAL || itl2.protocol == InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_LOCAL)) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("processOpenChannel bufSize=");
            sb.append(bufSize);
            sb.append(" protocol=");
            sb.append(itl2.protocol);
            sb.append(" APN=");
            sb.append(networkAccessName != null ? networkAccessName : "undefined");
            sb.append(" user/password=");
            sb.append(userLogin != null ? userLogin : "---");
            sb.append("/");
            sb.append(userPassword != null ? userPassword : "---");
            String msg = sb.toString();
            CatLog.d((Object) this, msg);
            OpenChannelParams openChannelParams = r3;
            Iterator<ComprehensionTlv> it = iter2;
            ComprehensionTlv comprehensionTlv4 = ctlv9;
            String str = msg;
            BearerDescription bearerDescription6 = bearerDescription;
            InterfaceTransportLevel interfaceTransportLevel4 = itl2;
            OpenChannelParams openChannelParams2 = new OpenChannelParams(cmdDet, confirmMsg, bufSize, itl2, destinationAddress2, bearerDescription, networkAccessName, userLogin, userPassword);
            this.mCmdParams = openChannelParams;
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
            int channel2 = channel & 15;
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
            if (ctlv2 != null) {
                alertMsg.text = ValueParser.retrieveBIPAlphaId(ctlv2);
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv3 != null) {
                alertMsg.iconSelfExplanatory = ValueParser.retrieveIconId(ctlv3).selfExplanatory;
            }
            this.mCmdParams = new CloseChannelParams(cmdDet, alertMsg, channel2);
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
            int channel2 = channel & 15;
            TextMessage textMsg = null;
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
            if (ctlv2 != null) {
                textMsg = new TextMessage();
                textMsg.text = ValueParser.retrieveBIPAlphaId(ctlv2);
                textMsg.responseNeeded = false;
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.CHANNEL_DATA_LENGTH, ctlvs);
            if (ctlv3 != null) {
                this.mCmdParams = new ReceiveDataParams(cmdDet, channel2, ValueParser.retrieveChannelDataLength(ctlv3), textMsg);
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
            int channel2 = channel & 15;
            TextMessage textMsg = null;
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
            if (ctlv2 != null) {
                textMsg = new TextMessage();
                textMsg.text = ValueParser.retrieveBIPAlphaId(ctlv2);
                textMsg.responseNeeded = false;
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.CHANNEL_DATA, ctlvs);
            if (ctlv3 != null) {
                this.mCmdParams = new SendDataParams(cmdDet, channel2, ValueParser.retrieveChannelData(ctlv3), textMsg);
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
