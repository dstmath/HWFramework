package com.gsma.services.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;
import java.io.IOException;

@Deprecated
public class SEController {
    private static Context mContext = null;

    public interface Callbacks {
        void onGetDefaultController(SEController sEController);
    }

    SEController() {
    }

    @Deprecated
    public static void getDefaultController(Context context, Callbacks cb) throws IOException {
        mContext = context;
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String getActiveSecureElement() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setActiveSecureElement(String SEName) throws IllegalStateException, SecurityException, UnsupportedOperationException {
        if (!NfcAdapter.getNfcAdapter(mContext).isEnabled()) {
            throw new IllegalStateException("Nfc not enabled");
        } else if (canUseApi()) {
            throw new UnsupportedOperationException();
        } else {
            throw new SecurityException("Can not use this API");
        }
    }

    private boolean canUseApi() {
        return false;
    }
}
