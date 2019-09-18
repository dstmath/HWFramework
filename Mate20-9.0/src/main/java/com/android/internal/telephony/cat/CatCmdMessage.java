package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.InterfaceTransportLevel;

public class CatCmdMessage implements Parcelable {
    public static final Parcelable.Creator<CatCmdMessage> CREATOR = new Parcelable.Creator<CatCmdMessage>() {
        public CatCmdMessage createFromParcel(Parcel in) {
            return HwTelephonyFactory.getHwUiccManager().createHwCatCmdMessage(in);
        }

        public CatCmdMessage[] newArray(int size) {
            return new CatCmdMessage[size];
        }
    };
    private static final int INVALID_SUBID = -1;
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
    private int mSlotId;
    private TextMessage mTextMsg;
    private ToneSettings mToneSettings;

    public class BrowserSettings {
        public LaunchBrowserMode mode;
        public String url;

        public BrowserSettings() {
        }
    }

    public final class BrowserTerminationCauses {
        public static final int ERROR_TERMINATION = 1;
        public static final int USER_TERMINATION = 0;

        public BrowserTerminationCauses() {
        }
    }

    public class CallSettings {
        public TextMessage callMsg;
        public TextMessage confirmMsg;

        public CallSettings() {
        }
    }

    public static class ChannelSettings {
        public BearerDescription bearerDescription;
        public int bufSize;
        public int channel;
        public byte[] destinationAddress;
        public String networkAccessName;
        public int port;
        public InterfaceTransportLevel.TransportProtocol protocol;
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

        public SetupEventListConstants() {
        }
    }

    public class SetupEventListSettings {
        public int[] eventList;

        public SetupEventListSettings() {
        }
    }

    public void setSlotId(int slotid) {
        this.mSlotId = slotid;
    }

    public int getSlotId() {
        return this.mSlotId;
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
        this.mSlotId = -1;
        this.mIsWifiConnected = false;
        this.mCmdDet = cmdParams.mCmdDet;
        this.mLoadIconFailed = cmdParams.mLoadIconFailed;
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            switch (getCmdType()) {
                case OPEN_CHANNEL:
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
                case CLOSE_CHANNEL:
                    CloseChannelParams ccp = (CloseChannelParams) cmdParams;
                    this.mTextMsg = ccp.alertMsg;
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = ccp.channel;
                    this.mDataSettings.length = 0;
                    this.mDataSettings.data = null;
                    return;
                case RECEIVE_DATA:
                    ReceiveDataParams rdp = (ReceiveDataParams) cmdParams;
                    this.mTextMsg = rdp.textMsg;
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = rdp.channel;
                    this.mDataSettings.length = rdp.datLen;
                    this.mDataSettings.data = null;
                    return;
                case SEND_DATA:
                    SendDataParams sdp = (SendDataParams) cmdParams;
                    this.mTextMsg = sdp.textMsg;
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = sdp.channel;
                    this.mDataSettings.length = 0;
                    this.mDataSettings.data = sdp.data;
                    return;
                case GET_CHANNEL_STATUS:
                    this.mDataSettings = new DataSettings();
                    this.mDataSettings.channel = 0;
                    this.mDataSettings.length = 0;
                    this.mDataSettings.data = null;
                    return;
            }
        }
        switch (getCmdType()) {
            case OPEN_CHANNEL:
            case CLOSE_CHANNEL:
            case RECEIVE_DATA:
            case SEND_DATA:
                this.mTextMsg = ((BIPClientParams) cmdParams).mTextMsg;
                break;
            case GET_CHANNEL_STATUS:
                this.mTextMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                break;
            case SET_UP_MENU:
            case SELECT_ITEM:
                this.mMenu = ((SelectItemParams) cmdParams).mMenu;
                break;
            case DISPLAY_TEXT:
            case SET_UP_IDLE_MODE_TEXT:
            case SEND_DTMF:
            case SEND_SMS:
            case SEND_SS:
            case SEND_USSD:
                this.mTextMsg = ((DisplayTextParams) cmdParams).mTextMsg;
                break;
            case GET_INPUT:
            case GET_INKEY:
                this.mInput = ((GetInputParams) cmdParams).mInput;
                break;
            case LAUNCH_BROWSER:
                this.mTextMsg = ((LaunchBrowserParams) cmdParams).mConfirmMsg;
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = ((LaunchBrowserParams) cmdParams).mUrl;
                this.mBrowserSettings.mode = ((LaunchBrowserParams) cmdParams).mMode;
                break;
            case PLAY_TONE:
                PlayToneParams params = (PlayToneParams) cmdParams;
                this.mToneSettings = params.mSettings;
                this.mTextMsg = params.mTextMsg;
                break;
            case SET_UP_CALL:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                this.mCallSettings.callMsg = ((CallSetupParams) cmdParams).mCallMsg;
                break;
            case SET_UP_EVENT_LIST:
                this.mSetupEventListSettings = new SetupEventListSettings();
                this.mSetupEventListSettings.eventList = ((SetEventListParams) cmdParams).mEventInfo;
                break;
        }
    }

    public CatCmdMessage(Parcel in) {
        this.mBrowserSettings = null;
        this.mToneSettings = null;
        this.mCallSettings = null;
        this.mLoadIconFailed = false;
        this.mSetupEventListSettings = null;
        this.mChannelSettings = null;
        this.mDataSettings = null;
        this.mSlotId = -1;
        this.mIsWifiConnected = false;
        this.mCmdDet = (CommandDetails) in.readParcelable(null);
        this.mTextMsg = (TextMessage) in.readParcelable(null);
        this.mMenu = (Menu) in.readParcelable(null);
        this.mInput = (Input) in.readParcelable(null);
        this.mLoadIconFailed = in.readByte() != 1 ? false : true;
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            switch (getCmdType()) {
                case OPEN_CHANNEL:
                    this.mChannelSettings = new ChannelSettings();
                    this.mChannelSettings.channel = in.readInt();
                    this.mChannelSettings.protocol = InterfaceTransportLevel.TransportProtocol.values()[in.readInt()];
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
                case CLOSE_CHANNEL:
                case RECEIVE_DATA:
                case SEND_DATA:
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
            }
        }
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
        if (i != 22) {
            switch (i) {
                case 16:
                    this.mBrowserSettings = new BrowserSettings();
                    this.mBrowserSettings.url = in.readString();
                    this.mBrowserSettings.mode = LaunchBrowserMode.values()[in.readInt()];
                    break;
                case 17:
                    this.mToneSettings = (ToneSettings) in.readParcelable(null);
                    break;
                case 18:
                    this.mCallSettings = new CallSettings();
                    this.mCallSettings.confirmMsg = (TextMessage) in.readParcelable(null);
                    this.mCallSettings.callMsg = (TextMessage) in.readParcelable(null);
                    break;
                case 19:
                    this.mSetupEventListSettings = new SetupEventListSettings();
                    int length = in.readInt();
                    this.mSetupEventListSettings.eventList = new int[length];
                    for (int i2 = 0; i2 < length; i2++) {
                        this.mSetupEventListSettings.eventList[i2] = in.readInt();
                    }
                    break;
            }
        } else {
            this.mLanguageNotification = in.readString();
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCmdDet, 0);
        dest.writeParcelable(this.mTextMsg, 0);
        dest.writeParcelable(this.mMenu, 0);
        dest.writeParcelable(this.mInput, 0);
        dest.writeByte(this.mLoadIconFailed ? (byte) 1 : 0);
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            switch (getCmdType()) {
                case OPEN_CHANNEL:
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
                case CLOSE_CHANNEL:
                case RECEIVE_DATA:
                case SEND_DATA:
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
            }
        }
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
        if (i != 22) {
            switch (i) {
                case 16:
                    dest.writeString(this.mBrowserSettings.url);
                    dest.writeInt(this.mBrowserSettings.mode.ordinal());
                    break;
                case 17:
                    dest.writeParcelable(this.mToneSettings, 0);
                    break;
                case 18:
                    dest.writeParcelable(this.mCallSettings.confirmMsg, 0);
                    dest.writeParcelable(this.mCallSettings.callMsg, 0);
                    break;
                case 19:
                    dest.writeIntArray(this.mSetupEventListSettings.eventList);
                    break;
            }
        } else {
            dest.writeString(this.mLanguageNotification);
        }
    }

    public int describeContents() {
        return 0;
    }

    public AppInterface.CommandType getCmdType() {
        return AppInterface.CommandType.fromInt(this.mCmdDet.typeOfCommand);
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
