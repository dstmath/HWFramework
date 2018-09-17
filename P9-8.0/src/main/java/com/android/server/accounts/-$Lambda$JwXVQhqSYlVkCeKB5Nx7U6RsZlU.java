package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountManagerInternal.OnAppPermissionChangeListener;
import android.content.pm.PackageManager.OnPermissionsChangedListener;

final /* synthetic */ class -$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU implements OnPermissionsChangedListener {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((OnAppPermissionChangeListener) this.-$f1).onAppPermissionChanged((Account) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((AccountManagerService) this.-$f1).lambda$-com_android_server_accounts_AccountManagerService_106115((Account) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass2(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((OnAppPermissionChangeListener) this.-$f1).onAppPermissionChanged((Account) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass3(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0(int arg0) {
        ((AccountManagerService) this.-$f0).lambda$-com_android_server_accounts_AccountManagerService_16015(arg0);
    }

    public /* synthetic */ -$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU(Object obj) {
        this.-$f0 = obj;
    }

    public final void onPermissionsChanged(int i) {
        $m$0(i);
    }
}
