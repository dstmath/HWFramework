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

    void format(NdefMessage firstMessage, boolean makeReadOnly) throws IOException, FormatException {
        checkConnected();
        try {
            int serviceHandle = this.mTag.getServiceHandle();
            INfcTag tagService = this.mTag.getTagService();
            switch (tagService.formatNdef(serviceHandle, MifareClassic.KEY_DEFAULT)) {
                case -8:
                    throw new FormatException();
                case -1:
                    throw new IOException();
                case 0:
                    if (tagService.isNdef(serviceHandle)) {
                        if (firstMessage != null) {
                            switch (tagService.ndefWrite(serviceHandle, firstMessage)) {
                                case -8:
                                    throw new FormatException();
                                case -1:
                                    throw new IOException();
                                case 0:
                                    break;
                                default:
                                    throw new IOException();
                            }
                        }
                        if (makeReadOnly) {
                            switch (tagService.ndefMakeReadOnly(serviceHandle)) {
                                case -8:
                                    throw new IOException();
                                case -1:
                                    throw new IOException();
                                case 0:
                                    return;
                                default:
                                    throw new IOException();
                            }
                        }
                        return;
                    }
                    throw new IOException();
                default:
                    throw new IOException();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
        Log.e(TAG, "NFC service dead", e);
    }
}
