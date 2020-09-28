package com.mediatek.gba;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class GbaHttpUrlCredential extends GbaBaseCredential {
    private static final String TAG = "GbaCredentials";
    private Authenticator mAuthenticator;

    public GbaHttpUrlCredential(Context context, String nafAddress) {
        this(context, nafAddress, -1);
    }

    public GbaHttpUrlCredential(Context context, String nafAddress, int subId) {
        super(context, nafAddress, subId);
        this.mAuthenticator = new GbaAuthenticator();
        System.setProperty("http.digest.support", "true");
    }

    public Authenticator getAuthenticator() {
        return this.mAuthenticator;
    }

    private class GbaAuthenticator extends Authenticator {
        private PasswordAuthentication mPasswordAuthentication;

        private GbaAuthenticator() {
        }

        /* access modifiers changed from: protected */
        public PasswordAuthentication getPasswordAuthentication() {
            Log.i(GbaHttpUrlCredential.TAG, "getPasswordAuthentication");
            Log.i(GbaHttpUrlCredential.TAG, "Run GBA procedure");
            NafSessionKey nafSessionKey = GbaHttpUrlCredential.this.getNafSessionKey();
            if (nafSessionKey == null || nafSessionKey.getKey() == null) {
                return null;
            }
            this.mPasswordAuthentication = new PasswordAuthentication(nafSessionKey.getBtid(), Base64.encodeToString(nafSessionKey.getKey(), 2).toCharArray());
            return this.mPasswordAuthentication;
        }
    }
}
