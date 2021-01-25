package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$bWorJXx4P4Fp-QbwvdnEgec3SFs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$bWorJXx4P4FpQbwvdnEgec3SFs implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$bWorJXx4P4FpQbwvdnEgec3SFs INSTANCE = new $$Lambda$ModeFunctionUtil$bWorJXx4P4FpQbwvdnEgec3SFs();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$bWorJXx4P4FpQbwvdnEgec3SFs() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addSmileDetectionCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
