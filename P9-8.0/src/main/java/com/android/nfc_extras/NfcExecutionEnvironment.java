package com.android.nfc_extras;

import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import java.io.IOException;

public class NfcExecutionEnvironment {
    public static final String ACTION_AID_SELECTED = "com.android.nfc_extras.action.AID_SELECTED";
    public static final String ACTION_APDU_RECEIVED = "com.android.nfc_extras.action.APDU_RECEIVED";
    public static final String ACTION_EMV_CARD_REMOVAL = "com.android.nfc_extras.action.EMV_CARD_REMOVAL";
    public static final String ACTION_MIFARE_ACCESS_DETECTED = "com.android.nfc_extras.action.MIFARE_ACCESS_DETECTED";
    private static final int EE_ERROR_ALREADY_OPEN = -2;
    private static final int EE_ERROR_EXT_FIELD = -5;
    private static final int EE_ERROR_INIT = -3;
    private static final int EE_ERROR_IO = -1;
    private static final int EE_ERROR_LISTEN_MODE = -4;
    private static final int EE_ERROR_NFC_DISABLED = -6;
    public static final String EXTRA_AID = "com.android.nfc_extras.extra.AID";
    public static final String EXTRA_APDU_BYTES = "com.android.nfc_extras.extra.APDU_BYTES";
    public static final String EXTRA_MIFARE_BLOCK = "com.android.nfc_extras.extra.MIFARE_BLOCK";
    private final NfcAdapterExtras mExtras;
    private final Binder mToken = new Binder();

    NfcExecutionEnvironment(NfcAdapterExtras extras) {
        this.mExtras = extras;
    }

    public void open() throws EeIOException {
        try {
            throwBundle(this.mExtras.getService().open(this.mExtras.mPackageName, this.mToken));
        } catch (RemoteException e) {
            this.mExtras.attemptDeadServiceRecovery(e);
            throw new EeIOException("NFC Service was dead, try again");
        }
    }

    public void close() throws IOException {
        try {
            throwBundle(this.mExtras.getService().close(this.mExtras.mPackageName, this.mToken));
        } catch (RemoteException e) {
            this.mExtras.attemptDeadServiceRecovery(e);
            throw new IOException("NFC Service was dead");
        }
    }

    public byte[] transceive(byte[] in) throws IOException {
        try {
            Bundle b = this.mExtras.getService().transceive(this.mExtras.mPackageName, in);
            throwBundle(b);
            return b.getByteArray("out");
        } catch (RemoteException e) {
            this.mExtras.attemptDeadServiceRecovery(e);
            throw new IOException("NFC Service was dead, need to re-open");
        }
    }

    private static void throwBundle(Bundle b) throws EeIOException {
        switch (b.getInt("e")) {
            case EE_ERROR_NFC_DISABLED /*-6*/:
                throw new EeNfcDisabledException(b.getString("m"));
            case EE_ERROR_EXT_FIELD /*-5*/:
                throw new EeExternalFieldException(b.getString("m"));
            case EE_ERROR_LISTEN_MODE /*-4*/:
                throw new EeListenModeException(b.getString("m"));
            case EE_ERROR_INIT /*-3*/:
                throw new EeInitializationException(b.getString("m"));
            case EE_ERROR_ALREADY_OPEN /*-2*/:
                throw new EeAlreadyOpenException(b.getString("m"));
            case EE_ERROR_IO /*-1*/:
                throw new EeIOException(b.getString("m"));
            default:
                return;
        }
    }
}
