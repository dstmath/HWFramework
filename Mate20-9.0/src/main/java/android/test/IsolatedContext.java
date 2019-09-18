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
