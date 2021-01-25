package android.test;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.test.mock.MockAccountManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Deprecated
public class IsolatedContext extends ContextWrapper {
    private List<Intent> mBroadcastIntents = new ArrayList();
    private final AccountManager mMockAccountManager;
    private ContentResolver mResolver;

    public IsolatedContext(ContentResolver resolver, Context targetContext) {
        super(targetContext);
        this.mResolver = resolver;
        this.mMockAccountManager = MockAccountManager.newMockAccountManager(this);
    }

    public List<Intent> getAndClearBroadcastIntents() {
        List<Intent> intents = this.mBroadcastIntents;
        this.mBroadcastIntents = new ArrayList();
        return intents;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public ContentResolver getContentResolver() {
        return this.mResolver;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return false;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean bindService(Intent service, int flags, Executor executor, ServiceConnection conn) {
        return false;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean bindIsolatedService(Intent service, int flags, String instanceName, Executor executor, ServiceConnection conn) {
        return false;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void unregisterReceiver(BroadcastReceiver receiver) {
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void sendBroadcast(Intent intent) {
        this.mBroadcastIntents.add(intent);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        this.mBroadcastIntents.add(intent);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Object getSystemService(String name) {
        if ("account".equals(name)) {
            return this.mMockAccountManager;
        }
        return null;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public File getFilesDir() {
        return new File("/dev/null");
    }
}
