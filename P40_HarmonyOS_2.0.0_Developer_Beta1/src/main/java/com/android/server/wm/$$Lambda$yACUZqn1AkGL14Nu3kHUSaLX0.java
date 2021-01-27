package com.android.server.wm;

import android.view.SurfaceControl;
import com.android.server.wm.Dimmer;

/* renamed from: com.android.server.wm.-$$Lambda$yACUZqn1Ak-GL14-Nu3kHUSaLX0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$yACUZqn1AkGL14Nu3kHUSaLX0 implements Dimmer.SurfaceAnimatorStarter {
    public static final /* synthetic */ $$Lambda$yACUZqn1AkGL14Nu3kHUSaLX0 INSTANCE = new $$Lambda$yACUZqn1AkGL14Nu3kHUSaLX0();

    private /* synthetic */ $$Lambda$yACUZqn1AkGL14Nu3kHUSaLX0() {
    }

    @Override // com.android.server.wm.Dimmer.SurfaceAnimatorStarter
    public final void startAnimation(SurfaceAnimator surfaceAnimator, SurfaceControl.Transaction transaction, AnimationAdapter animationAdapter, boolean z) {
        surfaceAnimator.startAnimation(transaction, animationAdapter, z);
    }
}
