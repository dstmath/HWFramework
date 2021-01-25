package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$W7rMRe_63vP8bzUshnohWhgTkYg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$W7rMRe_63vP8bzUshnohWhgTkYg implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$W7rMRe_63vP8bzUshnohWhgTkYg INSTANCE = new $$Lambda$ModeFunctionUtil$W7rMRe_63vP8bzUshnohWhgTkYg();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$W7rMRe_63vP8bzUshnohWhgTkYg() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addColorModeCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
