package com.android.server.rollback;

import android.content.Context;
import com.android.server.SystemService;

public final class RollbackManagerService extends SystemService {
    private RollbackManagerServiceImpl mService;

    public RollbackManagerService(Context context) {
        super(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.rollback.RollbackManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.rollback.RollbackManagerServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        this.mService = new RollbackManagerServiceImpl(getContext());
        publishBinderService("rollback", this.mService);
    }

    @Override // com.android.server.SystemService
    public void onUnlockUser(int user) {
        this.mService.onUnlockUser(user);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 1000) {
            this.mService.onBootCompleted();
        }
    }
}
