package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.Duration;
import com.android.internal.telephony.cat.HwCustCatService;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

public class CatService extends Handler implements AppInterface, ICatServiceInner {
    private static final boolean DBG = false;
    private static final int DEFAULT_SEND_RETRY_DELAY = 20000;
    private static final int DEV_ID_DISPLAY = 2;
    private static final int DEV_ID_KEYPAD = 1;
    private static final int DEV_ID_NETWORK = 131;
    private static final int DEV_ID_TERMINAL = 130;
    private static final int DEV_ID_UICC = 129;
    private static final int EVENT_LENGTH = 10;
    private static final int EVENT_SEND_ENVELOPE_RESULT = 100;
    private static final int EVENT_SEND_ENVELOPE_RETRY = 99;
    private static final int EVENT_SEND_RESPONSE_WAIT = 40;
    private static final int EVENT_SEND_RESPONSE_WAIT_TIME = 60;
    private static final int ID_BROWSER_TERMINATION_EVENT = 5;
    private static final int ID_BROWSING_STATUS_EVENT = 6;
    private static final int ID_CHANNEL_STATUS_EVENT = 0;
    private static final int ID_DATA_AVAILABLE_EVENT = 1;
    private static final int ID_IDLE_SCREEN_AVAILABLE_EVENT = 3;
    private static final int ID_LANGUAGE_SELECTION_EVENT = 4;
    private static final int ID_USER_ACTIVITY_EVENT = 2;
    private static final int MAX_SEND_RETRIES = 5;
    protected static final int MSG_ID_ALPHA_NOTIFY = 9;
    protected static final int MSG_ID_CALL_SETUP = 4;
    protected static final int MSG_ID_EVENT_NOTIFY = 3;
    protected static final int MSG_ID_ICC_CHANGED = 8;
    private static final int MSG_ID_ICC_RECORDS_LOADED = 20;
    private static final int MSG_ID_ICC_REFRESH = 30;
    protected static final int MSG_ID_PROACTIVE_COMMAND = 2;
    static final int MSG_ID_REFRESH = 5;
    private static final int MSG_ID_REFRESH_FILE_CHANGE_NOTIFICATION = -1;
    static final int MSG_ID_RESPONSE = 6;
    static final int MSG_ID_RIL_MSG_DECODED = 10;
    protected static final int MSG_ID_SESSION_END = 1;
    static final int MSG_ID_SIM_READY = 7;
    private static final int MTK_EVENT_SEND_RESPONSE_WAIT_TIME = 300;
    static final String STK_DEFAULT = "Default Message";
    private static final boolean isHideStkpop = SystemProperties.getBoolean("ro.hwpp.hidestk", false);
    private static IccRecords mIccRecords;
    private static UiccCardApplication mUiccApplication;
    @UnsupportedAppUsage
    private static CatService[] sInstance = null;
    @UnsupportedAppUsage
    private static final Object sInstanceLock = new Object();
    public int OTA_TYPE = 255;
    private BipProxy mBipProxy = null;
    private IccCardStatus.CardState mCardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
    @UnsupportedAppUsage
    private CommandsInterface mCmdIf;
    @UnsupportedAppUsage
    private Context mContext;
    @UnsupportedAppUsage
    private CatCmdMessage mCurrntCmd = null;
    private DefaultBearerStateReceiver mDefaultBearerStateReceiver = null;
    private int[] mEvents = new int[10];
    private IHwCatServiceEx mHwCatServiceEx;
    private HwCustCatService mHwCustCatService = null;
    private boolean mIsWifiConnected = false;
    private int mMainSlot = 0;
    @UnsupportedAppUsage
    private CatCmdMessage mMenuCmd = null;
    @UnsupportedAppUsage
    private RilMessageDecoder mMsgDecoder = null;
    private int mRetryCount = 1;
    private String mRetryHexString = null;
    @UnsupportedAppUsage
    private int mSlotId;
    @UnsupportedAppUsage
    private boolean mStkAppInstalled = false;
    @UnsupportedAppUsage
    private UiccController mUiccController;
    public int otaCmdType = 255;

    private CatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccProfile uiccProfile, int slotId) {
        if (ci == null || ca == null || ir == null || context == null || fh == null || uiccProfile == null) {
            throw new NullPointerException("Service: Input parameters must not be null");
        }
        this.mCmdIf = ci;
        this.mContext = context;
        this.mSlotId = slotId;
        this.mHwCatServiceEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwCatServiceEx(this);
        this.mMsgDecoder = RilMessageDecoder.getInstance(this, fh, slotId);
        RilMessageDecoder rilMessageDecoder = this.mMsgDecoder;
        if (rilMessageDecoder == null) {
            CatLog.d(this, "Null RilMessageDecoder instance");
            return;
        }
        rilMessageDecoder.start();
        this.mCmdIf.setOnCatSessionEnd(this, 1, null);
        this.mCmdIf.setOnCatProactiveCmd(this, 2, null);
        this.mCmdIf.setOnCatEvent(this, 3, null);
        this.mCmdIf.setOnCatCallSetUp(this, 4, null);
        this.mCmdIf.registerForIccRefresh(this, 30, null);
        this.mCmdIf.setOnCatCcAlphaNotify(this, 9, null);
        mIccRecords = ir;
        mUiccApplication = ca;
        mIccRecords.registerForRecordsLoaded(this, 20, null);
        CatLog.d(this, "registerForRecordsLoaded slotid=" + this.mSlotId);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 8, null);
        this.mBipProxy = new BipProxy(this, this.mCmdIf, this.mContext);
        this.mDefaultBearerStateReceiver = new DefaultBearerStateReceiver(context);
        this.mDefaultBearerStateReceiver.startListening();
        this.mStkAppInstalled = isStkAppInstalled();
        this.mHwCustCatService = (HwCustCatService) HwCustUtils.createObj(HwCustCatService.class, new Object[]{this, this.mContext});
        CatLog.d(this, "Running CAT service on Slotid: " + this.mSlotId + ". STK app installed:" + this.mStkAppInstalled);
    }

    public static CatService getInstance(CommandsInterface ci, Context context, UiccProfile uiccProfile, int slotId) {
        IccFileHandler fh;
        IccRecords ir;
        UiccCardApplication ca;
        if (uiccProfile != null) {
            UiccCardApplication ca2 = uiccProfile.getApplicationIndex(0);
            if (ca2 != null) {
                ca = ca2;
                fh = ca2.getIccFileHandler();
                ir = ca2.getIccRecords();
            } else {
                ca = ca2;
                fh = null;
                ir = null;
            }
        } else {
            ca = null;
            fh = null;
            ir = null;
        }
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                int simCount = TelephonyManager.getDefault().getSimCount();
                sInstance = new CatService[simCount];
                for (int i = 0; i < simCount; i++) {
                    sInstance[i] = null;
                }
            }
            if (sInstance[slotId] == null) {
                if (!(ci == null || ca == null || ir == null || context == null || fh == null)) {
                    if (uiccProfile != null) {
                        sInstance[slotId] = new CatService(ci, ca, ir, context, fh, uiccProfile, slotId);
                    }
                }
                return null;
            } else if (!(ir == null || mIccRecords == ir)) {
                if (mIccRecords != null) {
                    mIccRecords.unregisterForRecordsLoaded(sInstance[slotId]);
                }
                mIccRecords = ir;
                mUiccApplication = ca;
                mIccRecords.registerForRecordsLoaded(sInstance[slotId], 20, null);
                CatLog.d(sInstance[slotId], "registerForRecordsLoaded slotid=" + slotId + " instance:" + sInstance[slotId]);
            }
            return sInstance[slotId];
        }
    }

    @UnsupportedAppUsage
    public void dispose() {
        synchronized (sInstanceLock) {
            CatLog.d(this, "Disposing CatService object");
            mIccRecords.unregisterForRecordsLoaded(this);
            broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState.CARDSTATE_ABSENT, null);
            this.mCmdIf.unSetOnCatSessionEnd(this);
            this.mCmdIf.unSetOnCatProactiveCmd(this);
            this.mCmdIf.unSetOnCatEvent(this);
            this.mCmdIf.unSetOnCatCallSetUp(this);
            this.mCmdIf.unSetOnCatCcAlphaNotify(this);
            this.mCmdIf.unregisterForIccRefresh(this);
            if (this.mUiccController != null) {
                this.mUiccController.unregisterForIccChanged(this);
                this.mUiccController = null;
            }
            this.mDefaultBearerStateReceiver.stopListening();
            if (this.mMsgDecoder != null) {
                this.mMsgDecoder.dispose();
            }
            this.mMsgDecoder = null;
            removeCallbacksAndMessages(null);
            if (sInstance != null) {
                if (SubscriptionManager.isValidSlotIndex(this.mSlotId)) {
                    sInstance[this.mSlotId] = null;
                } else {
                    CatLog.d(this, "error: invaild slot id: " + this.mSlotId);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        CatLog.d(this, "Service finalized");
    }

    private void handleRilMsg(RilMessage rilMsg) {
        CommandParams cmdParams;
        CommandParams cmdParams2;
        if (rilMsg != null) {
            int i = rilMsg.mId;
            if (i == 1) {
                handleSessionEnd();
            } else if (i == 2) {
                try {
                    CommandParams cmdParams3 = (CommandParams) rilMsg.mData;
                    if (cmdParams3 == null) {
                        return;
                    }
                    if (rilMsg.mResCode == ResultCode.OK || (ResultCode.PRFRMD_ICON_NOT_DISPLAYED == rilMsg.mResCode && AppInterface.CommandType.SET_UP_MENU == cmdParams3.getCommandType())) {
                        handleCommand(cmdParams3, true);
                    } else {
                        sendTerminalResponse(cmdParams3.mCmdDet, rilMsg.mResCode, false, 0, null);
                    }
                } catch (ClassCastException e) {
                    CatLog.d(this, "Fail to parse proactive command");
                    CatCmdMessage catCmdMessage = this.mCurrntCmd;
                    if (catCmdMessage != null) {
                        sendTerminalResponse(catCmdMessage.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                    }
                }
            } else if (i != 3) {
                if (i == 5 && (cmdParams2 = (CommandParams) rilMsg.mData) != null) {
                    handleCommand(cmdParams2, false);
                }
            } else if ((rilMsg.mData instanceof CommandParams) && rilMsg.mResCode == ResultCode.OK && (cmdParams = (CommandParams) rilMsg.mData) != null) {
                handleCommand(cmdParams, false);
            }
        }
    }

    private boolean isSupportedSetupEventCommand(CatCmdMessage cmdMsg) {
        boolean flag = true;
        int[] iArr = cmdMsg.getSetEventList().eventList;
        for (int eventVal : iArr) {
            CatLog.d(this, "Event: " + eventVal);
            if (!(eventVal == 4 || eventVal == 5 || eventVal == 7)) {
                flag = false;
            }
        }
        return flag;
    }

    private void checkSetupEventCommand(CatCmdMessage cmdMsg) {
        for (int i = 0; i < 10; i++) {
            this.mEvents[i] = 0;
        }
        int cmdMsgLenth = cmdMsg.getSetEventList().eventList.length;
        for (int i2 = 0; i2 < cmdMsgLenth; i2++) {
            int eventval = cmdMsg.getSetEventList().eventList[i2];
            CatLog.d(this, "Event: " + eventval);
            if (eventval == 4) {
                CatLog.d(this, "USER_ACTIVITY_EVENT is true");
                this.mEvents[2] = 1;
            } else if (eventval == 5) {
                CatLog.d(this, "IDLE_SCREEN_AVAILABLE_EVENT is true");
                this.mEvents[3] = 1;
            } else if (eventval != 15) {
                switch (eventval) {
                    case 7:
                        CatLog.d(this, "LANGUAGE_SELECTION_EVENT is true");
                        this.mEvents[4] = 1;
                        continue;
                    case 8:
                        CatLog.d(this, "BROWSER_TERMINATION_EVENT is true");
                        this.mEvents[5] = 1;
                        continue;
                    case 9:
                        CatLog.d(this, "DATA_AVAILABLE_EVENT is true");
                        this.mEvents[1] = 1;
                        continue;
                    case 10:
                        CatLog.d(this, "CHANNEL_STATUS_EVENT is true");
                        this.mEvents[0] = 1;
                        continue;
                }
            } else {
                CatLog.d(this, "BROWSING_STATUS_EVENT is true");
                this.mEvents[6] = 1;
            }
        }
    }

    private void handleRefreshFile(CommandParams cmdParams) {
        HwCustCatService hwCustCatService = this.mHwCustCatService;
        if (hwCustCatService != null && hwCustCatService.supportSimFileRefresh() && this.mHwCustCatService.handleRefreshNotification(this.mUiccController, cmdParams, this.mSlotId)) {
            if (this.mHwCustCatService.supportDocomoEsim()) {
                sendEmptyMessageDelayed(-1, 3000);
            }
            if (this.mHwCustCatService.supportDocomoUimLock()) {
                sendEmptyMessageDelayed(13, 3000);
            }
        }
    }

    private void handleCommand(CommandParams cmdParams, boolean isProactiveCmd) {
        ResultCode resultCode;
        ResultCode resultCode2;
        ResultCode result;
        boolean noAlphaUsrCnf;
        UiccController uiccController;
        CatLog.d(this, cmdParams.getCommandType().name());
        if (isProactiveCmd && (uiccController = this.mUiccController) != null) {
            uiccController.addCardLog("ProactiveCommand mSlotId=" + this.mSlotId + " cmdParams=" + cmdParams);
        }
        CatCmdMessage cmdMsg = new CatCmdMessage(cmdParams);
        if (cmdParams.getCommandType() != null) {
            CatLog.d(this, cmdParams.getCommandType().name());
            switch (cmdParams.getCommandType()) {
                case SET_UP_MENU:
                    CatLog.d(this, "handleProactiveCommand()  SET_UP_MENU ");
                    if (removeMenu(cmdMsg.getMenu())) {
                        this.mMenuCmd = null;
                    } else {
                        this.mMenuCmd = cmdMsg;
                    }
                    if (cmdParams.mLoadIconFailed) {
                        resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                    } else {
                        resultCode = ResultCode.OK;
                    }
                    if (isProactiveCmd) {
                        sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                        break;
                    }
                    break;
                case DISPLAY_TEXT:
                    if (isHideStkpop) {
                        int modemReboot = SystemProperties.getInt("gsm.stk.hide", 0);
                        CatLog.d(this, "Receive DisplayTetxt modem reboot=" + modemReboot);
                        if (1 == modemReboot) {
                            CatLog.d(this, "Modem reboot, avoid DisplayText");
                            SystemProperties.set("gsm.stk.hide", ProxyController.MODEM_0);
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                            return;
                        }
                    }
                    sendTerminalWaitResponse(cmdMsg, cmdParams);
                    break;
                case REFRESH:
                    handleRefreshFile(cmdParams);
                    cmdParams.mCmdDet.typeOfCommand = AppInterface.CommandType.SET_UP_IDLE_MODE_TEXT.value();
                    break;
                case SET_UP_IDLE_MODE_TEXT:
                    if (cmdParams.mLoadIconFailed) {
                        resultCode2 = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                    } else {
                        resultCode2 = ResultCode.OK;
                    }
                    if (isProactiveCmd) {
                        sendTerminalResponse(cmdParams.mCmdDet, resultCode2, false, 0, null);
                        break;
                    }
                    break;
                case SET_UP_EVENT_LIST:
                    if (HuaweiTelephonyConfigs.isModemBipEnable()) {
                        if (isProactiveCmd) {
                            if (!isSupportedSetupEventCommand(cmdMsg)) {
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                                break;
                            } else {
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                                break;
                            }
                        }
                    } else {
                        checkSetupEventCommand(cmdMsg);
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        break;
                    }
                    break;
                case PROVIDE_LOCAL_INFORMATION:
                    int i = cmdParams.mCmdDet.commandQualifier;
                    if (i == 3) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new DTTZResponseData(null));
                        return;
                    } else if (i != 4) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    } else {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new LanguageResponseData(Locale.getDefault().getLanguage()));
                        return;
                    }
                case LAUNCH_BROWSER:
                    if ((((LaunchBrowserParams) cmdParams).mConfirmMsg.text != null && ((LaunchBrowserParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) || ((LaunchBrowserParams) cmdParams).mConfirmMsg.text == null) {
                        ((LaunchBrowserParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17040397).toString();
                        break;
                    }
                case SELECT_ITEM:
                case GET_INPUT:
                case GET_INKEY:
                case PLAY_TONE:
                    break;
                case RUN_AT:
                    if (STK_DEFAULT.equals(((DisplayTextParams) cmdParams).mTextMsg.text)) {
                        ((DisplayTextParams) cmdParams).mTextMsg.text = null;
                        break;
                    }
                    break;
                case SEND_DTMF:
                case SEND_SMS:
                case SEND_SS:
                case SEND_USSD:
                    if (((DisplayTextParams) cmdParams).mTextMsg.text != null && ((DisplayTextParams) cmdParams).mTextMsg.text.equals(STK_DEFAULT)) {
                        ((DisplayTextParams) cmdParams).mTextMsg.text = this.mContext.getText(17041192).toString();
                        break;
                    }
                case SET_UP_CALL:
                    if (((CallSetupParams) cmdParams).mConfirmMsg.text != null && ((CallSetupParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                        ((CallSetupParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17039485).toString();
                        break;
                    }
                case LANGUAGE_NOTIFICATION:
                    String language = ((LanguageParams) cmdParams).mLanguage;
                    ResultCode result2 = ResultCode.OK;
                    if (language != null && language.length() > 0) {
                        try {
                            changeLanguage(language);
                        } catch (RemoteException e) {
                            result = ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS;
                        }
                    }
                    result = result2;
                    sendTerminalResponse(cmdParams.mCmdDet, result, false, 0, null);
                    return;
                case OPEN_CHANNEL:
                case CLOSE_CHANNEL:
                case RECEIVE_DATA:
                case SEND_DATA:
                    removeFailMsg(cmdParams);
                    if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                        if (cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL) {
                            CatCmdMessage.ChannelSettings newChannel = cmdMsg.getChannelSettings();
                            if (newChannel != null) {
                                if (this.mBipProxy.canHandleNewChannel()) {
                                    if (cmdMsg.geTextMessage() != null && cmdMsg.geTextMessage().responseNeeded) {
                                        CatLog.d(this, "open channel text not null");
                                        break;
                                    }
                                } else {
                                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BIP_ERROR, true, 1, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
                                    return;
                                }
                            } else {
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                                return;
                            }
                        }
                        cmdMsg.setWifiConnectedFlag(this.mIsWifiConnected);
                        this.mCurrntCmd = cmdMsg;
                        cmdMsg.setSlotId(this.mSlotId);
                        this.mBipProxy.handleBipCommand(cmdMsg);
                        if (cmdMsg.geTextMessage() == null || cmdMsg.geTextMessage().text == null) {
                            return;
                        }
                    } else {
                        BIPClientParams cmd = (BIPClientParams) cmdParams;
                        try {
                            noAlphaUsrCnf = this.mContext.getResources().getBoolean(17891529);
                        } catch (Resources.NotFoundException e2) {
                            noAlphaUsrCnf = false;
                        }
                        if (cmd.mTextMsg.text != null || (!cmd.mHasAlphaId && !noAlphaUsrCnf)) {
                            if (!this.mStkAppInstalled) {
                                CatLog.d(this, "No STK application found.");
                                if (isProactiveCmd) {
                                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                                    return;
                                }
                            }
                            if (isProactiveCmd && (cmdParams.getCommandType() == AppInterface.CommandType.CLOSE_CHANNEL || cmdParams.getCommandType() == AppInterface.CommandType.RECEIVE_DATA || cmdParams.getCommandType() == AppInterface.CommandType.SEND_DATA)) {
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                                break;
                            }
                        } else {
                            CatLog.d(this, "cmd " + cmdParams.getCommandType() + " with null alpha id");
                            if (isProactiveCmd) {
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                                return;
                            } else if (cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL) {
                                this.mCmdIf.handleCallSetupRequestFromSim(true, null);
                                return;
                            } else {
                                return;
                            }
                        }
                    }
                    break;
                case GET_CHANNEL_STATUS:
                    if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                        this.mCurrntCmd = cmdMsg;
                        cmdMsg.setSlotId(this.mSlotId);
                        this.mBipProxy.handleBipCommand(cmdMsg);
                        if (cmdMsg.geTextMessage() == null || cmdMsg.geTextMessage().text == null) {
                            return;
                        }
                    }
                    break;
                default:
                    CatLog.d(this, "Unsupported command");
                    return;
            }
            this.mCurrntCmd = cmdMsg;
            broadcastCatCmdIntent(cmdMsg);
            return;
        }
        CatLog.d(this, "Unsupported command");
    }

    private void sendTerminalWaitResponse(CatCmdMessage cmdMsg, CommandParams cmdParams) {
        if ((HuaweiTelephonyConfigs.isHisiPlatform() || HuaweiTelephonyConfigs.isMTKPlatform()) && !cmdMsg.geTextMessage().responseNeeded) {
            ResultCode resultCode = cmdParams.mLoadIconFailed ? ResultCode.PRFRMD_ICON_NOT_DISPLAYED : ResultCode.OK;
            Message sendTerminalResponseWait = obtainMessage(40);
            sendTerminalResponseWait.obj = cmdParams.mCmdDet;
            sendTerminalResponseWait.arg1 = resultCode.value();
            if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                sendMessageDelayed(sendTerminalResponseWait, 300);
            } else {
                sendMessageDelayed(sendTerminalResponseWait, 60);
            }
        }
    }

    private void broadcastCatCmdIntent(CatCmdMessage cmdMsg) {
        Intent intent = new Intent(AppInterface.CAT_CMD_ACTION);
        intent.addFlags(268435456);
        intent.addFlags(67108864);
        intent.putExtra("STK CMD", cmdMsg);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        CatLog.d(this, "Sending CmdMsg: " + cmdMsg + " on slotid:" + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void handleSessionEnd() {
        CatLog.d(this, "SESSION END on " + this.mSlotId);
        this.mCurrntCmd = this.mMenuCmd;
        Intent intent = new Intent(AppInterface.CAT_SESSION_END_ACTION);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    @UnsupportedAppUsage
    private void sendTerminalResponse(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        if (cmdDet != null) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Input cmdInput = null;
            CatCmdMessage catCmdMessage = this.mCurrntCmd;
            if (catCmdMessage != null) {
                cmdInput = catCmdMessage.geInput();
            }
            int tag = ComprehensionTlvTag.COMMAND_DETAILS.value();
            if (cmdDet.compRequired) {
                tag |= 128;
            }
            buf.write(tag);
            buf.write(3);
            buf.write(cmdDet.commandNumber);
            buf.write(cmdDet.typeOfCommand);
            buf.write(cmdDet.commandQualifier);
            buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value());
            int length = 2;
            buf.write(2);
            buf.write(130);
            buf.write(129);
            int tag2 = ComprehensionTlvTag.RESULT.value();
            if (cmdDet.compRequired) {
                tag2 |= 128;
            }
            buf.write(tag2);
            if (!includeAdditionalInfo) {
                length = 1;
            }
            buf.write(length);
            buf.write(resultCode.value());
            if (includeAdditionalInfo) {
                buf.write(additionalInfo);
            }
            if (resp != null) {
                resp.format(buf);
            } else {
                encodeOptionalTags(cmdDet, resultCode, cmdInput, buf);
            }
            String hexString = IccUtils.bytesToHexString(buf.toByteArray());
            if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false) && resultCode != ResultCode.OK) {
                sendBroadcastToOtaUI(this.OTA_TYPE, false);
            }
            this.mCmdIf.sendTerminalResponse(hexString, null);
        }
    }

    private void encodeOptionalTags(CommandDetails cmdDet, ResultCode resultCode, Input cmdInput, ByteArrayOutputStream buf) {
        AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
        if (cmdType != null) {
            int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdType.ordinal()];
            if (i != 6) {
                if (i != 9 && i != 10) {
                    CatLog.d(this, "encodeOptionalTags() Unsupported Cmd details=" + cmdDet);
                } else if (resultCode.value() == ResultCode.NO_RESPONSE_FROM_USER.value() && cmdInput != null && cmdInput.duration != null) {
                    getInKeyResponse(buf, cmdInput);
                }
            } else if (cmdDet.commandQualifier == 4 && resultCode.value() == ResultCode.OK.value()) {
                getPliResponse(buf);
            }
        } else {
            CatLog.d(this, "encodeOptionalTags() bad Cmd details=" + cmdDet);
        }
    }

    private void getInKeyResponse(ByteArrayOutputStream buf, Input cmdInput) {
        buf.write(ComprehensionTlvTag.DURATION.value());
        buf.write(2);
        Duration.TimeUnit timeUnit = cmdInput.duration.timeUnit;
        buf.write(Duration.TimeUnit.SECOND.value());
        buf.write(cmdInput.duration.timeInterval);
    }

    private void getPliResponse(ByteArrayOutputStream buf) {
        String lang = Locale.getDefault().getLanguage();
        if (lang != null) {
            buf.write(ComprehensionTlvTag.LANGUAGE.value());
            ResponseData.writeLength(buf, lang.length());
            buf.write(lang.getBytes(), 0, lang.length());
        }
    }

    private void sendMenuSelection(int menuId, boolean helpRequired) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(211);
        buf.write(0);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        buf.write(1);
        buf.write(129);
        buf.write(ComprehensionTlvTag.ITEM_ID.value() | 128);
        buf.write(1);
        buf.write(menuId);
        if (helpRequired) {
            buf.write(ComprehensionTlvTag.HELP_REQUEST.value());
            buf.write(0);
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        this.mCmdIf.sendEnvelope(IccUtils.bytesToHexString(rawData), null);
    }

    @Override // com.android.internal.telephony.cat.AppInterface
    public void onEventDownload(CatEventMessage eventMsg) {
        CatLog.d(this, "Download event: " + eventMsg.getEvent());
        if (eventMsg.getEvent() == 10 && this.mEvents[0] == 0) {
            CatLog.d(this, "channel_status == 0 and don't send envelope to card");
        } else {
            eventDownload(eventMsg.getEvent(), eventMsg.getSourceId(), eventMsg.getDestId(), eventMsg.getAdditionalInfo(), eventMsg.isOneShot());
        }
    }

    private void eventDownload(int event, int sourceId, int destinationId, byte[] additionalInfo, boolean oneShot) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(BerTlv.BER_EVENT_DOWNLOAD_TAG);
        buf.write(0);
        buf.write(ComprehensionTlvTag.EVENT_LIST.value() | 128);
        buf.write(1);
        buf.write(event);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        buf.write(sourceId);
        buf.write(destinationId);
        boolean isRetry = false;
        if (event != 4) {
            if (event == 5) {
                CatLog.d(sInstance, " Sending Idle Screen Available event download to ICC");
            } else if (event == 7) {
                CatLog.d(sInstance, " Sending Language Selection event download to ICC");
                buf.write(ComprehensionTlvTag.LANGUAGE.value() | 128);
                buf.write(2);
            } else if (event == 9 && !HuaweiTelephonyConfigs.isModemBipEnable()) {
                isRetry = true;
                CatLog.d(this, "DATA_AVAILABLE_EVENT isRetry true");
            }
        }
        if (additionalInfo != null) {
            for (byte b : additionalInfo) {
                buf.write(b);
            }
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        String hexString = IccUtils.bytesToHexString(rawData);
        CatLog.d(this, "ENVELOPE COMMAND: " + hexString);
        if (isRetry) {
            this.mRetryHexString = hexString;
            CatLog.d(this, "ENVELOPE COMMAND mRetryHexString: " + this.mRetryHexString);
            this.mCmdIf.sendEnvelope(hexString, obtainMessage(100));
            return;
        }
        this.mCmdIf.sendEnvelope(hexString, null);
    }

    public static AppInterface getInstance() {
        int slotId = 0;
        SubscriptionController sControl = SubscriptionController.getInstance();
        if (sControl != null) {
            slotId = sControl.getSlotIndex(sControl.getDefaultSubId());
        }
        return getInstance(null, null, null, slotId);
    }

    public static AppInterface getInstance(int slotId) {
        return getInstance(null, null, null, slotId);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        AsyncResult ar;
        CatLog.d(this, "handleMessage[" + msg.what + "]");
        int i = msg.what;
        if (i == -1) {
            HwCustCatService hwCustCatService = this.mHwCustCatService;
            if (hwCustCatService != null) {
                hwCustCatService.broadcastFileChangeNotification(this.mSlotId);
            }
        } else if (i == 20) {
        } else {
            if (i != 30) {
                if (i != 40) {
                    if (i == 99) {
                        CatLog.d(this, "SEND ENVELOPE retry times " + this.mRetryCount + " RETRY ENVELOPE COMMAND " + this.mRetryHexString);
                        this.mCmdIf.sendEnvelope(this.mRetryHexString, obtainMessage(100));
                        this.mRetryCount = this.mRetryCount + 1;
                    } else if (i != 100) {
                        switch (i) {
                            case 1:
                            case 2:
                            case 3:
                            case 5:
                                CatLog.d(this, "ril message arrived,slotid:" + this.mSlotId);
                                String data = null;
                                if (!(msg.obj == null || (ar = (AsyncResult) msg.obj) == null || ar.result == null)) {
                                    try {
                                        data = (String) ar.result;
                                        CatLog.d(this, " cmdCode = " + data);
                                        setLanguageNotificationCode(data);
                                    } catch (ClassCastException e) {
                                        return;
                                    }
                                }
                                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, data));
                                return;
                            case 4:
                                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
                                return;
                            case 6:
                                if (msg.obj instanceof CatResponseMessage) {
                                    handleCmdResponse((CatResponseMessage) msg.obj);
                                    return;
                                }
                                return;
                            default:
                                switch (i) {
                                    case 8:
                                        updateIccAvailability();
                                        return;
                                    case 9:
                                        CatLog.d(this, "Received CAT CC Alpha message from card");
                                        if (msg.obj != null) {
                                            AsyncResult ar2 = (AsyncResult) msg.obj;
                                            if (ar2 == null || ar2.result == null) {
                                                CatLog.d(this, "CAT Alpha message: ar.result is null");
                                                return;
                                            } else {
                                                broadcastAlphaMessage((String) ar2.result);
                                                return;
                                            }
                                        } else {
                                            CatLog.d(this, "CAT Alpha message: msg.obj is null");
                                            return;
                                        }
                                    case 10:
                                        if (msg.obj instanceof RilMessage) {
                                            handleRilMsg((RilMessage) msg.obj);
                                            return;
                                        }
                                        return;
                                    case 11:
                                        HwCustCatService hwCustCatService2 = this.mHwCustCatService;
                                        if (hwCustCatService2 != null) {
                                            hwCustCatService2.handleOtaCommand((HwCustCatService.OtaCmdMessage) msg.obj, this.mCmdIf);
                                            return;
                                        }
                                        return;
                                    case 12:
                                        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false)) {
                                            HwCustCatService.OtaCmdMessage resMsg = (HwCustCatService.OtaCmdMessage) msg.obj;
                                            if (resMsg == null) {
                                                CatLog.d(this, "handle msg when not receive open channel, ota cmd is null ");
                                                return;
                                            }
                                            CatLog.d(this, "send fail to UI when not receive open channel, otatype is " + resMsg.otaType);
                                            sendBroadcastToOtaUI(resMsg.otaType, false);
                                            if (resMsg.otaType == this.otaCmdType) {
                                                this.otaCmdType = 255;
                                                return;
                                            }
                                            return;
                                        }
                                        return;
                                    case 13:
                                        HwCustCatService hwCustCatService3 = this.mHwCustCatService;
                                        if (hwCustCatService3 != null) {
                                            hwCustCatService3.broadcastUimLockNotification(this.mSlotId);
                                            return;
                                        }
                                        return;
                                    default:
                                        throw new AssertionError("Unrecognized CAT command: " + msg.what);
                                }
                        }
                    } else if (((AsyncResult) msg.obj).exception == null) {
                        CatLog.d(this, "SEND ENVELOPE SUCCESS");
                        this.mRetryCount = 1;
                        this.mRetryHexString = null;
                    } else if (this.mRetryCount <= 5) {
                        sendMessageDelayed(obtainMessage(99), 20000);
                    } else {
                        CatLog.d(this, "SEND ENVELOPE COMMAND exceed MAX RETRIES");
                        this.mRetryCount = 1;
                        this.mRetryHexString = null;
                    }
                } else if (msg.obj == null || ResultCode.fromInt(msg.arg1) == null) {
                    CatLog.d(this, "Wait Message is null");
                } else {
                    sendTerminalResponse((CommandDetails) msg.obj, ResultCode.fromInt(msg.arg1), false, 0, null);
                }
            } else if (msg.obj != null) {
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3 == null || ar3.result == null) {
                    CatLog.d(this, "Icc REFRESH with exception: " + ar3.exception);
                    return;
                }
                broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState.CARDSTATE_PRESENT, (IccRefreshResponse) ar3.result);
            } else {
                CatLog.d(this, "IccRefresh Message is null");
            }
        }
    }

    private void broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState cardState, IccRefreshResponse iccRefreshState) {
        Intent intent = new Intent(AppInterface.CAT_ICC_STATUS_CHANGE);
        intent.addFlags(268435456);
        boolean cardPresent = cardState == IccCardStatus.CardState.CARDSTATE_PRESENT;
        if (iccRefreshState != null) {
            intent.putExtra(AppInterface.REFRESH_RESULT, iccRefreshState.refreshResult);
            CatLog.d(this, "Sending IccResult with Result: " + iccRefreshState.refreshResult);
        }
        intent.putExtra(AppInterface.CARD_STATUS, cardPresent);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.putExtra("SLOT_ID", this.mSlotId);
        CatLog.d(this, "Sending Card Status: " + cardState + " cardPresent: " + cardPresent + "SLOT_ID: " + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void broadcastAlphaMessage(String alphaString) {
        CatLog.d(this, "Broadcasting CAT Alpha message from card: " + alphaString);
        Intent intent = new Intent(AppInterface.CAT_ALPHA_NOTIFY_ACTION);
        intent.addFlags(268435456);
        intent.putExtra(AppInterface.ALPHA_STRING, alphaString);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    @Override // com.android.internal.telephony.cat.AppInterface
    public synchronized void onCmdResponse(CatResponseMessage resMsg) {
        if (resMsg != null) {
            obtainMessage(6, resMsg).sendToTarget();
        }
    }

    private boolean validateResponse(CatResponseMessage resMsg) {
        boolean validResponse = false;
        if (AppInterface.CommandType.DISPLAY_TEXT.value() == resMsg.mCmdDet.typeOfCommand && this.mCurrntCmd == null && this.mMenuCmd == null) {
            return true;
        }
        if (resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_CALL.value() && this.mCurrntCmd == null) {
            CatLog.d(this, "validateResponse: SET_UP_CALL");
            validResponse = true;
        }
        if (resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_EVENT_LIST.value() || resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_MENU.value()) {
            CatLog.d(this, "CmdType: " + resMsg.mCmdDet.typeOfCommand);
            return true;
        } else if (this.mCurrntCmd == null) {
            return validResponse;
        } else {
            boolean validResponse2 = resMsg.mCmdDet.compareTo(this.mCurrntCmd.mCmdDet);
            CatLog.d(this, "isResponse for last valid cmd: " + validResponse2);
            return validResponse2;
        }
    }

    private boolean removeMenu(Menu menu) {
        try {
            return menu.items.size() == 1 && menu.items.get(0) == null;
        } catch (NullPointerException e) {
            CatLog.d(this, "Unable to get Menu's items size");
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x00d3  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01a1  */
    private void handleCmdResponse(CatResponseMessage resMsg) {
        boolean helpRequired;
        CatCmdMessage catCmdMessage;
        CatCmdMessage catCmdMessage2;
        CatCmdMessage catCmdMessage3;
        if (resMsg != null && validateResponse(resMsg)) {
            CatLog.d(this, "code is " + resMsg.mResCode + ";type is " + resMsg.mCmdDet.typeOfCommand);
            if (!(resMsg.getCmdDetails() == null || (catCmdMessage3 = this.mCurrntCmd) == null || catCmdMessage3.mCmdDet == null)) {
                resMsg.getCmdDetails().compRequired = this.mCurrntCmd.mCmdDet.compRequired;
            }
            if (resMsg.envelopeCmd != null) {
                this.mCmdIf.sendEnvelope(resMsg.envelopeCmd, null);
                return;
            }
            ResponseData resp = null;
            CommandDetails cmdDet = resMsg.getCmdDetails();
            AppInterface.CommandType type = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
            switch (resMsg.mResCode) {
                case HELP_INFO_REQUIRED:
                    helpRequired = true;
                    if (type == null) {
                        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[type.ordinal()];
                        boolean helpRequired2 = true;
                        if (i == 1) {
                            if (resMsg.mResCode != ResultCode.HELP_INFO_REQUIRED) {
                                helpRequired2 = false;
                            }
                            sendMenuSelection(resMsg.mUsersMenuSelection, helpRequired2);
                            return;
                        } else if (i != 2) {
                            if (i != 5) {
                                if (i != 17) {
                                    if (i != 19) {
                                        switch (i) {
                                            case 7:
                                                if (resMsg.mResCode != ResultCode.LAUNCH_BROWSER_ERROR) {
                                                    resMsg.mIncludeAdditionalInfo = false;
                                                    resMsg.mAdditionalInfo = 0;
                                                    break;
                                                } else {
                                                    resMsg.setAdditionalInfo(4);
                                                    break;
                                                }
                                            case 8:
                                                resp = new SelectItemResponseData(resMsg.mUsersMenuSelection);
                                                break;
                                            case 9:
                                            case 10:
                                                CatCmdMessage catCmdMessage4 = this.mCurrntCmd;
                                                if (catCmdMessage4 != null) {
                                                    Input input = catCmdMessage4.geInput();
                                                    if (!input.yesNo) {
                                                        if (!helpRequired) {
                                                            resp = new GetInkeyInputResponseData(resMsg.mUsersInput, input.ucs2, input.packed);
                                                            break;
                                                        }
                                                    } else {
                                                        resp = new GetInkeyInputResponseData(resMsg.mUsersYesNoSelection);
                                                        break;
                                                    }
                                                }
                                                break;
                                        }
                                    } else if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
                                        if (resMsg.mResCode == ResultCode.OK && resMsg.mUsersConfirm && (catCmdMessage = this.mCurrntCmd) != null) {
                                            catCmdMessage.setWifiConnectedFlag(this.mIsWifiConnected);
                                            this.mCurrntCmd.setSlotId(this.mSlotId);
                                            this.mBipProxy.handleBipCommand(this.mCurrntCmd);
                                            return;
                                        }
                                    }
                                }
                                this.mCmdIf.handleCallSetupRequestFromSim(resMsg.mUsersConfirm, null);
                                this.mCurrntCmd = null;
                                return;
                            } else if (5 == resMsg.mEventValue) {
                                eventDownload(resMsg.mEventValue, 2, 129, resMsg.mAddedInfo, false);
                                return;
                            } else {
                                eventDownload(resMsg.mEventValue, 130, 129, resMsg.mAddedInfo, false);
                                return;
                            }
                        } else if (resMsg.mResCode == ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS) {
                            resMsg.setAdditionalInfo(1);
                        } else {
                            resMsg.mIncludeAdditionalInfo = false;
                            resMsg.mAdditionalInfo = 0;
                        }
                    } else {
                        CatLog.d(this, "handleCmdResponse() bad Cmd details=" + cmdDet);
                    }
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                case OK:
                case PRFRMD_WITH_PARTIAL_COMPREHENSION:
                case PRFRMD_WITH_MISSING_INFO:
                case PRFRMD_WITH_ADDITIONAL_EFS_READ:
                case PRFRMD_ICON_NOT_DISPLAYED:
                case PRFRMD_MODIFIED_BY_NAA:
                case PRFRMD_LIMITED_SERVICE:
                case PRFRMD_WITH_MODIFICATION:
                case PRFRMD_NAA_NOT_ACTIVE:
                case PRFRMD_TONE_NOT_PLAYED:
                case LAUNCH_BROWSER_ERROR:
                case TERMINAL_CRNTLY_UNABLE_TO_PROCESS:
                    helpRequired = false;
                    if (type == null) {
                    }
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                case BACKWARD_MOVE_BY_USER:
                case USER_NOT_ACCEPT:
                    if (type == AppInterface.CommandType.SET_UP_CALL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    if (type != AppInterface.CommandType.OPEN_CHANNEL) {
                        resp = null;
                    } else if (HuaweiTelephonyConfigs.isModemBipEnable()) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    } else if (!resMsg.mUsersConfirm && (catCmdMessage2 = this.mCurrntCmd) != null && catCmdMessage2.geTextMessage().responseNeeded) {
                        CatCmdMessage.ChannelSettings params = this.mCurrntCmd.getChannelSettings();
                        resMsg.mResCode = ResultCode.USER_NOT_ACCEPT;
                        resp = new OpenChannelResponseData(params.bufSize, null, params.bearerDescription);
                    }
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                case NO_RESPONSE_FROM_USER:
                    if (type == AppInterface.CommandType.SET_UP_CALL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    resp = null;
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                case UICC_SESSION_TERM_BY_USER:
                    resp = null;
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                default:
                    return;
            }
        }
    }

    @UnsupportedAppUsage
    private boolean isStkAppInstalled() {
        List<ResolveInfo> broadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(new Intent(AppInterface.CAT_CMD_ACTION), 128);
        if ((broadcastReceivers == null ? 0 : broadcastReceivers.size()) > 0) {
            return true;
        }
        return false;
    }

    public void update(CommandsInterface ci, Context context, UiccProfile uiccProfile) {
        UiccCardApplication ca = null;
        IccRecords ir = null;
        if (!(uiccProfile == null || (ca = uiccProfile.getApplicationIndex(0)) == null)) {
            ir = ca.getIccRecords();
        }
        synchronized (sInstanceLock) {
            if (ir != null) {
                if (mIccRecords != ir) {
                    if (mIccRecords != null) {
                        mIccRecords.unregisterForRecordsLoaded(this);
                    }
                    CatLog.d(this, "Reinitialize the Service with SIMRecords and UiccCardApplication");
                    mIccRecords = ir;
                    mUiccApplication = ca;
                    mIccRecords.registerForRecordsLoaded(this, 20, null);
                    CatLog.d(this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateIccAvailability() {
        if (this.mUiccController != null) {
            IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
            UiccCard newCard = this.mUiccController.getUiccCard(this.mSlotId);
            if (newCard != null) {
                newState = newCard.getCardState();
            }
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCardState = newState;
            int oldMainSlot = this.mMainSlot;
            int newMainSlot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
            this.mMainSlot = newMainSlot;
            if (oldState != newState) {
                CatLog.d(this, "New Card State = " + newState + " Old Card State = " + oldState);
            }
            if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState != IccCardStatus.CardState.CARDSTATE_PRESENT) {
                broadcastCardStateAndIccRefreshResp(newState, null);
            } else if (oldState != IccCardStatus.CardState.CARDSTATE_PRESENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT) {
                this.mCmdIf.reportStkServiceIsRunning(null);
            } else if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT && oldMainSlot != newMainSlot) {
                CatLog.d(this, "switch the main slot! oldMainSlot=" + oldMainSlot + " newMainSlot=" + newMainSlot);
                this.mCmdIf.reportStkServiceIsRunning(null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class DefaultBearerStateReceiver extends BroadcastReceiver {
        private Context mContext;
        private IntentFilter mFilter;
        private boolean mIsRegistered = false;

        public DefaultBearerStateReceiver(Context context) {
            this.mContext = context;
            this.mFilter = new IntentFilter();
            this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                CatLog.d(this, "Received broadcast: intent is null");
            } else if (intent.getAction() == null) {
                CatLog.d(this, "Received broadcast: Action is null");
            } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                boolean oldIsWifiConnected = CatService.this.mIsWifiConnected;
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                CatService.this.mIsWifiConnected = networkInfo != null && networkInfo.isConnected();
                if (oldIsWifiConnected != CatService.this.mIsWifiConnected) {
                    CatLog.d(this, "WifiManager.NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + CatService.this.mIsWifiConnected);
                }
            }
        }

        public void startListening() {
            if (!this.mIsRegistered) {
                this.mContext.registerReceiver(this, this.mFilter);
                this.mIsRegistered = true;
            }
        }

        public void stopListening() {
            if (this.mIsRegistered) {
                this.mContext.unregisterReceiver(this);
                this.mIsRegistered = false;
            }
        }
    }

    public void sendBroadcastToOtaUI(int OtaType, boolean processResult) {
        HwCustCatService hwCustCatService = this.mHwCustCatService;
        if (hwCustCatService != null) {
            hwCustCatService.sendBroadcastToOtaUI(OtaType, processResult);
        }
    }

    private void removeFailMsg(CommandParams cmdParams) {
        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false) && cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL && hasMessages(12)) {
            CatLog.d(this, "received open chanel msg--Remove MSG_ID_NO_OPEN_CHANNEL_RECEIVED message");
            removeMessages(12);
        }
    }

    public synchronized void onOtaCommand(int otaType) {
        CatLog.d(this, "enter on otacommand otatype=" + otaType);
        if (this.mHwCustCatService != null) {
            CatLog.d(this, "on otacommand otatype=" + otaType);
            this.mHwCustCatService.onOtaCommand(otaType);
        }
    }

    private void changeLanguage(String language) throws RemoteException {
        IActivityManager am = ActivityManagerNative.getDefault();
        Configuration config = am.getConfiguration();
        config.setLocales(new LocaleList(new Locale(language), LocaleList.getDefault()));
        config.userSetLocale = true;
        am.updatePersistentConfiguration(config);
        BackupManager.dataChanged("com.android.providers.settings");
    }

    public void sendTerminalResponseHw(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        sendTerminalResponse(cmdDet, resultCode, includeAdditionalInfo, additionalInfo, resp);
    }

    @Override // com.android.internal.telephony.cat.AppInterface
    public String getLanguageNotificationCode() {
        return this.mHwCatServiceEx.getLanguageNotificationCode();
    }

    public void setLanguageNotificationCode(String strLanguageNotificationCode) {
        this.mHwCatServiceEx.setLanguageNotificationCode(strLanguageNotificationCode);
    }
}
