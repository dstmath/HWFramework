package android.test.mock;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Handler;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Deprecated
public class MockAccountManager {

    private static class MockAccountManagerFuture<T> implements AccountManagerFuture<T> {
        T mResult;

        MockAccountManagerFuture(T result) {
            this.mResult = result;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }

        public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return this.mResult;
        }

        public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return getResult();
        }
    }

    private static class MockAccountManagerImpl extends AccountManager {
        MockAccountManagerImpl(Context context) {
            super(context, null, null);
        }

        public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately) {
        }

        public Account[] getAccounts() {
            return new Account[0];
        }

        public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(String type, String[] features, AccountManagerCallback<Account[]> accountManagerCallback, Handler handler) {
            return new MockAccountManagerFuture(new Account[0]);
        }

        public String blockingGetAuthToken(Account account, String authTokenType, boolean notifyAuthFailure) throws OperationCanceledException, IOException, AuthenticatorException {
            return null;
        }
    }

    public static AccountManager newMockAccountManager(Context context) {
        return new MockAccountManagerImpl(context);
    }

    private MockAccountManager() {
    }
}
