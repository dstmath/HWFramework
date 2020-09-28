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

    @Override // android.nfc.tech.BasicTagTechnology, java.io.Closeable, android.nfc.tech.TagTechnology, java.lang.AutoCloseable
    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ void connect() throws IOException {
        super.connect();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ void reconnect() throws IOException {
        super.reconnect();
    }

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
        int i = this.mNdefType;
        if (i == 1) {
            return NFC_FORUM_TYPE_1;
        }
        if (i == 2) {
            return NFC_FORUM_TYPE_2;
        }
        if (i == 3) {
            return NFC_FORUM_TYPE_3;
        }
        if (i == 4) {
            return NFC_FORUM_TYPE_4;
        }
        if (i == 101) {
            return MIFARE_CLASSIC;
        }
        if (i != 102) {
            return UNKNOWN;
        }
        return ICODE_SLI;
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
            if (tagService != null) {
                int serviceHandle = this.mTag.getServiceHandle();
                if (tagService.isNdef(serviceHandle)) {
                    NdefMessage msg = tagService.ndefRead(serviceHandle);
                    if (msg == null) {
                        if (!tagService.isPresent(serviceHandle)) {
                            throw new TagLostException();
                        }
                    }
                    return msg;
                } else if (tagService.isPresent(serviceHandle)) {
                    return null;
                } else {
                    throw new TagLostException();
                }
            } else {
                throw new IOException("Mock tags don't support this operation.");
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
            if (tagService != null) {
                int serviceHandle = this.mTag.getServiceHandle();
                if (tagService.isNdef(serviceHandle)) {
                    int errorCode = tagService.ndefWrite(serviceHandle, msg);
                    if (errorCode == -8) {
                        throw new FormatException();
                    } else if (errorCode == -1) {
                        throw new IOException();
                    } else if (errorCode != 0) {
                        throw new IOException();
                    }
                } else {
                    throw new IOException("Tag is not ndef");
                }
            } else {
                throw new IOException("Mock tags don't support this operation.");
            }
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
                int errorCode = tagService.ndefMakeReadOnly(this.mTag.getServiceHandle());
                if (errorCode == -8) {
                    return false;
                }
                if (errorCode == -1) {
                    throw new IOException();
                } else if (errorCode == 0) {
                    return true;
                } else {
                    throw new IOException();
                }
            } else {
                throw new IOException("Tag is not ndef");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return false;
        }
    }
}
