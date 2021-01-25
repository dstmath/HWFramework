package com.android.server.autofill;

import com.android.server.infra.AbstractMasterSystemService;

/* renamed from: com.android.server.autofill.-$$Lambda$AutofillManagerService$1$1-WNu3tTkxodB_LsZ7dGIlvrPN0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AutofillManagerService$1$1WNu3tTkxodB_LsZ7dGIlvrPN0 implements AbstractMasterSystemService.Visitor {
    public static final /* synthetic */ $$Lambda$AutofillManagerService$1$1WNu3tTkxodB_LsZ7dGIlvrPN0 INSTANCE = new $$Lambda$AutofillManagerService$1$1WNu3tTkxodB_LsZ7dGIlvrPN0();

    private /* synthetic */ $$Lambda$AutofillManagerService$1$1WNu3tTkxodB_LsZ7dGIlvrPN0() {
    }

    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
    public final void visit(Object obj) {
        ((AutofillManagerServiceImpl) obj).destroyFinishedSessionsLocked();
    }
}
