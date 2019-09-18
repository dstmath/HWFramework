package android.telecom;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    private static Authenticator mAuthenticator;

    public class Authenticator extends AbstractAccountAuthenticator {
        public Authenticator(Context context) {
            super(context);
        }

        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            throw new UnsupportedOperationException();
        }

        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s2, String[] strings, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        public String getAuthTokenLabel(String s) {
            throw new UnsupportedOperationException();
        }

        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }
    }

    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
