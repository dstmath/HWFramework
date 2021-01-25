package ohos.nfc.tag;

import java.io.IOException;
import ohos.aafwk.content.IntentParams;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.RemoteException;

public class NdefTag extends TagManager {
    public static final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";
    public static final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";
    public static final String EXTRA_NDEF_MSG = "ndefmsg";
    public static final String EXTRA_NDEF_TYPE = "ndeftype";
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NdefTag");
    public static final int NDEF_MODE_READ_ONLY = 1;
    public static final int NDEF_MODE_READ_WRITE = 2;
    public static final int NDEF_MODE_UNKNOWN = 3;
    public static final String NDEF_TYPE_1 = "nfcforum.type1";
    public static final String NDEF_TYPE_2 = "nfcforum.type2";
    public static final String NDEF_TYPE_3 = "nfcforum.type3";
    public static final String NDEF_TYPE_4 = "nfcforum.type4";
    public static final String NDEF_TYPE_ICODE_SLI = "nxp.icode.sli";
    public static final String NDEF_TYPE_MIFARE_CLASSIC = "nxp.mifare.classic";
    public static final String NDEF_TYPE_UNKNOWN = "harmony.ndef.unknown";
    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;
    public static final int TYPE_3 = 3;
    public static final int TYPE_4 = 4;
    public static final int TYPE_ICODE_SLI = 102;
    public static final int TYPE_MIFARE_CLASSIC = 101;
    public static final int TYPE_OTHER = -1;
    private final int mCardState;
    private final int mMaxNdefSize;
    private final NdefMessage mNdefMsg;
    private final int mNdefType;
    private NfcTagProxy mNfcTagProxy;
    private TagInfo mTagInfo;

    public NdefTag(TagInfo tagInfo) {
        super(tagInfo, 6);
        if (tagInfo != null) {
            if (tagInfo.isProfileSupported(6)) {
                IntentParams orElse = tagInfo.getProfileExtras(6).orElse(null);
                if (orElse != null) {
                    this.mMaxNdefSize = ((Integer) orElse.getParam(EXTRA_NDEF_MAXLENGTH)).intValue();
                    this.mCardState = ((Integer) orElse.getParam(EXTRA_NDEF_CARDSTATE)).intValue();
                    if (orElse.getParam(EXTRA_NDEF_MSG) instanceof NdefMessage) {
                        this.mNdefMsg = (NdefMessage) orElse.getParam(EXTRA_NDEF_MSG);
                    } else {
                        this.mNdefMsg = new NdefMessage(new MessageRecord[0]);
                    }
                    this.mNdefType = ((Integer) orElse.getParam(EXTRA_NDEF_TYPE)).intValue();
                } else {
                    throw new NullPointerException("NDEF tech extras are null");
                }
            } else {
                this.mMaxNdefSize = 0;
                this.mCardState = 0;
                this.mNdefMsg = new NdefMessage(new MessageRecord[0]);
                this.mNdefType = 0;
            }
            this.mNfcTagProxy = NfcTagProxy.getInstance();
            this.mTagInfo = getTagInfo();
            return;
        }
        throw new NullPointerException("NDEF TagInfo is null.");
    }

    public NdefMessage getNdefMessage() {
        return this.mNdefMsg;
    }

    public int getNdefMaxSize() {
        return this.mMaxNdefSize;
    }

    public String getTagType() {
        int i = this.mNdefType;
        if (i == 1) {
            return NDEF_TYPE_1;
        }
        if (i == 2) {
            return NDEF_TYPE_2;
        }
        if (i == 3) {
            return NDEF_TYPE_3;
        }
        if (i == 4) {
            return NDEF_TYPE_4;
        }
        if (i != 101) {
            return i != 102 ? NDEF_TYPE_UNKNOWN : NDEF_TYPE_ICODE_SLI;
        }
        return NDEF_TYPE_MIFARE_CLASSIC;
    }

    public NdefMessage readNdefMessage() throws IOException, IllegalArgumentException {
        checkConnected();
        try {
            if (this.mNfcTagProxy != null) {
                int tagHandle = this.mTagInfo.getTagHandle();
                if (this.mNfcTagProxy.isNdefTag(tagHandle)) {
                    NdefMessage ndefRead = this.mNfcTagProxy.ndefRead(tagHandle);
                    if (ndefRead != null || this.mNfcTagProxy.isTagConnected(tagHandle)) {
                        return ndefRead;
                    }
                    throw new IllegalArgumentException("Invalid param when read");
                } else if (this.mNfcTagProxy.isTagConnected(tagHandle)) {
                    return null;
                } else {
                    throw new IllegalArgumentException("Invalid param when read");
                }
            } else {
                throw new IOException("Mock tags don't support this operation.");
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "readNdefMessage open RemoteException", new Object[0]);
            return null;
        }
    }

    public void writeNdefMessage(NdefMessage ndefMessage) throws IOException, IllegalArgumentException {
        checkConnected();
        try {
            if (this.mNfcTagProxy != null) {
                int tagHandle = this.mTagInfo.getTagHandle();
                if (this.mNfcTagProxy.isNdefTag(tagHandle)) {
                    int ndefWrite = this.mNfcTagProxy.ndefWrite(tagHandle, ndefMessage);
                    if (ndefWrite == -8) {
                        throw new IllegalArgumentException("Invalid param when write");
                    } else if (ndefWrite == -1) {
                        throw new IOException("IO Exception occured when write");
                    } else if (ndefWrite != 0) {
                        throw new IOException("IO Exception occured when write");
                    }
                } else {
                    throw new IOException("Tag is not NDEF.");
                }
            } else {
                throw new IOException("Mock tags don't support this operation.");
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "writeNdefMessage open RemoteException", new Object[0]);
        }
    }

    public boolean canSetReadOnly() {
        NfcTagProxy nfcTagProxy = this.mNfcTagProxy;
        if (nfcTagProxy == null) {
            return false;
        }
        try {
            return nfcTagProxy.canSetReadOnly(this.mNdefType);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "canSetReadOnly open RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean setReadOnly() throws IOException {
        checkConnected();
        try {
            if (this.mNfcTagProxy == null) {
                return false;
            }
            int tagHandle = this.mTagInfo.getTagHandle();
            if (this.mNfcTagProxy.isNdefTag(tagHandle)) {
                int ndefSetReadOnly = this.mNfcTagProxy.ndefSetReadOnly(tagHandle);
                if (ndefSetReadOnly == -8) {
                    return false;
                }
                if (ndefSetReadOnly == -1) {
                    throw new IOException("IO Exception occured when setReadOnly");
                } else if (ndefSetReadOnly == 0) {
                    return true;
                } else {
                    throw new IOException("IO Exception occured when setReadOnly");
                }
            } else {
                throw new IOException("Tag is not NDEF.");
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "setReadOnly open RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean isNdefWritable() {
        return this.mCardState == 2;
    }
}
