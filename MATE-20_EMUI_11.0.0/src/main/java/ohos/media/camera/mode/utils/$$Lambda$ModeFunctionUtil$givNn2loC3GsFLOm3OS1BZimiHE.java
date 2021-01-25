package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$givNn2loC3GsFLOm3OS1BZimiHE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$givNn2loC3GsFLOm3OS1BZimiHE implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$givNn2loC3GsFLOm3OS1BZimiHE INSTANCE = new $$Lambda$ModeFunctionUtil$givNn2loC3GsFLOm3OS1BZimiHE();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$givNn2loC3GsFLOm3OS1BZimiHE() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addVideoStabilizationCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
