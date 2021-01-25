package ohos.nfc.tag;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.RemoteException;

public abstract class TagManager {
    public static final int ISO_DEP = 3;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "TagManager");
    public static final int MIFARE_CLASSIC = 8;
    public static final int MIFARE_ULTRALIGHT = 9;
    public static final int NDEF = 6;
    public static final int NFC_A = 1;
    public static final int NFC_B = 2;
    private boolean mIsTagConnected = false;
    private NfcTagProxy mNfcTagProxy;
    private int mTagHandle;
    private TagInfo mTagInfo;
    private int mTagProfile;

    public TagManager(TagInfo tagInfo, int i) {
        if (tagInfo != null) {
            this.mTagInfo = tagInfo;
            this.mTagProfile = i;
            this.mNfcTagProxy = NfcTagProxy.getInstance();
            this.mTagHandle = this.mTagInfo.getTagHandle();
        }
    }

    public TagInfo getTagInfo() {
        return this.mTagInfo;
    }

    public boolean connectTag() {
        try {
            this.mIsTagConnected = this.mNfcTagProxy.connectTag(this.mTagHandle, this.mTagProfile);
            return this.mIsTagConnected;
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "connectTag open RemoteException", new Object[0]);
            return false;
        }
    }

    public void reset() {
        resetSendDataTimeout();
        reconnectTag();
        this.mIsTagConnected = false;
    }

    public boolean isTagConnected() {
        try {
            return this.mNfcTagProxy.isTagConnected(this.mTagHandle);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isTagConnected open RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean setSendDataTimeout(int i) {
        try {
            return this.mNfcTagProxy.setSendDataTimeout(this.mTagProfile, i);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "setSendDataTimeout open RemoteException", new Object[0]);
            return false;
        }
    }

    public int getSendDataTimeout() {
        try {
            return this.mNfcTagProxy.getSendDataTimeout(this.mTagProfile);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getSendDataTimeout open RemoteException", new Object[0]);
            return 0;
        }
    }

    public byte[] sendData(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            HiLog.warn(LABEL, "method sendData input param is null or empty", new Object[0]);
            return new byte[0];
        }
        try {
            return this.mNfcTagProxy.sendData(this.mTagHandle, bArr);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "sendData open RemoteException", new Object[0]);
            return new byte[0];
        }
    }

    public int getMaxSendLength() {
        try {
            return this.mNfcTagProxy.getMaxSendLength(this.mTagProfile);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getMaxSendLength open RemoteException", new Object[0]);
            return 0;
        }
    }

    public void checkConnected() {
        if (!this.mIsTagConnected) {
            throw new IllegalStateException("Call connectTag() first!");
        }
    }

    private boolean reconnectTag() {
        try {
            return this.mNfcTagProxy.reconnectTag(this.mTagHandle);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "reconnectTag open RemoteException", new Object[0]);
            return false;
        }
    }

    private void resetSendDataTimeout() {
        try {
            this.mNfcTagProxy.resetSendDataTimeout();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "resetSendDataTimeout open RemoteException", new Object[0]);
        }
    }
}
