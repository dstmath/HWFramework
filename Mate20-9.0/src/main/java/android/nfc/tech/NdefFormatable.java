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

    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    public /* bridge */ /* synthetic */ void connect() throws IOException {
        super.connect();
    }

    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

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
            if (errorCode != -8) {
                switch (errorCode) {
                    case -1:
                        throw new IOException();
                    case 0:
                        if (tagService.isNdef(serviceHandle)) {
                            if (firstMessage != null) {
                                int errorCode2 = tagService.ndefWrite(serviceHandle, firstMessage);
                                if (errorCode2 != -8) {
                                    switch (errorCode2) {
                                        case -1:
                                            throw new IOException();
                                        case 0:
                                            break;
                                        default:
                                            throw new IOException();
                                    }
                                } else {
                                    throw new FormatException();
                                }
                            }
                            if (makeReadOnly) {
                                int errorCode3 = tagService.ndefMakeReadOnly(serviceHandle);
                                if (errorCode3 != -8) {
                                    switch (errorCode3) {
                                        case -1:
                                            throw new IOException();
                                        case 0:
                                            break;
                                        default:
                                            throw new IOException();
                                    }
                                } else {
                                    throw new IOException();
                                }
                            }
                            return;
                        }
                        throw new IOException();
                    default:
                        throw new IOException();
                }
            } else {
                throw new FormatException();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }
}
