package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$nn2WcbgVnsgm1uNJ7q-GRyQlb34  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$nn2WcbgVnsgm1uNJ7qGRyQlb34 implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$nn2WcbgVnsgm1uNJ7qGRyQlb34 INSTANCE = new $$Lambda$ModeFunctionUtil$nn2WcbgVnsgm1uNJ7qGRyQlb34();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$nn2WcbgVnsgm1uNJ7qGRyQlb34() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addFlashModeCapability(modeAbilityImpl, cameraAbilityImpl, ModeNameUtil.getModeNameById(i));
    }
}
