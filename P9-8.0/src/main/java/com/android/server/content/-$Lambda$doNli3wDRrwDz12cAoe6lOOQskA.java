package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountManagerInternal.OnAppPermissionChangeListener;
import android.os.Bundle;
import android.os.RemoteCallback.OnResultListener;
import com.android.server.content.SyncStorageEngine.EndPoint;

final /* synthetic */ class -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA implements OnAppPermissionChangeListener {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA$1 */
    final /* synthetic */ class AnonymousClass1 implements OnResultListener {
        private final /* synthetic */ long -$f0;
        private final /* synthetic */ long -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0(Bundle arg0) {
            ((SyncHandler) this.-$f2).lambda$-com_android_server_content_SyncManager$SyncHandler_128357((EndPoint) this.-$f3, this.-$f0, this.-$f1, (Bundle) this.-$f4, arg0);
        }

        public /* synthetic */ AnonymousClass1(long j, long j2, Object obj, Object obj2, Object obj3) {
            this.-$f0 = j;
            this.-$f1 = j2;
            this.-$f2 = obj;
            this.-$f3 = obj2;
            this.-$f4 = obj3;
        }

        public final void onResult(Bundle bundle) {
            $m$0(bundle);
        }
    }

    /* renamed from: com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA$2 */
    final /* synthetic */ class AnonymousClass2 implements OnResultListener {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ int -$f2;
        private final /* synthetic */ long -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;
        private final /* synthetic */ Object -$f7;

        private final /* synthetic */ void $m$0(Bundle arg0) {
            ((SyncManager) this.-$f4).lambda$-com_android_server_content_SyncManager_40478((AccountAndUser) this.-$f5, this.-$f0, this.-$f1, (String) this.-$f6, (Bundle) this.-$f7, this.-$f2, this.-$f3, arg0);
        }

        public /* synthetic */ AnonymousClass2(int i, int i2, int i3, long j, Object obj, Object obj2, Object obj3, Object obj4) {
            this.-$f0 = i;
            this.-$f1 = i2;
            this.-$f2 = i3;
            this.-$f3 = j;
            this.-$f4 = obj;
            this.-$f5 = obj2;
            this.-$f6 = obj3;
            this.-$f7 = obj4;
        }

        public final void onResult(Bundle bundle) {
            $m$0(bundle);
        }
    }

    private final /* synthetic */ void $m$0(Account arg0, int arg1) {
        ((SyncManager) this.-$f0).lambda$-com_android_server_content_SyncManager_23978(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA(Object obj) {
        this.-$f0 = obj;
    }

    public final void onAppPermissionChanged(Account account, int i) {
        $m$0(account, i);
    }
}
