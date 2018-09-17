package android.test;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import com.google.android.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Deprecated
public class IsolatedContext extends ContextWrapper {
    private List<Intent> mBroadcastIntents = Lists.newArrayList();
    private final MockAccountManager mMockAccountManager;
    private ContentResolver mResolver;

    private class MockAccountManager extends AccountManager {

        private class MockAccountManagerFuture<T> implements AccountManagerFuture<T> {
            T mResult;

            public MockAccountManagerFuture(T result) {
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

        public MockAccountManager() {
            super(IsolatedContext.this, null, null);
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

    public IsolatedContext(ContentResolver resolver, Context targetContext) {
        super(targetContext);
        this.mResolver = resolver;
        this.mMockAccountManager = new MockAccountManager();
    }

    public List<Intent> getAndClearBroadcastIntents() {
        List<Intent> intents = this.mBroadcastIntents;
        this.mBroadcastIntents = Lists.newArrayList();
        return intents;
    }

    public ContentResolver getContentResolver() {
        return this.mResolver;
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return false;
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
    }

    public void sendBroadcast(Intent intent) {
        this.mBroadcastIntents.add(intent);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        this.mBroadcastIntents.add(intent);
    }

    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    public Object getSystemService(String name) {
        if ("account".equals(name)) {
            return this.mMockAccountManager;
        }
        return null;
    }

    public File getFilesDir() {
        return new File("/dev/null");
    }
}
