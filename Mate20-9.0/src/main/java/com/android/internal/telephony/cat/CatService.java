package com.android.internal.telephony.cat;

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
import android.os.HandlerThread;
import android.os.LocaleList;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
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

public class CatService extends AbstractCatService {
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
    private static CatService[] sInstance = null;
    private static final Object sInstanceLock = new Object();
    private BipProxy mBipProxy = null;
    private IccCardStatus.CardState mCardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
    private CommandsInterface mCmdIf;
    private Context mContext;
    private CatCmdMessage mCurrntCmd = null;
    private DefaultBearerStateReceiver mDefaultBearerStateReceiver = null;
    private int[] mEvents = new int[10];
    private HandlerThread mHandlerThread;
    private HwCustCatService mHwCustCatService = null;
    /* access modifiers changed from: private */
    public boolean mIsWifiConnected = false;
    private int mMainSlot = 0;
    private CatCmdMessage mMenuCmd = null;
    private RilMessageDecoder mMsgDecoder = null;
    private int mRetryCount = 1;
    private String mRetryHexString = null;
    private int mSlotId;
    private boolean mStkAppInstalled = false;
    private UiccController mUiccController;

    class DefaultBearerStateReceiver extends BroadcastReceiver {
        private Context mContext;
        private IntentFilter mFilter;
        private boolean mIsRegistered = false;

        public DefaultBearerStateReceiver(Context context) {
            this.mContext = context;
            this.mFilter = new IntentFilter();
            this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                CatLog.d((Object) this, "Received broadcast: intent is null");
            } else if (intent.getAction() == null) {
                CatLog.d((Object) this, "Received broadcast: Action is null");
            } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                boolean oldIsWifiConnected = CatService.this.mIsWifiConnected;
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                boolean unused = CatService.this.mIsWifiConnected = networkInfo != null && networkInfo.isConnected();
                if (oldIsWifiConnected != CatService.this.mIsWifiConnected) {
                    CatLog.d((Object) this, "WifiManager.NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + CatService.this.mIsWifiConnected);
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

    private CatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccProfile uiccProfile, int slotId) {
        if (ci == null || ca == null || ir == null || context == null || fh == null || uiccProfile == null) {
            throw new NullPointerException("Service: Input parameters must not be null");
        }
        this.mCmdIf = ci;
        this.mContext = context;
        this.mSlotId = slotId;
        this.mHandlerThread = new HandlerThread("Cat Telephony service" + slotId);
        this.mHandlerThread.start();
        this.mMsgDecoder = RilMessageDecoder.getInstance(this, fh, slotId);
        if (this.mMsgDecoder == null) {
            CatLog.d((Object) this, "Null RilMessageDecoder instance");
            return;
        }
        this.mMsgDecoder.start();
        this.mCmdIf.setOnCatSessionEnd(this, 1, null);
        this.mCmdIf.setOnCatProactiveCmd(this, 2, null);
        this.mCmdIf.setOnCatEvent(this, 3, null);
        this.mCmdIf.setOnCatCallSetUp(this, 4, null);
        this.mCmdIf.registerForIccRefresh(this, 30, null);
        this.mCmdIf.setOnCatCcAlphaNotify(this, 9, null);
        mIccRecords = ir;
        mUiccApplication = ca;
        mIccRecords.registerForRecordsLoaded(this, 20, null);
        CatLog.d((Object) this, "registerForRecordsLoaded slotid=" + this.mSlotId);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 8, null);
        this.mBipProxy = new BipProxy(this, this.mCmdIf, this.mContext);
        this.mDefaultBearerStateReceiver = new DefaultBearerStateReceiver(context);
        this.mDefaultBearerStateReceiver.startListening();
        this.mStkAppInstalled = isStkAppInstalled();
        this.mHwCustCatService = (HwCustCatService) HwCustUtils.createObj(HwCustCatService.class, new Object[]{this, this.mContext});
        CatLog.d((Object) this, "Running CAT service on Slotid: " + this.mSlotId + ". STK app installed:" + this.mStkAppInstalled);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0062, code lost:
        return null;
     */
    public static CatService getInstance(CommandsInterface ci, Context context, UiccProfile uiccProfile, int slotId) {
        UiccProfile uiccProfile2 = uiccProfile;
        int i = slotId;
        UiccCardApplication ca = null;
        IccFileHandler fh = null;
        IccRecords ir = null;
        int i2 = 0;
        if (uiccProfile2 != null) {
            ca = uiccProfile2.getApplicationIndex(0);
            if (ca != null) {
                fh = ca.getIccFileHandler();
                ir = ca.getIccRecords();
            }
        }
        UiccCardApplication ca2 = ca;
        IccFileHandler fh2 = fh;
        IccRecords ir2 = ir;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                int simCount = TelephonyManager.getDefault().getSimCount();
                sInstance = new CatService[simCount];
                while (true) {
                    int i3 = i2;
                    if (i3 >= simCount) {
                        break;
                    }
                    sInstance[i3] = null;
                    i2 = i3 + 1;
                }
            }
            if (sInstance[i] == null) {
                if (!(ci == null || ca2 == null || ir2 == null || context == null || fh2 == null)) {
                    if (uiccProfile2 != null) {
                        CatService[] catServiceArr = sInstance;
                        CatService catService = new CatService(ci, ca2, ir2, context, fh2, uiccProfile2, i);
                        catServiceArr[i] = catService;
                    }
                }
            } else if (!(ir2 == null || mIccRecords == ir2)) {
                if (mIccRecords != null) {
                    mIccRecords.unregisterForRecordsLoaded(sInstance[i]);
                }
                mIccRecords = ir2;
                mUiccApplication = ca2;
                mIccRecords.registerForRecordsLoaded(sInstance[i], 20, null);
                CatService catService2 = sInstance[i];
                CatLog.d((Object) catService2, "registerForRecordsLoaded slotid=" + i + " instance:" + sInstance[i]);
            }
            CatService catService3 = sInstance[i];
            return catService3;
        }
    }

    public void dispose() {
        synchronized (sInstanceLock) {
            CatLog.d((Object) this, "Disposing CatService object");
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
            this.mHandlerThread.quit();
            this.mHandlerThread = null;
            removeCallbacksAndMessages(null);
            if (sInstance != null) {
                if (SubscriptionManager.isValidSlotIndex(this.mSlotId)) {
                    sInstance[this.mSlotId] = null;
                } else {
                    CatLog.d((Object) this, "error: invaild slot id: " + this.mSlotId);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        CatLog.d((Object) this, "Service finalized");
    }

    private void handleRilMsg(RilMessage rilMsg) {
        if (rilMsg != null) {
            int i = rilMsg.mId;
            if (i != 5) {
                switch (i) {
                    case 1:
                        handleSessionEnd();
                        break;
                    case 2:
                        try {
                            CommandParams cmdParams = (CommandParams) rilMsg.mData;
                            if (cmdParams != null) {
                                if (rilMsg.mResCode != ResultCode.OK && (ResultCode.PRFRMD_ICON_NOT_DISPLAYED != rilMsg.mResCode || AppInterface.CommandType.SET_UP_MENU != cmdParams.getCommandType())) {
                                    sendTerminalResponse(cmdParams.mCmdDet, rilMsg.mResCode, false, 0, null);
                                    break;
                                } else {
                                    handleCommand(cmdParams, true);
                                    break;
                                }
                            }
                        } catch (ClassCastException e) {
                            CatLog.d((Object) this, "Fail to parse proactive command");
                            if (this.mCurrntCmd != null) {
                                sendTerminalResponse(this.mCurrntCmd.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                                break;
                            }
                        }
                        break;
                    case 3:
                        if ((rilMsg.mData instanceof CommandParams) && rilMsg.mResCode == ResultCode.OK) {
                            CommandParams cmdParams2 = (CommandParams) rilMsg.mData;
                            if (cmdParams2 != null) {
                                handleCommand(cmdParams2, false);
                                break;
                            }
                        }
                        break;
                }
            } else {
                CommandParams cmdParams3 = (CommandParams) rilMsg.mData;
                if (cmdParams3 != null) {
                    handleCommand(cmdParams3, false);
                }
            }
        }
    }

    private boolean isSupportedSetupEventCommand(CatCmdMessage cmdMsg) {
        boolean flag = true;
        for (int eventVal : cmdMsg.getSetEventList().eventList) {
            CatLog.d((Object) this, "Event: " + eventVal);
            if (eventVal != 7) {
                switch (eventVal) {
                    case 4:
                    case 5:
                        break;
                    default:
                        flag = false;
                        break;
                }
            }
        }
        return flag;
    }

    private void checkSetupEventCommand(CatCmdMessage cmdMsg) {
        for (int i = 0; i < 10; i++) {
            this.mEvents[i] = 0;
        }
        for (int eventval : cmdMsg.getSetEventList().eventList) {
            CatLog.d((Object) this, "Event: " + eventval);
            if (eventval != 15) {
                switch (eventval) {
                    case 4:
                        CatLog.d((Object) this, "USER_ACTIVITY_EVENT is true");
                        this.mEvents[2] = 1;
                        break;
                    case 5:
                        CatLog.d((Object) this, "IDLE_SCREEN_AVAILABLE_EVENT is true");
                        this.mEvents[3] = 1;
                        break;
                    default:
                        switch (eventval) {
                            case 7:
                                CatLog.d((Object) this, "LANGUAGE_SELECTION_EVENT is true");
                                this.mEvents[4] = 1;
                                break;
                            case 8:
                                CatLog.d((Object) this, "BROWSER_TERMINATION_EVENT is true");
                                this.mEvents[5] = 1;
                                break;
                            case 9:
                                CatLog.d((Object) this, "DATA_AVAILABLE_EVENT is true");
                                this.mEvents[1] = 1;
                                break;
                            case 10:
                                CatLog.d((Object) this, "CHANNEL_STATUS_EVENT is true");
                                this.mEvents[0] = 1;
                                break;
                        }
                }
            } else {
                CatLog.d((Object) this, "BROWSING_STATUS_EVENT is true");
                this.mEvents[6] = 1;
            }
        }
    }

    private void handleRefreshFile(CommandParams cmdParams) {
        if (this.mHwCustCatService != null && this.mHwCustCatService.supportSimFileRefresh() && this.mHwCustCatService.handleRefreshNotification(this.mUiccController, cmdParams, this.mSlotId)) {
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
        CatLog.d((Object) this, cmdParams.getCommandType().name());
        if (isProactiveCmd && this.mUiccController != null) {
            UiccController uiccController = this.mUiccController;
            uiccController.addCardLog("ProactiveCommand mSlotId=" + this.mSlotId + " cmdParams=" + cmdParams);
        }
        CatCmdMessage cmdMsg = new CatCmdMessage(cmdParams);
        if (cmdParams.getCommandType() != null) {
            CatLog.d((Object) this, cmdParams.getCommandType().name());
            switch (cmdParams.getCommandType()) {
                case SET_UP_MENU:
                    CatLog.d((Object) this, "handleProactiveCommand()  SET_UP_MENU ");
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
                    ResultCode resultCode3 = resultCode;
                    if (isProactiveCmd) {
                        sendTerminalResponse(cmdParams.mCmdDet, resultCode3, false, 0, null);
                        break;
                    }
                    break;
                case DISPLAY_TEXT:
                    if (isHideStkpop) {
                        int modemReboot = SystemProperties.getInt("gsm.stk.hide", 0);
                        CatLog.d((Object) this, "Receive DisplayTetxt modem reboot=" + modemReboot);
                        if (1 == modemReboot) {
                            CatLog.d((Object) this, "Modem reboot, avoid DisplayText");
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
                    ResultCode resultCode4 = resultCode2;
                    if (isProactiveCmd) {
                        sendTerminalResponse(cmdParams.mCmdDet, resultCode4, false, 0, null);
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
                    switch (cmdParams.mCmdDet.commandQualifier) {
                        case 3:
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new DTTZResponseData(null));
                            break;
                        case 4:
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new LanguageResponseData(Locale.getDefault().getLanguage()));
                            break;
                        default:
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                            break;
                    }
                    return;
                case LAUNCH_BROWSER:
                    if ((((LaunchBrowserParams) cmdParams).mConfirmMsg.text != null && ((LaunchBrowserParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) || ((LaunchBrowserParams) cmdParams).mConfirmMsg.text == null) {
                        CharSequence message = this.mContext.getText(17040320);
                        ((LaunchBrowserParams) cmdParams).mConfirmMsg.text = message.toString();
                        break;
                    }
                case SELECT_ITEM:
                case GET_INPUT:
                case GET_INKEY:
                case PLAY_TONE:
                    break;
                case SEND_DTMF:
                case SEND_SMS:
                case SEND_SS:
                case SEND_USSD:
                    if (((DisplayTextParams) cmdParams).mTextMsg.text != null && ((DisplayTextParams) cmdParams).mTextMsg.text.equals(STK_DEFAULT)) {
                        CharSequence message2 = this.mContext.getText(17041068);
                        ((DisplayTextParams) cmdParams).mTextMsg.text = message2.toString();
                        break;
                    }
                case SET_UP_CALL:
                    if (((CallSetupParams) cmdParams).mConfirmMsg.text != null && ((CallSetupParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                        CharSequence message3 = this.mContext.getText(17039479);
                        ((CallSetupParams) cmdParams).mConfirmMsg.text = message3.toString();
                        break;
                    }
                case LANGUAGE_NOTIFICATION:
                    String language = ((LanguageParams) cmdParams).mLanguage;
                    ResultCode result2 = ResultCode.OK;
                    if (language != null && language.length() > 0) {
                        try {
                            changeLanguage(language);
                        } catch (RemoteException e) {
                            RemoteException remoteException = e;
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
                                        CatLog.d((Object) this, "open channel text not null");
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
                            noAlphaUsrCnf = this.mContext.getResources().getBoolean(17957031);
                        } catch (Resources.NotFoundException e2) {
                            noAlphaUsrCnf = false;
                        }
                        if (cmd.mTextMsg.text != null || (!cmd.mHasAlphaId && !noAlphaUsrCnf)) {
                            if (!this.mStkAppInstalled) {
                                CatLog.d((Object) this, "No STK application found.");
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
                            CatLog.d((Object) this, "cmd " + cmdParams.getCommandType() + " with null alpha id");
                            if (isProactiveCmd) {
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                            } else if (cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL) {
                                this.mCmdIf.handleCallSetupRequestFromSim(true, null);
                            }
                            return;
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
                    CatLog.d((Object) this, "Unsupported command");
                    return;
            }
            this.mCurrntCmd = cmdMsg;
            broadcastCatCmdIntent(cmdMsg);
            return;
        }
        CatLog.d((Object) this, "Unsupported command");
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
        CatLog.d((Object) this, "Sending CmdMsg: " + cmdMsg + " on slotid:" + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void handleSessionEnd() {
        CatLog.d((Object) this, "SESSION END on " + this.mSlotId);
        this.mCurrntCmd = this.mMenuCmd;
        Intent intent = new Intent(AppInterface.CAT_SESSION_END_ACTION);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    public void sendTerminalResponse(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        if (cmdDet != null) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Input cmdInput = null;
            if (this.mCurrntCmd != null) {
                cmdInput = this.mCurrntCmd.geInput();
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
            if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false) && resultCode != ResultCode.OK) {
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
                switch (i) {
                    case 9:
                    case 10:
                        if (resultCode.value() == ResultCode.NO_RESPONSE_FROM_USER.value() && cmdInput != null && cmdInput.duration != null) {
                            getInKeyResponse(buf, cmdInput);
                            return;
                        }
                        return;
                    default:
                        CatLog.d((Object) this, "encodeOptionalTags() Unsupported Cmd details=" + cmdDet);
                        return;
                }
            } else if (cmdDet.commandQualifier == 4 && resultCode.value() == ResultCode.OK.value()) {
                getPliResponse(buf);
            }
        } else {
            CatLog.d((Object) this, "encodeOptionalTags() bad Cmd details=" + cmdDet);
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
        buf.write(128 | ComprehensionTlvTag.DEVICE_IDENTITIES.value());
        buf.write(2);
        buf.write(1);
        buf.write(129);
        buf.write(128 | ComprehensionTlvTag.ITEM_ID.value());
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

    public void onEventDownload(CatEventMessage eventMsg) {
        CatLog.d((Object) this, "Download event: " + eventMsg.getEvent());
        if (eventMsg.getEvent() == 10 && this.mEvents[0] == 0) {
            CatLog.d((Object) this, "channel_status == 0 and don't send envelope to card");
            return;
        }
        eventDownload(eventMsg.getEvent(), eventMsg.getSourceId(), eventMsg.getDestId(), eventMsg.getAdditionalInfo(), eventMsg.isOneShot());
    }

    private void eventDownload(int event, int sourceId, int destinationId, byte[] additionalInfo, boolean oneShot) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(BerTlv.BER_EVENT_DOWNLOAD_TAG);
        buf.write(0);
        buf.write(128 | ComprehensionTlvTag.EVENT_LIST.value());
        buf.write(1);
        buf.write(event);
        buf.write(128 | ComprehensionTlvTag.DEVICE_IDENTITIES.value());
        buf.write(2);
        buf.write(sourceId);
        buf.write(destinationId);
        boolean isRetry = false;
        if (event == 7) {
            CatLog.d((Object) sInstance, " Sending Language Selection event download to ICC");
            buf.write(128 | ComprehensionTlvTag.LANGUAGE.value());
            buf.write(2);
        } else if (event != 9) {
            switch (event) {
                case 5:
                    CatLog.d((Object) sInstance, " Sending Idle Screen Available event download to ICC");
                    break;
            }
        } else if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            isRetry = true;
            CatLog.d((Object) this, "DATA_AVAILABLE_EVENT isRetry " + true);
        }
        if (additionalInfo != null) {
            for (byte b : additionalInfo) {
                buf.write(b);
            }
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        String hexString = IccUtils.bytesToHexString(rawData);
        CatLog.d((Object) this, "ENVELOPE COMMAND: " + hexString);
        if (isRetry) {
            this.mRetryHexString = hexString;
            CatLog.d((Object) this, "ENVELOPE COMMAND mRetryHexString: " + this.mRetryHexString);
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

    public void handleMessage(Message msg) {
        CatLog.d((Object) this, "handleMessage[" + msg.what + "]");
        int i = msg.what;
        if (i != -1) {
            if (i != 20) {
                if (i != 30) {
                    if (i != 40) {
                        switch (i) {
                            case 1:
                            case 2:
                            case 3:
                            case 5:
                                CatLog.d((Object) this, "ril message arrived,slotid:" + this.mSlotId);
                                String data = null;
                                if (msg.obj != null) {
                                    AsyncResult ar = (AsyncResult) msg.obj;
                                    if (!(ar == null || ar.result == null)) {
                                        try {
                                            data = (String) ar.result;
                                            CatLog.d((Object) this, " cmdCode = " + data);
                                            setLanguageNotificationCode(data);
                                        } catch (ClassCastException e) {
                                            break;
                                        }
                                    }
                                }
                                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, data));
                                break;
                            case 4:
                                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
                                break;
                            case 6:
                                if (msg.obj instanceof CatResponseMessage) {
                                    handleCmdResponse((CatResponseMessage) msg.obj);
                                    break;
                                }
                                break;
                            default:
                                switch (i) {
                                    case 8:
                                        updateIccAvailability();
                                        break;
                                    case 9:
                                        CatLog.d((Object) this, "Received CAT CC Alpha message from card");
                                        if (msg.obj == null) {
                                            CatLog.d((Object) this, "CAT Alpha message: msg.obj is null");
                                            break;
                                        } else {
                                            AsyncResult ar2 = (AsyncResult) msg.obj;
                                            if (ar2 != null && ar2.result != null) {
                                                broadcastAlphaMessage((String) ar2.result);
                                                break;
                                            } else {
                                                CatLog.d((Object) this, "CAT Alpha message: ar.result is null");
                                                break;
                                            }
                                        }
                                        break;
                                    case 10:
                                        if (msg.obj instanceof RilMessage) {
                                            handleRilMsg((RilMessage) msg.obj);
                                            break;
                                        }
                                        break;
                                    case 11:
                                        if (this.mHwCustCatService != null) {
                                            this.mHwCustCatService.handleOtaCommand((HwCustCatService.OtaCmdMessage) msg.obj, this.mCmdIf);
                                            break;
                                        }
                                        break;
                                    case 12:
                                        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false)) {
                                            HwCustCatService.OtaCmdMessage resMsg = (HwCustCatService.OtaCmdMessage) msg.obj;
                                            if (resMsg != null) {
                                                CatLog.d((Object) this, "send fail to UI when not receive open channel, otatype is " + resMsg.otaType);
                                                sendBroadcastToOtaUI(resMsg.otaType, false);
                                                if (resMsg.otaType == this.mOtaCmdType) {
                                                    this.mOtaCmdType = 255;
                                                    break;
                                                }
                                            } else {
                                                CatLog.d((Object) this, "handle msg when not receive open channel, ota cmd is null ");
                                                return;
                                            }
                                        }
                                        break;
                                    case 13:
                                        if (this.mHwCustCatService != null) {
                                            this.mHwCustCatService.broadcastUimLockNotification(this.mSlotId);
                                            break;
                                        }
                                        break;
                                    default:
                                        switch (i) {
                                            case 99:
                                                CatLog.d((Object) this, "SEND ENVELOPE retry times " + this.mRetryCount + " RETRY ENVELOPE COMMAND " + this.mRetryHexString);
                                                this.mCmdIf.sendEnvelope(this.mRetryHexString, obtainMessage(100));
                                                this.mRetryCount = this.mRetryCount + 1;
                                                break;
                                            case 100:
                                                if (((AsyncResult) msg.obj).exception != null) {
                                                    if (this.mRetryCount > 5) {
                                                        CatLog.d((Object) this, "SEND ENVELOPE COMMAND exceed MAX RETRIES");
                                                        this.mRetryCount = 1;
                                                        this.mRetryHexString = null;
                                                        break;
                                                    } else {
                                                        sendMessageDelayed(obtainMessage(99), 20000);
                                                        break;
                                                    }
                                                } else {
                                                    CatLog.d((Object) this, "SEND ENVELOPE SUCCESS");
                                                    this.mRetryCount = 1;
                                                    this.mRetryHexString = null;
                                                    break;
                                                }
                                            default:
                                                throw new AssertionError("Unrecognized CAT command: " + msg.what);
                                        }
                                }
                        }
                    } else if (msg.obj == null || ResultCode.fromInt(msg.arg1) == null) {
                        CatLog.d((Object) this, "Wait Message is null");
                    } else {
                        sendTerminalResponse((CommandDetails) msg.obj, ResultCode.fromInt(msg.arg1), false, 0, null);
                    }
                } else if (msg.obj != null) {
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3 == null || ar3.result == null) {
                        CatLog.d((Object) this, "Icc REFRESH with exception: " + ar3.exception);
                    } else {
                        broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState.CARDSTATE_PRESENT, (IccRefreshResponse) ar3.result);
                    }
                } else {
                    CatLog.d((Object) this, "IccRefresh Message is null");
                }
            }
        } else if (this.mHwCustCatService != null) {
            this.mHwCustCatService.broadcastFileChangeNotification(this.mSlotId);
        }
    }

    private void broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState cardState, IccRefreshResponse iccRefreshState) {
        Intent intent = new Intent(AppInterface.CAT_ICC_STATUS_CHANGE);
        intent.addFlags(268435456);
        boolean cardPresent = cardState == IccCardStatus.CardState.CARDSTATE_PRESENT;
        if (iccRefreshState != null) {
            intent.putExtra(AppInterface.REFRESH_RESULT, iccRefreshState.refreshResult);
            CatLog.d((Object) this, "Sending IccResult with Result: " + iccRefreshState.refreshResult);
        }
        intent.putExtra(AppInterface.CARD_STATUS, cardPresent);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.putExtra("SLOT_ID", this.mSlotId);
        CatLog.d((Object) this, "Sending Card Status: " + cardState + " cardPresent: " + cardPresent + "SLOT_ID: " + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void broadcastAlphaMessage(String alphaString) {
        CatLog.d((Object) this, "Broadcasting CAT Alpha message from card: " + alphaString);
        Intent intent = new Intent(AppInterface.CAT_ALPHA_NOTIFY_ACTION);
        intent.addFlags(268435456);
        intent.putExtra(AppInterface.ALPHA_STRING, alphaString);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

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
            CatLog.d((Object) this, "validateResponse: SET_UP_CALL");
            validResponse = true;
        }
        if (resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_EVENT_LIST.value() || resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_MENU.value()) {
            CatLog.d((Object) this, "CmdType: " + resMsg.mCmdDet.typeOfCommand);
            validResponse = true;
        } else if (this.mCurrntCmd != null) {
            validResponse = resMsg.mCmdDet.compareTo(this.mCurrntCmd.mCmdDet);
            CatLog.d((Object) this, "isResponse for last valid cmd: " + validResponse);
        }
        return validResponse;
    }

    private boolean removeMenu(Menu menu) {
        try {
            return menu.items.size() == 1 && menu.items.get(0) == null;
        } catch (NullPointerException e) {
            CatLog.d((Object) this, "Unable to get Menu's items size");
            return true;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007c, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007e, code lost:
        r7 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00cc, code lost:
        r10 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d0, code lost:
        if (r9 == null) goto L_0x019e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00d2, code lost:
        r0 = com.android.internal.telephony.cat.CatService.AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[r9.ordinal()];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00db, code lost:
        if (r0 == 5) goto L_0x017f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00df, code lost:
        if (r0 == 16) goto L_0x0175;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e3, code lost:
        if (r0 == 18) goto L_0x0152;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00e5, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e6, code lost:
        switch(r0) {
            case 1: goto L_0x0143;
            case 2: goto L_0x0132;
            default: goto L_0x00e9;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e9, code lost:
        switch(r0) {
            case 7: goto L_0x0120;
            case 8: goto L_0x0115;
            case 9: goto L_0x00ee;
            case 10: goto L_0x00ee;
            default: goto L_0x00ec;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f0, code lost:
        if (r11.mCurrntCmd == null) goto L_0x01b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f2, code lost:
        r0 = r11.mCurrntCmd.geInput();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00fa, code lost:
        if (r0.yesNo != false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00fc, code lost:
        if (r10 != false) goto L_0x01b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00fe, code lost:
        r7 = new com.android.internal.telephony.cat.GetInkeyInputResponseData(r12.mUsersInput, r0.ucs2, r0.packed);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x010b, code lost:
        r7 = new com.android.internal.telephony.cat.GetInkeyInputResponseData(r12.mUsersYesNoSelection);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0115, code lost:
        r7 = new com.android.internal.telephony.cat.SelectItemResponseData(r12.mUsersMenuSelection);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0124, code lost:
        if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.LAUNCH_BROWSER_ERROR) goto L_0x012c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0126, code lost:
        r12.setAdditionalInfo(4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x012c, code lost:
        r12.mIncludeAdditionalInfo = false;
        r12.mAdditionalInfo = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0136, code lost:
        if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS) goto L_0x013d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0138, code lost:
        r12.setAdditionalInfo(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x013d, code lost:
        r12.mIncludeAdditionalInfo = false;
        r12.mAdditionalInfo = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0147, code lost:
        if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.HELP_INFO_REQUIRED) goto L_0x014a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x014a, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x014b, code lost:
        sendMenuSelection(r12.mUsersMenuSelection, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0151, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0156, code lost:
        if (com.android.internal.telephony.HuaweiTelephonyConfigs.isModemBipEnable() != false) goto L_0x0175;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x015c, code lost:
        if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.OK) goto L_0x01b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0160, code lost:
        if (r12.mUsersConfirm == false) goto L_0x01b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0164, code lost:
        if (r11.mCurrntCmd == null) goto L_0x01b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0166, code lost:
        r11.mCurrntCmd.setWifiConnectedFlag(r11.mIsWifiConnected);
        r11.mBipProxy.handleBipCommand(r11.mCurrntCmd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0174, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0175, code lost:
        r11.mCmdIf.handleCallSetupRequestFromSim(r12.mUsersConfirm, null);
        r11.mCurrntCmd = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x017e, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0181, code lost:
        if (5 != r12.mEventValue) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0183, code lost:
        eventDownload(r12.mEventValue, 2, 129, r12.mAddedInfo, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0190, code lost:
        eventDownload(r12.mEventValue, 130, 129, r12.mAddedInfo, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x019d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x019e, code lost:
        com.android.internal.telephony.cat.CatLog.d((java.lang.Object) r11, "handleCmdResponse() bad Cmd details=" + r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01b3, code lost:
        sendTerminalResponse(r8, r12.mResCode, r12.mIncludeAdditionalInfo, r12.mAdditionalInfo, r7);
        r11.mCurrntCmd = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01c1, code lost:
        return;
     */
    private void handleCmdResponse(CatResponseMessage resMsg) {
        ResponseData resp;
        if (resMsg != null && validateResponse(resMsg)) {
            CatLog.d((Object) this, "code is " + resMsg.mResCode + ";type is " + resMsg.mCmdDet.typeOfCommand);
            if (!(resMsg.getCmdDetails() == null || this.mCurrntCmd == null || this.mCurrntCmd.mCmdDet == null)) {
                resMsg.getCmdDetails().compRequired = this.mCurrntCmd.mCmdDet.compRequired;
            }
            if (resMsg.envelopeCmd != null) {
                this.mCmdIf.sendEnvelope(resMsg.envelopeCmd, null);
                return;
            }
            ResponseData resp2 = null;
            boolean helpRequired = false;
            CommandDetails cmdDet = resMsg.getCmdDetails();
            AppInterface.CommandType type = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
            switch (resMsg.mResCode) {
                case HELP_INFO_REQUIRED:
                    helpRequired = true;
                    break;
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
                    break;
                case BACKWARD_MOVE_BY_USER:
                case USER_NOT_ACCEPT:
                    if (type == AppInterface.CommandType.SET_UP_CALL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    } else if (type != AppInterface.CommandType.OPEN_CHANNEL) {
                        resp = null;
                    } else if (HuaweiTelephonyConfigs.isModemBipEnable()) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    } else if (resMsg.mUsersConfirm || this.mCurrntCmd == null || !this.mCurrntCmd.geTextMessage().responseNeeded) {
                        break;
                    } else {
                        CatCmdMessage.ChannelSettings params = this.mCurrntCmd.getChannelSettings();
                        resMsg.mResCode = ResultCode.USER_NOT_ACCEPT;
                        resp = new OpenChannelResponseData(params.bufSize, null, params.bearerDescription);
                    }
                    break;
                case NO_RESPONSE_FROM_USER:
                    if (type == AppInterface.CommandType.SET_UP_CALL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    break;
                case UICC_SESSION_TERM_BY_USER:
                    break;
                default:
                    return;
            }
        }
    }

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
        if (uiccProfile != null) {
            ca = uiccProfile.getApplicationIndex(0);
            if (ca != null) {
                ir = ca.getIccRecords();
            }
        }
        synchronized (sInstanceLock) {
            if (ir != null) {
                try {
                    if (mIccRecords != ir) {
                        if (mIccRecords != null) {
                            mIccRecords.unregisterForRecordsLoaded(this);
                        }
                        CatLog.d((Object) this, "Reinitialize the Service with SIMRecords and UiccCardApplication");
                        mIccRecords = ir;
                        mUiccApplication = ca;
                        mIccRecords.registerForRecordsLoaded(this, 20, null);
                        CatLog.d((Object) this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
                    }
                } catch (Throwable th) {
                    throw th;
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
                CatLog.d((Object) this, "New Card State = " + newState + " Old Card State = " + oldState);
            }
            if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState != IccCardStatus.CardState.CARDSTATE_PRESENT) {
                broadcastCardStateAndIccRefreshResp(newState, null);
            } else if (oldState != IccCardStatus.CardState.CARDSTATE_PRESENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT) {
                this.mCmdIf.reportStkServiceIsRunning(null);
            } else if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT && oldMainSlot != newMainSlot) {
                CatLog.d((Object) this, "switch the main slot! oldMainSlot=" + oldMainSlot + " newMainSlot=" + newMainSlot);
                this.mCmdIf.reportStkServiceIsRunning(null);
            }
        }
    }

    public void sendBroadcastToOtaUI(int OtaType, boolean processResult) {
        if (this.mHwCustCatService != null) {
            this.mHwCustCatService.sendBroadcastToOtaUI(OtaType, processResult);
        }
    }

    public synchronized void onOtaCommand(int otaType) {
        CatLog.d((Object) this, "enter on otacommand otatype=" + otaType);
        if (this.mHwCustCatService != null) {
            CatLog.d((Object) this, "on otacommand otatype=" + otaType);
            this.mHwCustCatService.onOtaCommand(otaType);
        }
    }

    private void removeFailMsg(CommandParams cmdParams) {
        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false) && cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL && hasMessages(12)) {
            CatLog.d((Object) this, "received open chanel msg--Remove MSG_ID_NO_OPEN_CHANNEL_RECEIVED message");
            removeMessages(12);
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
}
