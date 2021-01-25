package com.android.server.autofill;

import com.android.server.infra.AbstractMasterSystemService;

/* renamed from: com.android.server.autofill.-$$Lambda$AutofillManagerService$PR6iUwKxXatnzjgBDLARdxaGV3A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AutofillManagerService$PR6iUwKxXatnzjgBDLARdxaGV3A implements AbstractMasterSystemService.Visitor {
    public static final /* synthetic */ $$Lambda$AutofillManagerService$PR6iUwKxXatnzjgBDLARdxaGV3A INSTANCE = new $$Lambda$AutofillManagerService$PR6iUwKxXatnzjgBDLARdxaGV3A();

    private /* synthetic */ $$Lambda$AutofillManagerService$PR6iUwKxXatnzjgBDLARdxaGV3A() {
    }

    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
    public final void visit(Object obj) {
        ((AutofillManagerServiceImpl) obj).destroyLocked();
    }
}
