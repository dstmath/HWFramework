package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.cat.InterfaceTransportLevel.TransportProtocol;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;

public class CatCmdMessage implements Parcelable {
    private static final /* synthetic */ int[] -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues = null;
    public static final Creator<CatCmdMessage> CREATOR = null;
    private BrowserSettings mBrowserSettings;
    private CallSettings mCallSettings;
    private ChannelSettings mChannelSettings;
    CommandDetails mCmdDet;
    private DataSettings mDataSettings;
    private Input mInput;
    private boolean mIsWifiConnected;
    private String mLanguageNotification;
    private boolean mLoadIconFailed;
    private Menu mMenu;
    private SetupEventListSettings mSetupEventListSettings;
    private TextMessage mTextMsg;
    private ToneSettings mToneSettings;

    public class BrowserSettings {
        public LaunchBrowserMode mode;
        public String url;
    }

    public final class BrowserTerminationCauses {
        public static final int ERROR_TERMINATION = 1;
        public static final int USER_TERMINATION = 0;
    }

    public class CallSettings {
        public TextMessage callMsg;
        public TextMessage confirmMsg;
    }

    public static class ChannelSettings {
        public BearerDescription bearerDescription;
        public int bufSize;
        public int channel;
        public byte[] destinationAddress;
        public String networkAccessName;
        public int port;
        public TransportProtocol protocol;
        public String userLogin;
        public String userPassword;
    }

    public static class DataSettings {
        public int channel;
        public byte[] data;
        public int length;
    }

    public final class SetupEventListConstants {
        public static final int BROWSER_TERMINATION_EVENT = 8;
        public static final int BROWSING_STATUS_EVENT = 15;
        public static final int CHANNEL_STATUS_EVENT = 10;
        public static final int DATA_AVAILABLE_EVENT = 9;
        public static final int IDLE_SCREEN_AVAILABLE_EVENT = 5;
        public static final int LANGUAGE_SELECTION_EVENT = 7;
        public static final int USER_ACTIVITY_EVENT = 4;
    }

    public class SetupEventListSettings {
        public int[] eventList;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues() {
        if (-com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues != null) {
            return -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = 4;
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
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.CatCmdMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.CatCmdMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatCmdMessage.<clinit>():void");
    }

    public void setWifiConnectedFlag(boolean wifiConnected) {
        this.mIsWifiConnected = wifiConnected;
    }

    public boolean getWifiConnectedFlag() {
        return this.mIsWifiConnected;
    }

    CatCmdMessage(CommandParams cmdParams) {
        this.mBrowserSettings = null;
        this.mToneSettings = null;
        this.mCallSettings = null;
        this.mLoadIconFailed = false;
        this.mSetupEventListSettings = null;
        this.mChannelSettings = null;
        this.mDataSettings = null;
        this.mIsWifiConnected = false;
        this.mCmdDet = cmdParams.mCmdDet;
        this.mLoadIconFailed = cmdParams.mLoadIconFailed;
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[getCmdType().ordinal()]) {
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    CloseChannelParams ccp = (CloseChannelParams) cmdParams;
                    this.mTextMsg = ccp.alertMsg;
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = ccp.channel;
                    this.mDataSettings.length = 0;
                    this.mDataSettings.data = null;
                    return;
                case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = 0;
                    this.mDataSettings.length = 0;
                    this.mDataSettings.data = null;
                    return;
                case CharacterSets.ISO_8859_5 /*8*/:
                    OpenChannelParams ocp = (OpenChannelParams) cmdParams;
                    this.mTextMsg = ocp.confirmMsg;
                    this.mChannelSettings = new ChannelSettings();
                    this.mChannelSettings.channel = 0;
                    this.mChannelSettings.protocol = ocp.itl.protocol;
                    this.mChannelSettings.port = ocp.itl.port;
                    this.mChannelSettings.bufSize = ocp.bufSize;
                    this.mChannelSettings.destinationAddress = ocp.destinationAddress;
                    this.mChannelSettings.bearerDescription = ocp.bearerDescription;
                    this.mChannelSettings.networkAccessName = ocp.networkAccessName;
                    this.mChannelSettings.userLogin = ocp.userLogin;
                    this.mChannelSettings.userPassword = ocp.userPassword;
                    return;
                case CharacterSets.ISO_8859_8 /*11*/:
                    ReceiveDataParams rdp = (ReceiveDataParams) cmdParams;
                    this.mTextMsg = rdp.textMsg;
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = rdp.channel;
                    this.mDataSettings.length = rdp.datLen;
                    this.mDataSettings.data = null;
                    return;
                case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                    SendDataParams sdp = (SendDataParams) cmdParams;
                    this.mTextMsg = sdp.textMsg;
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = sdp.channel;
                    this.mDataSettings.length = 0;
                    this.mDataSettings.data = sdp.data;
                    return;
            }
        }
        switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[getCmdType().ordinal()]) {
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
            case CharacterSets.ISO_8859_5 /*8*/:
            case CharacterSets.ISO_8859_8 /*11*/:
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                this.mTextMsg = ((BIPClientParams) cmdParams).mTextMsg;
                break;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
            case PduHeaders.MMS_VERSION_1_0 /*16*/:
            case PduHeaders.MMS_VERSION_1_1 /*17*/:
            case PduHeaders.MMS_VERSION_1_2 /*18*/:
            case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                this.mTextMsg = ((DisplayTextParams) cmdParams).mTextMsg;
                break;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                this.mTextMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                break;
            case CharacterSets.ISO_8859_1 /*4*/:
            case CharacterSets.ISO_8859_2 /*5*/:
                this.mInput = ((GetInputParams) cmdParams).mInput;
                break;
            case CharacterSets.ISO_8859_4 /*7*/:
                this.mTextMsg = ((LaunchBrowserParams) cmdParams).mConfirmMsg;
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = ((LaunchBrowserParams) cmdParams).mUrl;
                this.mBrowserSettings.mode = ((LaunchBrowserParams) cmdParams).mMode;
                break;
            case CharacterSets.ISO_8859_6 /*9*/:
                PlayToneParams params = (PlayToneParams) cmdParams;
                this.mToneSettings = params.mSettings;
                this.mTextMsg = params.mTextMsg;
                break;
            case UserData.ASCII_CR_INDEX /*13*/:
            case CallFailCause.NUMBER_CHANGED /*22*/:
                this.mMenu = ((SelectItemParams) cmdParams).mMenu;
                break;
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                this.mCallSettings.callMsg = ((CallSetupParams) cmdParams).mCallMsg;
                break;
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                this.mSetupEventListSettings = new SetupEventListSettings();
                this.mSetupEventListSettings.eventList = ((SetEventListParams) cmdParams).mEventInfo;
                break;
        }
    }

    public CatCmdMessage(Parcel in) {
        boolean z;
        this.mBrowserSettings = null;
        this.mToneSettings = null;
        this.mCallSettings = null;
        this.mLoadIconFailed = false;
        this.mSetupEventListSettings = null;
        this.mChannelSettings = null;
        this.mDataSettings = null;
        this.mIsWifiConnected = false;
        this.mCmdDet = (CommandDetails) in.readParcelable(null);
        this.mTextMsg = (TextMessage) in.readParcelable(null);
        this.mMenu = (Menu) in.readParcelable(null);
        this.mInput = (Input) in.readParcelable(null);
        if (in.readByte() == (byte) 1) {
            z = true;
        } else {
            z = false;
        }
        this.mLoadIconFailed = z;
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[getCmdType().ordinal()]) {
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                case CharacterSets.ISO_8859_8 /*11*/:
                case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = in.readInt();
                    this.mDataSettings.length = in.readInt();
                    this.mDataSettings.data = null;
                    int len = in.readInt();
                    if (len > 0) {
                        this.mDataSettings.data = new byte[len];
                        in.readByteArray(this.mDataSettings.data);
                    }
                    return;
                case CharacterSets.ISO_8859_5 /*8*/:
                    this.mChannelSettings = new ChannelSettings();
                    this.mChannelSettings.channel = in.readInt();
                    this.mChannelSettings.protocol = TransportProtocol.values()[in.readInt()];
                    this.mChannelSettings.port = in.readInt();
                    this.mChannelSettings.bufSize = in.readInt();
                    this.mChannelSettings.destinationAddress = new byte[in.readInt()];
                    if (this.mChannelSettings.destinationAddress.length > 0) {
                        in.readByteArray(this.mChannelSettings.destinationAddress);
                    }
                    this.mChannelSettings.bearerDescription = (BearerDescription) in.readValue(BearerDescription.class.getClassLoader());
                    this.mChannelSettings.networkAccessName = in.readString();
                    this.mChannelSettings.userLogin = in.readString();
                    this.mChannelSettings.userPassword = in.readString();
                    return;
            }
        }
        switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[getCmdType().ordinal()]) {
            case CharacterSets.ISO_8859_3 /*6*/:
                this.mLanguageNotification = in.readString();
                break;
            case CharacterSets.ISO_8859_4 /*7*/:
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = in.readString();
                this.mBrowserSettings.mode = LaunchBrowserMode.values()[in.readInt()];
                break;
            case CharacterSets.ISO_8859_6 /*9*/:
                this.mToneSettings = (ToneSettings) in.readParcelable(null);
                break;
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = (TextMessage) in.readParcelable(null);
                this.mCallSettings.callMsg = (TextMessage) in.readParcelable(null);
                break;
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                this.mSetupEventListSettings = new SetupEventListSettings();
                int length = in.readInt();
                this.mSetupEventListSettings.eventList = new int[length];
                for (int i = 0; i < length; i++) {
                    this.mSetupEventListSettings.eventList[i] = in.readInt();
                }
                break;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeParcelable(this.mCmdDet, 0);
        dest.writeParcelable(this.mTextMsg, 0);
        dest.writeParcelable(this.mMenu, 0);
        dest.writeParcelable(this.mInput, 0);
        if (this.mLoadIconFailed) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[getCmdType().ordinal()]) {
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                case CharacterSets.ISO_8859_8 /*11*/:
                case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                    dest.writeInt(this.mDataSettings.channel);
                    dest.writeInt(this.mDataSettings.length);
                    int len = 0;
                    if (this.mDataSettings.data != null) {
                        len = this.mDataSettings.data.length;
                    }
                    dest.writeInt(len);
                    if (len > 0) {
                        dest.writeByteArray(this.mDataSettings.data);
                    }
                    return;
                case CharacterSets.ISO_8859_5 /*8*/:
                    dest.writeInt(this.mChannelSettings.channel);
                    dest.writeInt(this.mChannelSettings.protocol.value());
                    dest.writeInt(this.mChannelSettings.port);
                    dest.writeInt(this.mChannelSettings.bufSize);
                    if (this.mChannelSettings.destinationAddress != null) {
                        dest.writeInt(this.mChannelSettings.destinationAddress.length);
                        dest.writeByteArray(this.mChannelSettings.destinationAddress);
                    } else {
                        dest.writeInt(0);
                    }
                    dest.writeValue(this.mChannelSettings.bearerDescription);
                    dest.writeString(this.mChannelSettings.networkAccessName);
                    dest.writeString(this.mChannelSettings.userLogin);
                    dest.writeString(this.mChannelSettings.userPassword);
                    return;
            }
        }
        switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[getCmdType().ordinal()]) {
            case CharacterSets.ISO_8859_3 /*6*/:
                dest.writeString(this.mLanguageNotification);
                break;
            case CharacterSets.ISO_8859_4 /*7*/:
                dest.writeString(this.mBrowserSettings.url);
                dest.writeInt(this.mBrowserSettings.mode.ordinal());
                break;
            case CharacterSets.ISO_8859_6 /*9*/:
                dest.writeParcelable(this.mToneSettings, 0);
                break;
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                dest.writeParcelable(this.mCallSettings.confirmMsg, 0);
                dest.writeParcelable(this.mCallSettings.callMsg, 0);
                break;
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                dest.writeIntArray(this.mSetupEventListSettings.eventList);
                break;
        }
    }

    public int describeContents() {
        return 0;
    }

    public CommandType getCmdType() {
        return CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public Input geInput() {
        return this.mInput;
    }

    public TextMessage geTextMessage() {
        return this.mTextMsg;
    }

    public BrowserSettings getBrowserSettings() {
        return this.mBrowserSettings;
    }

    public ToneSettings getToneSettings() {
        return this.mToneSettings;
    }

    public CallSettings getCallSettings() {
        return this.mCallSettings;
    }

    public DataSettings getDataSettings() {
        return this.mDataSettings;
    }

    public ChannelSettings getChannelSettings() {
        return this.mChannelSettings;
    }

    public int getCommandQualifier() {
        return this.mCmdDet.commandQualifier;
    }

    public SetupEventListSettings getSetEventList() {
        return this.mSetupEventListSettings;
    }

    public boolean hasIconLoadFailed() {
        return this.mLoadIconFailed;
    }

    public String getLanguageNotification() {
        return this.mLanguageNotification;
    }
}
