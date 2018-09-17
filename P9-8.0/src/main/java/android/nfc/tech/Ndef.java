package android.nfc.tech;

import android.nfc.FormatException;
import android.nfc.INfcTag;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class Ndef extends BasicTagTechnology {
    public static final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";
    public static final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";
    public static final String EXTRA_NDEF_MSG = "ndefmsg";
    public static final String EXTRA_NDEF_TYPE = "ndeftype";
    public static final String ICODE_SLI = "com.nxp.ndef.icodesli";
    public static final String MIFARE_CLASSIC = "com.nxp.ndef.mifareclassic";
    public static final int NDEF_MODE_READ_ONLY = 1;
    public static final int NDEF_MODE_READ_WRITE = 2;
    public static final int NDEF_MODE_UNKNOWN = 3;
    public static final String NFC_FORUM_TYPE_1 = "org.nfcforum.ndef.type1";
    public static final String NFC_FORUM_TYPE_2 = "org.nfcforum.ndef.type2";
    public static final String NFC_FORUM_TYPE_3 = "org.nfcforum.ndef.type3";
    public static final String NFC_FORUM_TYPE_4 = "org.nfcforum.ndef.type4";
    private static final String TAG = "NFC";
    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;
    public static final int TYPE_3 = 3;
    public static final int TYPE_4 = 4;
    public static final int TYPE_ICODE_SLI = 102;
    public static final int TYPE_MIFARE_CLASSIC = 101;
    public static final int TYPE_OTHER = -1;
    public static final String UNKNOWN = "android.ndef.unknown";
    private final int mCardState;
    private final int mMaxNdefSize;
    private final NdefMessage mNdefMsg;
    private final int mNdefType;

    public static Ndef get(Tag tag) {
        if (!tag.hasTech(6)) {
            return null;
        }
        try {
            return new Ndef(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public Ndef(Tag tag) throws RemoteException {
        super(tag, 6);
        Bundle extras = tag.getTechExtras(6);
        if (extras != null) {
            this.mMaxNdefSize = extras.getInt(EXTRA_NDEF_MAXLENGTH);
            this.mCardState = extras.getInt(EXTRA_NDEF_CARDSTATE);
            this.mNdefMsg = (NdefMessage) extras.getParcelable(EXTRA_NDEF_MSG);
            this.mNdefType = extras.getInt(EXTRA_NDEF_TYPE);
            return;
        }
        throw new NullPointerException("NDEF tech extras are null.");
    }

    public NdefMessage getCachedNdefMessage() {
        return this.mNdefMsg;
    }

    public String getType() {
        switch (this.mNdefType) {
            case 1:
                return NFC_FORUM_TYPE_1;
            case 2:
                return NFC_FORUM_TYPE_2;
            case 3:
                return NFC_FORUM_TYPE_3;
            case 4:
                return NFC_FORUM_TYPE_4;
            case 101:
                return MIFARE_CLASSIC;
            case 102:
                return ICODE_SLI;
            default:
                return UNKNOWN;
        }
    }

    public int getMaxSize() {
        return this.mMaxNdefSize;
    }

    public boolean isWritable() {
        return this.mCardState == 2;
    }

    public NdefMessage getNdefMessage() throws IOException, FormatException {
        checkConnected();
        try {
            INfcTag tagService = this.mTag.getTagService();
            if (tagService == null) {
                throw new IOException("Mock tags don't support this operation.");
            }
            int serviceHandle = this.mTag.getServiceHandle();
            if (tagService.isNdef(serviceHandle)) {
                NdefMessage msg = tagService.ndefRead(serviceHandle);
                if (msg != null || (tagService.isPresent(serviceHandle) ^ 1) == 0) {
                    return msg;
                }
                throw new TagLostException();
            } else if (tagService.isPresent(serviceHandle)) {
                return null;
            } else {
                throw new TagLostException();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return null;
        }
    }

    public void writeNdefMessage(NdefMessage msg) throws IOException, FormatException {
        checkConnected();
        try {
            INfcTag tagService = this.mTag.getTagService();
            if (tagService == null) {
                throw new IOException("Mock tags don't support this operation.");
            }
            int serviceHandle = this.mTag.getServiceHandle();
            if (tagService.isNdef(serviceHandle)) {
                switch (tagService.ndefWrite(serviceHandle, msg)) {
                    case -8:
                        throw new FormatException();
                    case -1:
                        throw new IOException();
                    case 0:
                        return;
                    default:
                        throw new IOException();
                }
            }
            throw new IOException("Tag is not ndef");
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    public boolean canMakeReadOnly() {
        INfcTag tagService = this.mTag.getTagService();
        if (tagService == null) {
            return false;
        }
        try {
            return tagService.canMakeReadOnly(this.mNdefType);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return false;
        }
    }

    public boolean makeReadOnly() throws IOException {
        checkConnected();
        try {
            INfcTag tagService = this.mTag.getTagService();
            if (tagService == null) {
                return false;
            }
            if (tagService.isNdef(this.mTag.getServiceHandle())) {
                switch (tagService.ndefMakeReadOnly(this.mTag.getServiceHandle())) {
                    case -8:
                        return false;
                    case -1:
                        throw new IOException();
                    case 0:
                        return true;
                    default:
                        throw new IOException();
                }
                Log.e(TAG, "NFC service dead", e);
                return false;
            }
            throw new IOException("Tag is not ndef");
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return false;
        }
    }
}
