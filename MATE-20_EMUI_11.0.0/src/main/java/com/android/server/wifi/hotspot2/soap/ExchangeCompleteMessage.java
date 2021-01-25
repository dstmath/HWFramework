package com.android.server.wifi.hotspot2.soap;

import android.util.Log;
import org.ksoap2.serialization.SoapObject;

public class ExchangeCompleteMessage extends SppResponseMessage {
    private static final String TAG = "PasspointExchangeCompleteMessage";

    private ExchangeCompleteMessage(SoapObject response) throws IllegalArgumentException {
        super(response, 1);
    }

    public static ExchangeCompleteMessage createInstance(SoapObject response) {
        try {
            return new ExchangeCompleteMessage(response);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "fails to create an Instance: " + e);
            return null;
        }
    }
}
