package android.nfc.tech;

import android.nfc.FormatException;
import android.nfc.INfcTag;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.io.IOException;

public final class NdefFormatable extends BasicTagTechnology {
    private static final String TAG = "NFC";

    public /* bridge */ /* synthetic */ void close() {
        super.close();
    }

    public /* bridge */ /* synthetic */ void connect() {
        super.connect();
    }

    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    public /* bridge */ /* synthetic */ void reconnect() {
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

    void format(NdefMessage firstMessage, boolean makeReadOnly) throws IOException, FormatException {
        checkConnected();
        try {
            int serviceHandle = this.mTag.getServiceHandle();
            INfcTag tagService = this.mTag.getTagService();
            switch (tagService.formatNdef(serviceHandle, MifareClassic.KEY_DEFAULT)) {
                case TextToSpeech.ERROR_INVALID_REQUEST /*-8*/:
                    throw new FormatException();
                case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                    throw new IOException();
                case TextToSpeech.SUCCESS /*0*/:
                    if (tagService.isNdef(serviceHandle)) {
                        if (firstMessage != null) {
                            switch (tagService.ndefWrite(serviceHandle, firstMessage)) {
                                case TextToSpeech.ERROR_INVALID_REQUEST /*-8*/:
                                    throw new FormatException();
                                case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                                    throw new IOException();
                                case TextToSpeech.SUCCESS /*0*/:
                                    break;
                                default:
                                    throw new IOException();
                            }
                        }
                        if (makeReadOnly) {
                            switch (tagService.ndefMakeReadOnly(serviceHandle)) {
                                case TextToSpeech.ERROR_INVALID_REQUEST /*-8*/:
                                    throw new IOException();
                                case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                                    throw new IOException();
                                case TextToSpeech.SUCCESS /*0*/:
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
