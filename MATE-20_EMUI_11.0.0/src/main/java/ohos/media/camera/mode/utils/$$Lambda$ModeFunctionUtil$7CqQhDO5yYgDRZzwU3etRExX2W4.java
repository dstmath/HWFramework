package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$7CqQhDO5yYgDRZzwU3etRExX2W4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$7CqQhDO5yYgDRZzwU3etRExX2W4 implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$7CqQhDO5yYgDRZzwU3etRExX2W4 INSTANCE = new $$Lambda$ModeFunctionUtil$7CqQhDO5yYgDRZzwU3etRExX2W4();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$7CqQhDO5yYgDRZzwU3etRExX2W4() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addMirrorCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
