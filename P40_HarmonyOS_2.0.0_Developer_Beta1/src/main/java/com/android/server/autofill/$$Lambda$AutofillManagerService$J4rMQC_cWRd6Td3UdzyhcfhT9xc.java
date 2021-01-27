package com.android.server.autofill;

import com.android.server.infra.AbstractMasterSystemService;

/* renamed from: com.android.server.autofill.-$$Lambda$AutofillManagerService$J4rMQC_cWRd6Td3UdzyhcfhT9xc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AutofillManagerService$J4rMQC_cWRd6Td3UdzyhcfhT9xc implements AbstractMasterSystemService.Visitor {
    public static final /* synthetic */ $$Lambda$AutofillManagerService$J4rMQC_cWRd6Td3UdzyhcfhT9xc INSTANCE = new $$Lambda$AutofillManagerService$J4rMQC_cWRd6Td3UdzyhcfhT9xc();

    private /* synthetic */ $$Lambda$AutofillManagerService$J4rMQC_cWRd6Td3UdzyhcfhT9xc() {
    }

    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
    public final void visit(Object obj) {
        ((AutofillManagerServiceImpl) obj).destroySessionsLocked();
    }
}
