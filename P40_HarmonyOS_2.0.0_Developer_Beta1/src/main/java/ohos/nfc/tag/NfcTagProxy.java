package ohos.nfc.tag;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcCommProxy;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Sequenceable;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

/* access modifiers changed from: package-private */
public class NfcTagProxy extends NfcCommProxy implements ITagInfo {
    private static final int FALSE = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NfcTagProxy");
    private static final String TAG_TOKEN = "ohos.nfc.ITagInfo";
    private static final int TRUE = 1;
    private static NfcTagProxy sNfcTagProxy;

    private NfcTagProxy() {
        super(SystemAbilityDefinition.NFC_MANAGER_SYS_ABILITY_ID);
    }

    public static synchronized NfcTagProxy getInstance() {
        NfcTagProxy nfcTagProxy;
        synchronized (NfcTagProxy.class) {
            if (sNfcTagProxy == null) {
                sNfcTagProxy = new NfcTagProxy();
            }
            nfcTagProxy = sNfcTagProxy;
        }
        return nfcTagProxy;
    }

    @Override // ohos.nfc.tag.ITagInfo
    public boolean connectTag(int i, int i2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        obtain.writeInt(i);
        obtain.writeInt(i2);
        return requestWithoutCheck(4, obtain).readBoolean();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public boolean reconnectTag(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        obtain.writeInt(i);
        return requestWithoutCheck(5, obtain).readBoolean();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public boolean isTagConnected(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        obtain.writeInt(i);
        return requestWithoutCheck(6, obtain).readBoolean();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public boolean setSendDataTimeout(int i, int i2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        obtain.writeInt(i);
        obtain.writeInt(i2);
        return requestWithoutCheck(7, obtain).readBoolean();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public int getSendDataTimeout(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        obtain.writeInt(i);
        return requestWithoutCheck(8, obtain).readInt();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public byte[] sendData(int i, byte[] bArr) throws RemoteException {
        if (bArr == null || bArr.length == 0) {
            HiLog.warn(LABEL, "method sendData input param is null or empty", new Object[0]);
            return new byte[0];
        }
        MessageParcel obtain = MessageParcel.obtain();
        NfcKitsUtils.writeInterfaceToken(NfcKitsUtils.TAG_DESCRIPTOR, obtain);
        obtain.writeInt(i);
        obtain.writeByteArray(bArr);
        MessageParcel request = request(9, obtain);
        ResponseInfo responseInfo = new ResponseInfo();
        if (responseInfo instanceof Sequenceable) {
            request.readSequenceable(responseInfo);
        }
        return responseInfo.getResponse();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public int getMaxSendLength(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        obtain.writeInt(i);
        return requestWithoutCheck(10, obtain).readInt();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public void resetSendDataTimeout() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(TAG_TOKEN);
        request(11, obtain);
    }

    @Override // ohos.nfc.tag.ITagInfo
    public boolean isNdefTag(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        NfcKitsUtils.writeInterfaceToken(NfcKitsUtils.TAG_DESCRIPTOR, obtain);
        obtain.writeInt(i);
        return requestWithoutCheck(31, obtain).readBoolean();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public NdefMessage ndefRead(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        NfcKitsUtils.writeInterfaceToken(NfcKitsUtils.TAG_DESCRIPTOR, obtain);
        obtain.writeInt(i);
        MessageParcel requestWithoutCheck = requestWithoutCheck(32, obtain);
        NdefMessage ndefMessage = new NdefMessage(new MessageRecord[0]);
        if (ndefMessage instanceof Sequenceable) {
            requestWithoutCheck.readSequenceable(ndefMessage);
        }
        return ndefMessage;
    }

    @Override // ohos.nfc.tag.ITagInfo
    public int ndefWrite(int i, NdefMessage ndefMessage) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        NfcKitsUtils.writeInterfaceToken(NfcKitsUtils.TAG_DESCRIPTOR, obtain);
        obtain.writeInt(i);
        if (ndefMessage instanceof Sequenceable) {
            obtain.writeSequenceable(ndefMessage);
        }
        return requestWithoutCheck(33, obtain).readInt();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public boolean canSetReadOnly(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        NfcKitsUtils.writeInterfaceToken(NfcKitsUtils.TAG_DESCRIPTOR, obtain);
        obtain.writeInt(i);
        return requestWithoutCheck(35, obtain).readBoolean();
    }

    @Override // ohos.nfc.tag.ITagInfo
    public int ndefSetReadOnly(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        NfcKitsUtils.writeInterfaceToken(NfcKitsUtils.TAG_DESCRIPTOR, obtain);
        obtain.writeInt(i);
        return requestWithoutCheck(34, obtain).readInt();
    }
}
