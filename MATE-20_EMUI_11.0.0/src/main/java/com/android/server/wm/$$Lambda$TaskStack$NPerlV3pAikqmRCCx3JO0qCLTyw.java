package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$TaskStack$NPerlV3pAikqmRCCx3JO0qCLTyw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskStack$NPerlV3pAikqmRCCx3JO0qCLTyw implements Consumer {
    public static final /* synthetic */ $$Lambda$TaskStack$NPerlV3pAikqmRCCx3JO0qCLTyw INSTANCE = new $$Lambda$TaskStack$NPerlV3pAikqmRCCx3JO0qCLTyw();

    private /* synthetic */ $$Lambda$TaskStack$NPerlV3pAikqmRCCx3JO0qCLTyw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WindowState) obj).mWinAnimator.resetDrawState();
    }
}
