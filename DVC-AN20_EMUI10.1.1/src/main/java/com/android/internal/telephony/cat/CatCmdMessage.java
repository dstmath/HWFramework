package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.InterfaceTransportLevel;

public class CatCmdMessage implements Parcelable {
    public static final Parcelable.Creator<CatCmdMessage> CREATOR = new Parcelable.Creator<CatCmdMessage>() {
        /* class com.android.internal.telephony.cat.CatCmdMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CatCmdMessage createFromParcel(Parcel in) {
            return HwTelephonyFactory.getHwUiccManager().createHwCatCmdMessage(in);
        }

        @Override // android.os.Parcelable.Creator
        public CatCmdMessage[] newArray(int size) {
            return new CatCmdMessage[size];
        }
    };
    private static final int INVALID_SUBID = -1;
    private BrowserSettings mBrowserSettings;
    @UnsupportedAppUsage
    private CallSettings mCallSettings;
    private ChannelSettings mChannelSettings;
    @UnsupportedAppUsage
    CommandDetails mCmdDet;
    private DataSettings mDataSettings;
    @UnsupportedAppUsage
    private Input mInput;
    private boolean mIsWifiConnected;
    private String mLanguageNotification;
    private boolean mLoadIconFailed;
    @UnsupportedAppUsage
    private Menu mMenu;
    private SetupEventListSettings mSetupEventListSettings;
    private int mSlotId;
    @UnsupportedAppUsage
    private TextMessage mTextMsg;
    private ToneSettings mToneSettings;

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

    public class BrowserSettings {
        public LaunchBrowserMode mode;
        public String url;

        public BrowserSettings() {
        }
    }

    public class CallSettings {
        @UnsupportedAppUsage
        public TextMessage callMsg;
        @UnsupportedAppUsage
        public TextMessage confirmMsg;

        public CallSettings() {
        }
    }

    public class SetupEventListSettings {
        @UnsupportedAppUsage
        public int[] eventList;

        public SetupEventListSettings() {
        }
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

    public final class BrowserTerminationCauses {
        public static final int ERROR_TERMINATION = 1;
        public static final int USER_TERMINATION = 0;

        public BrowserTerminationCauses() {
        }
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
            int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
            if (i == 1) {
                OpenChannelParams ocp = (OpenChannelParams) cmdParams;
                this.mTextMsg = ocp.confirmMsg;
                this.mChannelSettings = new ChannelSettings();
                ChannelSettings channelSettings = this.mChannelSettings;
                channelSettings.channel = 0;
                channelSettings.protocol = ocp.itl.protocol;
                this.mChannelSettings.port = ocp.itl.port;
                this.mChannelSettings.bufSize = ocp.bufSize;
                this.mChannelSettings.destinationAddress = ocp.destinationAddress;
                this.mChannelSettings.bearerDescription = ocp.bearerDescription;
                this.mChannelSettings.networkAccessName = ocp.networkAccessName;
                this.mChannelSettings.userLogin = ocp.userLogin;
                this.mChannelSettings.userPassword = ocp.userPassword;
                return;
            } else if (i == 2) {
                CloseChannelParams ccp = (CloseChannelParams) cmdParams;
                this.mTextMsg = ccp.alertMsg;
                this.mDataSettings = new DataSettings();
                this.mDataSettings.channel = ccp.channel;
                DataSettings dataSettings = this.mDataSettings;
                dataSettings.length = 0;
                dataSettings.data = null;
                return;
            } else if (i == 3) {
                ReceiveDataParams rdp = (ReceiveDataParams) cmdParams;
                this.mTextMsg = rdp.textMsg;
                this.mDataSettings = new DataSettings();
                this.mDataSettings.channel = rdp.channel;
                this.mDataSettings.length = rdp.datLen;
                this.mDataSettings.data = null;
                return;
            } else if (i == 4) {
                SendDataParams sdp = (SendDataParams) cmdParams;
                this.mTextMsg = sdp.textMsg;
                this.mDataSettings = new DataSettings();
                this.mDataSettings.channel = sdp.channel;
                DataSettings dataSettings2 = this.mDataSettings;
                dataSettings2.length = 0;
                dataSettings2.data = sdp.data;
                return;
            } else if (i == 5) {
                this.mDataSettings = new DataSettings();
                DataSettings dataSettings3 = this.mDataSettings;
                dataSettings3.channel = 0;
                dataSettings3.length = 0;
                dataSettings3.data = null;
                return;
            }
        }
        switch (getCmdType()) {
            case OPEN_CHANNEL:
            case CLOSE_CHANNEL:
            case RECEIVE_DATA:
            case SEND_DATA:
                this.mTextMsg = ((BIPClientParams) cmdParams).mTextMsg;
                return;
            case GET_CHANNEL_STATUS:
                this.mTextMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                return;
            case SET_UP_MENU:
            case SELECT_ITEM:
                this.mMenu = ((SelectItemParams) cmdParams).mMenu;
                return;
            case DISPLAY_TEXT:
            case SET_UP_IDLE_MODE_TEXT:
            case SEND_DTMF:
            case SEND_SMS:
            case REFRESH:
            case RUN_AT:
            case SEND_SS:
            case SEND_USSD:
                this.mTextMsg = ((DisplayTextParams) cmdParams).mTextMsg;
                return;
            case GET_INPUT:
            case GET_INKEY:
                this.mInput = ((GetInputParams) cmdParams).mInput;
                return;
            case LAUNCH_BROWSER:
                this.mTextMsg = ((LaunchBrowserParams) cmdParams).mConfirmMsg;
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = ((LaunchBrowserParams) cmdParams).mUrl;
                this.mBrowserSettings.mode = ((LaunchBrowserParams) cmdParams).mMode;
                return;
            case PLAY_TONE:
                PlayToneParams params = (PlayToneParams) cmdParams;
                this.mToneSettings = params.mSettings;
                this.mTextMsg = params.mTextMsg;
                return;
            case SET_UP_CALL:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                this.mCallSettings.callMsg = ((CallSetupParams) cmdParams).mCallMsg;
                return;
            case SET_UP_EVENT_LIST:
                this.mSetupEventListSettings = new SetupEventListSettings();
                this.mSetupEventListSettings.eventList = ((SetEventListParams) cmdParams).mEventInfo;
                return;
            default:
                return;
        }
    }

    public CatCmdMessage(Parcel in) {
        this.mBrowserSettings = null;
        this.mToneSettings = null;
        this.mCallSettings = null;
        boolean z = false;
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
        this.mLoadIconFailed = in.readByte() == 1 ? true : z;
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
            if (i == 1) {
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
            } else if (i == 2 || i == 3 || i == 4) {
                this.mDataSettings = new DataSettings();
                this.mDataSettings.channel = in.readInt();
                this.mDataSettings.length = in.readInt();
                this.mDataSettings.data = null;
                int len = in.readInt();
                if (len > 0) {
                    DataSettings dataSettings = this.mDataSettings;
                    dataSettings.data = new byte[len];
                    in.readByteArray(dataSettings.data);
                    return;
                }
                return;
            }
        }
        switch (getCmdType()) {
            case LAUNCH_BROWSER:
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = in.readString();
                this.mBrowserSettings.mode = LaunchBrowserMode.values()[in.readInt()];
                return;
            case PLAY_TONE:
                this.mToneSettings = (ToneSettings) in.readParcelable(null);
                return;
            case SET_UP_CALL:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = (TextMessage) in.readParcelable(null);
                this.mCallSettings.callMsg = (TextMessage) in.readParcelable(null);
                return;
            case SET_UP_EVENT_LIST:
                this.mSetupEventListSettings = new SetupEventListSettings();
                int length = in.readInt();
                this.mSetupEventListSettings.eventList = new int[length];
                for (int i2 = 0; i2 < length; i2++) {
                    this.mSetupEventListSettings.eventList[i2] = in.readInt();
                }
                return;
            case PROVIDE_LOCAL_INFORMATION:
            default:
                return;
            case LANGUAGE_NOTIFICATION:
                this.mLanguageNotification = in.readString();
                return;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCmdDet, 0);
        dest.writeParcelable(this.mTextMsg, 0);
        dest.writeParcelable(this.mMenu, 0);
        dest.writeParcelable(this.mInput, 0);
        dest.writeByte(this.mLoadIconFailed ? (byte) 1 : 0);
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
            if (i == 1) {
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
            } else if (i == 2 || i == 3 || i == 4) {
                dest.writeInt(this.mDataSettings.channel);
                dest.writeInt(this.mDataSettings.length);
                int len = 0;
                if (this.mDataSettings.data != null) {
                    len = this.mDataSettings.data.length;
                }
                dest.writeInt(len);
                if (len > 0) {
                    dest.writeByteArray(this.mDataSettings.data);
                    return;
                }
                return;
            }
        }
        switch (getCmdType()) {
            case LAUNCH_BROWSER:
                dest.writeString(this.mBrowserSettings.url);
                dest.writeInt(this.mBrowserSettings.mode.ordinal());
                return;
            case PLAY_TONE:
                dest.writeParcelable(this.mToneSettings, 0);
                return;
            case SET_UP_CALL:
                dest.writeParcelable(this.mCallSettings.confirmMsg, 0);
                dest.writeParcelable(this.mCallSettings.callMsg, 0);
                return;
            case SET_UP_EVENT_LIST:
                dest.writeIntArray(this.mSetupEventListSettings.eventList);
                return;
            case PROVIDE_LOCAL_INFORMATION:
            default:
                return;
            case LANGUAGE_NOTIFICATION:
                dest.writeString(this.mLanguageNotification);
                return;
        }
    }

    public int describeContents() {
        return 0;
    }

    @UnsupportedAppUsage
    public AppInterface.CommandType getCmdType() {
        return AppInterface.CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public Input geInput() {
        return this.mInput;
    }

    @UnsupportedAppUsage
    public TextMessage geTextMessage() {
        return this.mTextMsg;
    }

    public BrowserSettings getBrowserSettings() {
        return this.mBrowserSettings;
    }

    public ToneSettings getToneSettings() {
        return this.mToneSettings;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public SetupEventListSettings getSetEventList() {
        return this.mSetupEventListSettings;
    }

    @UnsupportedAppUsage
    public boolean hasIconLoadFailed() {
        return this.mLoadIconFailed;
    }

    public String getLanguageNotification() {
        return this.mLanguageNotification;
    }
}
