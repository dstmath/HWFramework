package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$5f4daakcHACEHBsxQZFedqT7Gcc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$5f4daakcHACEHBsxQZFedqT7Gcc implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$5f4daakcHACEHBsxQZFedqT7Gcc INSTANCE = new $$Lambda$ModeFunctionUtil$5f4daakcHACEHBsxQZFedqT7Gcc();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$5f4daakcHACEHBsxQZFedqT7Gcc() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addZoomCapability(modeAbilityImpl, cameraAbilityImpl, ModeNameUtil.getModeNameById(i));
    }
}
