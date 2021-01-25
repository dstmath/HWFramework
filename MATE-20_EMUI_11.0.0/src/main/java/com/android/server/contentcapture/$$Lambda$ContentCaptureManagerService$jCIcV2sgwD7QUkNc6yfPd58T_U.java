package com.android.server.contentcapture;

import com.android.server.infra.AbstractMasterSystemService;

/* renamed from: com.android.server.contentcapture.-$$Lambda$ContentCaptureManagerService$jCIcV2sgwD7QUkN-c6yfPd58T_U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureManagerService$jCIcV2sgwD7QUkNc6yfPd58T_U implements AbstractMasterSystemService.Visitor {
    public static final /* synthetic */ $$Lambda$ContentCaptureManagerService$jCIcV2sgwD7QUkNc6yfPd58T_U INSTANCE = new $$Lambda$ContentCaptureManagerService$jCIcV2sgwD7QUkNc6yfPd58T_U();

    private /* synthetic */ $$Lambda$ContentCaptureManagerService$jCIcV2sgwD7QUkNc6yfPd58T_U() {
    }

    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
    public final void visit(Object obj) {
        ((ContentCapturePerUserService) obj).destroySessionsLocked();
    }
}
