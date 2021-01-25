package com.android.server.wifi.hotspot2.soap.command;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.hotspot2.soap.command.SppCommand;
import java.util.Objects;
import org.ksoap2.serialization.PropertyInfo;

public class BrowserUri implements SppCommand.SppCommandData {
    private static final String TAG = "PasspointBrowserUri";
    private final String mUri;

    private BrowserUri(PropertyInfo command) {
        this.mUri = command.getValue().toString();
    }

    public static BrowserUri createInstance(PropertyInfo command) {
        if (TextUtils.equals(command.getName(), "launchBrowserToURI")) {
            return new BrowserUri(command);
        }
        Log.e(TAG, "received wrong command : " + command.getName());
        return null;
    }

    public String getUri() {
        return this.mUri;
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof BrowserUri)) {
            return false;
        }
        return TextUtils.equals(this.mUri, ((BrowserUri) thatObject).mUri);
    }

    public int hashCode() {
        return Objects.hash(this.mUri);
    }

    public String toString() {
        return "BrowserUri{mUri: " + this.mUri + "}";
    }
}
