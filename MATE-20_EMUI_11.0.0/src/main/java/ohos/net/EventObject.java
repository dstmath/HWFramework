package ohos.net;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;

class EventObject {
    private static final int CLASS_LINK_PROP = 4;
    private static final int CLASS_NET_CAPABILITIES = 2;
    private static final int CLASS_NET_HANDLE = 3;
    private static final int CLASS_NET_SPECIFIER = 1;
    private static final int CLASS_NONE = 0;
    private static final int DATA_MAGIC = 1279544898;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "EventObject");
    private static final int VAL_PARCELABLE = 4;
    public ConnectionProperties connectionProperties = null;
    public NetCapabilities netCapabilities = null;
    public NetHandle netHandle = null;
    public NetSpecifier netSpecifier = null;

    static boolean isValidData(MessageParcel messageParcel) {
        if (messageParcel.readInt() == -1) {
            HiLog.warn(LABEL, "invalid data length", new Object[0]);
            return false;
        }
        int readInt = messageParcel.readInt();
        if (readInt == DATA_MAGIC) {
            return true;
        }
        HiLog.error(LABEL, "invalid Magic number:%{public}d", Integer.valueOf(readInt));
        return false;
    }

    private int convertKey(String str) {
        if ("NetworkRequest".equals(str)) {
            return 1;
        }
        if ("NetworkCapabilities".equals(str)) {
            return 2;
        }
        if ("Network".equals(str)) {
            return 3;
        }
        if ("LinkProperties".equals(str)) {
            return 4;
        }
        HiLog.error(LABEL, "invalid name:%{public}s", str);
        return 0;
    }

    private NetSpecifier getNetSpecifier(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 4 || messageParcel.readString() == null) {
            return null;
        }
        HiLog.info(LABEL, "NetSpecifier unmarshalling", new Object[0]);
        NetSpecifier netSpecifier2 = new NetSpecifier();
        netSpecifier2.unmarshalling(messageParcel);
        return netSpecifier2;
    }

    private NetCapabilities getNetCapabilities(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 4 || messageParcel.readString() == null) {
            return null;
        }
        HiLog.info(LABEL, "NetCapabilities unmarshalling", new Object[0]);
        NetCapabilities netCapabilities2 = new NetCapabilities();
        netCapabilities2.unmarshalling(messageParcel);
        return netCapabilities2;
    }

    private NetHandle getNetHandle(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 4 || messageParcel.readString() == null) {
            return null;
        }
        HiLog.info(LABEL, "NetHandle unmarshalling", new Object[0]);
        NetHandle netHandle2 = new NetHandle();
        netHandle2.unmarshalling(messageParcel);
        return netHandle2;
    }

    private ConnectionProperties getConnectionProperties(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 4 || messageParcel.readString() == null) {
            return null;
        }
        HiLog.info(LABEL, "ConectionProperties unmarshalling", new Object[0]);
        ConnectionProperties connectionProperties2 = new ConnectionProperties();
        connectionProperties2.unmarshalling(messageParcel);
        return connectionProperties2;
    }

    public void unmarshalling(MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (readInt > 4) {
            HiLog.error(LABEL, "unmarshalling size: %{public}d", Integer.valueOf(readInt));
            return;
        }
        for (int i = 0; i < readInt; i++) {
            int convertKey = convertKey(messageParcel.readString());
            if (convertKey == 1) {
                this.netSpecifier = getNetSpecifier(messageParcel);
            } else if (convertKey == 2) {
                this.netCapabilities = getNetCapabilities(messageParcel);
            } else if (convertKey == 3) {
                this.netHandle = getNetHandle(messageParcel);
            } else if (convertKey == 4) {
                this.connectionProperties = getConnectionProperties(messageParcel);
            }
        }
    }
}
