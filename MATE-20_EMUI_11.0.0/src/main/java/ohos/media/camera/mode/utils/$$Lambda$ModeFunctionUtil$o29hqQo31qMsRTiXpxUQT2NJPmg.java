package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$o29hqQo31qMsRTiXpxUQT2NJPmg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$o29hqQo31qMsRTiXpxUQT2NJPmg implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$o29hqQo31qMsRTiXpxUQT2NJPmg INSTANCE = new $$Lambda$ModeFunctionUtil$o29hqQo31qMsRTiXpxUQT2NJPmg();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$o29hqQo31qMsRTiXpxUQT2NJPmg() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addWaterMarkCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
