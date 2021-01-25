package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$AnXxcdbN5OxzVry-U-EkGmASvng  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$AnXxcdbN5OxzVryUEkGmASvng implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$AnXxcdbN5OxzVryUEkGmASvng INSTANCE = new $$Lambda$ModeFunctionUtil$AnXxcdbN5OxzVryUEkGmASvng();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$AnXxcdbN5OxzVryUEkGmASvng() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addFilterEffectCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
