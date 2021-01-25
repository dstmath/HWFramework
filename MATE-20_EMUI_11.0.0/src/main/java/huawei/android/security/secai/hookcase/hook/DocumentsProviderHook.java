package huawei.android.security.secai.hookcase.hook;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.DocumentsProvider;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class DocumentsProviderHook {
    private static final String TAG = DocumentsProviderHook.class.getSimpleName();

    DocumentsProviderHook() {
    }

    @HookMethod(name = "query", params = {Uri.class, String[].class, Bundle.class, CancellationSignal.class}, targetClass = DocumentsProvider.class)
    static Cursor queryHook(Object obj, Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.DOCUMENT_DOCUMENTQUERY.getValue());
        Log.i(TAG, "Call System Hook Method: DocumentsProvider queryHook()");
        return queryBackup(obj, uri, projection, queryArgs, cancellationSignal);
    }

    @BackupMethod(name = "query", params = {Uri.class, String[].class, Bundle.class, CancellationSignal.class}, targetClass = DocumentsProvider.class)
    static Cursor queryBackup(Object obj, Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: DocumentsProvider queryBackup().");
        return null;
    }
}
