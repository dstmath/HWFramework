package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$g13UC4Ls8jHyifxt_90ZdZm2A6w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$g13UC4Ls8jHyifxt_90ZdZm2A6w implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$g13UC4Ls8jHyifxt_90ZdZm2A6w INSTANCE = new $$Lambda$ModeFunctionUtil$g13UC4Ls8jHyifxt_90ZdZm2A6w();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$g13UC4Ls8jHyifxt_90ZdZm2A6w() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addFaceDetectionCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
