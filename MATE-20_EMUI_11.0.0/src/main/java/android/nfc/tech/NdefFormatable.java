package android.nfc.tech;

import android.nfc.FormatException;
import android.nfc.INfcTag;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class NdefFormatable extends BasicTagTechnology {
    private static final String TAG = "NFC";

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology, java.io.Closeable, java.lang.AutoCloseable
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

    public static NdefFormatable get(Tag tag) {
        if (!tag.hasTech(7)) {
            return null;
        }
        try {
            return new NdefFormatable(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NdefFormatable(Tag tag) throws RemoteException {
        super(tag, 7);
    }

    public void format(NdefMessage firstMessage) throws IOException, FormatException {
        format(firstMessage, false);
    }

    public void formatReadOnly(NdefMessage firstMessage) throws IOException, FormatException {
        format(firstMessage, true);
    }

    /* access modifiers changed from: package-private */
    public void format(NdefMessage firstMessage, boolean makeReadOnly) throws IOException, FormatException {
        checkConnected();
        try {
            int serviceHandle = this.mTag.getServiceHandle();
            INfcTag tagService = this.mTag.getTagService();
            int errorCode = tagService.formatNdef(serviceHandle, MifareClassic.KEY_DEFAULT);
            if (errorCode == -8) {
                throw new FormatException();
            } else if (errorCode == -1) {
                throw new IOException();
            } else if (errorCode != 0) {
                throw new IOException();
            } else if (tagService.isNdef(serviceHandle)) {
                if (firstMessage != null) {
                    int errorCode2 = tagService.ndefWrite(serviceHandle, firstMessage);
                    if (errorCode2 == -8) {
                        throw new FormatException();
                    } else if (errorCode2 == -1) {
                        throw new IOException();
                    } else if (errorCode2 != 0) {
                        throw new IOException();
                    }
                }
                if (makeReadOnly) {
                    int errorCode3 = tagService.ndefMakeReadOnly(serviceHandle);
                    if (errorCode3 == -8) {
                        throw new IOException();
                    } else if (errorCode3 == -1) {
                        throw new IOException();
                    } else if (errorCode3 != 0) {
                        throw new IOException();
                    }
                }
            } else {
                throw new IOException();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }
}
